package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.data.model.Device
import com.example.data.model.DeviceThreshold
import com.example.ui.MainViewModel
import com.example.ui.theme.*

/**
 * Device Thresholds Card ("عتبات الأجهزة")
 * Manages performance and operational SLA thresholds per network device,
 * including latency limits, packet loss limits, CPU/RAM thresholds, and auto-alert triggers.
 */
@Composable
fun DeviceThresholdsCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onPingDevice: ((String) -> Unit)? = null
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDark) { getNetGuardColors(isDark) }

    val deviceThresholds by viewModel.deviceThresholds.collectAsState()
    val devices by viewModel.devices.collectAsState()

    var selectedThresholdForEdit by remember { mutableStateOf<DeviceThreshold?>(null) }
    var showAddNewThresholdDialog by remember { mutableStateOf(false) }

    val breachedCount = deviceThresholds.count { it.isBreached }
    val normalCount = deviceThresholds.size - breachedCount

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_thresholds_card"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (breachedCount > 0) colors.statusAmber else colors.cardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. Header & Summary
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.statusAmber.copy(alpha = 0.15f))
                            .border(1.dp, colors.statusAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = colors.statusAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "إدارة عتبات الأجهزة والحدود التشغيلية" else "DEVICE PERFORMANCE & SECURITY THRESHOLDS",
                            color = colors.statusAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isArabic) "عتبات التأخير، فقدان الحزم، واستنزاف الموارد" else "Latency, Packet Loss & Resource SLA Baselines",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Reset to defaults
                    IconButton(
                        onClick = { viewModel.resetDeviceThresholds() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Defaults",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Add new threshold
                    IconButton(
                        onClick = { showAddNewThresholdDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primaryAccent.copy(alpha = 0.15f)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Threshold",
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = if (isArabic)
                    "تحديد حدود الإنذار المبكر للأجهزة الحيوية: تصعيد التنبيهات تلقائياً عند تجاوز زمن الاستجابة (Latency) أو نسبة فقدان الحزم (Packet Loss) للحدود المقررة."
                else
                    "Configure SLA threshold limits for mission-critical endpoints: automatically trigger warnings or quarantine when latency or packet loss exceeds allowed parameters.",
                color = colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            // ==========================================
            // 2. Metrics Strip (Total, Healthy, Breached)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThresholdMetricBox(
                    label = if (isArabic) "العتبات المقررة" else "Total Rules",
                    value = "${deviceThresholds.size}",
                    color = colors.primaryAccent,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                ThresholdMetricBox(
                    label = if (isArabic) "ضمن النطاق السليم" else "Normal SLA",
                    value = "$normalCount",
                    color = colors.statusGreen,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                ThresholdMetricBox(
                    label = if (isArabic) "تجاوزت الحدود" else "Breached Limits",
                    value = "$breachedCount",
                    color = if (breachedCount > 0) colors.statusCrimson else colors.textSecondary,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = colors.cardBorder, thickness = 0.8.dp)

            // ==========================================
            // 3. List of Device Threshold Items
            // ==========================================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                deviceThresholds.forEach { threshold ->
                    DeviceThresholdItem(
                        threshold = threshold,
                        isDark = isDark,
                        isArabic = isArabic,
                        colors = colors,
                        onEdit = { selectedThresholdForEdit = threshold },
                        onPing = {
                            if (onPingDevice != null) {
                                onPingDevice(threshold.ipAddress)
                            } else {
                                viewModel.pingTarget(threshold.ipAddress)
                            }
                        },
                        onDelete = { viewModel.removeDeviceThreshold(threshold.deviceId) }
                    )
                }
            }
        }
    }

    // ==========================================
    // 4. Edit Threshold Modal Dialog
    // ==========================================
    selectedThresholdForEdit?.let { threshold ->
        DeviceThresholdConfigDialog(
            initialThreshold = threshold,
            isDark = isDark,
            isArabic = isArabic,
            colors = colors,
            onDismiss = { selectedThresholdForEdit = null },
            onSave = { updated ->
                viewModel.updateDeviceThreshold(updated)
                selectedThresholdForEdit = null
            }
        )
    }

    // ==========================================
    // 5. Add New Threshold Dialog
    // ==========================================
    if (showAddNewThresholdDialog) {
        AddNewDeviceThresholdDialog(
            availableDevices = devices,
            existingThresholds = deviceThresholds,
            isDark = isDark,
            isArabic = isArabic,
            colors = colors,
            onDismiss = { showAddNewThresholdDialog = false },
            onAdd = { newThreshold ->
                viewModel.updateDeviceThreshold(newThreshold)
                showAddNewThresholdDialog = false
            }
        )
    }
}

