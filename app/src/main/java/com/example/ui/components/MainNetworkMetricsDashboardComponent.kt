package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.BandwidthMetrics
import com.example.data.model.CriticalServer
import com.example.data.model.Device
import com.example.data.model.DeviceStatus
import com.example.data.model.DeviceType
import com.example.data.model.NetworkHealthMetrics
import com.example.data.model.ServerMetric
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.theme.ChartRxCyan
import com.example.ui.theme.ChartTxPurple
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.StatusOfflineRed
import com.example.ui.theme.StatusOnlineGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.getNetGuardColors
import java.util.Locale

/**
 * Filter mode for the Main Network Metrics Dashboard component
 */
enum class DashboardMetricTab {
    ALL,
    BANDWIDTH,
    DEVICES,
    SERVERS
}

/**
 * Connected ViewModel Composable entry point for the Main Network Metrics Dashboard UI component.
 */
@Composable
fun MainNetworkMetricsDashboardComponent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val bandwidth by viewModel.bandwidthMetrics.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val criticalServers by viewModel.criticalServers.collectAsState()
    val serverMetrics by viewModel.serverMetrics.collectAsState()
    val networkHealth by viewModel.networkHealth.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    MainNetworkMetricsDashboardComponent(
        bandwidth = bandwidth,
        devices = devices,
        criticalServers = criticalServers,
        serverMetrics = serverMetrics,
        networkHealth = networkHealth,
        isDarkMode = isDark,
        language = language,
        isScanning = isScanning,
        onRefreshMetrics = { viewModel.triggerScan() },
        onNavigateToDevices = { viewModel.selectTab(AppTab.DEVICES) },
        onNavigateToServers = { viewModel.selectTab(AppTab.REMEDIATION) },
        modifier = modifier
    )
}

