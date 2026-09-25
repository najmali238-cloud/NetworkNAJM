package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.theme.*

/**
 * Main Dashboard Card-Based Grid Layout
 * Organizes:
 * 1. High-level network status cards (Health, Latency, Bandwidth, Firewall)
 * 2. Device count cards (Total, Online, Warning/Offline, Rogue/Untrusted)
 * 3. Recent security alerts card-based grid/feed with severity tags & action triggers
 */
@Composable
fun DashboardCardGridLayout(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDark) { getNetGuardColors(isDark) }

    val devices by viewModel.devices.collectAsState()
    val rogues by viewModel.rogueDevices.collectAsState()
    val health by viewModel.networkHealth.collectAsState()
    val bandwidth by viewModel.bandwidthMetrics.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    // Calculated metrics
    val totalDevices = devices.size
    val onlineDevices = devices.count { it.status == DeviceStatus.ONLINE }
    val offlineDevices = devices.count { it.status == DeviceStatus.OFFLINE }
    val warningDevices = devices.count { it.status == DeviceStatus.WARNING }
    val activeRogues = rogues.count { it.status.name != "ISOLATED" && it.status.name != "TRUSTED" }

    var selectedSeverityFilter by remember { mutableStateOf<AlertSeverity?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_card_grid_layout"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ========================================================
        // 1. SECTION: HIGH-LEVEL NETWORK STATUS (GRID OF 4 CARDS)
        // ========================================================
        DashboardSectionHeader(
            title = if (isArabic) "حالة الشبكة الرئيسية" else "High-Level Network Status",
            subtitle = if (isArabic) "مؤشرات الأداء وزمن الاستجابة والجدار الناري" else "Real-time health, latency, throughput & security posture",
            icon = Icons.Default.Speed,
            badge = if (health.healthScore >= 90) (if (isArabic) "ممتاز" else "OPTIMAL") else (if (isArabic) "انتباه" else "WARNING"),
            badgeColor = if (health.healthScore >= 90) colors.statusGreen else colors.statusAmber,
            colors = colors
        )

        // 2x2 Grid for High-Level Network Status
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Network Health & Uptime
                NetworkStatusMiniCard(
                    title = if (isArabic) "صحة الشبكة الكلية" else "Overall Health",
                    value = "${health.healthScore}%",
                    subValue = if (isArabic) "البوابة: ${health.activeGatewayIp}" else "Gateway: ${health.activeGatewayIp}",
                    icon = Icons.Default.HealthAndSafety,
                    accentColor = if (health.healthScore >= 90) colors.statusGreen else colors.statusAmber,
                    statusText = if (health.healthScore >= 90) "99.98% SLA" else "Investigate",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("network_health_grid_card"),
                    colors = colors,
                    progress = (health.healthScore / 100f).coerceIn(0f, 1f)
                )

                // Card 2: Latency & Jitter
                NetworkStatusMiniCard(
                    title = if (isArabic) "متوسط زمن الاستجابة" else "Avg Ping Latency",
                    value = "${health.averageLatencyMs} ms",
                    subValue = if (isArabic) "فقدان الحزم: ${health.packetLossPercent}%" else "Loss: ${health.packetLossPercent}%",
                    icon = Icons.Default.NetworkCheck,
                    accentColor = colors.primaryAccent,
                    statusText = if (isArabic) "DNS: ${health.dnsResolutionMs.toInt()}ms" else "DNS: ${health.dnsResolutionMs.toInt()}ms",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("network_latency_grid_card"),
                    colors = colors,
                    progress = 0.88f
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 3: Bandwidth Throughput
                NetworkStatusMiniCard(
                    title = if (isArabic) "عرض النطاق الترددي" else "Live Throughput",
                    value = "${bandwidth.downloadMbps.toInt()} Mbps",
                    subValue = if (isArabic) "الرفع: ${bandwidth.uploadMbps.toInt()} Mbps" else "Up: ${bandwidth.uploadMbps.toInt()} Mbps",
                    icon = Icons.Default.SwapVert,
                    accentColor = colors.primaryAccent,
                    statusText = if (isArabic) "1.0 Gbps سعة" else "1.0G Fiber",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("network_bandwidth_grid_card"),
                    colors = colors,
                    progress = (bandwidth.downloadMbps / bandwidth.totalCapacityMbps).coerceIn(0f, 1f)
                )

                // Card 4: Firewall & Threat Defense
                NetworkStatusMiniCard(
                    title = if (isArabic) "جدار الحماية والأمان" else "Security Posture",
                    value = if (activeRogues == 0) (if (isArabic) "مُحصن" else "Secure") else (if (isArabic) "تهديد نشط" else "Threat Detected"),
                    subValue = if (isArabic) "WAF / IDS قيد العمل" else "IDS/IPS Active",
                    icon = Icons.Default.Shield,
                    accentColor = if (activeRogues == 0) colors.statusGreen else colors.statusCrimson,
                    statusText = if (activeRogues == 0) "100% Locked" else "$activeRogues Alerts",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("network_security_grid_card"),
                    colors = colors,
                    progress = if (activeRogues == 0) 1.0f else 0.5f
                )
            }
        }

        // ========================================================
        // 2. SECTION: DEVICE COUNT (GRID OF 4 CARDS)
        // ========================================================
        DashboardSectionHeader(
            title = if (isArabic) "إحصائيات الأجهزة المتصلة" else "Device Count & Inventory",
            subtitle = if (isArabic) "حصر وتوزيع الأجهزة المتصلة بالمقسمات والخوادم" else "Total monitored endpoints, switches, servers & rogue assets",
            icon = Icons.Default.Devices,
            badge = "$totalDevices ${if (isArabic) "أجهزة" else "Devices"}",
            badgeColor = colors.primaryAccent,
            colors = colors
        )

        // 2x2 Grid for Device Counts
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Devices Card
                DeviceCountGridCard(
                    title = if (isArabic) "إجمالي الأجهزة" else "Total Devices",
                    count = totalDevices,
                    description = if (isArabic) "مقسمات، خوادم، حواسيب" else "Switches, Servers & PCs",
                    icon = Icons.Default.Dns,
                    badgeText = if (isArabic) "الأصول" else "Audited",
                    accentColor = colors.primaryAccent,
                    onClick = { viewModel.selectTab(AppTab.DEVICES) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("device_count_total_card"),
                    colors = colors
                )

                // Active / Online Devices Card
                DeviceCountGridCard(
                    title = if (isArabic) "أجهزة متصلة ونشطة" else "Active / Online",
                    count = onlineDevices,
                    description = if (isArabic) "تستجيب لبروتوكول ICMP" else "Responsive via ICMP",
                    icon = Icons.Default.CheckCircle,
                    badgeText = "${((onlineDevices.toFloat() / totalDevices.coerceAtLeast(1)) * 100).toInt()}%",
                    accentColor = colors.statusGreen,
                    onClick = { viewModel.selectTab(AppTab.DEVICES) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("device_count_online_card"),
                    colors = colors
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Offline & Warning Devices Card
                DeviceCountGridCard(
                    title = if (isArabic) "أجهزة غير متصلة/تحذير" else "Offline / Warning",
                    count = offlineDevices + warningDevices,
                    description = if (isArabic) "$offlineDevices غير متصل | $warningDevices تحذير" else "$offlineDevices Offline | $warningDevices Warn",
                    icon = Icons.Default.Warning,
                    badgeText = if (offlineDevices + warningDevices > 0) (if (isArabic) "فحص" else "Review") else "0",
                    accentColor = if (offlineDevices + warningDevices > 0) colors.statusAmber else colors.textSecondary,
                    onClick = { viewModel.selectTab(AppTab.DEVICES) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("device_count_warning_card"),
                    colors = colors
                )

                // Rogue / Suspicious Devices Card
                DeviceCountGridCard(
                    title = if (isArabic) "أجهزة دخيلة ومشتبه بها" else "Rogue / Suspicious",
                    count = activeRogues,
                    description = if (activeRogues > 0) (if (isArabic) "غير مصرح بها في الشبكة" else "Not on enterprise whitelist") else (if (isArabic) "الشبكة نظيفة تماماً" else "Perimeter clear"),
                    icon = Icons.Default.GppMaybe,
                    badgeText = if (activeRogues > 0) (if (isArabic) "عاجل" else "ALERT") else "Safe",
                    accentColor = if (activeRogues > 0) colors.statusCrimson else colors.statusGreen,
                    onClick = { viewModel.selectTab(AppTab.ROGUE_DETECTOR) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("device_count_rogue_card"),
                    colors = colors,
                    hasPulse = activeRogues > 0
                )
            }
        }

        // ========================================================
        // 3. SECTION: RECENT SECURITY ALERTS (CARD-BASED FEED)
        // ========================================================
        val filteredAlerts = remember(alerts, selectedSeverityFilter) {
            if (selectedSeverityFilter == null) alerts else alerts.filter { it.severity == selectedSeverityFilter }
        }

        DashboardSectionHeader(
            title = if (isArabic) "التنبيهات الأمنية الحديثة" else "Recent Security Alerts",
            subtitle = if (isArabic) "رصد التهديدات، محاولات الاختراق، وشذوذ حركة البيانات" else "Active telemetry anomalies, intrusion triggers & mitigation logs",
            icon = Icons.Default.NotificationsActive,
            badge = "${alerts.size} ${if (isArabic) "تنبيه" else "Alerts"}",
            badgeColor = if (alerts.any { it.severity == AlertSeverity.CRITICAL }) colors.statusCrimson else colors.statusAmber,
            colors = colors
        )

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedSeverityFilter == null,
                onClick = { selectedSeverityFilter = null },
                label = { Text(if (isArabic) "الكل (${alerts.size})" else "All (${alerts.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primaryAccent.copy(alpha = 0.2f),
                    selectedLabelColor = colors.primaryAccent
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedSeverityFilter == null,
                    borderColor = if (selectedSeverityFilter == null) colors.primaryAccent else colors.cardBorder
                ),
                modifier = Modifier.testTag("filter_all_alerts")
            )

            FilterChip(
                selected = selectedSeverityFilter == AlertSeverity.CRITICAL,
                onClick = { selectedSeverityFilter = if (selectedSeverityFilter == AlertSeverity.CRITICAL) null else AlertSeverity.CRITICAL },
                label = {
                    Text(
                        if (isArabic) "حرجة (${alerts.count { it.severity == AlertSeverity.CRITICAL }})" else "Critical (${alerts.count { it.severity == AlertSeverity.CRITICAL }})",
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.statusCrimson.copy(alpha = 0.2f),
                    selectedLabelColor = colors.statusCrimson
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedSeverityFilter == AlertSeverity.CRITICAL,
                    borderColor = if (selectedSeverityFilter == AlertSeverity.CRITICAL) colors.statusCrimson else colors.cardBorder
                ),
                modifier = Modifier.testTag("filter_critical_alerts")
            )

            FilterChip(
                selected = selectedSeverityFilter == AlertSeverity.WARNING,
                onClick = { selectedSeverityFilter = if (selectedSeverityFilter == AlertSeverity.WARNING) null else AlertSeverity.WARNING },
                label = {
                    Text(
                        if (isArabic) "تحذيرات (${alerts.count { it.severity == AlertSeverity.WARNING }})" else "Warning (${alerts.count { it.severity == AlertSeverity.WARNING }})",
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.statusAmber.copy(alpha = 0.2f),
                    selectedLabelColor = colors.statusAmber
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedSeverityFilter == AlertSeverity.WARNING,
                    borderColor = if (selectedSeverityFilter == AlertSeverity.WARNING) colors.statusAmber else colors.cardBorder
                ),
                modifier = Modifier.testTag("filter_warning_alerts")
            )
        }

        // Alert Cards Feed
        if (filteredAlerts.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_alerts_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.statusGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.statusGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "لا توجد تنبيهات نشطة حالياً" else "No Active Security Alerts",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "جميع الأنظمة والمقاسم تعمل بكفاءة وأمان كامل" else "All endpoints and perimeter safeguards are in nominal state",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredAlerts.take(4).forEach { alert ->
                    RecentSecurityAlertCard(
                        alert = alert,
                        onIsolate = {
                            if (alert.deviceId?.startsWith("rogue") == true) {
                                viewModel.isolateRogue(alert.deviceId)
                            } else {
                                viewModel.selectTab(AppTab.INTRUSION)
                            }
                        },
                        onDismiss = {
                            viewModel.dismissAlert(alert.id)
                        },
                        isArabic = isArabic,
                        colors = colors,
                        modifier = Modifier.testTag("security_alert_card_${alert.id}")
                    )
                }
            }
        }

        // Quick Subnet Scan Trigger & Quick Navigation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_quick_scan_banner"),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "الفحص التلقائي لشبكة النطاق 192.168.1.0/24" else "Subnet Auto-Scanner (192.168.1.0/24)",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isArabic) "تحديث فوري للأجهزة الجديدة وعزل أي بروتوكول مشبوه" else "Continuously probing ARP tables, active ports, and ICMP responses",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = { viewModel.triggerScan() },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp).testTag("dashboard_grid_trigger_scan_btn")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = if (isDark) Color.Black else Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "جاري الفحص..." else "Scanning...", fontSize = 11.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "فحص فوري" else "Scan Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Section Header with title, subtitle, icon, and status badge
 */
@Composable
private fun DashboardSectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    badgeColor: Color,
    colors: NetGuardThemeColors
) {
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
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primaryAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primaryAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = badge,
                color = badgeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * High-Level Network Status Mini Card in Grid
 */
@Composable
private fun NetworkStatusMiniCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    accentColor: Color,
    statusText: String,
    modifier: Modifier = Modifier,
    colors: NetGuardThemeColors,
    progress: Float = 0.75f
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                color = colors.textSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = colors.surface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subValue,
                color = colors.textSecondary,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Device Count Card in Grid
 */
@Composable
private fun DeviceCountGridCard(
    title: String,
    count: Int,
    description: String,
    icon: ImageVector,
    badgeText: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: NetGuardThemeColors,
    hasPulse: Boolean = false
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (hasPulse) accentColor.copy(alpha = 0.6f) else colors.cardBorder
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$count",
                color = colors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                color = colors.textSecondary,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Individual Recent Security Alert Card
 */
@Composable
private fun RecentSecurityAlertCard(
    alert: AlertLog,
    onIsolate: () -> Unit,
    onDismiss: () -> Unit,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    val severityColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> colors.statusCrimson
        AlertSeverity.WARNING -> colors.statusAmber
        AlertSeverity.INFO -> colors.primaryAccent
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(severityColor.copy(alpha = 0.4f))
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(severityColor)
                    )

                    Text(
                        text = alert.title,
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(severityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = alert.severity.name,
                        color = severityColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alert.message,
                color = colors.textSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = alert.timestamp,
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                    if (alert.deviceId != null) {
                        Text(
                            text = "• ${alert.deviceId}",
                            color = colors.primaryAccent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("dismiss_alert_${alert.id}"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                        )
                    ) {
                        Text(
                            text = if (isArabic) "تجاهل" else "Dismiss",
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }

                    Button(
                        onClick = onIsolate,
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("isolate_alert_${alert.id}"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = severityColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (alert.deviceId?.startsWith("rogue") == true) (if (isArabic) "عزل" else "Isolate") else (if (isArabic) "تحقيق" else "Review"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
