package com.example.data.model

enum class DeviceType(val displayName: String) {
    ROUTER("Core Router / Gateway"),
    SWITCH("Network Switch"),
    SERVER("Enterprise Server"),
    WORKSTATION("Workstation / PC"),
    CAMERA("Security Camera"),
    PRINTER("Network Printer"),
    ROGUE("Unauthorized / Unknown")
}

enum class DeviceStatus(val label: String) {
    ONLINE("Online"),
    OFFLINE("Offline"),
    WARNING("Warning"),
    UNSTABLE("High Latency")
}

data class Device(
    val id: String,
    val name: String,
    val ip: String,
    val mac: String,
    val type: DeviceType,
    val status: DeviceStatus,
    val latencyMs: Long = 0L,
    val uptimePercent: Float = 99.9f,
    val lastChecked: String = "Just now",
    val openPorts: List<Int> = emptyList(),
    val cpuUsage: Float? = null,
    val ramUsage: Float? = null,
    val diskUsage: Float? = null,
    val isRogue: Boolean = false,
    val parentSwitchId: String? = null
)

enum class ThreatLevel(val label: String) {
    LOW("Low"),
    MEDIUM("Suspicious"),
    HIGH("Unauthorized Access"),
    CRITICAL("Rogue Threat")
}

enum class RogueStatus(val label: String) {
    NEW("Newly Discovered"),
    INVESTIGATING("Under Investigation"),
    ISOLATED("Isolated / Blocked"),
    TRUSTED("Authorized / Whitelisted")
}

data class AuthorizedDevice(
    val id: String,
    val mac: String,
    val name: String,
    val vendor: String,
    val authorizedDate: String,
    val ipRange: String = "192.168.1.0/24",
    val authorizedBy: String = "SecOps Lead"
)

data class WhitelistAuditSummary(
    val totalDiscovered: Int,
    val authorizedCount: Int,
    val unauthorizedCount: Int,
    val compliancePercent: Float,
    val lastAuditTime: String
)

data class RogueDevice(
    val id: String,
    val ip: String,
    val mac: String,
    val vendor: String,
    val detectedAt: String,
    val firstSeen: String,
    val openPorts: List<Int> = listOf(22, 80, 445),
    val threatLevel: ThreatLevel = ThreatLevel.HIGH,
    val status: RogueStatus = RogueStatus.NEW,
    val unauthorizedReason: String = "Hardware MAC not registered in Enterprise Whitelist"
)

enum class AlertSeverity {
    CRITICAL,
    WARNING,
    INFO
}

data class AlertLog(
    val id: String,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: String,
    val deviceId: String? = null,
    val channelNotified: String = "Telegram Bot",
    val isRead: Boolean = false
)

/**
 * Categorization of real-time network and security telemetry events.
 */
enum class NetworkEventType(val displayName: String, val categoryArabic: String) {
    ALL("All Events", "جميع الأحداث"),
    DEVICE_DISCOVERY("Device Discovery", "اكتشاف الأجهزة"),
    CONNECTIVITY_CHANGE("Connectivity", "تغييرات الاتصال"),
    REMEDIATION_ALERT("Remediation Alerts", "تنبيهات المعالجة"),
    SECURITY_ANOMALY("Security Anomaly", "شذوذ أمني")
}

/**
 * Deep metadata for remediation action execution and watchdog alerts.
 */
data class RemediationEventDetails(
    val serverId: String,
    val serverName: String,
    val targetService: String,
    val command: String,
    val status: String = "SUCCESS", // "SUCCESS", "EXECUTING", "FAILED", "PENDING"
    val outputSnippet: String = "",
    val triggerReason: String = "Watchdog Threshold Rule Exceeded",
    val isAutomated: Boolean = true,
    val canReExecute: Boolean = true
)

/**
 * Real-time network event log entry capturing discovery, link state changes,
 * and automated/manual remediation alerts.
 */
data class NetworkEventLog(
    val id: String,
    val title: String,
    val description: String,
    val eventType: NetworkEventType,
    val severity: AlertSeverity = AlertSeverity.INFO,
    val timestamp: String,
    val sourceDevice: String? = null,
    val ipAddress: String? = null,
    val macAddress: String? = null,
    val remediationDetails: RemediationEventDetails? = null,
    val isAcknowledged: Boolean = false
)

data class ServiceStatus(
    val name: String,
    val isRunning: Boolean,
    val port: Int
)

