package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Question
import com.example.data.model.QuestionType
import com.example.data.model.SubjectiveEvaluation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun callGeminiApi(prompt: String): String? = withContext(Dispatchers.IO) {
        val key = getApiKey()
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            Log.w("GeminiService", "GEMINI_API_KEY not configured, using fallback AI engine")
            return@withContext null
        }

        // Try gemini-2.5-flash first as requested in prompt, fallback to gemini-3.5-flash
        val models = listOf("gemini-2.5-flash", "gemini-3.5-flash")
        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key"
                val requestBodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.3)
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseStr = response.body?.string() ?: ""

                if (response.isSuccessful && responseStr.isNotBlank()) {
                    val root = JSONObject(responseStr)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text", "")
                            if (text.isNotBlank()) {
                                return@withContext text
                            }
                        }
                    }
                } else {
                    Log.w("GeminiService", "Model $model returned code: ${response.code}, msg: $responseStr")
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Error calling $model: ${e.message}")
            }
        }
        null
    }

    suspend fun extractKeywords(materialText: String): List<String> = withContext(Dispatchers.Default) {
        if (materialText.isBlank()) return@withContext emptyList()

        val prompt = """
            You are the VIAAS Smart Assessment keyword extractor.
            Scan the following educational syllabus / reading material and extract 8 to 15 core technical terms, fundamental concepts, and essential keywords that must be evaluated in a test.
            
            Material:
            \"\"\"
            $materialText
            \"\"\"
            
            Return ONLY a valid JSON array of strings, for example:
            ["TCP/IP Handshake", "Latency", "Bandwidth", "Packet Switching", "DNS Resolution", "OSI Model"]
        """.trimIndent()

        val response = callGeminiApi(prompt)
        if (response != null) {
            try {
                val clean = cleanJsonMarkdown(response)
                val jsonArray = JSONArray(clean)
                val result = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.optString(i)?.trim()
                    if (!item.isNullOrBlank()) {
                        result.add(item)
                    }
                }
                if (result.isNotEmpty()) return@withContext result
            } catch (e: Exception) {
                Log.e("GeminiService", "Failed to parse extracted keywords JSON: ${e.message}")
            }
        }

        // Heuristic fallback keyword extractor
        fallbackExtractKeywords(materialText)
    }

    suspend fun generateExamQuestions(
        materialText: String,
        keywords: List<String>,
        objectiveCount: Int,
        subjectiveCount: Int
    ): Pair<List<Question>, List<Question>> = withContext(Dispatchers.Default) {
        val prompt = """
            You are the VIAAS Evaluator quiz generator.
            Given the material and core keywords below, generate:
            - $objectiveCount Objective questions (mix of MCQ, TRUE_FALSE, and FILL_BLANK)
            - $subjectiveCount Subjective questions (Short answer or essay with specific required keywords and ideal answer)
            
            Keywords: ${keywords.joinToString(", ")}
            Material:
            \"\"\"
            $materialText
            \"\"\"
            
            Return ONLY a valid JSON object matching this schema:
            {
              "objectiveQuestions": [
                {
                  "id": "q1",
                  "type": "mcq", // "mcq" | "true_false" | "fill_blank"
                  "question": "What protocol provides reliable end-to-end transport?",
                  "options": ["TCP", "UDP", "ICMP", "ARP"], // for mcq or true_false (options: ["True", "False"])
                  "correctAnswer": "TCP",
                  "explanation": "TCP ensures reliable data delivery via sequence numbers and ACKs.",
                  "maxMarks": 2
                }
              ],
              "subjectiveQuestions": [
                {
                  "id": "sq1",
                  "question": "Explain the difference between TCP and UDP in packet transmission.",
                  "maxMarks": 10,
                  "requiredKeywords": ["Connection-oriented", "Reliability", "Three-way handshake", "Overhead", "Datagram"],
                  "idealAnswer": "TCP is a connection-oriented protocol that guarantees packet delivery using a three-way handshake and acknowledgments. UDP is connectionless and sends datagrams without guarantee, offering lower latency."
                }
              ]
            }
        """.trimIndent()

        val response = callGeminiApi(prompt)
        if (response != null) {
            try {
                val clean = cleanJsonMarkdown(response)
                val root = JSONObject(clean)
                val objList = mutableListOf<Question>()
                val subjList = mutableListOf<Question>()

                val objArray = root.optJSONArray("objectiveQuestions")
                if (objArray != null) {
                    for (i in 0 until objArray.length()) {
                        val item = objArray.getJSONObject(i)
                        val typeStr = item.optString("type", "mcq").lowercase()
                        val type = when {
                            typeStr.contains("true") -> QuestionType.TRUE_FALSE
                            typeStr.contains("fill") -> QuestionType.FILL_BLANK
                            else -> QuestionType.MCQ
                        }
                        val opts = mutableListOf<String>()
                        val optsArr = item.optJSONArray("options")
                        if (optsArr != null) {
                            for (j in 0 until optsArr.length()) {
                                opts.add(optsArr.getString(j))
                            }
                        } else if (type == QuestionType.TRUE_FALSE) {
                            opts.addAll(listOf("True", "False"))
                        }

                        objList.add(
                            Question(
                                id = item.optString("id", "obj_$i"),
                                type = type,
                                question = item.optString("question", ""),
                                options = opts,
                                correctAnswer = item.optString("correctAnswer", ""),
                                explanation = item.optString("explanation", ""),
                                maxMarks = item.optInt("maxMarks", 2)
                            )
                        )
                    }
                }

                val subjArray = root.optJSONArray("subjectiveQuestions")
                if (subjArray != null) {
                    for (i in 0 until subjArray.length()) {
                        val item = subjArray.getJSONObject(i)
                        val kwList = mutableListOf<String>()
                        val kwArr = item.optJSONArray("requiredKeywords")
                        if (kwArr != null) {
                            for (k in 0 until kwArr.length()) {
                                kwList.add(kwArr.getString(k))
                            }
                        }

                        subjList.add(
                            Question(
                                id = item.optString("id", "subj_$i"),
                                type = QuestionType.SUBJECTIVE,
                                question = item.optString("question", ""),
                                maxMarks = item.optInt("maxMarks", 10),
                                requiredKeywords = kwList,
                                idealAnswer = item.optString("idealAnswer", "")
                            )
                        )
                    }
                }

                if (objList.isNotEmpty() || subjList.isNotEmpty()) {
                    return@withContext Pair(objList, subjList)
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Failed to parse generated questions: ${e.message}")
            }
        }

        // Fallback question generation
        fallbackGenerateQuestions(keywords, objectiveCount, subjectiveCount)
    }

    suspend fun evaluateSubjectiveResponse(
        questionPrompt: String,
        idealAnswer: String,
        requiredKeywords: List<String>,
        maxMarks: Int,
        studentAnswer: String
    ): SubjectiveEvaluation = withContext(Dispatchers.Default) {
        if (studentAnswer.isBlank()) {
            return@withContext SubjectiveEvaluation(
                questionId = "",
                questionPrompt = questionPrompt,
                studentAnswer = studentAnswer,
                marksAwarded = 0,
                maxMarks = maxMarks,
                matchedKeywords = emptyList(),
                missingKeywords = requiredKeywords,
                feedback = "No answer provided. Student left this subjective question blank."
            )
        }

        val prompt = """
            You are the VIAAS AI Keyword Evaluation Engine.
            Evaluate this student's response against the required keywords and ideal answer.
            
            Question: "$questionPrompt"
            Ideal Answer: "$idealAnswer"
            Required Target Keywords: ${requiredKeywords.joinToString(", ")}
            Max Marks: $maxMarks
            Student's Response: "$studentAnswer"
            
            Determine:
            1. matchedKeywords: Which of the required target keywords did the student explicitly mention or accurately describe semantically?
            2. missingKeywords: Which target keywords were omitted or not explained?
            3. marksAwarded: Integer score from 0 to $maxMarks based on keyword completeness and conceptual accuracy.
            4. feedback: Professional, constructive feedback stating strengths and what was missing.
            
            Return ONLY a valid JSON object matching:
            {
              "marksAwarded": 8,
              "matchedKeywords": ["Three-way handshake", "Reliability"],
              "missingKeywords": ["Datagram", "Overhead"],
              "feedback": "Strong explanation of TCP connection mechanisms. To score full marks, compare packet overhead and datagram flow in UDP."
            }
        """.trimIndent()

        val response = callGeminiApi(prompt)
        if (response != null) {
            try {
                val clean = cleanJsonMarkdown(response)
                val obj = JSONObject(clean)
                val marks = obj.optInt("marksAwarded", maxMarks / 2).coerceIn(0, maxMarks)
                val matched = mutableListOf<String>()
                val matchedArr = obj.optJSONArray("matchedKeywords")
                if (matchedArr != null) {
                    for (i in 0 until matchedArr.length()) matched.add(matchedArr.getString(i))
                }
                val missing = mutableListOf<String>()
                val missingArr = obj.optJSONArray("missingKeywords")
                if (missingArr != null) {
                    for (i in 0 until missingArr.length()) missing.add(missingArr.getString(i))
                }
                val feedback = obj.optString("feedback", "Automated AI grading completed.")

                return@withContext SubjectiveEvaluation(
                    questionId = "",
                    questionPrompt = questionPrompt,
                    studentAnswer = studentAnswer,
                    marksAwarded = marks,
                    maxMarks = maxMarks,
                    matchedKeywords = matched,
                    missingKeywords = missing,
                    feedback = feedback
                )
            } catch (e: Exception) {
                Log.e("GeminiService", "Failed to parse evaluation response: ${e.message}")
            }
        }

        // Heuristic fallback grading engine
        fallbackEvaluateAnswer(questionPrompt, idealAnswer, requiredKeywords, maxMarks, studentAnswer)
    }

    private fun cleanJsonMarkdown(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json").trim()
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```").trim()
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```").trim()
        }
        return str
    }

    private fun fallbackExtractKeywords(text: String): List<String> {
        val stopWords = setOf(
            "the", "and", "a", "an", "is", "are", "was", "were", "to", "in", "of", "for", "on", "with",
            "as", "by", "at", "from", "that", "this", "which", "it", "be", "or", "an", "will", "can",
            "have", "has", "had", "not", "but", "also", "its", "all", "been", "when", "more", "into"
        )
        val words = text.split(Regex("[^a-zA-Z0-9_-]+"))
            .map { it.trim() }
            .filter { it.length > 3 && !stopWords.contains(it.lowercase()) }

        val freq = mutableMapOf<String, Int>()
        for (w in words) {
            val key = w.replaceFirstChar { it.uppercase() }
            freq[key] = (freq[key] ?: 0) + 1
        }

        val topWords = freq.entries.sortedByDescending { it.value }.take(10).map { it.key }
        return if (topWords.size >= 4) topWords else listOf(
            "Core Protocol", "Architecture", "Data Integrity", "Latency Optimization",
            "Throughput", "Concurrency", "Security Encryption", "Fault Tolerance"
        )
    }

    private fun fallbackGenerateQuestions(
        keywords: List<String>,
        objCount: Int,
        subjCount: Int
    ): Pair<List<Question>, List<Question>> {
        val safeKeywords = if (keywords.isNotEmpty()) keywords else listOf(
            "Bandwidth", "Handshake", "Packet Routing", "Checksum", "Latency"
        )
        val objList = mutableListOf<Question>()

        if (objCount >= 1) {
            val kw1 = safeKeywords.getOrElse(0) { "TCP Protocol" }
            objList.add(
                Question(
                    id = "obj_1",
                    type = QuestionType.MCQ,
                    question = "Which primary mechanism is used to ensure reliability in $kw1?",
                    options = listOf(
                        "Three-way handshake & ACKs",
                        "Best-effort broadcast",
                        "Unsynchronized UDP datagrams",
                        "Direct memory bus transfer"
                    ),
                    correctAnswer = "Three-way handshake & ACKs",
                    explanation = "$kw1 utilizes sequence numbers and acknowledgements for guaranteed delivery.",
                    maxMarks = 2
                )
            )
        }
        if (objCount >= 2) {
            val kw2 = safeKeywords.getOrElse(1) { "Throughput" }
            objList.add(
                Question(
                    id = "obj_2",
                    type = QuestionType.TRUE_FALSE,
                    question = "True or False: Increasing packet retransmission frequency directly optimizes $kw2 under severe network congestion.",
                    options = listOf("True", "False"),
                    correctAnswer = "False",
                    explanation = "Aggressive retransmissions during high congestion causes congestion collapse and lowers effective throughput.",
                    maxMarks = 2
                )
            )
        }
        if (objCount >= 3) {
            val kw3 = safeKeywords.getOrElse(2) { "Latency" }
            objList.add(
                Question(
                    id = "obj_3",
                    type = QuestionType.FILL_BLANK,
                    question = "The round-trip delay time taken for a data packet to travel from sender to receiver is known as ______.",
                    correctAnswer = kw3,
                    explanation = "$kw3 represents the round-trip propagation and queuing delay.",
                    maxMarks = 2
                )
            )
        }

        val subjList = mutableListOf<Question>()
        val kwTarget = safeKeywords.take(4)
        subjList.add(
            Question(
                id = "subj_1",
                type = QuestionType.SUBJECTIVE,
                question = "Describe the operational workflow and architectural advantages of ${kwTarget.joinToString(" and ")} in modern distributed computing.",
                maxMarks = 10,
                requiredKeywords = kwTarget,
                idealAnswer = "The core workflow relies on ${kwTarget.joinToString(", ")} to ensure synchronized state, resilient data transmission, and low latency across nodes."
            )
        )

        return Pair(objList, subjList)
    }

    private fun fallbackEvaluateAnswer(
        questionPrompt: String,
        idealAnswer: String,
        requiredKeywords: List<String>,
        maxMarks: Int,
        studentAnswer: String
    ): SubjectiveEvaluation {
        val lowerAnswer = studentAnswer.lowercase()
        val matched = mutableListOf<String>()
        val missing = mutableListOf<String>()

        for (kw in requiredKeywords) {
            val cleanKw = kw.trim().lowercase()
            val tokens = cleanKw.split(" ")
            val isMatched = if (tokens.size > 1) {
                lowerAnswer.contains(cleanKw) || tokens.all { lowerAnswer.contains(it) }
            } else {
                lowerAnswer.contains(cleanKw)
            }

            if (isMatched) {
                matched.add(kw)
            } else {
                missing.add(kw)
            }
        }

        val keywordRatio = if (requiredKeywords.isNotEmpty()) {
            matched.size.toDouble() / requiredKeywords.size.toDouble()
        } else 1.0

        val lengthFactor = (studentAnswer.length / 120.0).coerceIn(0.4, 1.0)
        val calculatedScore = (maxMarks * keywordRatio * lengthFactor).toInt().coerceIn(1, maxMarks)

        val feedback = when {
            keywordRatio >= 0.8 -> "Excellent conceptual understanding! Correctly articulated key technical terms including ${matched.take(3).joinToString(", ")}."
            keywordRatio >= 0.5 -> "Good foundation. You covered ${matched.joinToString(", ")}, but need to integrate missing concepts: ${missing.joinToString(", ")}."
            else -> "Needs improvement. Critical technical terms missing: ${missing.joinToString(", ")}. Expand your response with concrete principles."
        }

        return SubjectiveEvaluation(
            questionId = "",
            questionPrompt = questionPrompt,
            studentAnswer = studentAnswer,
            marksAwarded = calculatedScore,
            maxMarks = maxMarks,
            matchedKeywords = matched,
            missingKeywords = missing,
            feedback = feedback
        )
    }
}
