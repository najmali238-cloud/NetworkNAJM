package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.AppLanguage
import com.example.data.model.IntrusionAttempt
import com.example.data.service.PdfExportResult
import com.example.ui.MainViewModel
import com.example.ui.components.PdfReportExportDialog
import com.example.ui.theme.getNetGuardColors

enum class ThreatSection(val titleEn: String, val titleAr: String) {
    INTRUSIONS("Intrusion Attempts", "محاولات الاختراق"),
    TRACE_ROUTE("Threat Origin & Trace", "تتبع مسار التهديد"),
    VULNERABILITIES("CVE Vulnerabilities", "كشف الثغرات CVE")
}

@Composable
fun IntrusionDetectionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val intrusionAttempts by viewModel.intrusionAttempts.collectAsState()
    val vulnerabilities by viewModel.vulnerabilities.collectAsState()

    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }
    val isArabic = appLanguage == AppLanguage.ARABIC

    var selectedSection by remember { mutableStateOf(ThreatSection.INTRUSIONS) }
    var selectedIntrusionForTrace by remember { mutableStateOf<IntrusionAttempt?>(null) }
    var pdfExportResult by remember { mutableStateOf<PdfExportResult?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ids_header_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.4f))
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
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = if (isArabic) "نظام كشف الاختراق وتتبع التهديدات" else "Intrusion Detection & Threat Tracking",
                                color = colors.primaryAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.statusGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "IDS/IPS Active",
                                color = colors.statusGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isArabic) "نظام رصد ومكافحة التهديدات والثغرات الأمنية" else "Autonomous Threat Prevention & Vulnerability Engine",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "تطوير المهندس نجم الرئيس - حماية استباقية وتتبع فوري",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val blockedCount = intrusionAttempts.count { it.status == "BLOCKED" || it.status == "QUARANTINED" }
                        val activeVulns = vulnerabilities.count { !it.isPatched }

                        StatBox(
                            title = if (isArabic) "هجمات محظورة" else "Blocked Attacks",
                            value = "$blockedCount",
                            color = colors.statusGreen,
                            modifier = Modifier.weight(1f),
                            colors = colors
                        )
                        StatBox(
                            title = if (isArabic) "محاولات مرصودة" else "Total Intrusions",
                            value = "${intrusionAttempts.size}",
                            color = colors.primaryAccent,
                            modifier = Modifier.weight(1f),
                            colors = colors
                        )
                        StatBox(
                            title = if (isArabic) "ثغرات غير مرقعة" else "Active CVEs",
                            value = "$activeVulns",
                            color = if (activeVulns > 0) colors.statusCrimson else colors.statusGreen,
                            modifier = Modifier.weight(1f),
                            colors = colors
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isExportingPdf = true
                            val result = viewModel.exportNetworkStatusAndSecurityPdf(context)
                            pdfExportResult = result
                            isExportingPdf = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("ids_export_pdf_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.surface,
                            contentColor = colors.primaryAccent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isExportingPdf) {
                                if (isArabic) "جاري إعداد وثيقة PDF الأمنية..." else "Generating Security PDF..."
                            } else {
                                if (isArabic) "تصدير تقرير أمني PDF للمراجعة الإدارية" else "Export Security Logs & Network Status PDF"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section Selector Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThreatSection.entries.forEach { section ->
                    val isSelected = section == selectedSection
                    Button(
                        onClick = { selectedSection = section },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) colors.primaryAccent else colors.cardBackground,
                            contentColor = if (isSelected) (if (isDarkMode) Color.Black else Color.White) else colors.textSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_${section.name.lowercase()}")
                    ) {
                        Text(
                            text = if (isArabic) section.titleAr else section.titleEn,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // CONTENT BY SECTION
        when (selectedSection) {
            ThreatSection.INTRUSIONS -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "محاولات الاختراق المرصودة مباشرة (${intrusionAttempts.size})" else "Live Monitored Intrusion Attempts (${intrusionAttempts.size})",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = { viewModel.simulateNewThreat() },
                            modifier = Modifier.testTag("simulate_intrusion_btn")
                        ) {
                            Icon(Icons.Default.AddModerator, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "+ محاكاة هجوم" else "+ Simulate Attack",
                                color = colors.primaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                items(intrusionAttempts) { intrusion ->
                    val isBlocked = intrusion.status == "BLOCKED" || intrusion.status == "QUARANTINED"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("intrusion_${intrusion.id}"),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isBlocked) colors.cardBorder else colors.statusCrimson.copy(alpha = 0.6f)
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = intrusion.attackerFlag, fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = if (isArabic) intrusion.threatTypeArabic else intrusion.threatType,
                                            color = colors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Attacker IP: ${intrusion.attackerIp} (${intrusion.attackerCountry})",
                                            color = colors.primaryAccent,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isBlocked) colors.statusGreen.copy(alpha = 0.15f) else colors.statusCrimson.copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = intrusion.status,
                                        color = if (isBlocked) colors.statusGreen else colors.statusCrimson,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isArabic) intrusion.attackVectorArabic else intrusion.attackVector,
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Target: ${intrusion.targetIp}:${intrusion.targetPort} | Time: ${intrusion.timestamp}",
                                    color = colors.textMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // View trace button
                                    OutlinedButton(
                                        onClick = {
                                            selectedIntrusionForTrace = intrusion
                                            selectedSection = ThreatSection.TRACE_ROUTE
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isArabic) "تتبع" else "Trace", fontSize = 10.sp)
                                    }

                                    if (!isBlocked) {
                                        Button(
                                            onClick = { viewModel.blockAttackerIp(intrusion.attackerIp) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.statusCrimson,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isArabic) "حظر IP" else "Block", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ThreatSection.TRACE_ROUTE -> {
                val targetTrace = selectedIntrusionForTrace ?: intrusionAttempts.firstOrNull()

                item {
                    Text(
                        text = if (isArabic) "مخطط تتبع مسار هجوم الاختراق (Packet Origin & Hop Route)" else "Threat Origin & Packet Trace Route",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (targetTrace == null) {
                    item {
                        Text(
                            text = if (isArabic) "لا توجد حزم محددة للتتبع حالياً." else "No attack packet selected for trace.",
                            color = colors.textSecondary
                        )
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trace_route_card"),
                            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.4f))
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${targetTrace.attackerFlag} Attacker: ${targetTrace.attackerIp}",
                                        color = colors.statusCrimson,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Target: ${targetTrace.targetIp}:${targetTrace.targetPort}",
                                        color = colors.primaryAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Hop by hop visual route
                                val hops = targetTrace.traceHops.ifEmpty {
                                    listOf(
                                        "${targetTrace.attackerIp} (${targetTrace.attackerCountry})",
                                        "Edge Gateway (192.168.1.1)",
                                        "HQ Core Switch (192.168.1.2)",
                                        "${targetTrace.targetIp} [Target Node]"
                                    )
                                }

                                hops.forEachIndexed { index, hop ->
                                    val isOrigin = index == 0
                                    val isTarget = index == hops.size - 1
                                    val hopColor = when {
                                        isOrigin -> colors.statusCrimson
                                        isTarget -> colors.statusAmber
                                        else -> colors.primaryAccent
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(hopColor.copy(alpha = 0.2f))
                                                .border(1.dp, hopColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                color = hopColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = hop,
                                                color = colors.textPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = if (isOrigin || isTarget) FontWeight.Bold else FontWeight.Normal,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = if (isOrigin) (if (isArabic) "مصدر الهجوم" else "Attacker Origin") else if (isTarget) (if (isArabic) "الهدف النهائي" else "Target Node") else (if (isArabic) "عقدة تمرير وسيطة" else "Transit Hop"),
                                                color = colors.textMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    if (index < hops.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 11.dp, top = 2.dp, bottom = 2.dp)
                                                .width(2.dp)
                                                .height(16.dp)
                                                .background(colors.cardBorder)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ThreatSection.VULNERABILITIES -> {
                item {
                    Text(
                        text = if (isArabic) "فاحص الثغرات الأمنية في أجهزة الشبكة (CVE Scanner)" else "Network Node Vulnerabilities (CVE Scanner)",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(vulnerabilities) { vuln ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vuln_${vuln.id}"),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (vuln.isPatched) colors.statusGreen.copy(alpha = 0.5f) else colors.statusCrimson.copy(alpha = 0.5f)
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.primaryAccent.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = vuln.cveId,
                                            color = colors.primaryAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.statusCrimson.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "CVSS ${vuln.cvssScore}",
                                            color = colors.statusCrimson,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (vuln.isPatched) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.statusGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isArabic) "تم الترقيع ✓" else "Patched ✓",
                                            color = colors.statusGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isArabic) vuln.titleArabic else vuln.title,
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Host: ${vuln.targetDeviceName} (${vuln.targetIp})",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Recommendation: ${if (isArabic) vuln.remediationRecommendationArabic else vuln.remediationRecommendation}",
                                color = colors.textMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            if (!vuln.isPatched) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { viewModel.patchVulnerability(vuln.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primaryAccent,
                                        contentColor = if (isDarkMode) Color.Black else Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("patch_btn_${vuln.id}")
                                ) {
                                    Icon(Icons.Default.BuildCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "تطبيق الترقيع الأمني الفوري" else "Apply Security Patch",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pdfExportResult?.let { result ->
        PdfReportExportDialog(
            pdfResult = result,
            viewModel = viewModel,
            onDismiss = { pdfExportResult = null }
        )
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    colors: com.example.ui.theme.NetGuardThemeColors
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, color = colors.textMuted, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
