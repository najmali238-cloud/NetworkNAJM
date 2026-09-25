package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.Device
import com.example.data.model.DeviceType
import com.example.data.model.ServerMetric
import com.example.ui.theme.NetGuardThemeColors

@Composable
fun EnterpriseServersMatrixSection(
    devices: List<Device>,
    serverMetrics: List<ServerMetric>,
    isArabic: Boolean,
    colors: NetGuardThemeColors,
    modifier: Modifier = Modifier
) {
    var expandedFilter by remember { mutableStateOf("ALL") }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "مصفوفة الخوادم والأجهزة والأنظمة والمعدات المؤسسية" else "Enterprise Servers, Systems & Equipment Matrix",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isArabic) "جرد وتتبع مباشر للخوادم المضافة، أجهزة التوجيه، جدران الحماية، مصفوفات التخزين ووحدات PDU"
                        else "Telemetry for added servers, NGFW firewalls, load balancers, SAN arrays & smart PDUs",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.primaryAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${devices.size} " + (if (isArabic) "معدة نشطة" else "Units"),
                    color = colors.primaryAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf(
                "ALL" to (if (isArabic) "الكل" else "All"),
                "SERVERS" to (if (isArabic) "الخوادم" else "Servers"),
                "ROUTERS" to (if (isArabic) "الأنظمة والشبكات" else "Network/NGFW"),
                "STORAGE" to (if (isArabic) "المعدات والتخزين" else "Storage/PDU")
            )

            categories.forEach { (catKey, catLabel) ->
                val selected = expandedFilter == catKey
                FilterChip(
                    selected = selected,
                    onClick = { expandedFilter = catKey },
                    label = { Text(catLabel, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryAccent.copy(alpha = 0.2f),
                        selectedLabelColor = colors.primaryAccent
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        selectedBorderColor = colors.primaryAccent
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }
        }

        // Filter Devices based on selection
        val filteredDevices = remember(devices, expandedFilter) {
            when (expandedFilter) {
                "SERVERS" -> devices.filter { it.type == DeviceType.SERVER || it.id.contains("orchestrator") }
                "ROUTERS" -> devices.filter { it.type == DeviceType.ROUTER || it.type == DeviceType.SWITCH }
                "STORAGE" -> devices.filter { it.type != DeviceType.SERVER && it.type != DeviceType.ROUTER && it.type != DeviceType.SWITCH }
                else -> devices
            }
        }

        // List Cards
        filteredDevices.forEach { dev ->
            val metric = serverMetrics.firstOrNull { it.serverId == dev.id }
            EnterpriseDeviceCard(
                device = dev,
                metric = metric,
                isArabic = isArabic,
                colors = colors
            )
        }
    }
}

@Composable
private fun EnterpriseDeviceCard(
    device: Device,
    metric: ServerMetric?,
    isArabic: Boolean,
    colors: NetGuardThemeColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("equipment_item_${device.id}"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (device.type) {
                                DeviceType.SERVER -> Icons.Default.Dns
                                DeviceType.ROUTER -> Icons.Default.Router
                                DeviceType.SWITCH -> Icons.Default.Hub
                                DeviceType.CAMERA -> Icons.Default.Videocam
                                else -> Icons.Default.Computer
                            },
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = device.name,
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "IP: ${device.ip} | MAC: ${device.mac} | Type: ${device.type.displayName}",
                            color = colors.textMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.statusGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isArabic) "متصل وفعال" else "ONLINE",
                        color = colors.statusGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // If metrics available, show live CPU, RAM, Disk, Temp
            metric?.let { m ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniMetricBar(
                        label = "CPU",
                        percent = m.cpuPercent,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    MiniMetricBar(
                        label = "RAM",
                        percent = m.ramPercent,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    MiniMetricBar(
                        label = "DISK",
                        percent = m.diskPercent,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surface)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${m.temperatureC.toInt()}°C",
                                color = colors.primaryAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) "الحرارة" else "Temp",
                                color = colors.textMuted,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniMetricBar(
    label: String,
    percent: Float,
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
            Text(
                text = "${percent.toInt()}%",
                color = if (percent > 85f) colors.statusCrimson else if (percent > 65f) colors.statusAmber else colors.textPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = colors.textMuted,
                fontSize = 8.sp
            )
        }
    }
}
