package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Internet Card Tier defining bandwidth limits and visual accents
 */
enum class InternetCardTier(
    val titleArabic: String,
    val titleEnglish: String,
    val defaultQuotaGB: Float,
    val speedLimitMbps: Float,
    val colorHex: Long
) {
    BRONZE("برونزي اقتصادي", "Bronze Basic", 25f, 50f, 0xFFCD7F32),
    SILVER("فضي قياسي", "Silver Standard", 50f, 100f, 0xFF94A3B8),
    GOLD("ذهبي فائق", "Gold Premium", 100f, 300f, 0xFFF59E0B),
    PLATINUM_TURBO("بلاتينيوم توربو", "Platinum Turbo", 250f, 1000f, 0xFF06B6D4)
}

/**
 * Operational status of an internet card
 */
enum class InternetCardStatus(
    val labelArabic: String,
    val labelEnglish: String
) {
    ACTIVE("نشط ومتاح", "Active"),
    EXPIRING_SOON("ينتهي قريباً", "Expiring Soon"),
    DEPLETED("رصيد منتهي", "Depleted"),
    SUSPENDED("موقوف مؤقتاً", "Suspended")
}

/**
 * Status of voucher redemption
 */
enum class VoucherRedemptionStatus(
    val labelArabic: String,
    val labelEnglish: String
) {
    SUCCESS("تم الشحن بنجاح", "Redeemed Successfully"),
    INVALID_CODE("رمز القسيمة غير صالح", "Invalid Voucher Code"),
    ALREADY_REDEEMED("تم استخدام القسيمة مسبقاً", "Already Redeemed"),
    EXPIRED("القسيمة منتهية الصلاحية", "Expired Voucher")
}

/**
 * Transaction record of a single voucher redemption
 */
data class VoucherRedemptionRecord(
    val id: String = UUID.randomUUID().toString(),
    val voucherCode: String,
    val creditAmountGB: Float,
    val bonusAmountGB: Float = 0f,
    val redeemedAt: String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
    val status: VoucherRedemptionStatus = VoucherRedemptionStatus.SUCCESS,
    val notes: String = "شحن مباشر عبر تطبيق NetGuard"
)

/**
 * Comprehensive data model for managing Internet Card Credits and Balance
 */
data class InternetCardCredit(
    val cardId: String,
    val networkId: String,
    val cardSerialNumber: String = "NET-8842-7719-2026",
    val holderName: String = "Najm Ali",
    val planTitle: String = "Ultra Fiber Gold 100GB",
    val planTitleArabic: String = "باقة الألياف الضوئية الذهبية 100 جيجابايت",
    val tier: InternetCardTier = InternetCardTier.GOLD,
    val totalCreditGB: Float = 100.0f,
    val usedCreditGB: Float = 28.5f,
    val remainingCreditGB: Float = 71.5f,
    val creditUnit: String = "GB",
    val pointsBalance: Int = 420,
    val maxSpeedMbps: Float = 300f,
    val validityDaysLeft: Int = 24,
    val expiryDate: String = "2026-10-22",
    val cardStatus: InternetCardStatus = InternetCardStatus.ACTIVE,
    val activeVoucherCode: String = "VCH-GOLD-50GB",
    val lastRedemptionTimestamp: String? = "2026-09-20 14:30",
    val redemptionHistory: List<VoucherRedemptionRecord> = emptyList()
) {
    val consumptionPercent: Float
        get() = if (totalCreditGB > 0) ((usedCreditGB / totalCreditGB) * 100f).coerceIn(0f, 100f) else 0f

    val remainingFraction: Float
        get() = if (totalCreditGB > 0) (remainingCreditGB / totalCreditGB).coerceIn(0f, 1f) else 0f

    val isLowBalance: Boolean
        get() = remainingFraction < 0.20f
}

/**
 * Outcome result of a redemption attempt
 */
sealed class RedemptionResult {
    data class Success(
        val messageArabic: String,
        val messageEnglish: String,
        val creditedGB: Float,
        val bonusGB: Float,
        val newBalanceGB: Float
    ) : RedemptionResult()

    data class Failure(
        val messageArabic: String,
        val messageEnglish: String
    ) : RedemptionResult()
}

/**
 * Helper to validate voucher codes and resolve preset bonuses
 */
object VoucherValidator {
    private val PRESET_VOUCHERS = mapOf(
        "VCH-GOLD-50GB" to Pair(50.0f, 5.0f),
        "VCH-TURBO-100GB" to Pair(100.0f, 15.0f),
        "VCH-STARTER-20GB" to Pair(20.0f, 0.0f),
        "VCH-WEEKEND-10GB" to Pair(10.0f, 2.0f),
        "NET-VOUCH-7842-9901-X9" to Pair(35.0f, 5.0f),
        "SCR-8842-9901" to Pair(25.0f, 0.0f)
    )

    fun cleanCode(raw: String): String = raw.trim().uppercase()

    fun evaluate(rawCode: String): Pair<Boolean, Pair<Float, Float>> {
        val code = cleanCode(rawCode)
        if (code.isBlank()) return Pair(false, Pair(0f, 0f))

        // Check if preset code
        PRESET_VOUCHERS[code]?.let {
            return Pair(true, it)
        }

        // Generic dynamic voucher pattern: e.g. ABC-1234-5678 or 12-16 digits/chars
        val cleanAlphanumeric = code.replace("-", "").replace(" ", "")
        if (cleanAlphanumeric.length in 8..24) {
            // Calculate pseudo credit based on code hash for custom scratch cards
            val baseCredit = when {
                cleanAlphanumeric.contains("100") -> 100f
                cleanAlphanumeric.contains("50") -> 50f
                cleanAlphanumeric.contains("25") -> 25f
                cleanAlphanumeric.contains("10") -> 10f
                else -> 15.0f + (cleanAlphanumeric.hashCode().let { kotlin.math.abs(it) % 4 } * 10f)
            }
            val bonus = if (baseCredit >= 50f) 5f else 0f
            return Pair(true, Pair(baseCredit, bonus))
        }

        return Pair(false, Pair(0f, 0f))
    }
}
