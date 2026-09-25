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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.Traffic24hSummary
import com.example.data.model.TrafficTrend24hPoint
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class TimeSliceFilter(val englishTitle: String, val arabicTitle: String, val hourRange: IntRange) {
    ALL_24H("24h All", "كامل 24 ساعة", 0..23),
    BUSINESS_HOURS("Peak (09:00-17:00)", "ذروة العمل (09-17)", 9..17),
    NIGHT_OFFPEAK("Night (00:00-08:00)", "الليل والنسخ (00-08)", 0..8),
    EVENING("Evening (18:00-23:00)", "فترة المساء (18-23)", 18..23)
}

enum class MetricViewMode(val englishTitle: String, val arabicTitle: String) {
    THROUGHPUT_MBPS("Throughput (Mbps)", "سرعة التدفق (Mbps)"),
    VOLUME_GB("Volume (GB/h)", "حجم النقل (GB)"),
    PROTOCOLS("Protocols", "توزيع البروتوكولات")
}

/**
 * Recharts & D3-Inspired Visual Dashboard Summary Component
 * Displays comprehensive 24-hour network traffic trends with smooth Monotone Spline curves,
 * gradient area fills, interactive Cartesian grid, touch scrubbing tooltip,
 * KPI summary metrics, and protocol distributions.
 */
