package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.InternetCardCreditComponent
import com.example.ui.theme.NetGuardThemeColors
import com.example.ui.theme.getNetGuardColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNetworkPortalScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val colors = remember(isDarkMode) { getNetGuardColors(isDarkMode) }
    val isArabic = appLanguage == AppLanguage.ARABIC

    val currentUser by viewModel.currentLoggedInUser.collectAsState()
    val userAccounts by viewModel.userAccounts.collectAsState()
    val managedNetworks by viewModel.managedNetworks.collectAsState()
    val internetVouchers by viewModel.internetVouchers.collectAsState()
    val internetCardCredits by viewModel.internetCardCredits.collectAsState()
    val connectedClients by viewModel.connectedClients.collectAsState()

    // Determine the user's specific network
    val userNetwork = remember(currentUser, managedNetworks) {
        if (currentUser != null) {
            managedNetworks.find { it.id == currentUser?.assignedNetworkId }
                ?: managedNetworks.firstOrNull()
        } else {
            managedNetworks.firstOrNull()
        }
    }

    // Determine the voucher & card credit for this network
    val userVoucher = remember(userNetwork, internetVouchers) {
        userNetwork?.let { internetVouchers[it.id] }
    }

    val userCardCredit = remember(userNetwork, internetCardCredits) {
        userNetwork?.let { internetCardCredits[it.id] } ?: internetCardCredits.values.firstOrNull()
    }

    // Determine clients connected to this specific network
    val userClients = remember(userNetwork, connectedClients) {
        userNetwork?.let { net ->
            connectedClients.filter { it.networkId == net.id }
        } ?: emptyList()
    }

    // Dialog States
    var showLoginDialog by remember { mutableStateOf(currentUser == null) }
    var showRechargeDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportContent by remember { mutableStateOf("") }

    // Login Form State
    var loginIdentifierType by remember { mutableStateOf(LoginIdentifierType.EMAIL) }
    var identifierInput by remember { mutableStateOf(currentUser?.email ?: "najmali238@gmail.com") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    // Recharge Form State
    var rechargeCodeInput by remember { mutableStateOf("") }
    var selectedRechargeGB by remember { mutableFloatStateOf(20f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // -------------------------------------------------------------
        // USER AUTHENTICATION / HEADER BANNER
        // -------------------------------------------------------------
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryAccent.copy(alpha = 0.15f))
                                    .border(1.5.dp, colors.primaryAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentUser != null) Icons.Default.Person else Icons.Default.Lock,
                                    contentDescription = "User Avatar",
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = currentUser?.fullName ?: if (isArabic) "تسجيل الدخول لإدارة شبكتك" else "Login to Manage Your Network",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (currentUser != null) {
                                        "${currentUser?.email} • ${currentUser?.phone}"
                                    } else {
                                        if (isArabic) "يمكن الدخول بالبريد، اسم المستخدم، أو الهاتف" else "Login via Email, Username, or Phone"
                                    },
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        if (currentUser != null) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.logoutUser()
                                    showLoginDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("user_logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "تبديل / خروج" else "Switch / Exit",
                                    fontSize = 11.5.sp
                                )
                            }
                        } else {
                            Button(
                                onClick = { showLoginDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("open_login_dialog_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isArabic) "دخول" else "Login", fontSize = 12.sp)
                            }
                        }
                    }

                    // Assigned network pill
                    if (userNetwork != null) {
                        HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Router,
                                    contentDescription = null,
                                    tint = colors.statusGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isArabic) "الشبكة المخصصة لك:" else "Assigned Network:",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = if (isArabic) userNetwork.nameArabic else userNetwork.name,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primaryAccent
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.statusGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = userNetwork.cidr,
                                    color = colors.statusGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // QUICK METRIC & WORKFLOW CARDS (أداء وسير العمل لدى الشبكة)
        // -------------------------------------------------------------
        if (userNetwork != null) {
            item {
                Text(
                    text = if (isArabic) "سير العمل والأداء اللحظي لشبكتك" else "Real-time Operations & Network Health",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Operational Status
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(colors.statusGreen)
                                )
                                Text(
                                    text = if (isArabic) "حالة الخدمة" else "Status",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Text(
                                text = if (userNetwork.isConnected) (if (isArabic) "متصل ومستقر" else "Connected") else (if (isArabic) "معزول أمنياً" else "Isolated"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userNetwork.isConnected) colors.statusGreen else colors.statusCrimson
                            )
                            Text(
                                text = "Ping: ${userNetwork.latencyMs} ms",
                                fontSize = 10.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Live Throughput
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isArabic) "حركة المرور" else "Throughput",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Text(
                                text = "${userNetwork.currentTrafficMbps} Mbps",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryAccent
                            )
                            Text(
                                text = "حد النطاق: ${userNetwork.bandwidthLimitMbps.toInt()}M",
                                fontSize = 10.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Connected Count
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = colors.statusAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isArabic) "الأشخاص" else "Clients",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Text(
                                text = "${userClients.size} أجهزة",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.statusAmber
                            )
                            Text(
                                text = "${userNetwork.assignedRouterCount} راوتر",
                                fontSize = 10.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // MAXIMUM NETWORK EXPANSION (ميزة توسيع الشبكة إلى أكبر حد ممكن)
            // -------------------------------------------------------------
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (userNetwork.isExpandedToMax)
                            colors.primaryAccent.copy(alpha = if (isDarkMode) 0.12f else 0.08f)
                        else
                            colors.cardBackground
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (userNetwork.isExpandedToMax) colors.primaryAccent else colors.cardBorder
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("network_expansion_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.primaryAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hub,
                                        contentDescription = null,
                                        tint = colors.primaryAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isArabic) "توسيع الشبكة لأكبر طاقة استيعابية" else "Maximum Network Expansion",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (isArabic) "Super-Subnet • Multi-VLAN • 10 Gbps Fiber" else "Multi-VLAN Trunking & 10G Optical Backhaul",
                                        fontSize = 11.5.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            if (userNetwork.isExpandedToMax) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.statusGreen.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = colors.statusGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isArabic) "موسعة للحد الأقصى" else "Max Expanded",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.statusGreen
                                        )
                                    }
                                }
                            }
                        }

                        // Capacity Stats Comparison
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isArabic) "السعة القصوى" else "Capacity",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "${userNetwork.maxAllowedHosts} مضيف",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryAccent
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(colors.cardBorder)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isArabic) "خط النفاذ" else "Backhaul",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "${userNetwork.fiberBackhaulSpeedGbps.toInt()} Gbps Fiber",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.statusGreen
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(colors.cardBorder)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isArabic) "مسارات VLAN" else "VLANs",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = if (userNetwork.multiVlanTrunkEnabled) "Multi-Trunk" else "Single VLAN",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.statusAmber
                                )
                            }
                        }

                        if (!userNetwork.isExpandedToMax) {
                            Button(
                                onClick = { viewModel.expandNetworkToMax(userNetwork.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("expand_network_button")
                            ) {
                                Icon(Icons.Default.NorthEast, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "توسيع الشبكة الآن لأكبر طاقة استيعابية" else "Expand Network to Maximum Limit Now",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = if (isArabic)
                                    "✓ تم توسيع النطاق إلى Class Supernet (/16)، وتفعيل خطوط الألياف الضوئية بسرعة 10 Gbps، ودعم ما يصل إلى 65,534 جهاز متصل."
                                else
                                    "✓ Network scaled to Class Supernet (/16), 10 Gbps optical backhaul active, supporting up to 65,534 concurrent devices.",
                                fontSize = 11.5.sp,
                                color = colors.statusGreen
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // INTERNET CARD CREDITS MANAGEMENT & VOUCHER REDEMPTION (إدارة رصيد كروت الإنترنت وشحن القسائم)
            // -------------------------------------------------------------
            item {
                if (userCardCredit != null) {
                    InternetCardCreditComponent(
                        credit = userCardCredit,
                        colors = colors,
                        isArabic = isArabic,
                        onRedeemVoucher = { code ->
                            userNetwork?.let { net ->
                                viewModel.redeemCardVoucher(net.id, code)
                            } ?: RedemptionResult.Failure(
                                messageArabic = "لم يتم تحديد شبكة نشطة",
                                messageEnglish = "No active network selected"
                            )
                        }
                    )
                }
            }

            // -------------------------------------------------------------
            // CONNECTED USERS, ROUTERS & REAL-TIME SPEEDS
            // -------------------------------------------------------------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "الأشخاص والأجهزة المتصلة بالشبكة والراوتر" else "Connected Clients & Router Telemetry",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isArabic)
                                "${userClients.size} أجهزة متصلة عبر ${userNetwork.assignedRouterCount} راوتر مع قياس السرعة الحية"
                            else
                                "${userClients.size} devices connected via ${userNetwork.assignedRouterCount} routers with live speed",
                            fontSize = 11.5.sp,
                            color = colors.textSecondary
                        )
                    }

                    // Download Comprehensive Report Button
                    FilledTonalButton(
                        onClick = {
                            reportContent = viewModel.generateNetworkReport(userNetwork)
                            showReportDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("download_network_report_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "تقرير شبكتي" else "Report",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (userClients.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isArabic) "لا توجد أجهزة متصلة حالياً بهذه الشبكة" else "No connected clients detected on this network",
                                color = colors.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(userClients) { client ->
                    ClientDeviceCard(
                        client = client,
                        colors = colors,
                        isArabic = isArabic,
                        onToggleBlock = { viewModel.toggleBlockClient(client.id) },
                        onSetQoS = { priority -> viewModel.setClientQoS(client.id, priority) }
                    )
                }
            }
        }
    }

    // =========================================================================
    // DIALOG 1: USER MULTI-LOGIN (Email, Username, or Phone + Strong Password)
    // =========================================================================
    if (showLoginDialog) {
        Dialog(onDismissRequest = {
            if (currentUser != null) showLoginDialog = false
        }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_dialog_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = colors.primaryAccent)
                            Text(
                                text = if (isArabic) "تسجيل الدخول لإدارة شبكتك" else "Login to Your Network",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        if (currentUser != null) {
                            IconButton(onClick = { showLoginDialog = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                            }
                        }
                    }

                    Text(
                        text = if (isArabic)
                            "سجل دخولك عبر بريدك الإلكتروني، اسم المستخدم، أو رقم الهاتف مع كلمة مرور قوية لإدارة ومراقبة شبكتك الخاصة فقط."
                        else
                            "Sign in using your Email, Username, or Phone number with a strong password to manage only your dedicated network.",
                        fontSize = 11.5.sp,
                        color = colors.textSecondary
                    )

                    // Login Type Tabs (Email, Username, Phone)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surface, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LoginIdentifierType.entries.forEach { type ->
                            val isSelected = type == loginIdentifierType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) colors.primaryAccent else Color.Transparent)
                                    .clickable {
                                        loginIdentifierType = type
                                        loginErrorMessage = null
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isArabic) type.labelArabic else type.labelEnglish,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) (if (isDarkMode) Color.Black else Color.White) else colors.textSecondary
                                )
                            }
                        }
                    }

                    // Identifier Input Field
                    OutlinedTextField(
                        value = identifierInput,
                        onValueChange = {
                            identifierInput = it
                            loginErrorMessage = null
                        },
                        label = {
                            Text(
                                when (loginIdentifierType) {
                                    LoginIdentifierType.EMAIL -> if (isArabic) "البريد الإلكتروني" else "Email Address"
                                    LoginIdentifierType.USERNAME -> if (isArabic) "اسم المستخدم" else "Username"
                                    LoginIdentifierType.PHONE -> if (isArabic) "رقم الهاتف" else "Phone Number"
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                when (loginIdentifierType) {
                                    LoginIdentifierType.EMAIL -> "najmali238@gmail.com"
                                    LoginIdentifierType.USERNAME -> "najm_client"
                                    LoginIdentifierType.PHONE -> "+967771234567"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (loginIdentifierType) {
                                    LoginIdentifierType.EMAIL -> Icons.Default.Email
                                    LoginIdentifierType.USERNAME -> Icons.Default.AccountCircle
                                    LoginIdentifierType.PHONE -> Icons.Default.Phone
                                },
                                contentDescription = null,
                                tint = colors.primaryAccent
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = when (loginIdentifierType) {
                                LoginIdentifierType.EMAIL -> KeyboardType.Email
                                LoginIdentifierType.PHONE -> KeyboardType.Phone
                                else -> KeyboardType.Text
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_identifier_input")
                    )

                    // Strong Password Input Field
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            loginErrorMessage = null
                        },
                        label = { Text(if (isArabic) "كلمة المرور القوية" else "Strong Password") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = colors.primaryAccent)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    // Error Message
                    if (loginErrorMessage != null) {
                        Text(
                            text = loginErrorMessage ?: "",
                            color = colors.statusCrimson,
                            fontSize = 11.5.sp
                        )
                    }

                    // Quick Demo Accounts to switch seamlessly
                    Text(
                        text = if (isArabic) "أو اختر حساباً تجريبياً جاهزاً:" else "Or pick a demo account:",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        userAccounts.forEach { acc ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.surface,
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        identifierInput = when (loginIdentifierType) {
                                            LoginIdentifierType.EMAIL -> acc.email
                                            LoginIdentifierType.USERNAME -> acc.username
                                            LoginIdentifierType.PHONE -> acc.phone
                                        }
                                        passwordInput = acc.passwordHash
                                    }
                            ) {
                                Text(
                                    text = acc.username,
                                    fontSize = 10.5.sp,
                                    color = colors.primaryAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }

                    // Login Action Button
                    Button(
                        onClick = {
                            val success = viewModel.loginUser(identifierInput, passwordInput, loginIdentifierType)
                            if (success) {
                                showLoginDialog = false
                            } else {
                                loginErrorMessage = if (isArabic) "يرجى التحقق من البيانات وكلمة المرور" else "Check identifier and password"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_login_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "دخول إلى شبكتي الآن" else "Enter My Network Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG 2: INTERNET VOUCHER RECHARGE (خدمة تعبئة الرصيد)
    // =========================================================================
    if (showRechargeDialog && userNetwork != null) {
        Dialog(onDismissRequest = { showRechargeDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recharge_dialog_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, tint = colors.statusGreen)
                            Text(
                                text = if (isArabic) "تعبئة رصيد كرت الإنترنت" else "Recharge Internet Card",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        IconButton(onClick = { showRechargeDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                        }
                    }

                    Text(
                        text = if (isArabic)
                            "أدخل رمز بطاقة الشحن الجديدة أو اختر باقة شحن سريعة لإضافتها فوراً لكرت شبكتك."
                        else
                            "Enter a new scratch card code or pick a quick top-up pack to instantly credit your network.",
                        fontSize = 11.5.sp,
                        color = colors.textSecondary
                    )

                    // Scratch code input
                    OutlinedTextField(
                        value = rechargeCodeInput,
                        onValueChange = { rechargeCodeInput = it },
                        label = { Text(if (isArabic) "رمز كرت الشحن أو القسيمة" else "Scratch Card Code") },
                        placeholder = { Text("SCR-8842-9901") },
                        leadingIcon = {
                            Icon(Icons.Default.Pin, contentDescription = null, tint = colors.statusGreen)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scratch_code_input")
                    )

                    // Quick pack selector
                    Text(
                        text = if (isArabic) "باقات الشحن السريع المتاحة:" else "Quick Recharge Packs:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )

                    val quickPacks = listOf(10f, 25f, 50f, 100f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPacks.forEach { gb ->
                            val isSelected = selectedRechargeGB == gb
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) colors.statusGreen.copy(alpha = 0.2f) else colors.surface,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isSelected) colors.statusGreen else colors.cardBorder
                                    )
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRechargeGB = gb }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "+${gb.toInt()}GB",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) colors.statusGreen else colors.textPrimary
                                    )
                                    Text(
                                        text = if (isArabic) "جيجابايت" else "GB",
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.rechargeVoucher(userNetwork.id, rechargeCodeInput, selectedRechargeGB)
                            showRechargeDialog = false
                            rechargeCodeInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.statusGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_recharge_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "تأكيد شحن الرصيد (+${selectedRechargeGB.toInt()} GB)" else "Confirm Recharge (+${selectedRechargeGB.toInt()} GB)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG 3: COMPREHENSIVE NETWORK REPORT (تقرير الشبكة المتكامل الشامل)
    // =========================================================================
    if (showReportDialog) {
        Dialog(onDismissRequest = { showReportDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .testTag("network_report_dialog_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = colors.primaryAccent)
                            Text(
                                text = if (isArabic) "تقرير شبكتك المتكامل (PDF)" else "Comprehensive Network Report",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        IconButton(onClick = { showReportDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                        }
                    }

                    // Report text inside scrollable container
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(colors.surface, RoundedCornerShape(8.dp))
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(
                                    text = reportContent,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Network Report", reportContent))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_report_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "نسخ التقرير" else "Copy Text", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, reportContent)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share NetGuard Report")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_download_report_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "مشاركة وتنزيل" else "Share / Save", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: CLIENT DEVICE CARD WITH REAL-TIME SPEED & ROUTER HOOK
// -----------------------------------------------------------------------------
@Composable
fun ClientDeviceCard(
    client: ConnectedNetworkClient,
    colors: NetGuardThemeColors,
    isArabic: Boolean,
    onToggleBlock: () -> Unit,
    onSetQoS: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = if (client.isBlocked) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(colors.statusCrimson)
        ) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("client_card_${client.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    val deviceIcon = when (client.deviceType) {
                        ClientDeviceType.SMARTPHONE -> Icons.Default.Smartphone
                        ClientDeviceType.LAPTOP -> Icons.Default.Laptop
                        ClientDeviceType.TABLET -> Icons.Default.Tablet
                        ClientDeviceType.SMART_TV -> Icons.Default.Tv
                        ClientDeviceType.GAMING_CONSOLE -> Icons.Default.SportsEsports
                        ClientDeviceType.IOT_DEVICE -> Icons.Default.Sensors
                        ClientDeviceType.DESKTOP -> Icons.Default.Computer
                        ClientDeviceType.ROUTER_AP -> Icons.Default.Router
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (client.isBlocked) colors.statusCrimson.copy(alpha = 0.15f)
                                else colors.primaryAccent.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = deviceIcon,
                            contentDescription = null,
                            tint = if (client.isBlocked) colors.statusCrimson else colors.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = client.clientName,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isArabic) client.deviceType.displayNameArabic else client.deviceType.displayNameEnglish,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                            Text(text = "•", fontSize = 11.sp, color = colors.textSecondary)
                            Text(text = client.ip, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = colors.primaryAccent)
                        }
                    }
                }

                // Block/Unblock Button
                IconButton(
                    onClick = onToggleBlock,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (client.isBlocked) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = if (client.isBlocked) "Unblock" else "Block",
                        tint = if (client.isBlocked) colors.statusCrimson else colors.statusGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Router hook info & QoS Priority
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isArabic) "الراوتر: ${client.connectedRouterName}" else "Router: ${client.connectedRouterName}",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                // QoS Pill
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colors.primaryAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "QoS: ${client.qosPriority}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Real-Time Speed Telemetry (Download / Upload / Ping / Consumed Data)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Download Speed
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Download",
                        tint = colors.statusGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Column {
                        Text(
                            text = if (isArabic) "تحميل" else "Down",
                            fontSize = 9.5.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${client.downloadSpeedMbps} Mbps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.statusGreen
                        )
                    }
                }

                // Upload Speed
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Upload",
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Column {
                        Text(
                            text = if (isArabic) "رفع" else "Up",
                            fontSize = 9.5.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${client.uploadSpeedMbps} Mbps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryAccent
                        )
                    }
                }

                // Ping Latency
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Ping",
                        tint = colors.statusAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Column {
                        Text(
                            text = if (isArabic) "استجابة" else "Ping",
                            fontSize = 9.5.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${client.latencyMs} ms",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.statusAmber
                        )
                    }
                }

                // Consumed Quota
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isArabic) "الاستهلاك" else "Usage",
                        fontSize = 9.5.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "${(client.totalConsumedMB / 1024f).let { String.format("%.2f GB", it) }}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}