data class ServerMetric(
    val serverId: String,
    val serverName: String,
    val ip: String,
    val cpuPercent: Float,
    val ramPercent: Float,
    val diskPercent: Float,
    val temperatureC: Float,
    val processesCount: Int,
    val services: List<ServiceStatus>,
    val lastUpdated: String
)

data class RemediationAction(
    val id: String,
    val serverId: String,
    val serverName: String,
    val targetService: String,
    val command: String,
    val status: String, // "SUCCESS", "EXECUTING", "FAILED"
    val output: String,
    val executedAt: String,
    val isAutomated: Boolean = false,
    val triggerReason: String = "Manual operator execution"
)

data class AutoRemediationPolicy(
    val id: String,
    val serverId: String,
    val serverName: String,
    val serviceName: String,
    val triggerCondition: String,
    val sshCommand: String,
    val ramThresholdPercent: Float = 80f,
    val cpuThresholdPercent: Float = 85f,
    val isEnabled: Boolean = true,
    val lastTriggered: String? = null,
    val executionCount: Int = 0
)

enum class ThresholdActionType(val label: String, val defaultCommand: String) {
    NOTIFICATION_ONLY("Notification Only", ""),
    RESTART_SERVICE("Restart Service", "systemctl restart {service}"),
    PURGE_MEMORY_CACHE("Purge RAM Cache", "sync && echo 3 > /proc/sys/vm/drop_caches"),
    RESTART_DOCKER("Restart Docker Runtime", "systemctl restart docker"),
    CUSTOM_SCRIPT("Custom SSH Script", "systemctl restart worker-service"),
    EMERGENCY_REBOOT("Emergency Reboot", "shutdown -r now")
}

data class ServerThresholdRule(
    val id: String,
    val serverId: String, // Server id e.g. "dev-04", "dev-03", "dev-05", "dev-01" or "ALL"
    val serverName: String, // e.g. "Database Cluster Master (DB-01)"
    val cpuThresholdPercent: Float = 80f,
    val ramThresholdPercent: Float = 75f,
    val notifyInApp: Boolean = true,
    val notifyTelegram: Boolean = true,
    val severity: AlertSeverity = AlertSeverity.WARNING,
    val actionType: ThresholdActionType = ThresholdActionType.NOTIFICATION_ONLY,
    val targetService: String = "PostgreSQL DB",
    val customCommand: String = "",
    val isEnabled: Boolean = true,
    val lastTriggered: String? = null,
    val triggerCount: Int = 0
)

data class TopologyNode(
    val id: String,
    val label: String,
    val ip: String,
    val type: DeviceType,
    val status: DeviceStatus,
    val x: Float, // Relative canvas X (0.0 .. 1.0)
    val y: Float, // Relative canvas Y (0.0 .. 1.0)
    val parentId: String? = null,
    val activeAlertCount: Int = 0,
    val isRogue: Boolean = false,
    val latencyMs: Long = 2L,
    val macAddress: String = "",
    val alertsSummary: List<String> = emptyList(),
    val incomingMbps: Float = 0f,
    val outgoingMbps: Float = 0f
)

data class NodeBandwidthTraffic(
    val nodeId: String,
    val nodeLabel: String,
    val incomingMbps: Float = 0f,
    val outgoingMbps: Float = 0f,
    val peakIncomingMbps: Float = incomingMbps,
    val peakOutgoingMbps: Float = outgoingMbps,
    val incomingHistory: List<Float> = emptyList(),
    val outgoingHistory: List<Float> = emptyList(),
    val timestamps: List<String> = emptyList(),
    val totalRxMB: Float = 0f,
    val totalTxMB: Float = 0f,
    val packetsRxPerSec: Int = 0,
    val packetsTxPerSec: Int = 0,
    val isAnomalySpike: Boolean = false
) {
    companion object {
        fun defaultFor(nodeId: String, label: String) = NodeBandwidthTraffic(
            nodeId = nodeId,
            nodeLabel = label,
            incomingMbps = 15f,
            outgoingMbps = 8f,
            peakIncomingMbps = 25f,
            peakOutgoingMbps = 18f,
            incomingHistory = listOf(10f, 12f, 14f, 15f, 13f, 15f),
            outgoingHistory = listOf(5f, 6f, 7f, 8f, 7f, 8f),
            timestamps = listOf("-10s", "-8s", "-6s", "-4s", "-2s", "Live")
        )
    }
}

