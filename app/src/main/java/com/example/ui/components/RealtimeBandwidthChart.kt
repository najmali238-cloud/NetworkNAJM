package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NodeBandwidthTraffic
import com.example.data.model.TopologyNode
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.roundToInt

enum class ChartVisualizationMode(val label: String) {
    AREA_SPLINE("Recharts Area"),
    DUAL_LINE("D3 Dual Line"),
    BAR_FLOW("Grouped Bars")
}

@Composable
fun RealtimeBandwidthChart(
    nodeTraffic: NodeBandwidthTraffic,
    allNodes: List<TopologyNode>,
    selectedNodeId: String,
    onSelectNode: (String) -> Unit,
    onSimulateBurst: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var chartMode by remember { mutableStateOf(ChartVisualizationMode.AREA_SPLINE) }
    var isStreamPaused by remember { mutableStateOf(false) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    // Pulsing animation for live status badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val liveDotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDot"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bandwidth_monitoring_chart"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (nodeTraffic.isAnomalySpike) StatusRogueCrimson.copy(alpha = 0.6f) else CyberNavyBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header & Live Indicator
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
                        Text(
                            text = "REAL-TIME BANDWIDTH MONITOR",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        // D3 / Recharts Live badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isStreamPaused) TextMuted.copy(alpha = 0.2f) else StatusOnlineGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isStreamPaused) TextMuted
                                        else StatusOnlineGreen.copy(alpha = liveDotAlpha)
                                    )
                            )
                            Text(
                                text = if (isStreamPaused) "PAUSED" else "LIVE D3/RECHARTS",
                                color = if (isStreamPaused) TextMuted else StatusOnlineGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "${nodeTraffic.nodeLabel} • ID: ${nodeTraffic.nodeId}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Stream control icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onSimulateBurst(nodeTraffic.nodeId) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Simulate Traffic Surge",
                            tint = StatusWarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isStreamPaused = !isStreamPaused },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isStreamPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isStreamPaused) "Resume Stream" else "Pause Stream",
                            tint = if (isStreamPaused) StatusOnlineGreen else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Anomaly Warning Banner (if traffic surge or rogue exfiltration detected)
            if (nodeTraffic.isAnomalySpike) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusRogueCrimson.copy(alpha = 0.15f))
                        .border(1.dp, StatusRogueCrimson.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusRogueCrimson,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "TRAFFIC SURGE DETECTED: Anomalous packet burst exceeding nominal baseline.",
                            color = StatusRogueCrimson,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. Dual Metrics KPI Cards (Incoming RX vs Outgoing TX)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Incoming RX Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(ChartRxCyan.copy(alpha = 0.4f))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ChartRxCyanDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = ChartRxCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "INCOMING (RX)",
                                color = ChartRxCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%.1f", nodeTraffic.incomingMbps),
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = " Mbps",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Text(
                                text = "Peak: ${String.format("%.1f", nodeTraffic.peakIncomingMbps)}M • ${nodeTraffic.packetsRxPerSec} p/s",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Outgoing TX Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(ChartTxPurple.copy(alpha = 0.4f))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ChartTxPurpleDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = ChartTxPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "OUTGOING (TX)",
                                color = ChartTxPurple,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%.1f", nodeTraffic.outgoingMbps),
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = " Mbps",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Text(
                                text = "Peak: ${String.format("%.1f", nodeTraffic.peakOutgoingMbps)}M • ${nodeTraffic.packetsTxPerSec} p/s",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // 4. Visualization Mode Toggle Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChartVisualizationMode.values().forEach { mode ->
                        val isSelected = chartMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { chartMode = mode },
                            label = { Text(mode.label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.15f),
                                selectedLabelColor = CyberCyan,
                                containerColor = CyberNavyDark,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) CyberCyan else CyberNavyBorder
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ChartRxCyan)
                        )
                        Text("RX In", color = ChartRxCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ChartTxPurple)
                        )
                        Text("TX Out", color = ChartTxPurple, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 5. Interactive D3 / Recharts Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberNavyDark)
                    .border(1.dp, CyberNavyBorder, RoundedCornerShape(10.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val width = size.width
                                val totalPoints = nodeTraffic.incomingHistory.size
                                if (totalPoints > 1) {
                                    val paddingLeft = 40.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = width - paddingLeft - paddingRight
                                    val stepX = chartWidth / (totalPoints - 1)
                                    val clampedX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                                    val index = (clampedX / stepX).roundToInt().coerceIn(0, totalPoints - 1)
                                    selectedPointIndex = if (selectedPointIndex == index) null else index
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val width = size.width
                                val totalPoints = nodeTraffic.incomingHistory.size
                                if (totalPoints > 1) {
                                    val paddingLeft = 40.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = width - paddingLeft - paddingRight
                                    val stepX = chartWidth / (totalPoints - 1)
                                    val clampedX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                                    val index = (clampedX / stepX).roundToInt().coerceIn(0, totalPoints - 1)
                                    selectedPointIndex = index
                                }
                            },
                            onDrag = { change, _ ->
                                val width = size.width
                                val totalPoints = nodeTraffic.incomingHistory.size
                                if (totalPoints > 1) {
                                    val paddingLeft = 40.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = width - paddingLeft - paddingRight
                                    val stepX = chartWidth / (totalPoints - 1)
                                    val clampedX = (change.position.x - paddingLeft).coerceIn(0f, chartWidth)
                                    val index = (clampedX / stepX).roundToInt().coerceIn(0, totalPoints - 1)
                                    selectedPointIndex = index
                                }
                            },
                            onDragEnd = {
                                // Keep point inspected
                            }
                        )
                    }
            ) {
                RechartsD3Canvas(
                    nodeTraffic = nodeTraffic,
                    chartMode = chartMode,
                    selectedPointIndex = selectedPointIndex,
                    modifier = Modifier.fillMaxSize()
                )

                // Interactive Recharts Tooltip Overlay
                selectedPointIndex?.let { index ->
                    val rxValue = nodeTraffic.incomingHistory.getOrElse(index) { 0f }
                    val txValue = nodeTraffic.outgoingHistory.getOrElse(index) { 0f }
                    val timeTag = nodeTraffic.timestamps.getOrElse(index) { "-${(15 - index) * 2}s" }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberNavySurface.copy(alpha = 0.95f))
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "TIME: $timeTag",
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                IconButton(
                                    onClick = { selectedPointIndex = null },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Tooltip",
                                        tint = TextMuted,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ChartRxCyan))
                                Text(
                                    text = "RX: ${String.format("%.1f", rxValue)} Mbps",
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
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ChartTxPurple))
                                Text(
                                    text = "TX: ${String.format("%.1f", txValue)} Mbps",
                                    color = ChartTxPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "Throughput: ${String.format("%.1f", rxValue + txValue)} Mbps",
                                color = CyberCyanLight,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // 6. Device Quick-Switch Selector Chips (Switch between topology nodes)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SELECT TOPOLOGY NODE TO MONITOR:",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allNodes.forEach { node ->
                        val isSelected = node.id == selectedNodeId
                        val isThreat = node.isRogue
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectNode(node.id) },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isThreat) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = StatusRogueCrimson,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Text(
                                        text = node.label.take(18) + (if (node.label.length > 18) "…" else ""),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isThreat) StatusRogueCrimson.copy(alpha = 0.25f) else CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = if (isThreat) StatusRogueCrimson else CyberCyan,
                                containerColor = CyberNavyDark,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) {
                                    if (isThreat) StatusRogueCrimson else CyberCyan
                                } else CyberNavyBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RechartsD3Canvas(
    nodeTraffic: NodeBandwidthTraffic,
    chartMode: ChartVisualizationMode,
    selectedPointIndex: Int?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 20.dp.toPx()
        val paddingBottom = 26.dp.toPx()

        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom

        val rxData = nodeTraffic.incomingHistory
        val txData = nodeTraffic.outgoingHistory
        val dataCount = rxData.size

        if (dataCount < 2) return@Canvas

        // Calculate dynamic max value for scale
        val maxObserved = max(rxData.maxOrNull() ?: 10f, txData.maxOrNull() ?: 10f)
        val maxCeil = when {
            maxObserved <= 10f -> 10f
            maxObserved <= 50f -> 50f
            maxObserved <= 100f -> 100f
            maxObserved <= 250f -> 250f
            maxObserved <= 500f -> 500f
            else -> ((maxObserved / 100f).toInt() + 1) * 100f
        }

        // Draw horizontal D3/Recharts Grid Lines (4 levels)
        val gridLines = 4
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

        for (i in 0..gridLines) {
            val ratio = i.toFloat() / gridLines
            val y = paddingTop + plotHeight * (1f - ratio)

            // Dotted guide line
            drawLine(
                color = CyberNavyBorder.copy(alpha = 0.8f),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )
        }

        val stepX = plotWidth / (dataCount - 1)

        fun getY(value: Float): Float {
            val clamped = value.coerceIn(0f, maxCeil)
            return paddingTop + plotHeight * (1f - (clamped / maxCeil))
        }

        when (chartMode) {
            ChartVisualizationMode.AREA_SPLINE -> {
                // Compute Monotone / Catmull-Rom smooth cubic Bezier paths
                val rxPoints = rxData.mapIndexed { index, v -> Offset(paddingLeft + index * stepX, getY(v)) }
                val txPoints = txData.mapIndexed { index, v -> Offset(paddingLeft + index * stepX, getY(v)) }
                val baselineY = paddingTop + plotHeight

                // 1. Draw Incoming (RX) Gradient Area
                val rxAreaPath = Path().apply {
                    moveTo(rxPoints.first().x, baselineY)
                    lineTo(rxPoints.first().x, rxPoints.first().y)
                    for (i in 0 until rxPoints.size - 1) {
                        val p0 = rxPoints[i]
                        val p1 = rxPoints[i + 1]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                    lineTo(rxPoints.last().x, baselineY)
                    close()
                }

                drawPath(
                    path = rxAreaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(ChartRxCyan.copy(alpha = 0.35f), ChartRxCyan.copy(alpha = 0.02f)),
                        startY = paddingTop,
                        endY = baselineY
                    )
                )

                // 2. Draw Outgoing (TX) Gradient Area
                val txAreaPath = Path().apply {
                    moveTo(txPoints.first().x, baselineY)
                    lineTo(txPoints.first().x, txPoints.first().y)
                    for (i in 0 until txPoints.size - 1) {
                        val p0 = txPoints[i]
                        val p1 = txPoints[i + 1]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                    lineTo(txPoints.last().x, baselineY)
                    close()
                }

                drawPath(
                    path = txAreaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(ChartTxPurple.copy(alpha = 0.30f), ChartTxPurple.copy(alpha = 0.02f)),
                        startY = paddingTop,
                        endY = baselineY
                    )
                )

                // 3. Draw Spline Lines
                val rxLinePath = Path().apply {
                    moveTo(rxPoints.first().x, rxPoints.first().y)
                    for (i in 0 until rxPoints.size - 1) {
                        val p0 = rxPoints[i]
                        val p1 = rxPoints[i + 1]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }
                drawPath(
                    path = rxLinePath,
                    color = ChartRxCyan,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                val txLinePath = Path().apply {
                    moveTo(txPoints.first().x, txPoints.first().y)
                    for (i in 0 until txPoints.size - 1) {
                        val p0 = txPoints[i]
                        val p1 = txPoints[i + 1]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }
                drawPath(
                    path = txLinePath,
                    color = ChartTxPurple,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw live trailing dots at current head
                val lastRx = rxPoints.last()
                drawCircle(color = ChartRxCyan.copy(alpha = 0.3f), radius = 6.dp.toPx(), center = lastRx)
                drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = lastRx)

                val lastTx = txPoints.last()
                drawCircle(color = ChartTxPurple.copy(alpha = 0.3f), radius = 6.dp.toPx(), center = lastTx)
                drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = lastTx)
            }

            ChartVisualizationMode.DUAL_LINE -> {
                val rxPoints = rxData.mapIndexed { index, v -> Offset(paddingLeft + index * stepX, getY(v)) }
                val txPoints = txData.mapIndexed { index, v -> Offset(paddingLeft + index * stepX, getY(v)) }

                val rxLinePath = Path().apply {
                    moveTo(rxPoints.first().x, rxPoints.first().y)
                    for (i in 0 until rxPoints.size - 1) {
                        val p0 = rxPoints[i]
                        val p1 = rxPoints[i + 1]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }
                drawPath(
                    path = rxLinePath,
                    color = ChartRxCyan,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                val txLinePath = Path().apply {
                    moveTo(txPoints.first().x, txPoints.first().y)
                    for (i in 0 until txPoints.size - 1) {
                        val p0 = txPoints[i]
                        val p1 = txPoints[i + 1]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }
                drawPath(
                    path = txLinePath,
                    color = ChartTxPurple,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Render circular data point markers
                rxPoints.forEach { pt ->
                    drawCircle(color = ChartRxCyan, radius = 2.dp.toPx(), center = pt)
                }
                txPoints.forEach { pt ->
                    drawCircle(color = ChartTxPurple, radius = 2.dp.toPx(), center = pt)
                }
            }

            ChartVisualizationMode.BAR_FLOW -> {
                val barSlotWidth = plotWidth / dataCount
                val barWidth = barSlotWidth * 0.38f
                val baselineY = paddingTop + plotHeight

                for (i in 0 until dataCount) {
                    val slotCenterX = paddingLeft + (i + 0.5f) * barSlotWidth
                    val rxY = getY(rxData[i])
                    val txY = getY(txData[i])

                    val rxHeight = (baselineY - rxY).coerceAtLeast(2f)
                    val txHeight = (baselineY - txY).coerceAtLeast(2f)

                    // Incoming RX bar (left column in slot)
                    drawRoundRect(
                        color = ChartRxCyan,
                        topLeft = Offset(slotCenterX - barWidth - 1.dp.toPx(), rxY),
                        size = Size(barWidth, rxHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Outgoing TX bar (right column in slot)
                    drawRoundRect(
                        color = ChartTxPurple,
                        topLeft = Offset(slotCenterX + 1.dp.toPx(), txY),
                        size = Size(barWidth, txHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }

        // Selected Crosshair Scrubber Cursor (Recharts tooltip guide line)
        selectedPointIndex?.let { idx ->
            if (idx in 0 until dataCount) {
                val crossX = paddingLeft + idx * stepX
                val rxY = getY(rxData[idx])
                val txY = getY(txData[idx])

                // Vertical cursor line
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(crossX, paddingTop),
                    end = Offset(crossX, paddingTop + plotHeight),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )

                // Highlight dots
                drawCircle(color = ChartRxCyan, radius = 5.dp.toPx(), center = Offset(crossX, rxY))
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(crossX, rxY))

                drawCircle(color = ChartTxPurple, radius = 5.dp.toPx(), center = Offset(crossX, txY))
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(crossX, txY))
            }
        }
    }
}
