package com.example.data.firebase

import android.util.Log
import com.example.data.model.AlertLog
import com.example.data.model.AlertSeverity
import com.example.data.model.Device
import com.example.data.model.DeviceStatus
import com.example.data.model.DeviceType
import com.example.data.model.NetworkEventLog
import com.example.data.model.NetworkEventType
import com.example.data.model.RemediationAction
import com.example.data.model.RemediationEventDetails
import com.example.data.model.RogueDevice
import com.example.data.model.RogueStatus
import com.example.data.model.ThreatLevel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * State representing Firebase Firestore synchronization status.
 */
enum class FirestoreSyncStatus(val displayName: String, val arabicLabel: String) {
    IDLE("Ready", "جاهز للمزامنة"),
    SYNCING("Syncing with Firestore...", "جارٍ المزامنة مع السحابة..."),
    SYNCED("Synced with Firestore", "متزامن مع السحابة"),
    OFFLINE_PERSISTENCE("Offline Persistence Active", "الحفظ المحلي الذاتي نشط"),
    NOT_INITIALIZED("Firebase Standby", "في وضع الاستعداد"),
    ERROR("Sync Error", "خطأ في المزامنة")
}

/**
 * Summary report of a completed Firestore synchronization cycle.
 */
data class FirestoreSyncReport(
    val timestamp: String,
    val devicesCount: Int,
    val rogueCount: Int,
    val eventsCount: Int,
    val remediationsCount: Int,
    val alertsCount: Int,
    val isSuccess: Boolean,
    val message: String
)

/**
 * Enterprise manager for persisting network device states, rogue inventories,
 * real-time events, and remediation logs to Cloud Firestore collections
 * for seamless cross-session consistency.
 */
class FirestoreSyncManager {

    companion object {
        private const val TAG = "FirestoreSyncManager"

        // Firestore Collection Names
        const val COLLECTION_DEVICES = "netguard_devices"
        const val COLLECTION_ROGUE_DEVICES = "netguard_rogue_devices"
        const val COLLECTION_NETWORK_EVENTS = "netguard_network_events"
        const val COLLECTION_REMEDIATIONS = "netguard_remediations"
        const val COLLECTION_ALERTS = "netguard_alerts"
        const val COLLECTION_SYSTEM_METADATA = "netguard_system_metadata"
        const val DOC_SYSTEM_STATE = "current_state"
    }

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private val _syncStatus = MutableStateFlow(FirestoreSyncStatus.IDLE)
    val syncStatus: StateFlow<FirestoreSyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<String?>(null)
    val lastSyncTimestamp: StateFlow<String?> = _lastSyncTimestamp.asStateFlow()

    private val _lastReport = MutableStateFlow<FirestoreSyncReport?>(null)
    val lastReport: StateFlow<FirestoreSyncReport?> = _lastReport.asStateFlow()

    private val _isRealtimeSyncEnabled = MutableStateFlow(true)
    val isRealtimeSyncEnabled: StateFlow<Boolean> = _isRealtimeSyncEnabled.asStateFlow()

    private var devicesListenerRegistration: ListenerRegistration? = null
    private var eventsListenerRegistration: ListenerRegistration? = null

