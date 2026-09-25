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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.service.PdfExportResult
import com.example.ui.MainViewModel
import com.example.ui.theme.getNetGuardColors

@Composable
fun PdfReportExportDialog(
    pdfResult: PdfExportResult,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isArabic = appLanguage == AppLanguage.ARABIC
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }

    var currentResult by remember { mutableStateOf(pdfResult) }
    var isRegenerating by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pdf_report_export_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFDC2626),
                                    Color(0xFFB91C1C)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "تقرير PDF للمراجعة الإدارية جاهز" else "Administrative PDF Report Ready",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isArabic) "ملخص حالة الشبكة وسجلات الأمان الموثقة" else "Network Status & Security Logs Audit Document",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Document Metadata Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "اسم الملف:" else "File Name:",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                            Text(
                                text = currentResult.title,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryAccent
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "الحجم والصفحات:" else "Size & Pages:",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                            Text(
                                text = "${currentResult.pageCount} Pages (A4) • ${currentResult.fileSizeBytes / 1024} KB",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "المشرف المعتمد:" else "Auditor in Charge:",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                            Text(
                                text = "Eng. Najm Al-Raees",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.statusGreen
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "تاريخ ووقت الإنشاء:" else "Generated At:",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                            Text(
                                text = currentResult.generatedTimestamp,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Summary of Sections in the PDF
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardBackground,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.25f))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isArabic) "محتويات التقرير المضمنة في وثيقة PDF:" else "Report Sections Included in PDF Document:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryAccent
                        )

                        val checklist = if (isArabic) listOf(
                            "مؤشر صحة الشبكة وحالة البوابة النشطة ومعدل فقدان الحزم",
                            "بيانات حركة المرور اللحظية (التحميل والرفع وسعة الذروة)",
                            "جرد الأجهزة المتصلة وتفاصيل المنافذ وحالة السيرفرات",
                            "سجلات محاولات الاختراق (IDS) وتتبع عناوين المهاجمين وحالة العزل",
                            "ملخص تنبيهات الأمان الفورية وتجاوز عتبات تدفق الشبكة",
                            "تقييم الثغرات CVE والأجهزة الدخيلة والتوقيع الإداري الرسمي"
                        ) else listOf(
                            "Network Health Score, Active Gateway & Latency / Loss telemetry",
                            "Real-time bandwidth throughput (DL/UL & Peak traffic flow)",
                            "Monitored device inventory & Server node CPU/RAM allocations",
                            "Intrusion Detection System (IDS) threat origin logs & mitigation status",
                            "Security alert chronology & network traffic threshold breaches",
                            "Vulnerability (CVE) matrix, rogue devices & ISO 27001 sign-off"
                        )

                        checklist.forEach { item ->
                            Row(
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
                                    text = item,
                                    fontSize = 10.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }

                // Primary Action Buttons: Open, Share, Re-export
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.openGeneratedPdf(context, currentResult)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryAccent,
                            contentColor = if (isDarkMode) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("open_pdf_report_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "فتح الوثيقة (View)" else "Open PDF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.shareGeneratedPdf(context, currentResult)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F766E), // Teal 700
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("share_pdf_report_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "مشاركة وإرسال" else "Share PDF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        isRegenerating = true
                        val newResult = viewModel.exportNetworkStatusAndSecurityPdf(context)
                        currentResult = newResult
                        isRegenerating = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("regenerate_pdf_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary),
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = colors.primaryAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRegenerating) {
                            if (isArabic) "جاري إعادة التوليد..." else "Re-generating..."
                        } else {
                            if (isArabic) "تحديث التقرير بالبيانات الحالية" else "Re-generate with Latest Telemetry"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_pdf_dialog_btn")
            ) {
                Text(
                    text = if (isArabic) "إغلاق" else "Close",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        },
        containerColor = colors.cardBackground,
        shape = RoundedCornerShape(16.dp)
    )
}
