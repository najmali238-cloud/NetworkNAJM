package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.RechartsTrendPoint
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

enum class RechartsViewMode(val englishLabel: String, val arabicLabel: String) {
    COMBINED("Combined (Traffic & Load)", "مدمج (الحركة وحمل الخوادم)"),
    TRAFFIC_ONLY("Network Traffic", "حركة الشبكة فقط"),
    SERVER_LOAD_ONLY("Server Load", "حمل الخوادم فقط")
}

enum class RechartsTimeScale(val label: String, val pointsCount: Int) {
    LIVE_1M("1M Live", 12),
    WINDOW_5M("5M Window", 20),
    FULL_BUFFER("Max History", 30)
}

/**
 * Recharts-Inspired Interactive Multi-Series Line Chart Component
 * Visualizes real-time network traffic (Download & Upload Mbps) and
 * server load trends (Cluster CPU & RAM utilization) using an interactive
 * Cartesian line chart with tooltip scrubbing, series toggles, and live streaming.
 */
@Composable
fun RechartsInteractiveLineChart(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDark) { getNetGuardColors(isDark) }

    val rawTrendPoints by viewModel.rechartsTrendPoints.collectAsState()

    var viewMode by remember { mutableStateOf(RechartsViewMode.COMBINED) }
    var timeScale by remember { mutableStateOf(RechartsTimeScale.WINDOW_5M) }
    var isStreamPaused by remember { mutableStateOf(false) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    // Toggleable series visibility (Recharts Legend feature)
    var showDownloadSeries by remember { mutableStateOf(true) }
    var showUploadSeries by remember { mutableStateOf(true) }
    var showCpuSeries by remember { mutableStateOf(true) }
    var showRamSeries by remember { mutableStateOf(true) }

    // Filter points based on paused state and time scale
    val activePoints = remember(rawTrendPoints, isStreamPaused, timeScale) {
        rawTrendPoints.takeLast(timeScale.pointsCount)
    }

    // Live blinking pulse for status indicator
    val infiniteTransition = rememberInfiniteTransition(label = "rechartsPulse")
    val liveDotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Chart Palette Colors (matching Recharts modern palette)
    val colorDownload = Color(0xFF00E5FF) // Cyan
    val colorUpload = Color(0xFF3B82F6)   // Royal Blue
    val colorCpu = Color(0xFFF59E0B)      // Amber Orange
    val colorRam = Color(0xFFA855F7)      // Purple Violet
    val gridLineColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val axisTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Latest readings for quick header summary
    val latestPoint = activePoints.lastOrNull()
    val currentDl = latestPoint?.downloadMbps ?: 340f
    val currentUl = latestPoint?.uploadMbps ?: 75f
    val currentCpu = latestPoint?.serverCpuPercent ?: 42f
    val currentRam = latestPoint?.serverRamPercent ?: 58f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_interactive_line_chart"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (latestPoint?.isSpike == true) colors.statusCrimson.copy(alpha = 0.6f) else colors.cardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==========================================
            // 1. Header with Recharts badge & Controls
            // ==========================================
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
                            text = if (isArabic) "مؤشرات حركة الشبكة وحمل الخوادم" else "REAL-TIME TRAFFIC & SERVER LOAD",
                            color = colors.primaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        // Recharts Live badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isStreamPaused) colors.textSecondary.copy(alpha = 0.2f) else colors.statusGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isStreamPaused) colors.textSecondary
                                        else colors.statusGreen.copy(alpha = liveDotAlpha)
                                    )
                            )
                            Text(
                                text = if (isStreamPaused) (if (isArabic) "متوقف" else "PAUSED") else (if (isArabic) "مباشر RECHARTS" else "RECHARTS LIVE"),
                                color = if (isStreamPaused) colors.textSecondary else colors.statusGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (isArabic) "رسم بياني تفاعلي متعدد المسارات (Interactive Line Chart)" else "Interactive Multi-Series Cartesian Line Trends",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Controls: Simulate Spike & Pause
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.simulateRechartsTrafficSurge() },
                        modifier = Modifier.size(32.dp).testTag("recharts_simulate_burst_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Simulate Traffic Surge",
                            tint = colors.statusAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isStreamPaused = !isStreamPaused },
                        modifier = Modifier.size(32.dp).testTag("recharts_stream_pause_toggle")
                    ) {
                        Icon(
                            imageVector = if (isStreamPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isStreamPaused) "Resume" else "Pause",
                            tint = if (isStreamPaused) colors.statusGreen else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Spike Warning Banner if active
            if (latestPoint?.isSpike == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.statusCrimson.copy(alpha = 0.15f))
                        .border(1.dp, colors.statusCrimson.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.statusCrimson,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isArabic) "طفرة مرورية: تدفق الحزم وحمل المعالج تجاوزا المعدل الطبيعي!" else "TRAFFIC & LOAD SURGE: Ingress throughput or CPU spike detected!",
                            color = colors.statusCrimson,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ==========================================
            // 2. View Mode Tabs & Time Scale Selector
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Mode Pill Tabs
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    RechartsViewMode.values().forEach { mode ->
                        val isSelected = viewMode == mode
                        val label = if (isArabic) mode.arabicLabel else when (mode) {
                            RechartsViewMode.COMBINED -> "Combined"
                            RechartsViewMode.TRAFFIC_ONLY -> "Traffic (Mbps)"
                            RechartsViewMode.SERVER_LOAD_ONLY -> "Server Load (%)"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) colors.primaryAccent.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    viewMode = mode
                                    selectedPointIndex = null
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("recharts_view_mode_${mode.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) colors.primaryAccent else colors.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Time window resolution
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    RechartsTimeScale.values().forEach { scale ->
                        val isSelected = timeScale == scale
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) colors.primaryAccent.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { timeScale = scale }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = scale.label,
                                color = if (isSelected) colors.primaryAccent else colors.textSecondary,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 3. Interactive Recharts Legend (Clickable)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "المسارات:" else "Series:",
                    color = colors.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                if (viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.TRAFFIC_ONLY) {
                    RechartsLegendItem(
                        label = if (isArabic) "التحميل (${currentDl.toInt()} Mbps)" else "Download (${currentDl.toInt()}M)",
                        color = colorDownload,
                        isVisible = showDownloadSeries,
                        onToggle = { showDownloadSeries = !showDownloadSeries },
                        colors = colors
                    )

                    RechartsLegendItem(
                        label = if (isArabic) "الرفع (${currentUl.toInt()} Mbps)" else "Upload (${currentUl.toInt()}M)",
                        color = colorUpload,
                        isVisible = showUploadSeries,
                        onToggle = { showUploadSeries = !showUploadSeries },
                        colors = colors
                    )
                }

                if (viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.SERVER_LOAD_ONLY) {
                    RechartsLegendItem(
                        label = if (isArabic) "المعالج (${currentCpu.toInt()}%)" else "CPU (${currentCpu.toInt()}%)",
                        color = colorCpu,
                        isVisible = showCpuSeries,
                        onToggle = { showCpuSeries = !showCpuSeries },
                        colors = colors
                    )

                    RechartsLegendItem(
                        label = if (isArabic) "الذاكرة (${currentRam.toInt()}%)" else "RAM (${currentRam.toInt()}%)",
                        color = colorRam,
                        isVisible = showRamSeries,
                        onToggle = { showRamSeries = !showRamSeries },
                        colors = colors
                    )
                }
            }

            // ==========================================
            // 4. Interactive Line Chart Canvas
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .testTag("recharts_chart_canvas")
            ) {
                // Interactive Pointer Gesture Detector for Touch Scrubbing / Tooltip
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(activePoints) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val count = activePoints.size
                                    if (count > 1) {
                                        val chartW = size.width - 60f // account for axis margins
                                        val relX = (offset.x - 30f).coerceIn(0f, chartW)
                                        val index = ((relX / chartW) * (count - 1)).roundToInt().coerceIn(0, count - 1)
                                        selectedPointIndex = if (selectedPointIndex == index) null else index
                                    }
                                }
                            )
                        }
                        .pointerInput(activePoints) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val count = activePoints.size
                                if (count > 1) {
                                    val chartW = size.width - 60f
                                    val relX = (change.position.x - 30f).coerceIn(0f, chartW)
                                    val index = ((relX / chartW) * (count - 1)).roundToInt().coerceIn(0, count - 1)
                                    selectedPointIndex = index
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val leftMargin = 38f
                        val rightMargin = 38f
                        val topMargin = 20f
                        val bottomMargin = 28f
                        val plotW = w - leftMargin - rightMargin
                        val plotH = h - topMargin - bottomMargin

                        if (activePoints.size < 2 || plotW <= 0 || plotH <= 0) return@Canvas

                        // Compute Max scales
                        val maxTraffic = max(
                            activePoints.maxOfOrNull { max(it.downloadMbps, it.uploadMbps) } ?: 500f,
                            600f
                        )
                        val maxLoad = 100f // % for CPU/RAM

                        // ------------------------------------------
                        // A. Cartesian Grid (Horizontal Reference Lines)
                        // ------------------------------------------
                        val gridDivisions = 4
                        for (i in 0..gridDivisions) {
                            val y = topMargin + (plotH / gridDivisions) * i
                            // Dashed grid line
                            drawLine(
                                color = gridLineColor,
                                start = Offset(leftMargin, y),
                                end = Offset(w - rightMargin, y),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        }

                        // ------------------------------------------
                        // B. Curves Rendering Helpers
                        // ------------------------------------------
                        fun getPointX(idx: Int): Float {
                            return leftMargin + (idx.toFloat() / (activePoints.size - 1)) * plotW
                        }

                        fun getTrafficY(mbps: Float): Float {
                            val ratio = (mbps / maxTraffic).coerceIn(0f, 1f)
                            return topMargin + plotH * (1f - ratio)
                        }

                        fun getLoadY(percent: Float): Float {
                            val ratio = (percent / maxLoad).coerceIn(0f, 1f)
                            return topMargin + plotH * (1f - ratio)
                        }

                        // Helper to build smooth cubic spline
                        fun drawMonotoneCurve(
                            points: List<Offset>,
                            lineColor: Color,
                            areaBrush: Brush?
                        ) {
                            if (points.size < 2) return

                            val path = Path()
                            path.moveTo(points.first().x, points.first().y)

                            for (i in 0 until points.size - 1) {
                                val p0 = if (i > 0) points[i - 1] else points[i]
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                val p3 = if (i + 2 < points.size) points[i + 2] else p2

                                val cp1x = p1.x + (p2.x - p0.x) * 0.18f
                                val cp1y = p1.y + (p2.y - p0.y) * 0.18f
                                val cp2x = p2.x - (p3.x - p1.x) * 0.18f
                                val cp2y = p2.y - (p3.y - p1.y) * 0.18f

                                path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                            }

                            // Optional Area Fill
                            if (areaBrush != null) {
                                val areaPath = Path().apply {
                                    addPath(path)
                                    lineTo(points.last().x, topMargin + plotH)
                                    lineTo(points.first().x, topMargin + plotH)
                                    close()
                                }
                                drawPath(path = areaPath, brush = areaBrush)
                            }

                            // Main Stroke Line
                            drawPath(
                                path = path,
                                color = lineColor,
                                style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        // ------------------------------------------
                        // C. Render Active Series
                        // ------------------------------------------
                        // 1. Download Traffic (Cyan)
                        if ((viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.TRAFFIC_ONLY) && showDownloadSeries) {
                            val dlOffsets = activePoints.mapIndexed { i, pt ->
                                Offset(getPointX(i), getTrafficY(pt.downloadMbps))
                            }
                            drawMonotoneCurve(
                                points = dlOffsets,
                                lineColor = colorDownload,
                                areaBrush = Brush.verticalGradient(
                                    colors = listOf(colorDownload.copy(alpha = 0.25f), Color.Transparent),
                                    startY = topMargin,
                                    endY = topMargin + plotH
                                )
                            )
                        }

                        // 2. Upload Traffic (Blue)
                        if ((viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.TRAFFIC_ONLY) && showUploadSeries) {
                            val ulOffsets = activePoints.mapIndexed { i, pt ->
                                Offset(getPointX(i), getTrafficY(pt.uploadMbps))
                            }
                            drawMonotoneCurve(
                                points = ulOffsets,
                                lineColor = colorUpload,
                                areaBrush = Brush.verticalGradient(
                                    colors = listOf(colorUpload.copy(alpha = 0.15f), Color.Transparent),
                                    startY = topMargin,
                                    endY = topMargin + plotH
                                )
                            )
                        }

                        // 3. CPU Load (Amber)
                        if ((viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.SERVER_LOAD_ONLY) && showCpuSeries) {
                            val cpuOffsets = activePoints.mapIndexed { i, pt ->
                                Offset(getPointX(i), getLoadY(pt.serverCpuPercent))
                            }
                            drawMonotoneCurve(
                                points = cpuOffsets,
                                lineColor = colorCpu,
                                areaBrush = if (viewMode == RechartsViewMode.SERVER_LOAD_ONLY) {
                                    Brush.verticalGradient(
                                        colors = listOf(colorCpu.copy(alpha = 0.22f), Color.Transparent),
                                        startY = topMargin,
                                        endY = topMargin + plotH
                                    )
                                } else null
                            )
                        }

                        // 4. RAM Load (Purple)
                        if ((viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.SERVER_LOAD_ONLY) && showRamSeries) {
                            val ramOffsets = activePoints.mapIndexed { i, pt ->
                                Offset(getPointX(i), getLoadY(pt.serverRamPercent))
                            }
                            drawMonotoneCurve(
                                points = ramOffsets,
                                lineColor = colorRam,
                                areaBrush = if (viewMode == RechartsViewMode.SERVER_LOAD_ONLY) {
                                    Brush.verticalGradient(
                                        colors = listOf(colorRam.copy(alpha = 0.18f), Color.Transparent),
                                        startY = topMargin,
                                        endY = topMargin + plotH
                                    )
                                } else null
                            )
                        }

                        // ------------------------------------------
                        // D. Selected Point / Crosshair Cursor
                        // ------------------------------------------
                        selectedPointIndex?.let { selIdx ->
                            if (selIdx in activePoints.indices) {
                                val pt = activePoints[selIdx]
                                val curX = getPointX(selIdx)

                                // Vertical Reference Cursor Line (Recharts ReferenceLine)
                                drawLine(
                                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f),
                                    start = Offset(curX, topMargin),
                                    end = Offset(curX, topMargin + plotH),
                                    strokeWidth = 1.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                                )

                                // Concentric Highlight Rings on active series
                                if ((viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.TRAFFIC_ONLY) && showDownloadSeries) {
                                    val curY = getTrafficY(pt.downloadMbps)
                                    drawCircle(color = colorDownload.copy(alpha = 0.3f), radius = 8f, center = Offset(curX, curY))
                                    drawCircle(color = colorDownload, radius = 4f, center = Offset(curX, curY))
                                }

                                if ((viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.TRAFFIC_ONLY) && showUploadSeries) {
                                    val curY = getTrafficY(pt.uploadMbps)
                                    drawCircle(color = colorUpload.copy(alpha = 0.3f), radius = 7f, center = Offset(curX, curY))
                                    drawCircle(color = colorUpload, radius = 3.5f, center = Offset(curX, curY))
                                }

                                if ((viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.SERVER_LOAD_ONLY) && showCpuSeries) {
                                    val curY = getLoadY(pt.serverCpuPercent)
                                    drawCircle(color = colorCpu.copy(alpha = 0.3f), radius = 8f, center = Offset(curX, curY))
                                    drawCircle(color = colorCpu, radius = 4f, center = Offset(curX, curY))
                                }

                                if ((viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.SERVER_LOAD_ONLY) && showRamSeries) {
                                    val curY = getLoadY(pt.serverRamPercent)
                                    drawCircle(color = colorRam.copy(alpha = 0.3f), radius = 7f, center = Offset(curX, curY))
                                    drawCircle(color = colorRam, radius = 3.5f, center = Offset(curX, curY))
                                }
                            }
                        }
                    }
                }

                // Y-Axis Overlay Labels (Left: Traffic Mbps, Right: Server Load %)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 2.dp, top = 2.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (viewMode == RechartsViewMode.SERVER_LOAD_ONLY) "100%" else "600M", color = axisTextColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text(text = if (viewMode == RechartsViewMode.SERVER_LOAD_ONLY) "75%" else "450M", color = axisTextColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text(text = if (viewMode == RechartsViewMode.SERVER_LOAD_ONLY) "50%" else "300M", color = axisTextColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text(text = if (viewMode == RechartsViewMode.SERVER_LOAD_ONLY) "25%" else "150M", color = axisTextColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "0", color = axisTextColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                }

                // Right Y-Axis (Server Load % in Combined mode)
                if (viewMode == RechartsViewMode.COMBINED) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.TopEnd)
                            .padding(end = 2.dp, top = 2.dp, bottom = 18.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(text = "100%", color = colorCpu.copy(alpha = 0.8f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "75%", color = colorCpu.copy(alpha = 0.8f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "50%", color = colorCpu.copy(alpha = 0.8f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "25%", color = colorCpu.copy(alpha = 0.8f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "0%", color = colorCpu.copy(alpha = 0.8f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Bottom X-Axis Timestamps
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 38.dp, end = 38.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val firstTime = activePoints.firstOrNull()?.timestamp ?: "--"
                    val midTime = activePoints.getOrNull(activePoints.size / 2)?.timestamp ?: "--"
                    val lastTime = activePoints.lastOrNull()?.timestamp ?: "--"

                    Text(text = firstTime, color = axisTextColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text(text = midTime, color = axisTextColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text(text = lastTime, color = axisTextColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                }

                // ==========================================
                // E. Recharts Floating Tooltip Card
                // ==========================================
                selectedPointIndex?.let { selIdx ->
                    if (selIdx in activePoints.indices) {
                        val pt = activePoints[selIdx]
                        val isAlignEnd = selIdx < activePoints.size / 2

                        Box(
                            modifier = Modifier
                                .align(if (isAlignEnd) Alignment.TopEnd else Alignment.TopStart)
                                .padding(horizontal = 46.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF0F172A).copy(alpha = 0.94f) else Color(0xFFFFFFFF).copy(alpha = 0.95f))
                                .border(1.dp, if (pt.isSpike) colors.statusCrimson else colors.cardBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .testTag("recharts_interactive_tooltip")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = pt.timestamp,
                                        color = colors.textPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (pt.isSpike) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(colors.statusCrimson.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (isArabic) "طفرة" else "SPIKE",
                                                color = colors.statusCrimson,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Divider(color = colors.cardBorder, thickness = 0.5.dp)

                                if (viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.TRAFFIC_ONLY) {
                                    TooltipMetricRow(
                                        label = if (isArabic) "تنزيل" else "Download",
                                        value = "${pt.downloadMbps} Mbps",
                                        color = colorDownload
                                    )
                                    TooltipMetricRow(
                                        label = if (isArabic) "رفع" else "Upload",
                                        value = "${pt.uploadMbps} Mbps",
                                        color = colorUpload
                                    )
                                }

                                if (viewMode == RechartsViewMode.COMBINED || viewMode == RechartsViewMode.SERVER_LOAD_ONLY) {
                                    TooltipMetricRow(
                                        label = if (isArabic) "المعالج" else "CPU Load",
                                        value = "${pt.serverCpuPercent}%",
                                        color = colorCpu
                                    )
                                    TooltipMetricRow(
                                        label = if (isArabic) "الذاكرة" else "RAM Load",
                                        value = "${pt.serverRamPercent}%",
                                        color = colorRam
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 5. Bottom Metric Tiles Summary
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Peak Ingress
                RechartsSummaryTile(
                    title = if (isArabic) "أعلى تنزيل" else "Peak Traffic",
                    value = "${(activePoints.maxOfOrNull { it.downloadMbps } ?: 0f).toInt()}M",
                    subValue = if (isArabic) "ذروة التدفق" else "Ingress Peak",
                    color = colorDownload,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )

                // Egress Average
                RechartsSummaryTile(
                    title = if (isArabic) "متوسط الرفع" else "Avg Egress",
                    value = "${(activePoints.map { it.uploadMbps }.average().toFloat().takeIf { !it.isNaN() } ?: 0f).toInt()}M",
                    subValue = if (isArabic) "إرسال البيانات" else "Tx Steady",
                    color = colorUpload,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )

                // Peak CPU
                RechartsSummaryTile(
                    title = if (isArabic) "حمل المعالج" else "Cluster CPU",
                    value = "${currentCpu.toInt()}%",
                    subValue = if (currentCpu > 75f) (if (isArabic) "مرتفع" else "Heavy") else (if (isArabic) "طبيعي" else "Normal"),
                    color = colorCpu,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )

                // RAM Utilization
                RechartsSummaryTile(
                    title = if (isArabic) "استهلاك RAM" else "Cluster RAM",
                    value = "${currentRam.toInt()}%",
                    subValue = if (isArabic) "مستقر" else "Optimal",
                    color = colorRam,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun RechartsLegendItem(
    label: String,
    color: Color,
    isVisible: Boolean,
    onToggle: () -> Unit,
    colors: NetGuardThemeColors
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onToggle() }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isVisible) color else colors.textSecondary.copy(alpha = 0.4f))
        )
        Text(
            text = label,
            color = if (isVisible) colors.textPrimary else colors.textSecondary.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = if (isVisible) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun TooltipMetricRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label: ",
            color = Color.Gray,
            fontSize = 9.sp
        )
        Text(
            text = value,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun RechartsSummaryTile(
    title: String,
    value: String,
    subValue: String,
    color: Color,
    modifier: Modifier = Modifier,
    colors: NetGuardThemeColors
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(colors.cardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = colors.textSecondary,
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subValue,
                color = colors.textSecondary,
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