enum class SubscriptionPlan(
    val title: String,
    val maxDevices: Int,
    val price: String,
    val badge: String
) {
    FREE_TIER("Free Community", 5, "$0 / mo", "Free"),
    BASIC_TIER("Business Pro", 50, "$49 / mo", "Popular"),
    ENTERPRISE_TIER("Enterprise Sentinel", 9999, "$199 / mo", "Unlimited")
}

data class SlaReport(
    val generatedAt: String,
    val totalDevices: Int,
    val activeDevices: Int,
    val averageUptime: Float,
    val totalAlertsLast30Days: Int,
    val mttrMinutes: Float,
    val securityScore: Int,
    val incidentBreakdown: Map<String, Int>
)

data class BandwidthMetrics(
    val downloadMbps: Float = 342.8f,
    val uploadMbps: Float = 78.4f,
    val totalCapacityMbps: Float = 1000f,
    val downloadHistory: List<Float> = listOf(220f, 245f, 280f, 310f, 295f, 342.8f),
    val uploadHistory: List<Float> = listOf(45f, 52f, 68f, 74f, 62f, 78.4f)
)

data class NetworkHealthMetrics(
    val healthScore: Int = 98,
    val packetLossPercent: Float = 0.02f,
    val averageLatencyMs: Float = 3.4f,
    val dnsResolutionMs: Float = 12f,
    val activeGatewayIp: String = "192.168.1.1",
    val postureStatus: String = "OPTIMAL"
)

data class CriticalServer(
    val id: String,
    val name: String,
    val role: String,
    val ip: String,
    val status: DeviceStatus,
    val latencyMs: Long,
    val uptimePercent: Float,
    val cpuPercent: Float,
    val ramPercent: Float,
    val diskPercent: Float,
    val temperatureC: Float = 45f,
    val services: List<ServiceStatus>,
    val lastPing: String = "Just now"
)

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    ARABIC("ar", "العربية", "العربية")
}

data class IntrusionAttempt(
    val id: String,
    val timestamp: String,
    val attackerIp: String,
    val attackerCountry: String,
    val attackerFlag: String,
    val threatType: String,
    val threatTypeArabic: String,
    val targetIp: String,
    val targetPort: Int,
    val severity: AlertSeverity,
    val status: String, // "BLOCKED", "DETECTED", "QUARANTINED"
    val attackVector: String,
    val attackVectorArabic: String,
    val hopCount: Int = 4,
    val traceHops: List<String> = emptyList()
)

data class VulnerabilityItem(
    val id: String,
    val cveId: String,
    val title: String,
    val titleArabic: String,
    val targetDeviceName: String,
    val targetIp: String,
    val cvssScore: Float,
    val severity: AlertSeverity,
    val isPatched: Boolean = false,
    val remediationRecommendation: String,
    val remediationRecommendationArabic: String
)

data class AdminServiceStatus(
    val id: String,
    val serviceName: String,
    val serviceNameArabic: String,
    val description: String,
    val descriptionArabic: String,
    val isRunning: Boolean,
    val uptime: String,
    val category: String = "CORE"
)

data class AdminAuditLog(
    val id: String,
    val timestamp: String,
    val adminEmail: String,
    val action: String,
    val actionArabic: String,
    val severity: AlertSeverity = AlertSeverity.INFO
)

data class RealtimeThreatNotification(
    val id: String,
    val timestamp: String,
    val title: String,
    val titleArabic: String,
    val description: String,
    val descriptionArabic: String,
    val ipAddress: String,
    val macAddress: String,
    val severity: AlertSeverity,
    val rogueId: String? = null,
    val isDismissed: Boolean = false,
    val actionTaken: String? = null
)

data class RechartsTrendPoint(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val downloadMbps: Float,
    val uploadMbps: Float,
    val serverCpuPercent: Float,
    val serverRamPercent: Float,
    val isSpike: Boolean = false
)

/**
 * Represents a single hourly data point in the 24-hour network traffic trend timeline.
 * Inspired by Recharts / D3 time-series data structures.
 */
