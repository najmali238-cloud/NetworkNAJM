package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

/**
 * Full configuration UI for defining custom alert thresholds for CPU and Memory usage,
 * allowing users to receive notifications or trigger automated actions when specific servers exceed limits.
 */
@Composable
fun ServerThresholdConfigSection(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val thresholdRules by viewModel.serverThresholdRules.collectAsState()
    val serverMetrics by viewModel.serverMetrics.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<ServerThresholdRule?>(null) }
    var testFeedbackMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("server_threshold_config_card"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Server Alert & Action Thresholds",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "CPU & Memory Limits • Notification & Auto-Remediation",
                            color = CyberCyanLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Active Rules Counter Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberBlue.copy(alpha = 0.2f))
                        .border(1.dp, CyberBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${thresholdRules.count { it.isEnabled }} / ${thresholdRules.size} Active",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Define per-server CPU and memory usage ceilings. When exceeded, receive instant in-app/Telegram notifications and automatically execute SSH self-healing actions.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: "+ Add Threshold Rule" and "Evaluate Sweeps"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        ruleToEdit = null
                        showAddEditDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_add_threshold_rule"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = CyberNavyDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Custom Rule",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        viewModel.evaluateAllThresholdsNow()
                        testFeedbackMessage = "Checked ${thresholdRules.size} threshold rules against real-time server telemetry."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_eval_thresholds_now"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Check Limits Now",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (testFeedbackMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberNavyDark)
                        .border(1.dp, StatusOnlineGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusOnlineGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = testFeedbackMessage ?: "",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { testFeedbackMessage = null }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List of configured threshold rules
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                thresholdRules.forEach { rule ->
                    val serverMetric = serverMetrics.find { it.serverId == rule.serverId }
                    ThresholdRuleItemCard(
                        rule = rule,
                        currentCpu = serverMetric?.cpuPercent ?: 0f,
                        currentRam = serverMetric?.ramPercent ?: 0f,
                        onToggle = { isEnabled ->
                            viewModel.toggleThresholdRule(rule.id, isEnabled)
                        },
                        onEdit = {
                            ruleToEdit = rule
                            showAddEditDialog = true
                        },
                        onDelete = {
                            viewModel.deleteThresholdRule(rule.id)
                        },
                        onTestTrigger = {
                            viewModel.testThresholdRule(rule.id)
                            testFeedbackMessage = "Test Triggered: Dispatched alert & executed action for ${rule.serverName}"
                        }
                    )
                }
            }
        }
    }

    // Modal Add / Edit Threshold Rule Dialog
    if (showAddEditDialog) {
        AddEditThresholdRuleDialog(
            initialRule = ruleToEdit,
            serverMetrics = serverMetrics,
            onDismiss = { showAddEditDialog = false },
            onSave = { savedRule ->
                if (ruleToEdit == null) {
                    viewModel.addThresholdRule(savedRule)
                } else {
                    viewModel.updateThresholdRule(savedRule)
                }
                showAddEditDialog = false
            }
        )
    }
}

/**
 * Card representing a single defined Server Threshold Rule with live vs limit gauge.
 */
