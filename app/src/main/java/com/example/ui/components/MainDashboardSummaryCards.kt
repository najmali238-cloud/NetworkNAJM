package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.StatusOfflineRed
import com.example.ui.theme.StatusOnlineGreen
import com.example.ui.theme.StatusRogueCrimson
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Historical system load measurement point over time.
 */
data class SystemLoadHistoryPoint(
    val minuteOffset: Int,
    val loadPercentage: Float,
    val timestampLabel: String
)

/**
 * Generates realistic historical system load performance data across the last hour (60 minutes).
 */
fun generateLastHourSystemLoadHistory(currentLoad: Float): List<SystemLoadHistoryPoint> {
    val baseline = currentLoad.coerceIn(15f, 85f)
    // 13 sample intervals (-60m to 0m at 5-minute intervals)
    val minuteOffsets = listOf(-60, -55, -50, -45, -40, -35, -30, -25, -20, -15, -10, -5, 0)
    val organicDeltas = listOf(-5.4f, -2.1f, 3.8f, 7.5f, 4.2f, -1.8f, -6.1f, -2.7f, 3.1f, 8.4f, 4.6f, -1.2f, 0.0f)

    return minuteOffsets.mapIndexed { index, minOffset ->
        val delta = organicDeltas.getOrElse(index) { 0f }
        val value = if (index == minuteOffsets.lastIndex) {
            currentLoad.coerceIn(5f, 98f)
        } else {
            (baseline + delta).coerceIn(8f, 95f)
        }
        val label = if (minOffset == 0) "Now" else "${minOffset}m"
        SystemLoadHistoryPoint(
            minuteOffset = minOffset,
            loadPercentage = value,
            timestampLabel = label
        )
    }
}

/**
 * Main dashboard UI component using Material 3 that displays summary cards for:
 * 1. 'Active Devices'
 * 2. 'Rogue Devices Detected'
 * 3. 'System Load Percentage' (with Recharts/D3 1-hour line chart visualization)
 */
