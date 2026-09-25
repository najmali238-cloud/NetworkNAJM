package com.example.data.network

import com.example.data.model.Device
import com.example.data.model.RogueDevice
import com.example.data.model.ServerMetric
import com.example.data.model.AlertLog
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class NetGuardApiClient(private var baseUrl: String = "http://10.0.2.2:8000") {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    fun updateBaseUrl(newUrl: String) {
        baseUrl = if (newUrl.endsWith("/")) newUrl.dropLast(1) else newUrl
    }

    fun getBaseUrl(): String = baseUrl

    suspend fun testBackendConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/health")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendTelegramAlert(
        botToken: String,
        chatId: String,
        message: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (botToken.isBlank() || chatId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Bot Token and Chat ID are required"))
            }
            val url = "https://api.telegram.org/bot$botToken/sendMessage"
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("text", message)
                put("parse_mode", "Markdown")
            }
            val body = RequestBody.create("application/json".toMediaType(), json.toString())
            val request = Request.Builder().url(url).post(body).build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success("Alert successfully sent to Telegram: $responseStr")
                } else {
                    Result.failure(IOException("Telegram API Error (${response.code}): $responseStr"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
