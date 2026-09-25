package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.service.ai.AiChatMessage
import com.example.data.service.ai.ChatModelTier
import com.example.ui.MainViewModel
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.getNetGuardColors
import kotlinx.coroutines.launch

enum class AiStudioSubTab(val titleAr: String, val titleEn: String) {
    CHAT("محادثة ذكية Gemini", "Gemini Chat"),
    GROUNDING("البحث والخرائط", "Search & Maps"),
    VOICE_AUDIO("الصوت والتفريغ", "Voice & Audio"),
    VEO_VIDEO("استوديو الفيديو Veo", "Veo Video"),
    VISION_MUSIC("الصور والموسيقى", "Image & Music"),
    FIREBASE_AUTH("المصادقة والسحابة", "Auth & Cloud")
}

@Composable
fun AiSecurityHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val colors = remember(isDark) { getNetGuardColors(isDark) }
    val language = LocalAppLanguage.current
    val isArabic = language == AppLanguage.ARABIC

    var currentSubTab by remember { mutableStateOf(AiStudioSubTab.CHAT) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("ai_security_hub_screen")
    ) {
        // Studio Header
        Surface(
            color = colors.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
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
                                contentDescription = "AI Hub",
                                tint = if (isDark) colors.background else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isArabic) "مركز الذكاء الاصطناعي السيبراني" else "Cyber AI Intelligence Studio",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (isArabic) "مدعوم بنماذج Gemini 3.5 و Veo 3 و Lyria و Live API" else "Powered by Gemini 3.5, Veo 3, Lyria & Live API",
                                fontSize = 11.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Security notice badge
                    Surface(
                        color = colors.statusGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, colors.statusGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                                text = if (isArabic) "مُفعل ونشط" else "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.statusGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Security Prototype Notice Banner (Mandated by Secret Management Guideline)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.statusAmber.copy(alpha = 0.1f))
                        .border(1.dp, colors.statusAmber.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Alert",
                            tint = colors.statusAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isArabic)
                                "تنبيه الأمان: تُدار مفاتيح واجهة برمجة التطبيقات عبر إعدادات AI Studio الآمنة. يتم تشغيل الاستجابات المباشرة والمحاكاة الذكية بسلاسة."
                            else
                                "Security Notice: API keys are securely managed via AI Studio Secrets. Live execution & smart failover are armed.",
                            fontSize = 10.5.sp,
                            color = colors.textPrimary,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sub-tabs scrollable bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AiStudioSubTab.entries.forEach { subTab ->
                        val isSelected = subTab == currentSubTab
                        FilterChip(
                            selected = isSelected,
                            onClick = { currentSubTab = subTab },
                            label = {
                                Text(
                                    text = if (isArabic) subTab.titleAr else subTab.titleEn,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryAccent,
                                selectedLabelColor = if (isDark) colors.background else Color.White,
                                containerColor = colors.cardBackground,
                                labelColor = colors.textSecondary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colors.primaryAccent else colors.cardBorder
                            ),
                            modifier = Modifier.testTag("ai_subtab_${subTab.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // SubTab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (currentSubTab) {
                AiStudioSubTab.CHAT -> GeminiChatSubScreen(viewModel = viewModel, colors = colors, isArabic = isArabic)
                AiStudioSubTab.GROUNDING -> GroundingSubScreen(viewModel = viewModel, colors = colors, isArabic = isArabic)
                AiStudioSubTab.VOICE_AUDIO -> VoiceAudioSubScreen(viewModel = viewModel, colors = colors, isArabic = isArabic)
                AiStudioSubTab.VEO_VIDEO -> VeoVideoSubScreen(viewModel = viewModel, colors = colors, isArabic = isArabic)
                AiStudioSubTab.VISION_MUSIC -> VisionMusicSubScreen(viewModel = viewModel, colors = colors, isArabic = isArabic)
                AiStudioSubTab.FIREBASE_AUTH -> FirebaseAuthSubScreen(viewModel = viewModel, colors = colors, isArabic = isArabic)
            }
        }
    }
}

// =============================================================================
// SUB-SCREEN 1: GEMINI MULTI-TURN CHAT (PRO / FLASH / LITE)
// =============================================================================
@Composable
private fun GeminiChatSubScreen(
    viewModel: MainViewModel,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean
) {
    val messages by viewModel.chatMessages.collectAsState()
    val currentTier by viewModel.chatModelTier.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    var promptInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Model Tier Selection Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isArabic) "اختر فئة نموذج Gemini للمحادثة:" else "Select Gemini Model Tier:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChatModelTier.entries.forEach { tier ->
                        val isSelected = tier == currentTier
                        Surface(
                            onClick = { viewModel.selectChatModelTier(tier) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) colors.primaryAccent.copy(alpha = 0.2f) else colors.surface,
                            border = BorderStroke(1.dp, if (isSelected) colors.primaryAccent else colors.cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("model_tier_${tier.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isArabic) tier.arabicName else tier.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.primaryAccent else colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = tier.modelName,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = colors.textMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pre-canned cybersecurity prompts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = if (isArabic) listOf(
                "تحليل محاولات اختراق SSH الأخيرة",
                "كيفية التصدي لهجوم SYN Flood",
                "تدقيق جدار الحماية وعزل الدخلاء",
                "فحص ثغرة CVE-2024-6387 في OpenSSH"
            ) else listOf(
                "Analyze recent SSH login spikes",
                "How to mitigate SYN flood bursts",
                "Audit firewall rules and rogue devices",
                "Check CVE-2024-6387 OpenSSH vulnerability"
            )

            suggestions.forEach { suggestion ->
                Surface(
                    onClick = {
                        promptInput = suggestion
                        viewModel.sendChatMessage(suggestion)
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 10.5.sp,
                        color = colors.primaryAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isUser) 12.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 12.dp
                        ),
                        color = if (isUser) colors.primaryAccent else colors.cardBackground,
                        border = BorderStroke(1.dp, if (isUser) colors.primaryAccent else colors.cardBorder),
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isUser) (if (isArabic) "أنت" else "You") else msg.modelUsed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) (if (colors.isDark) colors.background else Color.White) else colors.primaryAccent
                                )
                                Text(
                                    text = msg.timestamp,
                                    fontSize = 9.sp,
                                    color = if (isUser) (if (colors.isDark) colors.background.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)) else colors.textMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.content,
                                fontSize = 12.5.sp,
                                color = if (isUser) (if (colors.isDark) colors.background else Color.White) else colors.textPrimary,
                                lineHeight = 18.sp
                            )

                            if (!isUser) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("NetGuard AI Response", msg.content))
                                            Toast.makeText(context, if (isArabic) "تم نسخ الرد" else "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = colors.textMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.cardBackground,
                            border = BorderStroke(1.dp, colors.cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = colors.primaryAccent,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isArabic) "جاري المعالجة والاستدلال عبر ${currentTier.modelName}..." else "Thinking with ${currentTier.modelName}...",
                                    fontSize = 11.5.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = {
                    Text(
                        text = if (isArabic) "اكتب استفسارك السيبراني هنا..." else "Ask NetGuard AI assistant...",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryAccent,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                maxLines = 3
            )

            IconButton(
                onClick = {
                    if (promptInput.isNotBlank()) {
                        val text = promptInput
                        promptInput = ""
                        viewModel.sendChatMessage(text)
                    }
                },
                enabled = promptInput.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (promptInput.isNotBlank() && !isLoading) colors.primaryAccent else colors.cardBorder)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (promptInput.isNotBlank() && !isLoading) (if (colors.isDark) colors.background else Color.White) else colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// =============================================================================
// SUB-SCREEN 2: GOOGLE SEARCH & MAPS GROUNDING (gemini-3.5-flash)
// =============================================================================
@Composable
private fun GroundingSubScreen(
    viewModel: MainViewModel,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean
) {
    var queryType by remember { mutableStateOf(0) } // 0 = Search Grounding, 1 = Maps Grounding
    var searchQuery by remember { mutableStateOf("CVE-2024-6387 OpenSSH vulnerability mitigation") }
    var mapsQuery by remember { mutableStateOf("Tier-4 data centers and internet exchange points in Riyadh") }

    val searchResult by viewModel.searchResult.collectAsState()
    val isSearchLoading by viewModel.isSearchLoading.collectAsState()

    val mapsText by viewModel.mapsResultText.collectAsState()
    val mapsLocations by viewModel.mapsLocations.collectAsState()
    val isMapsLoading by viewModel.isMapsLoading.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Mode Switcher (Search vs Maps Grounding)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { queryType = 0 },
                    shape = RoundedCornerShape(10.dp),
                    color = if (queryType == 0) colors.primaryAccent else colors.cardBackground,
                    border = BorderStroke(1.dp, if (queryType == 0) colors.primaryAccent else colors.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_search_grounding")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Grounding",
                            tint = if (queryType == 0) (if (colors.isDark) colors.background else Color.White) else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "بحث Google مباشر" else "Google Search Grounding",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (queryType == 0) (if (colors.isDark) colors.background else Color.White) else colors.textPrimary
                        )
                    }
                }

                Surface(
                    onClick = { queryType = 1 },
                    shape = RoundedCornerShape(10.dp),
                    color = if (queryType == 1) colors.secondaryAccent else colors.cardBackground,
                    border = BorderStroke(1.dp, if (queryType == 1) colors.secondaryAccent else colors.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_maps_grounding")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Maps Grounding",
                            tint = if (queryType == 1) Color.White else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "خرائط Google جغرافية" else "Google Maps Grounding",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (queryType == 1) Color.White else colors.textPrimary
                        )
                    }
                }
            }
        }

        if (queryType == 0) {
            // SEARCH GROUNDING VIEW
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.TravelExplore, contentDescription = null, tint = colors.primaryAccent)
                            Column {
                                Text(
                                    text = if (isArabic) "استخبارات التهديدات المعتمدة على بحث Google" else "Search Grounded Threat Intelligence",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Model: gemini-3.5-flash (with googleSearch tool)",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = colors.textMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text(if (isArabic) "استعلام التهديد الأمني أو رقم CVE" else "Threat Query / CVE Number", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_grounding_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.executeSearchGroundedThreatQuery(searchQuery) },
                            enabled = !isSearchLoading && searchQuery.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("execute_search_grounding_btn")
                        ) {
                            if (isSearchLoading) {
                                CircularProgressIndicator(color = colors.background, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isArabic) "جاري البحث في مصادر الويب الحية..." else "Grounding with Google Search...")
                            } else {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isArabic) "تنفيذ البحث المدعوم (Search Grounding)" else "Run Search Grounded Query")
                            }
                        }
                    }
                }
            }

            if (searchResult != null) {
                item {
                    val result = searchResult!!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.primaryAccent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = colors.statusGreen, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = if (isArabic) "نتائج مدققة ومحدثة من مصادر الويب" else "Verified Web Grounded Response",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                }
                                Text(text = result.timestamp, fontSize = 10.sp, color = colors.textMuted)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result.content,
                                fontSize = 12.5.sp,
                                color = colors.textPrimary,
                                lineHeight = 19.sp
                            )

                            if (result.searchSources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = colors.cardBorder)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isArabic) "المصادر المرجعية للبحث (Google Search Sources):" else "Grounding Sources & Citations:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.secondaryAccent
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                result.searchSources.forEach { source ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = colors.cardBackground,
                                        border = BorderStroke(1.dp, colors.cardBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(14.dp))
                                            Column {
                                                Text(text = source.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                                Text(text = source.url, fontSize = 9.5.sp, color = colors.textSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MAPS GROUNDING VIEW
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PinDrop, contentDescription = null, tint = colors.secondaryAccent)
                            Column {
                                Text(
                                    text = if (isArabic) "الاستعلام الجغرافي المعتمد على خرائط Google" else "Maps Grounded Geo-Location & Routing",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Model: gemini-3.5-flash (with googleMaps tool)",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = colors.textMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = mapsQuery,
                            onValueChange = { mapsQuery = it },
                            label = { Text(if (isArabic) "موقع مراكز البيانات أو تتبع مصدر IP" else "Data Center or IP Origin Location", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("maps_grounding_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.executeMapsGroundedQuery(mapsQuery) },
                            enabled = !isMapsLoading && mapsQuery.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("execute_maps_grounding_btn")
                        ) {
                            if (isMapsLoading) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isArabic) "جاري الاستعلام الجغرافي من الخرائط..." else "Grounding with Google Maps...")
                            } else {
                                Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isArabic) "تشغيل استعلام الخرائط (Maps Grounding)" else "Run Maps Grounded Query")
                            }
                        }
                    }
                }
            }

            if (mapsText != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.secondaryAccent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = mapsText ?: "",
                                fontSize = 12.sp,
                                color = colors.textPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (mapsLocations.isNotEmpty()) {
                item {
                    Text(
                        text = if (isArabic) "مواقع مراكز البيانات والعقد المحددة جغرفياً:" else "Identified Data Centers & Nodes:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary
                    )
                }

                items(mapsLocations) { loc ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = loc.placeName,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryAccent
                                )
                                Surface(
                                    color = colors.statusGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = loc.securityLevel,
                                        fontSize = 9.sp,
                                        color = colors.statusGreen,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = loc.address, fontSize = 11.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Coordinates: ${loc.latitude}, ${loc.longitude}",
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// SUB-SCREEN 3: VOICE CONVERSATIONS (gemini-3.8-live) & AUDIO TRANSCRIBE (gemini-3.5-transcribe)
// =============================================================================
@Composable
private fun VoiceAudioSubScreen(
    viewModel: MainViewModel,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean
) {
    val isListening by viewModel.isLiveVoiceListening.collectAsState()
    val transcripts by viewModel.liveVoiceTranscript.collectAsState()
    val audioTranscript by viewModel.audioTranscript.collectAsState()
    val isTranscribing by viewModel.isTranscribing.collectAsState()
    var spokenCommandInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. LIVE VOICE API CARD (gemini-3.8-live)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                border = BorderStroke(1.dp, if (isListening) colors.statusCrimson else colors.cardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = colors.primaryAccent)
                            Column {
                                Text(
                                    text = if (isArabic) "المحادثة الصوتية الحية (Live Voice API)" else "Live Voice Conversations (Live API)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Model: gemini-3.8-live",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = colors.textMuted
                                )
                            }
                        }

                        Surface(
                            color = if (isListening) colors.statusCrimson.copy(alpha = 0.2f) else colors.surface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isListening) colors.statusCrimson else colors.cardBorder)
                        ) {
                            Text(
                                text = if (isListening) (if (isArabic) "استماع نشط..." else "LISTENING") else (if (isArabic) "خامل" else "STANDBY"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isListening) colors.statusCrimson else colors.textMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Voice Waveform Visualization
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val barHeights = if (isListening) {
                                listOf(20.dp, 45.dp, 35.dp, 55.dp, 28.dp, 48.dp, 18.dp, 38.dp, 50.dp, 22.dp)
                            } else {
                                listOf(8.dp, 12.dp, 10.dp, 14.dp, 8.dp, 12.dp, 10.dp, 14.dp, 8.dp, 10.dp)
                            }

                            barHeights.forEach { height ->
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height(height)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (isListening) colors.statusCrimson else colors.primaryAccent.copy(alpha = 0.5f)
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Microphone toggle button
                    IconButton(
                        onClick = { viewModel.toggleLiveVoiceListening() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) colors.statusCrimson else colors.primaryAccent
                            )
                            .testTag("live_voice_mic_btn")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Toggle Mic",
                            tint = if (colors.isDark) colors.background else Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isListening) (if (isArabic) "اضغط لإيقاف الاستماع الصوتي" else "Tap to stop listening") else (if (isArabic) "اضغط لبدء التحدث مع المساعد الصوتي" else "Tap microphone to speak"),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick voice command simulation chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val voiceCmds = if (isArabic) listOf(
                            "ما هي حالة الشبكة الآن؟",
                            "اعزل الجهاز 192.168.1.105 فوراً",
                            "هل تم رصد ذروة حركة مرور غير معتادة؟"
                        ) else listOf(
                            "What is the current network status?",
                            "Isolate device 192.168.1.105 immediately",
                            "Did we experience any unusual traffic bursts?"
                        )

                        voiceCmds.forEach { cmd ->
                            Surface(
                                onClick = { viewModel.submitLiveVoiceCommand(cmd) },
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surface,
                                border = BorderStroke(1.dp, colors.cardBorder)
                            ) {
                                Text(
                                    text = "🗣️ $cmd",
                                    fontSize = 10.5.sp,
                                    color = colors.primaryAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Voice Dialog Feed
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isArabic) "سجل الحوار الصوتي المباشر:" else "Live Voice Dialog Stream:",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMuted
                        )
                        transcripts.forEach { t ->
                            Text(
                                text = t,
                                fontSize = 11.5.sp,
                                color = colors.textPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. AUDIO TRANSCRIPTION CARD (gemini-3.5-transcribe)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Hearing, contentDescription = null, tint = colors.secondaryAccent)
                        Column {
                            Text(
                                text = if (isArabic) "تفريغ الصوت إلى نصوص (Audio Transcription)" else "Audio Transcription to Text",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Model: gemini-3.5-transcribe",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isArabic)
                            "تفريغ تسجيلات المكالمات الطارئة والمذكرات الصوتية لمهندسي مركز العمليات (NOC) وتحويلها إلى تقارير حوادث رقمية دقيقة."
                        else
                            "Transcribe emergency NOC voice recordings, technician voice notes, or triage calls into structured incident logs.",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.transcribeIncidentVoice(null) },
                        enabled = !isTranscribing,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transcribe_audio_btn")
                    ) {
                        if (isTranscribing) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "جاري تفريغ الصوت عبر gemini-3.5-transcribe..." else "Transcribing with gemini-3.5-transcribe...")
                        } else {
                            Icon(imageVector = Icons.Default.SpatialAudio, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "تفريغ المذكرة الصوتية لغرفة العمليات" else "Transcribe Incident Voice Memo")
                        }
                    }

                    if (audioTranscript != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.secondaryAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = audioTranscript ?: "",
                                fontSize = 12.sp,
                                color = colors.textPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// SUB-SCREEN 4: VEO 3 VIDEO GENERATION (veo-3.1-fast-generate-preview)
// =============================================================================
@Composable
private fun VeoVideoSubScreen(
    viewModel: MainViewModel,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean
) {
    val videos by viewModel.videoList.collectAsState()
    val isGenerating by viewModel.isVideoGenerating.collectAsState()
    var videoPrompt by remember { mutableStateOf("3D holographic security patrol scanning corporate network nodes and blocking malware intrusion burst") }
    var selectedAspectRatio by remember { mutableStateOf("16:9") } // "16:9" or "9:16"
    var isImageToVideoMode by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = colors.primaryAccent)
                        Column {
                            Text(
                                text = if (isArabic) "توليد وتحريك الفيديو (Veo 3 Video Studio)" else "Veo 3 Video Generation & Animation",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Model: veo-3.1-fast-generate-preview (Aspect ratio: 16:9 or 9:16)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Generation Mode: Text-to-Video vs Image-to-Video
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isImageToVideoMode,
                            onClick = { isImageToVideoMode = false },
                            label = { Text(if (isArabic) "نص إلى فيديو (Text-to-Video)" else "Text-to-Video", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colors.primaryAccent),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isImageToVideoMode,
                            onClick = { isImageToVideoMode = true },
                            label = { Text(if (isArabic) "تحريك مخطط شبكة (Animate Image)" else "Animate Image", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colors.primaryAccent),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Aspect Ratio Selector (Mandatory: 16:9 or 9:16)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "نسبة العرض إلى الارتفاع (Aspect Ratio):" else "Aspect Ratio:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("16:9", "9:16").forEach { ratio ->
                                Surface(
                                    onClick = { selectedAspectRatio = ratio },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (selectedAspectRatio == ratio) colors.primaryAccent else colors.surface,
                                    border = BorderStroke(1.dp, if (selectedAspectRatio == ratio) colors.primaryAccent else colors.cardBorder)
                                ) {
                                    Text(
                                        text = ratio + if (ratio == "16:9") " (Landscape)" else " (Portrait)",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedAspectRatio == ratio) (if (colors.isDark) colors.background else Color.White) else colors.textPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = videoPrompt,
                        onValueChange = { videoPrompt = it },
                        label = { Text(if (isArabic) "وصف المشهد المراد توليده بالفيديو" else "Video prompt description", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("veo_prompt_input"),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.generateVeoVideo(
                                prompt = videoPrompt,
                                aspectRatio = selectedAspectRatio,
                                isImageAnimation = isImageToVideoMode
                            )
                        },
                        enabled = !isGenerating && videoPrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_veo_video_btn")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = colors.background, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "جاري توليد الفيديو عبر Veo 3..." else "Rendering with Veo 3...")
                        } else {
                            Icon(imageVector = Icons.Default.MovieFilter, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "توليد فيديو Veo ($selectedAspectRatio)" else "Generate Video with Veo ($selectedAspectRatio)")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (isArabic) "الفيديوهات المولدة حديثاً:" else "Generated Video Artifacts:",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary
            )
        }

        items(videos) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Video simulated canvas player box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (item.aspectRatio == "16:9") 140.dp else 190.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(colors.surface, Color(0xFF0F172A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryAccent.copy(alpha = 0.25f))
                                    .border(1.5.dp, colors.primaryAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "Veo 3 Rendered Clip (${item.aspectRatio} • 1080p)",
                                fontSize = 10.sp,
                                color = colors.primaryAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.prompt,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Model: ${item.model}", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = colors.textMuted)
                        Text(text = item.timestamp, fontSize = 9.5.sp, color = colors.textMuted)
                    }
                }
            }
        }
    }
}

// =============================================================================
// SUB-SCREEN 5: CREATE & EDIT IMAGES (gemini-3.1-flash-image-preview) & LYRIA MUSIC
// =============================================================================
@Composable
private fun VisionMusicSubScreen(
    viewModel: MainViewModel,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean
) {
    val images by viewModel.imageList.collectAsState()
    val isImageGenerating by viewModel.isImageGenerating.collectAsState()
    val musicList by viewModel.musicList.collectAsState()
    val isMusicGenerating by viewModel.isMusicGenerating.collectAsState()

    var imagePrompt by remember { mutableStateOf("Neon holographic cybersecurity network dashboard with shield defense telemetry gauges") }
    var musicPrompt by remember { mutableStateOf("Intense cyberpunk electronic alert alarm with pulsing synthesizers") }
    var isShortMusicClip by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. IMAGE GENERATION CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = colors.primaryAccent)
                        Column {
                            Text(
                                text = if (isArabic) "إنشاء وتعديل الصور (Create & Edit Images)" else "Create & Edit Images",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Model: gemini-3.1-flash-image-preview (1K resolution)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = imagePrompt,
                        onValueChange = { imagePrompt = it },
                        label = { Text(if (isArabic) "وصف الصورة أو المخطط السيبراني" else "Image or diagram description", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_prompt_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.generateImage(imagePrompt) },
                        enabled = !isImageGenerating && imagePrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_image_btn")
                    ) {
                        if (isImageGenerating) {
                            CircularProgressIndicator(color = colors.background, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "جاري توليد الصورة عبر gemini-3.1-flash-image-preview..." else "Generating image...")
                        } else {
                            Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "توليد الصورة الذكية" else "Generate Image Artifact")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Display images
                    images.forEach { img ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF0F172A), colors.primaryAccent.copy(alpha = 0.3f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = colors.primaryAccent,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = img.prompt, fontSize = 11.5.sp, color = colors.textPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Resolution: ${img.resolution} • Aspect: ${img.aspectRatio}", fontSize = 9.sp, color = colors.textMuted)
                            }
                        }
                    }
                }
            }
        }

        // 2. MUSIC GENERATION CARD (lyria-3-clip-preview / lyria-3-pro-preview)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = colors.secondaryAccent)
                        Column {
                            Text(
                                text = if (isArabic) "توليد الموسيقى والتنبيهات (Music Generation)" else "Music & Audio Alert Generation",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Models: lyria-3-clip-preview (up to 30s) or lyria-3-pro-preview (full track)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isShortMusicClip,
                            onClick = { isShortMusicClip = true },
                            label = { Text("lyria-3-clip-preview (30s)", fontSize = 10.5.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isShortMusicClip,
                            onClick = { isShortMusicClip = false },
                            label = { Text("lyria-3-pro-preview (Full)", fontSize = 10.5.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = musicPrompt,
                        onValueChange = { musicPrompt = it },
                        label = { Text(if (isArabic) "طابع التنبيه الصوتي أو الموسيقى" else "Audio mood / alert prompt", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("music_prompt_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.generateLyriaMusic(musicPrompt, isShortMusicClip) },
                        enabled = !isMusicGenerating && musicPrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_music_btn")
                    ) {
                        if (isMusicGenerating) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "جاري توليد النغمة عبر Lyria..." else "Synthesizing with Lyria...")
                        } else {
                            Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "توليد نغمة تنبيه سيبراني (Lyria)" else "Generate Audio Theme (Lyria)")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    musicList.forEach { track ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = colors.secondaryAccent)
                                    Column {
                                        Text(text = track.title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                        Text(text = "Model: ${track.model} • ${track.durationSeconds}s", fontSize = 9.sp, color = colors.textMuted)
                                    }
                                }
                                Surface(
                                    color = colors.secondaryAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "READY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.secondaryAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// SUB-SCREEN 6: FIREBASE AUTH & FIRESTORE CLOUD PERSISTENCE
// =============================================================================
@Composable
private fun FirebaseAuthSubScreen(
    viewModel: MainViewModel,
    colors: com.example.ui.theme.NetGuardThemeColors,
    isArabic: Boolean
) {
    val email by viewModel.userEmail.collectAsState()
    val isConnected by viewModel.isFirebaseConnected.collectAsState()
    val syncStatus by viewModel.firestoreSyncStatus.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = colors.statusGreen)
                        Column {
                            Text(
                                text = if (isArabic) "المصادقة وقاعدة البيانات السحابية (Firebase)" else "Firebase Auth & Cloud Firestore",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Google Sign-in with Firebase Auth + Cloud Firestore Data Sync",
                                fontSize = 10.sp,
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // User Profile Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User",
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "المستخدم المعتمد (Firebase Verified)" else "Authenticated Enterprise Admin",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = email,
                                    fontSize = 11.sp,
                                    color = colors.primaryAccent,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Surface(
                                color = colors.statusGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.statusGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Persistence status
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = colors.statusGreen, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (isArabic) syncStatus.arabicLabel else syncStatus.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.statusGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic)
                                    "يتم حفظ وتوثيق جميع محادثات Gemini، واستعلامات البحث والخرائط، وتقارير الحوادث تلقائياً في Cloud Firestore."
                                else
                                    "All Gemini chats, Grounding telemetry, and incident audit records are synchronized to Cloud Firestore.",
                                fontSize = 10.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
