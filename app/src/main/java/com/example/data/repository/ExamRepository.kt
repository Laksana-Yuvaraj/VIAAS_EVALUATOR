package com.example.data.repository

import com.example.data.model.BranchAction
import com.example.data.model.BranchRule
import com.example.data.model.ConditionType
import com.example.data.model.Exam
import com.example.data.model.Question
import com.example.data.model.QuestionType
import com.example.data.model.StudentSubmission
import com.example.data.model.SubjectiveEvaluation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExamRepository {

    private val _exams = MutableStateFlow<List<Exam>>(emptyList())
    val exams: StateFlow<List<Exam>> = _exams.asStateFlow()

    private val _submissions = MutableStateFlow<List<StudentSubmission>>(emptyList())
    val submissions: StateFlow<List<StudentSubmission>> = _submissions.asStateFlow()

    init {
        loadPreseededData()
    }

    private fun loadPreseededData() {
        val exam1 = Exam(
            id = "exam_cn_702",
            title = "Computer Networks & Cloud Protocols",
            description = "Midterm Assessment covering OSI layer 4 transport models, latency optimization, and reliability.",
            accessCode = "VIA-702",
            sourceMaterial = """
                The Transmission Control Protocol (TCP) is a connection-oriented, reliable transport layer protocol.
                Before any data is exchanged, TCP initiates a Three-way Handshake (SYN, SYN-ACK, ACK) to synchronize sequence numbers and verify mutual reachability.
                TCP guarantees delivery through acknowledgments, checksums, and packet retransmission.
                In contrast, User Datagram Protocol (UDP) is connectionless and does not guarantee packet arrival, minimizing header overhead and latency.
                Bandwidth refers to maximum transmission capacity, whereas Latency measures propagation and round-trip delay (RTT).
                Congestion control algorithms (like TCP Reno and BBR) regulate flow window sizes to prevent buffer bloat.
            """.trimIndent(),
            extractedKeywords = listOf(
                "Three-way Handshake",
                "TCP Protocol",
                "UDP Datagram",
                "Connection-oriented",
                "Latency",
                "Bandwidth",
                "Packet Retransmission",
                "Congestion Control",
                "Sequence Numbers",
                "Checksum"
            ),
            objectiveQuestions = listOf(
                Question(
                    id = "q1",
                    type = QuestionType.MCQ,
                    question = "Which sequence of control flags completes the TCP connection establishment?",
                    options = listOf(
                        "SYN -> SYN-ACK -> ACK",
                        "ACK -> SYN -> FIN",
                        "SYN -> ACK -> RST",
                        "DATA -> ACK -> PSH"
                    ),
                    correctAnswer = "SYN -> SYN-ACK -> ACK",
                    explanation = "TCP utilizes a three-step handshake: client sends SYN, server responds with SYN-ACK, client confirms with ACK.",
                    maxMarks = 2
                ),
                Question(
                    id = "q2",
                    type = QuestionType.TRUE_FALSE,
                    question = "UDP provides built-in packet retransmission and guarantees in-order segment delivery.",
                    options = listOf("True", "False"),
                    correctAnswer = "False",
                    explanation = "UDP is an unreliable, connectionless protocol with minimal overhead; retransmission logic must be handled at the application layer.",
                    maxMarks = 2
                ),
                Question(
                    id = "q3",
                    type = QuestionType.FILL_BLANK,
                    question = "The round-trip delay time taken for a packet to travel to destination and back is measured as ______.",
                    correctAnswer = "Latency",
                    explanation = "Latency (or RTT) quantifies communication delays over a network path.",
                    maxMarks = 2
                )
            ),
            subjectiveQuestions = listOf(
                Question(
                    id = "sq1",
                    type = QuestionType.SUBJECTIVE,
                    question = "In Question 1, you identified the protocol handshake as [Student's Answer to Q1]. Explain how this relates to TCP connection state, transmission reliability, and compare its latency suitability for real-time video streaming against UDP.",
                    maxMarks = 10,
                    requiredKeywords = listOf(
                        "Three-way Handshake",
                        "Connection-oriented",
                        "Packet Retransmission",
                        "UDP Datagram",
                        "Latency"
                    ),
                    idealAnswer = "TCP is a connection-oriented protocol requiring a Three-way Handshake and packet retransmission for guaranteed reliability. However, this causes higher latency. UDP sends lightweight datagrams without state or retransmission, making it ideal for real-time video streaming where low latency is preferred over complete lossless recovery."
                ),
                Question(
                    id = "sq1_remedial",
                    type = QuestionType.SUBJECTIVE,
                    question = "[REMEDIAL SYNTHESIS] Question 1 indicated lower score on handshake concepts. Review the 3-step TCP synchronization process (SYN, SYN-ACK, ACK) and explain why missing any packet results in timeout and retransmission.",
                    maxMarks = 5,
                    requiredKeywords = listOf(
                        "Three-way Handshake",
                        "SYN",
                        "SYN-ACK",
                        "ACK",
                        "Packet Retransmission"
                    ),
                    idealAnswer = "In the TCP Three-Way Handshake, the client sends a SYN flag with initial sequence number, the server replies with SYN-ACK, and client acknowledges with ACK. If any flag packet is dropped, connection timers trigger automatic retransmission.",
                    sectionId = "sec_remedial",
                    isRemedial = true
                )
            ),
            branchRules = listOf(
                BranchRule(
                    id = "rule_branch_1",
                    sourceQuestionId = "q1",
                    conditionType = ConditionType.SCORE_LESS_THAN,
                    conditionValue = "50%",
                    targetId = "sq1_remedial",
                    action = BranchAction.SHOW_REMEDIAL
                ),
                BranchRule(
                    id = "rule_branch_2",
                    sourceQuestionId = "q2",
                    conditionType = ConditionType.EQUALS_ANSWER,
                    conditionValue = "False",
                    targetId = "sec_b",
                    action = BranchAction.JUMP_TO_SECTION
                )
            ),
            isPasswordProtected = true,
            masterPassword = "VIAAS",
            hasNegativeMarking = true,
            negativeMarkingPenalty = 0.5,
            scoreMultiplier = 1.0,
            timeLimitMinutes = 15,
            perQuestionTargetSeconds = 90,
            googleSheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
        )

        val exam2 = Exam(
            id = "exam_ds_804",
            title = "Data Structures & Hash Optimization",
            description = "Module Evaluation on amortized Big-O performance, hash collisions, and cache locality.",
            accessCode = "VIA-804",
            sourceMaterial = """
                Hash tables provide average O(1) time complexity for lookup, insertion, and deletion.
                When two distinct keys produce the identical hash index, a Hash Collision occurs.
                Common resolution techniques include Chaining (linked lists or balanced trees) and Open Addressing (Linear Probing, Quadratic Probing, Double Hashing).
                Load Factor is defined as the ratio of stored elements to total bucket capacity. When the load factor exceeds a threshold (typically 0.75), rehashing is triggered.
            """.trimIndent(),
            extractedKeywords = listOf(
                "Hash Collision",
                "Chaining",
                "Open Addressing",
                "Load Factor",
                "Rehashing",
                "O(1) Time Complexity",
                "Linear Probing",
                "Bucket Capacity"
            ),
            objectiveQuestions = listOf(
                Question(
                    id = "ds_q1",
                    type = QuestionType.MCQ,
                    question = "What is the average time complexity for key retrieval in a well-distributed Hash Table?",
                    options = listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"),
                    correctAnswer = "O(1)",
                    explanation = "Direct array index hashing yields O(1) constant time average performance.",
                    maxMarks = 2
                ),
                Question(
                    id = "ds_q2",
                    type = QuestionType.FILL_BLANK,
                    question = "The ratio of elements to total bucket capacity in a hash table is called the ______.",
                    correctAnswer = "Load Factor",
                    explanation = "Load factor indicates bucket saturation before resizing.",
                    maxMarks = 2
                )
            ),
            subjectiveQuestions = listOf(
                Question(
                    id = "ds_sq1",
                    type = QuestionType.SUBJECTIVE,
                    question = "Explain how a Hash Collision is handled using Chaining versus Open Addressing, and describe when Rehashing is mandatory.",
                    maxMarks = 10,
                    requiredKeywords = listOf(
                        "Hash Collision",
                        "Chaining",
                        "Open Addressing",
                        "Load Factor",
                        "Rehashing"
                    ),
                    idealAnswer = "A Hash Collision happens when different keys map to the same bucket. In Chaining, collisions are stored in auxiliary linked lists or balanced trees. In Open Addressing, alternate vacant slots are checked via linear probing. When the Load Factor exceeds the threshold, Rehashing is mandatory to double capacity and redistribute keys."
                )
            ),
            timeLimitMinutes = 20,
            perQuestionTargetSeconds = 120,
            googleSheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
        )

        _exams.value = listOf(exam1, exam2)

        // Seed realistic student submissions for the Live Staff Dashboard
        val sub1 = StudentSubmission(
            id = "sub_101",
            examId = exam1.id,
            examTitle = exam1.title,
            studentName = "Alex Chen",
            rollNo = "24-CS-042",
            department = "Computer Science",
            totalScore = 15,
            maxScore = 16,
            percentage = 93.8,
            objectiveScore = 6,
            subjectiveScore = 9,
            totalTimeTakenSeconds = 345,
            questionTimes = mapOf("q1" to 32L, "q2" to 19L, "q3" to 44L, "sq1" to 250L),
            answers = mapOf(
                "q1" to "SYN -> SYN-ACK -> ACK",
                "q2" to "False",
                "q3" to "Latency",
                "sq1" to "TCP is a connection-oriented system that initiates with a Three-way Handshake. It uses packet retransmission for guaranteed reliability. On the other hand, UDP datagram streams have zero connection setup which slashes latency, making it the superior choice for live audio and video streaming."
            ),
            subjectiveEvaluations = listOf(
                SubjectiveEvaluation(
                    questionId = "sq1",
                    questionPrompt = exam1.subjectiveQuestions[0].question,
                    studentAnswer = "TCP is a connection-oriented system that initiates with a Three-way Handshake...",
                    marksAwarded = 9,
                    maxMarks = 10,
                    matchedKeywords = listOf("Three-way Handshake", "Connection-oriented", "Packet Retransmission", "UDP Datagram", "Latency"),
                    missingKeywords = emptyList(),
                    feedback = "Flawless demonstration of transport mechanisms. All 5 required keywords correctly synthesized with real-time video trade-offs."
                )
            ),
            googleSheetsSyncStatus = "SYNCED",
            timestamp = System.currentTimeMillis() - 3600000 * 2
        )

        val sub2 = StudentSubmission(
            id = "sub_102",
            examId = exam1.id,
            examTitle = exam1.title,
            studentName = "Priya Sharma",
            rollNo = "24-CS-089",
            department = "Information Technology",
            totalScore = 12,
            maxScore = 16,
            percentage = 75.0,
            objectiveScore = 6,
            subjectiveScore = 6,
            totalTimeTakenSeconds = 480,
            questionTimes = mapOf("q1" to 45L, "q2" to 28L, "q3" to 52L, "sq1" to 355L),
            answers = mapOf(
                "q1" to "SYN -> SYN-ACK -> ACK",
                "q2" to "False",
                "q3" to "Latency",
                "sq1" to "TCP needs a connection-oriented handshake before sending. It retries lost packets. UDP does not connect and is faster for video calls."
            ),
            subjectiveEvaluations = listOf(
                SubjectiveEvaluation(
                    questionId = "sq1",
                    questionPrompt = exam1.subjectiveQuestions[0].question,
                    studentAnswer = "TCP needs a connection-oriented handshake before sending...",
                    marksAwarded = 6,
                    maxMarks = 10,
                    matchedKeywords = listOf("Connection-oriented", "Packet Retransmission"),
                    missingKeywords = listOf("Three-way Handshake", "UDP Datagram", "Latency"),
                    feedback = "Basic intuition is good. Missing exact terms: Three-way Handshake, UDP Datagram, and Latency measurement."
                )
            ),
            googleSheetsSyncStatus = "SYNCED",
            timestamp = System.currentTimeMillis() - 3600000 * 5
        )

        val sub3 = StudentSubmission(
            id = "sub_103",
            examId = exam1.id,
            examTitle = exam1.title,
            studentName = "Marcus Vance",
            rollNo = "24-CS-114",
            department = "Software Engineering",
            totalScore = 10,
            maxScore = 16,
            percentage = 62.5,
            objectiveScore = 4,
            subjectiveScore = 6,
            totalTimeTakenSeconds = 410,
            questionTimes = mapOf("q1" to 50L, "q2" to 35L, "q3" to 65L, "sq1" to 260L),
            answers = mapOf(
                "q1" to "SYN -> SYN-ACK -> ACK",
                "q2" to "True",
                "q3" to "Latency",
                "sq1" to "TCP has high reliability and handles packet retransmission. UDP is low latency and avoids connection establishment."
            ),
            subjectiveEvaluations = listOf(
                SubjectiveEvaluation(
                    questionId = "sq1",
                    questionPrompt = exam1.subjectiveQuestions[0].question,
                    studentAnswer = "TCP has high reliability...",
                    marksAwarded = 6,
                    maxMarks = 10,
                    matchedKeywords = listOf("Packet Retransmission", "Latency"),
                    missingKeywords = listOf("Three-way Handshake", "Connection-oriented", "UDP Datagram"),
                    feedback = "Good point on low latency. Needs deeper explanation of the Three-way Handshake and Datagram structure."
                )
            ),
            googleSheetsSyncStatus = "SYNCED",
            timestamp = System.currentTimeMillis() - 3600000 * 8
        )

        _submissions.value = listOf(sub1, sub2, sub3)
    }

    fun getExamByAccessCode(code: String): Exam? {
        val clean = code.trim().uppercase()
        return _exams.value.find { it.accessCode.trim().uppercase() == clean }
    }

    fun getExamById(id: String): Exam? {
        return _exams.value.find { it.id == id }
    }

    fun saveExam(exam: Exam) {
        _exams.update { current ->
            val index = current.indexOfFirst { it.id == exam.id }
            if (index >= 0) {
                current.toMutableList().also { it[index] = exam }
            } else {
                listOf(exam) + current
            }
        }
    }

    fun saveSubmission(submission: StudentSubmission) {
        _submissions.update { current ->
            listOf(submission) + current.filter { it.id != submission.id }
        }
    }

    fun getSubmissionsForExam(examId: String): List<StudentSubmission> {
        return _submissions.value.filter { it.examId == examId }
    }

    fun updateSheetId(examId: String, newSheetId: String) {
        _exams.update { current ->
            current.map {
                if (it.id == examId) it.copy(googleSheetId = newSheetId) else it
            }
        }
    }

    fun archiveSubmissionsOlderThan(days: Int): Int {
        val cutoff = System.currentTimeMillis() - (days.toLong() * 24L * 3600L * 1000L)
        var removedCount = 0
        _submissions.update { current ->
            val remaining = current.filter { it.timestamp >= cutoff }
            removedCount = current.size - remaining.size
            remaining
        }
        return removedCount
    }
}
