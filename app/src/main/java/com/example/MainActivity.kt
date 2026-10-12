package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CertificateReportView
import com.example.ui.components.ViaasTopHeader
import com.example.ui.screens.EducatorCreateExamScreen
import com.example.ui.screens.EducatorDashboardScreen
import com.example.ui.screens.PortalSelectorScreen
import com.example.ui.screens.StudentExamRunnerScreen
import com.example.ui.screens.TeacherAssessmentBuilderScreen
import com.example.ui.screens.StudentHistoryScreen
import com.example.ui.screens.StudentOnboardingScreen
import com.example.ui.screens.StudentResultScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate100
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ExamViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ViaasEvaluatorApp()
            }
        }
    }
}

@Composable
fun ViaasEvaluatorApp(
    viewModel: ExamViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val submissions by viewModel.submissions.collectAsState()
    val creationState by viewModel.creationState.collectAsState()
    val studentState by viewModel.studentState.collectAsState()
    val selectedExamForAnalytics by viewModel.selectedExamForAnalytics.collectAsState()
    val selectedHistorySubmission by viewModel.selectedHistorySubmission.collectAsState()
    val selectedCertificateSubmission by viewModel.selectedCertificateSubmission.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isCustomBranding by viewModel.isCustomBranding.collectAsState()

    // Handle back button behavior for sub-screens
    BackHandler(enabled = currentScreen != AppScreen.PORTAL_SELECTOR && currentScreen != AppScreen.STUDENT_EXAM_RUNNER) {
        when (currentScreen) {
            AppScreen.EDUCATOR_CREATE_EXAM -> {
                if (creationState.step > 1) {
                    viewModel.setCreationStep(creationState.step - 1)
                } else {
                    viewModel.navigateTo(AppScreen.EDUCATOR_DASHBOARD)
                }
            }
            AppScreen.EDUCATOR_DASHBOARD -> viewModel.navigateTo(AppScreen.PORTAL_SELECTOR)
            AppScreen.STUDENT_ONBOARDING -> viewModel.navigateTo(AppScreen.PORTAL_SELECTOR)
            AppScreen.STUDENT_HISTORY -> viewModel.navigateTo(AppScreen.STUDENT_ONBOARDING)
            AppScreen.STUDENT_HISTORY_DETAIL -> viewModel.navigateTo(AppScreen.STUDENT_HISTORY)
            AppScreen.STUDENT_RESULT -> viewModel.navigateTo(AppScreen.PORTAL_SELECTOR)
            AppScreen.CERTIFICATE_VIEW -> {
                if (studentState.latestSubmission != null) {
                    viewModel.navigateTo(AppScreen.STUDENT_RESULT)
                } else {
                    viewModel.navigateTo(AppScreen.EDUCATOR_DASHBOARD)
                }
            }
            else -> viewModel.navigateTo(AppScreen.PORTAL_SELECTOR)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentScreen != AppScreen.STUDENT_EXAM_RUNNER) {
                ViaasTopHeader(
                    onHomeClick = { viewModel.navigateTo(AppScreen.PORTAL_SELECTOR) },
                    currentLanguage = appLanguage,
                    onLanguageToggle = { viewModel.toggleLanguage() },
                    isWhiteLabeled = isCustomBranding
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate100),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1024.dp)
            ) {
                when (currentScreen) {
                    AppScreen.PORTAL_SELECTOR -> {
                    PortalSelectorScreen(
                        onSelectEducator = { viewModel.navigateTo(AppScreen.EDUCATOR_DASHBOARD) },
                        onSelectStudent = { viewModel.navigateTo(AppScreen.STUDENT_ONBOARDING) },
                        onOpenQuestionStudio = { viewModel.navigateTo(AppScreen.TEACHER_ASSESSMENT_BUILDER) },
                        totalExams = exams.size,
                        totalSubmissions = submissions.size
                    )
                }

                AppScreen.EDUCATOR_DASHBOARD -> {
                    EducatorDashboardScreen(
                        exams = exams,
                        submissions = submissions,
                        selectedExam = selectedExamForAnalytics,
                        onSelectExam = { viewModel.selectExamForAnalytics(it) },
                        onCreateExamClick = { viewModel.navigateTo(AppScreen.EDUCATOR_CREATE_EXAM) },
                        onEditExamClick = { viewModel.loadExamForEditing(it) },
                        onOpenQuestionStudio = { viewModel.navigateTo(AppScreen.TEACHER_ASSESSMENT_BUILDER) },
                        onBackClick = { viewModel.navigateTo(AppScreen.PORTAL_SELECTOR) },
                        onExportCsv = { list -> viewModel.exportCsvForSelectedExam(list) },
                        onOpenSheetUrl = { sheetId ->
                            val url = viewModel.getGoogleSheetUrl(sheetId)
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback if no browser
                            }
                        },
                        onExportToGoogleSheets = { exam, subs, onProgress, onComplete ->
                            viewModel.exportGradedResultsToGoogleSheet(exam, subs, onProgress, onComplete)
                        },
                        onViewCertificate = { viewModel.viewCertificate(it) },
                        onApplyDataRetention = { days -> viewModel.applyDataRetention(days) }
                    )
                }

                AppScreen.TEACHER_ASSESSMENT_BUILDER -> {
                    TeacherAssessmentBuilderScreen(
                        currentExam = selectedExamForAnalytics ?: exams.firstOrNull(),
                        onSaveExamQuestions = { objQs, subjQs, kws ->
                            viewModel.updateExamQuestionsAndKeywords(objQs, subjQs, kws)
                        },
                        onBackClick = { viewModel.navigateTo(AppScreen.EDUCATOR_DASHBOARD) }
                    )
                }

                AppScreen.EDUCATOR_CREATE_EXAM -> {
                    EducatorCreateExamScreen(
                        state = creationState,
                        onTitleChange = { viewModel.updateCreationTitle(it) },
                        onDescriptionChange = { viewModel.updateCreationDescription(it) },
                        onMaterialChange = { viewModel.updateCreationMaterial(it) },
                        onLoadPreset = { viewModel.loadSampleMaterial(it) },
                        onExtractKeywords = { viewModel.extractKeywords() },
                        onAddKeyword = { viewModel.addExtractedKeyword(it) },
                        onRemoveKeyword = { viewModel.removeExtractedKeyword(it) },
                        onUpdateKeyword = { old, new -> viewModel.updateExtractedKeyword(old, new) },
                        onSettingsChange = { obj, subj, time, sheetId ->
                            viewModel.updateCreationSettings(obj, subj, time, sheetId)
                        },
                        onGenerateQuiz = { viewModel.generateQuizQuestions() },
                        onUpdateObjectiveQuestion = { idx, q -> viewModel.updateObjectiveQuestion(idx, q) },
                        onRemoveObjectiveQuestion = { idx -> viewModel.removeObjectiveQuestion(idx) },
                        onAddObjectiveQuestion = { type -> viewModel.addObjectiveQuestion(type) },
                        onUpdateSubjectiveQuestion = { idx, sq -> viewModel.updateSubjectiveQuestion(idx, sq) },
                        onRemoveSubjectiveQuestion = { idx -> viewModel.removeSubjectiveQuestion(idx) },
                        onAddSubjectiveQuestion = { viewModel.addSubjectiveQuestion() },
                        onPublishExam = { viewModel.finalizeAndPublishExam() },
                        onStepChange = { viewModel.setCreationStep(it) },
                        onBackClick = { viewModel.navigateTo(AppScreen.EDUCATOR_DASHBOARD) },
                        onProSettingsChange = { passProt, pass, limit1, negMark, negPen, mult, ret, webhook ->
                            viewModel.updateProSettings(passProt, pass, limit1, negMark, negPen, mult, ret, webhook)
                        }
                    )
                }

                AppScreen.STUDENT_ONBOARDING -> {
                    StudentOnboardingScreen(
                        state = studentState,
                        onNameChange = { name ->
                            viewModel.updateStudentOnboarding(
                                name,
                                studentState.rollNo,
                                studentState.department,
                                studentState.accessCode,
                                studentState.enteredPasscode
                            )
                        },
                        onRollNoChange = { roll ->
                            viewModel.updateStudentOnboarding(
                                studentState.studentName,
                                roll,
                                studentState.department,
                                studentState.accessCode,
                                studentState.enteredPasscode
                            )
                        },
                        onDeptChange = { dept ->
                            viewModel.updateStudentOnboarding(
                                studentState.studentName,
                                studentState.rollNo,
                                dept,
                                studentState.accessCode,
                                studentState.enteredPasscode
                            )
                        },
                        onCodeChange = { code ->
                            viewModel.updateStudentOnboarding(
                                studentState.studentName,
                                studentState.rollNo,
                                studentState.department,
                                code,
                                studentState.enteredPasscode
                            )
                        },
                        onPasscodeChange = { passcode ->
                            viewModel.updateStudentOnboarding(
                                studentState.studentName,
                                studentState.rollNo,
                                studentState.department,
                                studentState.accessCode,
                                passcode
                            )
                        },
                        onStartExam = { viewModel.startStudentExam() },
                        onViewHistoryClick = { viewModel.navigateTo(AppScreen.STUDENT_HISTORY) },
                        onBackClick = { viewModel.navigateTo(AppScreen.PORTAL_SELECTOR) }
                    )
                }

                AppScreen.STUDENT_HISTORY -> {
                    StudentHistoryScreen(
                        submissions = submissions,
                        initialRollNo = studentState.rollNo,
                        onSelectSubmission = { viewModel.selectHistorySubmission(it) },
                        onBackClick = { viewModel.navigateTo(AppScreen.STUDENT_ONBOARDING) }
                    )
                }

                AppScreen.STUDENT_HISTORY_DETAIL -> {
                    val submission = selectedHistorySubmission
                    if (submission != null) {
                        StudentResultScreen(
                            submission = submission,
                            onHomeClick = { viewModel.navigateTo(AppScreen.STUDENT_HISTORY) },
                            onViewLiveDashboard = {
                                val exam = viewModel.exams.value.find { it.id == submission.examId }
                                if (exam != null) {
                                    viewModel.selectExamForAnalytics(exam)
                                }
                                viewModel.navigateTo(AppScreen.EDUCATOR_DASHBOARD)
                            },
                            onViewCertificate = { viewModel.viewCertificate(submission) }
                        )
                    } else {
                        viewModel.navigateTo(AppScreen.STUDENT_HISTORY)
                    }
                }

                AppScreen.STUDENT_EXAM_RUNNER -> {
                    StudentExamRunnerScreen(
                        state = studentState,
                        onAnswerChanged = { qId, ans -> viewModel.recordAnswer(qId, ans) },
                        onNavigateQuestion = { index -> viewModel.navigateToQuestion(index) },
                        onSubmitExam = { viewModel.submitExam() },
                        onCancelExit = { viewModel.navigateTo(AppScreen.PORTAL_SELECTOR) },
                        onDismissSectionTransition = { viewModel.dismissSectionTransition() }
                    )
                }

                AppScreen.STUDENT_RESULT -> {
                    val submission = studentState.latestSubmission
                    if (submission != null) {
                        StudentResultScreen(
                            submission = submission,
                            onHomeClick = { viewModel.navigateTo(AppScreen.PORTAL_SELECTOR) },
                            onViewLiveDashboard = {
                                val exam = viewModel.exams.value.find { it.id == submission.examId }
                                if (exam != null) {
                                    viewModel.selectExamForAnalytics(exam)
                                }
                                viewModel.navigateTo(AppScreen.EDUCATOR_DASHBOARD)
                            },
                            onViewCertificate = { viewModel.viewCertificate(submission) }
                        )
                    } else {
                        viewModel.navigateTo(AppScreen.PORTAL_SELECTOR)
                    }
                }

                AppScreen.CERTIFICATE_VIEW -> {
                    val certSubmission = selectedCertificateSubmission ?: studentState.latestSubmission
                    if (certSubmission != null) {
                        CertificateReportView(
                            submission = certSubmission,
                            onBackClick = {
                                if (studentState.latestSubmission != null) {
                                    viewModel.navigateTo(AppScreen.STUDENT_RESULT)
                                } else {
                                    viewModel.navigateTo(AppScreen.EDUCATOR_DASHBOARD)
                                }
                            }
                        )
                    } else {
                        viewModel.navigateTo(AppScreen.PORTAL_SELECTOR)
                    }
                }

                AppScreen.GOOGLE_SHEETS_VIEWER -> {
                    viewModel.navigateTo(AppScreen.EDUCATOR_DASHBOARD)
                }
            }
        }
    }
}
}
