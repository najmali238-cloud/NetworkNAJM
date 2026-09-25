package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.theme.getNetGuardColors

/**
 * Support Contact Component
 *
 * Provides floating interactive contact buttons to launch WhatsApp for 967738704940
 * and the system phone dialer for 967749154739.
 *
 * Designed as a unified management tool overlay:
 * - Allows network administrators and technicians to immediately escalate incidents,
 *   request technical assistance, or contact Eng. Najm Al-Raees from any screen.
 * - Includes smooth expand/collapse animations, Material 3 elevated action buttons,
 *   and direct integration with the unified NetGuard management platform.
 */
@Composable
fun SupportContactComponent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    SupportContactFloatingButtons(
        viewModel = viewModel,
        modifier = modifier
    )
}

@Composable
fun SupportContactFloatingButtons(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }

    // Floating speed-dial expansion state
    var isExpanded by remember { mutableStateOf(false) }

    val whatsappNumber = viewModel.supportWhatsAppNumber // "967738704940"
    val phoneNumber = viewModel.supportPhoneNumber // "967749154739"
    val whatsappDisplay = viewModel.supportWhatsAppDisplay
    val phoneDisplay = viewModel.supportPhoneDisplay

    Column(
        modifier = modifier
            .padding(bottom = 12.dp, end = 8.dp)
            .testTag("support_contact_component"),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expanded Speed-Dial Floating Actions
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Chip: NetGuard Unified Operations Desk
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardBackground,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
                    ),
                    shadowElevation = 6.dp,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.statusGreen)
                        )
                        Text(
                            text = if (isArabic) "مركز الدعم الموحد: م. نجم الرئيس" else "Unified Support: Eng. Najm Al-Raees",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }

                // 1. Floating WhatsApp Button (WhatsApp for 967738704940)
                FloatingActionButton(
                    onClick = {
                        isExpanded = false
                        viewModel.openWhatsAppSupport(context)
                    },
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("floating_whatsapp_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp $whatsappNumber",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Text(
                            text = if (isArabic) "واتساب: $whatsappDisplay" else "WhatsApp: $whatsappDisplay",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 2. Floating Phone Dialer Button (Dialer for 967749154739)
                FloatingActionButton(
                    onClick = {
                        isExpanded = false
                        viewModel.dialSupportPhone(context)
                    },
                    containerColor = colors.primaryAccent,
                    contentColor = if (isDarkMode) Color.Black else Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("floating_phone_dialer_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call $phoneNumber",
                            modifier = Modifier.size(20.dp),
                            tint = if (isDarkMode) Color.Black else Color.White
                        )
                        Text(
                            text = if (isArabic) "اتصال هاتف: $phoneDisplay" else "Dial: $phoneDisplay",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.Black else Color.White
                        )
                    }
                }

                // 3. Floating Quick Portal / Unified Support Desk Switcher
                FloatingActionButton(
                    onClick = {
                        isExpanded = false
                        viewModel.selectTab(AppTab.SUPPORT)
                    },
                    containerColor = colors.cardBackground,
                    contentColor = colors.primaryAccent,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(42.dp)
                        .border(
                            1.dp,
                            colors.primaryAccent.copy(alpha = 0.6f),
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("floating_open_support_tab_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Unified Support Portal",
                            modifier = Modifier.size(18.dp),
                            tint = colors.primaryAccent
                        )
                        Text(
                            text = if (isArabic) "الأسئلة الشائعة والاستعلامات" else "FAQs & Inquiries Hub",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }

        // Primary Master Floating Action Button (FAB)
        ExtendedFloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            icon = {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.SupportAgent,
                    contentDescription = if (isExpanded) "Close Support Menu" else "Open Support Contact",
                    modifier = Modifier.size(22.dp)
                )
            },
            text = {
                Text(
                    text = if (isExpanded) {
                        if (isArabic) "إغلاق" else "Close"
                    } else {
                        if (isArabic) "خدمة العملاء" else "Support Contact"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            },
            expanded = true,
            containerColor = if (isExpanded) colors.statusCrimson else colors.primaryAccent,
            contentColor = if (isExpanded) Color.White else (if (isDarkMode) Color.Black else Color.White),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .height(48.dp)
                .testTag("support_contact_fab")
        )
    }
}

/**
 * Embedded / Unified Management Support Card
 * Can be embedded directly within dashboards or admin panels.
 */
@Composable
fun SupportContactCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }

    val whatsappNumber = viewModel.supportWhatsAppNumber
    val phoneNumber = viewModel.supportPhoneNumber
    val whatsappDisplay = viewModel.supportWhatsAppDisplay
    val phoneDisplay = viewModel.supportPhoneDisplay

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("support_contact_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.primaryAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isArabic) "قنوات التواصل وخدمة العملاء الموحدة" else "Unified Support & Incident Escalation",
                            color = colors.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "إشراف: المهندس نجم الرئيس" else "Chief Architect: Eng. Najm Al-Raees",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = colors.statusGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "24/7 Active",
                        color = colors.statusGreen,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = if (isArabic) {
                    "أزرار اتصال فورية لحالات الطوارئ، فحص الاختراقات، والاستفسارات الفنية لضمان بقاء التطبيق كأداة إدارة متكاملة ومترابطة."
                } else {
                    "Instant communication buttons for emergency containment, intrusion response, and technical inquiries to maintain a unified management experience."
                },
                color = colors.textSecondary,
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WhatsApp Button
                Button(
                    onClick = { viewModel.openWhatsAppSupport(context) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("support_card_whatsapp_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp $whatsappNumber",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "واتساب: $whatsappDisplay" else "WhatsApp",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Phone Dialer Button
                Button(
                    onClick = { viewModel.dialSupportPhone(context) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("support_card_phone_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Dial $phoneNumber",
                        tint = if (isDarkMode) Color.Black else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "اتصال: $phoneDisplay" else "Call",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
