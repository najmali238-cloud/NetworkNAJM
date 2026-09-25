package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Device
import com.example.data.model.DeviceStatus
import com.example.data.model.DeviceType
import com.example.ui.MainViewModel
import com.example.ui.components.DeviceTypeIcon
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DevicesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val devices = viewModel.devices.value
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Servers", "Routers & Switches", "Cameras", "Printers", "Workstations")

    val filteredDevices = devices.filter { device ->
        val matchesSearch = device.name.contains(searchQuery, ignoreCase = true) ||
                device.ip.contains(searchQuery) ||
                device.mac.contains(searchQuery, ignoreCase = true)

        val matchesCategory = when (selectedFilter) {
            "Servers" -> device.type == DeviceType.SERVER
            "Routers & Switches" -> device.type == DeviceType.ROUTER || device.type == DeviceType.SWITCH
            "Cameras" -> device.type == DeviceType.CAMERA
            "Printers" -> device.type == DeviceType.PRINTER
            "Workstations" -> device.type == DeviceType.WORKSTATION
            else -> true
        }
        matchesSearch && matchesCategory
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberNavyDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyberCyan,
                contentColor = CyberNavyDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Device")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, IP, or MAC...", color = TextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberNavyBorder,
                    focusedContainerColor = CyberNavyCard,
                    unfocusedContainerColor = CyberNavyCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { option ->
                    val isSelected = selectedFilter == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = option },
                        label = { Text(option, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = CyberNavyDark,
                            containerColor = CyberNavyCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CyberCyan else CyberNavyBorder
                        )
                    )
                }
            }

            // Results summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monitored Devices (${filteredDevices.size})",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = { viewModel.triggerScan() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Sweep Subnet", color = CyberCyan, fontSize = 12.sp)
                }
            }

            // Device List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDevices) { device ->
                    DeviceCardItem(
                        device = device,
                        onPingClick = { viewModel.triggerScan() }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, ip, type ->
                showAddDialog = false
                viewModel.triggerScan()
            }
        )
    }
}

@Composable
fun DeviceCardItem(
    device: Device,
    onPingClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (device.status == DeviceStatus.OFFLINE) StatusOfflineRed.copy(alpha = 0.5f) else CyberNavyBorder
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberNavyDark),
                        contentAlignment = Alignment.Center
                    ) {
                        DeviceTypeIcon(
                            type = device.type,
                            tint = if (device.status == DeviceStatus.OFFLINE) StatusOfflineRed else CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = device.name,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${device.ip} • ${device.mac}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                StatusBadge(status = device.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (device.status == DeviceStatus.OFFLINE) "Latency: Timeout" else "Latency: ${device.latencyMs}ms",
                        color = if (device.status == DeviceStatus.OFFLINE) StatusOfflineRed else TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Uptime: ${device.uptimePercent}%",
                        color = StatusOnlineGreen,
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPingClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkPing,
                            contentDescription = "Ping",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Open ports chips if available
            if (device.openPorts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    device.openPorts.forEach { port ->
                        val portName = when (port) {
                            22 -> "22/SSH"
                            80 -> "80/HTTP"
                            443 -> "443/HTTPS"
                            554 -> "554/RTSP"
                            5432 -> "5432/PGSQL"
                            6379 -> "6379/REDIS"
                            3389 -> "3389/RDP"
                            else -> "$port/TCP"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberNavyDark)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = portName, color = CyberBlueLight, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, DeviceType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("192.168.1.") }
    var selectedType by remember { mutableStateOf(DeviceType.SERVER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Monitored Asset", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Device Name (e.g. ERP-Srv-02)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("IP Address") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && ip.isNotBlank()) onAdd(name, ip, selectedType) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberNavyDark)
            ) {
                Text("Add Asset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CyberNavyCard
    )
}
