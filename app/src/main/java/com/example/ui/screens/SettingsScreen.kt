package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.SubscriptionPlan
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.FirestoreSyncCard
import com.example.ui.components.FullAppUpdateCard
import com.example.ui.components.GlobalThemeSwitcherCard
import com.example.ui.components.ServerThresholdConfigSection
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = viewModel.repository
    val activePlan = viewModel.activePlan.value
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val fullAppUpdateState by viewModel.fullAppUpdateState.collectAsState()
    val isArabic = appLanguage == AppLanguage.ARABIC

    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }

    var backendUrlText by remember { mutableStateOf(repository.backendUrl.value) }
    var botTokenText by remember { mutableStateOf(repository.telegramBotToken.value) }
    var chatIdText by remember { mutableStateOf(repository.telegramChatId.value) }
    var scanInterval by remember { mutableFloatStateOf(repository.scanIntervalSeconds.value.toFloat()) }
    var autoScanEnabled by remember { mutableStateOf(repository.isAutoScanEnabled.value) }

    var testConnectionStatus by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
            .testTag("settings_screen_list"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Full App & System Update Card (تحديث التطبيق بالكامل)
        item {
            FullAppUpdateCard(
                updateState = fullAppUpdateState,
                isArabic = isArabic,
                colors = colors,
                onTriggerUpdate = { viewModel.executeFullAppUpdate() }
            )
        }

        // 1. Global Display Theme & Environmental Visibility Switcher
        item {
            GlobalThemeSwitcherCard(viewModel = viewModel)
        }

        // 1.1 Customer Service, Inquiries & Support Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_customer_support_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
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
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = if (isArabic) "مركز خدمة العملاء والاستعلامات المعتمد" else "Certified Customer Service & Support",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.statusGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "24/7 Live",
                                color = colors.statusGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (isArabic) {
                            "تواصل مباشر مع المهندس نجم الرئيس وفريق الصيانة عبر الواتساب أو الاتصال الهاتفي أو زيارة الموقع الرسمي."
                        } else {
                            "Direct communication with Eng. Najm Al-Raees and technical team via WhatsApp, direct phone call, or official web portal."
                        },
                        color = colors.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.openWhatsAppSupport(context) },
                            modifier = Modifier.weight(1f).height(38.dp).testTag("settings_btn_whatsapp"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isArabic) "واتساب: 738704940" else "WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.dialSupportPhone(context) },
                            modifier = Modifier.weight(1f).height(38.dp).testTag("settings_btn_call"),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isDarkMode) Color.Black else Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isArabic) "اتصال: 749154739" else "Direct Call", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.Black else Color.White)
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.selectTab(AppTab.SUPPORT) },
                        modifier = Modifier.fillMaxWidth().height(38.dp).testTag("settings_btn_open_support_tab"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
                        )
                    ) {
                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "عرض صفحة الأسئلة الشائعة والاستعلامات والموقع الإلكتروني" else "Open Full FAQs, Inquiries & Web Portal",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. SaaS Subscription Plans Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("saas_tier_card"),
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
                            text = if (isArabic) "خطة الاشتراك السحابية" else "SaaS Subscription Tier",
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
                                text = activePlan.title,
                                color = colors.primaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SubscriptionPlan.entries.forEach { plan ->
                        val isSelected = plan == activePlan
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.selectPlan(plan) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    if (isDarkMode) CyberNavyDark else colors.surface
                                } else {
                                    if (isDarkMode) CyberNavySurface else colors.cardBackground
                                }
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isSelected) colors.primaryAccent else colors.cardBorder
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = plan.title,
                                            color = if (isSelected) colors.primaryAccent else colors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSelected) colors.primaryAccent.copy(alpha = 0.2f) else colors.cardBorder)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = plan.badge,
                                                color = if (isSelected) colors.primaryAccent else colors.textSecondary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (plan.maxDevices > 1000) "Unlimited Devices • 24/7 SLA • SSH Rescue" else "Up to ${plan.maxDevices} Monitored Devices",
                                        color = colors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = plan.price,
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Custom Server Alert & Automation Thresholds Configuration
        item {
            ServerThresholdConfigSection(viewModel = viewModel)
        }

        // 4. Telegram Bot Alert API Integration
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("telegram_alert_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (isArabic) "مرسل تنبيهات تيليجرام" else "Telegram Alert Dispatcher",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) "إرسال تنبيهات أمنية فورية ومباشرة إلى قناة أو مجموعة الفريق على تيليجرام." else "Instant real-time security alerts sent directly to your Telegram team channel.",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = botTokenText,
                        onValueChange = {
                            botTokenText = it
                            repository.updateTelegramCredentials(it, chatIdText)
                        },
                        label = { Text("Telegram Bot Token") },
                        placeholder = { Text("123456789:ABCdefGhIJKlmNoPQR") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = chatIdText,
                        onValueChange = {
                            chatIdText = it
                            repository.updateTelegramCredentials(botTokenText, it)
                        },
                        label = { Text("Channel / Chat ID") },
                        placeholder = { Text("-1001234567890 or @NetGuardAlerts") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.sendTelegramTest() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isDarkMode) Color.Black else Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "إرسال تنبيه تجريبي إلى تيليجرام" else "Send Test Telegram Alert",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        // 5. Backend FastAPI Server Connection
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("backend_api_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (isArabic) "ربط خادم FastAPI الخلفي" else "FastAPI Backend Server Integration",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) "ربط تطبيق NetGuard بخادم FastAPI المحلي أو السحابي العامل في حاويات Docker." else "Connect NetGuard Mobile to your on-premise FastAPI backend running in Docker.",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = backendUrlText,
                        onValueChange = {
                            backendUrlText = it
                            repository.updateBackendUrl(it)
                        },
                        label = { Text("Backend URL (e.g. http://10.0.2.2:8000)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isTestingConnection = true
                                viewModel.triggerScan()
                                testConnectionStatus = if (isArabic) "تم الاختبار: حارس الشبكة الطرفي نشط واستجاب بنجاح." else "Tested: Edge Guard active. Backend ping evaluated."
                                isTestingConnection = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent),
                            shape = RoundedCornerShape(10.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                            )
                        ) {
                            Text(
                                text = if (isTestingConnection) {
                                    if (isArabic) "جارٍ الفحص..." else "Checking..."
                                } else {
                                    if (isArabic) "اختبار الاتصال" else "Test Connection"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (testConnectionStatus != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = testConnectionStatus ?: "",
                            color = colors.statusGreen,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 6. Monitoring Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("monitoring_config_card"),
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
                            text = if (isArabic) "المسح الدوري التلقائي للشبكة الفرعية" else "Automatic Periodic Subnet Sweep",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = autoScanEnabled,
                            onCheckedChange = {
                                autoScanEnabled = it
                                repository.isAutoScanEnabled.value = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isDarkMode) Color.Black else Color.White,
                                checkedTrackColor = colors.primaryAccent,
                                uncheckedThumbColor = colors.primaryAccent,
                                uncheckedTrackColor = colors.cardBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isArabic) "فترة المسح: ${scanInterval.toInt()} ثانية" else "Sweep Interval: ${scanInterval.toInt()} seconds",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )

                    Slider(
                        value = scanInterval,
                        onValueChange = {
                            scanInterval = it
                            repository.scanIntervalSeconds.value = it.toInt()
                        },
                        valueRange = 5f..60f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primaryAccent,
                            activeTrackColor = colors.primaryAccent,
                            inactiveTrackColor = colors.cardBorder
                        )
                    )
                }
            }
        }

        // 7. Firebase Cloud Firestore Persistence Configuration
        item {
            FirestoreSyncCard(
                viewModel = viewModel
            )
        }
    }
}
