package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.data.model.CriticalServer
import com.example.data.model.ServerMetric
import com.example.data.service.ServerMetricsExportFormat
import com.example.data.service.ServerMetricsExportResult
import com.example.data.service.ServerMetricsExportService
import com.example.ui.MainViewModel
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.getNetGuardColors

/**
 * Enterprise Dialog for exporting and archiving server metrics telemetry as CSV or JSON.
 * Provides live code preview, format selection, server scoping, and direct device file saving.
 */
@Composable
fun ExportServerMetricsDialog(
    serverMetrics: List<ServerMetric>,
    criticalServers: List<CriticalServer> = emptyList(),
    viewModel: MainViewModel,
    initialFormat: ServerMetricsExportFormat = ServerMetricsExportFormat.CSV,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isArabic = appLanguage == AppLanguage.ARABIC
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }

    val exportService = remember { ServerMetricsExportService(context) }

    var selectedFormat by remember { mutableStateOf(initialFormat) }
    var selectedServerId by remember { mutableStateOf("ALL") }
    var includeServices by remember { mutableStateOf(true) }
    var includeClusterSummary by remember { mutableStateOf(true) }
    var isCopied by remember { mutableStateOf(false) }

    // Generate exported content dynamically based on current configuration
    val rawContent = remember(
        selectedFormat,
        selectedServerId,
        includeServices,
        includeClusterSummary,
        serverMetrics,
        criticalServers
    ) {
        if (selectedFormat == ServerMetricsExportFormat.CSV) {
            exportService.generateCsv(
                serverMetrics = serverMetrics,
                criticalServers = criticalServers,
                includeServices = includeServices,
                includeSummary = includeClusterSummary,
                filterServerId = if (selectedServerId == "ALL") null else selectedServerId
            )
        } else {
            exportService.generateJson(
                serverMetrics = serverMetrics,
                criticalServers = criticalServers,
                includeServices = includeServices,
                includeSummary = includeClusterSummary,
                filterServerId = if (selectedServerId == "ALL") null else selectedServerId
            )
        }
    }

    val estimatedBytes = remember(rawContent) { rawContent.toByteArray(Charsets.UTF_8).size }
    val defaultFileName = remember(selectedFormat) {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
        "server_metrics_archive_$timestamp${selectedFormat.extension}"
    }

    // Storage Access Framework Launcher for saving directly to device storage
    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(selectedFormat.mimeType)
    ) { destinationUri ->
        if (destinationUri != null) {
            val success = exportService.writeToUri(destinationUri, rawContent)
            if (success) {
                Toast.makeText(
                    context,
                    if (isArabic) "تم حفظ ملف الأرشفة بنجاح في جهازك" else "Archive file saved successfully to device storage",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    if (isArabic) "فشل حفظ الملف، يرجى المحاولة مرة أخرى" else "Failed to write file to selected location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("export_server_metrics_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = colors.cardBackground,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
            ),
            tonalElevation = 8.dp
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
                                imageVector = if (selectedFormat == ServerMetricsExportFormat.CSV) Icons.Default.TableChart else Icons.Default.Code,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isArabic) "تصدير وأرشفة بيانات السيرفرات" else "EXPORT SERVER METRICS",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = if (isArabic) "حفظ التقرير بصيغة CSV أو JSON للأرشفة" else "Archive Telemetry as CSV or JSON File",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_export_metrics_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Format Selector (CSV vs JSON)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServerMetricsExportFormat.values().forEach { format ->
                        val isSelected = selectedFormat == format
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedFormat = format
                                isCopied = false
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (format == ServerMetricsExportFormat.CSV) Icons.Default.TableRows else Icons.Default.DataArray,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) colors.primaryAccent else colors.textSecondary
                                    )
                                    Text(
                                        text = if (isArabic) format.titleAr else format.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryAccent.copy(alpha = 0.2f),
                                selectedLabelColor = colors.primaryAccent,
                                containerColor = colors.surface,
                                labelColor = colors.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) colors.primaryAccent else colors.cardBorder
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("format_${format.name.lowercase()}_chip")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Server Filter Scoping Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isArabic) "النطاق:" else "Scope:",
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    val serverOptions = listOf("ALL" to (if (isArabic) "جميع السيرفرات (${serverMetrics.size})" else "All Servers (${serverMetrics.size})")) +
                            serverMetrics.map { it.serverId to "${it.serverId} (${it.serverName.take(12)})" }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        serverOptions.forEach { (id, label) ->
                            val isSelected = selectedServerId == id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedServerId = id
                                    isCopied = false
                                },
                                label = { Text(label, fontSize = 10.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primaryAccent.copy(alpha = 0.25f),
                                    selectedLabelColor = colors.primaryAccent,
                                    containerColor = colors.surface,
                                    labelColor = colors.textMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) colors.primaryAccent else colors.cardBorder
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("server_scope_$id")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Optional inclusion toggles (Services breakdown & Cluster Aggregates)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = includeServices,
                        onClick = {
                            includeServices = !includeServices
                            isCopied = false
                        },
                        label = {
                            Text(
                                text = if (isArabic) "تفاصيل الخدمات (Services)" else "Services Breakdown",
                                fontSize = 10.5.sp
                            )
                        },
                        leadingIcon = {
                            if (includeServices) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = colors.primaryAccent)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.surface,
                            selectedLabelColor = colors.textPrimary,
                            containerColor = colors.cardBackground,
                            labelColor = colors.textMuted
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_include_services")
                    )

                    FilterChip(
                        selected = includeClusterSummary,
                        onClick = {
                            includeClusterSummary = !includeClusterSummary
                            isCopied = false
                        },
                        label = {
                            Text(
                                text = if (isArabic) "ملخص التجميع (Cluster KPI)" else "Cluster KPI Summary",
                                fontSize = 10.5.sp
                            )
                        },
                        leadingIcon = {
                            if (includeClusterSummary) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = colors.primaryAccent)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.surface,
                            selectedLabelColor = colors.textPrimary,
                            containerColor = colors.cardBackground,
                            labelColor = colors.textMuted
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_include_summary")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // File stats metadata bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = defaultFileName,
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", estimatedBytes / 1024.0)} KB",
                        color = colors.statusGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live Content Preview Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .testTag("metrics_content_preview_box")
                ) {
                    val verticalScroll = rememberScrollState()
                    val horizontalScroll = rememberScrollState()

                    Text(
                        text = rawContent,
                        color = colors.textPrimary,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(verticalScroll)
                            .horizontalScroll(horizontalScroll)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Row: Copy, Save / Archive (SAF), Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Copy to Clipboard
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Server Metrics Archive", rawContent)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                            Toast.makeText(
                                context,
                                if (isArabic) "تم نسخ محتوى التقرير إلى الحافظة" else "Copied metrics to clipboard",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .weight(0.9f)
                            .height(44.dp)
                            .testTag("copy_metrics_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent)
                        )
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) (if (isArabic) "تم النسخ" else "Copied!") else (if (isArabic) "نسخ" else "Copy"),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. Save / Archive File to Device Storage (SAF)
                    Button(
                        onClick = {
                            saveDocumentLauncher.launch(defaultFileName)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .testTag("save_archive_file_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryAccent,
                            contentColor = if (isDarkMode) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "حفظ في الجهاز (Archive)" else "Save to Device",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 3. Share / Send File via Sharesheet
                    Button(
                        onClick = {
                            val exportResult = exportService.exportToFile(
                                format = selectedFormat,
                                serverMetrics = serverMetrics,
                                criticalServers = criticalServers,
                                includeServices = includeServices,
                                includeSummary = includeClusterSummary,
                                filterServerId = if (selectedServerId == "ALL") null else selectedServerId
                            )
                            exportService.shareExport(exportResult)
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(44.dp)
                            .testTag("share_metrics_file_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F766E), // Teal 700
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "مشاركة" else "Share",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
