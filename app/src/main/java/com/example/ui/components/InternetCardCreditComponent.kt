package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.NetGuardThemeColors

/**
 * Modern Jetpack Compose UI component for managing internet card credits,
 * showcasing remaining balance, consumption statistics, and interactive voucher redemption.
 */
@Composable
fun InternetCardCreditComponent(
    credit: InternetCardCredit,
    colors: NetGuardThemeColors,
    isArabic: Boolean,
    modifier: Modifier = Modifier,
    onRedeemVoucher: (String) -> RedemptionResult = { code ->
        val (isValid, amounts) = VoucherValidator.evaluate(code)
        if (isValid) {
            RedemptionResult.Success(
                messageArabic = "تم شحن ${amounts.first.toInt()} جيجابايت + ${amounts.second.toInt()} جيجابايت إضافية بنجاح!",
                messageEnglish = "Successfully redeemed ${amounts.first.toInt()} GB + ${amounts.second.toInt()} GB bonus!",
                creditedGB = amounts.first,
                bonusGB = amounts.second,
                newBalanceGB = credit.remainingCreditGB + amounts.first + amounts.second
            )
        } else {
            RedemptionResult.Failure(
                messageArabic = "رمز القسيمة غير صالح. يرجى التأكد من الرمز المدخل.",
                messageEnglish = "Invalid voucher code. Please verify and try again."
            )
        }
    }
) {
    val context = LocalContext.current
    var voucherInput by remember { mutableStateOf("") }
    var isRedeeming by remember { mutableStateOf(false) }
    var redemptionFeedback by remember { mutableStateOf<RedemptionResult?>(null) }
    var isHistoryExpanded by remember { mutableStateOf(false) }

    // Tier specific gradient accents
    val cardGradient = remember(credit.tier, colors.isDark) {
        val accentColor = Color(credit.tier.colorHex)
        if (colors.isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0F172A),
                    Color(0xFF1E293B),
                    accentColor.copy(alpha = 0.35f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF8FAFC),
                    Color(0xFFE2E8F0),
                    accentColor.copy(alpha = 0.25f)
                )
            )
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    colors.cardBorder,
                    Color(credit.tier.colorHex).copy(alpha = 0.6f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("internet_card_credit_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -----------------------------------------------------------------
            // 1. DIGITAL SMARTCARD HEADER & BRANDING
            // -----------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardGradient)
                    .border(
                        width = 1.dp,
                        color = Color(credit.tier.colorHex).copy(alpha = 0.4f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Top Row: Chip icon, Network Tier badge, and Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Smartcard Chip representation
                            Box(
                                modifier = Modifier
                                    .size(28.dp, 22.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.85f))
                                    .border(1.dp, Color(0xFFD97706), RoundedCornerShape(4.dp))
                            )
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "WiFi",
                                tint = colors.textPrimary.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "NetGuard Telecom",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary
                            )
                        }

                        // Tier Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(credit.tier.colorHex).copy(alpha = 0.2f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(Color(credit.tier.colorHex), Color.White.copy(alpha = 0.5f))
                                )
                            )
                        ) {
                            Text(
                                text = if (isArabic) credit.tier.titleArabic else credit.tier.titleEnglish,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(credit.tier.colorHex),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Middle Section: Massive Remaining Balance Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "الرصيد المتبقي المتاح" else "Available Remaining Balance",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = String.format("%.1f", credit.remainingCreditGB),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (credit.isLowBalance) colors.statusCrimson else colors.statusGreen,
                                    modifier = Modifier.testTag("remaining_credit_balance_text")
                                )
                                Text(
                                    text = credit.creditUnit,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        // Max speed badge & Validity
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.surface.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = colors.primaryAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "${credit.maxSpeedMbps.toInt()} Mbps",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryAccent
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) "متبقي ${credit.validityDaysLeft} يوماً" else "${credit.validityDaysLeft} days left",
                                fontSize = 10.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Capacity Progress Bar with Used vs Remaining
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "المستهلك: ${String.format("%.1f", credit.usedCreditGB)} GB" else "Used: ${String.format("%.1f", credit.usedCreditGB)} GB",
                                fontSize = 10.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = if (isArabic) "إجمالي الباقة: ${credit.totalCreditGB.toInt()} GB" else "Total: ${credit.totalCreditGB.toInt()} GB",
                                fontSize = 10.sp,
                                color = colors.textSecondary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { credit.remainingFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (credit.isLowBalance) colors.statusCrimson else colors.statusGreen,
                            trackColor = colors.cardBorder.copy(alpha = 0.5f)
                        )
                    }

                    // Bottom info: Serial Number & Cardholder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = credit.cardSerialNumber,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Text(
                            text = credit.holderName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // 2. VOUCHER REDEMPTION SECTION (شحن رصيد كروت الإنترنت)
            // -----------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.primaryAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isArabic) "شحن رصيد الكرت وقسائم التعبئة" else "Redeem Internet Voucher",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (isArabic) "أدخل رمز القسيمة أو البطاقة لزيادة رصيدك فوراً" else "Enter voucher code to instantly top-up credits",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Loyalty Points Chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.statusAmber.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = colors.statusAmber, modifier = Modifier.size(12.dp))
                            Text(
                                text = "${credit.pointsBalance} pts",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.statusAmber
                            )
                        }
                    }
                }

                // Input Field for Voucher Redemption
                OutlinedTextField(
                    value = voucherInput,
                    onValueChange = {
                        voucherInput = it.uppercase()
                        redemptionFeedback = null
                    },
                    label = { Text(if (isArabic) "رمز كرت الإنترنت أو القسيمة" else "Voucher or Scratch Card Code") },
                    placeholder = { Text("مثال: VCH-GOLD-50GB أو NET-8842-X9") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = colors.primaryAccent
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (voucherInput.isNotBlank()) {
                                IconButton(onClick = { voucherInput = "" }, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        val text = clip.getItemAt(0).text?.toString() ?: ""
                                        voucherInput = text.uppercase()
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryAccent,
                        unfocusedBorderColor = colors.cardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voucher_redemption_input")
                )

                // Quick Demo Sample Codes (for easy testing & rapid recharge)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isArabic) "قسائم تجريبية للشحن السريع:" else "Quick Sample Vouchers:",
                        fontSize = 10.5.sp,
                        color = colors.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val samples = listOf(
                            "VCH-GOLD-50GB" to "+50GB",
                            "VCH-TURBO-100GB" to "+100GB",
                            "VCH-STARTER-20GB" to "+20GB",
                            "VCH-WEEKEND-10GB" to "+10GB"
                        )
                        samples.forEach { (code, label) ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.surface,
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        voucherInput = code
                                        redemptionFeedback = null
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryAccent
                                    )
                                }
                            }
                        }
                    }
                }

                // Redeem Action Button
                Button(
                    onClick = {
                        if (voucherInput.isNotBlank()) {
                            isRedeeming = true
                            val result = onRedeemVoucher(voucherInput)
                            redemptionFeedback = result
                            if (result is RedemptionResult.Success) {
                                voucherInput = ""
                            }
                            isRedeeming = false
                        }
                    },
                    enabled = voucherInput.isNotBlank() && !isRedeeming,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("redeem_voucher_button")
                ) {
                    if (isRedeeming) {
                        CircularProgressIndicator(
                            color = colors.background,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "تأكيد شحن القسيمة للرصيد" else "Redeem Voucher to Balance",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Feedback Banner (Animated)
                AnimatedVisibility(
                    visible = redemptionFeedback != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    when (val result = redemptionFeedback) {
                        is RedemptionResult.Success -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.statusGreen.copy(alpha = 0.12f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(colors.statusGreen, colors.statusGreen))
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("redemption_success_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Celebration, contentDescription = null, tint = colors.statusGreen, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(
                                            text = if (isArabic) result.messageArabic else result.messageEnglish,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.statusGreen
                                        )
                                        Text(
                                            text = if (isArabic) "الرصيد الجديد: ${String.format("%.1f", result.newBalanceGB)} جيجابايت"
                                                else "New Balance: ${String.format("%.1f", result.newBalanceGB)} GB",
                                            fontSize = 10.5.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                        is RedemptionResult.Failure -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.statusCrimson.copy(alpha = 0.12f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(colors.statusCrimson, colors.statusCrimson))
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("redemption_failure_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = colors.statusCrimson, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = if (isArabic) result.messageArabic else result.messageEnglish,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.statusCrimson
                                    )
                                }
                            }
                        }
                        null -> {}
                    }
                }
            }

            // -----------------------------------------------------------------
            // 3. COLLAPSIBLE RECENT REDEMPTIONS HISTORY
            // -----------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isHistoryExpanded = !isHistoryExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isArabic) "سجل عمليات شحن الكروت السابقة" else "Recent Voucher Redemptions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = colors.surface
                        ) {
                            Text(
                                text = "${credit.redemptionHistory.size}",
                                fontSize = 10.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle History",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isHistoryExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (credit.redemptionHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.surface, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "لا توجد عمليات شحن سابقة مسجلة" else "No previous redemptions recorded",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            credit.redemptionHistory.forEach { record ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colors.surface, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = record.voucherCode,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = colors.primaryAccent
                                        )
                                        Text(
                                            text = record.redeemedAt,
                                            fontSize = 9.5.sp,
                                            color = colors.textSecondary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "+${record.creditAmountGB.toInt()} GB",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = colors.statusGreen
                                        )
                                        if (record.bonusAmountGB > 0f) {
                                            Text(
                                                text = "(+${record.bonusAmountGB.toInt()} Bonus)",
                                                fontSize = 9.5.sp,
                                                color = colors.statusAmber
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
    }
}
