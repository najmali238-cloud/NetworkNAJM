package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RogueDevice
import com.example.data.model.RogueStatus
import com.example.data.model.ThreatLevel
import com.example.ui.MainViewModel
import com.example.ui.components.PulsingRadarDot
import com.example.ui.components.RogueRemediationComponent
import com.example.ui.components.ThreatBadge
import com.example.ui.theme.*

@Composable
fun RogueDetectorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val rogueDevices = viewModel.rogueDevices.value
    var selectedRogueForScan by remember { mutableStateOf<RogueDevice?>(null) }
    var scanLogText by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StatusRogueCrimson.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StatusRogueCrimson.copy(alpha = 0.5f))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PulsingRadarDot(color = StatusRogueCrimson)
                        Text(
                            text = "ROGUE DEVICE DEFENSE SYSTEM",
                            color = StatusRogueCrimson,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Real-time ARP & DHCP Network Guard",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Any unapproved MAC or IP address communicating on the enterprise subnet is immediately quarantined and reported via Telegram in format: IP | MAC | Time",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Remediation & Access Blocking Component with Device Monitoring Setup
        item {
            RogueRemediationComponent(viewModel = viewModel)
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discovered Unauthorized Devices (${rogueDevices.size})",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = { viewModel.triggerScan() }) {
                    Icon(imageVector = Icons.Default.Radar, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Scan LAN", color = CyberCyan, fontSize = 12.sp)
                }
            }
        }

        // Rogue Devices Items
        items(rogueDevices) { rogue ->
            RogueDeviceCard(
                rogue = rogue,
                onIsolate = { viewModel.isolateRogue(rogue.id) },
                onTrust = { viewModel.trustRogue(rogue.id) },
                onDeepScan = {
                    selectedRogueForScan = rogue
                    scanLogText = "Nmap scan report for ${rogue.ip} (${rogue.mac})\n" +
                            "PORT     STATE SERVICE       VERSION\n" +
                            "22/tcp   open  ssh           OpenSSH 8.9p1\n" +
                            "80/tcp   open  http          Apache httpd 2.4.52\n" +
                            "445/tcp  open  microsoft-ds  Samba smbd 4.15\n" +
                            "3389/tcp open  ms-wbt-server Microsoft Terminal Services\n" +
                            "MAC Address: ${rogue.mac} (${rogue.vendor})\n" +
                            "Device type: general purpose | Threat score: 8.9/10"
                }
            )
        }

        // Deep Scan Result Console if opened
        if (scanLogText != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nmap Security Diagnostic Output",
                                color = CyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { scanLogText = null }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerminalBackground)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = scanLogText ?: "",
                                color = TerminalGreen,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RogueDeviceCard(
    rogue: RogueDevice,
    onIsolate: () -> Unit,
    onTrust: () -> Unit,
    onDeepScan: () -> Unit
) {
    val isIsolated = rogue.status == RogueStatus.ISOLATED
    val isTrusted = rogue.status == RogueStatus.TRUSTED

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isIsolated) StatusWarningAmber else StatusRogueCrimson.copy(alpha = 0.6f)
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Alert Format: IP | MAC | Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "INCIDENT SIGNATURE",
                        color = StatusRogueCrimson,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${rogue.ip} | ${rogue.mac} | ${rogue.detectedAt}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                ThreatBadge(threat = rogue.threatLevel)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberNavyDark)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Whitelist Violation:", color = TextSecondary, fontSize = 11.sp)
                    Text(rogue.unauthorizedReason, color = StatusRogueCrimson, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vendor / Hardware OUI:", color = TextSecondary, fontSize = 11.sp)
                    Text(rogue.vendor, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("First Seen:", color = TextSecondary, fontSize = 11.sp)
                    Text(rogue.firstSeen, color = TextPrimary, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Detected Open Ports:", color = TextSecondary, fontSize = 11.sp)
                    Text(rogue.openPorts.joinToString(", ") { "$it" }, color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Containment Status:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = rogue.status.label,
                        color = if (isIsolated) StatusWarningAmber else if (isTrusted) StatusOnlineGreen else StatusRogueCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isIsolated && !isTrusted) {
                    Button(
                        onClick = onIsolate,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRogueCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Isolate Device", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (!isTrusted) {
                    OutlinedButton(
                        onClick = onTrust,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOnlineGreen),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(StatusOnlineGreen.copy(alpha = 0.5f))
                        )
                    ) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trust / Whitelist", fontSize = 11.sp)
                    }
                }

                OutlinedButton(
                    onClick = onDeepScan,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Port Scan", fontSize = 11.sp)
                }
            }
        }
    }
}