/**
 * Reusable, decoupled Main Dashboard UI Component displaying:
 * 1. Key Bandwidth Usage (Live Download/Upload, Capacity utilization %, dynamic sparkline waveform)
 * 2. Active Devices (Online vs Total, Device category breakdown, Average latency)
 * 3. Server Health Summary Status (Overall health score, CPU/RAM/Disk averages, Service health matrix)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainNetworkMetricsDashboardComponent(
    bandwidth: BandwidthMetrics,
    devices: List<Device>,
    criticalServers: List<CriticalServer>,
    serverMetrics: List<ServerMetric>,
    networkHealth: NetworkHealthMetrics,
    isDarkMode: Boolean,
    language: AppLanguage,
    isScanning: Boolean,
    onRefreshMetrics: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToServers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }
    val isArabic = language == AppLanguage.ARABIC
    var selectedTab by remember { mutableStateOf(DashboardMetricTab.ALL) }

    // Computations
    val totalCapacity = if (bandwidth.totalCapacityMbps > 0f) bandwidth.totalCapacityMbps else 1000f
    val currentUsageMbps = bandwidth.downloadMbps + bandwidth.uploadMbps
    val bandwidthUtilizationPercent = ((currentUsageMbps / totalCapacity) * 100f).coerceIn(0f, 100f)

    val onlineDevicesCount = devices.count { it.status == DeviceStatus.ONLINE }
    val warningDevicesCount = devices.count { it.status == DeviceStatus.WARNING }
    val offlineDevicesCount = devices.count { it.status == DeviceStatus.OFFLINE }
    val totalDevicesCount = devices.size
    val activeDeviceRatio = if (totalDevicesCount > 0) (onlineDevicesCount.toFloat() / totalDevicesCount.toFloat()) * 100f else 0f

    // Device categories
    val serverDevicesCount = devices.count { it.type == DeviceType.SERVER && it.status == DeviceStatus.ONLINE }
    val networkEquipCount = devices.count { (it.type == DeviceType.ROUTER || it.type == DeviceType.SWITCH) && it.status == DeviceStatus.ONLINE }
    val workstationCount = devices.count { it.type == DeviceType.WORKSTATION && it.status == DeviceStatus.ONLINE }
    val iotEndpointsCount = devices.count { (it.type == DeviceType.CAMERA || it.type == DeviceType.PRINTER || it.type == DeviceType.ROGUE) && it.status == DeviceStatus.ONLINE }

    // Server health averages
    val allCpuValues = mutableListOf<Float>()
    val allRamValues = mutableListOf<Float>()
    val allDiskValues = mutableListOf<Float>()
    criticalServers.forEach {
        allCpuValues.add(it.cpuPercent)
        allRamValues.add(it.ramPercent)
        allDiskValues.add(it.diskPercent)
    }
    serverMetrics.forEach {
        allCpuValues.add(it.cpuPercent)
        allRamValues.add(it.ramPercent)
        allDiskValues.add(it.diskPercent)
    }

    val avgCpu = if (allCpuValues.isNotEmpty()) allCpuValues.average().toFloat() else 34.5f
    val avgRam = if (allRamValues.isNotEmpty()) allRamValues.average().toFloat() else 58.2f
    val avgDisk = if (allDiskValues.isNotEmpty()) allDiskValues.average().toFloat() else 46.0f

    // Services status
    val totalServices = criticalServers.flatMap { it.services }
    val runningServicesCount = totalServices.count { it.isRunning }
    val totalServicesCount = totalServices.size.coerceAtLeast(1)

    // Pulse animation for live status
    val infiniteTransition = rememberInfiniteTransition(label = "telemetry_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_network_metrics_dashboard")
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // HEADER: Title, Live Telemetry Pill & Refresh Action
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(colors.primaryAccent.copy(alpha = pulseAlpha))
                        )
                        Text(
                            text = if (isArabic) "لوحة قياسات الشبكة المركزية" else "NETWORK TELEMETRY DASHBOARD",
                            color = colors.primaryAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                    }

                    Text(
                        text = if (isArabic) "مؤشرات النطاق الترددي، الأجهزة النشطة وحالة الخوادم" else "Key Bandwidth, Active Devices & Server Health Telemetry",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Live status pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.statusGreen.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.statusGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(colors.statusGreen)
                            )
                            Text(
                                text = if (isArabic) "بث حي" else "LIVE",
                                color = colors.statusGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Refresh Button
                    IconButton(
                        onClick = onRefreshMetrics,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refresh_telemetry_button"),
                        colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                            containerColor = colors.surface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (isArabic) "تحديث القياسات" else "Refresh Telemetry",
                            tint = if (isScanning) colors.primaryAccent else colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ==========================================
            // FILTER TABS (All / Bandwidth / Devices / Servers)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardTabChip(
                    title = if (isArabic) "نظرة شاملة" else "Overview",
                    isSelected = selectedTab == DashboardMetricTab.ALL,
                    onClick = { selectedTab = DashboardMetricTab.ALL },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                DashboardTabChip(
                    title = if (isArabic) "النطاق الترددي" else "Bandwidth",
                    isSelected = selectedTab == DashboardMetricTab.BANDWIDTH,
                    onClick = { selectedTab = DashboardMetricTab.BANDWIDTH },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                DashboardTabChip(
                    title = if (isArabic) "الأجهزة" else "Devices",
                    isSelected = selectedTab == DashboardMetricTab.DEVICES,
                    onClick = { selectedTab = DashboardMetricTab.DEVICES },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                DashboardTabChip(
                    title = if (isArabic) "الخوادم" else "Servers",
                    isSelected = selectedTab == DashboardMetricTab.SERVERS,
                    onClick = { selectedTab = DashboardMetricTab.SERVERS },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            // ==========================================
            // METRIC 1: BANDWIDTH USAGE
            // ==========================================
            AnimatedVisibility(
                visible = selectedTab == DashboardMetricTab.ALL || selectedTab == DashboardMetricTab.BANDWIDTH,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BandwidthMetricCard(
                    bandwidth = bandwidth,
                    totalCapacity = totalCapacity,
                    utilizationPercent = bandwidthUtilizationPercent,
                    colors = colors,
                    isArabic = isArabic,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ==========================================
            // METRIC 2: ACTIVE DEVICES
            // ==========================================
            AnimatedVisibility(
                visible = selectedTab == DashboardMetricTab.ALL || selectedTab == DashboardMetricTab.DEVICES,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ActiveDevicesMetricCard(
                    onlineCount = onlineDevicesCount,
                    warningCount = warningDevicesCount,
                    offlineCount = offlineDevicesCount,
                    totalCount = totalDevicesCount,
                    activeRatio = activeDeviceRatio,
                    serverCount = serverDevicesCount,
                    networkEquipCount = networkEquipCount,
                    workstationCount = workstationCount,
                    iotCount = iotEndpointsCount,
                    avgLatencyMs = networkHealth.averageLatencyMs,
                    onNavigateToDevices = onNavigateToDevices,
                    colors = colors,
                    isArabic = isArabic,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ==========================================
            // METRIC 3: SERVER HEALTH SUMMARY STATUS
            // ==========================================
            AnimatedVisibility(
                visible = selectedTab == DashboardMetricTab.ALL || selectedTab == DashboardMetricTab.SERVERS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ServerHealthSummaryMetricCard(
                    healthMetrics = networkHealth,
                    criticalServers = criticalServers,
                    avgCpu = avgCpu,
                    avgRam = avgRam,
                    avgDisk = avgDisk,
                    runningServicesCount = runningServicesCount,
                    totalServicesCount = totalServicesCount,
                    onNavigateToServers = onNavigateToServers,
                    colors = colors,
                    isArabic = isArabic,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Filter tab chip with compact modern pill styling
 */