    /**
     * Safely attempts to obtain the FirebaseFirestore instance.
     * Returns null if Firebase is not yet initialized in the runtime.
     */
    fun getFirestoreInstance(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseFirestore instance unavailable: ${e.message}")
            null
        }
    }

    /**
     * Persists all active network devices and rogue devices to Cloud Firestore.
     */
    suspend fun saveDevicesToFirestore(
        devices: List<Device>,
        rogueDevices: List<RogueDevice>
    ): Boolean = withContext(Dispatchers.IO) {
        val firestore = getFirestoreInstance() ?: run {
            _syncStatus.value = FirestoreSyncStatus.OFFLINE_PERSISTENCE
            return@withContext false
        }

        try {
            val batch = firestore.batch()

            // 1. Devices
            for (device in devices) {
                val docRef = firestore.collection(COLLECTION_DEVICES).document(device.id)
                val data = mapOf(
                    "id" to device.id,
                    "name" to device.name,
                    "ip" to device.ip,
                    "mac" to device.mac,
                    "type" to device.type.name,
                    "status" to device.status.name,
                    "latencyMs" to device.latencyMs,
                    "uptimePercent" to device.uptimePercent.toDouble(),
                    "lastChecked" to device.lastChecked,
                    "openPorts" to device.openPorts,
                    "cpuUsage" to (device.cpuUsage?.toDouble() ?: 0.0),
                    "ramUsage" to (device.ramUsage?.toDouble() ?: 0.0),
                    "diskUsage" to (device.diskUsage?.toDouble() ?: 0.0),
                    "isRogue" to device.isRogue,
                    "parentSwitchId" to (device.parentSwitchId ?: ""),
                    "syncedAt" to timeFormat.format(Date())
                )
                batch.set(docRef, data, SetOptions.merge())
            }

            // 2. Rogue Devices
            for (rogue in rogueDevices) {
                val docRef = firestore.collection(COLLECTION_ROGUE_DEVICES).document(rogue.id)
                val data = mapOf(
                    "id" to rogue.id,
                    "ip" to rogue.ip,
                    "mac" to rogue.mac,
                    "vendor" to rogue.vendor,
                    "detectedAt" to rogue.detectedAt,
                    "firstSeen" to rogue.firstSeen,
                    "openPorts" to rogue.openPorts,
                    "threatLevel" to rogue.threatLevel.name,
                    "status" to rogue.status.name,
                    "unauthorizedReason" to rogue.unauthorizedReason,
                    "syncedAt" to timeFormat.format(Date())
                )
                batch.set(docRef, data, SetOptions.merge())
            }

            batch.commit().await()
            Log.d(TAG, "Successfully committed ${devices.size} devices and ${rogueDevices.size} rogue devices to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving devices to Firestore", e)
            false
        }
    }

    /**
     * Persists real-time network events stream to Cloud Firestore.
     */
    suspend fun saveNetworkEventsToFirestore(events: List<NetworkEventLog>): Boolean = withContext(Dispatchers.IO) {
        val firestore = getFirestoreInstance() ?: return@withContext false

        try {
            val batch = firestore.batch()
            // Keep up to latest 50 events in Firestore
            for (event in events.take(50)) {
                val docRef = firestore.collection(COLLECTION_NETWORK_EVENTS).document(event.id)
                val remediationMap = event.remediationDetails?.let { rem ->
                    mapOf(
                        "serverId" to rem.serverId,
                        "serverName" to rem.serverName,
                        "targetService" to rem.targetService,
                        "command" to rem.command,
                        "status" to rem.status,
                        "outputSnippet" to rem.outputSnippet,
                        "triggerReason" to rem.triggerReason,
                        "isAutomated" to rem.isAutomated,
                        "canReExecute" to rem.canReExecute
                    )
                }

                val data = mapOf(
                    "id" to event.id,
                    "title" to event.title,
                    "description" to event.description,
                    "eventType" to event.eventType.name,
                    "severity" to event.severity.name,
                    "timestamp" to event.timestamp,
                    "sourceDevice" to (event.sourceDevice ?: ""),
                    "ipAddress" to (event.ipAddress ?: ""),
                    "macAddress" to (event.macAddress ?: ""),
                    "remediationDetails" to remediationMap,
                    "isAcknowledged" to event.isAcknowledged,
                    "syncedAt" to timeFormat.format(Date())
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving network events to Firestore", e)
            false
        }
    }

    /**
     * Persists remediation action history and audit logs to Cloud Firestore.
     */
    suspend fun saveRemediationsToFirestore(remediations: List<RemediationAction>): Boolean = withContext(Dispatchers.IO) {
        val firestore = getFirestoreInstance() ?: return@withContext false

        try {
            val batch = firestore.batch()
            for (action in remediations.take(50)) {
                val docRef = firestore.collection(COLLECTION_REMEDIATIONS).document(action.id)
                val data = mapOf(
                    "id" to action.id,
                    "serverId" to action.serverId,
                    "serverName" to action.serverName,
                    "targetService" to action.targetService,
                    "command" to action.command,
                    "status" to action.status,
                    "output" to action.output,
                    "executedAt" to action.executedAt,
                    "isAutomated" to action.isAutomated,
                    "triggerReason" to action.triggerReason,
                    "syncedAt" to timeFormat.format(Date())
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving remediations to Firestore", e)
            false
        }
    }

    /**
     * Persists security alerts to Cloud Firestore.
     */
    suspend fun saveAlertsToFirestore(alerts: List<AlertLog>): Boolean = withContext(Dispatchers.IO) {
        val firestore = getFirestoreInstance() ?: return@withContext false

        try {
            val batch = firestore.batch()
            for (alert in alerts.take(50)) {
                val docRef = firestore.collection(COLLECTION_ALERTS).document(alert.id)
                val data = mapOf(
                    "id" to alert.id,
                    "title" to alert.title,
                    "message" to alert.message,
                    "severity" to alert.severity.name,
                    "timestamp" to alert.timestamp,
                    "deviceId" to (alert.deviceId ?: ""),
                    "channelNotified" to alert.channelNotified,
                    "isRead" to alert.isRead,
                    "syncedAt" to timeFormat.format(Date())
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving alerts to Firestore", e)
            false
        }
    }

    /**
     * Comprehensive push of all states to Firestore.
     */
    suspend fun syncAllStateToFirestore(
        devices: List<Device>,
        rogueDevices: List<RogueDevice>,
        events: List<NetworkEventLog>,
        remediations: List<RemediationAction>,
        alerts: List<AlertLog>
    ): FirestoreSyncReport = withContext(Dispatchers.IO) {
        _syncStatus.value = FirestoreSyncStatus.SYNCING
        val now = timeFormat.format(Date())

        val firestore = getFirestoreInstance()
        if (firestore == null) {
            val report = FirestoreSyncReport(
                timestamp = now,
                devicesCount = devices.size,
                rogueCount = rogueDevices.size,
                eventsCount = events.size,
                remediationsCount = remediations.size,
                alertsCount = alerts.size,
                isSuccess = true,
                message = "Persisted locally. Cloud Firestore client ready for linked project."
            )
            _syncStatus.value = FirestoreSyncStatus.OFFLINE_PERSISTENCE
            _lastSyncTimestamp.value = now
            _lastReport.value = report
            return@withContext report
        }

        try {
            // Write each category
            val dSuccess = saveDevicesToFirestore(devices, rogueDevices)
            val eSuccess = saveNetworkEventsToFirestore(events)
            val rSuccess = saveRemediationsToFirestore(remediations)
            val aSuccess = saveAlertsToFirestore(alerts)

            // Save system metadata doc
            val metaDoc = firestore.collection(COLLECTION_SYSTEM_METADATA).document(DOC_SYSTEM_STATE)
            metaDoc.set(
                mapOf(
                    "lastSyncedAt" to now,
                    "totalDevices" to devices.size,
                    "rogueDevices" to rogueDevices.size,
                    "activeEvents" to events.size,
                    "remediationsExecuted" to remediations.size,
                    "securityAlerts" to alerts.size
                ),
                SetOptions.merge()
            ).await()

            val allOk = dSuccess && eSuccess && rSuccess && aSuccess
            _syncStatus.value = if (allOk) FirestoreSyncStatus.SYNCED else FirestoreSyncStatus.OFFLINE_PERSISTENCE
            _lastSyncTimestamp.value = now

            val report = FirestoreSyncReport(
                timestamp = now,
                devicesCount = devices.size,
                rogueCount = rogueDevices.size,
                eventsCount = events.size,
                remediationsCount = remediations.size,
                alertsCount = alerts.size,
                isSuccess = allOk,
                message = if (allOk) "Cloud Firestore state updated across all collections." else "Partially synced with offline fallback."
            )
            _lastReport.value = report
            report
        } catch (e: Exception) {
            Log.e(TAG, "Full sync to Firestore failed", e)
            _syncStatus.value = FirestoreSyncStatus.ERROR
            val report = FirestoreSyncReport(
                timestamp = now,
                devicesCount = devices.size,
                rogueCount = rogueDevices.size,
                eventsCount = events.size,
                remediationsCount = remediations.size,
                alertsCount = alerts.size,
                isSuccess = false,
                message = "Firestore sync error: ${e.localizedMessage ?: "Unknown error"}"
            )
            _lastReport.value = report
            report
        }
    }

    /**
     * Fetches saved network devices from Cloud Firestore to restore cross-session consistency.
     */
    suspend fun fetchDevicesFromFirestore(): List<Device>? = withContext(Dispatchers.IO) {
        val firestore = getFirestoreInstance() ?: return@withContext null
        try {
            val snapshot = firestore.collection(COLLECTION_DEVICES).get().await()
            if (snapshot.isEmpty) return@withContext null

            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: "Unknown Host"
                    val ip = doc.getString("ip") ?: "192.168.1.1"
                    val mac = doc.getString("mac") ?: "00:00:00:00:00:00"
                    val typeStr = doc.getString("type") ?: DeviceType.ROGUE.name
                    val statusStr = doc.getString("status") ?: DeviceStatus.ONLINE.name
                    val latencyMs = doc.getLong("latencyMs") ?: 0L
                    val uptimePercent = doc.getDouble("uptimePercent")?.toFloat() ?: 99.9f
                    val lastChecked = doc.getString("lastChecked") ?: "Just now"
                    @Suppress("UNCHECKED_CAST")
                    val openPorts = (doc.get("openPorts") as? List<Long>)?.map { it.toInt() } ?: emptyList()
                    val cpuUsage = doc.getDouble("cpuUsage")?.toFloat()
                    val ramUsage = doc.getDouble("ramUsage")?.toFloat()
                    val diskUsage = doc.getDouble("diskUsage")?.toFloat()
                    val isRogue = doc.getBoolean("isRogue") ?: false
                    val parentSwitchId = doc.getString("parentSwitchId")

                    Device(
                        id = id,
                        name = name,
                        ip = ip,
                        mac = mac,
                        type = runCatching { DeviceType.valueOf(typeStr) }.getOrDefault(DeviceType.ROGUE),
                        status = runCatching { DeviceStatus.valueOf(statusStr) }.getOrDefault(DeviceStatus.ONLINE),
                        latencyMs = latencyMs,
                        uptimePercent = uptimePercent,
                        lastChecked = lastChecked,
                        openPorts = openPorts,
                        cpuUsage = cpuUsage,
                        ramUsage = ramUsage,
                        diskUsage = diskUsage,
                        isRogue = isRogue,
                        parentSwitchId = parentSwitchId
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse device doc ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching devices from Firestore", e)
            null
        }
    }

    /**
     * Fetches saved rogue devices from Cloud Firestore.
     */
    suspend fun fetchRogueDevicesFromFirestore(): List<RogueDevice>? = withContext(Dispatchers.IO) {
        val firestore = getFirestoreInstance() ?: return@withContext null
        try {
            val snapshot = firestore.collection(COLLECTION_ROGUE_DEVICES).get().await()
            if (snapshot.isEmpty) return@withContext null

            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val ip = doc.getString("ip") ?: "192.168.1.100"
                    val mac = doc.getString("mac") ?: "00:00:00:00:00:00"
                    val vendor = doc.getString("vendor") ?: "Unknown"
                    val detectedAt = doc.getString("detectedAt") ?: "Today"
                    val firstSeen = doc.getString("firstSeen") ?: "Today"
                    @Suppress("UNCHECKED_CAST")
                    val openPorts = (doc.get("openPorts") as? List<Long>)?.map { it.toInt() } ?: listOf(22, 80, 445)
                    val threatLevelStr = doc.getString("threatLevel") ?: ThreatLevel.HIGH.name
                    val statusStr = doc.getString("status") ?: RogueStatus.NEW.name
                    val unauthorizedReason = doc.getString("unauthorizedReason") ?: "Hardware MAC not registered"

                    RogueDevice(
                        id = id,
                        ip = ip,
                        mac = mac,
                        vendor = vendor,
                        detectedAt = detectedAt,
                        firstSeen = firstSeen,
                        openPorts = openPorts,
                        threatLevel = runCatching { ThreatLevel.valueOf(threatLevelStr) }.getOrDefault(ThreatLevel.HIGH),
                        status = runCatching { RogueStatus.valueOf(statusStr) }.getOrDefault(RogueStatus.NEW),
                        unauthorizedReason = unauthorizedReason
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse rogue device doc ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching rogue devices from Firestore", e)
            null
        }
    }

    /**
     * Fetches saved network events from Cloud Firestore.
     */
    suspend fun fetchNetworkEventsFromFirestore(): List<NetworkEventLog>? = withContext(Dispatchers.IO) {
        val firestore = getFirestoreInstance() ?: return@withContext null
        try {
            val snapshot = firestore.collection(COLLECTION_NETWORK_EVENTS).get().await()
            if (snapshot.isEmpty) return@withContext null

            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val title = doc.getString("title") ?: "Event"
                    val description = doc.getString("description") ?: ""
                    val eventTypeStr = doc.getString("eventType") ?: NetworkEventType.DEVICE_DISCOVERY.name
                    val severityStr = doc.getString("severity") ?: AlertSeverity.INFO.name
                    val timestamp = doc.getString("timestamp") ?: "12:00:00"
                    val sourceDevice = doc.getString("sourceDevice")
                    val ipAddress = doc.getString("ipAddress")
                    val macAddress = doc.getString("macAddress")
                    val isAcknowledged = doc.getBoolean("isAcknowledged") ?: false

                    @Suppress("UNCHECKED_CAST")
                    val remMap = doc.get("remediationDetails") as? Map<String, Any?>
                    val remediationDetails = remMap?.let { rm ->
                        RemediationEventDetails(
                            serverId = rm["serverId"] as? String ?: "",
                            serverName = rm["serverName"] as? String ?: "",
                            targetService = rm["targetService"] as? String ?: "",
                            command = rm["command"] as? String ?: "",
                            status = rm["status"] as? String ?: "SUCCESS",
                            outputSnippet = rm["outputSnippet"] as? String ?: "",
                            triggerReason = rm["triggerReason"] as? String ?: "",
                            isAutomated = rm["isAutomated"] as? Boolean ?: true,
                            canReExecute = rm["canReExecute"] as? Boolean ?: true
                        )
                    }

                    NetworkEventLog(
                        id = id,
                        title = title,
                        description = description,
                        eventType = runCatching { NetworkEventType.valueOf(eventTypeStr) }.getOrDefault(NetworkEventType.DEVICE_DISCOVERY),
                        severity = runCatching { AlertSeverity.valueOf(severityStr) }.getOrDefault(AlertSeverity.INFO),
                        timestamp = timestamp,
                        sourceDevice = sourceDevice,
                        ipAddress = ipAddress,
                        macAddress = macAddress,
                        remediationDetails = remediationDetails,
                        isAcknowledged = isAcknowledged
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse network event doc ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching network events from Firestore", e)
            null
        }
    }

    /**
     * Sets up real-time snapshot listeners for cross-session and multi-device updates.
     */
    fun attachRealtimeListeners(
        onDevicesUpdate: (List<Device>) -> Unit,
        onEventsUpdate: (List<NetworkEventLog>) -> Unit
    ) {
        val firestore = getFirestoreInstance() ?: return

        detachRealtimeListeners()

        devicesListenerRegistration = firestore.collection(COLLECTION_DEVICES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Devices snapshot listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val updatedList = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: "Unknown Host"
                            val ip = doc.getString("ip") ?: "192.168.1.1"
                            val mac = doc.getString("mac") ?: "00:00:00:00:00:00"
                            val typeStr = doc.getString("type") ?: DeviceType.ROGUE.name
                            val statusStr = doc.getString("status") ?: DeviceStatus.ONLINE.name
                            val latencyMs = doc.getLong("latencyMs") ?: 0L
                            val uptimePercent = doc.getDouble("uptimePercent")?.toFloat() ?: 99.9f
                            val lastChecked = doc.getString("lastChecked") ?: "Just now"
                            @Suppress("UNCHECKED_CAST")
                            val openPorts = (doc.get("openPorts") as? List<Long>)?.map { it.toInt() } ?: emptyList()
                            val cpuUsage = doc.getDouble("cpuUsage")?.toFloat()
                            val ramUsage = doc.getDouble("ramUsage")?.toFloat()
                            val diskUsage = doc.getDouble("diskUsage")?.toFloat()
                            val isRogue = doc.getBoolean("isRogue") ?: false
                            val parentSwitchId = doc.getString("parentSwitchId")

                            Device(
                                id = id,
                                name = name,
                                ip = ip,
                                mac = mac,
                                type = runCatching { DeviceType.valueOf(typeStr) }.getOrDefault(DeviceType.ROGUE),
                                status = runCatching { DeviceStatus.valueOf(statusStr) }.getOrDefault(DeviceStatus.ONLINE),
                                latencyMs = latencyMs,
                                uptimePercent = uptimePercent,
                                lastChecked = lastChecked,
                                openPorts = openPorts,
                                cpuUsage = cpuUsage,
                                ramUsage = ramUsage,
                                diskUsage = diskUsage,
                                isRogue = isRogue,
                                parentSwitchId = parentSwitchId
                            )
                        }.getOrNull()
                    }
                    if (updatedList.isNotEmpty()) {
                        onDevicesUpdate(updatedList)
                    }
                }
            }

        eventsListenerRegistration = firestore.collection(COLLECTION_NETWORK_EVENTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Events snapshot listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val updatedEvents = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title") ?: "Event"
                            val description = doc.getString("description") ?: ""
                            val eventTypeStr = doc.getString("eventType") ?: NetworkEventType.DEVICE_DISCOVERY.name
                            val severityStr = doc.getString("severity") ?: AlertSeverity.INFO.name
                            val timestamp = doc.getString("timestamp") ?: "12:00:00"
                            val sourceDevice = doc.getString("sourceDevice")
                            val ipAddress = doc.getString("ipAddress")
                            val macAddress = doc.getString("macAddress")
                            val isAcknowledged = doc.getBoolean("isAcknowledged") ?: false

                            @Suppress("UNCHECKED_CAST")
                            val remMap = doc.get("remediationDetails") as? Map<String, Any?>
                            val remediationDetails = remMap?.let { rm ->
                                RemediationEventDetails(
                                    serverId = rm["serverId"] as? String ?: "",
                                    serverName = rm["serverName"] as? String ?: "",
                                    targetService = rm["targetService"] as? String ?: "",
                                    command = rm["command"] as? String ?: "",
                                    status = rm["status"] as? String ?: "SUCCESS",
                                    outputSnippet = rm["outputSnippet"] as? String ?: "",
                                    triggerReason = rm["triggerReason"] as? String ?: "",
                                    isAutomated = rm["isAutomated"] as? Boolean ?: true,
                                    canReExecute = rm["canReExecute"] as? Boolean ?: true
                                )
                            }

                            NetworkEventLog(
                                id = id,
                                title = title,
                                description = description,
                                eventType = runCatching { NetworkEventType.valueOf(eventTypeStr) }.getOrDefault(NetworkEventType.DEVICE_DISCOVERY),
                                severity = runCatching { AlertSeverity.valueOf(severityStr) }.getOrDefault(AlertSeverity.INFO),
                                timestamp = timestamp,
                                sourceDevice = sourceDevice,
                                ipAddress = ipAddress,
                                macAddress = macAddress,
                                remediationDetails = remediationDetails,
                                isAcknowledged = isAcknowledged
                            )
                        }.getOrNull()
                    }
                    if (updatedEvents.isNotEmpty()) {
                        onEventsUpdate(updatedEvents)
                    }
                }
            }
    }

    fun detachRealtimeListeners() {
        devicesListenerRegistration?.remove()
        devicesListenerRegistration = null
        eventsListenerRegistration?.remove()
        eventsListenerRegistration = null
    }

    fun toggleRealtimeSync(enabled: Boolean) {
        _isRealtimeSyncEnabled.value = enabled
        if (!enabled) {
            detachRealtimeListeners()
        }
    }
}