/**
 * Individual Device Threshold Item Card
 */
@Composable
private fun DeviceThresholdItem(
    threshold: DeviceThreshold,
    isDark: Boolean,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onEdit: () -> Unit,
    onPing: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("threshold_item_${threshold.deviceId}"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (threshold.isBreached) colors.statusCrimson else colors.cardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Device Name, IP, and Breach State
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (threshold.isBreached) colors.statusCrimson.copy(alpha = 0.2f)
                                else colors.statusGreen.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (threshold.isBreached) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (threshold.isBreached) colors.statusCrimson else colors.statusGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = threshold.deviceName,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = threshold.ipAddress,
                            color = colors.primaryAccent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Breach indicator badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (threshold.isBreached) colors.statusCrimson.copy(alpha = 0.2f)
                            else colors.statusGreen.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (threshold.isBreached) (if (isArabic) "تم التجاوز!" else "BREACHED") else (if (isArabic) "سليم" else "IN SLA"),
                        color = if (threshold.isBreached) colors.statusCrimson else colors.statusGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Threshold Parameters Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ThresholdParamChip(
                    label = if (isArabic) "التأخير" else "Latency",
                    value = "< ${threshold.maxLatencyMs}ms",
                    color = colors.primaryAccent,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )

                ThresholdParamChip(
                    label = if (isArabic) "فقدان الحزم" else "Loss",
                    value = "< ${threshold.maxPacketLossPercent.toInt()}%",
                    color = colors.statusAmber,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )

                ThresholdParamChip(
                    label = if (isArabic) "المعالج" else "CPU",
                    value = "< ${threshold.maxCpuPercent.toInt()}%",
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )

                ThresholdParamChip(
                    label = if (isArabic) "الذاكرة" else "RAM",
                    value = "< ${threshold.maxRamPercent.toInt()}%",
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
            }

            // Breach reason strip if active
            if (threshold.isBreached && !threshold.breachReason.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.statusCrimson.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${if (isArabic) "سبب التنبيه: " else "Alert: "}${threshold.breachReason} (${threshold.lastBreachedTimestamp ?: ""})",
                        color = colors.statusCrimson,
                        fontSize = 10.sp
                    )
                }
            }

            // Action Buttons: Ping, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent.copy(alpha = 0.15f),
                        contentColor = colors.primaryAccent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("ping_device_btn_${threshold.deviceId}")
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isArabic) "فحص Ping" else "Ping Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(colors.cardBorder)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(36.dp)
                        .testTag("edit_threshold_btn_${threshold.deviceId}")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isArabic) "تعديل العتبة" else "Configure", fontSize = 11.sp)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/**
 * Dialog to Configure / Edit Device Thresholds ("ضبط عتبات الجهاز")
 */
