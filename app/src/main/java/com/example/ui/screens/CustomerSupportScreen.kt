package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CustomerInquiry
import com.example.data.model.FaqCategory
import com.example.data.model.FaqItem
import com.example.ui.MainViewModel
import com.example.ui.theme.*

/**
 * Customer Support, Inquiries & FAQ Screen (مركز خدمة العملاء والاستعلامات والأسئلة الشائعة)
 * Developed and Supervised by Engineer Najm Al-Raees (المهندس نجم الرئيس)
 *
 * Features:
 * 1. Direct 24/7 WhatsApp Support: +967 738 704 940 (967738704940)
 * 2. Direct Emergency Phone Calling: +967 749 154 739 (967749154739)
 * 3. Official Web Security Platform Integration: https://netguard.enterprise.security
 * 4. Interactive Inquiries & Consultation Form (نموذج الاستعلامات وتذاكر الدعم)
 * 5. Comprehensive FAQs Library with search & category filters (الأسئلة الشائعة وإجاباتها)
 * 6. Security Health & Readiness Assessment Service (فحص الجاهزية والأمان الشامل)
 */
@Composable
fun CustomerSupportScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDark) { getNetGuardColors(isDark) }

    val faqList by viewModel.faqList.collectAsState()
    val inquiries by viewModel.customerInquiries.collectAsState()
    val securityAudit by viewModel.securityHealthAudit.collectAsState()

    var selectedFaqCategory by remember { mutableStateOf(FaqCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedFaqId by remember { mutableStateOf<String?>("faq-01") }

    // Inquiry Form State
    var showInquiryDialog by remember { mutableStateOf(false) }
    var senderNameInput by remember { mutableStateOf("") }
    var contactInfoInput by remember { mutableStateOf(viewModel.supportWhatsAppDisplay) }
    var inquiryTypeInput by remember { mutableStateOf("استفسار فني عام") }
    var subjectInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionSuccessBanner by remember { mutableStateOf<String?>(null) }

    // Filter FAQs by search query and category
    val filteredFaqs = remember(faqList, selectedFaqCategory, searchQuery) {
        faqList.filter { item ->
            val matchesCategory = (selectedFaqCategory == FaqCategory.ALL || item.category == selectedFaqCategory)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.questionArabic.contains(searchQuery, ignoreCase = true) ||
                item.questionEnglish.contains(searchQuery, ignoreCase = true) ||
                item.answerArabic.contains(searchQuery, ignoreCase = true) ||
                item.answerEnglish.contains(searchQuery, ignoreCase = true) ||
                item.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
            .testTag("customer_support_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. Chief Architect & Developer Credit Card
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("support_architect_header_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryAccent.copy(alpha = 0.18f))
                                    .border(1.5.dp, colors.primaryAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isArabic) "مركز خدمة العملاء والاستعلامات والدعم" else "Customer Service & Operations Desk",
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isArabic) "إشراف وتطوير: المهندس نجم الرئيس" else "Supervised by: Eng. Najm Al-Raees",
                                    color = colors.primaryAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.statusGreen.copy(alpha = 0.15f))
                                .border(1.dp, colors.statusGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(colors.statusGreen)
                                )
                                Text(
                                    text = if (isArabic) "24/7 متاح" else "24/7 Active",
                                    color = colors.statusGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isArabic) {
                            "مرحباً بك في مركز خدمة العملاء المعتمد لمنظومة NetGuard. نوفر لك دعماً تقنياً مباشراً، واستشارات أمنية متقدمة، وقنوات اتصال سريعة عبر الواتساب والهاتف والموقع الرسمي."
                        } else {
                            "Welcome to the certified NetGuard Customer Support & Inquiries Center. We provide real-time assistance, incident consultations, direct WhatsApp/phone dispatch, and comprehensive service FAQs."
                        },
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // ==========================================
        // 2. Direct Action Contact Cards (WhatsApp, Call, Web Portal)
        // ==========================================
        item {
            Text(
                text = if (isArabic) "قنوات التواصل المباشر وخدمة العملاء" else "Direct Support & Contact Channels",
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // WhatsApp Support Card (967738704940)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_whatsapp_support")
                        .clickable { viewModel.openWhatsAppSupport(context) },
                    shape = RoundedCornerShape(14.dp),
                    color = colors.cardBackground,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(Color(0xFF25D366).copy(alpha = 0.6f))
                    ),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366).copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF25D366).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "WhatsApp",
                                    color = Color(0xFF25D366),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (isArabic) "واتساب خدمة العملاء" else "WhatsApp Desk",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = viewModel.supportWhatsAppDisplay,
                            color = Color(0xFF25D366),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = if (isArabic) "محادثة فورية مع المهندس نجم الرئيس" else "Instant chat with Eng. Najm Al-Raees",
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )

                        Button(
                            onClick = { viewModel.openWhatsAppSupport(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isArabic) "مراسلة فورية" else "Chat Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Direct Call Support Card (967749154739)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_phone_support")
                        .clickable { viewModel.dialSupportPhone(context) },
                    shape = RoundedCornerShape(14.dp),
                    color = colors.cardBackground,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(colors.primaryAccent.copy(alpha = 0.6f))
                    ),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryAccent.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Direct Call",
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.primaryAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "اتصال مباشر" else "Direct Call",
                                    color = colors.primaryAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (isArabic) "رقم الاتصال المباشر" else "Direct Hot-Line",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = viewModel.supportPhoneDisplay,
                            color = colors.primaryAccent,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = if (isArabic) "خط الطوارئ والاستفسارات العاجلة" else "Emergency line & urgent inquiries",
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )

                        Button(
                            onClick = { viewModel.dialSupportPhone(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryAccent,
                                contentColor = if (isDark) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isArabic) "اتصال الآن" else "Call Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. Official Web Security Platform Integration Card
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_official_website"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(colors.primaryAccent.copy(alpha = 0.4f))
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.primaryAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isArabic) "الموقع الإلكتروني الرسمي لمنظومة NetGuard" else "Official NetGuard Web Portal",
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = viewModel.officialWebsiteDomain,
                                    color = colors.primaryAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = colors.statusGreen, modifier = Modifier.size(13.dp))
                            Text(text = "SSL 256-bit", color = colors.statusGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isArabic) {
                            "بوابة الويب المتكاملة لمتابعة سجلات البنية التحتية، وتحميل تحديثات الحماية وقواعد الـ CVE، وإدارة اشتراكات المؤسسات، وتوثيق ربط واجهات برمجة التطبيقات (API Reference)."
                        } else {
                            "Enterprise cloud portal for infrastructure audit logs, real-time CVE vulnerability feeds, subscription management, and complete REST API developer documentation."
                        },
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.openOfficialWebsite(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
                            )
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "فتح الموقع الإلكتروني" else "Visit Web Platform",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.openWhatsAppSupport(
                                    context,
                                    "السلام عليكم مهندس نجم، أود طلب ترخيص وربط الموقع الإلكتروني الرسمي NetGuard مع سيرفرات مؤسستنا."
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent)
                        ) {
                            Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isDark) Color.Black else Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "طلب ربط API" else "Request API Link",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. Proposed High-Value Service: Comprehensive Security Health Audit
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_health_audit_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(colors.statusGreen.copy(alpha = 0.5f))
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
                            Icon(Icons.Default.Verified, contentDescription = null, tint = colors.statusGreen, modifier = Modifier.size(20.dp))
                            Text(
                                text = if (isArabic) "خدمة فحص الأمان الشامل ومقياس الجاهزية" else "Comprehensive Security Readiness Check",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.statusGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = securityAudit.grade,
                                color = colors.statusGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isArabic) {
                            "خدمة تدقيق أمني متقدمة تفحص حالة جدار الحماية، وحساسات التسلل، وعزل الأجهزة الدخيلة، والنسخ الاحتياطي السحابي، لتقييم كفاءة وجودة الشبكة فورياً."
                        } else {
                            "Automated readiness assessment checking firewall rules, IDS sensors, rogue isolation latency, and backup sync to ensure peak operational reliability."
                        },
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audit metrics grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surface
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = if (isArabic) "مؤشر الكفاءة" else "Score", color = colors.textMuted, fontSize = 10.sp)
                                Text(text = "${securityAudit.scorePercent}%", color = colors.statusGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surface
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = if (isArabic) "الحماية اللحظية" else "IDS Sensor", color = colors.textMuted, fontSize = 10.sp)
                                Text(text = "100% نشط", color = colors.primaryAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surface
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = if (isArabic) "النسخ الاحتياطي" else "Cloud Sync", color = colors.textMuted, fontSize = 10.sp)
                                Text(text = "متزامن ✓", color = colors.statusGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.runSecurityHealthAudit() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_run_security_audit"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.statusGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "إعادة فحص الجاهزية الشامل وتحديث التقييم" else "Run Comprehensive Health Audit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ==========================================
        // 5. Inquiries & Technical Consultation Section (صفحة ونموذج الاستعلامات)
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_inquiries_section_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(colors.cardBorder)
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
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(20.dp))
                            Text(
                                text = if (isArabic) "قسم الاستعلامات والاستفسارات الفنية" else "Inquiries & Consultation Portal",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showInquiryDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_open_inquiry_form")
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isDark) Color.Black else Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "تقديم استعلام" else "New Inquiry",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.Black else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isArabic) {
                            "أرسل استفسارك أو طلب استشارتك التقنية مباشرة إلى المهندس نجم الرئيس وفريق الصيانة، وتابع الردود الرسمية المعتمدة فوراً."
                        } else {
                            "Submit inquiries or requests directly to Eng. Najm Al-Raees and review verified official responses in real time."
                        },
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )

                    if (submissionSuccessBanner != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.statusGreen.copy(alpha = 0.15f))
                                .border(1.dp, colors.statusGreen, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = submissionSuccessBanner ?: "",
                                color = colors.statusGreen,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of inquiries
                    inquiries.forEach { inq ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(colors.cardBorder.copy(alpha = 0.6f))
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = inq.subject,
                                        color = colors.textPrimary,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.statusGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = if (isArabic) "تم الرد ✓" else "Answered ✓", color = colors.statusGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(text = inq.message, color = colors.textSecondary, fontSize = 11.sp)

                                if (inq.officialReply != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.cardBackground)
                                            .border(1.dp, colors.primaryAccent.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(12.dp))
                                                Text(text = if (isArabic) "الرد الرسمي المعتمد:" else "Official Response:", color = colors.primaryAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = inq.officialReply, color = colors.textPrimary, fontSize = 10.5.sp, lineHeight = 14.sp)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = inq.senderName, color = colors.textMuted, fontSize = 10.sp)
                                    Text(text = inq.timestamp, color = colors.textMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. Comprehensive Smart FAQs Library (صفحة الأسئلة الشائعة وإجاباتها)
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(20.dp))
                        Text(
                            text = if (isArabic) "الأسئلة الشائعة وإجابات الخدمات" else "Frequently Asked Questions (FAQs)",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${filteredFaqs.size} ${if (isArabic) "سؤال وجواب" else "Q&As"}",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = if (isArabic) "إجابات وافية وشاملة حول كافة خدمات وتشغيل منظومة NetGuard والأمان السيبراني." else "Detailed technical explanations for all NetGuard monitoring, IDS, ping, and configuration services.",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // FAQ Search Box
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isArabic) "ابحث في الأسئلة والخدمات..." else "Search questions & services...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = colors.textMuted)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("faq_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryAccent,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // FAQ Category Filter Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FaqCategory.entries.forEach { category ->
                    val isSelected = category == selectedFaqCategory
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedFaqCategory = category }
                            .testTag("faq_cat_${category.name.lowercase()}"),
                        color = if (isSelected) colors.primaryAccent else colors.surface,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(if (isSelected) colors.primaryAccent else colors.cardBorder)
                        )
                    ) {
                        Text(
                            text = if (isArabic) category.titleArabic else category.titleEnglish,
                            color = if (isSelected) (if (isDark) Color.Black else Color.White) else colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // FAQ Accordion Items
        items(filteredFaqs, key = { it.id }) { faq ->
            val isExpanded = expandedFaqId == faq.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedFaqId = if (isExpanded) null else faq.id
                    }
                    .testTag("faq_item_${faq.id}"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(if (isExpanded) colors.primaryAccent else colors.cardBorder)
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "?",
                                    color = colors.primaryAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = if (isArabic) faq.questionArabic else faq.questionEnglish,
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.textMuted
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f))

                            Text(
                                text = if (isArabic) faq.answerArabic else faq.answerEnglish,
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.5.sp
                            )

                            // Tag chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                faq.tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.surface)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "#$tag", color = colors.textMuted, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // Interactive Inquiry Submission Dialog (نافذة تقديم الاستعلام)
    // ==========================================
    if (showInquiryDialog) {
        AlertDialog(
            onDismissRequest = { showInquiryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ContactSupport, contentDescription = null, tint = colors.primaryAccent)
                    Text(
                        text = if (isArabic) "تقديم استعلام أو استفسار فني" else "Submit Technical Inquiry",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isArabic) "أدخل بياناتك وموضوع استفسارك ليصل مباشرة إلى فريق المهندس نجم الرئيس:" else "Enter your inquiry details to route directly to Eng. Najm Al-Raees:",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = senderNameInput,
                        onValueChange = { senderNameInput = it },
                        label = { Text(if (isArabic) "الاسم / الجهة" else "Name / Entity") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("inquiry_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryAccent,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = contactInfoInput,
                        onValueChange = { contactInfoInput = it },
                        label = { Text(if (isArabic) "رقم الهاتف / الواتساب" else "Phone / WhatsApp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("inquiry_contact_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryAccent,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = subjectInput,
                        onValueChange = { subjectInput = it },
                        label = { Text(if (isArabic) "موضوع الاستعلام" else "Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("inquiry_subject_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryAccent,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        label = { Text(if (isArabic) "نص الاستفسار أو الطلب بالتفصيل" else "Message Details") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("inquiry_message_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryAccent,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectInput.isNotBlank() && messageInput.isNotBlank()) {
                            val inq = viewModel.submitCustomerInquiry(
                                senderName = senderNameInput,
                                contactInfo = contactInfoInput,
                                inquiryType = inquiryTypeInput,
                                subject = subjectInput,
                                message = messageInput
                            )
                            submissionSuccessBanner = if (isArabic) {
                                "تم إرسال الاستعلام بنجاح وتوثيقه بالرقم [${inq.id}]، وسيتواصل معك المهندس نجم الرئيس قريباً."
                            } else {
                                "Inquiry [${inq.id}] recorded successfully. Eng. Najm Al-Raees team will follow up promptly."
                            }
                            showInquiryDialog = false
                            subjectInput = ""
                            messageInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                    modifier = Modifier.testTag("inquiry_submit_dialog_btn")
                ) {
                    Text(
                        text = if (isArabic) "إرسال الاستعلام الآن" else "Submit Inquiry",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.Black else Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showInquiryDialog = false }) {
                    Text(text = if (isArabic) "إلغاء" else "Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