@Composable
fun ThresholdRuleItemCard(
    rule: ServerThresholdRule,
    currentCpu: Float,
    currentRam: Float,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCpuBreached = currentCpu >= rule.cpuThresholdPercent
    val isRamBreached = currentRam >= rule.ramThresholdPercent
    val hasBreach = (isCpuBreached || isRamBreached) && rule.isEnabled

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("threshold_rule_${rule.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (hasBreach) CyberNavyDark else CyberNavySurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                when {
                    hasBreach -> StatusOfflineRed.copy(alpha = 0.8f)
                    rule.isEnabled -> CyberNavyBorder
                    else -> CyberNavyDark
                }
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Server Name, Severity, and Toggle Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    !rule.isEnabled -> TextMuted
                                    hasBreach -> StatusOfflineRed
                                    else -> StatusOnlineGreen
                                }
                            )
                    )
                    Column {
                        Text(
                            text = rule.serverName,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (rule.serverId == "ALL") "All Cluster Nodes" else "ID: ${rule.serverId}",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (rule.severity == AlertSeverity.CRITICAL)
                                            StatusOfflineRed.copy(alpha = 0.2f)
                                        else
                                            StatusWarningAmber.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = rule.severity.name,
                                    color = if (rule.severity == AlertSeverity.CRITICAL) StatusOfflineRed else StatusWarningAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberNavyDark,
                        checkedTrackColor = CyberCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CyberNavyBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Limits Gauge Section (CPU & RAM limit vs live telemetry)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // CPU Limit Metric Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberNavyDark)
                        .border(
                            1.dp,
                            if (isCpuBreached && rule.isEnabled) StatusOfflineRed else CyberNavyBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CPU THRESHOLD",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "> ${rule.cpuThresholdPercent.toInt()}%",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (currentCpu / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = when {
                                currentCpu >= rule.cpuThresholdPercent -> StatusOfflineRed
                                currentCpu >= 70f -> StatusWarningAmber
                                else -> StatusOnlineGreen
                            },
                            trackColor = CyberNavyCard
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Live: ${currentCpu.toInt()}%",
                            color = if (isCpuBreached) StatusOfflineRed else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // RAM Limit Metric Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberNavyDark)
                        .border(
                            1.dp,
                            if (isRamBreached && rule.isEnabled) StatusOfflineRed else CyberNavyBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "MEMORY THRESHOLD",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "> ${rule.ramThresholdPercent.toInt()}%",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (currentRam / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = when {
                                currentRam >= rule.ramThresholdPercent -> StatusOfflineRed
                                currentRam >= 70f -> StatusWarningAmber
                                else -> StatusOnlineGreen
                            },
                            trackColor = CyberNavyCard
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Live: ${currentRam.toInt()}%",
                            color = if (isRamBreached) StatusOfflineRed else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action & Notification Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberNavyDark.copy(alpha = 0.7f))
                    .border(1.dp, CyberNavyBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Automated Action Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (rule.actionType == ThresholdActionType.NOTIFICATION_ONLY)
                                    Icons.Default.Notifications
                                else
                                    Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = if (rule.actionType == ThresholdActionType.NOTIFICATION_ONLY) CyberCyan else StatusWarningAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Action: ${rule.actionType.label}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Notification Channel Indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (rule.notifyInApp) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "In-App",
                                        color = CyberCyanLight,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (rule.notifyTelegram) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StatusOnlineGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Telegram",
                                        color = StatusOnlineGreen,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (rule.actionType != ThresholdActionType.NOTIFICATION_ONLY) {
                        Text(
                            text = rule.customCommand.ifBlank {
                                when (rule.actionType) {
                                    ThresholdActionType.RESTART_SERVICE -> "systemctl restart ${rule.targetService.lowercase()}"
                                    ThresholdActionType.PURGE_MEMORY_CACHE -> "sync && echo 3 > /proc/sys/vm/drop_caches"
                                    ThresholdActionType.RESTART_DOCKER -> "systemctl restart docker"
                                    ThresholdActionType.EMERGENCY_REBOOT -> "shutdown -r now"
                                    else -> "systemctl restart worker-service"
                                }
                            },
                            color = CyberCyanLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Trigger Stats & Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (rule.lastTriggered != null)
                        "Triggered: ${rule.triggerCount}x • Last: ${rule.lastTriggered}"
                    else
                        "No trigger events recorded",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Test Trigger button
                    IconButton(
                        onClick = onTestTrigger,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test Trigger",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Edit button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Rule",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Rule",
                            tint = StatusOfflineRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive dialog to create or edit a custom threshold alert and automated action policy.
 */
@Composable
fun AddEditThresholdRuleDialog(
    initialRule: ServerThresholdRule?,
    serverMetrics: List<ServerMetric>,
    onDismiss: () -> Unit,
    onSave: (ServerThresholdRule) -> Unit
) {
    val isEditing = initialRule != null

    var selectedServerId by remember {
        mutableStateOf(initialRule?.serverId ?: serverMetrics.firstOrNull()?.serverId ?: "dev-04")
    }
    var cpuThreshold by remember {
        mutableFloatStateOf(initialRule?.cpuThresholdPercent ?: 80f)
    }
    var ramThreshold by remember {
        mutableFloatStateOf(initialRule?.ramThresholdPercent ?: 75f)
    }
    var notifyInApp by remember {
        mutableStateOf(initialRule?.notifyInApp ?: true)
    }
    var notifyTelegram by remember {
        mutableStateOf(initialRule?.notifyTelegram ?: true)
    }
    var severity by remember {
        mutableStateOf(initialRule?.severity ?: AlertSeverity.WARNING)
    }
    var selectedActionType by remember {
        mutableStateOf(initialRule?.actionType ?: ThresholdActionType.NOTIFICATION_ONLY)
    }
    var targetServiceText by remember {
        mutableStateOf(initialRule?.targetService ?: "PostgreSQL DB")
    }
    var customCommandText by remember {
        mutableStateOf(initialRule?.customCommand ?: "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Dialog Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.AddAlert,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isEditing) "Edit Threshold Rule" else "New Threshold Rule",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Target Server Picker
                Text(
                    text = "TARGET SERVER",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val serverOptions = listOf(
                        Triple("dev-04", "DB-01 Master Cluster", "192.168.1.12"),
                        Triple("dev-03", "Prod-01 App Server", "192.168.1.10"),
                        Triple("dev-05", "NAS-01 Storage Vault", "192.168.1.20"),
                        Triple("dev-01", "Core Gateway & Firewall", "192.168.1.1")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        serverOptions.take(2).forEach { (id, name, ip) ->
                            val isSelected = selectedServerId == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyberNavyBorder else CyberNavySurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) CyberCyan else CyberNavyBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedServerId = id }
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = name.split(" ").first(),
                                        color = if (isSelected) CyberCyan else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = ip, color = TextMuted, fontSize = 9.sp)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        serverOptions.drop(2).forEach { (id, name, ip) ->
                            val isSelected = selectedServerId == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyberNavyBorder else CyberNavySurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) CyberCyan else CyberNavyBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedServerId = id }
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = name.split(" ").first(),
                                        color = if (isSelected) CyberCyan else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = ip, color = TextMuted, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. CPU Threshold Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CPU ALERT THRESHOLD",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${cpuThreshold.toInt()}%",
                        color = when {
                            cpuThreshold >= 85f -> StatusOfflineRed
                            cpuThreshold >= 70f -> StatusWarningAmber
                            else -> StatusOnlineGreen
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = cpuThreshold,
                    onValueChange = { cpuThreshold = it },
                    valueRange = 40f..98f,
                    steps = 57,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = when {
                            cpuThreshold >= 85f -> StatusOfflineRed
                            cpuThreshold >= 70f -> StatusWarningAmber
                            else -> CyberCyan
                        },
                        inactiveTrackColor = CyberNavyDark
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Memory (RAM) Threshold Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MEMORY (RAM) THRESHOLD",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${ramThreshold.toInt()}%",
                        color = when {
                            ramThreshold >= 85f -> StatusOfflineRed
                            ramThreshold >= 70f -> StatusWarningAmber
                            else -> StatusOnlineGreen
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = ramThreshold,
                    onValueChange = { ramThreshold = it },
                    valueRange = 40f..98f,
                    steps = 57,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = when {
                            ramThreshold >= 85f -> StatusOfflineRed
                            ramThreshold >= 70f -> StatusWarningAmber
                            else -> CyberCyan
                        },
                        inactiveTrackColor = CyberNavyDark
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Notification Channels & Severity
                Text(
                    text = "NOTIFICATION DISPATCH",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // In-App Notification Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberNavySurface)
                            .clickable { notifyInApp = !notifyInApp }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Checkbox(
                            checked = notifyInApp,
                            onCheckedChange = { notifyInApp = it },
                            colors = CheckboxDefaults.colors(checkedColor = CyberCyan, checkmarkColor = CyberNavyDark)
                        )
                        Text(text = "In-App Alert", color = TextPrimary, fontSize = 11.sp)
                    }

                    // Telegram Dispatch Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberNavySurface)
                            .clickable { notifyTelegram = !notifyTelegram }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Checkbox(
                            checked = notifyTelegram,
                            onCheckedChange = { notifyTelegram = it },
                            colors = CheckboxDefaults.colors(checkedColor = StatusOnlineGreen, checkmarkColor = CyberNavyDark)
                        )
                        Text(text = "Telegram", color = TextPrimary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Automated Action on Breach
                Text(
                    text = "AUTOMATED REMEDIATION ACTION",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Action Type Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val actions = listOf(
                        ThresholdActionType.NOTIFICATION_ONLY,
                        ThresholdActionType.RESTART_SERVICE,
                        ThresholdActionType.PURGE_MEMORY_CACHE,
                        ThresholdActionType.RESTART_DOCKER,
                        ThresholdActionType.CUSTOM_SCRIPT
                    )

                    actions.chunked(2).forEach { rowActions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowActions.forEach { action ->
                                val isSelected = selectedActionType == action
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CyberNavyBorder else CyberNavySurface)
                                        .border(
                                            1.dp,
                                            if (isSelected) CyberCyan else CyberNavyBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedActionType = action }
                                        .padding(horizontal = 8.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = action.label,
                                        color = if (isSelected) CyberCyan else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom Command / Service Input if needed
                if (selectedActionType == ThresholdActionType.RESTART_SERVICE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetServiceText,
                        onValueChange = { targetServiceText = it },
                        label = { Text("Service Name (e.g. nginx, postgresql)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberNavyBorder,
                            focusedContainerColor = CyberNavySurface,
                            unfocusedContainerColor = CyberNavySurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else if (selectedActionType == ThresholdActionType.CUSTOM_SCRIPT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customCommandText,
                        onValueChange = { customCommandText = it },
                        label = { Text("Custom SSH Script") },
                        placeholder = { Text("systemctl restart worker-service") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberNavyBorder,
                            focusedContainerColor = CyberNavySurface,
                            unfocusedContainerColor = CyberNavySurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save & Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        shape = RoundedCornerShape(10.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                        )
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val serverName = when (selectedServerId) {
                                "dev-04" -> "Database Cluster Master (DB-01)"
                                "dev-03" -> "Production App Server (Prod-01)"
                                "dev-05" -> "Backup & Storage Server (NAS-01)"
                                "dev-01" -> "Core Gateway & Edge Firewall"
                                else -> "Server ($selectedServerId)"
                            }

                            val newRule = ServerThresholdRule(
                                id = initialRule?.id ?: "rule-${System.currentTimeMillis()}",
                                serverId = selectedServerId,
                                serverName = serverName,
                                cpuThresholdPercent = cpuThreshold,
                                ramThresholdPercent = ramThreshold,
                                notifyInApp = notifyInApp,
                                notifyTelegram = notifyTelegram,
                                severity = severity,
                                actionType = selectedActionType,
                                targetService = targetServiceText.ifBlank { "PostgreSQL DB" },
                                customCommand = customCommandText,
                                isEnabled = true,
                                lastTriggered = initialRule?.lastTriggered,
                                triggerCount = initialRule?.triggerCount ?: 0
                            )
                            onSave(newRule)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = CyberNavyDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Rule", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
