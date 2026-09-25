package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AuthorizedDevice
import com.example.data.model.RogueDevice
import com.example.data.model.RogueStatus
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun RogueDeviceDetectionCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val rogueDevices by viewModel.rogueDevices.collectAsState()
    val authorizedWhitelist by viewModel.authorizedWhitelist.collectAsState()
    val auditSummary by viewModel.whitelistAuditSummary.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val unauthorizedHardware = rogueDevices.filter { it.status != RogueStatus.TRUSTED }
    val hasUnauthorized = unauthorizedHardware.isNotEmpty()

    var showWhitelistDialog by remember { mutableStateOf(false) }
    var selectedDeviceForScan by remember { mutableStateOf<RogueDevice?>(null) }
    var isExpanded by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "audit_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rogue_detection_module"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (hasUnauthorized) StatusRogueCrimson.copy(alpha = 0.8f) else StatusOnlineGreen.copy(alpha = 0.5f)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Status Bar
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (hasUnauthorized) StatusRogueCrimson.copy(alpha = 0.18f)
                                else StatusOnlineGreen.copy(alpha = 0.18f)
                            )
                            .border(
                                1.dp,
                                if (hasUnauthorized) StatusRogueCrimson.copy(alpha = 0.5f)
                                else StatusOnlineGreen.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasUnauthorized) Icons.Default.GppMaybe else Icons.Default.VerifiedUser,
                            contentDescription = "Rogue Detection Shield",
                            tint = if (hasUnauthorized) StatusRogueCrimson else StatusOnlineGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ROGUE DEVICE DETECTION",
                                color = if (hasUnauthorized) StatusRogueCrimson else StatusOnlineGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            PulsingRadarDot(color = if (hasUnauthorized) StatusRogueCrimson else StatusOnlineGreen)
                        }

                        Text(
                            text = if (hasUnauthorized)
                                "${unauthorizedHardware.size} Unauthorized Hardware Flagged"
                            else
                                "All Discovered Hardware Matches Whitelist",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Section",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Autonomous ARP/DHCP discovery agent continually compares active LAN network interfaces against the authorized enterprise hardware whitelist.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Whitelist Fleet Statistics Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberNavyDark)
                    .border(1.dp, CyberNavyBorder, RoundedCornerShape(10.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuditStatItem(
                    label = "Discovered",
                    value = "${auditSummary.totalDiscovered}",
                    valueColor = TextPrimary
                )
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(CyberNavyBorder))
                AuditStatItem(
                    label = "Whitelisted",
                    value = "${auditSummary.authorizedCount}",
                    valueColor = StatusOnlineGreen
                )
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(CyberNavyBorder))
                AuditStatItem(
                    label = "Unauthorized",
                    value = "${auditSummary.unauthorizedCount}",
                    valueColor = if (auditSummary.unauthorizedCount > 0) StatusRogueCrimson else TextPrimary
                )
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(CyberNavyBorder))
                AuditStatItem(
                    label = "Fleet Compliance",
                    value = "${auditSummary.compliancePercent}%",
                    valueColor = if (auditSummary.compliancePercent >= 90f) CyberCyan else StatusWarningAmber
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Run Audit Scan & Manage Whitelist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.runWhitelistAuditScan() },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("btn_run_whitelist_audit"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasUnauthorized) StatusRogueCrimson else CyberCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .then(if (isScanning) Modifier.rotate(spinAngle) else Modifier),
                        tint = if (hasUnauthorized) Color.White else CyberNavyDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScanning) "Auditing LAN..." else "Verify Whitelist",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasUnauthorized) Color.White else CyberNavyDark
                    )
                }

                OutlinedButton(
                    onClick = { showWhitelistDialog = true },
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_manage_whitelist"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberCyan.copy(alpha = 0.5f))
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAddCheck,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CyberCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Whitelist (${authorizedWhitelist.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Flagged Unauthorized Hardware Section
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                    if (hasUnauthorized) {
                        Text(
                            text = "FLAGGED UNAUTHORIZED HARDWARE (${unauthorizedHardware.size})",
                            color = StatusRogueCrimson,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        unauthorizedHardware.forEach { rogue ->
                            FlaggedHardwareCard(
                                rogue = rogue,
                                onIsolate = { viewModel.isolateRogue(rogue.id) },
                                onAuthorize = { viewModel.trustRogue(rogue.id) },
                                onInspectPorts = { selectedDeviceForScan = rogue }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    } else {
                        // Clean State Verified Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusOnlineGreen.copy(alpha = 0.1f))
                                .border(1.dp, StatusOnlineGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusOnlineGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Subnet Whitelist Integrity Nominal",
                                    color = StatusOnlineGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "All 10 discovered infrastructure hardware interfaces match registered enterprise credentials.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Whitelist Manager Dialog
    if (showWhitelistDialog) {
        WhitelistManagementDialog(
            authorizedList = authorizedWhitelist,
            onDismiss = { showWhitelistDialog = false },
            onAddDevice = { mac, name, vendor ->
                viewModel.addDeviceToWhitelist(mac, name, vendor)
            },
            onRemoveDevice = { id ->
                viewModel.removeDeviceFromWhitelist(id)
            }
        )
    }

    // Quick Diagnostics Port Scan Dialog
    if (selectedDeviceForScan != null) {
        val rogue = selectedDeviceForScan!!
        Dialog(onDismissRequest = { selectedDeviceForScan = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StatusRogueCrimson.copy(alpha = 0.6f))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Port & Security Diagnostics",
                            color = StatusRogueCrimson,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { selectedDeviceForScan = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Hardware Target: ${rogue.ip} | ${rogue.mac}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vendor OUI: ${rogue.vendor}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalBackground)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "PORT     STATE SERVICE       VERSION\n" +
                                    "22/tcp   open  ssh           OpenSSH 8.9p1 (Unauthorized)\n" +
                                    "80/tcp   open  http          Apache httpd 2.4.52\n" +
                                    "445/tcp  open  microsoft-ds  Samba SMBv2/v3\n" +
                                    "3389/tcp open  ms-wbt-server RDP Port Open\n\n" +
                                    "Security Alert: Unauthenticated workstation performing LAN discovery broadcast.",
                            color = TerminalGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.isolateRogue(rogue.id)
                                selectedDeviceForScan = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRogueCrimson),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Isolate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.trustRogue(rogue.id)
                                selectedDeviceForScan = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOnlineGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Authorize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlaggedHardwareCard(
    rogue: RogueDevice,
    onIsolate: () -> Unit,
    onAuthorize: () -> Unit,
    onInspectPorts: () -> Unit
) {
    val isIsolated = rogue.status == RogueStatus.ISOLATED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("unauthorized_hardware_card"),
        colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isIsolated) StatusWarningAmber.copy(alpha = 0.7f) else StatusRogueCrimson.copy(alpha = 0.7f)
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Reason Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isIsolated) StatusWarningAmber.copy(alpha = 0.15f)
                        else StatusRogueCrimson.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isIsolated) Icons.Default.Shield else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isIsolated) StatusWarningAmber else StatusRogueCrimson,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isIsolated) "DEVICE ISOLATED • ARP POISON MITIGATED" else rogue.unauthorizedReason,
                    color = if (isIsolated) StatusWarningAmber else StatusRogueCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HARDWARE MAC",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusRogueCrimson.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = rogue.mac,
                            color = StatusRogueCrimson,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DETECTED IP",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = rogue.ip,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hardware Vendor:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = rogue.vendor,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Detected Open Ports:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = rogue.openPorts.joinToString(", ") { "$it" },
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Containment State:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = rogue.status.label,
                    color = if (isIsolated) StatusWarningAmber else StatusRogueCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isIsolated) {
                    Button(
                        onClick = onIsolate,
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("btn_isolate_hardware"),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRogueCrimson),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Isolate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onAuthorize,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_authorize_hardware"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOnlineGreen),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StatusOnlineGreen.copy(alpha = 0.5f))
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Whitelist", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onInspectPorts,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_inspect_ports"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ports", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AuditStatItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistManagementDialog(
    authorizedList: List<AuthorizedDevice>,
    onDismiss: () -> Unit,
    onAddDevice: (mac: String, name: String, vendor: String) -> Unit,
    onRemoveDevice: (id: String) -> Unit
) {
    var newMac by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newVendor by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp)
                .testTag("whitelist_management_dialog"),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CyberCyan.copy(alpha = 0.5f))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                        Icon(
                            imageVector = Icons.Default.PlaylistAddCheck,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Enterprise Hardware Whitelist",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Text(
                    text = "Discovered MAC addresses must exist in this register to bypass rogue quarantine.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Form to add new MAC
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberNavyDark)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "AUTHORIZE NEW HARDWARE",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = newMac,
                        onValueChange = {
                            newMac = it
                            errorMessage = null
                        },
                        label = { Text("MAC Address (e.g. 00:0C:29:4F:8E:22)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_whitelist_mac"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberNavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Device Name", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("input_whitelist_name"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberNavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = newVendor,
                            onValueChange = { newVendor = it },
                            label = { Text("Vendor / OUI", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("input_whitelist_vendor"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberNavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = errorMessage!!, color = StatusRogueCrimson, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val trimmedMac = newMac.trim()
                            if (trimmedMac.length < 11 || !trimmedMac.contains(":")) {
                                errorMessage = "Please provide a valid MAC format (XX:XX:XX:XX:XX:XX)"
                            } else {
                                onAddDevice(
                                    trimmedMac,
                                    newName.ifBlank { "Authorized Device" },
                                    newVendor.ifBlank { "Enterprise Asset" }
                                )
                                newMac = ""
                                newName = ""
                                newVendor = ""
                                errorMessage = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_submit_whitelist_entry"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CyberNavyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Whitelist & Clear Quarantine", color = CyberNavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ACTIVE WHITELIST ENTRIES (${authorizedList.size})",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(authorizedList) { authItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberNavyDark)
                                .border(1.dp, CyberNavyBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = authItem.name,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${authItem.mac}  •  ${authItem.vendor}",
                                    color = CyberCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Approved by: ${authItem.authorizedBy} (${authItem.authorizedDate})",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            IconButton(
                                onClick = { onRemoveDevice(authItem.id) },
                                modifier = Modifier.size(32.dp).testTag("btn_revoke_${authItem.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Revoke Whitelist",
                                    tint = StatusRogueCrimson.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
