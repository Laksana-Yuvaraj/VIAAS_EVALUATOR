package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.Question
import com.example.data.model.QuestionType
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IceBlueBackground
import com.example.ui.theme.IceBlueDark
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg

/**
 * Dedicated teacher screen for defining assessment questions (MCQs, Fill-in-Blanks,
 * True/False, and Subjective prompts) along with their associated grading keywords,
 * point weights, ideal benchmark answers, and keyword matching thresholds.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherAssessmentBuilderScreen(
    currentExam: Exam?,
    onSaveExamQuestions: (List<Question>, List<Question>, List<String>) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBackClick() }

    var examTitle by remember { mutableStateOf(currentExam?.title ?: "New Assessment") }
    var objectiveQuestions by remember { mutableStateOf(currentExam?.objectiveQuestions ?: emptyList()) }
    var subjectiveQuestions by remember { mutableStateOf(currentExam?.subjectiveQuestions ?: emptyList()) }
    var globalKeywords by remember { mutableStateOf(currentExam?.extractedKeywords ?: emptyList()) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Subjective & Grading Keywords, 1: Objective Questions, 2: Global Syllabus Keywords
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var questionTypeToAdd by remember { mutableStateOf(QuestionType.SUBJECTIVE) }

    var editingQuestionIndex by remember { mutableStateOf<Int?>(null) }
    var isEditingObjective by remember { mutableStateOf(false) }

    var keywordInputText by remember { mutableStateOf("") }

    val totalMaxMarks = remember(objectiveQuestions, subjectiveQuestions) {
        objectiveQuestions.sumOf { it.maxMarks } + subjectiveQuestions.sumOf { it.maxMarks }
    }

    val totalGradingKeywords = remember(subjectiveQuestions) {
        subjectiveQuestions.sumOf { it.requiredKeywords.size }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("assessment_builder_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyDark
                        )
                    }
                    Column {
                        Text(
                            text = "Teacher Question & Keyword Studio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Define assessment questions and assign strict grading keywords",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                }

                Button(
                    onClick = {
                        onSaveExamQuestions(objectiveQuestions, subjectiveQuestions, globalKeywords)
                        Toast.makeText(context, "Assessment questions & grading criteria saved!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_all_questions_btn")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Criteria", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Assessment Summary Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assessment_summary_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = examTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Automated Grading Formula: Marks awarded per target keyword detected in student answer",
                                fontSize = 11.sp,
                                color = IceBlueLight,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(IceBluePrimary)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$totalMaxMarks Marks",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricPill(
                            title = "Subjective Qs",
                            value = "${subjectiveQuestions.size}",
                            subtitle = "Keyword graded",
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            title = "Objective Qs",
                            value = "${objectiveQuestions.size}",
                            subtitle = "Auto-checked",
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            title = "Target Keywords",
                            value = "$totalGradingKeywords",
                            subtitle = "In rubrics",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Navigation Tabs (Subjective, Objective, Global Pool)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = NavyPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Slate300, RoundedCornerShape(12.dp)),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NavyPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Subjective & Keywords (${subjectiveQuestions.size})",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Objective Qs (${objectiveQuestions.size})",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Keyword Pool (${globalKeywords.size})",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        // TAB 0: SUBJECTIVE QUESTIONS WITH DETAILED KEYWORD RUBRICS
        if (selectedTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Subjective Evaluation Questions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Define conceptual questions and required keywords students must include",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }

                    Button(
                        onClick = {
                            questionTypeToAdd = QuestionType.SUBJECTIVE
                            showAddQuestionDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_subjective_q_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Subjective Question", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (subjectiveQuestions.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Subjective Questions Defined",
                        message = "Click '+ Subjective Question' above to craft your first open-ended question and specify the critical keywords required for automated AI grading.",
                        actionText = "Create Subjective Question",
                        onAction = {
                            questionTypeToAdd = QuestionType.SUBJECTIVE
                            showAddQuestionDialog = true
                        }
                    )
                }
            } else {
                itemsIndexed(subjectiveQuestions) { index, question ->
                    TeacherSubjectiveQuestionCard(
                        index = index,
                        question = question,
                        onEdit = {
                            editingQuestionIndex = index
                            isEditingObjective = false
                        },
                        onDelete = {
                            subjectiveQuestions = subjectiveQuestions.toMutableList().also { it.removeAt(index) }
                        },
                        onAddKeyword = { newKw ->
                            val updated = question.copy(
                                requiredKeywords = (question.requiredKeywords + newKw.trim()).distinct()
                            )
                            subjectiveQuestions = subjectiveQuestions.toMutableList().also { it[index] = updated }
                        },
                        onRemoveKeyword = { kw ->
                            val updated = question.copy(
                                requiredKeywords = question.requiredKeywords.filter { it != kw }
                            )
                            subjectiveQuestions = subjectiveQuestions.toMutableList().also { it[index] = updated }
                        }
                    )
                }
            }
        }

        // TAB 1: OBJECTIVE QUESTIONS (MCQ, Fill in blank, True/False)
        if (selectedTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Objective Questions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Create Multiple Choice, True/False, and Fill-in-the-Blank items",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SmallAddButton("+ MCQ") {
                            questionTypeToAdd = QuestionType.MCQ
                            showAddQuestionDialog = true
                        }
                        SmallAddButton("+ Fill") {
                            questionTypeToAdd = QuestionType.FILL_BLANK
                            showAddQuestionDialog = true
                        }
                        SmallAddButton("+ T/F") {
                            questionTypeToAdd = QuestionType.TRUE_FALSE
                            showAddQuestionDialog = true
                        }
                    }
                }
            }

            if (objectiveQuestions.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Objective Questions Defined",
                        message = "Add multiple-choice, fill-in-the-blank, or true/false questions to test fundamental recall before subjective synthesis.",
                        actionText = "Add Multiple Choice Question",
                        onAction = {
                            questionTypeToAdd = QuestionType.MCQ
                            showAddQuestionDialog = true
                        }
                    )
                }
            } else {
                itemsIndexed(objectiveQuestions) { index, question ->
                    TeacherObjectiveQuestionCard(
                        index = index,
                        question = question,
                        onEdit = {
                            editingQuestionIndex = index
                            isEditingObjective = true
                        },
                        onDelete = {
                            objectiveQuestions = objectiveQuestions.toMutableList().also { it.removeAt(index) }
                        }
                    )
                }
            }
        }

        // TAB 2: GLOBAL KEYWORD BANK
        if (selectedTab == 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Syllabus Keyword Repository",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                            Text(
                                text = "${globalKeywords.size} Keywords Available",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }

                        Text(
                            text = "Teachers can quickly assign these vetted concepts to any subjective prompt or fill-in-the-blank question.",
                            fontSize = 11.sp,
                            color = Slate600,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Add Keyword Input Field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = keywordInputText,
                                onValueChange = { keywordInputText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_global_keyword_field"),
                                placeholder = { Text("e.g. Distributed Consensus, Paxos, Quorum...", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (keywordInputText.isNotBlank()) {
                                        globalKeywords = (globalKeywords + keywordInputText.trim()).distinct()
                                        keywordInputText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("add_global_keyword_btn")
                            ) {
                                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Keyword chips
                        if (globalKeywords.isEmpty()) {
                            Text(
                                text = "No keywords added yet. Add domain concepts or scan from course materials.",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                globalKeywords.forEach { kw ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(IceBlueLight)
                                            .border(1.dp, IceBluePrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = kw,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = IceBlueDark
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove keyword",
                                                tint = IceBlueDark,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable {
                                                        globalKeywords = globalKeywords.filter { it != kw }
                                                    }
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
    }

    // DIALOG: ADD NEW QUESTION
    if (showAddQuestionDialog) {
        CreateQuestionDialog(
            type = questionTypeToAdd,
            availableKeywords = globalKeywords,
            onDismiss = { showAddQuestionDialog = false },
            onConfirm = { newQ ->
                if (newQ.type == QuestionType.SUBJECTIVE) {
                    subjectiveQuestions = subjectiveQuestions + newQ
                } else {
                    objectiveQuestions = objectiveQuestions + newQ
                }
                showAddQuestionDialog = false
            }
        )
    }

    // DIALOG: EDIT EXISTING QUESTION
    if (editingQuestionIndex != null) {
        val idx = editingQuestionIndex!!
        val currentQuestion = if (isEditingObjective) {
            objectiveQuestions.getOrNull(idx)
        } else {
            subjectiveQuestions.getOrNull(idx)
        }

        if (currentQuestion != null) {
            EditQuestionDetailsDialog(
                question = currentQuestion,
                availableKeywords = globalKeywords,
                onDismiss = { editingQuestionIndex = null },
                onSave = { updatedQ ->
                    if (isEditingObjective) {
                        objectiveQuestions = objectiveQuestions.toMutableList().also { it[idx] = updatedQ }
                    } else {
                        subjectiveQuestions = subjectiveQuestions.toMutableList().also { it[idx] = updatedQ }
                    }
                    editingQuestionIndex = null
                }
            )
        }
    }
}

// -------------------------------------------------------------
// UI SUBCOMPONENTS & CARDS
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeacherSubjectiveQuestionCard(
    index: Int,
    question: Question,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit
) {
    var quickKeywordText by remember { mutableStateOf("") }
    var isAddingKeyword by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("teacher_subjective_card_$index"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyPrimary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "SQ ${index + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IceBlueLight)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${question.maxMarks} Marks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IceBlueDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Subjective Evaluation",
                        fontSize = 11.sp,
                        color = Slate600,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_sq_btn_$index")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Question", tint = NavyPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_sq_btn_$index")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Question", tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Statement
            Text(
                text = question.question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grading Rubric Box: Associated Grading Keywords
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(IceBlueBackground)
                    .border(1.dp, IceBlueLight, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = IceBlueDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Grading Keywords (${question.requiredKeywords.size} required)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IceBlueDark
                            )
                        }

                        TextButton(
                            onClick = { isAddingKeyword = !isAddingKeyword }
                        ) {
                            Text(
                                text = if (isAddingKeyword) "Done" else "+ Add Keyword",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }
                    }

                    // Inline add keyword input
                    AnimatedVisibility(visible = isAddingKeyword) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = quickKeywordText,
                                onValueChange = { quickKeywordText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Keyword to award marks for...", fontSize = 11.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (quickKeywordText.isNotBlank()) {
                                        onAddKeyword(quickKeywordText)
                                        quickKeywordText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (question.requiredKeywords.isEmpty()) {
                        Text(
                            text = "⚠️ No grading keywords linked! Add keywords so the AI engine can score student responses.",
                            fontSize = 11.sp,
                            color = WarningAmber,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            question.requiredKeywords.forEach { kw ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, IceBluePrimary, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = kw,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate900
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove keyword",
                                            tint = Slate600,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clickable { onRemoveKeyword(kw) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (question.idealAnswer.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Model Benchmark Answer:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Text(
                            text = question.idealAnswer,
                            fontSize = 11.sp,
                            color = Slate600,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherObjectiveQuestionCard(
    index: Int,
    question: Question,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("teacher_objective_card_$index"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavySecondary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Q ${index + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate200)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = question.type.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${question.maxMarks} Mark${if (question.maxMarks > 1) "s" else ""}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Question", tint = NavyPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Question", tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Statement
            Text(
                text = question.question,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            // Options display for MCQ
            if (question.type == QuestionType.MCQ && question.options.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    question.options.forEach { opt ->
                        val isCorrect = opt.equals(question.correctAnswer, ignoreCase = true)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCorrect) SuccessGreen.copy(alpha = 0.1f) else Slate100)
                                .border(
                                    1.dp,
                                    if (isCorrect) SuccessGreen.copy(alpha = 0.3f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = if (isCorrect) SuccessGreen else Slate600,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = opt,
                                fontSize = 12.sp,
                                fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCorrect) SuccessGreen else Slate800
                            )
                            if (isCorrect) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Correct Answer", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }
                    }
                }
            }

            // Fill in the blank / True false answer label
            if (question.type != QuestionType.MCQ) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SuccessGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Answer Key: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    Text(question.correctAnswer, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
            }

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Explanation: ${question.explanation}",
                    fontSize = 11.sp,
                    color = Slate600
                )
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOGS: CREATE & EDIT QUESTIONS
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateQuestionDialog(
    type: QuestionType,
    availableKeywords: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (Question) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var marks by remember { mutableIntStateOf(if (type == QuestionType.SUBJECTIVE) 10 else 2) }
    var optionsText by remember { mutableStateOf("Option A\nOption B\nOption C\nOption D") }
    var correctAnswer by remember { mutableStateOf(if (type == QuestionType.TRUE_FALSE) "True" else "") }
    var idealAnswer by remember { mutableStateOf("") }
    var keywordsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var newKeywordInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (prompt.isBlank()) return@Button
                    val id = "q_${System.currentTimeMillis() % 100000}"
                    val options = if (type == QuestionType.MCQ) {
                        optionsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                    } else if (type == QuestionType.TRUE_FALSE) {
                        listOf("True", "False")
                    } else emptyList()

                    val created = Question(
                        id = id,
                        type = type,
                        question = prompt,
                        options = options,
                        correctAnswer = correctAnswer,
                        idealAnswer = idealAnswer,
                        requiredKeywords = keywordsList,
                        maxMarks = marks
                    )
                    onConfirm(created)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Add Question")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Define New ${type.name} Question", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Question Statement / Prompt") },
                        minLines = 2
                    )
                }

                if (type == QuestionType.SUBJECTIVE) {
                    item {
                        Column {
                            Text(
                                text = "Associated Grading Keywords:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                            Text(
                                text = "Answers must include these terms to earn marks",
                                fontSize = 10.sp,
                                color = Slate600
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newKeywordInput,
                                    onValueChange = { newKeywordInput = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("e.g. Encryption", fontSize = 11.sp) },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (newKeywordInput.isNotBlank()) {
                                            keywordsList = (keywordsList + newKeywordInput.trim()).distinct()
                                            newKeywordInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                                ) {
                                    Text("Add", fontSize = 11.sp)
                                }
                            }

                            if (availableKeywords.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Suggested from Syllabus:", fontSize = 10.sp, color = Slate600)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    availableKeywords.take(6).forEach { kw ->
                                        if (!keywordsList.contains(kw)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(IceBlueLight)
                                                    .clickable { keywordsList = keywordsList + kw }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("+ $kw", fontSize = 10.sp, color = IceBlueDark)
                                            }
                                        }
                                    }
                                }
                            }

                            if (keywordsList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    keywordsList.forEach { kw ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(IceBlueLight)
                                                .border(1.dp, IceBluePrimary, RoundedCornerShape(10.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(kw, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IceBlueDark)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clickable { keywordsList = keywordsList.filter { it != kw } }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = idealAnswer,
                            onValueChange = { idealAnswer = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Model Benchmark Answer") },
                            minLines = 3
                        )
                    }
                }

                if (type == QuestionType.MCQ) {
                    item {
                        OutlinedTextField(
                            value = optionsText,
                            onValueChange = { optionsText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Options (One per line)") },
                            minLines = 3
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = correctAnswer,
                            onValueChange = { correctAnswer = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Exact Correct Answer") }
                        )
                    }
                }

                if (type == QuestionType.TRUE_FALSE) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("True", "False").forEach { tf ->
                                val isSelected = correctAnswer == tf
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NavyPrimary else Slate200)
                                        .clickable { correctAnswer = tf }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tf,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Slate700
                                    )
                                }
                            }
                        }
                    }
                }

                if (type == QuestionType.FILL_BLANK) {
                    item {
                        OutlinedTextField(
                            value = correctAnswer,
                            onValueChange = { correctAnswer = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Required Target Term / Word") }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Marks: $marks", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row {
                            SmallAddButton("-") { if (marks > 1) marks-- }
                            Spacer(modifier = Modifier.width(6.dp))
                            SmallAddButton("+") { marks++ }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditQuestionDetailsDialog(
    question: Question,
    availableKeywords: List<String>,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var prompt by remember { mutableStateOf(question.question) }
    var marks by remember { mutableIntStateOf(question.maxMarks) }
    var optionsText by remember { mutableStateOf(question.options.joinToString("\n")) }
    var correctAnswer by remember { mutableStateOf(question.correctAnswer) }
    var idealAnswer by remember { mutableStateOf(question.idealAnswer) }
    var keywordsList by remember { mutableStateOf(question.requiredKeywords) }
    var newKeywordInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val opts = if (question.type == QuestionType.MCQ) {
                        optionsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                    } else question.options

                    onSave(
                        question.copy(
                            question = prompt,
                            options = opts,
                            correctAnswer = correctAnswer,
                            idealAnswer = idealAnswer,
                            requiredKeywords = keywordsList,
                            maxMarks = marks
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Edit Question & Grading Rubric", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Question Prompt") },
                        minLines = 2
                    )
                }

                if (question.type == QuestionType.SUBJECTIVE) {
                    item {
                        Column {
                            Text("Grading Keywords Rubric:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newKeywordInput,
                                    onValueChange = { newKeywordInput = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Add grading keyword...", fontSize = 11.sp) },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (newKeywordInput.isNotBlank()) {
                                            keywordsList = (keywordsList + newKeywordInput.trim()).distinct()
                                            newKeywordInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                                ) {
                                    Text("Add", fontSize = 11.sp)
                                }
                            }

                            if (keywordsList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    keywordsList.forEach { kw ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(IceBlueLight)
                                                .border(1.dp, IceBluePrimary, RoundedCornerShape(10.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(kw, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IceBlueDark)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clickable { keywordsList = keywordsList.filter { it != kw } }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = idealAnswer,
                            onValueChange = { idealAnswer = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Model Benchmark Answer") },
                            minLines = 3
                        )
                    }
                }

                if (question.type == QuestionType.MCQ) {
                    item {
                        OutlinedTextField(
                            value = optionsText,
                            onValueChange = { optionsText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Options (One per line)") },
                            minLines = 3
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = correctAnswer,
                            onValueChange = { correctAnswer = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Exact Correct Option") }
                        )
                    }
                }

                if (question.type == QuestionType.TRUE_FALSE) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("True", "False").forEach { tf ->
                                val isSelected = correctAnswer == tf
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NavyPrimary else Slate200)
                                        .clickable { correctAnswer = tf }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tf,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Slate700
                                    )
                                }
                            }
                        }
                    }
                }

                if (question.type == QuestionType.FILL_BLANK) {
                    item {
                        OutlinedTextField(
                            value = correctAnswer,
                            onValueChange = { correctAnswer = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Target Word / Term") }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Marks: $marks", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row {
                            SmallAddButton("-") { if (marks > 1) marks-- }
                            Spacer(modifier = Modifier.width(6.dp))
                            SmallAddButton("+") { marks++ }
                        }
                    }
                }
            }
        }
    )
}

// -------------------------------------------------------------
// HELPER COMPONENTS
// -------------------------------------------------------------

@Composable
private fun MetricPill(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(title, fontSize = 10.sp, color = IceBlueLight)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = 9.sp, color = IceBlueLight)
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = null,
                tint = IceBluePrimary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                fontSize = 11.sp,
                color = Slate600,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(actionText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SmallAddButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NavyPrimary)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
