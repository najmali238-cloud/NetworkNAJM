package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.MainViewModel
import com.example.ui.theme.*

/**
 * Global Theme Switcher component for NetGuard Application Settings.
 * Allows switching between Dark Cyber (optimized for dark NOC rooms, server cages, and 24/7 security monitoring)
 * and Light Enterprise (optimized for daylight environments, bright offices, and administrative presentations).
 */
@Composable
fun GlobalThemeSwitcherCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isArabic = appLanguage == AppLanguage.ARABIC

    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("global_theme_switcher_card"),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
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
                            .clip(CircleShape)
                            .background(colors.primaryAccent.copy(alpha = 0.16f))
                            .border(1.dp, colors.primaryAccent.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Theme Icon",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF0284C7),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "مظهر العرض العام ونمط الرؤية" else "Global Display Theme & Visibility",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "مواءمة الشاشة مع بيئة الإضاءة التشغيلية" else "Adapt display to physical monitoring lighting",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Active Mode Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE0F2FE)
                        )
                        .border(
                            1.dp,
                            if (isDarkMode) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFF0284C7).copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isDarkMode) {
                            if (isArabic) "🌙 ليلي سيبراني" else "🌙 Dark Cyber"
                        } else {
                            if (isArabic) "☀️ نهاري مؤسسي" else "☀️ Light Enterprise"
                        },
                        color = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Description
            Text(
                text = if (isArabic) {
                    "اختر نمط المظهر المناسب لبيئة العمل الفيزيائية لتحسين راحة العين وسرعة رصد التنبيهات في مختلف ظروف الإضاءة."
                } else {
                    "Select the visual theme optimized for your monitoring environment to minimize eye strain and maximize anomaly detection speed."
                },
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            // Two Interactive Mode Selection Cards (Side-by-Side or Responsive Stack)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 1: Dark Mode Card
                val isDarkSelected = isDarkMode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_option_dark")
                        .clickable { viewModel.setDarkMode(true) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkSelected) {
                        if (isDarkMode) CyberNavySurface else Color(0xFF0F172A)
                    } else {
                        if (isDarkMode) CyberNavySurface.copy(alpha = 0.5f) else Color(0xFFF1F5F9)
                    },
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isDarkSelected) CyberCyan else colors.cardBorder.copy(alpha = 0.6f)
                        )
                    ),
                    tonalElevation = if (isDarkSelected) 4.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF030712)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            RadioButton(
                                selected = isDarkSelected,
                                onClick = { viewModel.setDarkMode(true) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CyberCyan,
                                    unselectedColor = colors.textMuted
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = if (isArabic) "الوضع الداكن" else "Dark Cyber",
                            color = if (isDarkSelected) (if (isDarkMode) TextPrimary else Color.White) else colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Environment Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF030712).copy(alpha = 0.8f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isArabic) "مراكز العمليات NOC" else "NOC & Low-Light",
                                color = CyberCyanLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Palette preview dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CyberNavyDark))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CyberNavySurface))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CyberCyan))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StatusOnlineGreen))
                        }

                        Text(
                            text = if (isArabic) "انعدام الوهج للشاشات المظلمة والمناوبات الليلية" else "Zero glare, OLED deep contrast for dark control centers",
                            color = if (isDarkSelected) (if (isDarkMode) TextSecondary else Color(0xFF94A3B8)) else colors.textSecondary,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                // Option 2: Light Mode Card
                val isLightSelected = !isDarkMode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_option_light")
                        .clickable { viewModel.setDarkMode(false) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLightSelected) {
                        LightCanvasSurface
                    } else {
                        if (isDarkMode) CyberNavySurface.copy(alpha = 0.5f) else Color(0xFFF1F5F9)
                    },
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isLightSelected) LightAccentCyan else colors.cardBorder.copy(alpha = 0.6f)
                        )
                    ),
                    tonalElevation = if (isLightSelected) 4.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            RadioButton(
                                selected = isLightSelected,
                                onClick = { viewModel.setDarkMode(false) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = LightAccentCyan,
                                    unselectedColor = colors.textMuted
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = if (isArabic) "الوضع الفاتح" else "Light Enterprise",
                            color = if (isLightSelected) LightTextDarkPrimary else colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Environment Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE0F2FE))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isArabic) "المكاتب والضوء الساطع" else "Offices & Daylight",
                                color = Color(0xFF0369A1),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Palette preview dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(LightCanvasBackground).border(0.5.dp, Color(0xFFCBD5E1), CircleShape))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(LightCanvasSurface).border(0.5.dp, Color(0xFFCBD5E1), CircleShape))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(LightAccentCyan))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(LightStatusGreen))
                        }

                        Text(
                            text = if (isArabic) "وضوح فائق تحت أشعة الشمس والمكاتب عالية الإضاءة" else "High readability under direct sunlight and bright office lights",
                            color = if (isLightSelected) LightTextDarkSecondary else colors.textSecondary,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Quick Toggle Switch Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = colors.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder.copy(alpha = 0.5f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (isArabic) "تفعيل الوضع الداكن (السيبراني)" else "Enable Dark Cyber Theme",
                                color = colors.textPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isArabic) {
                                    if (isDarkMode) "الوضع الليلي نشط حالياً" else "الوضع الفاتح نشط حالياً"
                                } else {
                                    if (isDarkMode) "Currently in Dark Mode" else "Currently in Light Mode"
                                },
                                color = colors.textMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = colors.primaryAccent,
                            uncheckedThumbColor = colors.primaryAccent,
                            uncheckedTrackColor = colors.cardBorder
                        ),
                        modifier = Modifier.testTag("global_theme_switch")
                    )
                }
            }

            // Environmental Visibility Tip Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isDarkMode) CyberNavySurface else Color(0xFFF1F5F9)
                    )
                    .border(
                        1.dp,
                        if (isDarkMode) colors.primaryAccent.copy(alpha = 0.2f) else Color(0xFFE2E8F0),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("theme_visibility_environment_tip")
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isDarkMode) "💡" else "☀️",
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isDarkMode) {
                            if (isArabic) {
                                "إرشادات الرؤية: الوضع الداكن يمنع التشتت البصري أثناء مراقبة خريطة الطوبولوجيا ومخططات المرور في غرف الحراسة الليلية."
                            } else {
                                "Visibility Insight: Dark Cyber mode minimizes optical fatigue when analyzing continuous real-time telemetry and IDS radars in dimly lit security rooms."
                            }
                        } else {
                            if (isArabic) {
                                "إرشادات الرؤية: الوضع النهاري يوفر أعلى تباين لتقارير SLA وقوائم فحص الأجهزة أثناء الاجتماعات الإدارية وتحت الإضاءة المرتفعة."
                            } else {
                                "Visibility Insight: Light Enterprise mode maximizes typography sharpness and chart readability during bright daytime executive reviews and site audits."
                            }
                        },
                        color = colors.textSecondary,
                        fontSize = 10.5.sp,
                        lineHeight = 14.5.sp
                    )
                }
            }
        }
    }
}
