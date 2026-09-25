package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.Device
import com.example.data.model.DeviceThreshold
import com.example.ui.MainViewModel
import com.example.ui.theme.*

/**
 * Network Diagnostic Ping Utility Card
 * Allows network engineers to directly ping any IP address or host from the Dashboard,
 * view real-time latency stats (min/avg/max, packet loss %), inspect live packet stream,
 * and verify against device threshold baselines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDiagnosticPingCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onOpenThresholdSetup: ((String) -> Unit)? = null
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDark) { getNetGuardColors(isDark) }

    val devices by viewModel.devices.collectAsState()
    val diagnosticSession by viewModel.diagnosticSession.collectAsState()
    val deviceThresholds by viewModel.deviceThresholds.collectAsState()

    var targetIpInput by remember { mutableStateOf("192.168.1.1") }
    var packetCount by remember { mutableStateOf(4) }
    var showDevicePicker by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Quick target presets
    val quickPresets = remember(devices) {
        listOf(
            "192.168.1.1" to "Gateway",
            "192.168.1.2" to "Core Switch",
            "192.168.1.10" to "App Server",
            "192.168.1.12" to "DB Master",
            "1.1.1.1" to "Cloudflare",
            "8.8.8.8" to "Google DNS"
        )
    }

    // Matching threshold for current target if available
    val activeThreshold = remember(diagnosticSession.targetIp, deviceThresholds) {
        deviceThresholds.find { it.ipAddress.equals(diagnosticSession.targetIp, ignoreCase = true) }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("network_diagnostic_ping_card"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (diagnosticSession.isThresholdBreached) colors.statusCrimson else colors.cardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. Header with Terminal Badge
            // ==========================================
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.primaryAccent.copy(alpha = 0.15f))
                            .border(1.dp, colors.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "أداة تشخيص الشبكة وفحص الاتصال (PING)" else "NETWORK DIAGNOSTIC & ICMP PING UTILITY",
                            color = colors.primaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isArabic) "فحص استجابة وتأخير عناوين IP مباشرة" else "Direct IP Connectivity & Latency Prober",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Status or Clear Button
                if (diagnosticSession.results.isNotEmpty() && !diagnosticSession.isRunning) {
                    IconButton(
                        onClick = { viewModel.clearDiagnosticSession() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Session",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = if (isArabic)
                    "فحص زمن الاستجابة الفعلي (RTT)، معدل فقدان الحزم، والتحقق التلقائي من مطابقة زمن الاستجابة لعتبات الأجهزة المحددة."
                else
                    "Probe live round-trip latency (RTT), packet delivery success, and automatically verify against configured device performance thresholds.",
                color = colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            // ==========================================
            // 2. Input Row & Target Controls
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = targetIpInput,
                    onValueChange = { targetIpInput = it },
                    label = { Text(if (isArabic) "عنوان IP أو اسم المضيف" else "Target IP or Hostname", fontSize = 11.sp) },
                    placeholder = { Text("192.168.1.1", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ping_ip_input_field"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            focusManager.clearFocus()
                            if (targetIpInput.isNotBlank()) {
                                viewModel.pingTarget(targetIpInput, packetCount)
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryAccent,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedLabelColor = colors.primaryAccent,
                        cursorColor = colors.primaryAccent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        if (targetIpInput.isNotEmpty()) {
                            IconButton(onClick = { targetIpInput = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                )

                // Pick from inventory button
                IconButton(
                    onClick = { showDevicePicker = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
                        .testTag("ping_pick_device_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Pick Device",
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Packet Count Selector
                Box {
                    var showPacketMenu by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { showPacketMenu = true },
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("ping_packet_count_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(colors.cardBorder))
                    ) {
                        Text("${packetCount}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }

                    DropdownMenu(
                        expanded = showPacketMenu,
                        onDismissRequest = { showPacketMenu = false }
                    ) {
                        listOf(3, 4, 8, 10).forEach { count ->
                            DropdownMenuItem(
                                text = { Text("$count ${if (isArabic) "حزم" else "Packets"}") },
                                onClick = {
                                    packetCount = count
                                    showPacketMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Quick Target Preset Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPresets.forEach { (ip, label) ->
                    val isSelected = targetIpInput == ip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.primaryAccent.copy(alpha = 0.2f) else colors.surface)
                            .border(1.dp, if (isSelected) colors.primaryAccent else colors.cardBorder, RoundedCornerShape(8.dp))
                            .clickable { targetIpInput = ip }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) colors.primaryAccent else colors.textSecondary.copy(alpha = 0.5f))
                            )
                            Text(
                                text = label,
                                color = if (isSelected) colors.primaryAccent else colors.textPrimary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = ip,
                                color = colors.textSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Execute Ping Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (targetIpInput.isNotBlank()) {
                            viewModel.pingTarget(targetIpInput, packetCount)
                        }
                    },
                    enabled = !diagnosticSession.isRunning && targetIpInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("ping_execute_button")
                ) {
                    if (diagnosticSession.isRunning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "جارٍ الفحص..." else "Probing...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "بدء فحص الاتصال (Ping)" else "Ping IP Address",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (diagnosticSession.isRunning) {
                    OutlinedButton(
                        onClick = { viewModel.stopPing() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.statusCrimson),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(colors.statusCrimson)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("ping_stop_button")
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isArabic) "إيقاف" else "Stop", fontSize = 12.sp)
                    }
                }
            }

            // ==========================================
            // 3. Live Stats & Metrics Summary
            // ==========================================
            if (diagnosticSession.results.isNotEmpty() || diagnosticSession.isRunning) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Metric Badges Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DiagnosticMetricBadge(
                            label = if (isArabic) "الحزم المرسلة" else "Sent/Received",
                            value = "${diagnosticSession.packetsReceived}/${diagnosticSession.packetsSent}",
                            color = colors.primaryAccent,
                            modifier = Modifier.weight(1f),
                            colors = colors
                        )

                        DiagnosticMetricBadge(
                            label = if (isArabic) "فقدان الحزم" else "Packet Loss",
                            value = "${diagnosticSession.packetLossPercent.toInt()}%",
                            color = if (diagnosticSession.packetLossPercent > 0f) colors.statusAmber else colors.statusGreen,
                            modifier = Modifier.weight(1f),
                            colors = colors
                        )

                        DiagnosticMetricBadge(
                            label = if (isArabic) "متوسط التأخير" else "Avg Latency",
                            value = "${diagnosticSession.avgLatencyMs} ms",
                            color = when {
                                diagnosticSession.avgLatencyMs == 0L -> colors.textSecondary
                                diagnosticSession.avgLatencyMs < 30L -> colors.statusGreen
                                diagnosticSession.avgLatencyMs < 80L -> colors.statusAmber
                                else -> colors.statusCrimson
                            },
                            modifier = Modifier.weight(1f),
                            colors = colors
                        )

                        DiagnosticMetricBadge(
                            label = if (isArabic) "الحد الأدنى/الأقصى" else "Min / Max",
                            value = "${diagnosticSession.minLatencyMs}/${diagnosticSession.maxLatencyMs} ms",
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1.1f),
                            colors = colors
                        )
                    }

                    // Threshold Check Result Banner
                    if (diagnosticSession.isThresholdBreached) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.statusCrimson.copy(alpha = 0.15f))
                                .border(1.dp, colors.statusCrimson.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = colors.statusCrimson, modifier = Modifier.size(18.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isArabic) "تنبيه: تم تجاوز عتبة الجهاز المقررة!" else "Threshold Limit Breached!",
                                        color = colors.statusCrimson,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = diagnosticSession.thresholdBreachReason ?: "",
                                        color = colors.textPrimary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    } else if (!diagnosticSession.isRunning && diagnosticSession.results.isNotEmpty() && activeThreshold != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.statusGreen.copy(alpha = 0.12f))
                                .border(1.dp, colors.statusGreen.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.statusGreen, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (isArabic)
                                        "الأداء ضمن العتبات المصرحة: تأخير < ${activeThreshold.maxLatencyMs}ms وفقدان < ${activeThreshold.maxPacketLossPercent.toInt()}%"
                                    else
                                        "Within defined thresholds: Latency < ${activeThreshold.maxLatencyMs}ms & Loss < ${activeThreshold.maxPacketLossPercent.toInt()}%",
                                    color = colors.statusGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 4. Live Terminal Console Output
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0D1117))
                            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Terminal Header Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                                }
                                Text(
                                    text = "PING ${diagnosticSession.targetIp} (${diagnosticSession.targetLabel})",
                                    color = Color(0xFF8B949E),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Divider(color = Color(0xFF21262D), thickness = 0.8.dp, modifier = Modifier.padding(vertical = 4.dp))

                            // Terminal Output Lines
                            diagnosticSession.results.forEach { result ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (result.isSuccess) {
                                        Text(
                                            text = "${result.bytes} bytes from ${result.ip}: icmp_seq=${result.sequence} ttl=${result.ttl} time=${result.latencyMs} ms",
                                            color = when {
                                                result.latencyMs < 30L -> Color(0xFF3FB950)
                                                result.latencyMs < 80L -> Color(0xFFD29922)
                                                else -> Color(0xFFF85149)
                                            },
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    } else {
                                        Text(
                                            text = "Request timeout for icmp_seq ${result.sequence}: ${result.errorMessage ?: "Host Unreachable"}",
                                            color = Color(0xFFF85149),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Text(
                                        text = result.timestamp,
                                        color = Color(0xFF484F58),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (diagnosticSession.isRunning) {
                                Text(
                                    text = "Probing sequence ${diagnosticSession.packetsSent + 1}...",
                                    color = Color(0xFF58A6FF),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // Device Selection Dialog
    // ==========================================
    if (showDevicePicker) {
        AlertDialog(
            onDismissRequest = { showDevicePicker = false },
            title = {
                Text(
                    text = if (isArabic) "اختر جهازاً للفحص" else "Select Target Device",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    devices.forEach { dev ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    targetIpInput = dev.ip
                                    showDevicePicker = false
                                },
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(dev.name, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(dev.ip, color = colors.primaryAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                                Text("${dev.latencyMs} ms", color = colors.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDevicePicker = false }) {
                    Text(if (isArabic) "إغلاق" else "Close")
                }
            }
        )
    }
}

@Composable
private fun DiagnosticMetricBadge(
    label: String,
    value: String,
    color: Color,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
