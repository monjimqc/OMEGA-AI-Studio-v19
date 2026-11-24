package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun getSelfHealingFix(exceptionMessage: String, failingLogs: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "No API key configured. Utilizing local fallback heuristics."
        }

        val prompt = """
            You are Layer 3 (Self-Healing) and Layer 8 (Smart Exception Analysis) of a Verified Engineering Pipeline (VEP).
            An enterprise Android compilation has failed with the following exception:
            Exception: $exceptionMessage
            
            Logs:
            $failingLogs
            
            Based on this compilation error, perform a short root cause analysis and write a fully completed Kotlin file patch to resolve it.
            You must format your response exactly as a JSON object with two fields:
            - "analysis": A clear, short, 1-2 sentence explanation of why this error occurred.
            - "patch": The Kotlin code fix (e.g., corrected class import, fix of variable scope, missing parameter initialization). Make it look highly realistic and clean.
            
            Return ONLY the raw JSON object, do not wrap it in markdown codeblocks (no ```json ... ```) and do not add conversational text.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.2f),
            systemInstruction = Content(parts = listOf(Part(text = "You are a senior Android systems reliability engineer. Output only JSON.")))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            "Error calling Gemini: ${e.message}"
        }
    }
}