@Composable
fun NetworkTraffic24hTrendsDashboardCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDark) { getNetGuardColors(isDark) }

    val allPoints by viewModel.trafficTrend24hPoints.collectAsState()
    val summary = remember(allPoints) { viewModel.get24hTrafficSummary() }

    // Controls & State
    var selectedFilter by remember { mutableStateOf(TimeSliceFilter.ALL_24H) }
    var metricMode by remember { mutableStateOf(MetricViewMode.THROUGHPUT_MBPS) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var isHourlyTableExpanded by remember { mutableStateOf(false) }

    // Recharts Series toggles
    var showInboundSeries by remember { mutableStateOf(true) }
    var showOutboundSeries by remember { mutableStateOf(true) }
    var showPeakEnvelope by remember { mutableStateOf(true) }
    var showAreaFills by remember { mutableStateOf(true) }

    // Filter points based on selected time-slice
    val filteredPoints = remember(allPoints, selectedFilter) {
        allPoints.filter { it.hourIndex in selectedFilter.hourRange }
    }

    // Default to the highest peak point if none selected yet, for immediate rich executive insight
    val activeScrubPoint = remember(selectedPointIndex, filteredPoints) {
        if (selectedPointIndex != null && selectedPointIndex!! in filteredPoints.indices) {
            filteredPoints[selectedPointIndex!!]
        } else {
            filteredPoints.maxByOrNull { it.inboundMbps } ?: filteredPoints.lastOrNull()
        }
    }

    // D3 / Recharts Palette
    val colorInbound = Color(0xFF00E5FF)   // Neon Cyan
    val colorOutbound = Color(0xFF6366F1)  // Indigo Blue
    val colorPeak = Color(0xFFF59E0B)      // Amber Orange
    val colorGrid = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val colorAxisText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("network_traffic_24h_trends_card"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (activeScrubPoint?.isSpike == true) colors.statusAmber.copy(alpha = 0.5f) else colors.cardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==============================================================
            // 1. HEADER: Title, D3 / Recharts Engine Badge & Simulation Button
            // ==============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isArabic) "اتجاهات حركة الشبكة (24 ساعة)" else "24-HOUR NETWORK TRAFFIC TRENDS",
                            color = colors.primaryAccent,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        // D3 / Recharts Visual Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.primaryAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "D3 / RECHARTS",
                                color = colors.primaryAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isArabic) "ملخص بصري متكامل للتدفقات الصادرة والواردة وذروة الاستهلاك"
                            else "Visual Summary of Inbound/Outbound Bandwidth & Peak Bursts",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Interactive Surge Simulation Button
                IconButton(
                    onClick = { viewModel.simulate24hTrafficSpike() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                        .testTag("simulate_24h_surge_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Simulate 24h Traffic Surge",
                        tint = colors.statusAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ==============================================================
            // 2. EXECUTIVE 24H KPI SUMMARY CARDS (4 Cards Grid)
            // ==============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: Total Inbound Volume
                KpiMetricCard(
                    title = if (isArabic) "إجمالي الوارد (Inbound)" else "24h Inbound Volume",
                    mainValue = String.format(Locale.US, "%.2f TB", summary.totalInboundTB),
                    subValue = String.format(Locale.US, "Avg: %.0f Mbps", summary.avgInboundMbps),
                    accentColor = colorInbound,
                    icon = Icons.Default.CloudDownload,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )

                // Card 2: Total Outbound Volume
                KpiMetricCard(
                    title = if (isArabic) "إجمالي الصادر (Outbound)" else "24h Outbound Volume",
                    mainValue = String.format(Locale.US, "%.2f TB", summary.totalOutboundTB),
                    subValue = String.format(Locale.US, "Avg: %.0f Mbps", summary.avgOutboundMbps),
                    accentColor = colorOutbound,
                    icon = Icons.Default.CloudUpload,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 3: 24h Peak Throughput
                KpiMetricCard(
                    title = if (isArabic) "أعلى ذروة تدفق (Peak)" else "24h Peak Throughput",
                    mainValue = String.format(Locale.US, "%.0f Mbps", summary.peakThroughputMbps),
                    subValue = if (isArabic) "في الساعة ${summary.peakHourLabel}" else "Recorded at ${summary.peakHourLabel}",
                    accentColor = colorPeak,
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )

                // Card 4: SLA Uptime & Anomalies
                KpiMetricCard(
                    title = if (isArabic) "امتثال الخدمة (SLA)" else "Network SLA Uptime",
                    mainValue = String.format(Locale.US, "%.2f%%", summary.slaCompliancePercent),
                    subValue = if (isArabic) "${summary.totalAnomaliesCount} أحداث ذروة مراقبة" else "${summary.totalAnomaliesCount} Events Flagged",
                    accentColor = colors.statusGreen,
                    icon = Icons.Default.VerifiedUser,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
            }

            // ==============================================================
            // 3. TIME-SLICE & METRIC CONTROLS (Recharts Filter Bar)
            // ==============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Range Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(TimeSliceFilter.values()) { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.primaryAccent else colors.surface)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.primaryAccent else colors.cardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedFilter = filter
                                    selectedPointIndex = null
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("time_filter_${filter.name.lowercase()}")
                        ) {
                            Text(
                                text = if (isArabic) filter.arabicTitle else filter.englishTitle,
                                color = if (isSelected) (if (isDark) Color.Black else Color.White) else colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Metric Mode Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    MetricViewMode.values().forEach { mode ->
                        val isSelected = metricMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) colors.primaryAccent.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { metricMode = mode }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isArabic) mode.arabicTitle else mode.englishTitle,
                                color = if (isSelected) colors.primaryAccent else colors.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // ==============================================================
            // 4. RECHARTS INTERACTIVE LEGEND (SERIES TOGGLES)
            // ==============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Inbound Toggle
                RechartsSeriesPill(
                    label = if (isArabic) "الوارد (Inbound Mbps)" else "Inbound (Download)",
                    color = colorInbound,
                    isActive = showInboundSeries,
                    onToggle = { showInboundSeries = !showInboundSeries },
                    colors = colors
                )

                // Outbound Toggle
                RechartsSeriesPill(
                    label = if (isArabic) "الصادر (Outbound Mbps)" else "Outbound (Upload)",
                    color = colorOutbound,
                    isActive = showOutboundSeries,
                    onToggle = { showOutboundSeries = !showOutboundSeries },
                    colors = colors
                )

                // Peak Spikes Toggle
                RechartsSeriesPill(
                    label = if (isArabic) "ذروة التدفق" else "Peak Burst",
                    color = colorPeak,
                    isActive = showPeakEnvelope,
                    onToggle = { showPeakEnvelope = !showPeakEnvelope },
                    colors = colors
                )

                // Area Fill Toggle
                IconButton(
                    onClick = { showAreaFills = !showAreaFills },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (showAreaFills) Icons.Default.Layers else Icons.Default.LayersClear,
                        contentDescription = "Toggle Area Fills",
                        tint = if (showAreaFills) colors.primaryAccent else colors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // ==============================================================
            // 5. INTERACTIVE D3 / RECHARTS CANVAS (MONOTONE SPLINE & SCRUBBING)
            // ==============================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .testTag("d3_recharts_canvas_box")
            ) {
                // Pointer Gesture Detector for Touch Scrubbing / Tooltip
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredPoints) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val count = filteredPoints.size
                                    if (count > 1) {
                                        val chartW = size.width - 70f
                                        val relX = (offset.x - 40f).coerceIn(0f, chartW)
                                        val index = ((relX / chartW) * (count - 1)).roundToInt().coerceIn(0, count - 1)
                                        selectedPointIndex = if (selectedPointIndex == index) null else index
                                    }
                                }
                            )
                        }
                        .pointerInput(filteredPoints) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val count = filteredPoints.size
                                if (count > 1) {
                                    val chartW = size.width - 70f
                                    val relX = (change.position.x - 40f).coerceIn(0f, chartW)
                                    val index = ((relX / chartW) * (count - 1)).roundToInt().coerceIn(0, count - 1)
                                    selectedPointIndex = index
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val leftMargin = 42f
                        val rightMargin = 28f
                        val topMargin = 22f
                        val bottomMargin = 30f
                        val plotW = w - leftMargin - rightMargin
                        val plotH = h - topMargin - bottomMargin

                        if (filteredPoints.size < 2 || plotW <= 0 || plotH <= 0) return@Canvas

                        // Determine Y-scale max based on metricMode
                        val maxYValue = when (metricMode) {
                            MetricViewMode.THROUGHPUT_MBPS -> max(
                                filteredPoints.maxOfOrNull { max(it.peakBurstMbps, max(it.inboundMbps, it.outboundMbps)) } ?: 600f,
                                700f
                            )
                            MetricViewMode.VOLUME_GB -> max(
                                filteredPoints.maxOfOrNull { max(it.inboundGB, it.outboundGB) } ?: 250f,
                                300f
                            )
                            MetricViewMode.PROTOCOLS -> 100f
                        }

                        fun getX(index: Int): Float {
                            return leftMargin + (plotW / (filteredPoints.size - 1)) * index
                        }

                        fun getY(value: Float): Float {
                            val ratio = (value / maxYValue).coerceIn(0f, 1f)
                            return topMargin + plotH * (1f - ratio)
                        }

                        // --------------------------------------------------
                        // A. Peak Business Hours Shaded Background Band (09:00 - 17:00)
                        // --------------------------------------------------
                        if (selectedFilter == TimeSliceFilter.ALL_24H) {
                            val peakStartIdx = filteredPoints.indexOfFirst { it.hourIndex == 9 }.coerceAtLeast(0)
                            val peakEndIdx = filteredPoints.indexOfLast { it.hourIndex == 17 }.coerceAtMost(filteredPoints.size - 1)
                            if (peakStartIdx in filteredPoints.indices && peakEndIdx in filteredPoints.indices && peakEndIdx > peakStartIdx) {
                                val startX = getX(peakStartIdx)
                                val endX = getX(peakEndIdx)
                                drawRect(
                                    color = colorInbound.copy(alpha = 0.05f),
                                    topLeft = Offset(startX, topMargin),
                                    size = Size(endX - startX, plotH)
                                )
                            }
                        }

                        // --------------------------------------------------
                        // B. Cartesian D3 Gridlines (Horizontal Reference Lines)
                        // --------------------------------------------------
                        val divisions = 4
                        for (i in 0..divisions) {
                            val y = topMargin + (plotH / divisions) * i
                            drawLine(
                                color = colorGrid,
                                start = Offset(leftMargin, y),
                                end = Offset(w - rightMargin, y),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        }

                        // --------------------------------------------------
                        // C. D3 Monotone Spline Curve Drawer
                        // --------------------------------------------------
                        fun drawMonotoneSeries(
                            points: List<Offset>,
                            strokeColor: Color,
                            areaBrush: Brush?
                        ) {
                            if (points.size < 2) return
                            val path = Path()
                            path.moveTo(points[0].x, points[0].y)

                            // Monotone Cubic Hermite Spline calculation
                            for (i in 0 until points.size - 1) {
                                val p0 = if (i > 0) points[i - 1] else points[i]
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                val p3 = if (i < points.size - 2) points[i + 2] else p2

                                val cp1x = p1.x + (p2.x - p0.x) / 6f
                                val cp1y = p1.y + (p2.y - p0.y) / 6f
                                val cp2x = p2.x - (p3.x - p1.x) / 6f
                                val cp2y = p2.y - (p3.y - p1.y) / 6f

                                path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                            }

                            // Optional Area Fill
                            if (areaBrush != null && showAreaFills) {
                                val areaPath = Path()
                                areaPath.addPath(path)
                                areaPath.lineTo(points.last().x, topMargin + plotH)
                                areaPath.lineTo(points.first().x, topMargin + plotH)
                                areaPath.close()
                                drawPath(path = areaPath, brush = areaBrush)
                            }

                            // Curve Stroke
                            drawPath(
                                path = path,
                                color = strokeColor,
                                style = Stroke(width = 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        // --------------------------------------------------
                        // D. Render Active Series Based on Metric Mode
                        // --------------------------------------------------
                        when (metricMode) {
                            MetricViewMode.THROUGHPUT_MBPS -> {
                                // 1. Inbound Throughput Curve (Cyan)
                                if (showInboundSeries) {
                                    val inOffsets = filteredPoints.mapIndexed { idx, pt ->
                                        Offset(getX(idx), getY(pt.inboundMbps))
                                    }
                                    drawMonotoneSeries(
                                        points = inOffsets,
                                        strokeColor = colorInbound,
                                        areaBrush = Brush.verticalGradient(
                                            colors = listOf(colorInbound.copy(alpha = 0.28f), Color.Transparent),
                                            startY = topMargin,
                                            endY = topMargin + plotH
                                        )
                                    )
                                }

                                // 2. Outbound Throughput Curve (Indigo)
                                if (showOutboundSeries) {
                                    val outOffsets = filteredPoints.mapIndexed { idx, pt ->
                                        Offset(getX(idx), getY(pt.outboundMbps))
                                    }
                                    drawMonotoneSeries(
                                        points = outOffsets,
                                        strokeColor = colorOutbound,
                                        areaBrush = Brush.verticalGradient(
                                            colors = listOf(colorOutbound.copy(alpha = 0.18f), Color.Transparent),
                                            startY = topMargin,
                                            endY = topMargin + plotH
                                        )
                                    )
                                }

                                // 3. Peak Burst Envelope (Dashed Amber Curve)
                                if (showPeakEnvelope) {
                                    val peakOffsets = filteredPoints.mapIndexed { idx, pt ->
                                        Offset(getX(idx), getY(pt.peakBurstMbps))
                                    }
                                    val peakPath = Path()
                                    peakOffsets.forEachIndexed { i, off ->
                                        if (i == 0) peakPath.moveTo(off.x, off.y) else peakPath.lineTo(off.x, off.y)
                                    }
                                    drawPath(
                                        path = peakPath,
                                        color = colorPeak.copy(alpha = 0.75f),
                                        style = Stroke(
                                            width = 1.6f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                                        )
                                    )
                                }
                            }
                            MetricViewMode.VOLUME_GB -> {
                                // Volume Mode: Inbound & Outbound Data in GB
                                if (showInboundSeries) {
                                    val inGbOffsets = filteredPoints.mapIndexed { idx, pt ->
                                        Offset(getX(idx), getY(pt.inboundGB))
                                    }
                                    drawMonotoneSeries(
                                        points = inGbOffsets,
                                        strokeColor = colorInbound,
                                        areaBrush = Brush.verticalGradient(
                                            colors = listOf(colorInbound.copy(alpha = 0.25f), Color.Transparent),
                                            startY = topMargin,
                                            endY = topMargin + plotH
                                        )
                                    )
                                }
                                if (showOutboundSeries) {
                                    val outGbOffsets = filteredPoints.mapIndexed { idx, pt ->
                                        Offset(getX(idx), getY(pt.outboundGB))
                                    }
                                    drawMonotoneSeries(
                                        points = outGbOffsets,
                                        strokeColor = colorOutbound,
                                        areaBrush = Brush.verticalGradient(
                                            colors = listOf(colorOutbound.copy(alpha = 0.18f), Color.Transparent),
                                            startY = topMargin,
                                            endY = topMargin + plotH
                                        )
                                    )
                                }
                            }
                            MetricViewMode.PROTOCOLS -> {
                                // Top HTTPS % Curve
                                val httpsOffsets = filteredPoints.mapIndexed { idx, pt ->
                                    val pct = pt.protocolBreakdown["HTTPS"] ?: 55f
                                    Offset(getX(idx), getY(pct))
                                }
                                drawMonotoneSeries(
                                    points = httpsOffsets,
                                    strokeColor = colorInbound,
                                    areaBrush = Brush.verticalGradient(
                                        colors = listOf(colorInbound.copy(alpha = 0.2f), Color.Transparent),
                                        startY = topMargin,
                                        endY = topMargin + plotH
                                    )
                                )
                            }
                        }

                        // --------------------------------------------------
                        // E. Anomaly Reference Markers
                        // --------------------------------------------------
                        filteredPoints.forEachIndexed { idx, pt ->
                            if (pt.isSpike || pt.anomalyNote != null) {
                                val x = getX(idx)
                                val y = when (metricMode) {
                                    MetricViewMode.THROUGHPUT_MBPS -> getY(pt.peakBurstMbps)
                                    MetricViewMode.VOLUME_GB -> getY(pt.inboundGB)
                                    MetricViewMode.PROTOCOLS -> getY(70f)
                                }
                                // Glowing marker ring
                                drawCircle(
                                    color = colorPeak.copy(alpha = 0.35f),
                                    radius = 8f,
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = colorPeak,
                                    radius = 4f,
                                    center = Offset(x, y)
                                )
                            }
                        }

                        // --------------------------------------------------
                        // F. Selected Point / Crosshair Cursor (Recharts ReferenceLine)
                        // --------------------------------------------------
                        selectedPointIndex?.let { selIdx ->
                            if (selIdx in filteredPoints.indices) {
                                val curX = getX(selIdx)
                                val curPt = filteredPoints[selIdx]

                                // Vertical D3 Reference Line
                                drawLine(
                                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.45f),
                                    start = Offset(curX, topMargin),
                                    end = Offset(curX, topMargin + plotH),
                                    strokeWidth = 1.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                                )

                                // Focal Glowing Target Circles
                                if (showInboundSeries) {
                                    val inY = when (metricMode) {
                                        MetricViewMode.THROUGHPUT_MBPS -> getY(curPt.inboundMbps)
                                        MetricViewMode.VOLUME_GB -> getY(curPt.inboundGB)
                                        MetricViewMode.PROTOCOLS -> getY(curPt.protocolBreakdown["HTTPS"] ?: 55f)
                                    }
                                    drawCircle(
                                        color = colorInbound.copy(alpha = 0.3f),
                                        radius = 10f,
                                        center = Offset(curX, inY)
                                    )
                                    drawCircle(
                                        color = colorInbound,
                                        radius = 4.5f,
                                        center = Offset(curX, inY)
                                    )
                                }

                                if (showOutboundSeries && metricMode != MetricViewMode.PROTOCOLS) {
                                    val outY = when (metricMode) {
                                        MetricViewMode.THROUGHPUT_MBPS -> getY(curPt.outboundMbps)
                                        MetricViewMode.VOLUME_GB -> getY(curPt.outboundGB)
                                        else -> getY(30f)
                                    }
                                    drawCircle(
                                        color = colorOutbound.copy(alpha = 0.3f),
                                        radius = 10f,
                                        center = Offset(curX, outY)
                                    )
                                    drawCircle(
                                        color = colorOutbound,
                                        radius = 4.5f,
                                        center = Offset(curX, outY)
                                    )
                                }
                            }
                        }
                    }
                }

                // X-Axis Time Labels Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 36.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val step = max(1, filteredPoints.size / 6)
                    filteredPoints.forEachIndexed { index, point ->
                        if (index % step == 0 || index == filteredPoints.size - 1) {
                            Text(
                                text = point.timeLabel,
                                color = if (activeScrubPoint?.hourIndex == point.hourIndex) colors.primaryAccent else colorAxisText,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (activeScrubPoint?.hourIndex == point.hourIndex) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // ==============================================================
            // 6. RICH RECHARTS TOOLTIP CARD (INSPECTION DRAWER)
            // ==============================================================
            activeScrubPoint?.let { pt ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(if (pt.isSpike) colorPeak.copy(alpha = 0.6f) else colors.cardBorder)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recharts_scrub_tooltip_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tooltip Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${pt.timeLabel} (${pt.relativeLabel})",
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (pt.isPeakHour) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colorPeak.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "ذروة العمل PEAK" else "PEAK WINDOW",
                                        color = colorPeak,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (pt.isOffPeak) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.secondaryAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "خارج الذروة OFF-PEAK" else "OFF-PEAK",
                                        color = colors.secondaryAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Anomaly Notice Banner if present
                        pt.anomalyNote?.let { note ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colorPeak.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = colorPeak, modifier = Modifier.size(14.dp))
                                Text(
                                    text = note,
                                    color = colorPeak,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Detailed Metrics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "معدل الوارد (Inbound)" else "Inbound Speed",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f Mbps", pt.inboundMbps),
                                    color = colorInbound,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f GB transferred", pt.inboundGB),
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            Column {
                                Text(
                                    text = if (isArabic) "معدل الصادر (Outbound)" else "Outbound Speed",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f Mbps", pt.outboundMbps),
                                    color = colorOutbound,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f GB transferred", pt.outboundGB),
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isArabic) "أعلى ذروة (Peak Burst)" else "Peak Burst",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f Mbps", pt.peakBurstMbps),
                                    color = colorPeak,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${pt.activeSessions} sessions • ${pt.latencyMs}ms",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Protocol Distribution Mini Stacked Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "البروتوكول السائد: ${pt.topProtocol}" else "Dominant Protocol: ${pt.topProtocol}",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "HTTPS 58% | Cloud 22% | DB 12%",
                                    color = colors.textSecondary,
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Horizontal stacked bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            ) {
                                Box(modifier = Modifier.weight(0.58f).fillMaxHeight().background(colorInbound))
                                Box(modifier = Modifier.weight(0.22f).fillMaxHeight().background(colorOutbound))
                                Box(modifier = Modifier.weight(0.12f).fillMaxHeight().background(colorPeak))
                                Box(modifier = Modifier.weight(0.08f).fillMaxHeight().background(colors.statusGreen))
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 7. EXPANDABLE 24H HOURLY AUDIT TABLE
            // ==============================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isHourlyTableExpanded = !isHourlyTableExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableRows,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isArabic) "عرض جدول القياسات لكل ساعة (24 نقطة)" else "View Hourly Telemetry Table (24 Points)",
                        color = colors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = if (isHourlyTableExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isHourlyTableExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "HOUR", color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                        Text(text = "INBOUND", color = colorInbound, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                        Text(text = "OUTBOUND", color = colorOutbound, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                        Text(text = "PEAK", color = colorPeak, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(text = "SESSIONS", color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    }
                    Divider(color = colors.cardBorder, thickness = 0.5.dp)

                    // Top 8 or all filtered rows
                    filteredPoints.forEach { pt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (pt.hourIndex == activeScrubPoint?.hourIndex) colors.primaryAccent.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { selectedPointIndex = filteredPoints.indexOf(pt) }
                                .padding(horizontal = 4.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = pt.timeLabel, color = colors.textPrimary, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))
                            Text(text = String.format(Locale.US, "%.0f M", pt.inboundMbps), color = colorInbound, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                            Text(text = String.format(Locale.US, "%.0f M", pt.outboundMbps), color = colorOutbound, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                            Text(text = String.format(Locale.US, "%.0f", pt.peakBurstMbps), color = colorPeak, fontSize = 10.5.sp, modifier = Modifier.weight(1f))
                            Text(text = "${pt.activeSessions}", color = colors.textSecondary, fontSize = 10.sp, modifier = Modifier.weight(1.2f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Mini KPI Card for Executive Visual Telemetry
 */
@Composable
private fun KpiMetricCard(
    title: String,
    mainValue: String,
    subValue: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    colors: com.example.ui.theme.NetGuardThemeColors
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(colors.cardBorder)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = colors.textSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = mainValue,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = subValue,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Recharts Interactive Legend Pill with Series Toggle
 */
@Composable
private fun RechartsSeriesPill(
    label: String,
    color: Color,
    isActive: Boolean,
    onToggle: () -> Unit,
    colors: com.example.ui.theme.NetGuardThemeColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) color.copy(alpha = 0.12f) else colors.surface)
            .border(
                1.dp,
                if (isActive) color.copy(alpha = 0.4f) else colors.cardBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable { onToggle() }
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isActive) color else colors.textSecondary)
        )
        Text(
            text = label,
            color = if (isActive) colors.textPrimary else colors.textSecondary,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