data class TrafficTrend24hPoint(
    val hourIndex: Int, // 0..23 (0 = 23 hours ago, 23 = current hour)
    val timeLabel: String, // e.g. "14:00"
    val relativeLabel: String, // e.g. "-9h" or "Now"
    val inboundMbps: Float,
    val outboundMbps: Float,
    val peakBurstMbps: Float,
    val inboundGB: Float,
    val outboundGB: Float,
    val activeSessions: Int,
    val packetLossPercent: Float = 0.02f,
    val latencyMs: Float = 3.5f,
    val isPeakHour: Boolean = false,
    val isOffPeak: Boolean = false,
    val isSpike: Boolean = false,
    val anomalyNote: String? = null,
    val topProtocol: String = "HTTPS (443)",
    val protocolBreakdown: Map<String, Float> = mapOf(
        "HTTPS" to 58f,
        "Cloud Sync" to 22f,
        "Database" to 12f,
        "VoIP/SIP" to 8f
    )
)

/**
 * 24-Hour Network Traffic Executive Summary Metrics (KPIs)
 */
data class Traffic24hSummary(
    val totalInboundTB: Float,
    val totalOutboundTB: Float,
    val avgInboundMbps: Float,
    val avgOutboundMbps: Float,
    val peakThroughputMbps: Float,
    val peakHourLabel: String,
    val minThroughputMbps: Float,
    val minHourLabel: String,
    val totalActiveSessions: Int,
    val totalAnomaliesCount: Int,
    val avgLatencyMs: Float,
    val slaCompliancePercent: Float = 99.98f
)

data class DeviceMonitoringConfig(
    val deviceId: String,
    val deviceName: String,
    val ip: String,
    val mac: String,
    val monitoringIntervalSeconds: Int = 10,
    val autoIsolateOnThreat: Boolean = true,
    val deepPacketInspection: Boolean = true,
    val arpSpoofDefense: Boolean = true,
    val portScanWatchdog: Boolean = true,
    val packetRateThreshold: Int = 600,
    val alertOnPromiscuousMode: Boolean = true,
    val notifyOnTelegram: Boolean = true,
    val lastUpdated: String = ""
)

data class PingResult(
    val sequence: Int,
    val ip: String,
    val bytes: Int = 64,
    val latencyMs: Long,
    val ttl: Int = 64,
    val isSuccess: Boolean = true,
    val timestamp: String = "",
    val errorMessage: String? = null
)

data class DiagnosticSession(
    val targetIp: String = "",
    val targetLabel: String = "",
    val isRunning: Boolean = false,
    val packetsSent: Int = 0,
    val packetsReceived: Int = 0,
    val packetLossPercent: Float = 0f,
    val minLatencyMs: Long = 0L,
    val avgLatencyMs: Long = 0L,
    val maxLatencyMs: Long = 0L,
    val results: List<PingResult> = emptyList(),
    val isThresholdBreached: Boolean = false,
    val thresholdBreachReason: String? = null,
    val completedTimestamp: String? = null
)

data class DeviceThreshold(
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val maxLatencyMs: Long = 80L, // Max allowable ping latency (عتبة التأخير)
    val maxPacketLossPercent: Float = 5f, // Max allowable packet loss % (عتبة فقدان الحزم)
    val maxCpuPercent: Float = 85f, // Max CPU load % (عتبة استهلاك المعالج)
    val maxRamPercent: Float = 80f, // Max RAM load % (عتبة استهلاك الذاكرة)
    val maxBandwidthMbps: Float = 500f, // Max Throughput limit (عتبة استهلاك النطاق)
    val consecutiveFailuresBeforeAlert: Int = 3, // Consecutive failure limit (عتبة الإخفاقات المتتالية)
    val alertOnBreach: Boolean = true,
    val autoIsolateOnBreach: Boolean = false,
    val isBreached: Boolean = false,
    val breachReason: String? = null,
    val lastBreachedTimestamp: String? = null,
    val totalBreachCount: Int = 0
)

data class PushNotificationRecord(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val trafficMbps: Float,
    val thresholdMbps: Float,
    val sourceTarget: String = "Network Gateway",
    val severity: AlertSeverity = AlertSeverity.CRITICAL,
    val isDelivered: Boolean = true
)

data class TrafficNotificationConfig(
    val isEnabled: Boolean = true,
    val bandwidthThresholdMbps: Float = 600f, // Global traffic threshold in Mbps
    val perDeviceThresholdEnabled: Boolean = true,
    val cooldownSeconds: Int = 12,
    val playSound: Boolean = true,
    val vibrate: Boolean = true,
    val lastTriggeredTimestamp: String? = null,
    val totalTriggeredCount: Int = 0
)

