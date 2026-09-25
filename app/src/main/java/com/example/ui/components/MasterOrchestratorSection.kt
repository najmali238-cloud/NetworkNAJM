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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MasterOrchestratorServer
import com.example.ui.theme.NetGuardThemeColors

@Composable
fun MasterOrchestratorSection(
    orchestrator: MasterOrchestratorServer,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onTriggerRebalance: () -> Unit,
    onReorderTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("master_orchestrator_card"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Server Header
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.primaryAccent.copy(alpha = 0.2f))
                            .border(1.5.dp, colors.primaryAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Master Orchestrator",
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isArabic) orchestrator.name else orchestrator.englishName,
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.statusGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "يعمل بسلاسة 100%" else "OPTIMAL 100%",
                                    color = colors.statusGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "ID: ${orchestrator.id} | IP: ${orchestrator.ip} | Uptime: ${orchestrator.uptime}",
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Explanation Banner: يعمل وينفذ ويتتبع ويرتب ويجهز ويسير الخدمة بكل سلاسة
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = if (isArabic) "وظائف ومهام خادم المايسترو والتشغيل الذاتي المركزي:"
                            else "Autonomous Master Orchestrator Operational Capabilities:",
                        color = colors.primaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // 6 Pillars of the Orchestrator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CapabilityBadge("يعمل", "Run 24/7", Icons.Default.PlayArrow, colors, Modifier.weight(1f))
                        CapabilityBadge("ينفذ", "Execute", Icons.Default.FlashOn, colors, Modifier.weight(1f))
                        CapabilityBadge("يتتبع", "Track", Icons.Default.Timeline, colors, Modifier.weight(1f))
                        CapabilityBadge("يرتب", "Order", Icons.Default.Sort, colors, Modifier.weight(1f))
                        CapabilityBadge("يجهز", "Prepare", Icons.Default.Build, colors, Modifier.weight(1f))
                        CapabilityBadge("يسير بسلاسة", "Smooth Ops", Icons.Default.Autorenew, colors, Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Realtime Dials
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrchestratorMetricBox(
                    label = if (isArabic) "معالجة CPU" else "CPU Load",
                    value = "${orchestrator.cpuUsagePercent}%",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                OrchestratorMetricBox(
                    label = if (isArabic) "ذاكرة RAM" else "RAM Load",
                    value = "${orchestrator.ramUsagePercent}%",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                OrchestratorMetricBox(
                    label = if (isArabic) "خيوط التشغيل" else "Threads",
                    value = "${orchestrator.activeThreads}",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                OrchestratorMetricBox(
                    label = if (isArabic) "مهام منفذة" else "Tasks Done",
                    value = "${orchestrator.totalTasksExecuted}",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTriggerRebalance,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("orchestrator_rebalance_btn")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "تسيير وموازنة المنظومة" else "Rebalance & Smooth Ops",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onReorderTasks,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("orchestrator_reorder_btn")
                ) {
                    Icon(Icons.Default.Reorder, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "إعادة ترتيب وجدولة الطابور" else "Reorder Task Queue",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Recent Orchestration Tasks Stream
            if (orchestrator.recentTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isArabic) "طابور وسجل مهام المايسترو المنفذة لحظياً:" else "Live Master Orchestrator Task Execution Stream:",
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    orchestrator.recentTasks.take(4).forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surface)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.primaryAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = task.category,
                                        color = colors.primaryAccent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (isArabic) task.titleArabic else task.titleEnglish,
                                    color = colors.textPrimary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }

                            Text(
                                text = "${task.durationMs}ms ✓",
                                color = colors.statusGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityBadge(
    arabicLabel: String,
    englishLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.cardBackground)
            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primaryAccent,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = arabicLabel,
                color = colors.textPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OrchestratorMetricBox(
    label: String,
    value: String,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(6.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, color = colors.primaryAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = colors.textSecondary, fontSize = 9.sp)
        }
    }
}
