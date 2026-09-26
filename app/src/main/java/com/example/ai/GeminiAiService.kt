package com.example.ai

import com.example.BuildConfig
import com.example.math.MathEvaluator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SolverResult(
    val quickAnswer: String,
    val steps: String,
    val formulasUsed: String,
    val stealthHint: String,
    val rawResponse: String
)

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun solveProblem(query: String): Result<SolverResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide intelligent local fallback evaluation if it's a math expression
            val localEval = MathEvaluator.evaluate(query)
            val fallbackAnswer = when (localEval) {
                is com.example.math.EvaluationResult.Success -> localEval.formatted
                is com.example.math.EvaluationResult.Error -> "Please configure GEMINI_API_KEY"
            }
            return@withContext Result.success(
                SolverResult(
                    quickAnswer = fallbackAnswer,
                    steps = "1. Evaluated locally with internal Math Engine: $query = $fallbackAnswer\n\n(Tip: Enter your GEMINI_API_KEY in AI Studio Secrets panel for AI word problems, calculus, and step-by-step proofs).",
                    formulasUsed = "Standard Operator Precedence (PEMDAS / Shunting-Yard)",
                    stealthHint = "Ans: $fallbackAnswer",
                    rawResponse = "Local solver output"
                )
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val promptText = """
                You are a high-speed STEM Math & Science AI Problem Solver Agent.
                Solve the following problem with utmost precision:
                Problem: "$query"

                Please structure your response strictly in the following format:
                [FINAL_ANSWER]
                The exact final simplified numerical or algebraic answer. Keep this very prominent and clean.

                [STEPS]
                Clear, numbered step-by-step derivation that can be easily written down on paper or an exam.

                [FORMULAS]
                The primary formulas, rules, or theorems applied (e.g., Chain Rule, Quadratic Formula, Ohm's Law).

                [STEALTH_HINT]
                A single short line containing only the answer and immediate core formula for quick discreet glance.
            """.trimIndent()

            val partsArray = JSONArray().apply {
                put(JSONObject().apply { put("text", promptText) })
            }
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply { put("parts", partsArray) })
            }
            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2) // Low temperature for high math accuracy
                    put("maxOutputTokens", 2048)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: "HTTP ${response.code}"
                    return@withContext Result.failure(Exception("AI Solver Error (${response.code}): $errBody"))
                }

                val bodyString = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext Result.failure(Exception("No candidate returned from AI"))
                }

                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text") ?: ""

                val parsed = parseStructuredOutput(text, query)
                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseStructuredOutput(text: String, originalQuery: String): SolverResult {
        var finalAnswer = ""
        var steps = ""
        var formulas = ""
        var stealthHint = ""

        if (text.contains("[FINAL_ANSWER]")) {
            val afterAns = text.substringAfter("[FINAL_ANSWER]")
            finalAnswer = afterAns.substringBefore("[STEPS]").trim()

            if (text.contains("[STEPS]")) {
                val afterSteps = text.substringAfter("[STEPS]")
                steps = afterSteps.substringBefore("[FORMULAS]").trim()
            }

            if (text.contains("[FORMULAS]")) {
                val afterFormulas = text.substringAfter("[FORMULAS]")
                formulas = afterFormulas.substringBefore("[STEALTH_HINT]").trim()
            }

            if (text.contains("[STEALTH_HINT]")) {
                stealthHint = text.substringAfter("[STEALTH_HINT]").trim()
            }
        } else {
            // Fallback if model didn't use tags strictly
            finalAnswer = text.lines().firstOrNull { it.isNotBlank() } ?: text
            steps = text
            formulas = "Direct Derivation"
            stealthHint = finalAnswer.take(40)
        }

        if (stealthHint.isBlank()) {
            stealthHint = "Ans: ${finalAnswer.take(30)}"
        }

        return SolverResult(
            quickAnswer = finalAnswer,
            steps = steps,
            formulasUsed = formulas,
            stealthHint = stealthHint,
            rawResponse = text
        )
    }
}
