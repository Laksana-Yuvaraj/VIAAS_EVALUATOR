package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Question
import com.example.data.model.QuestionType
import com.example.ui.theme.IceBlueBackground
import com.example.ui.theme.IceBlueDark
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBlueMedium
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.StudentExamState

@Composable
fun StudentExamRunnerScreen(
    state: StudentExamState,
    onAnswerChanged: (String, String) -> Unit,
    onNavigateQuestion: (Int) -> Unit,
    onSubmitExam: () -> Unit,
    onCancelExit: () -> Unit,
    onDismissSectionTransition: () -> Unit = {}
) {
    val exam = state.currentExam ?: return
    val allQuestions = exam.allQuestions
    val currentIndex = state.currentQuestionIndex.coerceIn(0, allQuestions.size - 1)
    val currentQuestion = allQuestions[currentIndex]
    val currentAnswer = state.answers[currentQuestion.id] ?: ""

    // Answer Piping (Tally Pro feature): dynamically reference previous student answers
    val q1Answer = state.answers["q1"] ?: state.answers.values.firstOrNull() ?: ""
    val displayQuestionText = remember(currentQuestion.question, q1Answer) {
        if (q1Answer.isNotBlank()) {
            currentQuestion.question
                .replace("{{q1_answer}}", "\"$q1Answer\"")
                .replace("[Student's Answer to Q1]", "\"$q1Answer\"")
                .replace("[Student's Answer to Question 1]", "\"$q1Answer\"")
        } else {
            currentQuestion.question
                .replace("{{q1_answer}}", "[Your response to Question 1]")
                .replace("[Student's Answer to Q1]", "[Your response to Question 1]")
        }
    }

    var showSubmitConfirmation by remember { mutableStateOf(false) }
    var showExitWarning by remember { mutableStateOf(false) }

    // Intercept hardware/system back button during active exam
    BackHandler {
        showExitWarning = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Sticky Header: Google Forms Brand & Telemetry Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exam.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Candidate: ${state.studentName} (${state.rollNo})",
                            color = IceBlueLight,
                            fontSize = 10.sp
                        )
                    }

                    // Total Elapsed Timer
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavyPrimary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = IceBlueLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val totalMins = state.totalElapsedSeconds / 60
                            val totalSecs = state.totalElapsedSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", totalMins, totalSecs),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Per-Question Active Timer & Question Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${allQuestions.size} (${currentQuestion.maxMarks} marks)",
                        color = IceBlueMedium,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // LIVE PER-QUESTION TIME TRACKER
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(IceBluePrimary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("per_question_timer_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Active Question Timer",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "⏱️ ${state.activeQuestionSeconds}s on this Q",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Question Selector Drawer / Navigation Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            allQuestions.forEachIndexed { idx, q ->
                val isAnswered = !state.answers[q.id].isNullOrBlank()
                val isCurrent = idx == currentIndex
                val pillBg = when {
                    isCurrent -> IceBluePrimary
                    isAnswered -> SuccessGreen
                    else -> Color.White
                }
                val pillText = when {
                    isCurrent || isAnswered -> Color.White
                    else -> Slate700
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(pillBg)
                        .border(1.dp, if (isCurrent) IceBluePrimary else Slate300, CircleShape)
                        .clickable { onNavigateQuestion(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${idx + 1}",
                        color = pillText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Conditional Routing Alert Banner (Tally Pro feature)
        if (state.conditionalRoutingBanner != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF3C7))
                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.conditionalRoutingBanner,
                        fontSize = 11.sp,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Active Question Card (Google Forms Style)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("current_question_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Google Forms top ice-blue border
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(if (currentQuestion.type == QuestionType.SUBJECTIVE) IceBluePrimary else NavyPrimary)
                        )

                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(IceBlueLight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = when (currentQuestion.type) {
                                            QuestionType.MCQ -> "Multiple Choice"
                                            QuestionType.TRUE_FALSE -> "True / False"
                                            QuestionType.FILL_BLANK -> "Fill in the Blank"
                                            QuestionType.SUBJECTIVE -> "Subjective (AI Keyword Evaluated)"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IceBlueDark
                                    )
                                }

                                Text(
                                    text = "${currentQuestion.maxMarks} Marks",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate600
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Answer Piping notice badge if referencing earlier question
                            if (q1Answer.isNotBlank() && currentQuestion.type == QuestionType.SUBJECTIVE) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(IceBlueBackground)
                                        .border(1.dp, IceBlueLight, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "⚡ Answer Piping: Question dynamically references your previous response ($q1Answer)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = IceBlueDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                text = displayQuestionText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Render Input by Question Type
                            when (currentQuestion.type) {
                                QuestionType.MCQ -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        currentQuestion.options.forEach { opt ->
                                            val isSelected = currentAnswer == opt
                                            McqOptionCard(
                                                text = opt,
                                                isSelected = isSelected,
                                                onSelect = { onAnswerChanged(currentQuestion.id, opt) }
                                            )
                                        }
                                    }
                                }

                                QuestionType.TRUE_FALSE -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        listOf("True", "False").forEach { valOpt ->
                                            val isSelected = currentAnswer == valOpt
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) IceBlueLight else Slate100)
                                                    .border(
                                                        2.dp,
                                                        if (isSelected) IceBluePrimary else Slate300,
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { onAnswerChanged(currentQuestion.id, valOpt) }
                                                    .padding(vertical = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = valOpt,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) NavyDark else Slate800
                                                )
                                            }
                                        }
                                    }
                                }

                                QuestionType.FILL_BLANK -> {
                                    OutlinedTextField(
                                        value = currentAnswer,
                                        onValueChange = { onAnswerChanged(currentQuestion.id, it) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("fill_blank_input"),
                                        label = { Text("Your Answer") },
                                        placeholder = { Text("Type missing word or phrase...") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                QuestionType.SUBJECTIVE -> {
                                    Column {
                                        OutlinedTextField(
                                            value = currentAnswer,
                                            onValueChange = { onAnswerChanged(currentQuestion.id, it) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(190.dp)
                                                .testTag("subjective_answer_input"),
                                            placeholder = {
                                                Text("Synthesize your conceptual answer incorporating core domain terms and technical principles...")
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Live Word & Character Counter
                                        val wordCount = currentAnswer.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                                        val charCount = currentAnswer.length

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "AI Keyword Engine checks technical depth & key terms.",
                                                fontSize = 10.sp,
                                                color = Slate600
                                            )
                                            Text(
                                                text = "$wordCount words • $charCount characters",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = NavyPrimary,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Controls: Previous, Next, Submit
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentIndex > 0) onNavigateQuestion(currentIndex - 1)
                    },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("prev_question_btn")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous", fontSize = 12.sp)
                }

                if (currentIndex < allQuestions.size - 1) {
                    Button(
                        onClick = { onNavigateQuestion(currentIndex + 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("next_question_btn")
                    ) {
                        Text("Next", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = { showSubmitConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_exam_open_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Finish & Submit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Submit Confirmation Checklist Dialog
    if (showSubmitConfirmation) {
        val totalQ = allQuestions.size
        val answeredQ = allQuestions.count { !state.answers[it.id].isNullOrBlank() }
        val unansweredQ = totalQ - answeredQ

        AlertDialog(
            onDismissRequest = { if (!state.isSubmitting) showSubmitConfirmation = false },
            confirmButton = {
                Button(
                    onClick = { onSubmitExam() },
                    enabled = !state.isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.testTag("confirm_final_submit_btn")
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grading with Gemini...")
                    } else {
                        Text("Confirm & Evaluate", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!state.isSubmitting) {
                    TextButton(onClick = { showSubmitConfirmation = false }) {
                        Text("Review Questions")
                    }
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Checklist, contentDescription = null, tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pre-Submission Checklist", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Verify your answers before the AI Keyword Evaluation Engine processes your submission and appends the record to Google Sheets:",
                        fontSize = 11.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ChecklistStatusRow(
                        label = "Answered Questions",
                        value = "$answeredQ / $totalQ",
                        isGood = answeredQ == totalQ
                    )
                    ChecklistStatusRow(
                        label = "Unanswered Questions",
                        value = "$unansweredQ",
                        isGood = unansweredQ == 0
                    )
                    ChecklistStatusRow(
                        label = "Total Time Logged",
                        value = "${state.totalElapsedSeconds / 60}m ${state.totalElapsedSeconds % 60}s",
                        isGood = true
                    )
                }
            }
        )
    }

    // Multi-Page & Section Break Transition Modal (Tally Pro feature)
    if (state.isSectionTransitionActive) {
        AlertDialog(
            onDismissRequest = { onDismissSectionTransition() },
            confirmButton = {
                Button(
                    onClick = { onDismissSectionTransition() },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Proceed", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = IceBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(state.sectionTransitionTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = state.sectionTransitionMessage,
                        fontSize = 13.sp,
                        color = Slate800,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(IceBlueBackground)
                            .border(1.dp, IceBlueLight, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 Real-time conditional logic evaluates responses instantly to provide optimal pacing and remedial assistance.",
                            fontSize = 11.sp,
                            color = IceBlueDark
                        )
                    }
                }
            }
        )
    }

    if (showExitWarning) {
        AlertDialog(
            onDismissRequest = { showExitWarning = false },
            confirmButton = {
                Button(
                    onClick = {
                        showExitWarning = false
                        onCancelExit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Abandon Exam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitWarning = false }) {
                    Text("Continue Test")
                }
            },
            title = { Text("Exit Assessment?") },
            text = { Text("Exiting now will discard your current progress and timer telemetry.") }
        )
    }
}

@Composable
private fun McqOptionCard(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) IceBlueLight else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) IceBluePrimary else Slate200
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = IceBluePrimary,
                    unselectedColor = Slate400
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) NavyDark else Slate900
            )
        }
    }
}

@Composable
private fun ChecklistStatusRow(label: String, value: String, isGood: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = Slate700)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isGood) SuccessGreen else WarningAmber
        )
    }
}
