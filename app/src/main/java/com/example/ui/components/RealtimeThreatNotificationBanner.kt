package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.AppLanguage
import com.example.data.model.RealtimeThreatNotification
import com.example.ui.MainViewModel
import com.example.ui.theme.getNetGuardColors

@Composable
fun RealtimeThreatNotificationBanner(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onNavigateToThreats: () -> Unit = {}
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val notifications by viewModel.realtimeThreatNotifications.collectAsState()
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }
    val isArabic = appLanguage == AppLanguage.ARABIC

    val activeThreats = notifications.filter { !it.isDismissed }
    val currentThreat = activeThreats.firstOrNull()

    var showHistoryDialog by remember { mutableStateOf(false) }

    // Pulsing glow animation for active critical alert
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = currentThreat != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            currentThreat?.let { threat ->
                val isCritical = threat.severity == AlertSeverity.CRITICAL
                val bannerBorderColor = if (isCritical) colors.statusCrimson else colors.statusAmber
                val bannerBg = if (isDarkMode) {
                    if (isCritical) Color(0xFF2A0812) else Color(0xFF2A1C08)
                } else {
                    if (isCritical) Color(0xFFFFF1F2) else Color(0xFFFFFBEB)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("realtime_threat_banner"),
                    colors = CardDefaults.cardColors(containerColor = bannerBg),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(bannerBorderColor.copy(alpha = alphaAnim))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Header row with animated radar indicator and badge
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
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(bannerBorderColor.copy(alpha = alphaAnim))
                                )

                                Icon(
                                    imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = bannerBorderColor,
                                    modifier = Modifier.size(18.dp)
                                )

                                Text(
                                    text = if (isArabic) "إنذار أمني فوري لحظي" else "REAL-TIME THREAT DETECTED",
                                    color = bannerBorderColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(bannerBorderColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = threat.severity.name,
                                        color = bannerBorderColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = threat.timestamp,
                                    color = colors.textMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Threat title & details
                        Text(
                            text = if (isArabic) threat.titleArabic else threat.title,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isArabic) threat.descriptionArabic else threat.description,
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // IP and MAC chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.cardBackground)
                                    .border(1.dp, colors.cardBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "IP: ${threat.ipAddress}",
                                    color = colors.primaryAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.cardBackground)
                                    .border(1.dp, colors.cardBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "MAC: ${threat.macAddress}",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Isolate Action
                            if (threat.rogueId != null) {
                                Button(
                                    onClick = {
                                        viewModel.isolateRogue(threat.rogueId)
                                        viewModel.dismissThreatNotification(threat.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.statusCrimson,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f).testTag("isolate_rogue_btn")
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "عزل فوري" else "Isolate Now",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.blockAttackerIp(threat.ipAddress)
                                        viewModel.dismissThreatNotification(threat.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.statusCrimson,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f).testTag("block_ip_btn")
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "حظر IP فوراً" else "Block IP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Inspect Action
                            OutlinedButton(
                                onClick = onNavigateToThreats,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
                                ),
                                modifier = Modifier.weight(1f).testTag("inspect_threat_btn")
                            ) {
                                Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "تتبع وتحليل" else "Trace & Inspect",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Dismiss Action
                            IconButton(
                                onClick = { viewModel.dismissThreatNotification(threat.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("dismiss_threat_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick threat controls bar: Simulation button and History drawer button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showHistoryDialog = true }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = if (activeThreats.isNotEmpty()) colors.statusCrimson else colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isArabic) "سجل التنبيهات الفورية (${notifications.size})" else "Live Alert Feed (${notifications.size})",
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Simulate threat button
            TextButton(
                onClick = { viewModel.simulateNewThreat() },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("simulate_threat_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AddAlert,
                    contentDescription = null,
                    tint = colors.primaryAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isArabic) "+ محاكاة هجوم فوري" else "+ Simulate Attack",
                    color = colors.primaryAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = colors.primaryAccent)
                    Text(
                        text = if (isArabic) "سجل التنبيهات الأمنية الفورية" else "Real-time Threat Alerts History",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (notifications.isEmpty()) {
                        item {
                            Text(
                                text = if (isArabic) "لا توجد تنبيهات أمنية حالية." else "No security threat notifications logged.",
                                color = colors.textMuted,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        items(notifications) { n ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isArabic) n.titleArabic else n.title,
                                            color = colors.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = n.timestamp,
                                            color = colors.textMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isArabic) n.descriptionArabic else n.description,
                                        color = colors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "IP: ${n.ipAddress} | MAC: ${n.macAddress}",
                                        color = colors.primaryAccent,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHistoryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent, contentColor = Color.White)
                ) {
                    Text(if (isArabic) "إغلاق" else "Close")
                }
            },
            containerColor = colors.cardBackground
        )
    }
}