// ==========================================
// CUSTOMER SERVICE, FAQS & INQUIRIES MODELS
// ==========================================

enum class FaqCategory(val titleArabic: String, val titleEnglish: String) {
    ALL("الكل", "All"),
    OVERVIEW("نظرة عامة والخدمات", "Overview & Services"),
    SECURITY("الأمان ومكافحة التهديدات", "Security & IDS"),
    INTEGRATION("الربط السحابي والـ API", "Cloud & API Integration"),
    REPORTS("التقارير الإدارية", "Administrative Reports"),
    TROUBLESHOOTING("الدعم واستكشاف الأخطاء", "Support & Diagnostics")
}

data class FaqItem(
    val id: String,
    val category: FaqCategory,
    val questionArabic: String,
    val questionEnglish: String,
    val answerArabic: String,
    val answerEnglish: String,
    val tags: List<String> = emptyList()
)

data class CustomerInquiry(
    val id: String,
    val senderName: String,
    val contactInfo: String,
    val inquiryType: String,
    val subject: String,
    val message: String,
    val timestamp: String,
    val status: String = "ANSWERED", // "PENDING", "IN_PROGRESS", "ANSWERED"
    val officialReply: String? = null
)

data class SecurityHealthAudit(
    val scorePercent: Int = 96,
    val grade: String = "A+",
    val firewallStatus: String = "Active & Enforced",
    val idsSensorStatus: String = "Operational (100% Real-time)",
    val rogueDefenseStatus: String = "Instant Auto-Isolation Enabled",
    val backupReadiness: String = "Verified & Synchronized",
    val telegramAlertPipeline: String = "Connected",
    val recommendations: List<String> = emptyList()
)

// ==========================================
// CONNECTIVITY HEALTH MONITOR MODELS
// ==========================================

enum class EndpointStatus(val labelEnglish: String, val labelArabic: String) {
    REACHABLE("Reachable", "متصل ومستقر"),
    DEGRADED("High Latency", "استجابة بطيئة"),
    UNREACHABLE("Unreachable", "غير متاح / منقطع"),
    CHECKING("Testing Link...", "جاري الفحص...")
}

enum class EndpointCategory(val labelArabic: String, val labelEnglish: String) {
    GATEWAY("بوابة النطاق والتحويل", "Core Gateway & Routing"),
    DNS("خوادم التحليل والتسمية DNS", "Enterprise DNS & Resolver"),
    SECURITY_CLOUD("منظومة الأمان السحابية", "Security Cloud & Telemetry"),
    IDENTITY("خوادم الهوية والدليل النشط", "Identity & Active Directory"),
    SIEM("مركز مراقبة السجلات SIEM", "SIEM & Security Operations"),
    PUBLIC_WAN("بوابة الخروج للعالم الخارجي", "WAN Edge & Connectivity")
}

data class EnterpriseEndpoint(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val protocol: String,
    val category: EndpointCategory,
    val isCritical: Boolean = true,
    val description: String = ""
)

data class EndpointHealthResult(
    val endpoint: EnterpriseEndpoint,
    val status: EndpointStatus,
    val latencyMs: Long,
    val lastCheckedTimestamp: String,
    val responseDetails: String = "",
    val httpOrSocketCode: String? = null
)

enum class ConnectivityOverallStatus(val labelArabic: String, val labelEnglish: String) {
    OPTIMAL("اتصال مؤسسي مثالي", "Optimal Reachability"),
    DEGRADED("استجابة بطيئة / تأخير", "Degraded Connectivity"),
    CRITICAL_OUTAGE("انقطاع حرج في المسارات", "Critical Outage"),
    CHECKING("جاري فحص المسارات...", "Checking Reachability...")
}

data class ConnectivityHealthSummary(
    val totalEndpoints: Int,
    val reachableCount: Int,
    val degradedCount: Int,
    val unreachableCount: Int,
    val averageLatencyMs: Long,
    val overallStatus: ConnectivityOverallStatus,
    val lastCheckedTimestamp: String,
    val endpoints: List<EndpointHealthResult>
)

// ==========================================
// MANAGED NETWORKS & MULTI-NETWORK CONTROL
// ==========================================

