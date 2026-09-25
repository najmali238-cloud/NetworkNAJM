package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.AiIntelligenceHubCard
import com.example.ui.components.ConnectivityHealthMonitorCard
import com.example.ui.components.ConnectivityStatusIndicatorChip
import com.example.ui.components.DashboardCardGridLayout
import com.example.ui.components.DeviceThresholdsCard
import com.example.ui.components.ExportServerMetricsDialog
import com.example.ui.components.FirestoreSyncCard
import com.example.ui.components.MainDashboardSummaryCards
import com.example.ui.components.MainNetworkMetricsDashboardComponent
import com.example.ui.components.NetGuardOfficialWebsiteButton
import com.example.ui.components.NetGuardWebsiteResourceCard
import com.example.ui.components.NetworkDiagnosticPingCard
import com.example.ui.components.NetworkExecutiveSummaryCard
import com.example.ui.components.NetworkTraffic24hTrendsDashboardCard
import com.example.ui.components.NetworkTrafficNotificationCard
import com.example.ui.components.PulsingRadarDot
import com.example.ui.components.RealtimeNetworkEventsLogCard
import com.example.ui.components.RealtimeThreatNotificationBanner
import com.example.ui.components.RechartsInteractiveLineChart
import com.example.ui.components.RogueDeviceDetectionCard
import com.example.ui.components.RogueRemediationComponent
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val rogues by viewModel.rogueDevices.collectAsState()
    val bandwidth by viewModel.bandwidthMetrics.collectAsState()
    val healthMetrics by viewModel.networkHealth.collectAsState()
    val criticalServers by viewModel.criticalServers.collectAsState()
    val serverMetrics by viewModel.serverMetrics.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val backendStatus by viewModel.backendStatus.collectAsState()

    var showExportServerMetricsDialog by remember { mutableStateOf(false) }

    val onlineCount = devices.count { it.status == DeviceStatus.ONLINE }
    val totalCount = devices.size
    val activeRoguesCount = rogues.count { it.status.name != "ISOLATED" && it.status.name != "TRUSTED" }

    val systemLoadPercentage: Float = remember(serverMetrics, criticalServers, devices) {
        val cpuValues = mutableListOf<Float>()
        serverMetrics.forEach { cpuValues.add(it.cpuPercent) }
        criticalServers.forEach { cpuValues.add(it.cpuPercent) }
        devices.forEach { dev -> dev.cpuUsage?.let { cpuValues.add(it) } }
        if (cpuValues.isNotEmpty()) {
            cpuValues.average().toFloat()
        } else {
            38.5f
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Real-time Notification System: Alerts user when a rogue device or security threat is detected
        item {
            RealtimeThreatNotificationBanner(
                viewModel = viewModel,
                onNavigateToThreats = { viewModel.selectTab(AppTab.INTRUSION) }
            )
        }

        // NetGuard Cyber AI Intelligence Hub Quick Launch Card
        item {
            AiIntelligenceHubCard(
                viewModel = viewModel
            )
        }

        // Main Dashboard Key Network Metrics Component: Bandwidth Usage, Active Devices, and Server Health Summary
        item {
            MainNetworkMetricsDashboardComponent(
                viewModel = viewModel
            )
        }

        // Main Dashboard Card-Based Grid Layout (High-level network status, device count, and recent security alerts)
        item {
            DashboardCardGridLayout(
                viewModel = viewModel
            )
        }

        // 0. Main Executive Network Summary Dashboard Component (حالة الشبكة، الأجهزة المتصلة، التهديدات الأمنية النشطة - يدعم العربية والوضع الليلي/العادي)
        item {
            NetworkExecutiveSummaryCard(
                viewModel = viewModel
            )
        }

        // Official NetGuard Company Website & Resources Button Component (يفتح الموقع الرسمي عبر Intent للوصول المباشر للموارد)
        item {
            NetGuardWebsiteResourceCard(
                viewModel = viewModel
            )
        }

        // Dedicated Enterprise Connectivity Health Monitor (مراقب فحص الاتصال المؤسسي للنقاط الحيوية)
        item {
            ConnectivityHealthMonitorCard(
                viewModel = viewModel
            )
        }

        // 1. Top Network Health Sentinel Banner
        item {
            NetworkHealthOverviewCard(
                healthMetrics = healthMetrics,
                backendStatus = backendStatus,
                isScanning = isScanning,
                activeRoguesCount = activeRoguesCount,
                onTriggerScan = { viewModel.triggerScan() }
            )
        }

        // 1.5 Main Dashboard UI Summary Cards (Active Devices, Rogue Devices Detected, System Load Percentage)
        item {
            MainDashboardSummaryCards(
                activeDevicesCount = onlineCount,
                totalDevicesCount = totalCount,
                rogueDevicesCount = activeRoguesCount,
                systemLoadPercentage = systemLoadPercentage,
                onActiveDevicesClick = { viewModel.selectTab(AppTab.DEVICES) },
                onRogueDevicesClick = { viewModel.selectTab(AppTab.ROGUE_DETECTOR) },
                onSystemLoadClick = { viewModel.selectTab(AppTab.REMEDIATION) }
            )
        }

        // 2. Active Devices & Asset Health Summary Card
        item {
            TotalActiveDevicesCard(
                onlineCount = onlineCount,
                totalCount = totalCount,
                activeRoguesCount = activeRoguesCount,
                devices = devices,
                onViewAllDevices = { viewModel.selectTab(AppTab.DEVICES) },
                onViewRogues = { viewModel.selectTab(AppTab.ROGUE_DETECTOR) }
            )
        }

        // 2.5 Rogue Device Detection & Whitelist Verification Sentinel Card
        item {
            RogueDeviceDetectionCard(
                viewModel = viewModel
            )
        }

        // 2.6 Rogue Device Remediation & Access Blocking (عزل الأجهزة واعداد مراقبة الجهاز)
        item {
            RogueRemediationComponent(
                viewModel = viewModel
            )
        }

        // 2.7 Real-Time Network Events & Remediation Alerts Stream (سجل أحداث الشبكة الفوري وتنبيهات المعالجة)
        item {
            RealtimeNetworkEventsLogCard(
                viewModel = viewModel
            )
        }

        // 2.8 Firebase Firestore Cloud Persistence & Cross-Session Sync (المزامنة السحابية عبر فايرستور)
        item {
            FirestoreSyncCard(
                viewModel = viewModel
            )
        }

        // 2.9 Network Diagnostic Utility (Ping specific IP addresses directly from dashboard to verify connectivity)
        item {
            NetworkDiagnosticPingCard(
                viewModel = viewModel
            )
        }

        // 2.10 Device Thresholds Management (عتبات الأجهزة)
        item {
            DeviceThresholdsCard(
                viewModel = viewModel,
                onPingDevice = { ip ->
                    viewModel.pingTarget(ip)
                }
            )
        }

        // 2.11 Push Notification Service (Triggers alerts when network traffic exceeds established thresholds)
        item {
            NetworkTrafficNotificationCard(
                viewModel = viewModel
            )
        }

        // 3. Visual Dashboard Summary (Recharts / D3 24-Hour Network Traffic Trends)
        item {
            NetworkTraffic24hTrendsDashboardCard(
                viewModel = viewModel
            )
        }

        // 3.1 Recharts Interactive Multi-Series Line Chart (Real-Time Live Streaming)
        item {
            RechartsInteractiveLineChart(
                viewModel = viewModel
            )
        }

        // 3.1 Real-Time Bandwidth Usage & Throughput Waveform
        item {
            BandwidthUsageCard(
                bandwidth = bandwidth
            )
        }

        // 4. Aggregate Cluster Server Health Metrics Widget (CPU, RAM, Disk & Temperature)
        item {
            ClusterAggregateHealthCard(
                serverMetrics = serverMetrics,
                criticalServers = criticalServers,
                onNavigateToRemediation = { viewModel.selectTab(AppTab.REMEDIATION) },
                onExportMetrics = { showExportServerMetricsDialog = true }
            )
        }

        // 5. Monitored Devices Health & Server Metrics Grid
        item {
            MonitoredDevicesHealthGrid(
                devices = devices,
                onDeviceClick = { device ->
                    viewModel.pingServer(device.id, device.name)
                }
            )
        }

        // 6. Critical Servers Header
        item {
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
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Critical Infrastructure & Servers",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberNavyCard)
                        .border(1.dp, CyberNavyBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${criticalServers.size} Hosts",
                        color = CyberCyanLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 5. Status List of Critical Servers
        items(criticalServers, key = { it.id }) { server ->
            CriticalServerStatusCard(
                server = server,
                onPingClick = {
                    viewModel.pingServer(server.id, server.name)
                },
                onRemediateClick = {
                    viewModel.selectTab(AppTab.REMEDIATION)
                }
            )
        }

        // 6. Active Security Alerts & Live Telemetry
        if (alerts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recent_alerts_header"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Perimeter Alerts",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${alerts.size} Active",
                        color = if (activeRoguesCount > 0) StatusRogueCrimson else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(alerts.take(3), key = { it.id }) { alert ->
                DashboardAlertCard(
                    alert = alert,
                    modifier = Modifier.testTag("recent_alert_${alert.id}"),
                    onIsolateClick = {
                        if (alert.deviceId?.startsWith("rogue") == true) {
                            viewModel.isolateRogue(alert.deviceId)
                        }
                    },
                    onDismissClick = {
                        viewModel.dismissAlert(alert.id)
                    }
                )
            }
        }

        // Bottom space padding for comfortable scrolling
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showExportServerMetricsDialog) {
        ExportServerMetricsDialog(
            serverMetrics = serverMetrics,
            criticalServers = criticalServers,
            viewModel = viewModel,
            onDismiss = { showExportServerMetricsDialog = false }
        )
    }
}

/**
 * 1. Visualizes Real-Time Network Health Metrics
 */
@Composable
fun NetworkHealthOverviewCard(
    healthMetrics: NetworkHealthMetrics,
    backendStatus: String,
    isScanning: Boolean,
    activeRoguesCount: Int,
    onTriggerScan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("network_health_card"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (activeRoguesCount > 0) StatusRogueCrimson.copy(alpha = 0.5f) else CyberNavyBorder
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                    PulsingRadarDot(color = if (activeRoguesCount > 0) StatusRogueCrimson else CyberCyan)
                    Text(
                        text = "REAL-TIME NETWORK HEALTH",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Quick Scan Action Button
                IconButton(
                    onClick = onTriggerScan,
                    enabled = !isScanning,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CyberNavyDark)
                        .testTag("health_scan_button")
                ) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.Sync else Icons.Default.Refresh,
                        contentDescription = "Refresh Health",
                        tint = if (isScanning) CyberCyanLight else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Health Gauge & Score Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Gauge Graphic
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(76.dp)) {
                        val strokeWidth = 8.dp.toPx()
                        // Background track
                        drawArc(
                            color = CyberNavyBorder,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Progress Arc
                        val progressSweep = 270f * (healthMetrics.healthScore / 100f)
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(CyberCyan, StatusOnlineGreen, CyberCyanLight)
                            ),
                            startAngle = 135f,
                            sweepAngle = progressSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${healthMetrics.healthScore}%",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "HEALTH",
                            color = CyberCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Health Status Information
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (activeRoguesCount > 0) StatusWarningAmber else StatusOnlineGreen)
                        )
                        Text(
                            text = if (activeRoguesCount > 0) "Perimeter Warning Detected" else "Network Condition: Nominal",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = backendStatus,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    // Posture Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusOnlineGreen.copy(alpha = 0.15f))
                            .border(1.dp, StatusOnlineGreen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Zero-Trust Sentinel Active • SLA 99.98%",
                            color = StatusOnlineGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = CyberNavyBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 4 Mini Health Metric Indicators Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthMetricStat(
                    label = "Avg Latency",
                    value = "${healthMetrics.averageLatencyMs} ms",
                    icon = Icons.Default.Speed,
                    tint = CyberCyan
                )
                HealthMetricStat(
                    label = "Packet Loss",
                    value = "${healthMetrics.packetLossPercent}%",
                    icon = Icons.Default.NetworkCheck,
                    tint = StatusOnlineGreen
                )
                HealthMetricStat(
                    label = "DNS Resolve",
                    value = "${healthMetrics.dnsResolutionMs.toInt()} ms",
                    icon = Icons.Default.Public,
                    tint = CyberBlueLight
                )
                HealthMetricStat(
                    label = "Core Gateway",
                    value = healthMetrics.activeGatewayIp,
                    icon = Icons.Default.Router,
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
fun HealthMetricStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * 2. Visualizes Total Active Devices & Breakdown
 */
@Composable
fun TotalActiveDevicesCard(
    onlineCount: Int,
    totalCount: Int,
    activeRoguesCount: Int,
    devices: List<Device>,
    onViewAllDevices: () -> Unit,
    onViewRogues: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_devices_card"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        tint = StatusOnlineGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Total Active Devices",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onViewAllDevices,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "View Inventory →",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Device Numbers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$onlineCount",
                        color = StatusOnlineGreen,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = " / $totalCount Active",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 5.dp, start = 4.dp)
                    )
                }

                val activeRatio = if (totalCount > 0) (onlineCount.toFloat() / totalCount * 100).toInt() else 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusOnlineGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$activeRatio% Online",
                        color = StatusOnlineGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar showing online ratio
            LinearProgressIndicator(
                progress = { if (totalCount > 0) onlineCount.toFloat() / totalCount else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StatusOnlineGreen,
                trackColor = CyberNavyDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Device Categories Sub-Grid
            val serversCount = devices.count { it.type == DeviceType.SERVER }
            val infrastructureCount = devices.count { it.type == DeviceType.ROUTER || it.type == DeviceType.SWITCH }
            val endpointsCount = devices.count { it.type == DeviceType.WORKSTATION || it.type == DeviceType.CAMERA || it.type == DeviceType.PRINTER }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeviceCategoryPill(
                    title = "Infrastructure",
                    count = "$infrastructureCount",
                    subtext = "Switches/Gateways",
                    icon = Icons.Default.DeviceHub,
                    color = CyberCyan,
                    modifier = Modifier.weight(1f)
                )

                DeviceCategoryPill(
                    title = "Servers",
                    count = "$serversCount",
                    subtext = "Critical Hosts",
                    icon = Icons.Default.Dns,
                    color = CyberBlueLight,
                    modifier = Modifier.weight(1f)
                )

                DeviceCategoryPill(
                    title = "Endpoints",
                    count = "$endpointsCount",
                    subtext = "PCs & IoT",
                    icon = Icons.Default.Computer,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                if (activeRoguesCount > 0) {
                    DeviceCategoryPill(
                        title = "Rogue",
                        count = "$activeRoguesCount",
                        subtext = "Alert",
                        icon = Icons.Default.Warning,
                        color = StatusRogueCrimson,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onViewRogues() }
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceCategoryPill(
    title: String,
    count: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberNavyDark)
            .border(1.dp, CyberNavyBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = subtext, color = TextMuted, fontSize = 9.sp)
        }
    }
}

/**
 * 3. Visualizes Current Bandwidth Usage with Live Chart & Speeds
 */
@Composable
fun BandwidthUsageCard(
    bandwidth: BandwidthMetrics
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bandwidth_usage_card"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Current Bandwidth Usage",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberCyan.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "1.0 Gbps Pipe",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Download & Upload Speed Tiles Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Download Throughput
                BandwidthSpeedTile(
                    title = "DOWNLOAD (INGRESS)",
                    speedMbps = bandwidth.downloadMbps,
                    icon = Icons.Default.ArrowDownward,
                    color = CyberCyan,
                    modifier = Modifier.weight(1f)
                )

                // Upload Throughput
                BandwidthSpeedTile(
                    title = "UPLOAD (EGRESS)",
                    speedMbps = bandwidth.uploadMbps,
                    icon = Icons.Default.ArrowUpward,
                    color = CyberBlueLight,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Waveform Canvas
            Text(
                text = "Live Throughput Stream (Mbps)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberNavyDark)
                    .border(1.dp, CyberNavyBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val maxCapacity = 500f // Scaling max for chart

                    // Draw subtle grid lines
                    val gridLines = 3
                    for (i in 1..gridLines) {
                        val y = h * (i.toFloat() / (gridLines + 1))
                        drawLine(
                            color = CyberNavyBorder.copy(alpha = 0.6f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Draw Download curve (Cyan)
                    if (bandwidth.downloadHistory.size > 1) {
                        val dlPath = Path()
                        val dlPoints = bandwidth.downloadHistory
                        val stepX = w / (dlPoints.size - 1)

                        dlPoints.forEachIndexed { index, value ->
                            val x = index * stepX
                            val normalizedY = (1f - (value / maxCapacity).coerceIn(0.1f, 0.95f)) * h
                            if (index == 0) {
                                dlPath.moveTo(x, normalizedY)
                            } else {
                                dlPath.lineTo(x, normalizedY)
                            }
                        }

                        // Gradient fill under download path
                        val fillPath = Path()
                        fillPath.addPath(dlPath)
                        fillPath.lineTo(w, h)
                        fillPath.lineTo(0f, h)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(CyberCyan.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )

                        // Main stroke line
                        drawPath(
                            path = dlPath,
                            color = CyberCyan,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Pulsing tip point on the latest data point
                        val lastX = (dlPoints.size - 1) * stepX
                        val lastY = (1f - (dlPoints.last() / maxCapacity).coerceIn(0.1f, 0.95f)) * h
                        drawCircle(
                            color = CyberCyanLight,
                            radius = 4.dp.toPx(),
                            center = Offset(lastX, lastY)
                        )
                    }

                    // Draw Upload curve (Blue Light)
                    if (bandwidth.uploadHistory.size > 1) {
                        val ulPath = Path()
                        val ulPoints = bandwidth.uploadHistory
                        val stepX = w / (ulPoints.size - 1)

                        ulPoints.forEachIndexed { index, value ->
                            val x = index * stepX
                            val normalizedY = (1f - (value / maxCapacity).coerceIn(0.05f, 0.95f)) * h
                            if (index == 0) {
                                ulPath.moveTo(x, normalizedY)
                            } else {
                                ulPath.lineTo(x, normalizedY)
                            }
                        }

                        drawPath(
                            path = ulPath,
                            color = CyberBlueLight,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Capacity Bar Indicator
            val totalTrafficMbps = bandwidth.downloadMbps + bandwidth.uploadMbps
            val utilizationPercent = (totalTrafficMbps / bandwidth.totalCapacityMbps * 100).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pipe Saturation: ${totalTrafficMbps.toInt()} Mbps / 1000 Mbps",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "$utilizationPercent% Loaded",
                    color = if (utilizationPercent > 80) StatusOfflineRed else CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (totalTrafficMbps / bandwidth.totalCapacityMbps).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp)),
                color = CyberCyan,
                trackColor = CyberNavyDark
            )
        }
    }
}

@Composable
fun BandwidthSpeedTile(
    title: String,
    speedMbps: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberNavyDark)
            .border(1.dp, CyberNavyBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format("%.1f", speedMbps),
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " Mbps",
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

/**
 * 4. Status List of Critical Servers
 */
@Composable
fun CriticalServerStatusCard(
    server: CriticalServer,
    onPingClick: () -> Unit,
    onRemediateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_card_${server.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (server.status == DeviceStatus.WARNING) StatusWarningAmber.copy(alpha = 0.5f) else CyberNavyBorder
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Server Header (Name, IP, StatusBadge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingRadarDot(
                            color = when (server.status) {
                                DeviceStatus.ONLINE -> StatusOnlineGreen
                                DeviceStatus.WARNING -> StatusWarningAmber
                                DeviceStatus.OFFLINE -> StatusOfflineRed
                                DeviceStatus.UNSTABLE -> CyberBlueLight
                            }
                        )
                        Text(
                            text = server.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = server.role,
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "IP: ${server.ip} • Temp: ${server.temperatureC}°C",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                StatusBadge(status = server.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resource Bars Row (CPU, RAM, Disk)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ServerResourceBar(
                    label = "CPU Load",
                    percent = server.cpuPercent,
                    color = when {
                        server.cpuPercent > 80f -> StatusOfflineRed
                        server.cpuPercent > 65f -> StatusWarningAmber
                        else -> CyberCyan
                    },
                    modifier = Modifier.weight(1f)
                )

                ServerResourceBar(
                    label = "RAM Use",
                    percent = server.ramPercent,
                    color = when {
                        server.ramPercent > 85f -> StatusOfflineRed
                        server.ramPercent > 70f -> StatusWarningAmber
                        else -> CyberBlueLight
                    },
                    modifier = Modifier.weight(1f)
                )

                ServerResourceBar(
                    label = "Storage",
                    percent = server.diskPercent,
                    color = when {
                        server.diskPercent > 90f -> StatusOfflineRed
                        server.diskPercent > 80f -> StatusWarningAmber
                        else -> StatusOnlineGreen
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Microservices Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                server.services.take(3).forEach { service ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberNavyDark)
                            .border(1.dp, CyberNavyBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (service.isRunning) StatusOnlineGreen else StatusOfflineRed)
                            )
                            Text(
                                text = service.name.substringBefore(" "),
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Response Latency Chip
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberNavyDark)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "RTT: ${server.lastPing}",
                        color = CyberCyanLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = CyberNavyBorder.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Uptime: ${server.uptimePercent}%",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quick Ping Button
                    OutlinedButton(
                        onClick = onPingClick,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkPing,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Ping", color = CyberCyan, fontSize = 11.sp)
                    }

                    // SSH Remediation Trigger
                    Button(
                        onClick = onRemediateClick,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "SSH Terminal", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ServerResourceBar(
    label: String,
    percent: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextSecondary, fontSize = 10.sp)
            Text(
                text = "${percent.toInt()}%",
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = CyberNavyDark
        )
    }
}

@Composable
fun DashboardAlertCard(
    alert: AlertLog,
    onIsolateClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> StatusRogueCrimson
        AlertSeverity.WARNING -> StatusWarningAmber
        AlertSeverity.INFO -> CyberBlueLight
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor.copy(alpha = 0.6f))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(borderColor)
                    )
                    Text(
                        text = alert.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = alert.timestamp,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = alert.message,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notified: ${alert.channelNotified}",
                    color = CyberCyanLight,
                    fontSize = 11.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (alert.deviceId?.startsWith("rogue") == true) {
                        Button(
                            onClick = onIsolateClick,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRogueCrimson),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Isolate", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = onDismissClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                        )
                    ) {
                        Text("Dismiss", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

/**
 * Cluster Aggregate Server Health Metrics Card
 * Visualizes aggregate CPU usage, memory consumption, storage load, and cluster health status
 * across the entire server cluster with breakdown indicators and high-load warnings.
 */
@Composable
fun ClusterAggregateHealthCard(
    serverMetrics: List<ServerMetric>,
    criticalServers: List<CriticalServer>,
    onNavigateToRemediation: () -> Unit,
    onExportMetrics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalHosts = (serverMetrics.map { it.serverId } + criticalServers.map { it.id }).distinct().size.coerceAtLeast(1)

    // Compute cluster-wide aggregate metrics
    val avgCpu = if (serverMetrics.isNotEmpty()) {
        serverMetrics.map { it.cpuPercent }.average().toFloat()
    } else if (criticalServers.isNotEmpty()) {
        criticalServers.map { it.cpuPercent }.average().toFloat()
    } else {
        45f
    }

    val avgRam = if (serverMetrics.isNotEmpty()) {
        serverMetrics.map { it.ramPercent }.average().toFloat()
    } else if (criticalServers.isNotEmpty()) {
        criticalServers.map { it.ramPercent }.average().toFloat()
    } else {
        62f
    }

    val avgDisk = if (serverMetrics.isNotEmpty()) {
        serverMetrics.map { it.diskPercent }.average().toFloat()
    } else if (criticalServers.isNotEmpty()) {
        criticalServers.map { it.diskPercent }.average().toFloat()
    } else {
        54f
    }

    val avgTemp = if (serverMetrics.isNotEmpty()) {
        serverMetrics.map { it.temperatureC }.average().toFloat()
    } else if (criticalServers.isNotEmpty()) {
        criticalServers.map { it.temperatureC }.average().toFloat()
    } else {
        42f
    }

    // High utilization warning counts
    val highCpuCount = serverMetrics.count { it.cpuPercent >= 80f } + criticalServers.count { it.cpuPercent >= 80f }
    val highRamCount = serverMetrics.count { it.ramPercent >= 80f } + criticalServers.count { it.ramPercent >= 80f }
    val isClusterWarning = avgCpu > 75f || avgRam > 80f || highCpuCount > 0 || highRamCount > 0

    // Total running services across the cluster
    val totalServices = serverMetrics.flatMap { it.services }.size
    val activeServices = serverMetrics.flatMap { it.services }.count { it.isRunning }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cluster_aggregate_health_card"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Widget Title Header
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
                            .background(CyberBlue.copy(alpha = 0.2f))
                            .border(1.dp, CyberBlueLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "CLUSTER HEALTH METRICS",
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "Aggregate Load Across $totalHosts Server Nodes",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = onExportMetrics,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CyberCyan.copy(alpha = 0.15f),
                            contentColor = CyberCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("export_server_metrics_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export Server Metrics",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Export",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Cluster Health Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isClusterWarning) StatusWarningAmber.copy(alpha = 0.15f) else StatusOnlineGreen.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                if (isClusterWarning) StatusWarningAmber.copy(alpha = 0.4f) else StatusOnlineGreen.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PulsingRadarDot(
                                color = if (isClusterWarning) StatusWarningAmber else StatusOnlineGreen,
                                modifier = Modifier.size(8.dp)
                            )
                            Text(
                                text = if (isClusterWarning) "HIGH LOAD" else "BALANCED",
                                color = if (isClusterWarning) StatusWarningAmber else StatusOnlineGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Gauges Row: CPU Usage & Memory Consumption
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClusterMetricGauge(
                    title = "Cluster CPU Load",
                    percent = avgCpu,
                    activeLabel = "${avgCpu.toInt()}% Avg",
                    subtext = if (highCpuCount > 0) "$highCpuCount nodes > 80%" else "Nominal usage",
                    icon = Icons.Default.Memory,
                    barColor = when {
                        avgCpu > 80f -> StatusOfflineRed
                        avgCpu > 65f -> StatusWarningAmber
                        else -> CyberCyan
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "cluster_cpu_gauge"
                )

                ClusterMetricGauge(
                    title = "Cluster RAM Load",
                    percent = avgRam,
                    activeLabel = "${avgRam.toInt()}% Avg",
                    subtext = if (highRamCount > 0) "$highRamCount nodes > 80%" else "Optimal headroom",
                    icon = Icons.Default.Storage,
                    barColor = when {
                        avgRam > 85f -> StatusOfflineRed
                        avgRam > 70f -> StatusWarningAmber
                        else -> ChartTxPurple
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "cluster_ram_gauge"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Metrics Row: Storage Capacity, Temperature, & Microservices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClusterMiniStat(
                    title = "Avg Disk Load",
                    value = "${avgDisk.toInt()}%",
                    subtext = "Pooled Storage",
                    icon = Icons.Default.Folder,
                    tint = CyberBlueLight,
                    modifier = Modifier.weight(1f)
                )

                ClusterMiniStat(
                    title = "Thermal Avg",
                    value = "${avgTemp.toInt()}°C",
                    subtext = "Chassis Temp",
                    icon = Icons.Default.Thermostat,
                    tint = if (avgTemp > 50f) StatusWarningAmber else StatusOnlineGreen,
                    modifier = Modifier.weight(1f)
                )

                ClusterMiniStat(
                    title = "Core Services",
                    value = if (totalServices > 0) "$activeServices/$totalServices" else "12/12",
                    subtext = "Active Daemons",
                    icon = Icons.Default.CheckCircle,
                    tint = StatusOnlineGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // High load remediation prompt if cluster load is elevated
            if (isClusterWarning) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusWarningAmber.copy(alpha = 0.1f))
                        .border(1.dp, StatusWarningAmber.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable { onNavigateToRemediation() }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = StatusWarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Elevated cluster consumption detected. Run automated remediation.",
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Remediate",
                        tint = StatusWarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Telemetry Archive & Export Action Strip
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberNavyDark)
                    .border(1.dp, CyberNavyBorder, RoundedCornerShape(8.dp))
                    .clickable { onExportMetrics() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("cluster_metrics_archive_strip"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Archive Server Telemetry (CSV / JSON)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Export",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClusterMetricGauge(
    title: String,
    percent: Float,
    activeLabel: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    barColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(CyberNavyDark)
            .border(1.dp, CyberNavyBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = barColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = activeLabel,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (percent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = barColor,
                trackColor = CyberNavyCard
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtext,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ClusterMiniStat(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberNavyDark)
            .border(1.dp, CyberNavyBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextMuted, fontSize = 9.5.sp)
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(11.dp))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(text = subtext, color = TextSecondary, fontSize = 8.5.sp)
        }
    }
}

/**
 * Monitored Devices Health & Server Metrics Grid
 * Summarizes the health status and real-time CPU & RAM usage of all monitored devices in an interactive grid layout.
 */
@Composable
fun MonitoredDevicesHealthGrid(
    devices: List<Device>,
    onDeviceClick: (Device) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }

    val onlineCount = devices.count { it.status == DeviceStatus.ONLINE }
    val warningCount = devices.count { it.status == DeviceStatus.WARNING }
    val offlineCount = devices.count { it.status == DeviceStatus.OFFLINE }

    val filteredDevices = remember(devices, selectedFilter) {
        when (selectedFilter) {
            "Servers" -> devices.filter { it.type == DeviceType.SERVER }
            "Infrastructure" -> devices.filter { it.type == DeviceType.ROUTER || it.type == DeviceType.SWITCH }
            "Endpoints" -> devices.filter { it.type != DeviceType.SERVER && it.type != DeviceType.ROUTER && it.type != DeviceType.SWITCH }
            "Attention" -> devices.filter { it.status != DeviceStatus.ONLINE }
            else -> devices
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monitored_devices_health_grid"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "MONITORED DEVICES HEALTH GRID",
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "Real-time CPU, RAM & status across ${devices.size} hosts",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberNavyDark)
                        .border(1.dp, CyberNavyBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${filteredDevices.size} Hosts",
                        color = CyberCyanLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Health Status Summary Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberNavyDark)
                    .border(1.dp, CyberNavyBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusOnlineGreen)
                    )
                    Text(
                        text = "$onlineCount Healthy",
                        color = StatusOnlineGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusWarningAmber)
                    )
                    Text(
                        text = "$warningCount Warning",
                        color = StatusWarningAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusOfflineRed)
                    )
                    Text(
                        text = "$offlineCount Offline",
                        color = StatusOfflineRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf(
                    "All" to "All",
                    "Servers" to "Servers",
                    "Infra" to "Infrastructure",
                    "Endpoints" to "Endpoints",
                    "Alerts" to "Attention"
                )
                filters.forEach { (label, key) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberNavyDark)
                            .border(
                                1.dp,
                                if (isSelected) CyberCyan else CyberNavyBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedFilter = key }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) CyberCyan else TextSecondary,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid rows (2 columns)
            val chunked = filteredDevices.chunked(2)
            chunked.forEachIndexed { index, rowDevices ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowDevices.forEach { device ->
                        DeviceHealthGridCard(
                            device = device,
                            onClick = { onDeviceClick(device) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowDevices.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (index < chunked.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

/**
 * Individual device health card displayed in the grid layout, showcasing live CPU & RAM usage meters.
 */
@Composable
fun DeviceHealthGridCard(
    device: Device,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (device.status) {
        DeviceStatus.WARNING -> StatusWarningAmber.copy(alpha = 0.6f)
        DeviceStatus.OFFLINE -> StatusOfflineRed.copy(alpha = 0.6f)
        DeviceStatus.UNSTABLE -> CyberBlueLight.copy(alpha = 0.5f)
        DeviceStatus.ONLINE -> CyberNavyBorder
    }

    val typeIcon = when (device.type) {
        DeviceType.ROUTER -> Icons.Default.Router
        DeviceType.SWITCH -> Icons.Default.DeviceHub
        DeviceType.SERVER -> Icons.Default.Dns
        DeviceType.WORKSTATION -> Icons.Default.Computer
        DeviceType.CAMERA -> Icons.Default.Videocam
        DeviceType.PRINTER -> Icons.Default.Print
        DeviceType.ROGUE -> Icons.Default.Warning
    }

    val cpu = device.cpuUsage ?: 0f
    val ram = device.ramUsage ?: 0f

    val cpuColor = when {
        cpu >= 80f -> StatusOfflineRed
        cpu >= 65f -> StatusWarningAmber
        else -> CyberCyan
    }

    val ramColor = when {
        ram >= 85f -> StatusOfflineRed
        ram >= 70f -> StatusWarningAmber
        else -> CyberBlueLight
    }

    Box(
        modifier = modifier
            .testTag("device_metric_card_${device.id}")
            .clip(RoundedCornerShape(12.dp))
            .background(CyberNavyDark)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            // Header Row: Type Icon & Status Indicator
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
                            .size(22.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                when (device.type) {
                                    DeviceType.SERVER -> CyberCyan.copy(alpha = 0.15f)
                                    DeviceType.ROUTER, DeviceType.SWITCH -> CyberBlueLight.copy(alpha = 0.15f)
                                    else -> TextMuted.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = when (device.type) {
                                DeviceType.SERVER -> CyberCyan
                                DeviceType.ROUTER, DeviceType.SWITCH -> CyberBlueLight
                                else -> TextPrimary
                            },
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = device.type.name,
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Pulsing dot + status label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    PulsingRadarDot(
                        color = when (device.status) {
                            DeviceStatus.ONLINE -> StatusOnlineGreen
                            DeviceStatus.WARNING -> StatusWarningAmber
                            DeviceStatus.OFFLINE -> StatusOfflineRed
                            DeviceStatus.UNSTABLE -> CyberBlueLight
                        },
                        modifier = Modifier.size(7.dp)
                    )
                    Text(
                        text = device.status.label,
                        color = when (device.status) {
                            DeviceStatus.ONLINE -> StatusOnlineGreen
                            DeviceStatus.WARNING -> StatusWarningAmber
                            DeviceStatus.OFFLINE -> StatusOfflineRed
                            DeviceStatus.UNSTABLE -> CyberBlueLight
                        },
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Device Name
            Text(
                text = device.name,
                color = TextPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // IP & Latency
            Text(
                text = if (device.status == DeviceStatus.OFFLINE) "${device.ip} • Offline" else "${device.ip} • ${device.latencyMs}ms",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            // CPU Load Metric Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CPU",
                        color = TextMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (device.status == DeviceStatus.OFFLINE) "--" else "${cpu.toInt()}%",
                        color = if (device.status == DeviceStatus.OFFLINE) TextMuted else cpuColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { if (device.status == DeviceStatus.OFFLINE) 0f else (cpu / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = cpuColor,
                    trackColor = CyberNavyCard
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // RAM Load Metric Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RAM",
                        color = TextMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (device.status == DeviceStatus.OFFLINE) "--" else "${ram.toInt()}%",
                        color = if (device.status == DeviceStatus.OFFLINE) TextMuted else ramColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { if (device.status == DeviceStatus.OFFLINE) 0f else (ram / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = ramColor,
                    trackColor = CyberNavyCard
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Footer: Uptime & Uptime Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Uptime",
                    color = TextMuted,
                    fontSize = 9.sp
                )
                Text(
                    text = "${String.format("%.1f", device.uptimePercent)}%",
                    color = if (device.uptimePercent > 99f) CyberCyanLight else TextSecondary,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


