package com.example.data.model

enum class QuestionType {
    MCQ,
    TRUE_FALSE,
    FILL_BLANK,
    SUBJECTIVE
}

enum class ConditionType {
    EQUALS_ANSWER,
    CONTAINS_KEYWORD,
    SCORE_LESS_THAN
}

enum class BranchAction {
    JUMP_TO_QUESTION,
    JUMP_TO_SECTION,
    SHOW_REMEDIAL
}

data class BranchRule(
    val id: String = "rule_1",
    val sourceQuestionId: String,
    val conditionType: ConditionType,
    val conditionValue: String,
    val targetId: String,
    val action: BranchAction
)

data class ExamSection(
    val id: String,
    val title: String,
    val description: String = ""
)

data class Question(
    val id: String,
    val type: QuestionType,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val explanation: String = "",
    val maxMarks: Int = if (type == QuestionType.SUBJECTIVE) 10 else 2,
    val requiredKeywords: List<String> = emptyList(),
    val idealAnswer: String = "",
    val sectionId: String = "sec_a",
    val isRemedial: Boolean = false
)

data class Exam(
    val id: String,
    val title: String,
    val description: String = "",
    val accessCode: String,
    val sourceMaterial: String = "",
    val extractedKeywords: List<String> = emptyList(),
    val objectiveQuestions: List<Question> = emptyList(),
    val subjectiveQuestions: List<Question> = emptyList(),
    val timeLimitMinutes: Int = 15,
    val perQuestionTargetSeconds: Int = 90,
    val googleSheetId: String = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms",
    val createdAt: Long = System.currentTimeMillis(),
    
    // Tally Pro & Enterprise Features
    val sections: List<ExamSection> = listOf(
        ExamSection("sec_a", "Section A: Core Technical Objectives", "Fundamental knowledge questions and key concepts"),
        ExamSection("sec_b", "Section B: Conceptual Subjective Synthesis", "In-depth explanations evaluated by AI Keyword Engine")
    ),
    val branchRules: List<BranchRule> = emptyList(),
    val isPasswordProtected: Boolean = false,
    val masterPassword: String = "",
    val limitOnePerRollNo: Boolean = true,
    val hasNegativeMarking: Boolean = false,
    val negativeMarkingPenalty: Double = 0.5,
    val scoreMultiplier: Double = 1.0,
    val isAutoCloseScheduled: Boolean = false,
    val autoCloseMinutesFromNow: Int = 120,
    val retentionDays: Int = 90,
    val webhookNotificationUrl: String = "",
    val institutionName: String = "Vivekanandha Arts & Science College (VIAAS)",
    val themePalette: String = "VIAAS_NAVY" // VIAAS_NAVY, EMERALD_ACADEMIC, ROYAL_PURPLE, SLATE_ENTERPRISE
) {
    val totalQuestions: Int get() = objectiveQuestions.size + subjectiveQuestions.size
    val totalMaxMarks: Int get() = (objectiveQuestions.sumOf { it.maxMarks } + subjectiveQuestions.sumOf { it.maxMarks })
    val allQuestions: List<Question> get() = objectiveQuestions + subjectiveQuestions
}

data class SubjectiveEvaluation(
    val questionId: String,
    val questionPrompt: String,
    val studentAnswer: String,
    val marksAwarded: Int,
    val maxMarks: Int,
    val matchedKeywords: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val feedback: String = ""
) {
    val accuracyPercentage: Double
        get() = if (maxMarks > 0) (marksAwarded.toDouble() / maxMarks.toDouble()) * 100.0 else 0.0
}

data class StudentSubmission(
    val id: String,
    val examId: String,
    val examTitle: String,
    val studentName: String,
    val rollNo: String,
    val department: String = "Computer Science & Engineering",
    val totalScore: Int,
    val maxScore: Int,
    val percentage: Double,
    val objectiveScore: Int,
    val subjectiveScore: Int,
    val totalTimeTakenSeconds: Long,
    val questionTimes: Map<String, Long> = emptyMap(), // questionId -> seconds
    val answers: Map<String, String> = emptyMap(),
    val subjectiveEvaluations: List<SubjectiveEvaluation> = emptyList(),
    val googleSheetsSyncStatus: String = "SYNCED", // SYNCED, PENDING, FAILED
    val timestamp: Long = System.currentTimeMillis(),
    
    // Pro & Enterprise fields
    val isPartialDraft: Boolean = false,
    val emailNotificationSent: Boolean = false,
    val appliedNegativePenalty: Double = 0.0,
    val appliedMultiplier: Double = 1.0
) {
    val keywordMasteryPercentage: Double
        get() {
            var totalRequired = 0
            var totalMatched = 0
            subjectiveEvaluations.forEach {
                totalRequired += (it.matchedKeywords.size + it.missingKeywords.size)
                totalMatched += it.matchedKeywords.size
            }
            return if (totalRequired > 0) (totalMatched.toDouble() / totalRequired.toDouble()) * 100.0 else 100.0
        }

    fun getTimeBreakdownJson(): String {
        val pairs = questionTimes.entries.joinToString(separator = ", ") { "\"${it.key}\": \"${it.value}s\"" }
        return "{ $pairs }"
    }
}

data class GoogleSheetRow(
    val timestamp: String,
    val studentName: String,
    val rollNo: String,
    val totalScore: String,
    val percentage: String,
    val objectiveScore: String,
    val subjectiveScore: String,
    val totalTimeTakenMins: String,
    val timeSpentPerQuestionJson: String,
    val detailedFeedbackAndMissingKeywords: String
)