enum class NetworkSecurityZone(val labelArabic: String, val labelEnglish: String) {
    ENTERPRISE_CORE("النطاق الداخلي المحمي", "Enterprise Protected Core"),
    DMZ_PUBLIC("منطقة منزوعة السلاح DMZ", "Demilitarized Zone (DMZ)"),
    ISOLATED_IOT("شبكة أجهزة إنترنت الأشياء المعزولة", "Isolated IoT Network"),
    CLOUD_SD_WAN("شبكة الفروع السحابية SD-WAN", "Cloud SD-WAN Branch"),
    GUEST_PORTAL("شبكة الزوار المؤقتة", "Guest Portal Network")
}

enum class NetworkConnectionStatus(val labelArabic: String, val labelEnglish: String) {
    CONNECTED("متصل ومربوط بالمنظومة", "Connected & Linked"),
    ISOLATED("معزول أمنياً", "Isolated / Standalone"),
    SYNCING("جاري المزامنة والتحليل", "Syncing & Inspecting"),
    DEGRADED("أداء منخفض", "Performance Degraded")
}

data class ManagedNetwork(
    val id: String,
    val name: String,
    val nameArabic: String,
    val cidr: String,
    val vlanId: Int,
    val gatewayIp: String,
    val dnsPrimary: String = "1.1.1.1",
    val securityZone: NetworkSecurityZone = NetworkSecurityZone.ENTERPRISE_CORE,
    val isConnected: Boolean = true,
    val bandwidthLimitMbps: Float = 1000f,
    val currentTrafficMbps: Float = 120f,
    val deviceCount: Int = 8,
    val status: NetworkConnectionStatus = NetworkConnectionStatus.CONNECTED,
    val latencyMs: Long = 2L,
    val createdAt: String = "2026-09-23 08:00",
    val notes: String = "",
    val isExpandedToMax: Boolean = false,
    val maxAllowedHosts: Int = 254,
    val originalCidr: String = cidr,
    val multiVlanTrunkEnabled: Boolean = false,
    val fiberBackhaulSpeedGbps: Float = 1.0f,
    val assignedOwnerUsername: String = "najm_client",
    val assignedRouterCount: Int = 2,
    val connectedPeopleCount: Int = 6
)

// ==========================================
// USER AUTHENTICATION & MULTI-LOGIN MODELS
// ==========================================

enum class NetworkUserRole {
    USER, ADMIN
}

enum class LoginIdentifierType(val labelArabic: String, val labelEnglish: String) {
    EMAIL("البريد الإلكتروني", "Email Address"),
    USERNAME("اسم المستخدم", "Username"),
    PHONE("رقم الهاتف", "Phone Number")
}

data class NetworkUserAccount(
    val id: String,
    val username: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val fullName: String,
    val assignedNetworkId: String,
    val role: NetworkUserRole = NetworkUserRole.USER,
    val registeredAt: String = "2026-09-01",
    val lastLogin: String = "2026-09-23 09:15"
)

// ==========================================
// INTERNET VOUCHER & RECHARGE SYSTEM
// ==========================================

data class VoucherRechargeRecord(
    val id: String,
    val scratchCode: String,
    val quotaAddedGB: Float,
    val timestamp: String,
    val packageTitle: String = "باقة الشحن السريع"
)

data class NetworkInternetVoucher(
    val cardCode: String,
    val networkId: String,
    val planNameArabic: String = "باقة الألياف الضوئية الذهبية (Ultra Fiber)",
    val planNameEnglish: String = "Ultra Fiber Gold 50GB",
    val totalQuotaGB: Float = 50.0f,
    val usedQuotaGB: Float = 14.8f,
    val remainingQuotaGB: Float = 35.2f,
    val validityDaysLeft: Int = 21,
    val expiryDate: String = "2026-10-18",
    val maxSpeedMbps: Float = 150f,
    val rechargeHistory: List<VoucherRechargeRecord> = emptyList()
)

// ==========================================
// CONNECTED USERS, ROUTERS & CLIENT TELEMETRY
// ==========================================

enum class ClientDeviceType(val displayNameArabic: String, val displayNameEnglish: String) {
    SMARTPHONE("هاتف ذكي", "Smartphone"),
    LAPTOP("كمبيوتر محمول", "Laptop"),
    TABLET("جهاز لوحي (تابلت)", "Tablet"),
    SMART_TV("شاشة تلفاز ذكية", "Smart TV"),
    GAMING_CONSOLE("منصة ألعاب", "Gaming Console"),
    IOT_DEVICE("جهاز ذكي IoT", "IoT Device"),
    DESKTOP("حاسوب مكتبي", "Desktop PC"),
    ROUTER_AP("موجه / نقطة وصول", "Router / Access Point")
}

