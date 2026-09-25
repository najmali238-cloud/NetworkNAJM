package com.example.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.AlertSeverity
import com.example.data.model.PushNotificationRecord
import com.example.data.model.TrafficNotificationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enterprise Push Notification Service
 * Monitors network traffic throughput against established thresholds and
 * triggers real-time Android push notifications, heads-up system alerts, and vibration.
 */
class NetworkTrafficNotificationService(
    private val context: Context? = null
) {
    companion object {
        const val CHANNEL_ID = "netguard_traffic_threshold_channel"
        const val CHANNEL_NAME = "Network Traffic & Threshold Alerts"
        private var instance: NetworkTrafficNotificationService? = null

        fun getInstance(context: Context? = null): NetworkTrafficNotificationService {
            if (instance == null) {
                instance = NetworkTrafficNotificationService(context?.applicationContext)
            }
            return instance!!
        }
    }

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _config = MutableStateFlow(TrafficNotificationConfig())
    val config: StateFlow<TrafficNotificationConfig> = _config.asStateFlow()

    private val _notificationHistory = MutableStateFlow<List<PushNotificationRecord>>(
        listOf(
            PushNotificationRecord(
                id = "notif-init-1",
                title = "Network Traffic Threshold Alert",
                message = "Inbound bandwidth surged to 845.2 Mbps, exceeding configured baseline threshold of 600.0 Mbps",
                timestamp = "18:45:10",
                trafficMbps = 845.2f,
                thresholdMbps = 600.0f,
                sourceTarget = "Core Gateway (192.168.1.1)",
                severity = AlertSeverity.CRITICAL,
                isDelivered = true
            ),
            PushNotificationRecord(
                id = "notif-init-2",
                title = "Device Bandwidth Limit Breached",
                message = "Production App Server (Prod-01) egress peaked at 520.4 Mbps (Threshold: 500.0 Mbps)",
                timestamp = "17:30:22",
                trafficMbps = 520.4f,
                thresholdMbps = 500.0f,
                sourceTarget = "Prod-01 (192.168.1.10)",
                severity = AlertSeverity.WARNING,
                isDelivered = true
            )
        )
    )
    val notificationHistory: StateFlow<List<PushNotificationRecord>> = _notificationHistory.asStateFlow()

    private var lastNotificationTimeMs = 0L

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (context == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts dispatched when real-time network traffic or device throughput exceeds configured thresholds"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                enableLights(true)
                lightColor = android.graphics.Color.RED
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Checks whether runtime notification permission is active
     */
    fun hasNotificationPermission(): Boolean {
        if (context == null) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun updateConfig(newConfig: TrafficNotificationConfig) {
        _config.value = newConfig
    }

    fun updateBandwidthThreshold(thresholdMbps: Float) {
        _config.value = _config.value.copy(bandwidthThresholdMbps = thresholdMbps.coerceIn(100f, 2000f))
    }

    fun toggleEnabled(enabled: Boolean) {
        _config.value = _config.value.copy(isEnabled = enabled)
    }

    fun clearHistory() {
        _notificationHistory.value = emptyList()
    }

    /**
     * Evaluates live traffic metrics against established thresholds and
     * triggers system push notification if breached and cooldown elapsed.
     */
    fun checkAndNotifyTraffic(
        currentTrafficMbps: Float,
        sourceLabel: String = "Core Gateway",
        targetThresholdMbps: Float? = null,
        forceBypassCooldown: Boolean = false
    ): Boolean {
        val currentCfg = _config.value
        if (!currentCfg.isEnabled) return false

        val effectiveThreshold = targetThresholdMbps ?: currentCfg.bandwidthThresholdMbps
        if (currentTrafficMbps <= effectiveThreshold) return false

        val now = System.currentTimeMillis()
        val cooldownMs = currentCfg.cooldownSeconds * 1000L
        if (!forceBypassCooldown && (now - lastNotificationTimeMs < cooldownMs)) {
            // In cooldown period to prevent alert flood
            return false
        }
        lastNotificationTimeMs = now

        val title = "⚠️ Network Traffic Threshold Breached!"
        val message = "Throughput on $sourceLabel reached ${Math.round(currentTrafficMbps * 10f) / 10f} Mbps (Exceeds SLA threshold: ${effectiveThreshold.toInt()} Mbps)"

        return sendPushNotification(
            title = title,
            message = message,
            trafficMbps = currentTrafficMbps,
            thresholdMbps = effectiveThreshold,
            sourceTarget = sourceLabel,
            severity = if (currentTrafficMbps > effectiveThreshold * 1.3f) AlertSeverity.CRITICAL else AlertSeverity.WARNING
        )
    }

    /**
     * Dispatches an Android push notification to the system tray and status bar
     */
    fun sendPushNotification(
        title: String,
        message: String,
        trafficMbps: Float,
        thresholdMbps: Float,
        sourceTarget: String,
        severity: AlertSeverity = AlertSeverity.CRITICAL
    ): Boolean {
        val timestamp = timeFormat.format(Date())
        val notifId = (System.currentTimeMillis() % 100000).toInt()

        // 1. Record into internal state flow history
        val record = PushNotificationRecord(
            id = "notif-$notifId",
            title = title,
            message = message,
            timestamp = timestamp,
            trafficMbps = trafficMbps,
            thresholdMbps = thresholdMbps,
            sourceTarget = sourceTarget,
            severity = severity,
            isDelivered = true
        )
        _notificationHistory.value = (listOf(record) + _notificationHistory.value).take(30)
        _config.value = _config.value.copy(
            lastTriggeredTimestamp = timestamp,
            totalTriggeredCount = _config.value.totalTriggeredCount + 1
        )

        // 2. Build and dispatch system notification if context is present
        if (context == null) return true

        return try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "TRAFFIC_ALERTS")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText("$message\n\nThreshold: ${thresholdMbps.toInt()} Mbps | Current: ${trafficMbps.toInt()} Mbps\nTime: $timestamp"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(if (_config.value.vibrate) longArrayOf(0, 300, 150, 300) else null)

            val notificationManager = NotificationManagerCompat.from(context)
            if (hasNotificationPermission()) {
                notificationManager.notify(notifId, builder.build())
            }
            true
        } catch (e: SecurityException) {
            // Handled when permission is pending runtime confirmation
            false
        } catch (e: Exception) {
            false
        }
    }
}
