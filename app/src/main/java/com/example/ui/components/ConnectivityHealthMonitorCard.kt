package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.theme.getNetGuardColors

/**
 * Dedicated Connectivity Health Monitor Component
 *
 * Checks if the device and local network can reach common enterprise endpoints:
 * 1. Enterprise Core Gateway & Routing (192.168.1.1:80)
 * 2. Enterprise DNS Resolver (1.1.1.1:53)
 * 3. NetGuard Security Cloud & Telemetry (netguard.enterprise.security:443)
 * 4. Active Directory & Kerberos Authentication (192.168.1.10:389)
 * 5. Enterprise SIEM & SOC Collector (192.168.1.15:514)
 * 6. Public WAN Edge Anycast DNS (8.8.8.8:53)
 *
 * Displays a prominent status indicator on the dashboard with real-time reachability,
 * measured latency, protocol breakdown, and manual probe triggering.
 */
@Composable
fun ConnectivityHealthMonitorCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }

    val summary by viewModel.connectivityHealthSummary.collectAsState()
    val isChecking by viewModel.isCheckingConnectivity.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }
    var selectedEndpointForDetail by remember { mutableStateOf<EndpointHealthResult?>(null) }

    // Pulsing animation for the active connectivity status indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "connectivity_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Rotation animation for probe button when actively checking
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val overallColor = when (summary.overallStatus) {
        ConnectivityOverallStatus.OPTIMAL -> colors.statusGreen
        ConnectivityOverallStatus.DEGRADED -> colors.statusAmber
        ConnectivityOverallStatus.CRITICAL_OUTAGE -> colors.statusCrimson
        ConnectivityOverallStatus.CHECKING -> colors.primaryAccent
    }

    val overallStatusText = if (isArabic) {
        summary.overallStatus.labelArabic
    } else {
        summary.overallStatus.labelEnglish
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("connectivity_health_monitor_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(overallColor.copy(alpha = 0.45f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Title, Glowing Status Indicator Badge & Action
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
                            .clip(CircleShape)
                            .background(overallColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Connectivity Health Monitor",
                            tint = overallColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isArabic) "مراقب اتصال نقاط النهاية المؤسسية" else "Enterprise Connectivity Monitor",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            // Glowing status indicator dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(overallColor.copy(alpha = pulseAlpha))
                                    .testTag("connectivity_pulse_indicator")
                            )
                        }

                        Text(
                            text = if (isArabic) "فحص الوصول المباشر للخوادم والبوابات الحساسة" else "Direct reachability monitor for critical servers",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                // Status Indicator Pill (Overall Status)
                Surface(
                    color = overallColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(overallColor.copy(alpha = 0.5f))
                    ),
                    modifier = Modifier.testTag("connectivity_status_indicator")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(overallColor)
                        )
                        Text(
                            text = overallStatusText,
                            color = overallColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Summary Metrics Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric 1: Reachability Score
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = if (isArabic) "النقاط المتاحة" else "Reachable Endpoints",
                        fontSize = 10.5.sp,
                        color = colors.textSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${summary.reachableCount}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = overallColor
                        )
                        Text(
                            text = "/${summary.totalEndpoints}",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                // Metric 2: Average Latency
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isArabic) "متوسط زمن الاستجابة" else "Avg Latency",
                        fontSize = 10.5.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "${summary.averageLatencyMs} ms",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (summary.averageLatencyMs < 40) colors.statusGreen else colors.statusAmber
                    )
                }

                // Metric 3: Last Checked Time
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isArabic) "آخر فحص" else "Last Probed",
                        fontSize = 10.5.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = summary.lastCheckedTimestamp,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textPrimary
                    )
                }
            }

            // Progress bar showing reachability ratio
            val reachabilityRatio = if (summary.totalEndpoints > 0) {
                summary.reachableCount.toFloat() / summary.totalEndpoints.toFloat()
            } else 1.0f

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "نسبة جاهزية المسارات المؤسسية" else "Enterprise Route Availability",
                        fontSize = 10.5.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "${(reachabilityRatio * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = overallColor
                    )
                }
                LinearProgressIndicator(
                    progress = { reachabilityRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = overallColor,
                    trackColor = overallColor.copy(alpha = 0.15f)
                )
            }

            // Endpoints Quick Grid Preview (Compact chip row)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                summary.endpoints.take(if (isExpanded) summary.endpoints.size else 3).forEach { result ->
                    EndpointStatusRowItem(
                        result = result,
                        isArabic = isArabic,
                        colors = colors,
                        onClick = { selectedEndpointForDetail = result }
                    )
                }
            }

            // Controls: Expand/Collapse & Manual Trigger Probe Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExpanded) {
                            if (isArabic) "عرض أقل" else "Show Less"
                        } else {
                            if (isArabic) "عرض جميع النقاط (${summary.endpoints.size})" else "View All (${summary.endpoints.size})"
                        },
                        fontSize = 11.5.sp,
                        color = colors.primaryAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { viewModel.triggerConnectivityHealthCheck() },
                    enabled = !isChecking,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = if (isDarkMode) Color.Black else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("connectivity_probe_now_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Trigger Connectivity Probe",
                        modifier = Modifier
                            .size(16.dp)
                            .then(if (isChecking) Modifier.rotate(rotationAngle) else Modifier),
                        tint = if (isDarkMode) Color.Black else Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isChecking) {
                            if (isArabic) "جاري الفحص..." else "Probing..."
                        } else {
                            if (isArabic) "فحص المسارات الآن" else "Probe Now"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Detail Dialog for an individual endpoint
    selectedEndpointForDetail?.let { detail ->
        EndpointDetailDialog(
            result = detail,
            isArabic = isArabic,
            colors = colors,
            onDismiss = { selectedEndpointForDetail = null }
        )
    }
}

