package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.FirestoreSyncService
import com.example.data.gemini.GeminiService
import com.example.data.model.BranchAction
import com.example.data.model.BranchRule
import com.example.data.model.ConditionType
import com.example.data.model.Exam
import com.example.data.model.Question
import com.example.data.model.QuestionType
import com.example.data.model.StudentSubmission
import com.example.data.model.SubjectiveEvaluation
import com.example.data.repository.ExamRepository
import com.example.data.sheets.GoogleSheetsSyncManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    PORTAL_SELECTOR,
    EDUCATOR_DASHBOARD,
    EDUCATOR_CREATE_EXAM,
    TEACHER_ASSESSMENT_BUILDER,
    STUDENT_ONBOARDING,
    STUDENT_EXAM_RUNNER,
    STUDENT_RESULT,
    STUDENT_HISTORY,
    STUDENT_HISTORY_DETAIL,
    CERTIFICATE_VIEW,
    GOOGLE_SHEETS_VIEWER
}

data class ExamCreationState(
    val title: String = "",
    val description: String = "",
    val sourceMaterial: String = "",
    val extractedKeywords: List<String> = emptyList(),
    val isExtractingKeywords: Boolean = false,
    val objectiveCount: Int = 3,
    val subjectiveCount: Int = 1,
    val timeLimitMinutes: Int = 15,
    val googleSheetId: String = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms",
    val generatedObjectiveQuestions: List<Question> = emptyList(),
    val generatedSubjectiveQuestions: List<Question> = emptyList(),
    val isGeneratingExam: Boolean = false,
    val step: Int = 1, // 1: Material & Keywords, 2: Settings & Questions, 3: Review & Edit
    val editingExamId: String? = null,
    val editingExistingCode: String? = null,
    
    // Pro & Enterprise Settings
    val isPasswordProtected: Boolean = false,
    val masterPassword: String = "VIAAS2026",
    val limitOnePerRollNo: Boolean = true,
    val hasNegativeMarking: Boolean = false,
    val negativeMarkingPenalty: Double = 0.5,
    val scoreMultiplier: Double = 1.0,
    val retentionDays: Int = 90,
    val webhookUrl: String = "https://script.google.com/macros/s/AKfycbx_viaas_webhook/exec"
)

data class StudentExamState(
    val studentName: String = "",
    val rollNo: String = "",
    val department: String = "Computer Science & Engineering",
    val accessCode: String = "VIA-702",
    val enteredPasscode: String = "",
    val currentExam: Exam? = null,
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(), // questionId -> answer
    val questionTimesSeconds: Map<String, Long> = emptyMap(), // questionId -> seconds
    val activeQuestionSeconds: Long = 0L,
    val totalElapsedSeconds: Long = 0L,
    val isSubmitting: Boolean = false,
    val latestSubmission: StudentSubmission? = null,
    val errorMessage: String? = null,
    val isSectionTransitionActive: Boolean = false,
    val sectionTransitionTitle: String = "Section Transition",
    val sectionTransitionMessage: String = "You have completed Section A (Objectives) and are now advancing to Section B: Conceptual Subjective Synthesis.",
    val conditionalRoutingBanner: String? = null,
    val draftAutoSavedTime: String? = null
)

class ExamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExamRepository()
    private val geminiService = GeminiService()
    private val sheetsSyncManager = GoogleSheetsSyncManager(application)
    private val firestoreSyncService = FirestoreSyncService(application)

    private val _currentScreen = MutableStateFlow(AppScreen.PORTAL_SELECTOR)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val exams: StateFlow<List<Exam>> = repository.exams
    val submissions: StateFlow<List<StudentSubmission>> = repository.submissions

    private val _creationState = MutableStateFlow(ExamCreationState())
    val creationState: StateFlow<ExamCreationState> = _creationState.asStateFlow()

    private val _studentState = MutableStateFlow(StudentExamState())
    val studentState: StateFlow<StudentExamState> = _studentState.asStateFlow()

    private val _selectedExamForAnalytics = MutableStateFlow<Exam?>(null)
    val selectedExamForAnalytics: StateFlow<Exam?> = _selectedExamForAnalytics.asStateFlow()

    private val _selectedHistorySubmission = MutableStateFlow<StudentSubmission?>(null)
    val selectedHistorySubmission: StateFlow<StudentSubmission?> = _selectedHistorySubmission.asStateFlow()

    private val _selectedCertificateSubmission = MutableStateFlow<StudentSubmission?>(null)
    val selectedCertificateSubmission: StateFlow<StudentSubmission?> = _selectedCertificateSubmission.asStateFlow()

    private val _appLanguage = MutableStateFlow(com.example.ui.components.AppLanguage.ENGLISH)
    val appLanguage: StateFlow<com.example.ui.components.AppLanguage> = _appLanguage.asStateFlow()

    private val _isCustomBranding = MutableStateFlow(true)
    val isCustomBranding: StateFlow<Boolean> = _isCustomBranding.asStateFlow()

    private var questionTimerJob: Job? = null

    init {
        _selectedExamForAnalytics.value = repository.exams.value.firstOrNull()

        // Sync initial preseeded exams to Firestore in the background
        viewModelScope.launch {
            repository.exams.value.forEach { exam ->
                firestoreSyncService.syncExam(exam)
            }
            // Also seed initial submissions
            repository.submissions.value.forEach { sub ->
                firestoreSyncService.syncSubmission(sub)
            }
        }

        // Real-time synchronization: listen to submissions from Firestore to update educator dashboard in real-time
        viewModelScope.launch {
            firestoreSyncService.observeSubmissions().collect { remoteList ->
                if (remoteList.isNotEmpty()) {
                    remoteList.forEach { remoteSub ->
                        repository.saveSubmission(remoteSub)
                    }
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen != AppScreen.STUDENT_EXAM_RUNNER) {
            stopQuestionTimer()
        }
        _currentScreen.value = screen
    }

    fun viewCertificate(submission: StudentSubmission) {
        _selectedCertificateSubmission.value = submission
        _currentScreen.value = AppScreen.CERTIFICATE_VIEW
    }

    fun toggleLanguage() {
        _appLanguage.value = if (_appLanguage.value == com.example.ui.components.AppLanguage.ENGLISH) {
            com.example.ui.components.AppLanguage.TAMIL
        } else {
            com.example.ui.components.AppLanguage.ENGLISH
        }
    }

    fun setLanguage(lang: com.example.ui.components.AppLanguage) {
        _appLanguage.value = lang
    }

    fun toggleCustomBranding() {
        _isCustomBranding.value = !_isCustomBranding.value
    }

    fun applyDataRetention(days: Int): Int {
        return repository.archiveSubmissionsOlderThan(days)
    }

    fun selectExamForAnalytics(exam: Exam) {
        _selectedExamForAnalytics.value = exam
    }

    // --- Educator Exam Creation & Editing Workflows ---

    fun updateCreationMaterial(text: String) {
        _creationState.update { it.copy(sourceMaterial = text) }
    }

    fun updateCreationTitle(title: String) {
        _creationState.update { it.copy(title = title) }
    }

    fun updateCreationDescription(desc: String) {
        _creationState.update { it.copy(description = desc) }
    }

    fun updateCreationSettings(objCount: Int, subjCount: Int, timeLimit: Int, sheetId: String) {
        _creationState.update {
            it.copy(
                objectiveCount = objCount,
                subjectiveCount = subjCount,
                timeLimitMinutes = timeLimit,
                googleSheetId = sheetId
            )
        }
    }

    fun updateProSettings(
        isPasswordProtected: Boolean,
        masterPassword: String,
        limitOnePerRollNo: Boolean,
        hasNegativeMarking: Boolean,
        negativeMarkingPenalty: Double,
        scoreMultiplier: Double,
        retentionDays: Int,
        webhookUrl: String
    ) {
        _creationState.update {
            it.copy(
                isPasswordProtected = isPasswordProtected,
                masterPassword = masterPassword,
                limitOnePerRollNo = limitOnePerRollNo,
                hasNegativeMarking = hasNegativeMarking,
                negativeMarkingPenalty = negativeMarkingPenalty,
                scoreMultiplier = scoreMultiplier,
                retentionDays = retentionDays,
                webhookUrl = webhookUrl
            )
        }
    }

    fun setCreationStep(step: Int) {
        _creationState.update { it.copy(step = step) }
    }

    fun loadSampleMaterial(preset: String) {
        val (title, text) = when (preset) {
            "Database Systems" -> Pair(
                "Database Management & ACID Transactions",
                """
                    A Transaction is a logical unit of work in a Relational Database Management System (RDBMS).
                    Transactions must guarantee the ACID properties:
                    1. Atomicity: All operations succeed, or the entire transaction is rolled back using Write-Ahead Logging (WAL).
                    2. Consistency: Ensures state changes abide by schema integrity constraints and cascading foreign keys.
                    3. Isolation: Concurrent transactions do not interfere; achieved via Two-Phase Locking (2PL), MVCC (Multi-Version Concurrency Control), and Isolation Levels (Read Committed, Repeatable Read, Serializable).
                    4. Durability: Committed changes survive system crashes and power failures by flushing logs to persistent non-volatile storage.
                """.trimIndent()
            )
            "Operating Systems" -> Pair(
                "Operating Systems & Concurrency Synchronization",
                """
                    Concurrency in Modern OS involves process threads sharing address spaces.
                    A Race Condition occurs when multiple threads access shared resources without synchronization.
                    The Critical Section problem requires Mutual Exclusion, Progress, and Bounded Waiting.
                    Synchronization primitives include Semaphores (counting and binary mutexes) and Monitors.
                    A Deadlock arises under four simultaneous conditions: Mutual Exclusion, Hold and Wait, No Preemption, and Circular Wait.
                    Virtual memory relies on Paging and the Translation Lookaside Buffer (TLB).
                """.trimIndent()
            )
            else -> Pair(
                "Cloud Architecture & Microservices",
                """
                    Microservices architecture structures applications into independently deployable distributed services.
                    Communication occurs asynchronously via Event Streams (Kafka/RabbitMQ) or synchronously via REST/gRPC.
                    Service Discovery, Circuit Breakers, and API Gateways manage routing and prevent cascading failures.
                    Containerization with Docker and orchestration via Kubernetes ensure auto-scaling and zero-downtime rolling updates.
                """.trimIndent()
            )
        }
        _creationState.update {
            it.copy(
                title = title,
                sourceMaterial = text
            )
        }
    }

    fun extractKeywords() {
        val text = _creationState.value.sourceMaterial
        if (text.isBlank()) return

        viewModelScope.launch {
            _creationState.update { it.copy(isExtractingKeywords = true) }
            val keywords = geminiService.extractKeywords(text)
            _creationState.update {
                it.copy(
                    isExtractingKeywords = false,
                    extractedKeywords = keywords
                )
            }
        }
    }

    fun addExtractedKeyword(kw: String) {
        val clean = kw.trim()
        if (clean.isNotBlank()) {
            _creationState.update {
                if (!it.extractedKeywords.contains(clean)) {
                    it.copy(extractedKeywords = it.extractedKeywords + clean)
                } else it
            }
        }
    }

    fun removeExtractedKeyword(kw: String) {
        _creationState.update {
            it.copy(extractedKeywords = it.extractedKeywords.filter { item -> item != kw })
        }
    }

    fun updateExtractedKeyword(oldKeyword: String, newKeyword: String) {
        val clean = newKeyword.trim()
        if (clean.isNotBlank()) {
            _creationState.update { state ->
                val updated = state.extractedKeywords.map { if (it == oldKeyword) clean else it }.distinct()
                state.copy(extractedKeywords = updated)
            }
        }
    }

    fun generateQuizQuestions() {
        val state = _creationState.value
        viewModelScope.launch {
            _creationState.update { it.copy(isGeneratingExam = true) }
            val (objs, subjs) = geminiService.generateExamQuestions(
                materialText = state.sourceMaterial,
                keywords = state.extractedKeywords,
                objectiveCount = state.objectiveCount,
                subjectiveCount = state.subjectiveCount
            )
            _creationState.update {
                it.copy(
                    isGeneratingExam = false,
                    generatedObjectiveQuestions = objs,
                    generatedSubjectiveQuestions = subjs,
                    step = 3
                )
            }
        }
    }

    // --- Educator Question Editing Operations ---

    fun updateObjectiveQuestion(index: Int, updated: Question) {
        _creationState.update { state ->
            val list = state.generatedObjectiveQuestions.toMutableList()
            if (index in list.indices) {
                list[index] = updated
                state.copy(generatedObjectiveQuestions = list)
            } else state
        }
    }

    fun removeObjectiveQuestion(index: Int) {
        _creationState.update { state ->
            val list = state.generatedObjectiveQuestions.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
                state.copy(generatedObjectiveQuestions = list)
            } else state
        }
    }

    fun addObjectiveQuestion(type: QuestionType = QuestionType.MCQ) {
        _creationState.update { state ->
            val newId = "obj_${System.currentTimeMillis() % 10000}"
            val newQ = when (type) {
                QuestionType.MCQ -> Question(
                    id = newId,
                    type = QuestionType.MCQ,
                    question = "New Multiple Choice Question",
                    options = listOf("Option A", "Option B", "Option C", "Option D"),
                    correctAnswer = "Option A",
                    maxMarks = 2,
                    explanation = "Explanation for the correct choice"
                )
                QuestionType.TRUE_FALSE -> Question(
                    id = newId,
                    type = QuestionType.TRUE_FALSE,
                    question = "New True or False Question statement",
                    options = listOf("True", "False"),
                    correctAnswer = "True",
                    maxMarks = 2,
                    explanation = "Conceptual explanation"
                )
                QuestionType.FILL_BLANK -> Question(
                    id = newId,
                    type = QuestionType.FILL_BLANK,
                    question = "The core technical term for this process is ______.",
                    correctAnswer = state.extractedKeywords.firstOrNull() ?: "Protocol",
                    maxMarks = 2,
                    explanation = "Definition explanation"
                )
                else -> Question(id = newId, type = QuestionType.MCQ, question = "New Question")
            }
            state.copy(generatedObjectiveQuestions = state.generatedObjectiveQuestions + newQ)
        }
    }

    fun updateSubjectiveQuestion(index: Int, updated: Question) {
        _creationState.update { state ->
            val list = state.generatedSubjectiveQuestions.toMutableList()
            if (index in list.indices) {
                list[index] = updated
                state.copy(generatedSubjectiveQuestions = list)
            } else state
        }
    }

    fun removeSubjectiveQuestion(index: Int) {
        _creationState.update { state ->
            val list = state.generatedSubjectiveQuestions.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
                state.copy(generatedSubjectiveQuestions = list)
            } else state
        }
    }

    fun addSubjectiveQuestion() {
        _creationState.update { state ->
            val newId = "subj_${System.currentTimeMillis() % 10000}"
            val keywords = state.extractedKeywords.take(3).ifEmpty { listOf("Architecture", "Optimization") }
            val newSq = Question(
                id = newId,
                type = QuestionType.SUBJECTIVE,
                question = "Explain the fundamental principles and implementation mechanisms of...",
                maxMarks = 10,
                requiredKeywords = keywords,
                idealAnswer = "A comprehensive technical explanation emphasizing ${keywords.joinToString(" and ")}."
            )
            state.copy(generatedSubjectiveQuestions = state.generatedSubjectiveQuestions + newSq)
        }
    }

    fun loadExamForEditing(exam: Exam) {
        _creationState.value = ExamCreationState(
            title = exam.title,
            description = exam.description,
            sourceMaterial = exam.sourceMaterial,
            extractedKeywords = exam.extractedKeywords,
            objectiveCount = exam.objectiveQuestions.size,
            subjectiveCount = exam.subjectiveQuestions.size,
            timeLimitMinutes = exam.timeLimitMinutes,
            googleSheetId = exam.googleSheetId,
            generatedObjectiveQuestions = exam.objectiveQuestions,
            generatedSubjectiveQuestions = exam.subjectiveQuestions,
            isGeneratingExam = false,
            step = 3, // Directly in Review & Edit mode
            editingExamId = exam.id,
            editingExistingCode = exam.accessCode
        )
        _currentScreen.value = AppScreen.EDUCATOR_CREATE_EXAM
    }

    fun finalizeAndPublishExam(): Exam {
        val state = _creationState.value
        val examId = state.editingExamId ?: "exam_${UUID.randomUUID().toString().take(8)}"
        val code = state.editingExistingCode ?: run {
            val randomSuffix = (100..999).random()
            "VIA-$randomSuffix"
        }

        val exam = Exam(
            id = examId,
            title = state.title.ifBlank { "Smart Assessment $code" },
            description = state.description.ifBlank { "AI evaluated quiz powered by VIAAS Evaluator" },
            accessCode = code,
            sourceMaterial = state.sourceMaterial,
            extractedKeywords = state.extractedKeywords,
            objectiveQuestions = state.generatedObjectiveQuestions,
            subjectiveQuestions = state.generatedSubjectiveQuestions,
            timeLimitMinutes = state.timeLimitMinutes,
            perQuestionTargetSeconds = 90,
            googleSheetId = state.googleSheetId,
            isPasswordProtected = state.isPasswordProtected,
            masterPassword = state.masterPassword,
            limitOnePerRollNo = state.limitOnePerRollNo,
            hasNegativeMarking = state.hasNegativeMarking,
            negativeMarkingPenalty = state.negativeMarkingPenalty,
            scoreMultiplier = state.scoreMultiplier,
            retentionDays = state.retentionDays,
            webhookNotificationUrl = state.webhookUrl
        )

        repository.saveExam(exam)
        firestoreSyncService.syncExam(exam)
        _selectedExamForAnalytics.value = exam
        _creationState.value = ExamCreationState() // reset
        _currentScreen.value = AppScreen.EDUCATOR_DASHBOARD
        return exam
    }

    fun updateExamQuestionsAndKeywords(
        objectiveQs: List<Question>,
        subjectiveQs: List<Question>,
        keywords: List<String>
    ) {
        val targetExam = _selectedExamForAnalytics.value ?: repository.exams.value.firstOrNull()
        if (targetExam != null) {
            val updatedExam = targetExam.copy(
                objectiveQuestions = objectiveQs,
                subjectiveQuestions = subjectiveQs,
                extractedKeywords = keywords
            )
            repository.saveExam(updatedExam)
            firestoreSyncService.syncExam(updatedExam)
            _selectedExamForAnalytics.value = updatedExam
        }
    }

    // --- Student Portal Workflows ---

    fun updateStudentOnboarding(name: String, roll: String, dept: String, code: String, passcode: String = "") {
        _studentState.update {
            it.copy(
                studentName = name,
                rollNo = roll,
                department = dept,
                accessCode = code,
                enteredPasscode = passcode,
                errorMessage = null
            )
        }
    }

    fun startStudentExam(): Boolean {
        val s = _studentState.value
        if (s.studentName.isBlank() || s.rollNo.isBlank() || s.accessCode.isBlank()) {
            _studentState.update { it.copy(errorMessage = "Please enter student name, roll number, and exam code.") }
            return false
        }

        val exam = repository.getExamByAccessCode(s.accessCode)
        if (exam == null) {
            _studentState.update { it.copy(errorMessage = "Exam code '${s.accessCode}' not found. Try 'VIA-702' or 'VIA-804'.") }
            return false
        }

        // Pro Access Control: Master Passcode check
        if (exam.isPasswordProtected && exam.masterPassword.isNotBlank()) {
            if (s.enteredPasscode.trim() != exam.masterPassword.trim()) {
                _studentState.update { it.copy(errorMessage = "Master Passcode required for this protected exam. Contact instructor.") }
                return false
            }
        }

        // Pro Access Control: 1 Submission per Roll Number limit
        if (exam.limitOnePerRollNo) {
            val existing = repository.submissions.value.find {
                it.examId == exam.id && it.rollNo.equals(s.rollNo.trim(), ignoreCase = true) && !it.isPartialDraft
            }
            if (existing != null) {
                _studentState.update {
                    it.copy(errorMessage = "Single Submission Limit: Roll number '${s.rollNo}' already completed this assessment (Score: ${existing.totalScore}/${existing.maxScore}).")
                }
                return false
            }
        }

        _studentState.update {
            it.copy(
                currentExam = exam,
                currentQuestionIndex = 0,
                answers = emptyMap(),
                questionTimesSeconds = emptyMap(),
                activeQuestionSeconds = 0L,
                totalElapsedSeconds = 0L,
                isSubmitting = false,
                latestSubmission = null,
                errorMessage = null,
                isSectionTransitionActive = false,
                draftAutoSavedTime = null
            )
        }

        _currentScreen.value = AppScreen.STUDENT_EXAM_RUNNER
        startQuestionTimer()
        return true
    }

    private fun startQuestionTimer() {
        questionTimerJob?.cancel()
        questionTimerJob = viewModelScope.launch {
            var tickCount = 0
            while (true) {
                delay(1000)
                tickCount++
                _studentState.update { current ->
                    val activeSec = current.activeQuestionSeconds + 1
                    val totalSec = current.totalElapsedSeconds + 1
                    current.copy(
                        activeQuestionSeconds = activeSec,
                        totalElapsedSeconds = totalSec
                    )
                }

                // Periodic Partial Draft Auto-Save (Every 15s)
                if (tickCount % 15 == 0) {
                    saveDraftPartialSubmission()
                }
            }
        }
    }

    fun saveDraftPartialSubmission() {
        val state = _studentState.value
        val exam = state.currentExam ?: return
        if (state.studentName.isBlank() || state.rollNo.isBlank() || state.answers.isEmpty()) return

        val currentQ = exam.allQuestions.getOrNull(state.currentQuestionIndex)
        val currentTimes = state.questionTimesSeconds.toMutableMap()
        if (currentQ != null) {
            val prev = currentTimes[currentQ.id] ?: 0L
            currentTimes[currentQ.id] = prev + state.activeQuestionSeconds
        }

        val draft = StudentSubmission(
            id = "draft_${state.rollNo.replace(Regex("[^a-zA-Z0-9]"), "_")}_${exam.id}",
            examId = exam.id,
            examTitle = exam.title,
            studentName = state.studentName,
            rollNo = state.rollNo,
            department = state.department,
            totalScore = 0,
            maxScore = exam.totalMaxMarks,
            percentage = 0.0,
            objectiveScore = 0,
            subjectiveScore = 0,
            totalTimeTakenSeconds = state.totalElapsedSeconds,
            questionTimes = currentTimes,
            answers = state.answers,
            subjectiveEvaluations = emptyList(),
            googleSheetsSyncStatus = "DRAFT_SAVED",
            timestamp = System.currentTimeMillis(),
            isPartialDraft = true
        )
        repository.saveSubmission(draft)
        firestoreSyncService.syncSubmission(draft)
        _studentState.update { it.copy(draftAutoSavedTime = "Draft saved") }
    }

    private fun stopQuestionTimer() {
        questionTimerJob?.cancel()
        questionTimerJob = null
    }

    fun recordAnswer(questionId: String, answer: String) {
        _studentState.update {
            val updated = it.answers.toMutableMap()
            updated[questionId] = answer
            it.copy(answers = updated)
        }
        // Save partial draft on answer entry
        saveDraftPartialSubmission()
    }

    fun navigateToQuestion(newIndex: Int) {
        val current = _studentState.value
        val exam = current.currentExam ?: return
        val allQuestions = exam.allQuestions
        if (newIndex !in allQuestions.indices) return

        val currentQ = allQuestions.getOrNull(current.currentQuestionIndex)
        val updatedTimes = current.questionTimesSeconds.toMutableMap()
        if (currentQ != null) {
            val prevTime = updatedTimes[currentQ.id] ?: 0L
            updatedTimes[currentQ.id] = prevTime + current.activeQuestionSeconds
        }

        // Real-Time Conditional Question Logic & Branching Engine (Tally Pro feature)
        var targetIndex = newIndex
        var routingBanner: String? = null
        var transitionModalTitle = "Section Transition"
        var transitionModalMessage = "You have completed Section A (Objectives) and are now advancing to Section B: Conceptual Subjective Synthesis."
        var triggerModal = false

        if (currentQ != null && exam.branchRules.isNotEmpty()) {
            val currentAnswer = current.answers[currentQ.id]?.trim() ?: ""
            val isCurrentAnswerCorrect = when (currentQ.type) {
                QuestionType.MCQ, QuestionType.TRUE_FALSE -> currentAnswer.equals(currentQ.correctAnswer.trim(), ignoreCase = true)
                QuestionType.FILL_BLANK -> currentAnswer.contains(currentQ.correctAnswer.trim(), ignoreCase = true)
                else -> true
            }

            for (rule in exam.branchRules) {
                if (rule.sourceQuestionId == currentQ.id) {
                    val conditionMet = when (rule.conditionType) {
                        ConditionType.SCORE_LESS_THAN -> !isCurrentAnswerCorrect || currentAnswer.isBlank()
                        ConditionType.EQUALS_ANSWER -> currentAnswer.equals(rule.conditionValue.trim(), ignoreCase = true)
                        ConditionType.CONTAINS_KEYWORD -> currentAnswer.contains(rule.conditionValue.trim(), ignoreCase = true)
                    }

                    if (conditionMet) {
                        when (rule.action) {
                            BranchAction.SHOW_REMEDIAL -> {
                                val remedialIndex = allQuestions.indexOfFirst { it.id == rule.targetId || it.isRemedial }
                                if (remedialIndex >= 0 && remedialIndex != current.currentQuestionIndex) {
                                    targetIndex = remedialIndex
                                    routingBanner = "⚡ Conditional Branch Triggered: Lower score on ${currentQ.id.uppercase()}. Routing to targeted remedial synthesis content."
                                    triggerModal = true
                                    transitionModalTitle = "Remedial Learning Path Activated"
                                    transitionModalMessage = "Based on real-time response evaluation for ${currentQ.id.uppercase()}, adaptive routing has directed you to a guided remedial synthesis question before advancing."
                                }
                            }
                            BranchAction.JUMP_TO_SECTION -> {
                                val sectionIndex = allQuestions.indexOfFirst { it.sectionId == rule.targetId || it.type == QuestionType.SUBJECTIVE }
                                if (sectionIndex >= 0 && sectionIndex != current.currentQuestionIndex) {
                                    targetIndex = sectionIndex
                                    routingBanner = "⚡ Conditional Branch Triggered: Direct jump to ${rule.targetId.uppercase()} based on response."
                                    triggerModal = true
                                    transitionModalTitle = "Adaptive Section Jump"
                                    transitionModalMessage = "Branch rule satisfied: Jumping directly to ${rule.targetId.uppercase()}."
                                }
                            }
                            BranchAction.JUMP_TO_QUESTION -> {
                                val qIndex = allQuestions.indexOfFirst { it.id == rule.targetId }
                                if (qIndex >= 0) {
                                    targetIndex = qIndex
                                    routingBanner = "⚡ Conditional Branch Triggered: Routing to Question ${qIndex + 1}."
                                }
                            }
                        }
                        break
                    }
                }
            }
        }

        // Section transition detection: moving from Section A (Objectives) to Section B (Subjectives)
        val targetQ = allQuestions.getOrNull(targetIndex)
        val isMovingToSubjective = (currentQ?.type != QuestionType.SUBJECTIVE && targetQ?.type == QuestionType.SUBJECTIVE) || triggerModal

        _studentState.update {
            it.copy(
                currentQuestionIndex = targetIndex,
                questionTimesSeconds = updatedTimes,
                activeQuestionSeconds = 0L,
                isSectionTransitionActive = isMovingToSubjective,
                sectionTransitionTitle = transitionModalTitle,
                sectionTransitionMessage = transitionModalMessage,
                conditionalRoutingBanner = routingBanner
            )
        }
        saveDraftPartialSubmission()
    }

    fun dismissSectionTransition() {
        _studentState.update { it.copy(isSectionTransitionActive = false) }
    }

    fun submitExam() {
        val state = _studentState.value
        val exam = state.currentExam ?: return
        stopQuestionTimer()

        val allQuestions = exam.allQuestions
        val currentQ = allQuestions.getOrNull(state.currentQuestionIndex)
        val finalTimes = state.questionTimesSeconds.toMutableMap()
        if (currentQ != null) {
            val prev = finalTimes[currentQ.id] ?: 0L
            finalTimes[currentQ.id] = prev + state.activeQuestionSeconds
        }

        viewModelScope.launch {
            _studentState.update { it.copy(isSubmitting = true) }

            // 1. Evaluate Objective Questions
            var rawObjScore = 0
            var incorrectObjCount = 0
            for (q in exam.objectiveQuestions) {
                val studentAns = state.answers[q.id]?.trim() ?: ""
                val isCorrect = when (q.type) {
                    QuestionType.MCQ, QuestionType.TRUE_FALSE -> {
                        studentAns.equals(q.correctAnswer.trim(), ignoreCase = true)
                    }
                    QuestionType.FILL_BLANK -> {
                        val cleanStudent = studentAns.lowercase().replace(Regex("[^a-z0-9]"), "")
                        val cleanTarget = q.correctAnswer.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
                        cleanStudent == cleanTarget || cleanStudent.contains(cleanTarget) || cleanTarget.contains(cleanStudent)
                    }
                    else -> false
                }
                if (isCorrect) {
                    rawObjScore += q.maxMarks
                } else if (studentAns.isNotBlank()) {
                    incorrectObjCount++
                }
            }

            // Negative marking penalty calculation (Tally Pro feature)
            val penalty = if (exam.hasNegativeMarking) {
                incorrectObjCount * exam.negativeMarkingPenalty
            } else 0.0

            val effectiveObjScore = (rawObjScore.toDouble() - penalty).coerceAtLeast(0.0).toInt()

            // 2. Evaluate Subjective Questions with Gemini AI
            val subjEvaluations = mutableListOf<SubjectiveEvaluation>()
            var subjScore = 0

            for (sq in exam.subjectiveQuestions) {
                val studentAns = state.answers[sq.id]?.trim() ?: ""
                val eval = geminiService.evaluateSubjectiveResponse(
                    questionPrompt = sq.question,
                    idealAnswer = sq.idealAnswer,
                    requiredKeywords = sq.requiredKeywords,
                    maxMarks = sq.maxMarks,
                    studentAnswer = studentAns
                ).copy(questionId = sq.id)

                subjEvaluations.add(eval)
                subjScore += eval.marksAwarded
            }

            // Real-time score multiplier (Tally Pro formula)
            val rawTotal = effectiveObjScore + subjScore
            val multipliedTotal = (rawTotal.toDouble() * exam.scoreMultiplier).toInt()
            val maxScore = (exam.totalMaxMarks.toDouble() * exam.scoreMultiplier).toInt().coerceAtLeast(1)
            val percentage = (multipliedTotal.toDouble() / maxScore.toDouble()) * 100.0

            val submission = StudentSubmission(
                id = "sub_${UUID.randomUUID().toString().take(8)}",
                examId = exam.id,
                examTitle = exam.title,
                studentName = state.studentName,
                rollNo = state.rollNo,
                department = state.department,
                totalScore = multipliedTotal,
                maxScore = maxScore,
                percentage = percentage,
                objectiveScore = effectiveObjScore,
                subjectiveScore = subjScore,
                totalTimeTakenSeconds = state.totalElapsedSeconds,
                questionTimes = finalTimes,
                answers = state.answers,
                subjectiveEvaluations = subjEvaluations,
                googleSheetsSyncStatus = "SYNCED",
                timestamp = System.currentTimeMillis(),
                isPartialDraft = false,
                appliedNegativePenalty = penalty,
                appliedMultiplier = exam.scoreMultiplier
            )

            // Remove any draft and save finalized submission
            repository.saveSubmission(submission)
            firestoreSyncService.syncSubmission(submission)
            sheetsSyncManager.syncToGoogleSheets(
                sheetId = exam.googleSheetId,
                webhookUrl = exam.webhookNotificationUrl.ifBlank { null },
                submission = submission
            )

            _studentState.update {
                it.copy(
                    isSubmitting = false,
                    latestSubmission = submission
                )
            }
            _currentScreen.value = AppScreen.STUDENT_RESULT
        }
    }

    // --- Student Exam History ---

    fun selectHistorySubmission(submission: StudentSubmission) {
        _selectedHistorySubmission.value = submission
        _currentScreen.value = AppScreen.STUDENT_HISTORY_DETAIL
    }

    fun getSubmissionsForStudent(rollNo: String): List<StudentSubmission> {
        val clean = rollNo.trim().lowercase()
        val all = submissions.value
        return if (clean.isBlank()) all else all.filter { sub -> sub.rollNo.trim().lowercase() == clean || sub.rollNo.trim().lowercase().contains(clean) }
    }

    fun getGoogleSheetUrl(sheetId: String): String {
        return sheetsSyncManager.getGoogleSheetUrl(sheetId)
    }

    fun exportCsvForSelectedExam(customSubmissions: List<StudentSubmission>? = null): String {
        val subs = customSubmissions ?: run {
            val exam = _selectedExamForAnalytics.value ?: return ""
            repository.getSubmissionsForExam(exam.id)
        }
        return sheetsSyncManager.exportToCsv(subs)
    }

    fun exportGradedResultsToGoogleSheet(
        exam: Exam,
        submissions: List<StudentSubmission>,
        onProgress: (Int, Int) -> Unit,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            if (submissions.isEmpty()) {
                onComplete(false, "No graded submissions to export.")
                return@launch
            }
            var successCount = 0
            for ((index, sub) in submissions.withIndex()) {
                val success = sheetsSyncManager.syncToGoogleSheets(
                    sheetId = exam.googleSheetId,
                    webhookUrl = exam.webhookNotificationUrl.ifBlank { null },
                    submission = sub
                )
                if (success) successCount++
                onProgress(index + 1, submissions.size)
            }
            onComplete(true, "Successfully exported $successCount evaluation results to Google Sheets.")
        }
    }
}
