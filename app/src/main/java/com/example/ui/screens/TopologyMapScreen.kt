package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.DeviceStatus
import com.example.data.model.DeviceType
import com.example.data.model.NodeBandwidthTraffic
import com.example.data.model.TopologyNode
import com.example.ui.MainViewModel
import com.example.ui.components.DeviceTypeIcon
import com.example.ui.components.ExportReportDialog
import com.example.ui.components.NetworkTopologyViewer
import com.example.ui.components.PulsingRadarDot
import com.example.ui.components.RealtimeBandwidthChart
import com.example.ui.components.StatusBadge
import com.example.ui.components.TopologyFilterMode
import com.example.ui.theme.*

enum class TopologyScreenLayout(val title: String, val titleArabic: String, val icon: ImageVector) {
    SPLIT("Mesh & Chart", "المخطط والبيانات", Icons.Default.VerticalSplit),
    MESH_FULL("2D Mesh", "مخطط الشبكة 2D", Icons.Default.AccountTree),
    BANDWIDTH_ONLY("Bandwidth Monitor", "مراقبة النطاق الترددي", Icons.Default.Timeline)
}

@Composable
fun TopologyMapScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val appLanguage = LocalAppLanguage.current
    val isArabic = appLanguage == AppLanguage.ARABIC
    val colors = getNetGuardColors(isDark)

    val topologyNodes by viewModel.topologyNodes.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val nodeTrafficMap by viewModel.nodeTrafficMap.collectAsState()
    val serverMetrics by viewModel.serverMetrics.collectAsState()
    val networkHealth by viewModel.networkHealth.collectAsState()
    val bandwidthMetrics by viewModel.bandwidthMetrics.collectAsState()
    val slaReport by viewModel.currentSlaReport.collectAsState()

    var selectedNodeId by remember { mutableStateOf<String?>("dev-04") }
    var filterMode by remember { mutableStateOf(TopologyFilterMode.ALL) }
    var showDataFlow by remember { mutableStateOf(true) }
    var layoutMode by remember { mutableStateOf(TopologyScreenLayout.SPLIT) }
    var showExportDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val selectedNode = topologyNodes.find { it.id == selectedNodeId }

    val totalCount = topologyNodes.size
    val onlineCount = topologyNodes.count { it.status == DeviceStatus.ONLINE }
    val alertCount = topologyNodes.count { it.activeAlertCount > 0 }
    val rogueCount = topologyNodes.count { it.isRogue }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isArabic) "مخطط طوبولوجيا الشبكة" else "NETWORK TOPOLOGY",
                        color = colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = if (isArabic) "خريطة تفاعلية للشبكة • تدفق الحزم المباشر • فحص الاتصالات" else "Live 2D Mesh Graph • Real-time Threat & Anomaly Highlighting",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Export JSON / Report Button
                OutlinedButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("topology_export_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.6f))
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export Report",
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "تصدير" else "Export",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryAccent
                    )
                }

                IconButton(
                    onClick = { viewModel.triggerScan() },
                    enabled = !isScanning,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.cardBackground)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                        .size(36.dp)
                        .testTag("topology_refresh_button")
                ) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.Sync else Icons.Default.Refresh,
                        contentDescription = if (isArabic) "فحص الشبكة الفرعية" else "Sweep Subnet",
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. Metrics Statistics Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopologyMetricBadge(
                label = if (isArabic) "إجمالي الأجهزة" else "Total Assets",
                value = "$totalCount",
                color = colors.primaryAccent,
                modifier = Modifier.weight(1f)
            )
            TopologyMetricBadge(
                label = if (isArabic) "الروابط النشطة" else "Online Links",
                value = "$onlineCount",
                color = StatusOnlineGreen,
                modifier = Modifier.weight(1f)
            )
            TopologyMetricBadge(
                label = if (isArabic) "التنبيهات النشطة" else "Active Alerts",
                value = "$alertCount",
                color = if (alertCount > 0) StatusWarningAmber else colors.textMuted,
                modifier = Modifier.weight(1f)
            )
            TopologyMetricBadge(
                label = if (isArabic) "التهديدات الدخيلة" else "Rogue Threats",
                value = "$rogueCount",
                color = if (rogueCount > 0) StatusRogueCrimson else colors.textMuted,
                isCritical = rogueCount > 0,
                modifier = Modifier.weight(1f)
            )
        }

        // 2B. Topology Node & Device Search Bar
        val trimmedQuery = searchQuery.trim()
        val matchingNodesCount = if (trimmedQuery.isEmpty()) 0 else topologyNodes.count { node ->
            node.label.contains(trimmedQuery, ignoreCase = true) ||
                    node.ip.contains(trimmedQuery, ignoreCase = true) ||
                    node.id.contains(trimmedQuery, ignoreCase = true)
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                if (query.isNotBlank()) {
                    val firstMatch = topologyNodes.firstOrNull { node ->
                        node.label.contains(query.trim(), ignoreCase = true) ||
                                node.ip.contains(query.trim(), ignoreCase = true) ||
                                node.id.contains(query.trim(), ignoreCase = true)
                    }
                    if (firstMatch != null) {
                        selectedNodeId = firstMatch.id
                    }
                }
            },
            placeholder = {
                Text(
                    text = if (isArabic) "البحث عن عقدة أو عنوان IP (مثل البوابة، 192.168.1.1)..." else "Search nodes by name or IP (e.g. Gateway, 192.168.1.1, dev-01)...",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Topology Nodes",
                    tint = if (searchQuery.isNotEmpty()) colors.primaryAccent else colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (trimmedQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (matchingNodesCount > 0) colors.primaryAccent.copy(alpha = 0.2f) else StatusOfflineRed.copy(alpha = 0.2f))
                                .border(1.dp, if (matchingNodesCount > 0) colors.primaryAccent.copy(alpha = 0.5f) else StatusOfflineRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (matchingNodesCount > 0) {
                                    if (isArabic) "$matchingNodesCount نتيجة" else "$matchingNodesCount found"
                                } else {
                                    if (isArabic) "لا توجد نتائج" else "0 matches"
                                },
                                color = if (matchingNodesCount > 0) colors.primaryAccent else StatusOfflineRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryAccent,
                unfocusedBorderColor = colors.cardBorder,
                focusedContainerColor = colors.cardBackground,
                unfocusedContainerColor = colors.cardBackground,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.primaryAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("topology_search_bar")
        )

        // 3. Layout Mode Switcher (Split vs Mesh Only vs Bandwidth Only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TopologyScreenLayout.entries.forEach { mode ->
                val isSelected = layoutMode == mode
                FilterChip(
                    selected = isSelected,
                    onClick = { layoutMode = mode },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSelected) colors.primaryAccent else colors.textSecondary
                            )
                            Text(
                                text = if (isArabic) mode.titleArabic else mode.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryAccent.copy(alpha = 0.2f),
                        selectedLabelColor = colors.primaryAccent,
                        containerColor = colors.cardBackground,
                        labelColor = colors.textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) colors.primaryAccent else colors.cardBorder
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Filter Chips and Animation Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopologyFilterMode.entries.forEach { mode ->
                val isSelected = filterMode == mode
                FilterChip(
                    selected = isSelected,
                    onClick = { filterMode = mode },
                    label = {
                        Text(
                            text = when (mode) {
                                TopologyFilterMode.ALL -> if (isArabic) "كافة الأصول ($totalCount)" else "All Assets ($totalCount)"
                                TopologyFilterMode.ALERTS_ONLY -> if (isArabic) "التنبيهات ($alertCount)" else "Alerts ($alertCount)"
                                TopologyFilterMode.ROGUE_ONLY -> if (isArabic) "الدخيلة ($rogueCount)" else "Rogues ($rogueCount)"
                                TopologyFilterMode.INFRASTRUCTURE -> if (isArabic) "البنية الأساسية" else "Infra"
                                TopologyFilterMode.ENDPOINTS -> if (isArabic) "الأجهزة الطرفية" else "Endpoints"
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryAccent.copy(alpha = 0.2f),
                        selectedLabelColor = colors.primaryAccent,
                        containerColor = colors.cardBackground,
                        labelColor = colors.textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) colors.primaryAccent else colors.cardBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Data Flow toggle chip
            FilterChip(
                selected = showDataFlow,
                onClick = { showDataFlow = !showDataFlow },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (showDataFlow) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = null,
                            tint = if (showDataFlow) StatusOnlineGreen else colors.textMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(if (isArabic) "تدفق البيانات" else "Data Flow", fontSize = 11.sp)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StatusOnlineGreen.copy(alpha = 0.15f),
                    selectedLabelColor = StatusOnlineGreen,
                    containerColor = colors.cardBackground,
                    labelColor = colors.textSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = showDataFlow,
                    borderColor = if (showDataFlow) StatusOnlineGreen.copy(alpha = 0.5f) else colors.cardBorder
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // 5. Interactive Canvas / Bandwidth Layout Sections
        when (layoutMode) {
            TopologyScreenLayout.SPLIT -> {
                // Top: Interactive Canvas Topology Visualizer
                NetworkTopologyViewer(
                    nodes = topologyNodes,
                    selectedNodeId = selectedNodeId,
                    onNodeSelected = { selectedNodeId = it },
                    filterMode = filterMode,
                    showDataFlow = showDataFlow,
                    searchQuery = searchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.05f)
                )

                // Bottom: Real-Time Bandwidth Monitoring Chart
                val activeTraffic = nodeTrafficMap[selectedNodeId]
                    ?: nodeTrafficMap["dev-01"]
                    ?: NodeBandwidthTraffic.defaultFor(selectedNodeId ?: "dev-01", selectedNode?.label ?: "Core Gateway")

                RealtimeBandwidthChart(
                    nodeTraffic = activeTraffic,
                    allNodes = topologyNodes,
                    selectedNodeId = selectedNodeId ?: "dev-01",
                    onSelectNode = { selectedNodeId = it },
                    onSimulateBurst = { viewModel.simulateBurstTraffic(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.95f)
                )
            }

            TopologyScreenLayout.MESH_FULL -> {
                NetworkTopologyViewer(
                    nodes = topologyNodes,
                    selectedNodeId = selectedNodeId,
                    onNodeSelected = { selectedNodeId = it },
                    filterMode = filterMode,
                    showDataFlow = showDataFlow,
                    searchQuery = searchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                AnimatedVisibility(
                    visible = selectedNode != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    selectedNode?.let { node ->
                        TopologyNodeInspectorCard(
                            node = node,
                            onDismiss = { selectedNodeId = null },
                            onShowBandwidthChart = { layoutMode = TopologyScreenLayout.BANDWIDTH_ONLY },
                            onPing = {
                                viewModel.pingServer(node.id, node.label)
                            },
                            onIsolateRogue = {
                                viewModel.isolateRogue(node.id)
                            },
                            onTrustRogue = {
                                viewModel.trustRogue(node.id)
                            },
                            onRemediate = {
                                viewModel.executeRemediation(node.id, node.label, "systemctl restart postgresql")
                            }
                        )
                    }
                }
            }

            TopologyScreenLayout.BANDWIDTH_ONLY -> {
                val activeTraffic = nodeTrafficMap[selectedNodeId]
                    ?: nodeTrafficMap["dev-01"]
                    ?: NodeBandwidthTraffic.defaultFor(selectedNodeId ?: "dev-01", selectedNode?.label ?: "Core Gateway")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RealtimeBandwidthChart(
                        nodeTraffic = activeTraffic,
                        allNodes = topologyNodes,
                        selectedNodeId = selectedNodeId ?: "dev-01",
                        onSelectNode = { selectedNodeId = it },
                        onSimulateBurst = { viewModel.simulateBurstTraffic(it) }
                    )

                    if (selectedNode != null) {
                        TopologyNodeInspectorCard(
                            node = selectedNode,
                            onDismiss = { selectedNodeId = null },
                            onShowBandwidthChart = { /* already shown */ },
                            onPing = {
                                viewModel.pingServer(selectedNode.id, selectedNode.label)
                            },
                            onIsolateRogue = {
                                viewModel.isolateRogue(selectedNode.id)
                            },
                            onTrustRogue = {
                                viewModel.trustRogue(selectedNode.id)
                            },
                            onRemediate = {
                                viewModel.executeRemediation(selectedNode.id, selectedNode.label, "systemctl restart postgresql")
                            }
                        )
                    }
                }
            }
        }
    }

    // Export Topology & Server Health Report Dialog
    if (showExportDialog) {
        ExportReportDialog(
            topologyNodes = topologyNodes,
            serverMetrics = serverMetrics,
            networkHealth = networkHealth,
            bandwidthMetrics = bandwidthMetrics,
            alerts = alerts,
            slaReport = slaReport,
            nodeTrafficMap = nodeTrafficMap,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun TopologyMetricBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    isCritical: Boolean = false
) {
    val isDark = LocalThemeIsDark.current
    val colors = getNetGuardColors(isDark)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isCritical) StatusRogueCrimson.copy(alpha = 0.5f) else colors.cardBorder
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
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

@Composable
private fun TopologyNodeInspectorCard(
    node: TopologyNode,
    onDismiss: () -> Unit,
    onShowBandwidthChart: () -> Unit,
    onPing: () -> Unit,
    onIsolateRogue: () -> Unit,
    onTrustRogue: () -> Unit,
    onRemediate: () -> Unit
) {
    val isDark = LocalThemeIsDark.current
    val appLanguage = LocalAppLanguage.current
    val isArabic = appLanguage == AppLanguage.ARABIC
    val colors = getNetGuardColors(isDark)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("topology_inspector_card"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (node.isRogue) StatusRogueCrimson
                else if (node.activeAlertCount > 0) StatusWarningAmber
                else colors.primaryAccent
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Node Info & Dismiss
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (node.isRogue) StatusRogueCrimson.copy(alpha = 0.15f)
                                else if (node.activeAlertCount > 0) StatusWarningAmber.copy(alpha = 0.15f)
                                else colors.primaryAccent.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (node.isRogue) StatusRogueCrimson
                                else if (node.activeAlertCount > 0) StatusWarningAmber
                                else colors.primaryAccent,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        DeviceTypeIcon(
                            type = node.type,
                            tint = if (node.isRogue) StatusRogueCrimson
                            else if (node.activeAlertCount > 0) StatusWarningAmber
                            else colors.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = node.label,
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (node.isRogue) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StatusRogueCrimson.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "دخيل مشبوه" else "ROGUE",
                                        color = StatusRogueCrimson,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "IP: ${node.ip} • MAC: ${node.macAddress.ifEmpty { if (isArabic) "رابط ديناميكي" else "Dynamic Link" }}",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = node.status)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isArabic) "إغلاق" else "Close Inspector",
                            tint = colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Link & Uplink Diagnostics
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) CyberNavyDark else LightCanvasBackground)
                    .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "مسار الربط في الشبكة" else "TOPOLOGY UPLINK",
                            color = colors.textSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (node.parentId != null) {
                                if (isArabic) "متصل عبر ${node.parentId} (رابط غيغابت)" else "Connected via ${node.parentId} (Gigabit Uplink)"
                            } else {
                                if (isArabic) "البوابة الأساسية للشبكة (Root)" else "Root Core Backbone"
                            },
                            color = colors.primaryAccent,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (node.status == DeviceStatus.OFFLINE) {
                                if (isArabic) "0 ms (غير متصل)" else "0 ms (Offline)"
                            } else {
                                "${node.latencyMs} ms RTT"
                            },
                            color = if (node.status == DeviceStatus.OFFLINE) StatusOfflineRed else StatusOnlineGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Real-Time Bandwidth Telemetry Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) CyberNavyDark else LightCanvasBackground)
                    .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "معدل تدفق البيانات المباشر" else "LIVE BANDWIDTH THROUGHPUT",
                            color = colors.textSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = ChartRxCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "RX: ${String.format("%.1f", node.incomingMbps)} Mbps",
                                    color = ChartRxCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ChartTxPurple,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "TX: ${String.format("%.1f", node.outgoingMbps)} Mbps",
                                    color = ChartTxPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onShowBandwidthChart,
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent))
                    ) {
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "رسم بياني" else "Chart", fontSize = 10.sp, color = colors.primaryAccent)
                    }
                }
            }

            // Active Alerts Section on this node
            if (node.alertsSummary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isArabic) "التنبيهات النشطة على هذه العقدة (${node.alertsSummary.size}):" else "ACTIVE ALERTS ON THIS NODE (${node.alertsSummary.size}):",
                        color = StatusWarningAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    node.alertsSummary.forEach { alertTitle ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusWarningAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = alertTitle,
                                color = colors.textPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contextual Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (node.isRogue) {
                    Button(
                        onClick = onIsolateRogue,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRogueCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "عزل الجهاز فوراً" else "Isolate Rogue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onTrustRogue,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "توثيق كجهاز آمن" else "Whitelist", fontSize = 11.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onPing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.NetworkPing, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "فحص Ping" else "Ping Probe", fontSize = 11.sp)
                    }

                    if (node.type == DeviceType.SERVER) {
                        Button(
                            onClick = onRemediate,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent, contentColor = if (isDark) CyberNavyDark else Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "معالجة SSH الذاتية" else "SSH Self-Heal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