/**
 * Single Endpoint Row Item inside the Connectivity Health Monitor
 */
@Composable
private fun EndpointStatusRowItem(
    result: EndpointHealthResult,
    isArabic: Boolean,
    colors: com.example.ui.theme.NetGuardThemeColors,
    onClick: () -> Unit
) {
    val endpoint = result.endpoint
    val statusColor = when (result.status) {
        EndpointStatus.REACHABLE -> colors.statusGreen
        EndpointStatus.DEGRADED -> colors.statusAmber
        EndpointStatus.UNREACHABLE -> colors.statusCrimson
        EndpointStatus.CHECKING -> colors.primaryAccent
    }

    val icon: ImageVector = when (endpoint.category) {
        EndpointCategory.GATEWAY -> Icons.Default.Router
        EndpointCategory.DNS -> Icons.Default.Dns
        EndpointCategory.SECURITY_CLOUD -> Icons.Default.CloudQueue
        EndpointCategory.IDENTITY -> Icons.Default.Badge
        EndpointCategory.SIEM -> Icons.Default.Shield
        EndpointCategory.PUBLIC_WAN -> Icons.Default.Public
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.surface.copy(alpha = 0.6f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(statusColor.copy(alpha = 0.25f))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("endpoint_item_${endpoint.id}")
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = endpoint.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        if (endpoint.isCritical) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colors.statusCrimson.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (isArabic) "حرج" else "CRITICAL",
                                    color = colors.statusCrimson,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${endpoint.host}:${endpoint.port} • ${endpoint.protocol}",
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Latency & Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${result.latencyMs}ms",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = statusColor
                )

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isArabic) result.status.labelArabic else result.status.labelEnglish,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Detailed Inspector Dialog for a single Enterprise Endpoint
 */
@Composable
private fun EndpointDetailDialog(
    result: EndpointHealthResult,
    isArabic: Boolean,
    colors: com.example.ui.theme.NetGuardThemeColors,
    onDismiss: () -> Unit
) {
    val endpoint = result.endpoint
    val statusColor = when (result.status) {
        EndpointStatus.REACHABLE -> colors.statusGreen
        EndpointStatus.DEGRADED -> colors.statusAmber
        EndpointStatus.UNREACHABLE -> colors.statusCrimson
        EndpointStatus.CHECKING -> colors.primaryAccent
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = endpoint.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = endpoint.description,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )

                HorizontalDivider(color = colors.cardBorder)

                // Key Value Specs
                DetailSpecRow(
                    label = if (isArabic) "العنوان والمنفذ" else "Host & Port",
                    value = "${endpoint.host}:${endpoint.port}",
                    colors = colors,
                    isMono = true
                )
                DetailSpecRow(
                    label = if (isArabic) "التصنيف المؤسسي" else "Category",
                    value = if (isArabic) endpoint.category.labelArabic else endpoint.category.labelEnglish,
                    colors = colors
                )
                DetailSpecRow(
                    label = if (isArabic) "البروتوكول" else "Protocol",
                    value = endpoint.protocol,
                    colors = colors,
                    isMono = true
                )
                DetailSpecRow(
                    label = if (isArabic) "زمن الاستجابة" else "Latency",
                    value = "${result.latencyMs} ms",
                    colors = colors,
                    valueColor = statusColor,
                    isMono = true
                )
                DetailSpecRow(
                    label = if (isArabic) "استجابة المقبس (Socket ACK)" else "Socket ACK Response",
                    value = result.responseDetails,
                    colors = colors
                )
                DetailSpecRow(
                    label = if (isArabic) "آخر فحص مباشر" else "Last Probe Time",
                    value = result.lastCheckedTimestamp,
                    colors = colors,
                    isMono = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isArabic) "إغلاق" else "Close",
                    color = colors.primaryAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = colors.cardBackground,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DetailSpecRow(
    label: String,
    value: String,
    colors: com.example.ui.theme.NetGuardThemeColors,
    valueColor: Color = colors.textPrimary,
    isMono: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = colors.textSecondary
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default
        )
    }
}

/**
 * Compact Connectivity Status Indicator Chip for headers or status bars
 */
@Composable
fun ConnectivityStatusIndicatorChip(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }
    val summary by viewModel.connectivityHealthSummary.collectAsState()

    val statusColor = when (summary.overallStatus) {
        ConnectivityOverallStatus.OPTIMAL -> colors.statusGreen
        ConnectivityOverallStatus.DEGRADED -> colors.statusAmber
        ConnectivityOverallStatus.CRITICAL_OUTAGE -> colors.statusCrimson
        ConnectivityOverallStatus.CHECKING -> colors.primaryAccent
    }

    Surface(
        color = statusColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(statusColor.copy(alpha = 0.4f))
        ),
        modifier = modifier.testTag("compact_connectivity_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                text = "${summary.reachableCount}/${summary.totalEndpoints} " +
                        (if (isArabic) "نقاط متصلة" else "Endpoints OK"),
                color = statusColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
