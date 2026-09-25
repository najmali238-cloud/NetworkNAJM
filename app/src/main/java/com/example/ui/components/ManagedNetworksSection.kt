package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectedNetworkClient
import com.example.data.model.ManagedNetwork
import com.example.data.model.NetworkConnectionStatus
import com.example.data.model.NetworkInternetVoucher
import com.example.data.model.NetworkSecurityZone
import com.example.ui.theme.NetGuardThemeColors

@Composable
fun ManagedNetworksSection(
    networks: List<ManagedNetwork>,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onAddNetwork: (name: String, nameArabic: String, cidr: String, vlanId: Int, gateway: String, dns: String, zone: NetworkSecurityZone, bwLimit: Float, notes: String) -> Unit,
    onToggleConnection: (String) -> Unit,
    onIsolateNetwork: (String) -> Unit,
    onDeepScanNetwork: (String) -> Unit,
    onDeleteNetwork: (String) -> Unit,
    onExpandNetwork: ((String) -> Unit)? = null,
    internetVouchers: Map<String, NetworkInternetVoucher> = emptyMap(),
    connectedClients: List<ConnectedNetworkClient> = emptyList(),
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Section Header with Stats & "Add Network" Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "إدارة والتحكم في الشبكات المؤسسية المتعددة" else "Multi-Network Enterprise Orchestration",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isArabic) "إضافة، ربط، عزل، وإدارة كافة الشبكات الفرعية والفروع من لوحة المشرف"
                        else "Add, link, isolate, and manage subnets and remote branches",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("admin_add_network_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isArabic) "إضافة شبكة" else "Add Network",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Summary Quick Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val connectedCount = networks.count { it.isConnected }
            val totalDevices = networks.sumOf { it.deviceCount }
            val totalCapacityGbps = networks.sumOf { it.bandwidthLimitMbps.toDouble() } / 1000.0

            NetSummaryChip(
                label = if (isArabic) "إجمالي الشبكات" else "Networks",
                value = "${networks.size}",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            NetSummaryChip(
                label = if (isArabic) "الشبكات المربوطة" else "Linked",
                value = "$connectedCount / ${networks.size}",
                colors = colors,
                isHighlight = true,
                modifier = Modifier.weight(1f)
            )
            NetSummaryChip(
                label = if (isArabic) "الأجهزة التابعة" else "Endpoints",
                value = "$totalDevices",
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            NetSummaryChip(
                label = if (isArabic) "السعة الإجمالية" else "Bandwidth",
                value = "%.1f Gbps".format(totalCapacityGbps),
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        // Network Cards List
        networks.forEach { network ->
            val voucher = internetVouchers[network.id]
            val clients = connectedClients.filter { it.networkId == network.id }
            ManagedNetworkCard(
                network = network,
                voucher = voucher,
                clients = clients,
                isArabic = isArabic,
                colors = colors,
                onToggleConnection = { onToggleConnection(network.id) },
                onIsolate = { onIsolateNetwork(network.id) },
                onScan = { onDeepScanNetwork(network.id) },
                onDelete = { onDeleteNetwork(network.id) },
                onExpand = onExpandNetwork?.let { { it(network.id) } }
            )
        }
    }

    if (showAddDialog) {
        AddNewNetworkDialog(
            isArabic = isArabic,
            colors = colors,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, nameAr, cidr, vlan, gw, dns, zone, bw, notes ->
                onAddNetwork(name, nameAr, cidr, vlan, gw, dns, zone, bw, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ManagedNetworkCard(
    network: ManagedNetwork,
    voucher: NetworkInternetVoucher?,
    clients: List<ConnectedNetworkClient>,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onToggleConnection: () -> Unit,
    onIsolate: () -> Unit,
    onScan: () -> Unit,
    onDelete: () -> Unit,
    onExpand: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("managed_network_${network.id}"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (network.isConnected) colors.cardBorder else colors.statusCrimson.copy(alpha = 0.5f)
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Header + Zone + Status
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (network.isConnected) colors.primaryAccent.copy(alpha = 0.15f)
                                else colors.statusCrimson.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (network.isConnected) Icons.Default.Hub else Icons.Default.PortableWifiOff,
                            contentDescription = null,
                            tint = if (network.isConnected) colors.primaryAccent else colors.statusCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) network.nameArabic else network.name,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "CIDR: ${network.cidr} | VLAN: ${network.vlanId} | Gateway: ${network.gatewayIp}",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (network.isConnected) colors.statusGreen.copy(alpha = 0.15f)
                            else colors.statusCrimson.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (network.isConnected) (if (isArabic) "مربوطة بالنظام ✓" else "LINKED ✓")
                            else (if (isArabic) "معزولة / مفصولة ⚠️" else "ISOLATED ⚠️"),
                        color = if (network.isConnected) colors.statusGreen else colors.statusCrimson,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Metrics chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NetMiniStat(if (isArabic) "المنطقة" else "Zone", network.securityZone.name.replace("_", " "), colors, Modifier.weight(1f))
                NetMiniStat(if (isArabic) "الأجهزة" else "Devices", "${network.deviceCount}", colors, Modifier.weight(1f))
                NetMiniStat(if (isArabic) "المرور" else "Traffic", "${network.currentTrafficMbps.toInt()} / ${network.bandwidthLimitMbps.toInt()} Mbps", colors, Modifier.weight(1.4f))
                NetMiniStat(if (isArabic) "التأخير" else "Latency", "${network.latencyMs}ms", colors, Modifier.weight(0.9f))
            }

            // Voucher & Client Telemetry Bar
            Spacer(modifier = Modifier.height(8.dp))
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = colors.statusGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (voucher != null) {
                            if (isArabic) "الكرت: ${voucher.remainingQuotaGB}GB متبقي (${voucher.cardCode})"
                            else "Voucher: ${voucher.remainingQuotaGB}GB left (${voucher.cardCode})"
                        } else {
                            if (isArabic) "كرت الإنترنت: نشط وغير محدود" else "Voucher: Active Unlimited"
                        },
                        fontSize = 10.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (isArabic) "${clients.size} متصلين • ${network.assignedRouterCount} راوتر"
                            else "${clients.size} clients • ${network.assignedRouterCount} routers",
                        fontSize = 10.sp,
                        color = colors.primaryAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expansion Bar
            if (network.isExpandedToMax) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.statusGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.statusGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (isArabic) "موسعة لأكبر حد ممكن (Super Subnet • 65k أجهزة • 10 Gbps Fiber)"
                            else "Expanded to Max (Super Subnet • 65k hosts • 10 Gbps Fiber)",
                        fontSize = 10.sp,
                        color = colors.statusGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (onExpand != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "سعة محدودة (${network.maxAllowedHosts} جهاز)" else "Standard Subnet (${network.maxAllowedHosts} hosts)",
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                    OutlinedButton(
                        onClick = onExpand,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.NorthEast, contentDescription = null, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isArabic) "توسيع الشبكة لأكبر حد" else "Expand Network",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (network.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = network.notes,
                    color = colors.textSecondary,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Admin Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Connect/Disconnect Button
                OutlinedButton(
                    onClick = onToggleConnection,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (network.isConnected) colors.statusCrimson else colors.statusGreen
                    ),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Icon(
                        imageVector = if (network.isConnected) Icons.Default.LinkOff else Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (network.isConnected) (if (isArabic) "فصل" else "Unlink") else (if (isArabic) "ربط" else "Link"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Isolate Button
                Button(
                    onClick = onIsolate,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isArabic) Color(0xFF991B1B) else Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "عزل أمني" else "Isolate",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Deep Scan Button
                OutlinedButton(
                    onClick = onScan,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "فحص" else "Scan",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddNewNetworkDialog(
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    onDismiss: () -> Unit,
    onConfirm: (name: String, nameAr: String, cidr: String, vlan: Int, gw: String, dns: String, zone: NetworkSecurityZone, bw: Float, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameAr by remember { mutableStateOf("") }
    var cidr by remember { mutableStateOf("10.30.0.0/24") }
    var vlan by remember { mutableStateOf("120") }
    var gateway by remember { mutableStateOf("10.30.0.1") }
    var dns by remember { mutableStateOf("1.1.1.1") }
    var selectedZone by remember { mutableStateOf(NetworkSecurityZone.ENTERPRISE_CORE) }
    var bandwidth by remember { mutableStateOf("1000") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = colors.primaryAccent)
                Text(
                    text = if (isArabic) "إضافة شبكة جديدة وربطها بالنظام" else "Add & Link New Network",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isArabic) "أدخل بيانات الشبكة لربطها بلوحة تحكم المشرف وإخضاعها للرقابة والمايسترو:"
                        else "Enter network parameters to link with Admin Control & Autonomous Master:",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )

                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text(if (isArabic) "اسم الشبكة (عربي)" else "Network Name (Arabic)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isArabic) "الاسم بالإنجليزية" else "English Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cidr,
                        onValueChange = { cidr = it },
                        label = { Text("Subnet CIDR") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = vlan,
                        onValueChange = { vlan = it },
                        label = { Text("VLAN ID") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gateway,
                        onValueChange = { gateway = it },
                        label = { Text(if (isArabic) "بوابة التوجيه IP" else "Gateway IP") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bandwidth,
                        onValueChange = { bandwidth = it },
                        label = { Text("Mbps Limit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isArabic) "ملاحظات وتخصيص الشبكة" else "Notes / Purpose") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { err ->
                    Text(text = err, color = colors.statusCrimson, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cidr.isBlank() || gateway.isBlank()) {
                        errorMessage = if (isArabic) "يرجى تعبئة نطاق CIDR وبوابة التوجيه" else "CIDR and Gateway IP are required"
                        return@Button
                    }
                    val finalName = if (name.isNotBlank()) name else "Branch Network (${cidr})"
                    val finalNameAr = if (nameAr.isNotBlank()) nameAr else finalName
                    val vlanInt = vlan.toIntOrNull() ?: 100
                    val bwFloat = bandwidth.toFloatOrNull() ?: 1000f
                    onConfirm(finalName, finalNameAr, cidr, vlanInt, gateway, dns, selectedZone, bwFloat, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent)
            ) {
                Text(text = if (isArabic) "إضافة وربط الآن" else "Add & Link", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
private fun NetSummaryChip(
    label: String,
    value: String,
    colors: NetGuardThemeColors,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHighlight) colors.primaryAccent.copy(alpha = 0.12f) else colors.surface)
            .border(
                1.dp,
                if (isHighlight) colors.primaryAccent.copy(alpha = 0.4f) else colors.cardBorder,
                RoundedCornerShape(6.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = if (isHighlight) colors.primaryAccent else colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = label, color = colors.textSecondary, fontSize = 8.sp, maxLines = 1)
        }
    }
}

@Composable
private fun NetMiniStat(
    label: String,
    value: String,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(colors.surface)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, color = colors.textPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(text = label, color = colors.textMuted, fontSize = 8.sp, maxLines = 1)
        }
    }
}
