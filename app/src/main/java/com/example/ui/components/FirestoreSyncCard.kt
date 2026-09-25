package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirestoreSyncManager
import com.example.data.firebase.FirestoreSyncStatus
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun FirestoreSyncCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val syncStatus by viewModel.firestoreSyncStatus.collectAsState()
    val lastSyncTime by viewModel.firestoreLastSyncTimestamp.collectAsState()
    val lastReport by viewModel.firestoreLastReport.collectAsState()
    val isRealtimeEnabled by viewModel.isFirestoreRealtimeEnabled.collectAsState()

    val devices by viewModel.devices.collectAsState()
    val rogues by viewModel.rogueDevices.collectAsState()
    val events by viewModel.networkEvents.collectAsState()
    val remediations by viewModel.remediations.collectAsState()

    var showSchemaDialog by remember { mutableStateOf(false) }

    val statusColor = when (syncStatus) {
        FirestoreSyncStatus.SYNCED -> StatusOnlineGreen
        FirestoreSyncStatus.SYNCING -> CyberCyan
        FirestoreSyncStatus.OFFLINE_PERSISTENCE -> CyberBlueLight
        FirestoreSyncStatus.ERROR -> StatusRogueCrimson
        else -> StatusWarningAmber
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("firestore_sync_card")
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFA000).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFFFA000).copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Firestore Cloud Sync",
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Firebase Cloud Firestore",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFA000).copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "Cross-Session",
                                    color = Color(0xFFFFA000),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "State & Event Log Persistence • مزامنة سحابية مستمرة",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
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
                            text = syncStatus.displayName,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Sync Scope Summary Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FirestoreScopeMetricBadge(
                    label = "Devices",
                    count = devices.size,
                    color = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
                FirestoreScopeMetricBadge(
                    label = "Rogues",
                    count = rogues.size,
                    color = StatusRogueCrimson,
                    modifier = Modifier.weight(1f)
                )
                FirestoreScopeMetricBadge(
                    label = "Events Log",
                    count = events.size,
                    color = CyberBlueLight,
                    modifier = Modifier.weight(1f)
                )
                FirestoreScopeMetricBadge(
                    label = "Remediations",
                    count = remediations.size,
                    color = StatusOnlineGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // Real-time synchronization toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberNavyDark)
                    .border(1.dp, CyberNavyBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = if (isRealtimeEnabled) CyberCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = "Real-Time Snapshot Listeners",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isRealtimeEnabled) "Syncs updates instantly across sessions" else "Real-time sync paused",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Switch(
                    checked = isRealtimeEnabled,
                    onCheckedChange = { viewModel.toggleFirestoreRealtimeSync(it) },
                    modifier = Modifier.testTag("firestore_realtime_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberNavyDark,
                        checkedTrackColor = CyberCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CyberNavyBorder
                    )
                )
            }

            // Sync Information & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Last Synced: ${lastSyncTime ?: "Pending first run"}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (lastReport != null) {
                        Text(
                            text = lastReport!!.message,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Schema details button
                    IconButton(
                        onClick = { showSchemaDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("firestore_schema_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Firestore Schema Details",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Restore (Pull) button
                    OutlinedButton(
                        onClick = { viewModel.restoreFromFirestore() },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("firestore_restore_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CyberCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Restore", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Push (Sync) button
                    Button(
                        onClick = { viewModel.syncWithFirestore() },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("firestore_sync_now_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA000)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sync Now",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Schema Details Dialog
    if (showSchemaDialog) {
        AlertDialog(
            onDismissRequest = { showSchemaDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = Color(0xFFFFA000))
                    Text("Cloud Firestore Architecture", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Data persisted in Firestore collections ensures enterprise state is preserved across app closures, background task execution, and multi-session audits:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    FirestoreCollectionSpecRow(
                        name = FirestoreSyncManager.COLLECTION_DEVICES,
                        desc = "Stores active inventory, IP/MAC bindings, OS, ports, bandwidth usage.",
                        count = devices.size
                    )
                    FirestoreCollectionSpecRow(
                        name = FirestoreSyncManager.COLLECTION_ROGUE_DEVICES,
                        desc = "Tracks rogue hardware, isolation status, and threat assessments.",
                        count = rogues.size
                    )
                    FirestoreCollectionSpecRow(
                        name = FirestoreSyncManager.COLLECTION_NETWORK_EVENTS,
                        desc = "Logs discovery, connectivity changes, and remediation alerts.",
                        count = events.size
                    )
                    FirestoreCollectionSpecRow(
                        name = FirestoreSyncManager.COLLECTION_REMEDIATIONS,
                        desc = "Audit logs for automated and manual SSH commands and status.",
                        count = remediations.size
                    )
                    FirestoreCollectionSpecRow(
                        name = FirestoreSyncManager.COLLECTION_SYSTEM_METADATA,
                        desc = "Cluster synchronization heartbeat and health aggregates.",
                        count = 1
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSchemaDialog = false }) {
                    Text("Close", color = CyberCyan)
                }
            },
            containerColor = CyberNavyCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun FirestoreScopeMetricBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberNavyDark)
            .border(1.dp, CyberNavyBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FirestoreCollectionSpecRow(
    name: String,
    desc: String,
    count: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CyberNavyDark)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "$count docs",
                color = Color(0xFFFFA000),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}
