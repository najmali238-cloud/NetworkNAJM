package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.service.PdfExportResult
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.getNetGuardColors

@Composable
fun AdminControlPanelScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsState()
    val adminEmail by viewModel.adminEmail.collectAsState()
    val adminServices by viewModel.adminServices.collectAsState()
    val adminAuditLogs by viewModel.adminAuditLogs.collectAsState()
    val managedNetworks by viewModel.managedNetworks.collectAsState()
    val serviceBoosterState by viewModel.serviceBoosterState.collectAsState()
    val masterOrchestrator by viewModel.masterOrchestratorServer.collectAsState()
    val fullAppUpdateState by viewModel.fullAppUpdateState.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val serverMetrics by viewModel.serverMetrics.collectAsState()
    val internetVouchers by viewModel.internetVouchers.collectAsState()
    val connectedClients by viewModel.connectedClients.collectAsState()

    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }
    val isArabic = appLanguage == AppLanguage.ARABIC

    var pdfExportResult by remember { mutableStateOf<PdfExportResult?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    // Login Form State
    var emailInput by remember { mutableStateOf(adminEmail) }
    var passwordInput by remember { mutableStateOf("AdminNetGuard@2026") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRobotChecked by remember { mutableStateOf(false) }
    var captchaAnswerInput by remember { mutableStateOf("") }
    var captchaError by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Math Challenge Captcha: e.g. 7 + 6 = 13
    val captchaNum1 = 7
    val captchaNum2 = 6
    val expectedCaptchaAnswer = "13"

    AnimatedContent(
        targetState = isAdminAuthenticated,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "admin_auth_view"
    ) { authenticated ->
        if (!authenticated) {
            // LOGIN GATEWAY SCREEN WITH ROBOT VERIFICATION (خانة تحقق من أي روبوتات)
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .testTag("admin_login_card"),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.4f))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Admin Icon & Header
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.primaryAccent.copy(alpha = 0.15f))
                                .border(1.5.dp, colors.primaryAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isArabic) "لوحة تحكم المشرف العام" else "Master Admin Control Panel",
                            color = colors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "تطوير المهندس نجم الرئيس - إدارة كامل الخدمات",
                            color = colors.primaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isArabic) "يرجى تسجيل الدخول بالبريد وكلمة المرور وتأكيد التحقق البشري" else "Authenticate with credentials and verify anti-bot challenge",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Email Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it; loginError = null },
                            label = { Text(if (isArabic) "البريد الإلكتروني للمشرف" else "Admin Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = colors.primaryAccent) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryAccent,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; loginError = null },
                            label = { Text(if (isArabic) "كلمة المرور" else "Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colors.primaryAccent) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = colors.textMuted
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryAccent,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ROBOT VERIFICATION BOX (خانة تحقق من أي روبوتات)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bot_verification_box"),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isRobotChecked) colors.statusGreen else colors.cardBorder
                                )
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = isRobotChecked,
                                            onCheckedChange = { checked ->
                                                isRobotChecked = checked
                                                if (!checked) captchaAnswerInput = ""
                                                captchaError = null
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = colors.statusGreen,
                                                uncheckedColor = colors.textMuted
                                            ),
                                            modifier = Modifier.testTag("bot_checkbox")
                                        )

                                        Text(
                                            text = if (isArabic) "أنا لست برنامج روبوت" else "I'm not a robot",
                                            color = colors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Captcha logo badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = if (isRobotChecked) colors.statusGreen else colors.primaryAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "CAPTCHA",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textMuted
                                        )
                                    }
                                }

                                if (isRobotChecked) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isArabic) "اختبار الأمان البشري: ما ناتج $captchaNum1 + $captchaNum2 ؟" else "Security Math Challenge: What is $captchaNum1 + $captchaNum2 ?",
                                        color = colors.primaryAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = captchaAnswerInput,
                                            onValueChange = {
                                                captchaAnswerInput = it
                                                captchaError = null
                                            },
                                            placeholder = { Text(if (isArabic) "اكتب الإجابة هنا..." else "Enter answer...") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("captcha_answer_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = colors.statusGreen,
                                                unfocusedBorderColor = colors.cardBorder,
                                                focusedTextColor = colors.textPrimary,
                                                unfocusedTextColor = colors.textPrimary
                                            )
                                        )

                                        if (captchaAnswerInput == expectedCaptchaAnswer) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(colors.statusGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (isArabic) "تم التحقق ✓" else "Verified ✓",
                                                    color = colors.statusGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (captchaError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = captchaError ?: "",
                                color = colors.statusCrimson,
                                fontSize = 11.sp
                            )
                        }

                        if (loginError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = loginError ?: "",
                                color = colors.statusCrimson,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submit Login Button
                        Button(
                            onClick = {
                                if (!isRobotChecked) {
                                    captchaError = if (isArabic) "يرجى تحديد خانة التحقق من أنك لست روبوت" else "Please check the anti-bot verification checkbox"
                                    return@Button
                                }
                                if (captchaAnswerInput.trim() != expectedCaptchaAnswer) {
                                    captchaError = if (isArabic) "إجابة اختبار الأمان غير صحيحة، يرجى المحاولة مجدداً" else "Incorrect CAPTCHA answer. Try again."
                                    return@Button
                                }

                                val success = viewModel.authenticateAdmin(emailInput.trim(), passwordInput.trim())
                                if (!success) {
                                    loginError = if (isArabic) "بيانات الدخول غير صحيحة" else "Invalid admin credentials"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryAccent,
                                contentColor = if (isDarkMode) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("admin_login_submit_btn")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "الدخول إلى لوحة تحكم المشرف" else "Access Admin Dashboard",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // AUTHENTICATED MASTER ADMIN CONTROL PANEL
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Admin Profile & Developer Credit
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_profile_card"),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
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
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(colors.primaryAccent.copy(alpha = 0.2f))
                                            .border(1.5.dp, colors.primaryAccent, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = colors.primaryAccent,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "المهندس نجم الرئيس (مدير النظام العام)",
                                            color = colors.textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = adminEmail,
                                            color = colors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Logout Button
                                OutlinedButton(
                                    onClick = { viewModel.logoutAdmin() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.statusCrimson),
                                    border = ButtonDefaults.outlinedButtonBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(colors.statusCrimson.copy(alpha = 0.5f))
                                    ),
                                    modifier = Modifier.testTag("admin_logout_btn")
                                ) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isArabic) "خروج" else "Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Privilege Level & Bot Verification Pass Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.statusGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "تم اجتياز اختبار الروبوت بنجاح ✓" else "Human Verified ✓",
                                        color = colors.statusGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.primaryAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Super Admin (Full Root)",
                                        color = colors.primaryAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 1. FULL APP & SYSTEM REFRESH / UPDATE SECTION (زر تحديث التطبيق بالكامل مع جميع الخدمات والمميزات)
                item {
                    FullAppUpdateCard(
                        updateState = fullAppUpdateState,
                        isArabic = isArabic,
                        colors = colors,
                        onTriggerUpdate = { viewModel.executeFullAppUpdate() }
                    )
                }

                // 2. TURBO SERVICE BOOSTER ENGINE SECTION (أداة تقوي عمل وتسريع الخدمة)
                item {
                    ServiceBoosterSection(
                        boosterState = serviceBoosterState,
                        isArabic = isArabic,
                        colors = colors,
                        onToggleBooster = { viewModel.toggleServiceBooster() },
                        onSetBoostLevel = { level -> viewModel.activateServiceBooster(level) }
                    )
                }

                // 3. AUTONOMOUS MASTER ORCHESTRATOR SERVER SECTION (خادم المايسترو والتشغيل الذاتي: يعمل، ينفذ، يتتبع، يرتب، يجهز ويسير بسلاسة)
                item {
                    MasterOrchestratorSection(
                        orchestrator = masterOrchestrator,
                        isArabic = isArabic,
                        colors = colors,
                        onTriggerRebalance = { viewModel.triggerOrchestratorRebalance() },
                        onReorderTasks = { viewModel.reorderAndScheduleTasks() }
                    )
                }

                // 4. MULTI-NETWORK ENTERPRISE ORCHESTRATION & LINKING (إضافة شبكات جديدة والربط والتحكم في جميع الشبكات من لوحة المشرف)
                item {
                    ManagedNetworksSection(
                        networks = managedNetworks,
                        isArabic = isArabic,
                        colors = colors,
                        onAddNetwork = { name, nameAr, cidr, vlan, gw, dns, zone, bw, notes ->
                            viewModel.addManagedNetwork(
                                name = name,
                                nameArabic = nameAr,
                                cidr = cidr,
                                vlanId = vlan,
                                gatewayIp = gw,
                                dnsPrimary = dns,
                                securityZone = zone,
                                bandwidthLimitMbps = bw,
                                notes = notes
                            )
                        },
                        onToggleConnection = { netId -> viewModel.toggleNetworkConnection(netId) },
                        onIsolateNetwork = { netId -> viewModel.isolateNetwork(netId) },
                        onDeepScanNetwork = { netId -> viewModel.deepScanNetwork(netId) },
                        onDeleteNetwork = { netId -> viewModel.deleteManagedNetwork(netId) },
                        onExpandNetwork = { netId -> viewModel.expandNetworkToMax(netId) },
                        internetVouchers = internetVouchers,
                        connectedClients = connectedClients
                    )
                }

                // 5. ENTERPRISE SERVERS, SYSTEMS & EQUIPMENT MATRIX (الخوادم والأجهزة والأنظمة والمعدات المؤسسية)
                item {
                    EnterpriseServersMatrixSection(
                        devices = devices,
                        serverMetrics = serverMetrics,
                        isArabic = isArabic,
                        colors = colors
                    )
                }

                // 6. INDIVIDUAL USER NETWORKS & VOUCHERS PORTAL SHORTCUT
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_user_networks_portal_card"),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.primaryAccent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lan,
                                            contentDescription = null,
                                            tint = colors.primaryAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = if (isArabic) "بوابة إدارة شبكات المستخدمين وكروت الإنترنت" else "User Networks & Internet Vouchers Portal",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = if (isArabic) "تسجيل الدخول، كروت الرصيد، الأجهزة المتصلة والراوتر، وسرعة الاستهلاك"
                                                else "Individual authentication, vouchers, connected devices, routers & live speed",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.selectTab(com.example.ui.AppTab.MY_NETWORK) },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("open_user_network_portal_button")
                                ) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isArabic) "فتح البوابة" else "Open Portal", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }

                // Administrative Review PDF Export Card (تصدير وثيقة التدقيق الإداري PDF)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pdf_export_card"),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
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
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFDC2626).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = if (isArabic) "وثيقة التدقيق والمراجعة الإدارية (PDF)" else "Administrative Review & Audit PDF Document",
                                            color = colors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isArabic) "تصدير حالة الشبكة وسجلات الأمان الموثقة رسميّاً" else "Export verified network status & security audit summaries",
                                            color = colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (isArabic) "تتضمن الوثيقة الرسمية المكونة من صفحتين تقييم كفاءة الشبكة، وسجلات كشف التسلل (IDS)، وجرد الخوادم، وسجل التنبيهات الفورية مع التوقيع الإداري." else "The official 2-page document includes executive health metrics, IDS threat origin logs, server telemetry, and ISO 27001 sign-off.",
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    isGeneratingPdf = true
                                    val result = viewModel.exportNetworkStatusAndSecurityPdf(context)
                                    pdfExportResult = result
                                    isGeneratingPdf = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("admin_export_pdf_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primaryAccent,
                                    contentColor = if (isDarkMode) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isGeneratingPdf) {
                                        if (isArabic) "جاري إعداد وثيقة PDF الرسمية..." else "Generating Audit PDF..."
                                    } else {
                                        if (isArabic) "تصدير وثيقة التدقيق PDF للمراجعة الإدارية" else "Export Official PDF Audit Report"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Global Display Theme & Environmental Visibility Switcher (التحكم في نمط الإضاءة والمظهر العام)
                item {
                    GlobalThemeSwitcherCard(viewModel = viewModel)
                }

                // Customer Support & Direct Escalation (الدعم الفني والتصعيد المباشر)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("admin_customer_support_card"),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.primaryAccent.copy(alpha = 0.5f))
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = colors.primaryAccent)
                                    Text(
                                        text = if (isArabic) "التصعيد المباشر وخدمة العملاء (24/7)" else "Direct Escalation & Support Desk",
                                        color = colors.textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (isArabic) "المهندس نجم الرئيس" else "Eng. Najm Al-Raees",
                                    color = colors.primaryAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = if (isArabic) "قنوات التواصل الفوري للمشرفين مع إدارة العمليات وكبير المهندسين لحالات الطوارئ والاستعلامات الفنية." else "Instant escalation channels for NOC administrators to contact lead operations and customer care.",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.openWhatsAppSupport(context) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isArabic) "واتساب: 738704940" else "WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Button(
                                    onClick = { viewModel.dialSupportPhone(context) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isDarkMode) Color.Black else Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isArabic) "اتصال: 749154739" else "Call", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.Black else Color.White)
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.selectTab(com.example.ui.AppTab.SUPPORT) },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryAccent)
                            ) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = if (isArabic) "فتح مركز الاستعلامات والأسئلة الشائعة والموقع" else "Open FAQs, Inquiries & Web Portal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Emergency Lockdown Section (إجراءات الطوارئ للمشرف)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_lockdown_card"),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF2A0812) else Color(0xFFFFF1F2)),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.statusCrimson.copy(alpha = 0.6f))
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
                                    Icon(Icons.Default.Dangerous, contentDescription = null, tint = colors.statusCrimson)
                                    Text(
                                        text = if (isArabic) "إجراءات الطوارئ وعزل الشبكة" else "Emergency Network Lockdown",
                                        color = colors.statusCrimson,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { viewModel.triggerEmergencyLockdown() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.statusCrimson,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("trigger_lockdown_btn")
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "حظر شامل وعزل فوري" else "Execute Lockdown",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isArabic) "يقوم هذا الإجراء بعزل كافة الأجهزة الدخيلة وغير المعتمدة فوراً، وإغلاق المنافذ غير الحيوية على محولات الشبكة." else "Immediately isolates all rogue devices, applies strict port restrictions, and updates firewall rules.",
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Master Service Switchboard (لوحة تحكم كافة الخدمات)
                item {
                    Text(
                        text = if (isArabic) "لوحة تحكم خدمات التطبيق والمحركات" else "Core Services & Engine Switchboard",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(adminServices) { srv ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_service_${srv.id}"),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (srv.isRunning) colors.cardBorder else colors.statusCrimson.copy(alpha = 0.5f)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) srv.serviceNameArabic else srv.serviceName,
                                        color = colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (srv.isRunning) colors.statusGreen.copy(alpha = 0.15f) else colors.statusCrimson.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (srv.isRunning) (if (isArabic) "نشط" else "ACTIVE") else (if (isArabic) "متوقف" else "PAUSED"),
                                            color = if (srv.isRunning) colors.statusGreen else colors.statusCrimson,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (isArabic) srv.descriptionArabic else srv.description,
                                    color = colors.textSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Uptime: ${srv.uptime} | Category: ${srv.category}",
                                    color = colors.textMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Switch(
                                checked = srv.isRunning,
                                onCheckedChange = { viewModel.toggleAdminService(srv.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colors.primaryAccent,
                                    uncheckedThumbColor = colors.textMuted,
                                    uncheckedTrackColor = colors.surface
                                ),
                                modifier = Modifier.testTag("switch_${srv.id}")
                            )
                        }
                    }
                }

                // Admin Audit Trail (سجل نشاط وتدقيق المشرف)
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) "سجل تدقيق عمليات المشرف (Audit Trail)" else "Admin Audit Trail & Logs",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(adminAuditLogs) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_audit_log_${log.id}"),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.cardBorder)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.adminEmail,
                                    color = colors.primaryAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = log.timestamp,
                                    color = colors.textMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isArabic) log.actionArabic else log.action,
                                color = colors.textPrimary,
                                fontSize = 12.sp
                            )
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
