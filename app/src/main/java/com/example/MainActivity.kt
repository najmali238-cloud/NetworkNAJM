package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.PulsingRadarDot
import com.example.ui.components.SupportContactComponent
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize push notification channel and system service
        com.example.data.service.NetworkTrafficNotificationService.getInstance(this)
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val appLanguage by viewModel.appLanguage.collectAsState()
            MyApplicationTheme(darkTheme = isDarkMode, language = appLanguage) {
                NetGuardApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetGuardApp(viewModel: MainViewModel) {
    val currentTab = viewModel.currentTab.collectAsState().value
    val isScanning = viewModel.isScanning.collectAsState().value
    val userMessage = viewModel.userMessage.collectAsState().value
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val fullAppUpdateState by viewModel.fullAppUpdateState.collectAsState()

    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }
    val isArabic = appLanguage == AppLanguage.ARABIC

    val rogueCount = viewModel.rogueDevices.collectAsState().value.count {
        it.status.name != "ISOLATED" && it.status.name != "TRUSTED"
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.cardBackground,
                    contentColor = colors.textPrimary,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
            ) {
                // Developer credit banner at the very top of the screen in small font
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.primaryAccent.copy(alpha = if (isDarkMode) 0.12f else 0.08f))
                        .padding(vertical = 4.dp, horizontal = 12.dp)
                        .testTag("developer_credit_header"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "تطوير المهندس نجم الرئيس",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryAccent,
                        letterSpacing = 0.5.sp
                    )
                }

                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primaryAccent.copy(alpha = 0.15f))
                                    .border(1.dp, colors.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "NetGuard Logo",
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "حارس الشبكة" else "NETGUARD",
                                        color = colors.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    PulsingRadarDot(color = if (rogueCount > 0) colors.statusCrimson else colors.primaryAccent)
                                }
                                Text(
                                    text = if (isArabic) "مراقبة الأجهزة والأمان الطرفي" else "Enterprise Device & Network Guard",
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick language toggle in TopBar
                        TextButton(
                            onClick = { viewModel.toggleAppLanguage() },
                            modifier = Modifier.testTag("topbar_lang_btn")
                        ) {
                            Text(
                                text = if (isArabic) "EN" else "عربي",
                                color = colors.primaryAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quick Dark / Light mode toggle in TopBar
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.testTag("topbar_theme_btn")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkMode) "Light Mode" else "Dark Mode",
                                tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF6366F1),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Quick Customer Support & Inquiries Button in TopBar
                        IconButton(
                            onClick = { viewModel.selectTab(AppTab.SUPPORT) },
                            modifier = Modifier.testTag("topbar_support_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = if (isArabic) "خدمة العملاء والاستعلامات" else "Customer Support & FAQs",
                                tint = if (currentTab == AppTab.SUPPORT) colors.primaryAccent else colors.statusGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Quick AI Hub Button in TopBar
                        IconButton(
                            onClick = { viewModel.selectTab(AppTab.AI_HUB) },
                            modifier = Modifier.testTag("topbar_ai_hub_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Security Studio",
                                tint = if (currentTab == AppTab.AI_HUB) colors.secondaryAccent else colors.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Quick Admin Panel Button in TopBar
                        IconButton(
                            onClick = { viewModel.selectTab(AppTab.ADMIN_PANEL) },
                            modifier = Modifier.testTag("topbar_admin_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Control Panel",
                                tint = if (currentTab == AppTab.ADMIN_PANEL) colors.statusGreen else colors.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Full System & App Update Button (زر تحديث التطبيق بالكامل)
                        IconButton(
                            onClick = { viewModel.executeFullAppUpdate() },
                            enabled = !fullAppUpdateState.isUpdating,
                            modifier = Modifier.testTag("topbar_full_update_btn")
                        ) {
                            if (fullAppUpdateState.isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = colors.primaryAccent,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = if (isArabic) "تحديث التطبيق بالكامل" else "Full App Update",
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Subnet scan button
                        IconButton(
                            onClick = { viewModel.triggerScan() },
                            enabled = !isScanning,
                            modifier = Modifier.testTag("scan_button")
                        ) {
                            Icon(
                                imageVector = if (isScanning) Icons.Default.Sync else Icons.Default.Radar,
                                contentDescription = "Trigger Subnet Scan",
                                tint = if (isScanning) colors.primaryAccent.copy(alpha = 0.6f) else colors.primaryAccent
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.surface
                    )
                )

                // Animated Full Update Progress Banner
                AnimatedVisibility(visible = fullAppUpdateState.isUpdating) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.primaryAccent.copy(alpha = 0.16f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.primaryAccent
                                )
                                Text(
                                    text = if (isArabic) fullAppUpdateState.currentStepArabic else fullAppUpdateState.currentStepEnglish,
                                    fontSize = 11.sp,
                                    color = colors.primaryAccent,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${(fullAppUpdateState.progressPercent * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = colors.primaryAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = colors.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_bottom_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeThreatsCount = viewModel.realtimeThreatNotifications.collectAsState().value.count { !it.isDismissed }

                    AppTab.entries.forEach { tab ->
                        val isSelected = tab == currentTab
                        val icon = when (tab) {
                            AppTab.DASHBOARD -> Icons.Default.Dashboard
                            AppTab.MY_NETWORK -> Icons.Default.Lan
                            AppTab.AI_HUB -> Icons.Default.AutoAwesome
                            AppTab.TOPOLOGY -> Icons.Default.AccountTree
                            AppTab.DEVICES -> Icons.Default.Devices
                            AppTab.INTRUSION -> Icons.Default.Radar
                            AppTab.ROGUE_DETECTOR -> Icons.Default.GppMaybe
                            AppTab.REMEDIATION -> Icons.Default.Terminal
                            AppTab.REPORTS -> Icons.Default.Assessment
                            AppTab.ADMIN_PANEL -> Icons.Default.AdminPanelSettings
                            AppTab.SETTINGS -> Icons.Default.Settings
                            AppTab.SUPPORT -> Icons.Default.SupportAgent
                        }

                        val tabBadgeCount = when (tab) {
                            AppTab.ROGUE_DETECTOR -> rogueCount
                            AppTab.INTRUSION -> activeThreatsCount
                            else -> 0
                        }

                        val label = when (tab) {
                            AppTab.DASHBOARD -> if (isArabic) "الرئيسية" else "Home"
                            AppTab.MY_NETWORK -> if (isArabic) "شبكتي الخاصة" else "My Network"
                            AppTab.AI_HUB -> if (isArabic) "ذكاء NetGuard" else "AI Studio"
                            AppTab.TOPOLOGY -> if (isArabic) "المخطط" else "Topology"
                            AppTab.DEVICES -> if (isArabic) "الأجهزة" else "Assets"
                            AppTab.INTRUSION -> if (isArabic) "الاختراق" else "Threats"
                            AppTab.ROGUE_DETECTOR -> if (isArabic) "الدخلاء" else "Rogue"
                            AppTab.REMEDIATION -> if (isArabic) "المعالجة" else "SSH"
                            AppTab.REPORTS -> if (isArabic) "التقارير" else "Reports"
                            AppTab.ADMIN_PANEL -> if (isArabic) "المشرف" else "Admin"
                            AppTab.SETTINGS -> if (isArabic) "الإعدادات" else "Settings"
                            AppTab.SUPPORT -> if (isArabic) "الدعم والاستعلامات" else "Support"
                        }

                        Surface(
                            onClick = { viewModel.selectTab(tab) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) colors.primaryAccent else Color.Transparent,
                            contentColor = if (isSelected) (if (isDarkMode) CyberNavyDark else Color.White) else colors.textSecondary,
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("tab_${tab.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (tabBadgeCount > 0) {
                                            Badge(
                                                containerColor = colors.statusCrimson,
                                                contentColor = Color.White
                                            ) {
                                                Text("$tabBadgeCount")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            SupportContactComponent(viewModel = viewModel)
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                    AppTab.MY_NETWORK -> UserNetworkPortalScreen(viewModel = viewModel)
                    AppTab.AI_HUB -> AiSecurityHubScreen(viewModel = viewModel)
                    AppTab.TOPOLOGY -> TopologyMapScreen(viewModel = viewModel)
                    AppTab.DEVICES -> DevicesScreen(viewModel = viewModel)
                    AppTab.INTRUSION -> IntrusionDetectionScreen(viewModel = viewModel)
                    AppTab.ROGUE_DETECTOR -> RogueDetectorScreen(viewModel = viewModel)
                    AppTab.REMEDIATION -> RemediationScreen(viewModel = viewModel)
                    AppTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                    AppTab.ADMIN_PANEL -> AdminControlPanelScreen(viewModel = viewModel)
                    AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    AppTab.SUPPORT -> CustomerSupportScreen(viewModel = viewModel)
                }
            }
        }
    }
}
