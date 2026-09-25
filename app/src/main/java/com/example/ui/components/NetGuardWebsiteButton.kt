package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.MainViewModel
import com.example.ui.theme.getNetGuardColors

/**
 * NetGuard Official Website Button Component
 *
 * Designed for the Dashboard to launch the official NetGuard company website
 * (https://netguard.enterprise.security) via an Intent, giving administrators and users
 * direct access to official cybersecurity resources, enterprise documentation,
 * API references, and security advisories.
 */
@Composable
fun NetGuardOfficialWebsiteButton(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }

    val websiteUrl = viewModel.officialWebsiteUrl
    val websiteDomain = viewModel.officialWebsiteDomain

    Button(
        onClick = { viewModel.openOfficialWebsite(context) },
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("dashboard_official_website_btn"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primaryAccent,
            contentColor = if (isDarkMode) Color.Black else Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
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
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "NetGuard Website",
                        tint = if (isDarkMode) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isArabic) "زيارة موقع NetGuard الرسمي للموارد الأمنية" else "Visit Official NetGuard Portal",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.Black else Color.White
                    )
                    Text(
                        text = websiteDomain,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = if (isDarkMode) Color.Black.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open Website in Browser",
                tint = if (isDarkMode) Color.Black else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * NetGuard Website Resource Card Component
 *
 * An executive dashboard banner highlighting official enterprise resources,
 * technical whitepapers, and cloud telemetry integration, with a prominent
 * direct button action opening the company portal via an Android Intent.
 */
@Composable
fun NetGuardWebsiteResourceCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.appLanguage.collectAsState()
    val isArabic = language == AppLanguage.ARABIC
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }

    val websiteUrl = viewModel.officialWebsiteUrl
    val websiteDomain = viewModel.officialWebsiteDomain

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_website_resource_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(colors.primaryAccent.copy(alpha = 0.45f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Identity & Trust Badges
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.primaryAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "بوابة الموارد والتوثيق الرسمي للشركة" else "Official Company Portal & Resources",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isArabic) "منظومة NetGuard السيبرانية المعتمدة" else "NetGuard Enterprise Security Suite",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Surface(
                    color = colors.statusGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.statusGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "256-bit SSL",
                            color = colors.statusGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Description of resources available
            Text(
                text = if (isArabic) {
                    "احصل على أحدث التحديثات الأمنية، وتوثيق واجهات الـ API، وأدلة تكوين الجدران النارية والـ IDS، وتراخيص المؤسسات مباشرة من الموقع الرسمي لشركة NetGuard."
                } else {
                    "Access official enterprise cybersecurity documentation, sensor signature updates, firewall configuration whitepapers, and enterprise licensing directly on the NetGuard portal."
                },
                fontSize = 11.5.sp,
                color = colors.textSecondary,
                lineHeight = 16.sp
            )

            // Resource tags / topics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tags = if (isArabic) {
                    listOf("دليل الحماية", "المستندات التقنية", "تراخيص المؤسسات")
                } else {
                    listOf("Security Docs", "Whitepapers", "API Reference")
                }

                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.primaryAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            color = colors.primaryAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Prominent Official Website Action Button
            NetGuardOfficialWebsiteButton(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
