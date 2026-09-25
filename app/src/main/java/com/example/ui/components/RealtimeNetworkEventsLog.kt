package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AlertSeverity
import com.example.data.model.NetworkEventLog
import com.example.data.model.NetworkEventType
import com.example.data.model.RemediationEventDetails
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanLight
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.StatusOnlineGreen
import com.example.ui.theme.StatusRogueCrimson
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Scrollable list component for the main dashboard that logs real-time network events,
 * such as new device discovery, connectivity changes, and remediation alerts (تنبيهات المعالجة).
 */
@Composable
fun RealtimeNetworkEventsLogCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.networkEvents.collectAsState()
    var selectedFilter by remember { mutableStateOf(NetworkEventType.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isLiveStreamPaused by remember { mutableStateOf(false) }
    var selectedEventForDetails by remember { mutableStateOf<NetworkEventLog?>(null) }
    var showSimulateMenu by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Pulse animation for real-time live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_live")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Filter events by selected category and search query
    val filteredEvents = remember(events, selectedFilter, searchQuery) {
        events.filter { event ->
            val matchesCategory = selectedFilter == NetworkEventType.ALL || event.eventType == selectedFilter
            val matchesSearch = searchQuery.isBlank() ||
                    event.title.contains(searchQuery, ignoreCase = true) ||
                    event.description.contains(searchQuery, ignoreCase = true) ||
                    (event.ipAddress?.contains(searchQuery, ignoreCase = true) == true) ||
                    (event.sourceDevice?.contains(searchQuery, ignoreCase = true) == true) ||
                    (event.remediationDetails?.targetService?.contains(searchQuery, ignoreCase = true) == true)
            matchesCategory && matchesSearch
        }
    }

    // Active/unacknowledged remediation alerts count
    val remediationAlerts = remember(events) {
        events.filter { it.eventType == NetworkEventType.REMEDIATION_ALERT }
    }
    val unacknowledgedRemediations = remember(remediationAlerts) {
        remediationAlerts.filter { !it.isAcknowledged }
    }
    val latestRemediation = unacknowledgedRemediations.firstOrNull() ?: remediationAlerts.firstOrNull()

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_realtime_network_events_log"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = CyberNavyCard
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Live Indicator, Title, Count Badges & Control Actions
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
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Real-Time Network Events",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Live Pulse Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isLiveStreamPaused) TextMuted.copy(alpha = 0.2f)
                                        else StatusOnlineGreen.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isLiveStreamPaused) TextMuted
                                                else StatusOnlineGreen.copy(alpha = pulseAlpha)
                                            )
                                    )
                                    Text(
                                        text = if (isLiveStreamPaused) "PAUSED" else "LIVE",
                                        color = if (isLiveStreamPaused) TextMuted else StatusOnlineGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Subtitle with Arabic translation for remediation alerts
                        Text(
                            text = "سجل الأحداث الفوري • تنبيهات المعالجة والاكتشاف",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Quick Header Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Pause / Resume Stream
                    IconButton(
                        onClick = { isLiveStreamPaused = !isLiveStreamPaused },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_toggle_events_stream_pause")
                    ) {
                        Icon(
                            imageVector = if (isLiveStreamPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isLiveStreamPaused) "Resume Live Stream" else "Pause Live Stream",
                            tint = if (isLiveStreamPaused) StatusWarningAmber else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Simulate Live Event Button
                    IconButton(
                        onClick = { showSimulateMenu = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_simulate_network_event")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Simulate Network Event",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Clear Events
                    IconButton(
                        onClick = { viewModel.clearAllNetworkEvents() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_clear_network_events")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All Events",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // REMEDIATION ALERTS BANNER (تنبيهات المعالجة البارزة)
            if (latestRemediation != null) {
                RemediationAlertNotificationBanner(
                    event = latestRemediation,
                    activeRemediationsCount = unacknowledgedRemediations.size,
                    onInspectClick = { selectedEventForDetails = latestRemediation },
                    onAcknowledgeClick = { viewModel.acknowledgeNetworkEvent(latestRemediation.id) },
                    onReExecuteClick = { viewModel.reExecuteRemediationFromAlert(latestRemediation) },
                    onNavigateToRemediationTab = { viewModel.selectTab(AppTab.REMEDIATION) }
                )
            }

            // Filter Chips Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Filter Chips
                val categories = listOf(
                    NetworkEventType.ALL to "${events.size}",
                    NetworkEventType.REMEDIATION_ALERT to "${remediationAlerts.size}",
                    NetworkEventType.DEVICE_DISCOVERY to "${events.count { it.eventType == NetworkEventType.DEVICE_DISCOVERY }}",
                    NetworkEventType.CONNECTIVITY_CHANGE to "${events.count { it.eventType == NetworkEventType.CONNECTIVITY_CHANGE }}",
                    NetworkEventType.SECURITY_ANOMALY to "${events.count { it.eventType == NetworkEventType.SECURITY_ANOMALY }}"
                )

                categories.forEach { (cat, count) ->
                    val isSelected = selectedFilter == cat
                    val isRemediationCat = cat == NetworkEventType.REMEDIATION_ALERT

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = cat },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isRemediationCat && unacknowledgedRemediations.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(StatusWarningAmber)
                                    )
                                }
                                Text(
                                    text = if (isRemediationCat) "تنبيهات المعالجة (${count})" else "${cat.displayName} (${count})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isRemediationCat) StatusWarningAmber.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = if (isRemediationCat) StatusWarningAmber else CyberCyan,
                            containerColor = CyberNavyDark,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) (if (isRemediationCat) StatusWarningAmber else CyberCyan) else CyberNavyBorder
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("filter_chip_${cat.name.lowercase()}")
                    )
                }
            }

            // Quick Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search events by host, IP, or service...",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_network_events"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberNavyBorder,
                    focusedContainerColor = CyberNavyDark,
                    unfocusedContainerColor = CyberNavyDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // SCROLLABLE EVENT STREAM LIST
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberNavyDark)
                        .border(1.dp, CyberNavyBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No events matching '$searchQuery'" else "No network events recorded",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Button(
                            onClick = { viewModel.simulateNetworkEvent() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Simulate Live Event", color = CyberCyan, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .testTag("network_events_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredEvents,
                        key = { it.id }
                    ) { event ->
                        NetworkEventItemCard(
                            event = event,
                            onClick = { selectedEventForDetails = event },
                            onAcknowledge = { viewModel.acknowledgeNetworkEvent(event.id) },
                            onDismiss = { viewModel.dismissNetworkEvent(event.id) },
                            onReExecuteRemediation = { viewModel.reExecuteRemediationFromAlert(event) },
                            onPingDevice = {
                                if (event.ipAddress != null) {
                                    viewModel.pingServer(event.sourceDevice ?: "target", event.sourceDevice ?: event.ipAddress)
                                }
                            }
                        )
                    }
                }
            }

            // Footer Status & Quick Action Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredEvents.size} of ${events.size} telemetry records",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.selectTab(AppTab.REMEDIATION) },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCyan.copy(alpha = 0.5f))),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SSH Remediation Tab", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Interactive Full Event Details Dialog
    selectedEventForDetails?.let { event ->
        NetworkEventDetailDialog(
            event = event,
            onDismiss = { selectedEventForDetails = null },
            onAcknowledge = {
                viewModel.acknowledgeNetworkEvent(event.id)
                selectedEventForDetails = null
            },
            onReExecuteRemediation = {
                viewModel.reExecuteRemediationFromAlert(event)
                selectedEventForDetails = null
            },
            onDelete = {
                viewModel.dismissNetworkEvent(event.id)
                selectedEventForDetails = null
            }
        )
    }

    // Quick Simulation Selector Dialog
    if (showSimulateMenu) {
        SimulateNetworkEventDialog(
            onDismiss = { showSimulateMenu = false },
            onSelect = { type ->
                viewModel.simulateNetworkEvent(type)
                showSimulateMenu = false
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        )
    }
}

/**
 * Prominent Remediation Alert Banner (تنبيهات المعالجة السريعة)
 * Highlighted banner at the top of the event stream displaying urgent watchdog mitigations.
 */
@Composable
fun RemediationAlertNotificationBanner(
    event: NetworkEventLog,
    activeRemediationsCount: Int,
    onInspectClick: () -> Unit,
    onAcknowledgeClick: () -> Unit,
    onReExecuteClick: () -> Unit,
    onNavigateToRemediationTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val details = event.remediationDetails
    val statusColor = when (details?.status?.uppercase()) {
        "SUCCESS" -> StatusOnlineGreen
        "EXECUTING" -> CyberCyan
        "FAILED" -> StatusRogueCrimson
        else -> StatusWarningAmber
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("banner_remediation_alert")
            .clickable { onInspectClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(statusColor.copy(alpha = 0.6f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Header: Alert Tag, Timestamp & Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "تنبيه معالجة • REMEDIATION ALERT",
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (activeRemediationsCount > 1) {
                        Text(
                            text = "+${activeRemediationsCount - 1} more pending",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = event.timestamp,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Title & Server Target
            Text(
                text = event.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Command Snippet
            if (details != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberNavyCard)
                        .border(0.5.dp, CyberNavyBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = details.command,
                                color = CyberCyanLight,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Status Tag
                        Text(
                            text = details.status.uppercase(),
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.sourceDevice ?: "System Watchdog",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onAcknowledgeClick,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Acknowledge", fontSize = 10.sp)
                    }

                    Button(
                        onClick = onInspectClick,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Text("Inspect Log", color = CyberNavyDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Individual Event Item Card with distinctive categorization, severity indicators,
 * and context-sensitive quick actions.
 */
@Composable
fun NetworkEventItemCard(
    event: NetworkEventLog,
    onClick: () -> Unit,
    onAcknowledge: () -> Unit,
    onDismiss: () -> Unit,
    onReExecuteRemediation: () -> Unit,
    onPingDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (event.eventType) {
        NetworkEventType.REMEDIATION_ALERT -> StatusWarningAmber
        NetworkEventType.DEVICE_DISCOVERY -> CyberCyan
        NetworkEventType.CONNECTIVITY_CHANGE -> CyberBlue
        NetworkEventType.SECURITY_ANOMALY -> StatusRogueCrimson
        NetworkEventType.ALL -> TextPrimary
    }

    val categoryIcon = when (event.eventType) {
        NetworkEventType.REMEDIATION_ALERT -> Icons.Default.Terminal
        NetworkEventType.DEVICE_DISCOVERY -> Icons.Default.Devices
        NetworkEventType.CONNECTIVITY_CHANGE -> Icons.Default.SwapVert
        NetworkEventType.SECURITY_ANOMALY -> Icons.Default.Security
        NetworkEventType.ALL -> Icons.Default.NetworkCheck
    }

    val severityColor = when (event.severity) {
        AlertSeverity.CRITICAL -> StatusRogueCrimson
        AlertSeverity.WARNING -> StatusWarningAmber
        AlertSeverity.INFO -> CyberCyan
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_item_${event.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (event.isAcknowledged) CyberNavyBorder else categoryColor.copy(alpha = 0.45f)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header: Category Pill, Severity, Timestamp, Dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Category Name & Arabic tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryColor.copy(alpha = 0.12f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.eventType.displayName,
                            color = categoryColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Severity Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(severityColor.copy(alpha = 0.12f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.severity.name,
                            color = severityColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Timestamp & Dismiss Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = event.timestamp,
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Event",
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Title
            Text(
                text = event.title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Description
            Text(
                text = event.description,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Contextual Metadata Box: IPs, MACs, or Remediation Details
            if (event.remediationDetails != null) {
                val rem = event.remediationDetails
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberNavyCard)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CMD: ${rem.command}",
                            color = CyberCyanLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = rem.status,
                            color = if (rem.status == "SUCCESS") StatusOnlineGreen else StatusWarningAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else if (event.ipAddress != null || event.macAddress != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    event.ipAddress?.let { ip ->
                        Text(
                            text = "IP: $ip",
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    event.macAddress?.let { mac ->
                        Text(
                            text = "MAC: $mac",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Forensic Event Detail Dialog with complete execution output and parameters.
 */
@Composable
fun NetworkEventDetailDialog(
    event: NetworkEventLog,
    onDismiss: () -> Unit,
    onAcknowledge: () -> Unit,
    onReExecuteRemediation: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .testTag("dialog_network_event_detail"),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "EVENT TELEMETRY LOG",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = event.eventType.displayName + " • " + event.eventType.categoryArabic,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Title & Timestamp
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = event.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Timestamp: ${event.timestamp}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Severity: ${event.severity.name}",
                            color = when (event.severity) {
                                AlertSeverity.CRITICAL -> StatusRogueCrimson
                                AlertSeverity.WARNING -> StatusWarningAmber
                                AlertSeverity.INFO -> CyberCyan
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Description
                Text(
                    text = event.description,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Technical Metadata Table
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DetailMetaRow("Source Device", event.sourceDevice ?: "Local Subnet")
                        event.ipAddress?.let { DetailMetaRow("IP Address", it) }
                        event.macAddress?.let { DetailMetaRow("MAC Address", it) }
                        DetailMetaRow("Event Category", event.eventType.displayName)
                        DetailMetaRow("Status", if (event.isAcknowledged) "Acknowledged ✓" else "Active / Pending")
                    }
                }

                // Remediation Output (if applicable)
                event.remediationDetails?.let { rem ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "REMEDIATION EXECUTION TRACE (تنبيه المعالجة):",
                            color = StatusWarningAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberNavyDark)
                                .border(1.dp, CyberNavyBorder, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Target: ${rem.serverName}\nService: ${rem.targetService}\nTrigger: ${rem.triggerReason}\nCommand: ${rem.command}\nStatus: ${rem.status}\nOutput: ${rem.outputSnippet}",
                                color = CyberCyanLight,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Action Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Delete Event",
                            tint = StatusRogueCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (event.remediationDetails != null) {
                            Button(
                                onClick = onReExecuteRemediation,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusWarningAmber),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Re-Run Action", color = CyberNavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = onAcknowledge,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Acknowledge", color = CyberNavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 10.sp)
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Dialog to trigger a simulated network event for testing and verification.
 */
@Composable
fun SimulateNetworkEventDialog(
    onDismiss: () -> Unit,
    onSelect: (NetworkEventType?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = CyberNavySurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SIMULATE NETWORK EVENT",
                    color = CyberCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Choose an event type to inject into the live telemetry stream:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                val options = listOf(
                    Triple(NetworkEventType.DEVICE_DISCOVERY, "New Device Discovery", "Simulates ARP sweep finding an unauthorized or new host"),
                    Triple(NetworkEventType.CONNECTIVITY_CHANGE, "Connectivity Change", "Simulates link state transition, flap, or latency spike"),
                    Triple(NetworkEventType.REMEDIATION_ALERT, "Remediation Alert (تنبيه معالجة)", "Simulates automated watchdog recycling an overburdened service")
                )

                options.forEach { (type, label, desc) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(type) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(desc, color = TextMuted, fontSize = 10.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("Cancel", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
