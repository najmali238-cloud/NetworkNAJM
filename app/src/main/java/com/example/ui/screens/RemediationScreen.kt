package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutoRemediationPolicy
import com.example.data.model.RemediationAction
import com.example.ui.MainViewModel
import com.example.ui.components.PulsingRadarDot
import com.example.ui.components.RogueRemediationComponent
import com.example.ui.components.TerminalConsoleBox
import com.example.ui.theme.*

@Composable
fun RemediationScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val serverMetrics by viewModel.serverMetrics.collectAsState()
    val remediations by viewModel.remediations.collectAsState()
    val autoEnabled by viewModel.autoRemediationEnabled.collectAsState()
    val policies by viewModel.autoRemediationPolicies.collectAsState()

    var selectedServerId by remember { mutableStateOf(serverMetrics.firstOrNull()?.serverId ?: "dev-04") }
    var customCommand by remember { mutableStateOf("") }
    var terminalOutput by remember {
        mutableStateOf(
            "[SEC-SSH] NetGuard SSH Remote Incident & Auto-Remediation Console\n" +
            "[INFO] Authenticated as root@enterprise-gateway via RSA-4096 Key\n" +
            "[WATCHDOG] 24/7 Load-Based Service Watchdog: ACTIVE\n" +
            "[STATUS] Ready for automated service recovery or manual execution."
        )
    }

    val selectedServer = serverMetrics.find { it.serverId == selectedServerId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("remediation_header_card"),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "AUTOMATED SSH REMOTE EXECUTION",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        PulsingRadarDot(color = if (autoEnabled) StatusOnlineGreen else StatusWarningAmber)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Autonomous Service Self-Healing",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Monitors CPU and RAM loads in real time. When thresholds are breached or microservices become unresponsive, automated SSH sessions instantly restart daemons and flush system buffers.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Rogue Device Remediation & Network Access Blocking (عزل الأجهزة الدخيلة واعداد مراقبة الجهاز)
        item {
            RogueRemediationComponent(
                viewModel = viewModel
            )
        }

        // 3. Automated Master Watchdog Toggle Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_watchdog_toggle_card"),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (autoEnabled) StatusOnlineGreen.copy(alpha = 0.4f) else CyberNavyBorder
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Load Watchdog Auto-Restart",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (autoEnabled) StatusOnlineGreen.copy(alpha = 0.15f)
                                        else StatusWarningAmber.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (autoEnabled) "24/7 ACTIVE" else "PAUSED",
                                    color = if (autoEnabled) StatusOnlineGreen else StatusWarningAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (autoEnabled)
                                "Continuously evaluates server load. Triggers SSH command if RAM > 80% or service halts."
                            else
                                "Auto-trigger is paused. Only operator-initiated commands will execute.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = autoEnabled,
                        onCheckedChange = { viewModel.toggleAutoRemediation(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberNavyDark,
                            checkedTrackColor = StatusOnlineGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberNavyBorder
                        ),
                        modifier = Modifier.testTag("auto_remediation_master_switch")
                    )
                }
            }
        }

        // 3. Automated Policies List Section
        item {
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
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Automated Load-Trigger Policies",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${policies.count { it.isEnabled }} / ${policies.size} Active",
                    color = CyberCyanLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 4. Policy Cards
        items(policies, key = { it.id }) { policy ->
            val hostMetric = serverMetrics.find { it.serverId == policy.serverId }
            AutoRemediationPolicyCard(
                policy = policy,
                currentRamPercent = hostMetric?.ramPercent ?: 0f,
                currentCpuPercent = hostMetric?.cpuPercent ?: 0f,
                onToggleEnabled = { isChecked ->
                    viewModel.togglePolicy(policy.id, isChecked)
                },
                onUpdateThresholds = { ram, cpu ->
                    viewModel.updatePolicyThresholds(policy.id, ram, cpu)
                },
                onTriggerTest = {
                    viewModel.triggerPolicyNow(policy.id)
                    terminalOutput += "\n\n[MANUAL TRIGGER TEST] Policy: ${policy.serviceName} on ${policy.serverName}\n" +
                            "[SSH] Authenticated as root@${hostMetric?.ip ?: "192.168.1.12"}\n" +
                            "[EXEC] # ${policy.sshCommand}\n" +
                            "[OK] Service recycled cleanly. Exit code: 0"
                }
            )
        }

        // 5. Target Server Selector for Manual Actions
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manual Target Server Override",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                serverMetrics.forEach { server ->
                    val isSelected = server.serverId == selectedServerId
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedServerId = server.serverId },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyberNavyBorder else CyberNavyCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) CyberCyan else CyberNavyBorder)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = server.serverName.split(" ").first(),
                                color = if (isSelected) CyberCyan else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = server.ip,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "RAM: ${server.ramPercent.toInt()}%",
                                color = if (server.ramPercent > 80f) StatusWarningAmber else StatusOnlineGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 6. Rapid Self-Healing Presets
        item {
            Text(
                text = "One-Tap Service Recovery Presets",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemediationActionButton(
                        title = "Restart Nginx Web Server",
                        command = "systemctl restart nginx",
                        icon = Icons.Default.RestartAlt,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.executeRemediation(selectedServerId, "Nginx", "systemctl restart nginx")
                            terminalOutput += "\n\n[SSH TRIGGER] Executing: systemctl restart nginx on ${selectedServer?.ip}...\n" +
                                    "[OK] Service active (running). RAM buffers reclaimed."
                        }
                    )

                    RemediationActionButton(
                        title = "Restart PostgreSQL DB",
                        command = "systemctl restart postgresql",
                        icon = Icons.Default.Storage,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.executeRemediation(selectedServerId, "PostgreSQL", "systemctl restart postgresql")
                            terminalOutput += "\n\n[SSH TRIGGER] Executing: systemctl restart postgresql on ${selectedServer?.ip}...\n" +
                                    "[OK] PostgreSQL daemon recycled cleanly. Connection pools reset."
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemediationActionButton(
                        title = "Purge Cache & Reclaim RAM",
                        command = "sync && echo 3 > /proc/sys/vm/drop_caches",
                        icon = Icons.Default.Memory,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.executeRemediation(selectedServerId, "Kernel VM", "sync && echo 3 > /proc/sys/vm/drop_caches")
                            terminalOutput += "\n\n[SSH TRIGGER] Flushing page caches on ${selectedServer?.ip}...\n" +
                                    "[OK] Dropped inode and slab caches. 2.4 GB memory freed."
                        }
                    )

                    RemediationActionButton(
                        title = "Restart Docker Daemon",
                        command = "systemctl restart docker",
                        icon = Icons.Default.AllInclusive,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.executeRemediation(selectedServerId, "Docker", "systemctl restart docker")
                            terminalOutput += "\n\n[SSH TRIGGER] Recycling Docker Engine on ${selectedServer?.ip}...\n" +
                                    "[OK] Container runtime restored. Sockets active."
                        }
                    )
                }

                RemediationActionButton(
                    title = "Emergency System Reboot",
                    command = "shutdown -r now",
                    icon = Icons.Default.PowerSettingsNew,
                    modifier = Modifier.fillMaxWidth(),
                    isDangerous = true,
                    onClick = {
                        viewModel.executeRemediation(selectedServerId, "OS Core", "shutdown -r now")
                        terminalOutput += "\n\n[SSH TRIGGER] Issued 'shutdown -r now' to ${selectedServer?.ip}!\n" +
                                "[WARN] Server rebooting... ping watchdog monitoring for recovery."
                    }
                )
            }
        }

        // 7. Custom SSH Command Input
        item {
            Text(
                text = "Manual SSH Command Execution",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customCommand,
                    onValueChange = { customCommand = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_ssh_input"),
                    placeholder = { Text("e.g. systemctl restart redis", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberNavyBorder,
                        focusedContainerColor = CyberNavyCard,
                        unfocusedContainerColor = CyberNavyCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        if (customCommand.isNotBlank()) {
                            val cmd = customCommand
                            customCommand = ""
                            viewModel.executeRemediation(selectedServerId, "Custom", cmd)
                            terminalOutput += "\n\n[EXEC] root@${selectedServer?.ip}:# $cmd\n[OK] Command applied successfully (Exit: 0)."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberNavyDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("run_ssh_button")
                ) {
                    Text("Run", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 8. Live Terminal Console Box
        item {
            Text(
                text = "Live SSH Execution Terminal",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            TerminalConsoleBox(logs = terminalOutput)
        }

        // 9. Remediation Audit Trail History
        if (remediations.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remediation Audit Trail",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${remediations.size} Events",
                        color = CyberCyanLight,
                        fontSize = 11.sp
                    )
                }
            }

            items(remediations, key = { it.id }) { item ->
                RemediationHistoryCard(item = item)
            }
        }

        // Spacer at bottom
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Visualizes an Automated Self-Healing Policy based on monitored server load
 */
@Composable
fun AutoRemediationPolicyCard(
    policy: AutoRemediationPolicy,
    currentRamPercent: Float,
    currentCpuPercent: Float,
    onToggleEnabled: (Boolean) -> Unit,
    onUpdateThresholds: (ram: Float, cpu: Float) -> Unit = { _, _ -> },
    onTriggerTest: () -> Unit
) {
    var isTuning by remember { mutableStateOf(false) }
    var tunedRam by remember(policy.ramThresholdPercent) { mutableFloatStateOf(policy.ramThresholdPercent) }
    var tunedCpu by remember(policy.cpuThresholdPercent) { mutableFloatStateOf(policy.cpuThresholdPercent) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("policy_card_${policy.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (policy.isEnabled) CyberNavyBorder else CyberNavyDark
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Policy Header (Service + Switch)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (policy.isEnabled) StatusOnlineGreen else TextMuted)
                        )
                        Text(
                            text = policy.serviceName,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = policy.serverName,
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                }

                Switch(
                    checked = policy.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberNavyDark,
                        checkedTrackColor = CyberCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CyberNavyBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trigger Condition & Command
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberNavyDark)
                    .border(1.dp, CyberNavyBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TRIGGER THRESHOLD",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "RAM > ${policy.ramThresholdPercent.toInt()}% • CPU > ${policy.cpuThresholdPercent.toInt()}%",
                            color = StatusWarningAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SSH COMMAND",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = policy.sshCommand,
                            color = CyberCyanLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Monitored Load Gauge vs Threshold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Live RAM: ${currentRamPercent.toInt()}%",
                        color = if (currentRamPercent >= policy.ramThresholdPercent) StatusOfflineRed else StatusOnlineGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Limit: ${policy.ramThresholdPercent.toInt()}%",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Quick tune threshold toggle button
                    OutlinedButton(
                        onClick = { isTuning = !isTuning },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isTuning) CyberCyan else CyberNavyBorder
                            )
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (isTuning) CyberCyan else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTuning) "Close" else "Limits",
                            color = if (isTuning) CyberCyan else TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    // Quick test trigger button
                    OutlinedButton(
                        onClick = onTriggerTest,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Test SSH", color = CyberCyan, fontSize = 10.sp)
                    }
                }
            }

            // Expandable inline Threshold Tuning Panel
            AnimatedVisibility(visible = isTuning) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberNavyDark)
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "CUSTOM LOAD THRESHOLDS",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // RAM Threshold Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RAM Threshold Limit:", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            "${tunedRam.toInt()}%",
                            color = if (tunedRam >= 85f) StatusOfflineRed else CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = tunedRam,
                        onValueChange = { tunedRam = it },
                        valueRange = 40f..95f,
                        steps = 54,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = CyberNavyCard
                        )
                    )

                    // CPU Threshold Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CPU Threshold Limit:", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            "${tunedCpu.toInt()}%",
                            color = if (tunedCpu >= 85f) StatusOfflineRed else CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = tunedCpu,
                        onValueChange = { tunedCpu = it },
                        valueRange = 40f..95f,
                        steps = 54,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = CyberNavyCard
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                onUpdateThresholds(tunedRam, tunedCpu)
                                isTuning = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = CyberNavyDark
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Apply Thresholds", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RemediationHistoryCard(item: RemediationAction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (item.isAutomated) CyberCyan.copy(alpha = 0.4f) else CyberNavyBorder
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                            .background(
                                if (item.isAutomated) CyberCyan.copy(alpha = 0.2f)
                                else CyberBlue.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.isAutomated) "AUTOMATED" else "MANUAL",
                            color = if (item.isAutomated) CyberCyan else CyberBlueLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = item.serverName,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(text = item.executedAt, color = TextMuted, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.triggerReason,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$ ${item.command}",
                color = CyberCyanLight,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RemediationActionButton(
    title: String,
    command: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isDangerous: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDangerous) StatusOfflineRed.copy(alpha = 0.2f) else CyberNavyCard,
            contentColor = if (isDangerous) StatusOfflineRed else TextPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDangerous) StatusOfflineRed.copy(alpha = 0.5f) else CyberNavyBorder
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Column {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = command, color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            }
        }
    }
}