@Composable
private fun DashboardTabChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.example.ui.theme.NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) colors.primaryAccent.copy(alpha = 0.18f) else colors.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) colors.primaryAccent else colors.cardBorder
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colors.primaryAccent else colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ====================================================================
// 1. BANDWIDTH USAGE COMPONENT SECTION
// ====================================================================

@Composable
private fun BandwidthMetricCard(
    bandwidth: BandwidthMetrics,
    totalCapacity: Float,
    utilizationPercent: Float,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedUtilization by animateFloatAsState(
        targetValue = utilizationPercent / 100f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "bandwidth_bar"
    )

    Card(
        modifier = modifier.testTag("metric_bandwidth_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
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
                            .background(ChartRxCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ChartRxCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "استهلاك النطاق الترددي الفعلي" else "Bandwidth Usage & Throughput",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isArabic) "إجمالي السعة المتاحة: ${totalCapacity.toInt()} Mbps" else "Total Gateway Capacity: ${totalCapacity.toInt()} Mbps",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                // Utilization badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (utilizationPercent > 80f) StatusWarningAmber.copy(alpha = 0.18f) else colors.primaryAccent.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (utilizationPercent > 80f) StatusWarningAmber.copy(alpha = 0.5f) else colors.primaryAccent.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f%% %s", utilizationPercent, if (isArabic) "مشغول" else "Utilized"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (utilizationPercent > 80f) StatusWarningAmber else colors.primaryAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Download & Upload Speeds Dual Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Download (Rx)
                BandwidthStatPill(
                    label = if (isArabic) "التنزيل (Download)" else "Download (Rx)",
                    speedMbps = bandwidth.downloadMbps,
                    icon = Icons.Default.ArrowDownward,
                    accentColor = ChartRxCyan,
                    isArabic = isArabic,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                // Upload (Tx)
                BandwidthStatPill(
                    label = if (isArabic) "الرفع (Upload)" else "Upload (Tx)",
                    speedMbps = bandwidth.uploadMbps,
                    icon = Icons.Default.ArrowUpward,
                    accentColor = ChartTxPurple,
                    isArabic = isArabic,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            // Visual Capacity Utilization Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "نسبة إشغال السعة الكلية" else "Total Throughput Load",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f / %.0f Mbps", bandwidth.downloadMbps + bandwidth.uploadMbps, totalCapacity),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedUtilization },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (utilizationPercent > 85f) StatusOfflineRed else if (utilizationPercent > 70f) StatusWarningAmber else ChartRxCyan,
                    trackColor = colors.cardBorder
                )
            }

            // Real-time Waveform Canvas (Sparkline throughput graph)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "مخطط التدفق اللحظي للبيانات" else "Live Throughput Waveform",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendIndicator(label = if (isArabic) "تنزيل" else "Rx", color = ChartRxCyan)
                        LegendIndicator(label = if (isArabic) "رفع" else "Tx", color = ChartTxPurple)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.cardBackground)
                        .border(1.dp, colors.cardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    BandwidthMiniSparkline(
                        downloadHistory = bandwidth.downloadHistory,
                        uploadHistory = bandwidth.uploadHistory,
                        rxColor = ChartRxCyan,
                        txColor = ChartTxPurple,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun BandwidthStatPill(
    label: String,
    speedMbps: Float,
    icon: ImageVector,
    accentColor: Color,
    isArabic: Boolean,
    colors: com.example.ui.theme.NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f", speedMbps),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Mbps",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun LegendIndicator(label: String, color: Color) {
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
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BandwidthMiniSparkline(
    downloadHistory: List<Float>,
    uploadHistory: List<Float>,
    rxColor: Color,
    txColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val maxVal = maxOf(
            downloadHistory.maxOrNull() ?: 100f,
            uploadHistory.maxOrNull() ?: 50f,
            150f
        )

        fun drawCurve(data: List<Float>, strokeColor: Color) {
            if (data.size < 2) return
            val step = width / (data.size - 1)
            val path = Path()

            data.forEachIndexed { index, value ->
                val x = index * step
                val normalizedY = 1f - (value / maxVal).coerceIn(0.05f, 0.95f)
                val y = normalizedY * height

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = (index - 1) * step
                    val prevY = (1f - (data[index - 1] / maxVal).coerceIn(0.05f, 0.95f)) * height
                    val cx = (prevX + x) / 2f
                    path.cubicTo(cx, prevY, cx, y, x, y)
                }
            }

            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }

        drawCurve(downloadHistory, rxColor)
        drawCurve(uploadHistory, txColor)
    }
}

// ====================================================================
// 2. ACTIVE DEVICES COMPONENT SECTION
// ====================================================================

@Composable
private fun ActiveDevicesMetricCard(
    onlineCount: Int,
    warningCount: Int,
    offlineCount: Int,
    totalCount: Int,
    activeRatio: Float,
    serverCount: Int,
    networkEquipCount: Int,
    workstationCount: Int,
    iotCount: Int,
    avgLatencyMs: Float,
    onNavigateToDevices: () -> Unit,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_active_devices_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row with Count & Navigation Link
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
                            .background(StatusOnlineGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = StatusOnlineGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "الأجهزة النشطة والمتصلة" else "Active Connected Devices",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isArabic) "$onlineCount من أصل $totalCount جهاز متصل حالياً" else "$onlineCount of $totalCount devices online",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                // View All Devices Button
                Surface(
                    onClick = onNavigateToDevices,
                    shape = RoundedCornerShape(8.dp),
                    color = colors.primaryAccent.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryAccent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isArabic) "عرض الكل" else "View All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primaryAccent
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Big Statistics Highlight Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Online Devices Large Pill
                DeviceStatusLargePill(
                    count = onlineCount,
                    total = totalCount,
                    label = if (isArabic) "أجهزة متصلة" else "Online Active",
                    statusColor = StatusOnlineGreen,
                    percentage = activeRatio,
                    colors = colors,
                    isArabic = isArabic,
                    modifier = Modifier.weight(1.2f)
                )

                // Average Latency Metric Pill
                Surface(
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isArabic) "متوسط الاستجابة" else "Avg Latency",
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", avgLatencyMs),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryAccent,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "ms",
                                fontSize = 11.sp,
                                color = colors.primaryAccent,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = if (avgLatencyMs < 10f) (if (isArabic) "فائقة السرعة" else "Ultra Low") else (if (isArabic) "مقبولة" else "Normal"),
                            fontSize = 10.sp,
                            color = StatusOnlineGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Device Categories Grid (Servers, Network switches/routers, Workstations, IoT)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isArabic) "توزيع الأجهزة النشطة حسب التصنيف" else "Active Devices by Category",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryCountChip(
                        icon = Icons.Default.Dns,
                        name = if (isArabic) "خوادم" else "Servers",
                        count = serverCount,
                        color = CyberCyan,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    CategoryCountChip(
                        icon = Icons.Default.Router,
                        name = if (isArabic) "محولات/راوتر" else "Switches",
                        count = networkEquipCount,
                        color = colors.secondaryAccent,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    CategoryCountChip(
                        icon = Icons.Default.Computer,
                        name = if (isArabic) "أجهزة حاسوب" else "PCs",
                        count = workstationCount,
                        color = StatusOnlineGreen,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    CategoryCountChip(
                        icon = Icons.Default.Wifi,
                        name = if (isArabic) "إنترنت الأشياء" else "IoT",
                        count = iotCount,
                        color = StatusWarningAmber,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Connectivity State Bar: Online / Warning / Offline
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.cardBorder)
                ) {
                    if (totalCount > 0) {
                        val onlineWeight = (onlineCount.toFloat() / totalCount).coerceAtLeast(0.01f)
                        val warningWeight = (warningCount.toFloat() / totalCount).coerceAtLeast(0f)
                        val offlineWeight = (offlineCount.toFloat() / totalCount).coerceAtLeast(0f)

                        Box(
                            modifier = Modifier
                                .weight(onlineWeight)
                                .fillMaxHeight()
                                .background(StatusOnlineGreen)
                        )
                        if (warningWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(warningWeight)
                                    .fillMaxHeight()
                                    .background(StatusWarningAmber)
                            )
                        }
                        if (offlineWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(offlineWeight)
                                    .fillMaxHeight()
                                    .background(StatusOfflineRed)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DeviceStateDotLabel(color = StatusOnlineGreen, label = if (isArabic) "$onlineCount متصل" else "$onlineCount Online")
                    DeviceStateDotLabel(color = StatusWarningAmber, label = if (isArabic) "$warningCount تحذير" else "$warningCount Warning")
                    DeviceStateDotLabel(color = StatusOfflineRed, label = if (isArabic) "$offlineCount مفصول" else "$offlineCount Offline")
                }
            }
        }
    }
}

@Composable
private fun DeviceStatusLargePill(
    count: Int,
    total: Int,
    label: String,
    statusColor: Color,
    percentage: Float,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = statusColor.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$count",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "/ $total",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = String.format(Locale.US, "%.0f%%", percentage),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryCountChip(
    icon: ImageVector,
    name: String,
    count: Int,
    color: Color,
    colors: com.example.ui.theme.NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$count",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = name,
                fontSize = 9.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DeviceStateDotLabel(color: Color, label: String) {
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
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// ====================================================================
// 3. SERVER HEALTH SUMMARY STATUS COMPONENT SECTION
// ====================================================================

@Composable
private fun ServerHealthSummaryMetricCard(
    healthMetrics: NetworkHealthMetrics,
    criticalServers: List<CriticalServer>,
    avgCpu: Float,
    avgRam: Float,
    avgDisk: Float,
    runningServicesCount: Int,
    totalServicesCount: Int,
    onNavigateToServers: () -> Unit,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val overallHealthScore = healthMetrics.healthScore.coerceIn(0, 100)
    val healthGrade = if (overallHealthScore >= 90) {
        if (isArabic) "ممتاز (مستقر)" else "OPTIMAL"
    } else if (overallHealthScore >= 75) {
        if (isArabic) "جيد جداً" else "GOOD"
    } else {
        if (isArabic) "تحذير أداء" else "ATTENTION NEEDED"
    }

    val healthBadgeColor = if (overallHealthScore >= 90) StatusOnlineGreen else if (overallHealthScore >= 75) StatusWarningAmber else StatusOfflineRed

    Card(
        modifier = modifier.testTag("metric_server_health_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row with Overall Health Score & Diagnose Link
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
                            .background(healthBadgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = healthBadgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "الحالة الصحية العامة للخوادم" else "Server Fleet Health Summary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isArabic) "${criticalServers.size} خوادم رئيسية قيد المراقبة اللحظية" else "${criticalServers.size} Critical Enterprise Nodes Monitored",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                // Health Badge Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = healthBadgeColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, healthBadgeColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (overallHealthScore >= 80) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = healthBadgeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "$overallHealthScore% $healthGrade",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = healthBadgeColor
                        )
                    }
                }
            }

            // Triple Server Metric Gauges: CPU / RAM / Disk
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ServerResourceGaugeCard(
                    title = if (isArabic) "معالج CPU" else "Avg CPU",
                    valuePercent = avgCpu,
                    icon = Icons.Default.Memory,
                    accentColor = if (avgCpu > 80f) StatusOfflineRed else if (avgCpu > 65f) StatusWarningAmber else CyberCyan,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                ServerResourceGaugeCard(
                    title = if (isArabic) "ذاكرة RAM" else "Avg RAM",
                    valuePercent = avgRam,
                    icon = Icons.Default.Storage,
                    accentColor = if (avgRam > 85f) StatusOfflineRed else if (avgRam > 70f) StatusWarningAmber else colors.secondaryAccent,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                ServerResourceGaugeCard(
                    title = if (isArabic) "تخزين Disk" else "Avg Disk",
                    valuePercent = avgDisk,
                    icon = Icons.Default.Storage,
                    accentColor = if (avgDisk > 90f) StatusOfflineRed else StatusOnlineGreen,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            // Service Daemons & Thermal Health Status Strip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Running Services
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusOnlineGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (isArabic) "الخدمات والعمليات النشطة" else "Active Core Daemons",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "$runningServicesCount / $totalServicesCount ${if (isArabic) "خدمة تعمل بكفاءة" else "Services Running"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                    }

                    // Thermal Status
                    val avgTemp = if (criticalServers.isNotEmpty()) criticalServers.map { it.temperatureC }.average().toFloat() else 42.0f
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = if (avgTemp > 65f) StatusOfflineRed else StatusOnlineGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f°C", avgTemp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Quick Critical Server Nodes Chips List
            if (criticalServers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isArabic) "حالة خوادم المؤسسة الأساسية" else "Enterprise Server Cluster Nodes",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        criticalServers.take(3).forEach { server ->
                            ServerClusterNodeRow(
                                server = server,
                                colors = colors,
                                isArabic = isArabic,
                                onNavigateToServers = onNavigateToServers
                            )
                        }
                    }
                }
            }

            // Remediation & Server Management Action Button
            OutlinedButton(
                onClick = onNavigateToServers,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_servers_remediation_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primaryAccent
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryAccent.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isArabic) "إدارة الخوادم والمعالجة الفورية (SSH & Services)" else "Manage Enterprise Servers & Services",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerResourceGaugeCard(
    title: String,
    valuePercent: Float,
    icon: ImageVector,
    accentColor: Color,
    colors: com.example.ui.theme.NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = (valuePercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "resource_bar"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = String.format(Locale.US, "%.1f%%", valuePercent),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace
            )

            LinearProgressIndicator(
                progress = { animatedPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = colors.cardBorder
            )
        }
    }
}

@Composable
private fun ServerClusterNodeRow(
    server: CriticalServer,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean,
    onNavigateToServers: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToServers() },
        shape = RoundedCornerShape(8.dp),
        color = colors.cardBackground.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (server.status == DeviceStatus.ONLINE) StatusOnlineGreen else StatusOfflineRed)
                )
                Column {
                    Text(
                        text = server.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${server.ip} • ${server.role}",
                        fontSize = 9.sp,
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CPU ${server.cpuPercent.toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (server.cpuPercent > 80f) StatusOfflineRed else colors.primaryAccent,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "RAM ${server.ramPercent.toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