data class ConnectedNetworkClient(
    val id: String,
    val networkId: String,
    val clientName: String,
    val deviceType: ClientDeviceType,
    val ip: String,
    val macAddress: String,
    val connectedRouterId: String,
    val connectedRouterName: String,
    val downloadSpeedMbps: Float,
    val uploadSpeedMbps: Float,
    val latencyMs: Long,
    val totalConsumedMB: Float,
    val signalStrengthDbm: Int = -52,
    val connectedSince: String = "08:15 AM",
    val isBlocked: Boolean = false,
    val qosPriority: String = "HIGH"
)

// ==========================================
// SERVICE TURBO BOOSTER & ACCELERATOR
// ==========================================

data class ServiceBoosterState(
    val isBoostActive: Boolean = false,
    val boostLevelPercent: Int = 45,
    val throughputMultiplier: Float = 1.45f,
    val latencyReductionMs: Float = 12.8f,
    val packetCompressionRatio: Float = 1.65f,
    val lastBoostTimestamp: String? = null,
    val activeOptimizations: List<String> = listOf(
        "Path MTU Discovery 9000 (Jumbo Frames)",
        "TCP BBR Congestion Control Accelerator",
        "Zero-Copy Memory Packet Ring Buffer",
        "Hardware Offload & Real-time CPU Core Affinity",
        "ARP / DNS Cache Deep Acceleration & Pre-fetch"
    )
)

// ==========================================
// AUTONOMOUS MASTER ORCHESTRATOR SERVER
// ==========================================

enum class TaskExecStatus(val labelArabic: String, val labelEnglish: String) {
    SUCCESS("تم بنجاح", "Success"),
    RUNNING("قيد التنفيذ", "Executing"),
    SCHEDULED("مجدول", "Scheduled"),
    FAILED("إخفاق", "Failed")
}

data class OrchestratorTask(
    val id: String,
    val titleArabic: String,
    val titleEnglish: String,
    val category: String, // "EXECUTE", "TRACK", "ORDER", "PREPARE", "SMOOTH_OPS"
    val status: TaskExecStatus = TaskExecStatus.SUCCESS,
    val executedAt: String,
    val durationMs: Long = 12L,
    val details: String = ""
)

data class MasterOrchestratorServer(
    val id: String = "SRV-ORCHESTRATOR-01",
    val name: String = "خادم المايسترو والتشغيل الذاتي المركزي",
    val englishName: String = "NetGuard Autonomous Core Orchestrator Server",
    val ip: String = "192.168.1.5",
    val status: String = "RUNNING_OPTIMAL", // RUNNING_OPTIMAL, REBALANCING, MAINTENANCE
    val uptime: String = "99.999% (74d 18h)",
    val cpuUsagePercent: Float = 14.5f,
    val ramUsagePercent: Float = 28.2f,
    val activeThreads: Int = 128,
    val totalTasksExecuted: Int = 18450,
    val pendingTasks: Int = 0,
    val healthScore: Int = 100,
    val autonomousModeActive: Boolean = true,
    val lastHeartbeat: String = "Just now",
    val recentTasks: List<OrchestratorTask> = emptyList(),
    val capabilitiesSummary: List<String> = listOf(
        "يعمل على مدار الساعة بإتاحة 99.999%",
        "ينفذ إجراءات المعالجة التلقائية وحظر التهديدات لحظياً",
        "يتتبع تدفق الحزم وزمن الاستجابة بدقة ميكروثانية",
        "يرتب المهام حسب الأولوية وجدولة الأرشفة الدورية",
        "يجهز البيئات والشبكات الفرعية وقواعد الجدران النارية",
        "يسير كافة الخدمات بسلاسة وانسيابية تامة دون انقطاع"
    )
)

// ==========================================
// FULL SYSTEM REFRESH & APP UPDATE STATE
// ==========================================

data class FullAppUpdateState(
    val isUpdating: Boolean = false,
    val progressPercent: Float = 0f,
    val currentStepArabic: String = "",
    val currentStepEnglish: String = "",
    val lastUpdatedTimestamp: String? = "2026-09-23 08:30:00",
    val updatedComponentsCount: Int = 28,
    val updateSummary: String? = null
)






