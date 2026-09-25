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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.DeviceStatus
import com.example.data.model.DeviceType
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun NetworkExecutiveSummaryCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()

    val devices by viewModel.devices.collectAsState()
    val rogues by viewModel.rogueDevices.collectAsState()
    val health by viewModel.networkHealth.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val backendStatus by viewModel.backendStatus.collectAsState()

    val colors = remember(isDark) { getNetGuardColors(isDark) }
    val isArabic = language == AppLanguage.ARABIC

    // Calculations
    val onlineDevicesCount = devices.count { it.status == DeviceStatus.ONLINE }
    val warningDevicesCount = devices.count { it.status == DeviceStatus.WARNING }
    val offlineDevicesCount = devices.count { it.status == DeviceStatus.OFFLINE }
    val totalDevicesCount = devices.size

    val activeRogues = rogues.filter { it.status.name != "ISOLATED" && it.status.name != "TRUSTED" }
    val activeRoguesCount = activeRogues.size
    val isolatedCount = rogues.count { it.status.name == "ISOLATED" }

    // Categorized devices
    val switchesCount = devices.count { it.type == DeviceType.ROUTER || it.type == DeviceType.SWITCH }
    val serversCount = devices.count { it.type == DeviceType.SERVER }
    val workstationsCount = devices.count { it.type == DeviceType.WORKSTATION }
    val iotCount = devices.count { it.type == DeviceType.CAMERA || it.type == DeviceType.PRINTER || it.type == DeviceType.ROGUE }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("executive_summary_dashboard_card")
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // TOP CONTROLS BAR: Language & Theme Toggles
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header Title with Shield
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primaryAccent.copy(alpha = if (isDark) 0.15f else 0.12f))
                            .border(1.dp, colors.primaryAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = if (isArabic) "حارس الشبكة" else "Network Shield",
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "لوحة الأمان وحالة الشبكة" else "Executive Network Sentinel",
                            color = colors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "ملخص فوري: الحالة • الأجهزة • التهديدات" else "Real-time: Status • Endpoints • Threats",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Interactive Quick Toggles (Language & Dark/Light Mode)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language Switcher Pill (عربي / English)
                    OutlinedButton(
                        onClick = { viewModel.toggleAppLanguage() },
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("toggle_language_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDark) CyberNavyDark else Color(0xFFF1F5F9),
                            contentColor = colors.primaryAccent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "English" else "العربية",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Theme Switcher Button (☀️ Light / 🌙 Dark)
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) CyberNavyDark else Color(0xFFF1F5F9))
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
                            .testTag("toggle_theme_btn")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = if (isDark) Color(0xFFFBBF24) else Color(0xFF6366F1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Divider(color = colors.cardBorder.copy(alpha = 0.7f), thickness = 1.dp)

            // =========================================================================
            // 3 CORE SUMMARY TILES: (1) Network Status, (2) Connected Devices, (3) Threats
            // =========================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ----------------------------------------------------
                // TILE 1: Current Network Status (حالة الشبكة الحالية)
                // ----------------------------------------------------
                NetworkStatusTile(
                    healthScore = health.healthScore,
                    gatewayIp = health.activeGatewayIp,
                    latencyMs = health.averageLatencyMs,
                    packetLoss = health.packetLossPercent,
                    backendStatus = backendStatus,
                    hasThreats = activeRoguesCount > 0,
                    isArabic = isArabic,
                    colors = colors,
                    onInspectClick = { viewModel.selectTab(AppTab.TOPOLOGY) }
                )

                // ----------------------------------------------------
                // TILE 2: Number of Connected Devices (الأجهزة المتصلة)
                // ----------------------------------------------------
                ConnectedDevicesTile(
                    totalCount = totalDevicesCount,
                    onlineCount = onlineDevicesCount,
                    warningCount = warningDevicesCount,
                    offlineCount = offlineDevicesCount,
                    switchesCount = switchesCount,
                    serversCount = serversCount,
                    workstationsCount = workstationsCount,
                    iotCount = iotCount,
                    isArabic = isArabic,
                    colors = colors,
                    onViewInventoryClick = { viewModel.selectTab(AppTab.DEVICES) }
                )

                // ----------------------------------------------------
                // TILE 3: Active Security Threats (التهديدات الأمنية النشطة)
                // ----------------------------------------------------
                ActiveThreatsTile(
                    activeRoguesCount = activeRoguesCount,
                    isolatedCount = isolatedCount,
                    firstActiveRogue = activeRogues.firstOrNull(),
                    isArabic = isArabic,
                    colors = colors,
                    onIsolateRogue = { rogueId -> viewModel.isolateRogue(rogueId) },
                    onNavigateToRogueGuard = { viewModel.selectTab(AppTab.ROGUE_DETECTOR) }
                )
            }

            // ==========================================
            // BOTTOM ACTION BAR: Sweep & Audit Controls
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (activeRoguesCount > 0) colors.statusCrimson else colors.statusGreen)
                    )
                    Text(
                        text = if (isArabic) {
                            if (activeRoguesCount > 0) "تنبيه أمني نشط: جهاز دخيل قيد التحقق" else "حالة الاتصال: محمي بالكامل"
                        } else {
                            if (activeRoguesCount > 0) "Sentinel Alert: Active Rogue Under Quarantine" else "Sentinel Status: Fully Protected"
                        },
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { viewModel.triggerScan() },
                    enabled = !isScanning,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("executive_summary_scan_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = if (isDark) Color(0xFF030712) else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.Sync else Icons.Default.Radar,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) {
                            if (isScanning) "جارٍ الفحص..." else "فحص الشبكة الآن"
                        } else {
                            if (isScanning) "Scanning..." else "Scan Subnet"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// =========================================================================
// SUB-TILE 1: Current Network Status (حالة الشبكة الحالية)
// =========================================================================
@Composable
private fun NetworkStatusTile(
    healthScore: Int,
    gatewayIp: String,
    latencyMs: Float,
    packetLoss: Float,
    backendStatus: String,
    hasThreats: Boolean,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onInspectClick: () -> Unit
) {
    val statusBg = if (hasThreats) colors.statusCrimson.copy(alpha = 0.08f) else colors.statusGreen.copy(alpha = 0.08f)
    val statusBorder = if (hasThreats) colors.statusCrimson.copy(alpha = 0.35f) else colors.statusGreen.copy(alpha = 0.35f)
    val statusColor = if (hasThreats) colors.statusCrimson else colors.statusGreen

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(statusBg)
            .border(1.dp, statusBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onInspectClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (hasThreats) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (isArabic) "حالة الشبكة الحالية" else "Current Network Status",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) {
                                if (hasThreats) "مستوى المخاطر مرتفع - يوصى بالتدقيق" else "جميع المؤشرات الحيوية مستقرة"
                            } else {
                                if (hasThreats) "Elevated Risk - Review Rogue Devices" else "All Subnet Vitals Normal"
                            },
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Health Score Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "$healthScore%",
                            color = statusColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isArabic) "كفاءة" else "Health",
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Key Network Diagnostics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagnosticMiniCard(
                    label = if (isArabic) "البوابة الرئيسية" else "Active Gateway",
                    value = gatewayIp,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                DiagnosticMiniCard(
                    label = if (isArabic) "متوسط زمن الاستجابة" else "Avg Latency",
                    value = "${latencyMs}ms",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                DiagnosticMiniCard(
                    label = if (isArabic) "فقدان الحزم" else "Packet Loss",
                    value = "${packetLoss}%",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// =========================================================================
// SUB-TILE 2: Number of Connected Devices (الأجهزة المتصلة)
// =========================================================================
@Composable
private fun ConnectedDevicesTile(
    totalCount: Int,
    onlineCount: Int,
    warningCount: Int,
    offlineCount: Int,
    switchesCount: Int,
    serversCount: Int,
    workstationsCount: Int,
    iotCount: Int,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onViewInventoryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.primaryAccent.copy(alpha = 0.06f))
            .border(1.dp, colors.primaryAccent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(onClick = onViewInventoryClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (isArabic) "الأجهزة المتصلة بالشبكة" else "Connected Endpoints",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "إجمالي الأجهزة النشطة والمراقبة محلياً" else "Total monitored hardware on LAN subnets",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Total Devices Badge
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "$totalCount",
                        color = colors.primaryAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isArabic) "جهاز" else "devices",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Connection State Breakdown (Online, Warning, Offline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusCountBadge(
                    label = if (isArabic) "متصل" else "Online",
                    count = onlineCount,
                    color = colors.statusGreen,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                StatusCountBadge(
                    label = if (isArabic) "تحذير" else "Warning",
                    count = warningCount,
                    color = colors.statusAmber,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                StatusCountBadge(
                    label = if (isArabic) "غير متصل" else "Offline",
                    count = offlineCount,
                    color = colors.statusRed,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            // Hardware fleet distribution chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FleetTypeChip(icon = Icons.Default.Router, label = if (isArabic) "محولات ($switchesCount)" else "Switches ($switchesCount)", colors = colors, modifier = Modifier.weight(1f))
                FleetTypeChip(icon = Icons.Default.Dns, label = if (isArabic) "خوادم ($serversCount)" else "Servers ($serversCount)", colors = colors, modifier = Modifier.weight(1f))
                FleetTypeChip(icon = Icons.Default.Computer, label = if (isArabic) "أجهزة ($workstationsCount)" else "Clients ($workstationsCount)", colors = colors, modifier = Modifier.weight(1f))
                FleetTypeChip(icon = Icons.Default.Sensors, label = if (isArabic) "إنترنت أشياء ($iotCount)" else "IoT ($iotCount)", colors = colors, modifier = Modifier.weight(1f))
            }
        }
    }
}

// =========================================================================
// SUB-TILE 3: Active Security Threats (التهديدات الأمنية النشطة)
// =========================================================================
@Composable
private fun ActiveThreatsTile(
    activeRoguesCount: Int,
    isolatedCount: Int,
    firstActiveRogue: com.example.data.model.RogueDevice?,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onIsolateRogue: (String) -> Unit,
    onNavigateToRogueGuard: () -> Unit
) {
    val isThreatActive = activeRoguesCount > 0
    val cardBg = if (isThreatActive) colors.statusCrimson.copy(alpha = 0.09f) else colors.statusGreen.copy(alpha = 0.05f)
    val cardBorder = if (isThreatActive) colors.statusCrimson.copy(alpha = 0.45f) else colors.cardBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onNavigateToRogueGuard)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isThreatActive) Icons.Default.GppMaybe else Icons.Default.GppGood,
                        contentDescription = null,
                        tint = if (isThreatActive) colors.statusCrimson else colors.statusGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (isArabic) "التهديدات الأمنية النشطة" else "Active Security Threats",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) {
                                if (isThreatActive) "رصد أجهزة دخيلة غير مصرح بها على الشبكة" else "لم يتم العثور على أجهزة دخيلة أو مشبوهة"
                            } else {
                                if (isThreatActive) "Unauthorized rogue hardware detected on subnet" else "Zero rogue devices or unauthorized MACs"
                            },
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Threat Count Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isThreatActive) colors.statusCrimson.copy(alpha = 0.2f) else colors.statusGreen.copy(alpha = 0.15f))
                        .border(1.dp, if (isThreatActive) colors.statusCrimson.copy(alpha = 0.6f) else colors.statusGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "$activeRoguesCount",
                            color = if (isThreatActive) colors.statusCrimson else colors.statusGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isArabic) {
                                if (isThreatActive) "تهديد نشط" else "آمن"
                            } else {
                                if (isThreatActive) "Threat" else "Clean"
                            },
                            color = if (isThreatActive) colors.statusCrimson else colors.statusGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // If threat is detected, show actionable mitigation banner
            if (isThreatActive && firstActiveRogue != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (colors.isDark) CyberNavyDark else Color(0xFFFEE2E2))
                        .border(1.dp, colors.statusCrimson.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = firstActiveRogue.ip,
                                color = colors.statusCrimson,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "• ${firstActiveRogue.vendor}",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = if (isArabic) "MAC: ${firstActiveRogue.mac} (غير مصرح به)" else "MAC: ${firstActiveRogue.mac} (Unregistered)",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = { onIsolateRogue(firstActiveRogue.id) },
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("executive_summary_isolate_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.statusCrimson),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "عزل فوري" else "Isolate",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // All clear banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (colors.isDark) CyberNavyDark else Color(0xFFECFDF5))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = colors.statusGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isArabic) {
                            "المطابقة تامة: $isolatedCount أجهزة تم عزلها مسبقاً، ولا توجد محاولات تسلل جديدة."
                        } else {
                            "Whitelist verified: $isolatedCount quarantined, no intrusion attempts."
                        },
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPACT REUSABLE COMPONENTS
// ==========================================

@Composable
private fun DiagnosticMiniCard(
    label: String,
    value: String,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (colors.isDark) CyberNavyDark else Color(0xFFF8FAFC))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 10.sp,
                maxLines = 1
            )
            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun StatusCountBadge(
    label: String,
    count: Int,
    color: Color,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (colors.isDark) CyberNavyDark else Color(0xFFF8FAFC))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = "$count",
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun FleetTypeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (colors.isDark) CyberNavyDark else Color(0xFFF1F5F9))
            .padding(vertical = 4.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
