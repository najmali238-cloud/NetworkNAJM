package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.AppLanguage
import com.example.data.service.PdfExportResult
import com.example.data.service.ServerMetricsExportFormat
import com.example.ui.MainViewModel
import com.example.ui.components.ExportReportDialog
import com.example.ui.components.ExportServerMetricsDialog
import com.example.ui.components.PdfReportExportDialog
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }
    val isArabic = appLanguage == AppLanguage.ARABIC

    val report = viewModel.currentSlaReport.collectAsState().value
    val topologyNodes = viewModel.topologyNodes.collectAsState().value
    val serverMetrics = viewModel.serverMetrics.collectAsState().value
    val networkHealth = viewModel.networkHealth.collectAsState().value
    val bandwidthMetrics = viewModel.bandwidthMetrics.collectAsState().value
    val alerts = viewModel.alerts.collectAsState().value
    val nodeTrafficMap = viewModel.nodeTrafficMap.collectAsState().value

    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfExportResult by remember { mutableStateOf<PdfExportResult?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showDownloadReportDialog by remember { mutableStateOf(false) }
    var showExportServerMetricsDialog by remember { mutableStateOf(false) }
    var serverMetricsInitialFormat by remember { mutableStateOf(ServerMetricsExportFormat.CSV) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "تقرير أداء وعمل الشبكة ومستوى الخدمة (SLA)" else "Enterprise Network Performance & SLA Report",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.primaryAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ISO/IEC 27001",
                                color = colors.primaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "تطوير المهندس نجم الرئيس - تقرير شامل ومفصل لحالة ومؤشرات الشبكة",
                        color = colors.primaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isArabic) "تاريخ التوليد: ${report.generatedAt} | الأجهزة النشطة: ${report.activeDevices}/${report.totalDevices}" else "Generated: ${report.generatedAt} | Active Assets: ${report.activeDevices}/${report.totalDevices}",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // SLA Compliance Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "معدل التوافق مع اتفاقية مستوى الخدمة (SLA)" else "Overall SLA Compliance Rate",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${report.averageUptime}%",
                            color = if (report.averageUptime >= 99.0f) colors.statusGreen else colors.statusAmber,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.statusGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isArabic) "درجة الأمان ${report.securityScore}/100 ✓" else "Score: ${report.securityScore}/100",
                            color = colors.statusGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Core Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricScoreCard(
                    title = if (isArabic) "وقت التشغيل الفعلي" else "Actual Uptime",
                    score = "${report.averageUptime}%",
                    subtext = "Target: 99.95%",
                    accentColor = colors.statusGreen,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
                MetricScoreCard(
                    title = if (isArabic) "متوسط زمن الاستجابة" else "Avg Latency",
                    score = "${networkHealth.averageLatencyMs} ms",
                    subtext = "SLA: < 30ms",
                    accentColor = colors.primaryAccent,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricScoreCard(
                    title = if (isArabic) "متوسط وقت الإصلاح" else "MTTR",
                    score = "${report.mttrMinutes} min",
                    subtext = "Target: < 15 min",
                    accentColor = colors.statusGreen,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
                MetricScoreCard(
                    title = if (isArabic) "إجمالي التنبيهات (30 يوماً)" else "Alerts (30d)",
                    score = "${report.totalAlertsLast30Days}",
                    subtext = "Last 30 Days",
                    accentColor = colors.statusAmber,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
            }
        }

        // Incident Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "تصنيف وتوزيع الحوادث الأمنية والتشغيلية" else "Security Incident Categorization",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    report.incidentBreakdown.forEach { (category, count) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = category, color = colors.textSecondary, fontSize = 12.sp)
                                Text(text = "$count events", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (count / 10f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = when {
                                    category.contains("Rogue") -> colors.statusCrimson
                                    category.contains("Offline") -> colors.statusCrimson
                                    else -> colors.primaryAccent
                                },
                                trackColor = colors.surface
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: 1. Download & Share Report File, 2. PDF Report, 3. JSON/TXT Export
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // FEATURE: Download full report file (ميزة خدمة لمعرفة تقرير الشبكة وعملها ويكون قادر ان ينزل ملف التقرير)
                Button(
                    onClick = { showDownloadReportDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("download_report_file_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = if (isDarkMode) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "تنزيل ومشاركة ملف تقرير الشبكة الشامل" else "Download & Share Full Network Report File",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        isGeneratingPdf = true
                        val result = viewModel.exportNetworkStatusAndSecurityPdf(context)
                        pdfExportResult = result
                        isGeneratingPdf = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reports_export_pdf_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.cardBackground,
                        contentColor = colors.primaryAccent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.6f))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGeneratingPdf) {
                            if (isArabic) "جاري إعداد وثيقة PDF..." else "Generating PDF Report..."
                        } else {
                            if (isArabic) "تصدير تقرير PDF لحالة الشبكة والأمان للمراجعة الإدارية" else "Export Network Status & Security Logs PDF Report"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reports_export_json_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary),
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "تصدير بيانات القياس الخام (JSON / TXT)" else "Export Topology & Metrics (JSON / TXT)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dedicated Server Metrics Telemetry Archive Strip (CSV / JSON)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.35f))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("server_metrics_archive_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.primaryAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = colors.primaryAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isArabic) "أرشفة بيانات السيرفرات (CSV / JSON)" else "Archive Server Metrics Telemetry",
                                        color = colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isArabic) "تصدير فوري لمقاييس المعالج والذاكرة والمنافذ للأرشفة"
                                            else "Direct export of CPU, RAM, Disk & Services to archive files",
                                        color = colors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.primaryAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${serverMetrics.size} Hosts",
                                    color = colors.primaryAccent,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    serverMetricsInitialFormat = ServerMetricsExportFormat.CSV
                                    showExportServerMetricsDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("export_server_metrics_csv_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primaryAccent,
                                    contentColor = if (isDarkMode) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "تصدير CSV" else "Export CSV",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    serverMetricsInitialFormat = ServerMetricsExportFormat.JSON
                                    showExportServerMetricsDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("export_server_metrics_json_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0F766E),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.DataArray, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "تصدير JSON" else "Export JSON",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // DOWNLOAD REPORT FILE DIALOG
    if (showDownloadReportDialog) {
        val fullReportText = remember { viewModel.getDownloadableReport() }

        AlertDialog(
            onDismissRequest = { showDownloadReportDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = colors.primaryAccent)
                    Text(
                        text = if (isArabic) "ملف تقرير الشبكة وعملها جاهز للتنزيل" else "Downloadable Network Audit File",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isArabic) "يتضمن الملف تفاصيل التشغيل، تقييم الأمان، الأجهزة المتصلة، وتوصيات المهندس نجم الرئيس:" else "Report includes system performance, device audit, and security analysis:",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                        )
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = fullReportText,
                                    color = colors.textPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Native Android Share / Download Sheet
                        Button(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "NetGuard Network Performance Report - Eng. Najm Al-Raees")
                                    putExtra(Intent.EXTRA_TEXT, fullReportText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, if (isArabic) "تنزيل وحفظ ملف التقرير" else "Save / Download Report File"))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryAccent,
                                contentColor = if (isDarkMode) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("trigger_system_download_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "حفظ / تنزيل الملف" else "Save File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Copy to Clipboard
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("NetGuard Network Report", fullReportText)
                                clipboard.setPrimaryClip(clip)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("copy_report_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.primaryAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "نسخ النص" else "Copy", fontSize = 12.sp, color = colors.primaryAccent)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadReportDialog = false }) {
                    Text(if (isArabic) "إغلاق" else "Close", color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground
        )
    }

    if (showExportDialog) {
        ExportReportDialog(
            topologyNodes = topologyNodes,
            serverMetrics = serverMetrics,
            networkHealth = networkHealth,
            bandwidthMetrics = bandwidthMetrics,
            alerts = alerts,
            slaReport = report,
            nodeTrafficMap = nodeTrafficMap,
            onDismiss = { showExportDialog = false }
        )
    }

    pdfExportResult?.let { result ->
        PdfReportExportDialog(
            pdfResult = result,
            viewModel = viewModel,
            onDismiss = { pdfExportResult = null }
        )
    }

    if (showExportServerMetricsDialog) {
        val criticalServers by viewModel.criticalServers.collectAsState()
        ExportServerMetricsDialog(
            serverMetrics = serverMetrics,
            criticalServers = criticalServers,
            viewModel = viewModel,
            initialFormat = serverMetricsInitialFormat,
            onDismiss = { showExportServerMetricsDialog = false }
        )
    }
}

@Composable
fun MetricScoreCard(
    title: String,
    score: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    colors: com.example.ui.theme.NetGuardThemeColors
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = colors.textSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = score, color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
