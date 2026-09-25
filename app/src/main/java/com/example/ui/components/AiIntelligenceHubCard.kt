package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.getNetGuardColors

@Composable
fun AiIntelligenceHubCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val colors = remember(isDark) { getNetGuardColors(isDark) }
    val language = LocalAppLanguage.current
    val isArabic = language == AppLanguage.ARABIC

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_intelligence_hub_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = BorderStroke(1.dp, colors.primaryAccent.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .background(
                                Brush.linearGradient(
                                    listOf(colors.primaryAccent, colors.secondaryAccent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = if (colors.isDark) colors.background else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isArabic) "مركز استخبارات الذكاء الاصطناعي (Gemini Suite)" else "Cyber AI Intelligence Hub (Gemini Suite)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isArabic) "10 تكاملات ذكاء اصطناعي سيبراني متقدمة" else "10 Advanced Cyber AI Integrations Active",
                            fontSize = 11.sp,
                            color = colors.primaryAccent
                        )
                    }
                }

                Surface(
                    color = colors.statusGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ONLINE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.statusGreen,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Capabilities pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val chips = listOf(
                    "💬 Chatbot (Pro/Flash/Lite)",
                    "🔍 Google Search Grounding",
                    "📍 Google Maps Grounding",
                    "🎙️ Live Voice (gemini-3.8-live)",
                    "📝 Transcribe (gemini-3.5-transcribe)",
                    "🎬 Veo 3 Video (16:9 & 9:16)",
                    "🖼️ Image Studio",
                    "🎵 Lyria Music Alerts",
                    "🔐 Firebase Auth & Firestore"
                )

                chips.forEach { chipText ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 10.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CTA Button to open full AI Studio
            Button(
                onClick = { viewModel.selectTab(AppTab.AI_HUB) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryAccent,
                    contentColor = if (colors.isDark) colors.background else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_ai_studio_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Launch,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "فتح استوديو الذكاء الاصطناعي وتجربة الأدوات" else "Open AI Security Studio & Test All Tools",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