@Composable
fun DeviceThresholdConfigDialog(
    initialThreshold: DeviceThreshold,
    isDark: Boolean,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onDismiss: () -> Unit,
    onSave: (DeviceThreshold) -> Unit
) {
    var maxLatency by remember { mutableStateOf(initialThreshold.maxLatencyMs.toFloat()) }
    var maxPacketLoss by remember { mutableStateOf(initialThreshold.maxPacketLossPercent) }
    var maxCpu by remember { mutableStateOf(initialThreshold.maxCpuPercent) }
    var maxRam by remember { mutableStateOf(initialThreshold.maxRamPercent) }
    var alertOnBreach by remember { mutableStateOf(initialThreshold.alertOnBreach) }
    var autoIsolate by remember { mutableStateOf(initialThreshold.autoIsolateOnBreach) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("threshold_config_dialog"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.statusAmber))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.statusAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = colors.statusAmber, modifier = Modifier.size(20.dp))
                        }

                        Column {
                            Text(
                                text = if (isArabic) "ضبط عتبات الجهاز" else "DEVICE THRESHOLD SETUP",
                                color = colors.statusAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${initialThreshold.deviceName} (${initialThreshold.ipAddress})",
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                // 1. Latency Threshold Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "عتبة زمن الاستجابة (Latency Limit):" else "Max Latency Threshold:",
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${maxLatency.toInt()} ms",
                            color = colors.primaryAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = maxLatency,
                        onValueChange = { maxLatency = it },
                        valueRange = 5f..300f,
                        steps = 29,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primaryAccent,
                            activeTrackColor = colors.primaryAccent,
                            inactiveTrackColor = colors.cardBorder
                        )
                    )
                }

                // 2. Packet Loss Threshold Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "عتبة فقدان الحزم (Packet Loss Limit):" else "Max Packet Loss Limit:",
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${maxPacketLoss.toInt()} %",
                            color = colors.statusAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = maxPacketLoss,
                        onValueChange = { maxPacketLoss = it },
                        valueRange = 0f..25f,
                        steps = 24,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.statusAmber,
                            activeTrackColor = colors.statusAmber,
                            inactiveTrackColor = colors.cardBorder
                        )
                    )
                }

                // 3. CPU Utilization Threshold Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "عتبة استهلاك المعالج (CPU Limit):" else "Max CPU Load Limit:",
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${maxCpu.toInt()} %",
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = maxCpu,
                        onValueChange = { maxCpu = it },
                        valueRange = 40f..95f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primaryAccent,
                            activeTrackColor = colors.primaryAccent,
                            inactiveTrackColor = colors.cardBorder
                        )
                    )
                }

                // 4. RAM Utilization Threshold Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "عتبة استهلاك الذاكرة (RAM Limit):" else "Max RAM Load Limit:",
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${maxRam.toInt()} %",
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = maxRam,
                        onValueChange = { maxRam = it },
                        valueRange = 40f..95f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primaryAccent,
                            activeTrackColor = colors.primaryAccent,
                            inactiveTrackColor = colors.cardBorder
                        )
                    )
                }

                // 5. Switches
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "إرسال تنبيه فوري عند التجاوز" else "Alert on Threshold Breach",
                                color = colors.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isArabic) "تسجيل حدث أمني وإرسال إشعار للنظام" else "Log alert and trigger visual dashboard warning",
                                color = colors.textSecondary,
                                fontSize = 9.sp
                            )
                        }
                        Switch(checked = alertOnBreach, onCheckedChange = { alertOnBreach = it })
                    }

                    Divider(color = colors.cardBorder, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "عزل الجهاز تلقائياً عند التجاوز الحرج" else "Auto-Isolate on Critical Breach",
                                color = colors.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isArabic) "حظر الوصول تلقائياً لحماية بقية الشبكة" else "Quarantine node from gateway if packet loss or latency spikes",
                                color = colors.textSecondary,
                                fontSize = 9.sp
                            )
                        }
                        Switch(checked = autoIsolate, onCheckedChange = { autoIsolate = it })
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isArabic) "إلغاء" else "Cancel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val updated = initialThreshold.copy(
                                maxLatencyMs = maxLatency.toLong(),
                                maxPacketLossPercent = maxPacketLoss,
                                maxCpuPercent = maxCpu,
                                maxRamPercent = maxRam,
                                alertOnBreach = alertOnBreach,
                                autoIsolateOnBreach = autoIsolate,
                                isBreached = false
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.statusAmber),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_threshold_config_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "حفظ العتبة" else "Save Threshold",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog to Add Threshold Rule for an existing device or custom IP
 */
@Composable
private fun AddNewDeviceThresholdDialog(
    availableDevices: List<Device>,
    existingThresholds: List<DeviceThreshold>,
    isDark: Boolean,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onDismiss: () -> Unit,
    onAdd: (DeviceThreshold) -> Unit
) {
    var selectedDevice by remember {
        mutableStateOf(availableDevices.firstOrNull { dev -> existingThresholds.none { it.deviceId == dev.id } } ?: availableDevices.firstOrNull())
    }
    var customIp by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var isCustom by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isArabic) "إضافة عتبة أداء لجهاز" else "Add Device Threshold Rule",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isArabic) "اختر الجهاز المستهدف لتطبيق عتبات المراقبة عليه:" else "Select target device to configure operational thresholds:",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )

                if (!isCustom && selectedDevice != null) {
                    availableDevices.forEach { dev ->
                        val isSel = selectedDevice?.id == dev.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDevice = dev },
                            colors = CardDefaults.cardColors(containerColor = if (isSel) colors.primaryAccent.copy(alpha = 0.15f) else colors.surface),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(if (isSel) colors.primaryAccent else colors.cardBorder)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(dev.name, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(dev.ip, color = colors.primaryAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedDevice?.let { dev ->
                        val newThreshold = DeviceThreshold(
                            deviceId = dev.id,
                            deviceName = dev.name,
                            ipAddress = dev.ip,
                            maxLatencyMs = (dev.latencyMs * 4).coerceAtLeast(30L),
                            maxPacketLossPercent = 3f,
                            maxCpuPercent = 80f,
                            maxRamPercent = 80f
                        )
                        onAdd(newThreshold)
                    }
                },
                enabled = selectedDevice != null
            ) {
                Text(if (isArabic) "إضافة العتبة" else "Add Threshold")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
private fun ThresholdMetricBox(
    label: String,
    value: String,
    color: Color,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun ThresholdParamChip(
    label: String,
    value: String,
    color: Color,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.cardBackground)
            .border(0.8.dp, colors.cardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = colors.textSecondary, fontSize = 8.sp)
            Text(text = value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}
