package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.NetworkGuardRepository
import com.example.data.service.PdfExportResult
import com.example.data.service.PdfReportService
import com.example.data.service.ServerMetricsExportFormat
import com.example.data.service.ServerMetricsExportResult
import com.example.data.service.ServerMetricsExportService
import com.example.data.service.ai.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    MY_NETWORK("My Network"),
    AI_HUB("AI Security Studio"),
    TOPOLOGY("Topology Map"),
    DEVICES("Inventory"),
    INTRUSION("Threat IDS"),
    ROGUE_DETECTOR("Rogue Guard"),
    REMEDIATION("SSH Remediation"),
    REPORTS("SLA Reports"),
    ADMIN_PANEL("Admin Panel"),
    SETTINGS("Settings"),
    SUPPORT("Customer Support & FAQs")
}

class MainViewModel(
    val repository: NetworkGuardRepository = NetworkGuardRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Multi-User Authentication, Personal Network, Vouchers & Client Telemetry
    val userAccounts = repository.userAccounts
    val currentLoggedInUser = repository.currentLoggedInUser
    val internetVouchers = repository.internetVouchers
    val connectedClients = repository.connectedClients

    val devices = repository.devices
    val rogueDevices = repository.rogueDevices
    val authorizedWhitelist = repository.authorizedWhitelist
    val whitelistAuditSummary = repository.whitelistAuditSummary
    val serverMetrics = repository.serverMetrics
    val bandwidthMetrics = repository.bandwidthMetrics
    val networkHealth = repository.networkHealth
    val criticalServers = repository.criticalServers
    val alerts = repository.alerts
    val remediations = repository.remediations
    val networkEvents = repository.networkEvents
    val topologyNodes = repository.topologyNodes
    val nodeTrafficMap = repository.nodeTrafficMap
    val autoRemediationEnabled = repository.autoRemediationEnabled
    val autoRemediationPolicies = repository.autoRemediationPolicies
    val globalRamThreshold = repository.globalRamThreshold
    val globalCpuThreshold = repository.globalCpuThreshold
    val serverThresholdRules = repository.serverThresholdRules
    val isScanning = repository.isScanning
    val backendStatus = repository.backendStatus
    val activePlan = repository.activePlan

    // Customer Service, FAQs & Web Portal
    val faqList = repository.faqList
    val customerInquiries = repository.customerInquiries
    val securityHealthAudit = repository.securityHealthAudit

    val supportWhatsAppNumber = NetworkGuardRepository.SUPPORT_WHATSAPP_RAW
    val supportWhatsAppDisplay = NetworkGuardRepository.SUPPORT_WHATSAPP_DISPLAY
    val supportPhoneNumber = NetworkGuardRepository.SUPPORT_PHONE_RAW
    val supportPhoneDisplay = NetworkGuardRepository.SUPPORT_PHONE_DISPLAY
    val officialWebsiteUrl = NetworkGuardRepository.OFFICIAL_WEBSITE_URL
    val officialWebsiteTitle = NetworkGuardRepository.OFFICIAL_WEBSITE_TITLE
    val officialWebsiteDomain = NetworkGuardRepository.OFFICIAL_WEBSITE_DOMAIN
    val chiefArchitectTitle = NetworkGuardRepository.CHIEF_ARCHITECT_TITLE

    // Realtime Threats & Notifications
    val realtimeThreatNotifications = repository.realtimeThreatNotifications
    val intrusionAttempts = repository.intrusionAttempts
    val vulnerabilities = repository.vulnerabilities

    // Admin Control Panel & Services
    val adminServices = repository.adminServices
    val adminAuditLogs = repository.adminAuditLogs
    val isAdminAuthenticated = repository.isAdminAuthenticated
    val adminEmail = repository.adminEmail

    // Managed Networks, Turbo Booster, Master Orchestrator & Full App Update
    val managedNetworks = repository.managedNetworks
    val serviceBoosterState = repository.serviceBoosterState
    val masterOrchestratorServer = repository.masterOrchestratorServer
    val fullAppUpdateState = repository.fullAppUpdateState

    // Firebase Firestore Cloud State
    val firestoreSyncStatus = repository.firestoreSyncStatus
    val firestoreLastSyncTimestamp = repository.firestoreLastSyncTimestamp
    val firestoreLastReport = repository.firestoreLastReport
    val isFirestoreRealtimeEnabled = repository.isFirestoreRealtimeEnabled

    // Theme & Localization State
    val isDarkMode = repository.isDarkMode
    val appLanguage = repository.appLanguage

    // Recharts Interactive Real-time Trend Series
    val rechartsTrendPoints = repository.rechartsTrendPoints

    // 24-Hour Network Traffic Trends (D3 / Recharts 24-Hour Time-Series Telemetry)
    val trafficTrend24hPoints = repository.trafficTrend24hPoints

    fun get24hTrafficSummary(): Traffic24hSummary = repository.get24hTrafficSummary()

    fun simulate24hTrafficSpike() {
        repository.simulate24hTrafficSpike()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic)
            "تمت محاكاة ذروة حركة مرور لآخر 24 ساعة (785.4 Mbps)"
        else
            "Simulated 24-hour network traffic surge (785.4 Mbps peak burst)."
    }

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // =========================================================================
    // AI INTELLIGENCE & GEMINI INTEGRATIONS
    // =========================================================================
    val aiService = GeminiIntelligenceService()

    // 1. Multi-Turn Chat
    private val _chatModelTier = MutableStateFlow(ChatModelTier.FLASH)
    val chatModelTier: StateFlow<ChatModelTier> = _chatModelTier.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(listOf(
        AiChatMessage(
            role = "model",
            content = "مرحباً بك في مركز استخبارات NetGuard الذكي (Gemini Enterprise Security AI). كيف يمكنني مساعدتك اليوم في تدقيق الشبكة، فحص ثغرات CVE، أو رصد هجمات DDoS؟",
            timestamp = "08:30:00",
            modelUsed = "gemini-3.5-flash"
        )
    ))
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun selectChatModelTier(tier: ChatModelTier) {
        _chatModelTier.value = tier
    }

    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank()) return
        val now = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val userMsg = AiChatMessage(
            role = "user",
            content = prompt,
            timestamp = now,
            modelUsed = _chatModelTier.value.modelName
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            val result = aiService.sendMultiTurnChatMessage(
                messages = _chatMessages.value,
                tier = _chatModelTier.value
            )
            when (result) {
                is AiResult.Success -> {
                    _chatMessages.value = _chatMessages.value + result.data
                }
                is AiResult.Error -> {
                    val errMsg = AiChatMessage(
                        role = "model",
                        content = "⚠️ ${result.message}",
                        timestamp = now,
                        modelUsed = _chatModelTier.value.modelName
                    )
                    _chatMessages.value = _chatMessages.value + errMsg
                }
            }
            _isChatLoading.value = false
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
    }

    // 2. Google Search Grounding
    private val _searchResult = MutableStateFlow<AiChatMessage?>(null)
    val searchResult: StateFlow<AiChatMessage?> = _searchResult.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    fun executeSearchGroundedThreatQuery(query: String) {
        if (query.isBlank()) return
        _isSearchLoading.value = true
        viewModelScope.launch {
            val res = aiService.executeSearchGroundedThreatQuery(query)
            if (res is AiResult.Success) {
                _searchResult.value = res.data
            }
            _isSearchLoading.value = false
        }
    }

    // 3. Google Maps Grounding
    private val _mapsResultText = MutableStateFlow<String?>(null)
    val mapsResultText: StateFlow<String?> = _mapsResultText.asStateFlow()

    private val _mapsLocations = MutableStateFlow<List<MapLocationPoint>>(emptyList())
    val mapsLocations: StateFlow<List<MapLocationPoint>> = _mapsLocations.asStateFlow()

    private val _isMapsLoading = MutableStateFlow(false)
    val isMapsLoading: StateFlow<Boolean> = _isMapsLoading.asStateFlow()

    fun executeMapsGroundedQuery(query: String) {
        if (query.isBlank()) return
        _isMapsLoading.value = true
        viewModelScope.launch {
            val res = aiService.executeMapsGroundedGeoQuery(query)
            if (res is AiResult.Success) {
                _mapsResultText.value = res.data.first
                _mapsLocations.value = res.data.second
            }
            _isMapsLoading.value = false
        }
    }

    // 4. Audio Transcription
    private val _audioTranscript = MutableStateFlow<String?>(null)
    val audioTranscript: StateFlow<String?> = _audioTranscript.asStateFlow()

    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    fun transcribeIncidentVoice(base64Audio: String? = null) {
        _isTranscribing.value = true
        viewModelScope.launch {
            val res = aiService.transcribeAudio(base64Audio)
            if (res is AiResult.Success) {
                _audioTranscript.value = res.data
            }
            _isTranscribing.value = false
        }
    }

    // 5. Veo 3 Video Studio
    private val _videoList = MutableStateFlow<List<VideoGenerationItem>>(listOf(
        VideoGenerationItem(
            prompt = "3D holographic NOC simulation: Enterprise firewall detecting and deflecting high-bandwidth DDoS attack burst with cyan shield topology fly-through",
            model = "veo-3.1-fast-generate-preview",
            aspectRatio = "16:9",
            status = "READY",
            videoPreviewUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            timestamp = "2026-09-23 08:15"
        )
    ))
    val videoList: StateFlow<List<VideoGenerationItem>> = _videoList.asStateFlow()

    private val _isVideoGenerating = MutableStateFlow(false)
    val isVideoGenerating: StateFlow<Boolean> = _isVideoGenerating.asStateFlow()

    fun generateVeoVideo(prompt: String, aspectRatio: String, isImageAnimation: Boolean = false) {
        if (prompt.isBlank()) return
        _isVideoGenerating.value = true
        viewModelScope.launch {
            val res = aiService.generateVeoVideo(prompt = prompt, aspectRatio = aspectRatio, isImageAnimation = isImageAnimation)
            if (res is AiResult.Success) {
                _videoList.value = listOf(res.data) + _videoList.value
            }
            _isVideoGenerating.value = false
        }
    }

    // 6. Image Studio (gemini-3.1-flash-image-preview)
    private val _imageList = MutableStateFlow<List<ImageGenerationItem>>(listOf(
        ImageGenerationItem(
            prompt = "Enterprise SOC dark cyber defense console with glowing neon cyan telemetry gauges and topology mesh",
            model = "gemini-3.1-flash-image-preview",
            aspectRatio = "1:1",
            resolution = "1K",
            status = "READY",
            imageBase64 = null,
            timestamp = "2026-09-23 08:20"
        )
    ))
    val imageList: StateFlow<List<ImageGenerationItem>> = _imageList.asStateFlow()

    private val _isImageGenerating = MutableStateFlow(false)
    val isImageGenerating: StateFlow<Boolean> = _isImageGenerating.asStateFlow()

    fun generateImage(prompt: String, aspectRatio: String = "1:1", resolution: String = "1K") {
        if (prompt.isBlank()) return
        _isImageGenerating.value = true
        viewModelScope.launch {
            val res = aiService.generateOrEditImage(prompt, aspectRatio, resolution)
            if (res is AiResult.Success) {
                _imageList.value = listOf(res.data) + _imageList.value
            }
            _isImageGenerating.value = false
        }
    }

    // 7. Lyria Music Studio
    private val _musicList = MutableStateFlow<List<MusicGenerationItem>>(listOf(
        MusicGenerationItem(
            prompt = "Cyberpunk high-tension NOC telemetry warning alert jingle with pulsing synths",
            model = "lyria-3-clip-preview",
            durationSeconds = 30,
            title = "NetGuard Alert Jingle #1",
            audioGenre = "Electronic / Cyber Threat Alert",
            status = "READY",
            timestamp = "2026-09-23 08:25"
        )
    ))
    val musicList: StateFlow<List<MusicGenerationItem>> = _musicList.asStateFlow()

    private val _isMusicGenerating = MutableStateFlow(false)
    val isMusicGenerating: StateFlow<Boolean> = _isMusicGenerating.asStateFlow()

    fun generateLyriaMusic(prompt: String, isShortClip: Boolean) {
        if (prompt.isBlank()) return
        _isMusicGenerating.value = true
        viewModelScope.launch {
            val res = aiService.generateLyriaMusic(prompt, isShortClip)
            if (res is AiResult.Success) {
                _musicList.value = listOf(res.data) + _musicList.value
            }
            _isMusicGenerating.value = false
        }
    }

    // 8. Live Voice (gemini-3.8-live)
    private val _isLiveVoiceListening = MutableStateFlow(false)
    val isLiveVoiceListening: StateFlow<Boolean> = _isLiveVoiceListening.asStateFlow()

    private val _liveVoiceTranscript = MutableStateFlow<List<String>>(listOf(
        "NetGuard Live Voice Assistant (gemini-3.8-live) جاهز للاستماع. قل: 'افحص حالة الشبكة' أو 'اعزل الجهاز 192.168.1.105'"
    ))
    val liveVoiceTranscript: StateFlow<List<String>> = _liveVoiceTranscript.asStateFlow()

    fun toggleLiveVoiceListening() {
        _isLiveVoiceListening.value = !_isLiveVoiceListening.value
    }

    fun submitLiveVoiceCommand(command: String) {
        if (command.isBlank()) return
        val list = _liveVoiceTranscript.value.toMutableList()
        list.add("You: $command")
        _liveVoiceTranscript.value = list

        viewModelScope.launch {
            val res = aiService.executeLiveVoiceInteraction(command)
            if (res is AiResult.Success) {
                val updated = _liveVoiceTranscript.value.toMutableList()
                updated.add(res.data)
                _liveVoiceTranscript.value = updated
            }
        }
    }

    // 9. Firebase Auth & Persistence
    private val _userEmail = MutableStateFlow("najmali238@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isFirebaseConnected = MutableStateFlow(true)
    val isFirebaseConnected: StateFlow<Boolean> = _isFirebaseConnected.asStateFlow()


    private val _currentSlaReport = MutableStateFlow(repository.generateSlaReport())
    val currentSlaReport: StateFlow<SlaReport> = _currentSlaReport.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun triggerScan() {
        viewModelScope.launch {
            repository.performLiveNetworkAudit()
            _userMessage.value = "Live network & device sweep completed."
        }
    }

    fun isolateRogue(rogueId: String) {
        repository.isolateRogueDevice(rogueId)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم عزل الجهاز الدخيل وحظر الوصول إلى الشبكة بنجاح" else "Rogue device isolated and network access blocked."
    }

    fun restoreRogue(rogueId: String) {
        repository.restoreRogueDevice(rogueId)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم إلغاء عزل الجهاز واستعادة الاتصال بالشبكة" else "Rogue device unblocked and network access restored."
    }

    val deviceMonitoringConfigs = repository.deviceMonitoringConfigs

    fun updateDeviceMonitoringConfig(config: DeviceMonitoringConfig) {
        repository.updateDeviceMonitoringConfig(config)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم حفظ إعدادات مراقبة الجهاز: ${config.deviceName}" else "Monitoring configuration saved for ${config.deviceName}"
    }

    // Device Thresholds (عتبات الأجهزة)
    val deviceThresholds = repository.deviceThresholds

    fun updateDeviceThreshold(threshold: DeviceThreshold) {
        repository.updateDeviceThreshold(threshold)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم حفظ عتبات الجهاز: ${threshold.deviceName}" else "Device thresholds saved for ${threshold.deviceName}"
    }

    fun removeDeviceThreshold(deviceId: String) {
        repository.removeDeviceThreshold(deviceId)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم حذف عتبات الجهاز" else "Device threshold removed."
    }

    fun resetDeviceThresholds() {
        repository.resetDeviceThresholds()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تمت استعادة العتبات القياسية لجميع الأجهزة" else "Device thresholds reset to enterprise defaults."
    }

    // Network Diagnostic Utility (فحص Ping وتشخيص الاتصال)
    val diagnosticSession = repository.diagnosticSession

    fun pingTarget(targetIp: String, count: Int = 4) {
        repository.pingTarget(targetIp, count)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "جارٍ فحص الاتصال (Ping) مع $targetIp..." else "Pinging $targetIp to verify connectivity..."
    }

    fun stopPing() {
        repository.stopPing()
    }

    fun clearDiagnosticSession() {
        repository.clearDiagnosticSession()
    }

    fun trustRogue(rogueId: String) {
        repository.trustRogueDevice(rogueId)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم توثيق الجهاز وإضافته للقائمة البيضاء المعتمدة" else "Device authorized, whitelisted, and added to inventory."
    }

    fun runWhitelistAuditScan() {
        viewModelScope.launch {
            val summary = repository.runRogueDetectionAudit()
            if (summary.unauthorizedCount > 0) {
                _userMessage.value = "Whitelist audit flagged ${summary.unauthorizedCount} unauthorized hardware asset(s)."
            } else {
                _userMessage.value = "All ${summary.totalDiscovered} network assets match authorized whitelist."
            }
        }
    }

    fun addDeviceToWhitelist(mac: String, name: String, vendor: String) {
        repository.addAuthorizedDevice(mac, name, vendor)
        _userMessage.value = "Device MAC $mac added to authorized whitelist."
    }

    fun removeDeviceFromWhitelist(id: String) {
        repository.removeAuthorizedDevice(id)
        _userMessage.value = "Device revoked from authorized whitelist."
    }

    fun pingServer(serverId: String, serverName: String) {
        viewModelScope.launch {
            val latency = repository.pingCriticalServer(serverId)
            _userMessage.value = "Ping probe to $serverName: $latency ms"
        }
    }

    fun executeRemediation(serverId: String, service: String, command: String) {
        viewModelScope.launch {
            val result = repository.executeSshRemediation(serverId, service, command)
            _userMessage.value = "SSH command executed: ${result.command}"
        }
    }

    fun toggleAutoRemediation(enabled: Boolean) {
        repository.toggleAutoRemediation(enabled)
        _userMessage.value = if (enabled) "Auto-Remediation Watchdog Active (24/7)" else "Auto-Remediation Watchdog Paused"
    }

    fun togglePolicy(policyId: String, enabled: Boolean) {
        repository.toggleAutoRemediationPolicy(policyId, enabled)
    }

    fun updatePolicyThresholds(policyId: String, ram: Float, cpu: Float) {
        repository.updateAutoRemediationThresholds(policyId, ram, cpu)
        _userMessage.value = "Auto-remediation thresholds updated."
    }

    fun triggerPolicyNow(policyId: String) {
        viewModelScope.launch {
            val result = repository.triggerAutoRemediationNow(policyId)
            if (result != null) {
                _userMessage.value = "Automated SSH execution: ${result.targetService} on ${result.serverName}"
            }
        }
    }

    fun addThresholdRule(rule: ServerThresholdRule) {
        repository.addThresholdRule(rule)
        _userMessage.value = "Created alert rule for ${rule.serverName}"
    }

    fun updateThresholdRule(rule: ServerThresholdRule) {
        repository.updateThresholdRule(rule)
        _userMessage.value = "Updated thresholds for ${rule.serverName}"
    }

    fun deleteThresholdRule(ruleId: String) {
        repository.deleteThresholdRule(ruleId)
        _userMessage.value = "Threshold rule deleted."
    }

    fun toggleThresholdRule(ruleId: String, enabled: Boolean) {
        repository.toggleThresholdRule(ruleId, enabled)
    }

    fun testThresholdRule(ruleId: String) {
        viewModelScope.launch {
            val action = repository.testThresholdRule(ruleId)
            if (action != null) {
                _userMessage.value = "Triggered threshold action: ${action.command}"
            } else {
                _userMessage.value = "Dispatched threshold breach notification alert."
            }
        }
    }

    fun evaluateAllThresholdsNow() {
        viewModelScope.launch {
            repository.evaluateCustomThresholdRules()
            _userMessage.value = "Evaluated all server load thresholds against live telemetry."
        }
    }

    fun sendTelegramTest() {
        viewModelScope.launch {
            val result = repository.sendTelegramTestAlert()
            result.onSuccess { msg ->
                _userMessage.value = "Telegram Alert: Success!"
            }.onFailure { err ->
                _userMessage.value = "Telegram Error: ${err.message}"
            }
        }
    }

    fun dismissAlert(alertId: String) {
        repository.dismissAlert(alertId)
    }

    fun refreshReport() {
        _currentSlaReport.value = repository.generateSlaReport()
        _userMessage.value = "Updated SLA & Uptime statistics."
    }

    fun selectPlan(plan: SubscriptionPlan) {
        repository.updateSubscriptionPlan(plan)
        _userMessage.value = "Switched to ${plan.title} plan."
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun simulateBurstTraffic(nodeId: String) {
        repository.simulateBurstTraffic(nodeId)
        _userMessage.value = "Injected network traffic burst on $nodeId"
    }

    fun dismissNetworkEvent(eventId: String) {
        repository.dismissNetworkEvent(eventId)
    }

    fun acknowledgeNetworkEvent(eventId: String) {
        repository.acknowledgeNetworkEvent(eventId)
        _userMessage.value = "Event acknowledged."
    }

    fun clearAllNetworkEvents() {
        repository.clearAllNetworkEvents()
        _userMessage.value = "Cleared all logged network events."
    }

    fun simulateNetworkEvent(type: NetworkEventType? = null) {
        val evt = repository.simulateNetworkEvent(type)
        _userMessage.value = "Logged live event: ${evt.title}"
    }

    fun reExecuteRemediationFromAlert(event: NetworkEventLog) {
        val details = event.remediationDetails ?: return
        viewModelScope.launch {
            val result = repository.executeSshRemediation(
                serverId = details.serverId,
                targetService = details.targetService,
                command = details.command,
                isAutomated = false,
                triggerReason = "Manual re-execution from event alert (${details.triggerReason})"
            )
            _userMessage.value = "Re-executed remediation for ${result.targetService} on ${result.serverName}"
        }
    }

    fun syncWithFirestore() {
        viewModelScope.launch {
            val report = repository.syncAllToFirestore()
            _userMessage.value = report.message
        }
    }

    fun restoreFromFirestore() {
        viewModelScope.launch {
            val success = repository.restoreAllFromFirestore()
            _userMessage.value = if (success) {
                "Restored network devices and event logs from Cloud Firestore."
            } else {
                "Cloud Firestore restore: No remote records found or sync error."
            }
        }
    }

    fun toggleFirestoreRealtimeSync(enabled: Boolean) {
        repository.toggleRealtimeFirestoreSync(enabled)
        _userMessage.value = if (enabled) {
            "Real-time Cloud Firestore synchronization active."
        } else {
            "Real-time Cloud Firestore synchronization paused."
        }
    }

    fun toggleDarkMode(enabled: Boolean? = null) {
        repository.toggleDarkMode(enabled)
        val isDark = repository.isDarkMode.value
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isDark) {
            if (isArabic) "تم تفعيل الوضع الليلي" else "Dark Cyber Theme Active"
        } else {
            if (isArabic) "تم تفعيل الوضع العادي (النهاري)" else "Light Enterprise Theme Active"
        }
    }

    fun setDarkMode(enabled: Boolean) {
        toggleDarkMode(enabled)
    }

    fun setAppLanguage(language: AppLanguage) {
        repository.setAppLanguage(language)
        _userMessage.value = if (language == AppLanguage.ARABIC) "تم تحويل واجهة التطبيق إلى اللغة العربية" else "App interface switched to English"
    }

    fun toggleAppLanguage() {
        repository.toggleAppLanguage()
        val lang = repository.appLanguage.value
        _userMessage.value = if (lang == AppLanguage.ARABIC) "تم تحويل واجهة التطبيق إلى اللغة العربية" else "App interface switched to English"
    }

    // ==========================================
    // CUSTOMER SERVICE & INQUIRIES ACTIONS
    // ==========================================
    fun submitCustomerInquiry(
        senderName: String,
        contactInfo: String,
        inquiryType: String,
        subject: String,
        message: String
    ): CustomerInquiry {
        val inquiry = repository.submitInquiry(senderName, contactInfo, inquiryType, subject, message)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم إرسال استفسارك بنجاح وتلقي الرد المعتمد ✓" else "Inquiry submitted successfully with verified response ✓"
        return inquiry
    }

    fun runSecurityHealthAudit(): SecurityHealthAudit {
        val audit = repository.runComprehensiveSecurityHealthCheck()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "اكتمل فحص الجاهزية الشامل: التقييم ${audit.grade}" else "Security readiness check completed: Grade ${audit.grade}"
        return audit
    }

    fun openWhatsAppSupport(context: Context, customMessage: String? = null) {
        val message = customMessage ?: if (repository.appLanguage.value == AppLanguage.ARABIC) {
            "السلام عليكم مهندس نجم الرئيس، أود الاستفسار حول خدمات وتطبيق NetGuard لحماية الشبكات."
        } else {
            "Hello Eng. Najm Al-Raees, I would like to inquire about NetGuard network security services."
        }
        val uri = android.net.Uri.parse("https://wa.me/$supportWhatsAppNumber?text=" + android.net.Uri.encode(message))
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            _userMessage.value = if (repository.appLanguage.value == AppLanguage.ARABIC) "رقم الواتساب: $supportWhatsAppDisplay" else "WhatsApp: $supportWhatsAppDisplay"
        }
    }

    fun dialSupportPhone(context: Context) {
        val uri = android.net.Uri.parse("tel:$supportPhoneNumber")
        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, uri).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            _userMessage.value = if (repository.appLanguage.value == AppLanguage.ARABIC) "رقم الاتصال المباشر: $supportPhoneDisplay" else "Direct Call: $supportPhoneDisplay"
        }
    }

    fun openOfficialWebsite(context: Context) {
        val uri = android.net.Uri.parse(officialWebsiteUrl)
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            _userMessage.value = officialWebsiteUrl
        }
    }

    // ==========================================
    // ADMIN ACTIONS
    // ==========================================
    fun authenticateAdmin(email: String, pass: String): Boolean {
        val success = repository.authenticateAdmin(email, pass)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (success) {
            if (isArabic) "تم التحقق وتسجيل الدخول كمسؤول للنظام بنجاح" else "Admin privileges verified. Welcome!"
        } else {
            if (isArabic) "فشل التحقق: يرجى التحقق من البريد وكلمة المرور وحل اختبار الأمان" else "Authentication failed. Check credentials and CAPTCHA."
        }
        return success
    }

    fun logoutAdmin() {
        repository.logoutAdmin()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم تسجيل الخروج من لوحة تحكم المشرف" else "Admin logged out successfully."
    }

    fun toggleAdminService(serviceId: String) {
        repository.toggleAdminService(serviceId)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم تحديث حالة الخدمة بنجاح" else "Service status updated."
    }

    fun triggerEmergencyLockdown() {
        val count = repository.triggerEmergencyLockdown()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم تفعيل حظر الطوارئ الشامل وعزل $count أجهزة دخيلة!" else "EMERGENCY LOCKDOWN: $count rogue devices isolated!"
    }

    // ==========================================
    // INTRUSION & VULNERABILITY ACTIONS
    // ==========================================
    fun blockAttackerIp(ip: String) {
        repository.blockAttackerIp(ip)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم حظر عنوان IP المهاجم ($ip) عبر جدار الحماية" else "Attacker IP $ip blocked on firewall."
    }

    fun patchVulnerability(vulnId: String) {
        repository.patchVulnerability(vulnId)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم تطبيق الترقيع الأمني للثغرة بنجاح" else "Security vulnerability patch applied."
    }

    fun simulateNewThreat() {
        val notif = repository.simulateNewThreat()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم رصد تهديد جديد: ${notif.titleArabic}" else "New threat detected: ${notif.title}"
    }

    fun dismissThreatNotification(notifId: String) {
        repository.dismissThreatNotification(notifId)
    }

    fun simulateRechartsTrafficSurge() {
        repository.simulateRechartsTrafficSurge()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم محاكاة تدفق مروري مفاجئ وطفرة في حمل الخوادم" else "Simulated live traffic burst & CPU load spike"
    }

    // ==========================================
    // PUSH NOTIFICATION SERVICE (TRAFFIC & THRESHOLD ALERTS)
    // ==========================================
    val trafficNotificationConfig = repository.trafficNotificationConfig
    val trafficNotificationHistory = repository.trafficNotificationHistory

    fun updateTrafficNotificationConfig(config: com.example.data.model.TrafficNotificationConfig) {
        repository.updateTrafficNotificationConfig(config)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم تحديث إعدادات خدمة الإشعارات وتجاوز العتبات" else "Push notification service settings updated."
    }

    fun updateTrafficThreshold(thresholdMbps: Float) {
        repository.updateTrafficThreshold(thresholdMbps)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم ضبط عتبة حركة المرور إلى ${thresholdMbps.toInt()} ميجابت/ث" else "Traffic threshold updated to ${thresholdMbps.toInt()} Mbps"
    }

    fun toggleTrafficNotifications(enabled: Boolean) {
        repository.toggleTrafficNotifications(enabled)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (enabled) {
            if (isArabic) "تم تفعيل خدمة إشعارات تجاوز حركة المرور" else "Traffic push notifications enabled."
        } else {
            if (isArabic) "تم إيقاف خدمة إشعارات تجاوز حركة المرور مؤقتاً" else "Traffic push notifications paused."
        }
    }

    fun triggerTrafficPushNotificationTest() {
        repository.triggerTrafficPushNotificationTest()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم إرسال إشعار فحص لتجاوز عتبة حركة المرور بنجاح 🔔" else "Traffic threshold push notification alert sent 🔔"
    }

    fun clearTrafficNotificationHistory() {
        repository.clearTrafficNotificationHistory()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم مسح سجل إشعارات حركة المرور" else "Traffic notification history cleared."
    }

    fun getDownloadableReport(): String {
        return repository.generateDownloadableReport("MARKDOWN")
    }

    // ==========================================
    // PDF ADMINISTRATIVE REPORT EXPORT SERVICE
    // ==========================================
    private val _lastPdfExportResult = MutableStateFlow<PdfExportResult?>(null)
    val lastPdfExportResult: StateFlow<PdfExportResult?> = _lastPdfExportResult.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    fun exportNetworkStatusAndSecurityPdf(context: Context): PdfExportResult {
        _isGeneratingPdf.value = true
        try {
            val pdfService = PdfReportService(context)
            val result = pdfService.generateAdministrativeReport(
                networkHealth = repository.networkHealth.value,
                bandwidthMetrics = repository.bandwidthMetrics.value,
                devices = repository.devices.value,
                serverMetrics = repository.serverMetrics.value,
                alerts = repository.alerts.value,
                intrusionAttempts = repository.intrusionAttempts.value,
                vulnerabilities = repository.vulnerabilities.value,
                rogueDevices = repository.rogueDevices.value,
                trafficNotificationHistory = repository.trafficNotificationHistory.value,
                adminAuditor = "Eng. Najm Al-Raees (najmali238@gmail.com)"
            )
            _lastPdfExportResult.value = result
            val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
            _userMessage.value = if (isArabic) "تم تصدير تقرير PDF لحالة الشبكة وسجلات الأمان للمراجعة الإدارية بنجاح (${result.fileSizeBytes / 1024} KB)"
                else "Exported Network & Security PDF Report for administrative review successfully (${result.fileSizeBytes / 1024} KB)"
            return result
        } finally {
            _isGeneratingPdf.value = false
        }
    }

    fun openGeneratedPdf(context: Context, result: PdfExportResult? = null) {
        val target = result ?: _lastPdfExportResult.value ?: return
        PdfReportService(context).openPdf(target)
    }

    fun shareGeneratedPdf(context: Context, result: PdfExportResult? = null) {
        val target = result ?: _lastPdfExportResult.value ?: return
        PdfReportService(context).sharePdf(target)
    }

    // ==========================================
    // DEDICATED CONNECTIVITY HEALTH MONITOR
    // ==========================================
    val connectivityHealthSummary: StateFlow<ConnectivityHealthSummary> = repository.connectivityHealthSummary
    val isCheckingConnectivity: StateFlow<Boolean> = repository.isCheckingConnectivity
    val enterpriseEndpoints: StateFlow<List<EnterpriseEndpoint>> = repository.enterpriseEndpoints

    fun triggerConnectivityHealthCheck() {
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "جاري فحص اتصال نقاط النهاية المؤسسية..." else "Probing enterprise connectivity endpoints..."
        repository.triggerConnectivityHealthCheck()
    }

    suspend fun runConnectivityHealthCheck(): ConnectivityHealthSummary {
        val summary = repository.checkConnectivityHealth()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "اكتمل فحص الاتصال: ${summary.reachableCount}/${summary.totalEndpoints} نقطة متاحة (${summary.averageLatencyMs}ms)"
            else "Connectivity probe completed: ${summary.reachableCount}/${summary.totalEndpoints} reachable (${summary.averageLatencyMs}ms)"
        return summary
    }

    // ==========================================
    // SERVER METRICS TELEMETRY ARCHIVE & EXPORT
    // ==========================================
    fun exportServerMetricsToFile(
        context: Context,
        format: ServerMetricsExportFormat,
        includeServices: Boolean = true,
        includeSummary: Boolean = true,
        filterServerId: String? = null
    ): ServerMetricsExportResult {
        val service = ServerMetricsExportService(context)
        val result = service.exportToFile(
            format = format,
            serverMetrics = repository.serverMetrics.value,
            criticalServers = repository.criticalServers.value,
            includeServices = includeServices,
            includeSummary = includeSummary,
            filterServerId = filterServerId
        )
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) {
            "تم تصدير وأرشفة بيانات السيرفرات بصيغة ${format.name} بنجاح (${result.fileSizeBytes / 1024} KB)"
        } else {
            "Exported server metrics archive as ${format.name} successfully (${result.fileSizeBytes / 1024} KB)"
        }
        return result
    }

    fun shareServerMetricsExport(context: Context, result: ServerMetricsExportResult) {
        ServerMetricsExportService(context).shareExport(result)
    }

    // ==========================================
    // MANAGED NETWORKS & ORCHESTRATION ACTIONS
    // ==========================================
    fun addManagedNetwork(
        name: String,
        nameArabic: String,
        cidr: String,
        vlanId: Int,
        gatewayIp: String,
        dnsPrimary: String = "1.1.1.1",
        securityZone: NetworkSecurityZone = NetworkSecurityZone.ENTERPRISE_CORE,
        bandwidthLimitMbps: Float = 1000f,
        notes: String = ""
    ) {
        val newNetwork = ManagedNetwork(
            id = "net-${System.currentTimeMillis()}",
            name = name,
            nameArabic = if (nameArabic.isNotBlank()) nameArabic else name,
            cidr = cidr,
            vlanId = vlanId,
            gatewayIp = gatewayIp,
            dnsPrimary = dnsPrimary,
            securityZone = securityZone,
            isConnected = true,
            bandwidthLimitMbps = bandwidthLimitMbps,
            currentTrafficMbps = (15..120).random().toFloat(),
            deviceCount = (3..15).random(),
            status = NetworkConnectionStatus.CONNECTED,
            latencyMs = (1L..4L).random(),
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            notes = notes
        )
        repository.addManagedNetwork(newNetwork)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تمت إضافة وربط الشبكة الجديدة بنجاح: ${newNetwork.nameArabic}"
            else "New network added and linked successfully: ${newNetwork.name}"
    }

    fun toggleNetworkConnection(networkId: String) {
        repository.toggleNetworkConnection(networkId)
    }

    fun isolateNetwork(networkId: String) {
        repository.isolateNetwork(networkId)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم عزل الشبكة أمنياً بالكامل وحظر حركة المرور"
            else "Network isolated and quarantined successfully"
    }

    fun deepScanNetwork(networkId: String) {
        repository.deepScanNetwork(networkId)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "اكتمل الفحص التشخيصي والأمني للشبكة"
            else "Network diagnostic deep scan completed"
    }

    fun deleteManagedNetwork(networkId: String) {
        repository.deleteManagedNetwork(networkId)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم حذف الشبكة من لوحة التحكم"
            else "Network removed from control panel"
    }

    // ==========================================
    // TURBO SERVICE BOOSTER ACTIONS
    // ==========================================
    fun toggleServiceBooster() {
        repository.toggleServiceBooster()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        val isActive = repository.serviceBoosterState.value.isBoostActive
        _userMessage.value = if (isActive) {
            if (isArabic) "تم تفعيل أداة تعزيز وتقوية الخدمة (+45% سرعة، تقليل التأخير، TCP BBR)"
            else "Turbo Service Booster activated (+45% throughput boost, low latency)"
        } else {
            if (isArabic) "تم إيقاف أداة تعزيز الخدمة"
            else "Turbo Service Booster deactivated"
        }
    }

    fun activateServiceBooster(level: Int = 50) {
        repository.activateServiceBooster(level)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم ضبط أداة تعزيز الخدمة إلى المستوى $level%"
            else "Service Booster set to level $level%"
    }

    // ==========================================
    // AUTONOMOUS MASTER ORCHESTRATOR ACTIONS
    // ==========================================
    fun triggerOrchestratorRebalance() {
        repository.triggerOrchestratorRebalance()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تمت إعادة موازنة وتسيير كافة مسارات وخدمات المنظومة بسلاسة"
            else "Master Orchestrator rebalance executed smoothly across all services"
    }

    fun reorderAndScheduleTasks() {
        repository.reorderAndScheduleTasks()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تمت إعادة جدولة وترتيب طابور العمليات والأرشفة"
            else "Task queue re-ordered and scheduled successfully"
    }

    // ==========================================
    // FULL APP & SYSTEM REFRESH / UPDATE ACTION
    // ==========================================
    fun executeFullAppUpdate() {
        viewModelScope.launch {
            val result = repository.executeFullAppUpdate()
            val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
            _userMessage.value = if (isArabic) "تم تحديث المنظومة والتطبيق بالكامل بنجاح (${result.updatedComponentsCount} مكون)"
                else "Full app and system update finished successfully (${result.updatedComponentsCount} components)"
        }
    }

    // ==========================================
    // MULTI-LOGIN & USER AUTHENTICATION ACTIONS
    // ==========================================
    fun loginUser(identifier: String, password: String, identifierType: LoginIdentifierType): Boolean {
        val (success, message) = repository.loginUser(identifier, password, identifierType)
        _userMessage.value = message
        return success
    }

    fun logoutUser() {
        repository.logoutUser()
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم تسجيل الخروج بنجاح" else "Logged out successfully"
    }

    fun switchLoggedInUser(userId: String) {
        repository.switchLoggedInUser(userId)
    }

    // ==========================================
    // MAXIMUM NETWORK EXPANSION ACTION
    // ==========================================
    fun expandNetworkToMax(networkId: String) {
        val success = repository.expandNetworkToMax(networkId)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (success) {
            if (isArabic) "تم توسيع الشبكة بنجاح إلى أكبر حد ممكن (Super Subnet + ألياف 10Gbps + Multi-VLAN)"
            else "Network expanded to maximum capacity (Super Subnet + 10Gbps Fiber + Multi-VLAN)"
        } else {
            if (isArabic) "تعذر توسيع الشبكة" else "Failed to expand network"
        }
    }

    // ==========================================
    // INTERNET VOUCHER & RECHARGE ACTION
    // ==========================================
    val internetCardCredits = repository.internetCardCredits

    fun rechargeVoucher(networkId: String, scratchCode: String, quotaGB: Float) {
        val (success, message) = repository.rechargeVoucher(networkId, scratchCode, quotaGB)
        _userMessage.value = message
    }

    fun redeemCardVoucher(networkId: String, voucherCode: String): RedemptionResult {
        val result = repository.redeemCardVoucher(networkId, voucherCode)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        when (result) {
            is RedemptionResult.Success -> {
                _userMessage.value = if (isArabic) result.messageArabic else result.messageEnglish
            }
            is RedemptionResult.Failure -> {
                _userMessage.value = if (isArabic) result.messageArabic else result.messageEnglish
            }
        }
        return result
    }

    // ==========================================
    // CONNECTED CLIENT TELEMETRY & ROUTER CONTROLS
    // ==========================================
    fun toggleBlockClient(clientId: String) {
        repository.toggleBlockClient(clientId)
    }

    fun setClientQoS(clientId: String, priority: String) {
        repository.setClientQoS(clientId, priority)
        val isArabic = repository.appLanguage.value == AppLanguage.ARABIC
        _userMessage.value = if (isArabic) "تم تحديث أولوية جودة الخدمة (QoS: $priority)" else "Client QoS priority updated to $priority"
    }

    // ==========================================
    // DEDICATED NETWORK REPORT EXPORT
    // ==========================================
    fun generateNetworkReport(network: ManagedNetwork): String {
        val voucher = repository.internetVouchers.value[network.id]
        val clients = repository.connectedClients.value.filter { it.networkId == network.id }
        return repository.generateNetworkReportText(network, voucher, clients)
    }
}


