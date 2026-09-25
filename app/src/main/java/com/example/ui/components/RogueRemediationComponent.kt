package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.data.model.DeviceMonitoringConfig
import com.example.data.model.RogueDevice
import com.example.data.model.RogueStatus
import com.example.data.model.ThreatLevel
import com.example.ui.MainViewModel
import com.example.ui.theme.*

/**
 * Remediation Component for Rogue Devices
 * Lists detected unauthorized or rogue endpoints and provides an 'Isolate' button
 * to trigger network access blocking, alongside comprehensive Device Monitoring Setup
 * ("اعداد مراقبة الجهاز") for configuring traffic inspection, sweep intervals, and auto-quarantine.
 */
@Composable
fun RogueRemediationComponent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDark) { getNetGuardColors(isDark) }

    val rogueDevices by viewModel.rogueDevices.collectAsState()
    val monitoringConfigs by viewModel.deviceMonitoringConfigs.collectAsState()

    var selectedDeviceForSetup by remember { mutableStateOf<RogueDevice?>(null) }
    var filterStatus by remember { mutableStateOf<RogueStatus?>(null) }

    val isolatedCount = rogueDevices.count { it.status == RogueStatus.ISOLATED }
    val activeThreatCount = rogueDevices.count { it.status == RogueStatus.NEW || it.status == RogueStatus.INVESTIGATING }
    val trustedCount = rogueDevices.count { it.status == RogueStatus.TRUSTED }

    val displayedDevices = remember(rogueDevices, filterStatus) {
        if (filterStatus == null) rogueDevices else rogueDevices.filter { it.status == filterStatus }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("remediation_rogue_component"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (activeThreatCount > 0) colors.statusCrimson.copy(alpha = 0.6f) else colors.cardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. Header & Live Remediation Status
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
                            .background(colors.statusCrimson.copy(alpha = 0.15f))
                            .border(1.dp, colors.statusCrimson.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = colors.statusCrimson,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "إجراءات المعالجة وعزل الأجهزة الدخيلة" else "ROGUE DEVICE REMEDIATION & ACCESS BLOCKING",
                            color = colors.statusCrimson,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isArabic) "عزل فوري للشبكة واعداد مراقبة الجهاز" else "Real-Time Quarantine & Device Monitoring Setup",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Trigger network sweep button
                IconButton(
                    onClick = { viewModel.triggerScan() },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("remediation_refresh_sweep_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sweep Network",
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = if (isArabic)
                    "رصد الأجهزة غير المصرح بها وفرض العزل الفوري على مستوى جدار الحماية (Firewall) وحظر بروتوكول ARP لمنع الوصول إلى الشبكة الداخلية."
                else
                    "Detects rogue or unauthorized hardware assets and executes instant ARP isolation, MAC filtering, and gateway firewall blocks to prevent unauthorized lateral movement.",
                color = colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            // ==========================================
            // 2. Metrics Bar (Rogue, Isolated, Trusted)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RemediationStatBadge(
                    label = if (isArabic) "نشطة / خطر" else "Active Threats",
                    count = activeThreatCount,
                    color = colors.statusCrimson,
                    isSelected = filterStatus == RogueStatus.NEW,
                    onClick = { filterStatus = if (filterStatus == RogueStatus.NEW) null else RogueStatus.NEW },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                RemediationStatBadge(
                    label = if (isArabic) "معزولة / محظورة" else "Isolated / Blocked",
                    count = isolatedCount,
                    color = colors.statusAmber,
                    isSelected = filterStatus == RogueStatus.ISOLATED,
                    onClick = { filterStatus = if (filterStatus == RogueStatus.ISOLATED) null else RogueStatus.ISOLATED },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                RemediationStatBadge(
                    label = if (isArabic) "موثوقة معتمدة" else "Whitelisted",
                    count = trustedCount,
                    color = colors.statusGreen,
                    isSelected = filterStatus == RogueStatus.TRUSTED,
                    onClick = { filterStatus = if (filterStatus == RogueStatus.TRUSTED) null else RogueStatus.TRUSTED },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = colors.cardBorder, thickness = 0.8.dp)

            // ==========================================
            // 3. Rogue Devices Remediation List
            // ==========================================
            if (displayedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = colors.statusGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = if (isArabic) "لا توجد أجهزة دخيلة في هذه الحالة" else "No rogue devices matching filter",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    displayedDevices.forEach { device ->
                        RogueDeviceRemediationItem(
                            device = device,
                            isDark = isDark,
                            isArabic = isArabic,
                            colors = colors,
                            config = monitoringConfigs[device.id],
                            onIsolate = { viewModel.isolateRogue(device.id) },
                            onRestore = { viewModel.restoreRogue(device.id) },
                            onTrust = { viewModel.trustRogue(device.id) },
                            onOpenSetup = { selectedDeviceForSetup = device }
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // 4. Device Monitoring Setup Dialog (اعداد مراقبة الجهاز)
    // ==========================================
    selectedDeviceForSetup?.let { device ->
        val currentConfig = monitoringConfigs[device.id] ?: DeviceMonitoringConfig(
            deviceId = device.id,
            deviceName = "${device.vendor} (${device.ip})",
            ip = device.ip,
            mac = device.mac
        )

        DeviceMonitoringSetupDialog(
            device = device,
            initialConfig = currentConfig,
            isDark = isDark,
            isArabic = isArabic,
            colors = colors,
            onDismiss = { selectedDeviceForSetup = null },
            onSave = { updatedConfig ->
                viewModel.updateDeviceMonitoringConfig(updatedConfig)
                selectedDeviceForSetup = null
            }
        )
    }
}

/**
 * Individual Rogue Device Item Card with Isolate and Device Monitoring Setup Controls
 */
@Composable
private fun RogueDeviceRemediationItem(
    device: RogueDevice,
    isDark: Boolean,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    config: DeviceMonitoringConfig?,
    onIsolate: () -> Unit,
    onRestore: () -> Unit,
    onTrust: () -> Unit,
    onOpenSetup: () -> Unit
) {
    val isIsolated = device.status == RogueStatus.ISOLATED
    val isTrusted = device.status == RogueStatus.TRUSTED

    val cardBorderColor = when {
        isIsolated -> colors.statusCrimson
        isTrusted -> colors.statusGreen.copy(alpha = 0.5f)
        else -> colors.statusAmber.copy(alpha = 0.7f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rogue_item_${device.id}"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(cardBorderColor)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title & Threat Badge
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
                                if (isIsolated) colors.statusCrimson.copy(alpha = 0.2f)
                                else if (isTrusted) colors.statusGreen.copy(alpha = 0.2f)
                                else colors.statusAmber.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isIsolated -> Icons.Default.Block
                                isTrusted -> Icons.Default.CheckCircle
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = when {
                                isIsolated -> colors.statusCrimson
                                isTrusted -> colors.statusGreen
                                else -> colors.statusAmber
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = device.vendor.ifBlank { if (isArabic) "جهاز دخيل غير معروف" else "Unknown Rogue Endpoint" },
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${device.ip}  •  ${device.mac}",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Threat Level Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (device.threatLevel) {
                                ThreatLevel.CRITICAL -> colors.statusCrimson.copy(alpha = 0.2f)
                                ThreatLevel.HIGH -> colors.statusAmber.copy(alpha = 0.2f)
                                else -> colors.primaryAccent.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = device.threatLevel.name,
                        color = when (device.threatLevel) {
                            ThreatLevel.CRITICAL -> colors.statusCrimson
                            ThreatLevel.HIGH -> colors.statusAmber
                            else -> colors.primaryAccent
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Access Status & Unauthorized Reason
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.cardBackground)
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "حالة الوصول للشبكة:" else "Network Access State:",
                            color = colors.textSecondary,
                            fontSize = 10.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isIsolated) colors.statusCrimson else if (isTrusted) colors.statusGreen else colors.statusAmber)
                            )
                            Text(
                                text = when {
                                    isIsolated -> if (isArabic) "الوصول محظور (معزول)" else "BLOCKED (ISOLATED)"
                                    isTrusted -> if (isArabic) "مصرح وموثوق" else "AUTHORIZED (ONLINE)"
                                    else -> if (isArabic) "متصل نشط (يشكل تهديداً)" else "ACTIVE (RESTRICTED)"
                                },
                                color = if (isIsolated) colors.statusCrimson else if (isTrusted) colors.statusGreen else colors.statusAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "${if (isArabic) "السبب / النشاط: " else "Reason: "}${device.unauthorizedReason}",
                        color = colors.textPrimary,
                        fontSize = 11.sp
                    )

                    if (device.openPorts.isNotEmpty()) {
                        Text(
                            text = "${if (isArabic) "المنافذ المكشوفة: " else "Open Ports: "}TCP ${device.openPorts.joinToString(", ")}",
                            color = colors.statusAmber,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    config?.let {
                        Text(
                            text = "${if (isArabic) "المراقبة المخصصة: " else "Watchdog Profile: "}دورة ${it.monitoringIntervalSeconds}ث  •  عزل تلقائي: ${if (it.autoIsolateOnThreat) "مفعل" else "معطل"}",
                            color = colors.primaryAccent,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // ==========================================
            // Interactive Buttons (Isolate / Unblock, Setup, Whitelist)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PRIMARY: ISOLATE / BLOCK BUTTON
                if (!isIsolated) {
                    Button(
                        onClick = onIsolate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.statusCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(38.dp)
                            .testTag("isolate_button_${device.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "عزل وحظر الشبكة" else "Isolate",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onRestore,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.statusGreen),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(colors.statusGreen)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(38.dp)
                            .testTag("restore_button_${device.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "إلغاء الحظر" else "Restore Access",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // DEVICE MONITORING SETUP BUTTON (اعداد مراقبة الجهاز)
                OutlinedButton(
                    onClick = onOpenSetup,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(colors.primaryAccent)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(38.dp)
                        .testTag("device_monitoring_setup_button_${device.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsSuggest,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "اعداد مراقبة الجهاز" else "Monitor Setup",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // TRUST / WHITELIST BUTTON
                if (!isTrusted) {
                    IconButton(
                        onClick = onTrust,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cardBackground)
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                            .testTag("trust_button_${device.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Whitelist Device",
                            tint = colors.statusGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Device Monitoring Setup Dialog ("اعداد مراقبة الجهاز")
 * Configures continuous watchdog telemetry, sweep rates, DPI, and automated isolation rules.
 */
@Composable
fun DeviceMonitoringSetupDialog(
    device: RogueDevice,
    initialConfig: DeviceMonitoringConfig,
    isDark: Boolean,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onDismiss: () -> Unit,
    onSave: (DeviceMonitoringConfig) -> Unit
) {
    var interval by remember { mutableStateOf(initialConfig.monitoringIntervalSeconds) }
    var autoIsolate by remember { mutableStateOf(initialConfig.autoIsolateOnThreat) }
    var deepPacketInspection by remember { mutableStateOf(initialConfig.deepPacketInspection) }
    var arpSpoofDefense by remember { mutableStateOf(initialConfig.arpSpoofDefense) }
    var portScanWatchdog by remember { mutableStateOf(initialConfig.portScanWatchdog) }
    var packetRateThreshold by remember { mutableStateOf(initialConfig.packetRateThreshold.toFloat()) }
    var notifyTelegram by remember { mutableStateOf(initialConfig.notifyOnTelegram) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("device_monitoring_setup_dialog"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.primaryAccent))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
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
                                .background(colors.primaryAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isArabic) "اعداد مراقبة الجهاز" else "DEVICE MONITORING SETUP",
                                color = colors.primaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = device.vendor.ifBlank { device.ip },
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                // Device Identity Info Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "IP: ${device.ip}", color = colors.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "MAC: ${device.mac}", color = colors.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // 1. Monitoring Frequency Selector (دورة المراقبة)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isArabic) "دورة فحص ومسح الجهاز (ثواني):" else "Monitoring Sweep Interval:",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5 to "5s (Aggressive)", 10 to "10s (Standard)", 30 to "30s (Balanced)", 60 to "60s (Periodic)").forEach { (sec, label) ->
                            val isSelected = interval == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) colors.primaryAccent.copy(alpha = 0.25f) else colors.surface)
                                    .border(1.dp, if (isSelected) colors.primaryAccent else colors.cardBorder, RoundedCornerShape(6.dp))
                                    .clickable { interval = sec }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${sec}s",
                                    color = if (isSelected) colors.primaryAccent else colors.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // 2. Monitoring Switches
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Auto Isolate Switch
                    MonitoringToggleRow(
                        title = if (isArabic) "عزل فوري وتلقائي عند رصد خطر" else "Auto-Isolate on Threat",
                        subtitle = if (isArabic) "حظر الوصول للشبكة مباشرة دون انتظار موافقة يدوية" else "Automatically enforce firewall block when anomaly is detected",
                        checked = autoIsolate,
                        onCheckedChange = { autoIsolate = it },
                        colors = colors
                    )

                    Divider(color = colors.cardBorder, thickness = 0.5.dp)

                    // Deep Packet Inspection (DPI)
                    MonitoringToggleRow(
                        title = if (isArabic) "فحص الحزم المعمق (DPI L7)" else "Deep Packet Inspection (DPI)",
                        subtitle = if (isArabic) "تحليل ترويسات ومحتوى البروتوكولات لرصد الثغرات" else "Examine application payloads for exploit signatures",
                        checked = deepPacketInspection,
                        onCheckedChange = { deepPacketInspection = it },
                        colors = colors
                    )

                    Divider(color = colors.cardBorder, thickness = 0.5.dp)

                    // ARP Spoof Defense
                    MonitoringToggleRow(
                        title = if (isArabic) "حماية تثبيت ARP ومنع التسميم" else "ARP Spoof & Poisoning Defense",
                        subtitle = if (isArabic) "منع هجمات Man-in-the-Middle وتكرار عناوين MAC" else "Enforce static gateway MAC bindings against spoofing",
                        checked = arpSpoofDefense,
                        onCheckedChange = { arpSpoofDefense = it },
                        colors = colors
                    )

                    Divider(color = colors.cardBorder, thickness = 0.5.dp)

                    // Port Scanning Watchdog
                    MonitoringToggleRow(
                        title = if (isArabic) "مراقبة وحظر مسح المنافذ (SYN Watchdog)" else "Port Sweep & SYN Flood Watchdog",
                        subtitle = if (isArabic) "رصد محاولات مسح المنافذ العشوائية وحظر المصدر" else "Flag and throttle aggressive port enumeration probes",
                        checked = portScanWatchdog,
                        onCheckedChange = { portScanWatchdog = it },
                        colors = colors
                    )
                }

                // 3. Packet Rate Threshold Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "حد تدفق الحزم بالثانية:" else "Packet Rate Limit Threshold:",
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${packetRateThreshold.toInt()} pkts/s",
                            color = colors.primaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = packetRateThreshold,
                        onValueChange = { packetRateThreshold = it },
                        valueRange = 200f..2000f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primaryAccent,
                            activeTrackColor = colors.primaryAccent,
                            inactiveTrackColor = colors.cardBorder
                        )
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)
                    ) {
                        Text(text = if (isArabic) "إلغاء" else "Cancel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val updated = initialConfig.copy(
                                monitoringIntervalSeconds = interval,
                                autoIsolateOnThreat = autoIsolate,
                                deepPacketInspection = deepPacketInspection,
                                arpSpoofDefense = arpSpoofDefense,
                                portScanWatchdog = portScanWatchdog,
                                packetRateThreshold = packetRateThreshold.toInt(),
                                notifyOnTelegram = notifyTelegram
                            )
                            onSave(updated)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_monitoring_config_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "حفظ إعدادات المراقبة" else "Save Settings",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonitoringToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: NetGuardThemeColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 9.sp,
                lineHeight = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.surface,
                checkedTrackColor = colors.primaryAccent,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.cardBorder
            ),
            modifier = Modifier.scale(0.85f)
        )
    }
}

@Composable
private fun RemediationStatBadge(
    label: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else colors.surface)
            .border(1.dp, if (isSelected) color else colors.cardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$count",
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                color = if (isSelected) colors.textPrimary else colors.textSecondary,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
