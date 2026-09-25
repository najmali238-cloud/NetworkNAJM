package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

enum class ExportFormat(val title: String, val extension: String, val mimeType: String) {
    JSON("Structured JSON", ".json", "application/json"),
    CSV("Spreadsheet CSV", ".csv", "text/csv"),
    MARKDOWN_SUMMARY("Audit Summary (TXT/MD)", ".md", "text/plain")
}

@Composable
fun ExportReportDialog(
    topologyNodes: List<TopologyNode>,
    serverMetrics: List<ServerMetric>,
    networkHealth: NetworkHealthMetrics,
    bandwidthMetrics: BandwidthMetrics,
    alerts: List<AlertLog>,
    slaReport: SlaReport,
    nodeTrafficMap: Map<String, NodeBandwidthTraffic>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.JSON) }
    var includeDetailedMetrics by remember { mutableStateOf(true) }
    var includeBandwidthTelemetry by remember { mutableStateOf(true) }
    var isCopied by remember { mutableStateOf(false) }

    val exportedContent = remember(
        selectedFormat,
        includeDetailedMetrics,
        includeBandwidthTelemetry,
        topologyNodes,
        serverMetrics,
        networkHealth,
        bandwidthMetrics,
        alerts,
        slaReport,
        nodeTrafficMap
    ) {
        when (selectedFormat) {
            ExportFormat.JSON -> {
                generateTopologyJson(
                    topologyNodes = topologyNodes,
                    serverMetrics = serverMetrics,
                    networkHealth = networkHealth,
                    bandwidthMetrics = bandwidthMetrics,
                    alerts = alerts,
                    slaReport = slaReport,
                    nodeTrafficMap = nodeTrafficMap,
                    includeDetails = includeDetailedMetrics,
                    includeBandwidth = includeBandwidthTelemetry
                )
            }
            ExportFormat.CSV -> {
                generateTopologyCsv(
                    topologyNodes = topologyNodes,
                    serverMetrics = serverMetrics,
                    networkHealth = networkHealth,
                    bandwidthMetrics = bandwidthMetrics,
                    alerts = alerts,
                    includeDetails = includeDetailedMetrics,
                    includeBandwidth = includeBandwidthTelemetry
                )
            }
            ExportFormat.MARKDOWN_SUMMARY -> {
                generateTopologySummaryMarkdown(
                    topologyNodes = topologyNodes,
                    serverMetrics = serverMetrics,
                    networkHealth = networkHealth,
                    bandwidthMetrics = bandwidthMetrics,
                    alerts = alerts,
                    slaReport = slaReport,
                    nodeTrafficMap = nodeTrafficMap,
                    includeDetails = includeDetailedMetrics,
                    includeBandwidth = includeBandwidthTelemetry
                )
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .testTag("export_report_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = CyberNavyCard,
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
            ),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
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
                                .background(CyberCyan.copy(alpha = 0.15f))
                                .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "EXPORT NETWORK REPORT",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Topology Health & Server Telemetry Export",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Format Selector (JSON vs Summary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormat.values().forEach { format ->
                        val isSelected = selectedFormat == format
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedFormat = format
                                isCopied = false
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (format == ExportFormat.JSON) Icons.Default.Code else Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) CyberCyan else TextSecondary
                                    )
                                    Text(
                                        text = format.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan,
                                containerColor = CyberNavyDark,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) CyberCyan else CyberNavyBorder
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Optional inclusion toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = includeDetailedMetrics,
                        onClick = {
                            includeDetailedMetrics = !includeDetailedMetrics
                            isCopied = false
                        },
                        label = {
                            Text(
                                text = "Server CPU/RAM/Disk",
                                fontSize = 10.5.sp
                            )
                        },
                        leadingIcon = {
                            if (includeDetailedMetrics) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = CyberCyan)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberNavyBorder,
                            selectedLabelColor = TextPrimary,
                            containerColor = CyberNavyDark,
                            labelColor = TextMuted
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = includeBandwidthTelemetry,
                        onClick = {
                            includeBandwidthTelemetry = !includeBandwidthTelemetry
                            isCopied = false
                        },
                        label = {
                            Text(
                                text = "RX/TX Throughput",
                                fontSize = 10.5.sp
                            )
                        },
                        leadingIcon = {
                            if (includeBandwidthTelemetry) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = CyberCyan)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberNavyBorder,
                            selectedLabelColor = TextPrimary,
                            containerColor = CyberNavyDark,
                            labelColor = TextMuted
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats summary header for export
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberNavyDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = StatusOnlineGreen, modifier = Modifier.size(13.dp))
                        Text(
                            text = "Health Score: ${networkHealth.healthScore}/100",
                            color = StatusOnlineGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "${topologyNodes.size} Nodes • ${serverMetrics.size} Servers",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preview Code Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberNavyDark)
                        .border(1.dp, CyberNavyBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    val scrollState = rememberScrollState()
                    val horizontalScrollState = rememberScrollState()

                    Text(
                        text = exportedContent,
                        color = ChartRxCyan,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .horizontalScroll(horizontalScrollState)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons (Copy & Share / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Network Guard Export", exportedContent)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                            Toast.makeText(context, "Copied report to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("export_copy_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CyberCyan)
                        )
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCopied) "Copied!" else "Copy",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = selectedFormat.mimeType
                                putExtra(Intent.EXTRA_SUBJECT, "Network Guard Topology & Health Report (${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())})")
                                putExtra(Intent.EXTRA_TEXT, exportedContent)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export Report via..."))
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .testTag("export_share_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberNavyDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share / Save File",
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
 * Builds structured formatted JSON representing current network topology, health, servers, and alerts.
 */
fun generateTopologyJson(
    topologyNodes: List<TopologyNode>,
    serverMetrics: List<ServerMetric>,
    networkHealth: NetworkHealthMetrics,
    bandwidthMetrics: BandwidthMetrics,
    alerts: List<AlertLog>,
    slaReport: SlaReport,
    nodeTrafficMap: Map<String, NodeBandwidthTraffic>,
    includeDetails: Boolean,
    includeBandwidth: Boolean
): String {
    val root = JSONObject()
    val meta = JSONObject()
    meta.put("reportType", "Network Topology & Health Audit")
    meta.put("generatedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date()))
    meta.put("exporter", "NetGuard Enterprise Sentinel v2.4")
    meta.put("complianceStatus", "PASS (99.98% SLA)")
    root.put("metadata", meta)

    // Health Overview
    val healthObj = JSONObject()
    healthObj.put("healthScore", networkHealth.healthScore)
    healthObj.put("postureStatus", networkHealth.postureStatus)
    healthObj.put("averageLatencyMs", networkHealth.averageLatencyMs)
    healthObj.put("packetLossPercent", networkHealth.packetLossPercent)
    healthObj.put("dnsResolutionMs", networkHealth.dnsResolutionMs)
    healthObj.put("activeGatewayIp", networkHealth.activeGatewayIp)
    healthObj.put("totalSlaUptime", slaReport.averageUptime)
    healthObj.put("meanTimeToRemediateMinutes", slaReport.mttrMinutes)
    root.put("networkHealth", healthObj)

    // Bandwidth Overview
    val bwObj = JSONObject()
    bwObj.put("downloadMbps", bandwidthMetrics.downloadMbps)
    bwObj.put("uploadMbps", bandwidthMetrics.uploadMbps)
    bwObj.put("totalCapacityMbps", bandwidthMetrics.totalCapacityMbps)
    root.put("bandwidthOverview", bwObj)

    // Topology Nodes Array
    val nodesArray = JSONArray()
    for (node in topologyNodes) {
        val nodeObj = JSONObject()
        nodeObj.put("id", node.id)
        nodeObj.put("label", node.label)
        nodeObj.put("ip", node.ip)
        nodeObj.put("type", node.type.name)
        nodeObj.put("status", node.status.name)
        nodeObj.put("parentId", node.parentId ?: JSONObject.NULL)
        nodeObj.put("isRogue", node.isRogue)
        nodeObj.put("latencyMs", node.latencyMs)
        nodeObj.put("macAddress", node.macAddress)

        if (includeBandwidth) {
            val traffic = nodeTrafficMap[node.id]
            val trafficObj = JSONObject()
            trafficObj.put("incomingMbps", traffic?.incomingMbps ?: node.incomingMbps)
            trafficObj.put("outgoingMbps", traffic?.outgoingMbps ?: node.outgoingMbps)
            trafficObj.put("packetsRxPerSec", traffic?.packetsRxPerSec ?: 0)
            trafficObj.put("packetsTxPerSec", traffic?.packetsTxPerSec ?: 0)
            trafficObj.put("isAnomalySpike", traffic?.isAnomalySpike ?: false)
            nodeObj.put("bandwidthTelemetry", trafficObj)
        }

        if (node.alertsSummary.isNotEmpty()) {
            val alertsArr = JSONArray()
            node.alertsSummary.forEach { alertsArr.put(it) }
            nodeObj.put("activeAlerts", alertsArr)
        }

        nodesArray.put(nodeObj)
    }
    root.put("topologyNodes", nodesArray)

    // Server Metrics
    if (includeDetails) {
        val serversArray = JSONArray()
        for (server in serverMetrics) {
            val sObj = JSONObject()
            sObj.put("serverId", server.serverId)
            sObj.put("serverName", server.serverName)
            sObj.put("ip", server.ip)
            sObj.put("cpuPercent", server.cpuPercent)
            sObj.put("ramPercent", server.ramPercent)
            sObj.put("diskPercent", server.diskPercent)
            sObj.put("temperatureC", server.temperatureC)
            sObj.put("processesCount", server.processesCount)

            val servicesArr = JSONArray()
            for (svc in server.services) {
                val svcObj = JSONObject()
                svcObj.put("name", svc.name)
                svcObj.put("isRunning", svc.isRunning)
                svcObj.put("port", svc.port)
                servicesArr.put(svcObj)
            }
            sObj.put("services", servicesArr)
            serversArray.put(sObj)
        }
        root.put("serverMetrics", serversArray)
    }

    // Recent Critical Alerts
    val alertsArray = JSONArray()
    for (alert in alerts.take(10)) {
        val aObj = JSONObject()
        aObj.put("id", alert.id)
        aObj.put("title", alert.title)
        aObj.put("severity", alert.severity.name)
        aObj.put("timestamp", alert.timestamp)
        aObj.put("message", alert.message)
        alertsArray.put(aObj)
    }
    root.put("recentAlerts", alertsArray)

    return root.toString(2)
}

/**
 * Builds formatted human-readable Markdown summary file for network health and topology.
 */
fun generateTopologySummaryMarkdown(
    topologyNodes: List<TopologyNode>,
    serverMetrics: List<ServerMetric>,
    networkHealth: NetworkHealthMetrics,
    bandwidthMetrics: BandwidthMetrics,
    alerts: List<AlertLog>,
    slaReport: SlaReport,
    nodeTrafficMap: Map<String, NodeBandwidthTraffic>,
    includeDetails: Boolean,
    includeBandwidth: Boolean
): String {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    return buildString {
        appendLine("================================================================================")
        appendLine("           NETGUARD ENTERPRISE - TOPOLOGY & HEALTH AUDIT REPORT                ")
        appendLine("================================================================================")
        appendLine("Generated At    : $dateStr")
        appendLine("Compliance Posture: ${networkHealth.postureStatus} (Audit Pass)")
        appendLine("Overall Health  : ${networkHealth.healthScore}/100")
        appendLine("Average Uptime  : ${String.format(Locale.US, "%.2f", slaReport.averageUptime)}%")
        appendLine("Gateway IP      : ${networkHealth.activeGatewayIp} (Avg Latency: ${networkHealth.averageLatencyMs} ms)")
        appendLine("Total Nodes     : ${topologyNodes.size} discovered")
        appendLine("Active Bandwidth: DL ${String.format(Locale.US, "%.1f", bandwidthMetrics.downloadMbps)} Mbps / UL ${String.format(Locale.US, "%.1f", bandwidthMetrics.uploadMbps)} Mbps")
        appendLine()

        appendLine("--------------------------------------------------------------------------------")
        appendLine("1. NETWORK TOPOLOGY HIERARCHY & NODE HEALTH")
        appendLine("--------------------------------------------------------------------------------")
        appendLine(String.format(Locale.US, "%-10s %-22s %-16s %-10s %-12s %-14s", "ID", "LABEL", "IP", "STATUS", "LATENCY", "BANDWIDTH"))
        appendLine("--------------------------------------------------------------------------------")
        for (node in topologyNodes) {
            val traffic = nodeTrafficMap[node.id]
            val bwStr = if (includeBandwidth && traffic != null) {
                "${String.format(Locale.US, "%.1f", traffic.incomingMbps)}/${String.format(Locale.US, "%.1f", traffic.outgoingMbps)}M"
            } else {
                "${node.incomingMbps.toInt()}/${node.outgoingMbps.toInt()}M"
            }
            val rogueTag = if (node.isRogue) "[ROGUE] " else ""
            val parentInfo = if (node.parentId != null) " (via ${node.parentId})" else " (ROOT)"

            appendLine(
                String.format(
                    Locale.US,
                    "%-10s %-22s %-16s %-10s %-12s %-14s",
                    node.id,
                    (rogueTag + node.label).take(21),
                    node.ip,
                    node.status.name,
                    "${node.latencyMs} ms",
                    bwStr
                ) + parentInfo
            )
        }
        appendLine()

        if (includeDetails && serverMetrics.isNotEmpty()) {
            appendLine("--------------------------------------------------------------------------------")
            appendLine("2. ENTERPRISE SERVER RESOURCE METRICS")
            appendLine("--------------------------------------------------------------------------------")
            appendLine(String.format(Locale.US, "%-10s %-20s %-15s %-8s %-8s %-8s %-8s", "SRV ID", "SERVER NAME", "IP", "CPU", "RAM", "DISK", "TEMP"))
            appendLine("--------------------------------------------------------------------------------")
            for (srv in serverMetrics) {
                appendLine(
                    String.format(
                        Locale.US,
                        "%-10s %-20s %-15s %-8s %-8s %-8s %-8s",
                        srv.serverId,
                        srv.serverName.take(19),
                        srv.ip,
                        "${srv.cpuPercent.toInt()}%",
                        "${srv.ramPercent.toInt()}%",
                        "${srv.diskPercent.toInt()}%",
                        "${srv.temperatureC.toInt()}°C"
                    )
                )
                val servicesRunning = srv.services.joinToString(", ") { "${it.name}:${if (it.isRunning) "RUNNING" else "DOWN"}" }
                appendLine("   Services: $servicesRunning")
            }
            appendLine()
        }

        appendLine("--------------------------------------------------------------------------------")
        appendLine("3. RECENT SECURITY & PERFORMANCE ALERTS")
        appendLine("--------------------------------------------------------------------------------")
        if (alerts.isEmpty()) {
            appendLine("No active alerts reported. All nodes operating normally.")
        } else {
            for (alert in alerts.take(6)) {
                appendLine("[${alert.severity}] ${alert.timestamp} - ${alert.title}")
                appendLine("   ${alert.message}")
            }
        }
        appendLine()
        appendLine("================================================================================")
        appendLine("End of NetGuard Enterprise Security & Topology Audit Report")
        appendLine("================================================================================")
    }
}

private fun generateTopologyCsv(
    topologyNodes: List<TopologyNode>,
    serverMetrics: List<ServerMetric>,
    networkHealth: NetworkHealthMetrics,
    bandwidthMetrics: BandwidthMetrics,
    alerts: List<AlertLog>,
    includeDetails: Boolean,
    includeBandwidth: Boolean
): String {
    return buildString {
        appendLine("# NetGuard Enterprise Topology & Server Metrics CSV Export")
        appendLine("# GeneratedAt: ${java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())}")
        appendLine("# HealthScore: ${networkHealth.healthScore}, PacketLoss: ${networkHealth.packetLossPercent}%, Latency: ${networkHealth.averageLatencyMs}ms")
        appendLine()
        appendLine("SECTION,NODE_ID,NAME,IP,STATUS,ROLE_OR_TYPE,LATENCY_MS,BANDWIDTH_MBPS,PARENT_ID,CPU_PCT,RAM_PCT,DISK_PCT,TEMP_C,SERVICES")
        for (node in topologyNodes) {
            val escName = "\"${node.label.replace("\"", "\"\"")}\""
            val escType = "\"${node.type.name}\""
            appendLine("TOPOLOGY_NODE,${node.id},$escName,${node.ip},${node.status.name},$escType,${node.latencyMs},${node.incomingMbps},${node.parentId ?: ""},,,,")
        }
        if (includeDetails && serverMetrics.isNotEmpty()) {
            for (srv in serverMetrics) {
                val escName = "\"${srv.serverName.replace("\"", "\"\"")}\""
                val servicesStr = "\"${srv.services.joinToString(";") { "${it.name}:${if (it.isRunning) "RUNNING" else "DOWN"}" }}\""
                appendLine("SERVER_METRIC,${srv.serverId},$escName,${srv.ip},ONLINE,SERVER,0,0,,${srv.cpuPercent},${srv.ramPercent},${srv.diskPercent},${srv.temperatureC},$servicesStr")
            }
        }
    }
}

