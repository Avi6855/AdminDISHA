package com.avinashpatil.app.timepass2.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@Serializable
data class Candidate(
    val content: Content
)

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val json = Json { ignoreUnknownKeys = true }
    private val apiKey = "AIzaSyAGg8EhmZE8jLdwIJK3hbriXXo_r3EyMdg"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    suspend fun generateResponse(prompt: String, maxRetries: Int = 10): String {
        var lastException: Exception? = null

        for (attempt in 1..maxRetries) {
            try {
                return withContext(Dispatchers.IO) {
                    val request = GeminiRequest(
                        contents = listOf(
                            Content(
                                parts = listOf(
                                    Part(text = prompt)
                                )
                            )
                        )
                    )

                    val requestBody = json.encodeToString(request)
                        .toRequestBody("application/json".toMediaType())

                    val httpRequest = Request.Builder()
                        .url("$baseUrl?key=$apiKey")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(httpRequest).execute()
                    val responseBody = response.body?.string() ?: throw Exception("Empty response")

                    if (!response.isSuccessful) {
                        throw Exception("API request failed: ${response.code}")
                    }

                    val geminiResponse = json.decodeFromString<GeminiResponse>(responseBody)
                    geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "Sorry, I couldn't generate a response."
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt == maxRetries) {
                    return "Error: Network request failed after $maxRetries attempts. ${e.message}"
                } else {
                    // Exponential backoff delay
                    val delayMs = (1000L * (1 shl (attempt - 1))).coerceAtMost(10000L)
                    kotlinx.coroutines.delay(delayMs)
                }
            }
        }

        return "Error: Unexpected error occurred. ${lastException?.message}"
    }
}