@Composable
fun MainDashboardSummaryCards(
    activeDevicesCount: Int,
    totalDevicesCount: Int,
    rogueDevicesCount: Int,
    systemLoadPercentage: Float,
    modifier: Modifier = Modifier,
    historicalLoadData: List<SystemLoadHistoryPoint>? = null,
    onActiveDevicesClick: (() -> Unit)? = null,
    onRogueDevicesClick: (() -> Unit)? = null,
    onSystemLoadClick: (() -> Unit)? = null
) {
    val history = remember(systemLoadPercentage, historicalLoadData) {
        historicalLoadData ?: generateLastHourSystemLoadHistory(systemLoadPercentage)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_dashboard_summary_cards"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
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
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                )
                Text(
                    text = "EXECUTIVE TELEMETRY",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "1h Historical Stream",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 3 Material 3 Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: Active Devices
            ActiveDevicesSummaryCard(
                activeCount = activeDevicesCount,
                totalCount = totalDevicesCount,
                onClick = onActiveDevicesClick,
                modifier = Modifier.weight(1f)
            )

            // Card 2: Rogue Devices Detected
            RogueDevicesSummaryCard(
                rogueCount = rogueDevicesCount,
                onClick = onRogueDevicesClick,
                modifier = Modifier.weight(1f)
            )

            // Card 3: System Load Percentage with Recharts/D3 Line Chart
            SystemLoadSummaryCard(
                loadPercentage = systemLoadPercentage,
                historicalData = history,
                onClick = onSystemLoadClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Material 3 Summary Card: Active Devices
 */
@Composable
fun ActiveDevicesSummaryCard(
    activeCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val activeRatio = if (totalCount > 0) (activeCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = activeRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "active_devices_progress"
    )
    val percentage = (activeRatio * 100f).roundToInt()

    ElevatedCard(
        modifier = modifier
            .testTag("card_active_devices")
            .clickable(
                enabled = onClick != null,
                role = Role.Button,
                onClick = { onClick?.invoke() }
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = CyberNavyCard
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, StatusOnlineGreen.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Icon & Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusOnlineGreen.copy(alpha = 0.15f))
                            .border(1.dp, StatusOnlineGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = "Active Devices Icon",
                            tint = StatusOnlineGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Online percentage indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusOnlineGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$percentage%",
                            color = StatusOnlineGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title: 'Active Devices'
                Text(
                    text = "Active Devices",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Numbers
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "$activeCount",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "/$totalCount",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Material 3 Progress Indicator
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = StatusOnlineGreen,
                    trackColor = CyberNavyDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Status text
                Text(
                    text = if (activeCount == totalCount) "100% Online" else "${totalCount - activeCount} Offline",
                    color = if (activeCount == totalCount) StatusOnlineGreen else StatusWarningAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Material 3 Summary Card: Rogue Devices Detected
 */
@Composable
fun RogueDevicesSummaryCard(
    rogueCount: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isAlert = rogueCount > 0
    val infiniteTransition = rememberInfiniteTransition(label = "rogue_radar")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val accentColor = if (isAlert) StatusRogueCrimson else StatusOnlineGreen
    val borderColor = if (isAlert) StatusRogueCrimson.copy(alpha = 0.4f) else StatusOnlineGreen.copy(alpha = 0.2f)

    ElevatedCard(
        modifier = modifier
            .testTag("card_rogue_devices_detected")
            .clickable(
                enabled = onClick != null,
                role = Role.Button,
                onClick = { onClick?.invoke() }
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = CyberNavyCard
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Icon & Alert Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAlert) Icons.Default.GppMaybe else Icons.Default.Security,
                            contentDescription = "Rogue Devices Icon",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Threat status tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isAlert) StatusRogueCrimson.copy(alpha = pulseAlpha * 0.25f)
                                else StatusOnlineGreen.copy(alpha = 0.12f)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isAlert) "ALERT" else "SAFE",
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title: 'Rogue Devices Detected'
                Text(
                    text = "Rogue Devices Detected",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Value
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$rogueCount",
                        color = if (isAlert) StatusRogueCrimson else TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (rogueCount == 1) "threat" else "threats",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Threat indicator bar
                LinearProgressIndicator(
                    progress = { if (isAlert) 1f else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isAlert) StatusRogueCrimson else StatusOnlineGreen,
                    trackColor = CyberNavyDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Status text
                Text(
                    text = if (isAlert) "Quarantine Req." else "Subnet Clean",
                    color = if (isAlert) StatusRogueCrimson else StatusOnlineGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Material 3 Summary Card: System Load Percentage with Recharts/D3 1-Hour Line Chart
 */
@Composable
fun SystemLoadSummaryCard(
    loadPercentage: Float,
    modifier: Modifier = Modifier,
    historicalData: List<SystemLoadHistoryPoint>? = null,
    onClick: (() -> Unit)? = null
) {
    val history = remember(loadPercentage, historicalData) {
        historicalData ?: generateLastHourSystemLoadHistory(loadPercentage)
    }

    var showHistoryDialog by remember { mutableStateOf(false) }

    val clampedLoad = loadPercentage.coerceIn(0f, 100f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedLoad / 100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "system_load_progress"
    )

    val (loadColor, loadStatus) = when {
        clampedLoad >= 80f -> Pair(StatusRogueCrimson, "Heavy")
        clampedLoad >= 60f -> Pair(StatusWarningAmber, "Elevated")
        else -> Pair(CyberCyan, "Optimal")
    }

    val animatedColor by animateColorAsState(
        targetValue = loadColor,
        animationSpec = tween(durationMillis = 500),
        label = "load_color"
    )

    // Calculate 1h delta
    val initialHourLoad = history.firstOrNull()?.loadPercentage ?: clampedLoad
    val hourDelta = clampedLoad - initialHourLoad
    val isDeltaPositive = hourDelta >= 0f

    ElevatedCard(
        modifier = modifier
            .testTag("card_system_load_percentage")
            .clickable(
                role = Role.Button,
                onClick = {
                    showHistoryDialog = true
                    onClick?.invoke()
                }
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = CyberNavyCard
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, animatedColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Icon & Status Tag with Expand Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(animatedColor.copy(alpha = 0.15f))
                            .border(1.dp, animatedColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "System Load Icon",
                            tint = animatedColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Load status badge and expand button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(animatedColor.copy(alpha = 0.12f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = loadStatus,
                                color = animatedColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { showHistoryDialog = true },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("btn_inspect_system_load_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Inspect 1-Hour Performance History",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title: 'System Load Percentage'
                Text(
                    text = "System Load Percentage",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Value and 1h Delta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${clampedLoad.roundToInt()}%",
                            color = animatedColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "avg",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // 1-Hour Delta Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isDeltaPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isDeltaPositive && hourDelta > 3f) StatusWarningAmber else TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (isDeltaPositive) "+" else ""}${String.format("%.1f", hourDelta)}% 1h",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Material 3 Progress Indicator
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = animatedColor,
                    trackColor = CyberNavyDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // RECHARTS / D3 1-HOUR LINE CHART SPARKLINE
                RechartsD3SystemLoadSparkline(
                    history = history,
                    lineColor = animatedColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberNavyDark.copy(alpha = 0.6f))
                        .border(0.5.dp, CyberNavyBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Timeframe axis legend: -60m to Now
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "-60m",
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "1h Recharts/D3 Line",
                        color = animatedColor.copy(alpha = 0.8f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Now",
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    // Interactive Detailed 1-Hour Performance Dialog
    if (showHistoryDialog) {
        SystemLoadHistoricalDialog(
            history = history,
            currentLoad = clampedLoad,
            lineColor = animatedColor,
            onDismiss = { showHistoryDialog = false }
        )
    }
}

/**
 * Recharts / D3-inspired Spline Line Chart Canvas (compact card sparkline).
 * Renders smooth cubic bezier curve, gradient area fill, and 80% critical warning line.
 */
@Composable
fun RechartsD3SystemLoadSparkline(
    history: List<SystemLoadHistoryPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .testTag("recharts_d3_system_load_sparkline")
    ) {
        if (history.size < 2) return@Canvas

        val width = size.width
        val height = size.height

        val paddingLeft = 4.dp.toPx()
        val paddingRight = 4.dp.toPx()
        val paddingTop = 4.dp.toPx()
        val paddingBottom = 4.dp.toPx()

        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom

        val values = history.map { it.loadPercentage }
        val maxScale = 100f

        fun getY(value: Float): Float {
            val clamped = value.coerceIn(0f, maxScale)
            return paddingTop + plotHeight * (1f - (clamped / maxScale))
        }

        val stepX = plotWidth / (values.size - 1)
        val points = values.mapIndexed { index, v ->
            Offset(paddingLeft + index * stepX, getY(v))
        }

        val baselineY = paddingTop + plotHeight

        // 1. Draw 80% critical threshold dashed line
        val thresholdY = getY(80f)
        drawLine(
            color = StatusRogueCrimson.copy(alpha = 0.35f),
            start = Offset(paddingLeft, thresholdY),
            end = Offset(width - paddingRight, thresholdY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )

        // 2. Smooth Cubic Bezier Area Path (Recharts monotone / D3 curveMonotoneX)
        val areaPath = Path().apply {
            moveTo(points.first().x, baselineY)
            lineTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val midX = (p0.x + p1.x) / 2f
                cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }
            lineTo(points.last().x, baselineY)
            close()
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.35f),
                    lineColor.copy(alpha = 0.02f)
                ),
                startY = paddingTop,
                endY = baselineY
            )
        )

        // 3. Smooth Cubic Bezier Line Path (Recharts / D3 stroke)
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val midX = (p0.x + p1.x) / 2f
                cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }
        }

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // 4. Draw data point dot at the latest reading (Now)
        val lastPoint = points.last()
        drawCircle(
            color = lineColor,
            radius = 2.5.dp.toPx(),
            center = lastPoint
        )
        drawCircle(
            color = CyberNavyCard,
            radius = 1.2.dp.toPx(),
            center = lastPoint
        )
    }
}

enum class SystemLoadChartStyle(val label: String) {
    RECHARTS_SPLINE("Recharts Spline"),
    D3_STEP_LINE("D3 Stepped Line")
}

/**
 * Interactive full 1-Hour Performance History Dialog with touch scrubbing,
 * tooltips, peak/min KPI metrics, and Recharts/D3 curve rendering.
 */
@Composable
fun SystemLoadHistoricalDialog(
    history: List<SystemLoadHistoryPoint>,
    currentLoad: Float,
    lineColor: Color,
    onDismiss: () -> Unit
) {
    var chartStyle by remember { mutableStateOf(SystemLoadChartStyle.RECHARTS_SPLINE) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val values = history.map { it.loadPercentage }
    val peakLoad = values.maxOrNull() ?: currentLoad
    val minLoad = values.minOrNull() ?: currentLoad
    val avgLoad = if (values.isNotEmpty()) values.average().toFloat() else currentLoad
    val initialLoad = values.firstOrNull() ?: currentLoad
    val delta1h = currentLoad - initialLoad

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .testTag("dialog_system_load_history"),
            color = CyberNavySurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(lineColor)
                            )
                            Text(
                                text = "SYSTEM LOAD: 1-HOUR PERFORMANCE",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }
                        Text(
                            text = "Recharts / D3 Spline Visualization • 5-min Intervals",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_close_system_load_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 4 KPI Summary Metric Tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Peak Load
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(if (peakLoad >= 80f) StatusRogueCrimson.copy(alpha = 0.5f) else CyberNavyBorder)
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("1h Peak", color = TextMuted, fontSize = 9.sp)
                            Text(
                                "${peakLoad.roundToInt()}%",
                                color = if (peakLoad >= 80f) StatusRogueCrimson else TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Low Load
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(CyberNavyBorder)
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("1h Low", color = TextMuted, fontSize = 9.sp)
                            Text(
                                "${minLoad.roundToInt()}%",
                                color = StatusOnlineGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Moving Avg
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(CyberNavyBorder)
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("60m Avg", color = TextMuted, fontSize = 9.sp)
                            Text(
                                "${avgLoad.roundToInt()}%",
                                color = CyberCyan,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Net Delta
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(CyberNavyBorder)
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("1h Delta", color = TextMuted, fontSize = 9.sp)
                            Text(
                                "${if (delta1h >= 0f) "+" else ""}${String.format("%.1f", delta1h)}%",
                                color = if (delta1h > 4f) StatusWarningAmber else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Style Selector Chips: Recharts Spline vs D3 Stepped Line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENGINE:",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    SystemLoadChartStyle.values().forEach { style ->
                        val isSelected = chartStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { chartStyle = style },
                            label = { Text(style.label, fontSize = 11.sp) },
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
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }

                // Interactive Canvas Chart with Touch Drag/Tap Scrubbing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberNavyDark)
                        .border(1.dp, CyberNavyBorder, RoundedCornerShape(10.dp))
                        .pointerInput(history) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val count = history.size
                                    if (count > 1) {
                                        val paddingLeft = 36.dp.toPx()
                                        val paddingRight = 16.dp.toPx()
                                        val plotWidth = size.width - paddingLeft - paddingRight
                                        val stepX = plotWidth / (count - 1)
                                        val clampedX = (offset.x - paddingLeft).coerceIn(0f, plotWidth)
                                        val idx = (clampedX / stepX).roundToInt().coerceIn(0, count - 1)
                                        selectedIndex = if (selectedIndex == idx) null else idx
                                    }
                                }
                            )
                        }
                        .pointerInput(history) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val count = history.size
                                    if (count > 1) {
                                        val paddingLeft = 36.dp.toPx()
                                        val paddingRight = 16.dp.toPx()
                                        val plotWidth = size.width - paddingLeft - paddingRight
                                        val stepX = plotWidth / (count - 1)
                                        val clampedX = (offset.x - paddingLeft).coerceIn(0f, plotWidth)
                                        selectedIndex = (clampedX / stepX).roundToInt().coerceIn(0, count - 1)
                                    }
                                },
                                onDrag = { change, _ ->
                                    val count = history.size
                                    if (count > 1) {
                                        val paddingLeft = 36.dp.toPx()
                                        val paddingRight = 16.dp.toPx()
                                        val plotWidth = size.width - paddingLeft - paddingRight
                                        val stepX = plotWidth / (count - 1)
                                        val clampedX = (change.position.x - paddingLeft).coerceIn(0f, plotWidth)
                                        selectedIndex = (clampedX / stepX).roundToInt().coerceIn(0, count - 1)
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        val paddingLeft = 36.dp.toPx()
                        val paddingRight = 16.dp.toPx()
                        val paddingTop = 16.dp.toPx()
                        val paddingBottom = 26.dp.toPx()

                        val plotWidth = width - paddingLeft - paddingRight
                        val plotHeight = height - paddingTop - paddingBottom
                        val count = history.size

                        if (count < 2) return@Canvas

                        val maxScale = 100f
                        fun getY(v: Float): Float {
                            val clamped = v.coerceIn(0f, maxScale)
                            return paddingTop + plotHeight * (1f - (clamped / maxScale))
                        }

                        val stepX = plotWidth / (count - 1)
                        val baselineY = paddingTop + plotHeight

                        // Horizontal Reference Grid Lines (0%, 25%, 50%, 75%, 100%)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        val gridLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
                        for (level in gridLevels) {
                            val y = paddingTop + plotHeight * (1f - level)
                            drawLine(
                                color = CyberNavyBorder.copy(alpha = 0.7f),
                                start = Offset(paddingLeft, y),
                                end = Offset(width - paddingRight, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashEffect
                            )
                        }

                        // 80% Critical Threshold Warning Line
                        val y80 = getY(80f)
                        drawLine(
                            color = StatusRogueCrimson.copy(alpha = 0.6f),
                            start = Offset(paddingLeft, y80),
                            end = Offset(width - paddingRight, y80),
                            strokeWidth = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )

                        val pts = history.mapIndexed { idx, item ->
                            Offset(paddingLeft + idx * stepX, getY(item.loadPercentage))
                        }

                        when (chartStyle) {
                            SystemLoadChartStyle.RECHARTS_SPLINE -> {
                                // Smooth Catmull-Rom / Monotone Spline
                                val areaPath = Path().apply {
                                    moveTo(pts.first().x, baselineY)
                                    lineTo(pts.first().x, pts.first().y)
                                    for (i in 0 until pts.size - 1) {
                                        val p0 = pts[i]
                                        val p1 = pts[i + 1]
                                        val midX = (p0.x + p1.x) / 2f
                                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                                    }
                                    lineTo(pts.last().x, baselineY)
                                    close()
                                }

                                drawPath(
                                    path = areaPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(lineColor.copy(alpha = 0.40f), lineColor.copy(alpha = 0.02f)),
                                        startY = paddingTop,
                                        endY = baselineY
                                    )
                                )

                                val linePath = Path().apply {
                                    moveTo(pts.first().x, pts.first().y)
                                    for (i in 0 until pts.size - 1) {
                                        val p0 = pts[i]
                                        val p1 = pts[i + 1]
                                        val midX = (p0.x + p1.x) / 2f
                                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                                    }
                                }

                                drawPath(
                                    path = linePath,
                                    color = lineColor,
                                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            SystemLoadChartStyle.D3_STEP_LINE -> {
                                // D3 Stepped Line
                                val stepPath = Path().apply {
                                    moveTo(pts.first().x, pts.first().y)
                                    for (i in 0 until pts.size - 1) {
                                        val p0 = pts[i]
                                        val p1 = pts[i + 1]
                                        lineTo(p1.x, p0.y)
                                        lineTo(p1.x, p1.y)
                                    }
                                }

                                drawPath(
                                    path = stepPath,
                                    color = lineColor,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }

                        // Draw data point circles
                        pts.forEachIndexed { i, pt ->
                            val isSelected = selectedIndex == i
                            drawCircle(
                                color = if (isSelected) Color.White else lineColor,
                                radius = if (isSelected) 4.5.dp.toPx() else 2.5.dp.toPx(),
                                center = pt
                            )
                        }

                        // Selected point guideline and crosshair
                        selectedIndex?.let { idx ->
                            val pt = pts[idx]
                            drawLine(
                                color = Color.White.copy(alpha = 0.8f),
                                start = Offset(pt.x, paddingTop),
                                end = Offset(pt.x, baselineY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            )
                        }
                    }

                    // Floating Tooltip when scrubbed
                    selectedIndex?.let { idx ->
                        val point = history.getOrNull(idx)
                        if (point != null) {
                            val statusStr = when {
                                point.loadPercentage >= 80f -> "Heavy Load"
                                point.loadPercentage >= 60f -> "Elevated"
                                else -> "Nominal"
                            }
                            val statusColor = when {
                                point.loadPercentage >= 80f -> StatusRogueCrimson
                                point.loadPercentage >= 60f -> StatusWarningAmber
                                else -> CyberCyan
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberNavySurface.copy(alpha = 0.95f))
                                    .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "TIME: ${point.timestampLabel}",
                                            color = TextSecondary,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        IconButton(
                                            onClick = { selectedIndex = null },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss tooltip",
                                                tint = TextMuted,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Load: ${String.format("%.1f", point.loadPercentage)}%",
                                        color = statusColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Status: $statusStr",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // X-Axis Time Labels (-60m, -45m, -30m, -15m, Now)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("-60m", "-45m", "-30m", "-15m", "Now").forEach { label ->
                        Text(
                            text = label,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Footer with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Done",
                            color = CyberNavyDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
