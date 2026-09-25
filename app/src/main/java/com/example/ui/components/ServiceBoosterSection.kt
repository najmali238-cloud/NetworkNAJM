package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceBoosterState
import com.example.ui.theme.NetGuardThemeColors

@Composable
fun ServiceBoosterSection(
    boosterState: ServiceBoosterState,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onToggleBooster: () -> Unit,
    onSetBoostLevel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("service_booster_card"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (boosterState.isBoostActive) colors.primaryAccent else colors.cardBorder
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Switch
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (boosterState.isBoostActive) colors.primaryAccent.copy(alpha = 0.2f)
                                else colors.surface
                            )
                            .border(
                                1.5.dp,
                                if (boosterState.isBoostActive) colors.primaryAccent else colors.cardBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Turbo Booster",
                            tint = if (boosterState.isBoostActive) Color(0xFFF59E0B) else colors.textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isArabic) "أداة تعزيز وتقوية أداء الخدمة (Turbo Booster)" else "NetGuard Service Performance Booster",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (boosterState.isBoostActive) colors.statusGreen.copy(alpha = 0.2f)
                                        else colors.surface
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (boosterState.isBoostActive) (if (isArabic) "مُفَعّل ⚡" else "ACTIVE ⚡")
                                        else (if (isArabic) "معطل" else "OFF"),
                                    color = if (boosterState.isBoostActive) colors.statusGreen else colors.textMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (isArabic) "تسريع تدفق البيانات وتقليل التأخير وتطبيق خوارزمية TCP BBR" else "Kernel acceleration, latency reduction & dynamic packet compression",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = boosterState.isBoostActive,
                    onCheckedChange = { onToggleBooster() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.primaryAccent,
                        uncheckedThumbColor = colors.textMuted,
                        uncheckedTrackColor = colors.surface
                    ),
                    modifier = Modifier.testTag("booster_toggle_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Throughput Boost
                BoosterMetricChip(
                    title = if (isArabic) "تسريع التدفق" else "Throughput",
                    value = if (boosterState.isBoostActive) "+${((boosterState.throughputMultiplier - 1f) * 100).toInt()}%" else "1.0x",
                    icon = Icons.Default.Speed,
                    colors = colors,
                    isActive = boosterState.isBoostActive,
                    modifier = Modifier.weight(1f)
                )

                // Metric 2: Latency Cut
                BoosterMetricChip(
                    title = if (isArabic) "خفض التأخير" else "Latency Cut",
                    value = if (boosterState.isBoostActive) "-${boosterState.latencyReductionMs}ms" else "0ms",
                    icon = Icons.Default.Timer,
                    colors = colors,
                    isActive = boosterState.isBoostActive,
                    modifier = Modifier.weight(1f)
                )

                // Metric 3: Compression
                BoosterMetricChip(
                    title = if (isArabic) "نسبة الضغط" else "Compression",
                    value = "${boosterState.packetCompressionRatio}x",
                    icon = Icons.Default.Compress,
                    colors = colors,
                    isActive = boosterState.isBoostActive,
                    modifier = Modifier.weight(1f)
                )

                // Metric 4: Optimization Engine
                BoosterMetricChip(
                    title = if (isArabic) "بروتوكول النواة" else "Kernel QoS",
                    value = "TCP BBR",
                    icon = Icons.Default.Memory,
                    colors = colors,
                    isActive = boosterState.isBoostActive,
                    modifier = Modifier.weight(1f)
                )
            }

            // Level Selector Chips
            if (boosterState.isBoostActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "مستوى التعزيز:" else "Boost Level:",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    listOf(35, 50, 75, 100).forEach { level ->
                        val selected = boosterState.boostLevelPercent == level
                        FilterChip(
                            selected = selected,
                            onClick = { onSetBoostLevel(level) },
                            label = {
                                Text(
                                    text = "$level%",
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = colors.primaryAccent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = colors.primaryAccent
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoosterMetricChip(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: NetGuardThemeColors,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(
                1.dp,
                if (isActive) colors.primaryAccent.copy(alpha = 0.3f) else colors.cardBorder,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) colors.primaryAccent else colors.textMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = if (isActive) colors.primaryAccent else colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = colors.textSecondary,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
