package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.DeviceStatus
import com.example.data.model.DeviceType
import com.example.data.model.TopologyNode
import com.example.ui.theme.*
import kotlin.math.hypot

enum class TopologyFilterMode(val label: String, val labelArabic: String) {
    ALL("All Assets", "كافة الأصول"),
    ALERTS_ONLY("Active Alerts", "التنبيهات النشطة"),
    ROGUE_ONLY("Rogue Threats", "التهديدات الدخيلة"),
    INFRASTRUCTURE("Core Infra", "البنية التحتية"),
    ENDPOINTS("Endpoints", "الأجهزة الطرفية")
}

/**
 * Interactive Network Topology Visualization Component using Canvas in Jetpack Compose.
 * Features:
 * - Dynamic touch node dragging to interactively rearrange devices
 * - Multi-touch pan & pinch-to-zoom
 * - Rich connection links with capacity badges (10G Fiber, 10G Trunk, 1G Eth, PoE, Rogue)
 * - Real-time animated data packet flow
 * - Distinctive custom vector glyphs for routers, switches, servers, workstations, cameras, printers, and rogue threats
 * - Full Arabic (العربية) and English support
 * - Dark & Light mode theme adaptation
 */
@Composable
fun NetworkTopologyViewer(
    nodes: List<TopologyNode>,
    selectedNodeId: String?,
    onNodeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    filterMode: TopologyFilterMode = TopologyFilterMode.ALL,
    showDataFlow: Boolean = true,
    searchQuery: String = ""
) {
    val isDark = LocalThemeIsDark.current
    val appLanguage = LocalAppLanguage.current
    val isArabic = appLanguage == AppLanguage.ARABIC
    val colors = getNetGuardColors(isDark)

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Interactive per-node drag offsets (relative to initial canvas layout)
    val nodeDragOffsets = remember { mutableStateMapOf<String, Offset>() }
    var activeDraggedNodeId by remember { mutableStateOf<String?>(null) }

    // Toggle for link bandwidth/speed labels
    var showBandwidthLabels by remember { mutableStateOf(true) }
    // Toggle for legend overlay
    var showLegend by remember { mutableStateOf(true) }

    // Continuous Animations
    val infiniteTransition = rememberInfiniteTransition(label = "topology_anim")

    // Data packet flow along connections
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_phase"
    )

    // Alert beacon pulse animation
    val alertPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alert_pulse"
    )

    // Rogue threat expanding radar wave
    val roguePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rogue_pulse"
    )

    // Dashed line pulse phase for links
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash_phase"
    )

    // Search query matching target pulse animation
    val searchPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "search_pulse"
    )

    val cleanQuery = searchQuery.trim()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) CyberNavyDark else LightCanvasBackground)
            .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(16.dp))
            .testTag("network_topology_canvas_box")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Interactive Gesture Handler: single-finger node drag vs canvas pan, multi-touch zoom
                .pointerInput(nodes, scale, offset) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()
                        val hitRadius = 36.dp.toPx() * scale.coerceAtLeast(0.75f)

                        // Check if down position is near a node
                        val hitNode = nodes.find { node ->
                            val drag = nodeDragOffsets[node.id] ?: Offset.Zero
                            val nx = (node.x * canvasW + drag.x) * scale + offset.x
                            val ny = (node.y * canvasH + drag.y) * scale + offset.y
                            hypot((down.position.x - nx).toDouble(), (down.position.y - ny).toDouble()) <= hitRadius
                        }

                        var totalDrag = 0f
                        val draggedNode = hitNode
                        if (draggedNode != null) {
                            activeDraggedNodeId = draggedNode.id
                        }

                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val changes = event.changes
                            if (changes.isEmpty() || changes.all { !it.pressed }) {
                                break
                            }

                            if (changes.size == 1) {
                                val change = changes[0]
                                val dragDelta = change.position - change.previousPosition
                                totalDrag += dragDelta.getDistance()

                                if (draggedNode != null) {
                                    change.consume()
                                    val currentDrag = nodeDragOffsets[draggedNode.id] ?: Offset.Zero
                                    nodeDragOffsets[draggedNode.id] = currentDrag + (dragDelta / scale)
                                } else {
                                    change.consume()
                                    offset += dragDelta
                                }
                            } else if (changes.size >= 2) {
                                val p1 = changes[0]
                                val p2 = changes[1]
                                val prevDist = (p1.previousPosition - p2.previousPosition).getDistance()
                                val currDist = (p1.position - p2.position).getDistance()
                                if (prevDist > 0f) {
                                    val zoomFactor = currDist / prevDist
                                    scale = (scale * zoomFactor).coerceIn(0.55f, 2.5f)
                                }
                                val panDelta = ((p1.position - p1.previousPosition) + (p2.position - p2.previousPosition)) / 2f
                                offset += panDelta
                                p1.consume()
                                p2.consume()
                            }
                        }

                        activeDraggedNodeId = null
                        // If it was a quick tap on a node without significant drag, select it
                        if (draggedNode != null && totalDrag < 8.dp.toPx()) {
                            onNodeSelected(draggedNode.id)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = 1f
                            offset = Offset.Zero
                            nodeDragOffsets.clear()
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val nodeMap = nodes.associateBy { it.id }

            // Helper to get screen position for any node (with drag offsets + zoom + pan)
            fun getNodeScreenPos(node: TopologyNode): Offset {
                val drag = nodeDragOffsets[node.id] ?: Offset.Zero
                val cx = (node.x * width + drag.x) * scale + offset.x
                val cy = (node.y * height + drag.y) * scale + offset.y
                return Offset(cx, cy)
            }

            // 1. Grid Background
            val gridStep = 44.dp.toPx() * scale
            val startX = (offset.x % gridStep + gridStep) % gridStep
            val startY = (offset.y % gridStep + gridStep) % gridStep
            val gridDotColor = if (isDark) Color(0xFF14243F).copy(alpha = 0.8f) else Color(0xFFCBD5E1).copy(alpha = 0.7f)
            var gx = startX
            while (gx < width) {
                var gy = startY
                while (gy < height) {
                    drawCircle(
                        color = gridDotColor,
                        radius = 1.2.dp.toPx(),
                        center = Offset(gx, gy)
                    )
                    gy += gridStep
                }
                gx += gridStep
            }

            // Filter logic
            fun isNodeSearchMatch(node: TopologyNode): Boolean {
                if (cleanQuery.isEmpty()) return false
                return node.label.contains(cleanQuery, ignoreCase = true) ||
                        node.ip.contains(cleanQuery, ignoreCase = true) ||
                        node.id.contains(cleanQuery, ignoreCase = true)
            }

            fun isNodeDimmed(node: TopologyNode): Boolean {
                val matchesFilter = when (filterMode) {
                    TopologyFilterMode.ALL -> true
                    TopologyFilterMode.ALERTS_ONLY -> node.activeAlertCount > 0
                    TopologyFilterMode.ROGUE_ONLY -> node.isRogue
                    TopologyFilterMode.INFRASTRUCTURE -> node.type == DeviceType.ROUTER ||
                            node.type == DeviceType.SWITCH ||
                            node.type == DeviceType.SERVER
                    TopologyFilterMode.ENDPOINTS -> node.type != DeviceType.ROUTER &&
                            node.type != DeviceType.SWITCH &&
                            node.type != DeviceType.SERVER
                }
                if (!matchesFilter) return true
                if (cleanQuery.isNotEmpty()) {
                    return !isNodeSearchMatch(node)
                }
                return false
            }

            // Paints for text labels and badges
            val textPaintPrimary = Paint().apply {
                color = if (isDark) TextPrimary.toArgb() else Color(0xFF0F172A).toArgb()
                textSize = (10.sp.toPx() * scale).coerceIn(8.dp.toPx(), 16.dp.toPx())
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textPaintSecondary = Paint().apply {
                color = if (isDark) TextSecondary.toArgb() else Color(0xFF475569).toArgb()
                textSize = (8.5.sp.toPx() * scale).coerceIn(7.dp.toPx(), 13.dp.toPx())
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                isAntiAlias = true
            }

            val textPaintRogue = Paint().apply {
                color = StatusRogueCrimson.toArgb()
                textSize = (10.sp.toPx() * scale).coerceIn(8.dp.toPx(), 16.dp.toPx())
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textPaintSearchMatch = Paint().apply {
                color = CyberCyan.toArgb()
                textSize = (11.sp.toPx() * scale).coerceIn(9.dp.toPx(), 17.dp.toPx())
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val linkLabelPaint = Paint().apply {
                color = if (isDark) Color(0xFF94A3B8).toArgb() else Color(0xFF334155).toArgb()
                textSize = (7.5.sp.toPx() * scale).coerceIn(6.dp.toPx(), 12.dp.toPx())
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            // 2. DRAW CONNECTIONS BETWEEN NODES (Links, Cables, Bandwidth Badges, and Packet Flow)
            nodes.forEach { node ->
                if (node.parentId != null) {
                    val parent = nodeMap[node.parentId]
                    if (parent != null) {
                        val startPos = getNodeScreenPos(parent)
                        val endPos = getNodeScreenPos(node)

                        val isDimmed = isNodeDimmed(node) && isNodeDimmed(parent)
                        val alphaMultiplier = if (isDimmed) 0.15f else 1.0f

                        val isConnectedToSelected = selectedNodeId != null &&
                                (selectedNodeId == node.id || selectedNodeId == parent.id)

                        val isFiberBackbone = (parent.type == DeviceType.ROUTER && node.type == DeviceType.SWITCH) ||
                                (parent.type == DeviceType.SWITCH && node.type == DeviceType.SWITCH)
                        val isServerTrunk = node.type == DeviceType.SERVER

                        // Color selection
                        val baseLineColor = when {
                            node.isRogue -> StatusRogueCrimson
                            node.status == DeviceStatus.OFFLINE -> StatusOfflineRed
                            node.activeAlertCount > 0 -> StatusWarningAmber
                            isConnectedToSelected -> if (isDark) CyberCyanLight else LightAccentCyan
                            isFiberBackbone -> if (isDark) CyberCyan else Color(0xFF0284C7)
                            isServerTrunk -> if (isDark) CyberBlueLight else Color(0xFF2563EB)
                            else -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0D9488)
                        }

                        val strokeWidth = when {
                            isConnectedToSelected -> 4.2f * scale
                            isFiberBackbone -> 3.2f * scale
                            node.isRogue -> 2.8f * scale
                            else -> 2.0f * scale
                        }

                        val isDashed = node.isRogue || node.status == DeviceStatus.OFFLINE

                        // Outer Glow for Selected or Fiber connections
                        if ((isConnectedToSelected || isFiberBackbone) && !isDimmed) {
                            drawLine(
                                color = baseLineColor.copy(alpha = if (isConnectedToSelected) 0.45f else 0.20f),
                                start = startPos,
                                end = endPos,
                                strokeWidth = strokeWidth + (4.dp.toPx() * scale)
                            )
                        }

                        // Core Connection Line
                        drawLine(
                            color = baseLineColor.copy(alpha = (if (isConnectedToSelected) 0.95f else 0.65f) * alphaMultiplier),
                            start = startPos,
                            end = endPos,
                            strokeWidth = strokeWidth,
                            pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(12f * scale, 8f * scale), dashPhase) else null
                        )

                        val midX = (startPos.x + endPos.x) / 2f
                        val midY = (startPos.y + endPos.y) / 2f

                        // A. Link Bandwidth & Capacity Tag Badge
                        if (showBandwidthLabels && !isDimmed) {
                            val bandwidthText = when {
                                node.isRogue -> if (isArabic) "⚠️ مشبوه" else "⚠️ Rogue"
                                node.status == DeviceStatus.OFFLINE -> if (isArabic) "منقطع" else "Broken"
                                isFiberBackbone -> if (isArabic) "10G ليف بصري" else "10G Fiber"
                                isServerTrunk -> if (isArabic) "10G خادم" else "10G Trunk"
                                node.type == DeviceType.WORKSTATION -> if (isArabic) "1G إيثرنت" else "1G Eth"
                                node.type == DeviceType.CAMERA || node.type == DeviceType.PRINTER -> if (isArabic) "100M مراقبة" else "100M IoT"
                                else -> if (isArabic) "1G رابط" else "1G Link"
                            }

                            val badgeW = (42.dp.toPx() * scale).coerceIn(28.dp.toPx(), 65.dp.toPx())
                            val badgeH = (14.dp.toPx() * scale).coerceIn(10.dp.toPx(), 20.dp.toPx())

                            // Badge background pill
                            drawRoundRect(
                                color = if (isDark) CyberNavyDark.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.92f),
                                topLeft = Offset(midX - badgeW / 2f, midY - badgeH / 2f),
                                size = Size(badgeW, badgeH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(badgeH / 2f, badgeH / 2f)
                            )
                            drawRoundRect(
                                color = baseLineColor.copy(alpha = 0.7f),
                                topLeft = Offset(midX - badgeW / 2f, midY - badgeH / 2f),
                                size = Size(badgeW, badgeH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(badgeH / 2f, badgeH / 2f),
                                style = Stroke(width = 1.0f * scale)
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                bandwidthText,
                                midX,
                                midY + (badgeH * 0.28f),
                                linkLabelPaint
                            )
                        }

                        // B. Animated Data Packet Particles
                        if (showDataFlow && !isDimmed && node.status != DeviceStatus.OFFLINE) {
                            val particleT1 = (flowPhase + ((node.id.hashCode() and 0x7FFFFFFF) % 100) / 100f) % 1.0f
                            val pos1 = Offset(
                                startPos.x + (endPos.x - startPos.x) * particleT1,
                                startPos.y + (endPos.y - startPos.y) * particleT1
                            )

                            val particleColor = when {
                                node.isRogue -> StatusRogueCrimson
                                node.activeAlertCount > 0 -> StatusWarningAmber
                                isFiberBackbone -> CyberCyanLight
                                else -> StatusOnlineGreen
                            }

                            // Glowing particle halo
                            drawCircle(
                                color = particleColor.copy(alpha = 0.40f),
                                radius = 6.dp.toPx() * scale,
                                center = pos1
                            )
                            // Bright center particle
                            drawCircle(
                                color = particleColor,
                                radius = 2.6.dp.toPx() * scale,
                                center = pos1
                            )

                            // Second packet on high-speed fiber backbone
                            if (isFiberBackbone || isServerTrunk) {
                                val particleT2 = (particleT1 + 0.5f) % 1.0f
                                val pos2 = Offset(
                                    startPos.x + (endPos.x - startPos.x) * particleT2,
                                    startPos.y + (endPos.y - startPos.y) * particleT2
                                )
                                drawCircle(
                                    color = particleColor.copy(alpha = 0.35f),
                                    radius = 5.dp.toPx() * scale,
                                    center = pos2
                                )
                                drawCircle(
                                    color = particleColor,
                                    radius = 2.2.dp.toPx() * scale,
                                    center = pos2
                                )
                            }
                        }
                    }
                }
            }

            // 3. DRAW DEVICE NODES & CUSTOM CANVASES
            nodes.forEach { node ->
                val pos = getNodeScreenPos(node)
                val cx = pos.x
                val cy = pos.y
                val isSelected = node.id == selectedNodeId
                val isDimmed = isNodeDimmed(node)
                val isDragged = node.id == activeDraggedNodeId
                val alpha = if (isDimmed) 0.20f else 1.0f

                val baseRadius = when (node.type) {
                    DeviceType.ROUTER -> 24.dp.toPx() * scale
                    DeviceType.SWITCH -> 22.dp.toPx() * scale
                    DeviceType.SERVER -> 20.dp.toPx() * scale
                    DeviceType.ROGUE -> 21.dp.toPx() * scale
                    else -> 17.dp.toPx() * scale
                }

                val nodeThemeColor = when {
                    node.isRogue -> StatusRogueCrimson
                    node.status == DeviceStatus.OFFLINE -> StatusOfflineRed
                    node.status == DeviceStatus.WARNING -> StatusWarningAmber
                    node.type == DeviceType.ROUTER -> if (isDark) CyberCyan else Color(0xFF0284C7)
                    node.type == DeviceType.SWITCH -> if (isDark) CyberBlueLight else Color(0xFF2563EB)
                    node.type == DeviceType.SERVER -> if (isDark) CyberCyanLight else Color(0xFF0D9488)
                    else -> StatusOnlineGreen
                }

                // A. Lift Elevation Glow when user is actively dragging the node
                if (isDragged) {
                    drawCircle(
                        color = nodeThemeColor.copy(alpha = 0.35f),
                        radius = baseRadius + (18.dp.toPx() * scale),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = nodeThemeColor.copy(alpha = 0.6f),
                        radius = baseRadius + (10.dp.toPx() * scale),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5f * scale)
                    )
                }

                // B. Active Alert Beacon (Pulsing Amber Wave)
                if (node.activeAlertCount > 0 && !isDimmed) {
                    val pulseRadius = baseRadius + (20.dp.toPx() * alertPulse * scale)
                    drawCircle(
                        color = StatusWarningAmber.copy(alpha = (1f - alertPulse) * 0.65f),
                        radius = pulseRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5f * scale)
                    )
                }

                // C. Rogue Threat Radar (Concentric Expanding Radar Waves)
                if (node.isRogue && !isDimmed) {
                    val rPulse1 = baseRadius + (26.dp.toPx() * roguePulse * scale)
                    drawCircle(
                        color = StatusRogueCrimson.copy(alpha = (1f - roguePulse) * 0.75f),
                        radius = rPulse1,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3.0f * scale)
                    )

                    val roguePulse2 = (roguePulse + 0.5f) % 1.0f
                    val rPulse2 = baseRadius + (26.dp.toPx() * roguePulse2 * scale)
                    drawCircle(
                        color = StatusRogueCrimson.copy(alpha = (1f - roguePulse2) * 0.45f),
                        radius = rPulse2,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.0f * scale)
                    )
                }

                // D. Selection Halo & Targeting Crosshair
                if (isSelected && !isDimmed) {
                    drawCircle(
                        color = nodeThemeColor.copy(alpha = 0.28f),
                        radius = baseRadius + (12.dp.toPx() * scale),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = if (isDark) CyberCyan else Color(0xFF0284C7),
                        radius = baseRadius + (6.dp.toPx() * scale),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.4f * scale)
                    )
                    // Crosshair corner reticles
                    val chLen = 6.dp.toPx() * scale
                    val chDist = baseRadius + (10.dp.toPx() * scale)
                    drawLine(color = nodeThemeColor, start = Offset(cx - chDist, cy), end = Offset(cx - chDist + chLen, cy), strokeWidth = 2f * scale)
                    drawLine(color = nodeThemeColor, start = Offset(cx + chDist, cy), end = Offset(cx + chDist - chLen, cy), strokeWidth = 2f * scale)
                    drawLine(color = nodeThemeColor, start = Offset(cx, cy - chDist), end = Offset(cx, cy - chDist + chLen), strokeWidth = 2f * scale)
                    drawLine(color = nodeThemeColor, start = Offset(cx, cy + chDist), end = Offset(cx, cy + chDist - chLen), strokeWidth = 2f * scale)
                }

                // E. Search Matching Target Ring
                val isSearchMatch = isNodeSearchMatch(node)
                if (isSearchMatch && !isDimmed) {
                    val sPulseRadius = baseRadius + (18.dp.toPx() * searchPulse * scale)
                    drawCircle(
                        color = CyberCyan.copy(alpha = (1f - searchPulse) * 0.8f),
                        radius = sPulseRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.8f * scale)
                    )
                }

                // F. Node Background Fill (Theme Adaptive)
                val nodeFillColor = if (isDark) {
                    CyberNavyDark.copy(alpha = alpha)
                } else {
                    Color.White.copy(alpha = alpha)
                }
                drawCircle(
                    color = nodeFillColor,
                    radius = baseRadius,
                    center = Offset(cx, cy)
                )

                // G. Node Border Ring
                drawCircle(
                    color = nodeThemeColor.copy(alpha = alpha),
                    radius = baseRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = if (isSelected || isDragged) 3.6f * scale else 2.2f * scale)
                )

                // H. Distinctive Canvas Vector Glyphs for each Device Type
                val glyphColor = nodeThemeColor.copy(alpha = alpha)
                when (node.type) {
                    DeviceType.ROUTER -> {
                        // Core Gateway: Diamond with radiating broadcast radio wave arcs
                        val dSize = 7.dp.toPx() * scale
                        drawCircle(color = glyphColor, radius = 3.dp.toPx() * scale, center = Offset(cx, cy))
                        drawArc(
                            color = glyphColor,
                            startAngle = 210f,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = Offset(cx - dSize, cy - dSize),
                            size = Size(dSize * 2, dSize * 2),
                            style = Stroke(width = 1.8f * scale)
                        )
                        drawArc(
                            color = glyphColor,
                            startAngle = 30f,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = Offset(cx - dSize, cy - dSize),
                            size = Size(dSize * 2, dSize * 2),
                            style = Stroke(width = 1.8f * scale)
                        )
                    }

                    DeviceType.SWITCH -> {
                        // Enterprise Switch: Rack bar with flashing green port LED dots
                        val sw = 9.dp.toPx() * scale
                        val sh = 5.dp.toPx() * scale
                        drawRect(
                            color = glyphColor.copy(alpha = 0.25f),
                            topLeft = Offset(cx - sw, cy - sh),
                            size = Size(sw * 2, sh * 2)
                        )
                        drawRect(
                            color = glyphColor,
                            topLeft = Offset(cx - sw, cy - sh),
                            size = Size(sw * 2, sh * 2),
                            style = Stroke(width = 1.5f * scale)
                        )
                        // Port LED dots
                        val portDotR = 1.2.dp.toPx() * scale
                        drawCircle(color = StatusOnlineGreen, radius = portDotR, center = Offset(cx - 5.dp.toPx() * scale, cy))
                        drawCircle(color = StatusOnlineGreen, radius = portDotR, center = Offset(cx - 1.5.dp.toPx() * scale, cy))
                        drawCircle(color = StatusOnlineGreen, radius = portDotR, center = Offset(cx + 2.dp.toPx() * scale, cy))
                        drawCircle(color = StatusOnlineGreen, radius = portDotR, center = Offset(cx + 5.5.dp.toPx() * scale, cy))
                    }

                    DeviceType.SERVER -> {
                        // 3-Tier Enterprise Blade Server
                        val sW = 8.dp.toPx() * scale
                        val sH = 2.5.dp.toPx() * scale
                        val yOffsets = listOf(-4.5.dp.toPx() * scale, 0f, 4.5.dp.toPx() * scale)
                        yOffsets.forEach { yo ->
                            drawRect(
                                color = glyphColor,
                                topLeft = Offset(cx - sW, cy + yo - sH / 2f),
                                size = Size(sW * 2, sH)
                            )
                            drawCircle(
                                color = if (isDark) CyberNavyDark else Color.White,
                                radius = 1.0.dp.toPx() * scale,
                                center = Offset(cx + sW - 2.5.dp.toPx() * scale, cy + yo)
                            )
                        }
                    }

                    DeviceType.WORKSTATION -> {
                        // Desktop PC Monitor Screen with Stand
                        val mW = 8.dp.toPx() * scale
                        val mH = 5.5.dp.toPx() * scale
                        drawRect(
                            color = glyphColor,
                            topLeft = Offset(cx - mW, cy - 5.dp.toPx() * scale),
                            size = Size(mW * 2, mH),
                            style = Stroke(width = 1.6f * scale)
                        )
                        // Stand neck and base
                        drawLine(
                            color = glyphColor,
                            start = Offset(cx, cy + 0.5.dp.toPx() * scale),
                            end = Offset(cx, cy + 3.5.dp.toPx() * scale),
                            strokeWidth = 1.8f * scale
                        )
                        drawLine(
                            color = glyphColor,
                            start = Offset(cx - 4.dp.toPx() * scale, cy + 3.5.dp.toPx() * scale),
                            end = Offset(cx + 4.dp.toPx() * scale, cy + 3.5.dp.toPx() * scale),
                            strokeWidth = 1.8f * scale
                        )
                    }

                    DeviceType.CAMERA -> {
                        // Security Camera dome & lens aperture
                        val cR = 5.dp.toPx() * scale
                        drawCircle(color = glyphColor, radius = cR, center = Offset(cx, cy), style = Stroke(width = 1.6f * scale))
                        drawCircle(color = glyphColor, radius = 2.dp.toPx() * scale, center = Offset(cx, cy))
                    }

                    DeviceType.PRINTER -> {
                        // Laser printer chassis with paper output
                        val pW = 7.dp.toPx() * scale
                        val pH = 5.dp.toPx() * scale
                        drawRect(color = glyphColor, topLeft = Offset(cx - pW, cy - 2.dp.toPx() * scale), size = Size(pW * 2, pH), style = Stroke(width = 1.5f * scale))
                        drawLine(color = glyphColor, start = Offset(cx - 4.dp.toPx() * scale, cy - 5.dp.toPx() * scale), end = Offset(cx + 4.dp.toPx() * scale, cy - 5.dp.toPx() * scale), strokeWidth = 1.5f * scale)
                    }

                    DeviceType.ROGUE -> {
                        // Rogue threat: Hexagonal hazard symbol with exclamation mark
                        val rx = 6.5.dp.toPx() * scale
                        val roguePath = Path().apply {
                            moveTo(cx, cy - rx)
                            lineTo(cx + rx, cy - rx * 0.5f)
                            lineTo(cx + rx, cy + rx * 0.5f)
                            lineTo(cx, cy + rx)
                            lineTo(cx - rx, cy + rx * 0.5f)
                            lineTo(cx - rx, cy - rx * 0.5f)
                            close()
                        }
                        drawPath(roguePath, color = StatusRogueCrimson, style = Stroke(width = 2.0f * scale))
                        // Center exclamation mark
                        drawLine(
                            color = StatusRogueCrimson,
                            start = Offset(cx, cy - 3.5.dp.toPx() * scale),
                            end = Offset(cx, cy + 1.dp.toPx() * scale),
                            strokeWidth = 2.0f * scale
                        )
                        drawCircle(color = StatusRogueCrimson, radius = 1.0.dp.toPx() * scale, center = Offset(cx, cy + 3.dp.toPx() * scale))
                    }
                }

                // I. Top-Right Alert Badge Pin
                if (node.activeAlertCount > 0 && !isDimmed) {
                    val badgeRadius = 7.dp.toPx() * scale
                    val badgeCenter = Offset(cx + baseRadius * 0.74f, cy - baseRadius * 0.74f)
                    drawCircle(color = StatusWarningAmber, radius = badgeRadius, center = badgeCenter)
                    drawCircle(
                        color = if (isDark) CyberNavyDark else Color.White,
                        radius = badgeRadius,
                        center = badgeCenter,
                        style = Stroke(width = 1.2f * scale)
                    )

                    val badgePaint = Paint().apply {
                        color = Color(0xFF0F172A).toArgb()
                        textSize = (8.sp.toPx() * scale).coerceIn(6.dp.toPx(), 11.dp.toPx())
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "${node.activeAlertCount}",
                        badgeCenter.x,
                        badgeCenter.y + (3.dp.toPx() * scale),
                        badgePaint
                    )
                }

                // J. Node Primary Label & IP Address Subtext
                if (!isDimmed) {
                    val labelY = cy + baseRadius + (12.dp.toPx() * scale)
                    val ipY = labelY + (10.dp.toPx() * scale)

                    val labelPaint = when {
                        node.isRogue -> textPaintRogue
                        isSearchMatch -> textPaintSearchMatch
                        else -> textPaintPrimary
                    }
                    drawContext.canvas.nativeCanvas.drawText(node.label, cx, labelY, labelPaint)
                    drawContext.canvas.nativeCanvas.drawText(node.ip, cx, ipY, textPaintSecondary)
                }
            }
        }

        // OVERLAY 1: Interactive Canvas Control Bar (Zoom, Recenter, Reset Layout, Data Flow, Bandwidth)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Indicator Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background((if (isDark) CyberNavyCard else Color.White).copy(alpha = 0.92f))
                    .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isArabic) "مصفوفة الربط 2D" else "2D Topology Mesh",
                        color = colors.primaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Zoom In
            IconButton(
                onClick = { scale = (scale * 1.25f).coerceIn(0.55f, 2.5f) },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background((if (isDark) CyberNavyCard else Color.White).copy(alpha = 0.92f))
                    .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = if (isArabic) "تكبير" else "Zoom In",
                    tint = colors.primaryAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Zoom Out
            IconButton(
                onClick = { scale = (scale / 1.25f).coerceIn(0.55f, 2.5f) },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background((if (isDark) CyberNavyCard else Color.White).copy(alpha = 0.92f))
                    .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = if (isArabic) "تصغير" else "Zoom Out",
                    tint = colors.primaryAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Reset View & Clear Drag Overrides
            IconButton(
                onClick = {
                    scale = 1f
                    offset = Offset.Zero
                    nodeDragOffsets.clear()
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background((if (isDark) CyberNavyCard else Color.White).copy(alpha = 0.92f))
                    .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.FilterCenterFocus,
                    contentDescription = if (isArabic) "إعادة ضبط العرض" else "Reset View",
                    tint = colors.primaryAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Toggle Bandwidth Labels
            IconButton(
                onClick = { showBandwidthLabels = !showBandwidthLabels },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background((if (isDark) CyberNavyCard else Color.White).copy(alpha = 0.92f))
                    .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = if (showBandwidthLabels) Icons.Default.Speed else Icons.Default.LinearScale,
                    contentDescription = if (isArabic) "تبديل سرعات الروابط" else "Toggle Bandwidth Labels",
                    tint = if (showBandwidthLabels) StatusOnlineGreen else colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Toggle Legend Overlay
            IconButton(
                onClick = { showLegend = !showLegend },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background((if (isDark) CyberNavyCard else Color.White).copy(alpha = 0.92f))
                    .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = if (isArabic) "تبديل مفتاح الخريطة" else "Toggle Legend",
                    tint = colors.primaryAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // OVERLAY 2: Interactive Legend / Key
        AnimatedVisibility(
            visible = showLegend,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background((if (isDark) CyberNavyCard else Color.White).copy(alpha = 0.94f))
                    .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TopologyLegendRow(
                    color = if (isDark) CyberCyan else Color(0xFF0284C7),
                    label = if (isArabic) "بوابة رئيسية / موجه" else "Gateway / Core Switch",
                    textColor = colors.textSecondary
                )
                TopologyLegendRow(
                    color = StatusOnlineGreen,
                    label = if (isArabic) "جهاز طرفي متصل" else "Online Endpoint",
                    textColor = colors.textSecondary
                )
                TopologyLegendRow(
                    color = StatusWarningAmber,
                    label = if (isArabic) "تنبيه / حمل مرتفع" else "Alert / High Load",
                    textColor = colors.textSecondary
                )
                TopologyLegendRow(
                    color = StatusOfflineRed,
                    label = if (isArabic) "رابط غير متصل" else "Offline Link",
                    textColor = colors.textSecondary
                )
                TopologyLegendRow(
                    color = StatusRogueCrimson,
                    label = if (isArabic) "تهديد دخيل مشبوه" else "Rogue Threat (Radar)",
                    isAlert = true,
                    textColor = StatusRogueCrimson
                )
                if (nodeDragOffsets.isNotEmpty()) {
                    Text(
                        text = if (isArabic) "تم تعديل مواضع العقد يدويًا" else "Custom Drag Applied",
                        fontSize = 8.5.sp,
                        color = colors.primaryAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // OVERLAY 3: Bottom Gesture Guide & Interaction Status
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background((if (isDark) CyberNavyCard else Color.White).copy(alpha = 0.90f))
                .border(1.dp, if (isDark) CyberNavyBorder else LightCanvasBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (showDataFlow) StatusOnlineGreen else colors.textMuted)
                )
                Text(
                    text = if (isArabic) {
                        "اسحب أي عقدة لتحريكها • انقر للتحديد • ضغطتان لإعادة الضبط"
                    } else {
                        "Drag node to move • Tap to inspect • Double-tap to reset"
                    },
                    color = colors.textSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun TopologyLegendRow(
    color: Color,
    label: String,
    textColor: Color,
    isAlert: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = if (isAlert) FontWeight.Bold else FontWeight.Normal
        )
    }
}
