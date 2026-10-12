package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.Question
import com.example.data.model.QuestionType
import com.example.ui.components.KeywordChip
import com.example.ui.components.KeywordStatus
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IceBlueBackground
import com.example.ui.theme.IceBlueDark
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.ExamCreationState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EducatorCreateExamScreen(
    state: ExamCreationState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onLoadPreset: (String) -> Unit,
    onExtractKeywords: () -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onUpdateKeyword: (String, String) -> Unit,
    onSettingsChange: (Int, Int, Int, String) -> Unit,
    onGenerateQuiz: () -> Unit,
    onUpdateObjectiveQuestion: (Int, Question) -> Unit,
    onRemoveObjectiveQuestion: (Int) -> Unit,
    onAddObjectiveQuestion: (QuestionType) -> Unit,
    onUpdateSubjectiveQuestion: (Int, Question) -> Unit,
    onRemoveSubjectiveQuestion: (Int) -> Unit,
    onAddSubjectiveQuestion: () -> Unit,
    onPublishExam: () -> Exam,
    onStepChange: (Int) -> Unit,
    onBackClick: () -> Unit,
    onProSettingsChange: (Boolean, String, Boolean, Boolean, Double, Double, Int, String) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    var newKeywordInput by remember { mutableStateOf("") }
    var keywordToEdit by remember { mutableStateOf<String?>(null) }
    var editedKeywordText by remember { mutableStateOf("") }

    // Dialog state for editing a question
    var editingObjectiveIndex by remember { mutableStateOf<Int?>(null) }
    var editingSubjectiveIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Nav
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("create_exam_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NavyDark
                    )
                }
                Column {
                    Text(
                        text = if (state.editingExamId != null) "Edit Assessment (${state.editingExistingCode})" else "Create Assessment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "AI Keyword Extraction & Question Review and Edit Suite",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }
            }
        }

        // Stepper Progress Indicators
        item {
            StepperHeader(
                currentStep = state.step,
                onStepSelect = { onStepChange(it) }
            )
        }

        // STEP 1: Material Input & Keyword Extraction
        if (state.step == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1. Educational Source Material",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Text(
                            text = "Paste curriculum text or choose an academic preset below:",
                            fontSize = 11.sp,
                            color = Slate600,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )

                        // Sample preset quick buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetChip("Database Systems", onClick = { onLoadPreset("Database Systems") })
                            PresetChip("Operating Systems", onClick = { onLoadPreset("Operating Systems") })
                            PresetChip("Cloud Microservices", onClick = { onLoadPreset("Cloud Microservices") })
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.title,
                            onValueChange = onTitleChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("exam_title_field"),
                            label = { Text("Assessment Title") },
                            placeholder = { Text("e.g. Distributed Database Management Midterm") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.sourceMaterial,
                            onValueChange = onMaterialChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("source_material_field"),
                            label = { Text("Source Material / Syllabus Notes") },
                            placeholder = { Text("Paste chapter notes, lecture text, or syllabus concepts...") },
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onExtractKeywords,
                            enabled = state.sourceMaterial.isNotBlank() && !state.isExtractingKeywords,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("extract_keywords_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (state.isExtractingKeywords) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scanning & Extracting Core Keywords...")
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Extract Core Concepts & Keywords with Gemini", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Extracted Keywords Review and Edit Section
            if (state.extractedKeywords.isNotEmpty() || state.isExtractingKeywords) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = IceBluePrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Review & Edit Extracted Keywords",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(IceBlueLight)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${state.extractedKeywords.size} Keywords",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IceBlueDark
                                    )
                                }
                            }
                            Text(
                                text = "Tap any keyword chip to edit its text, tap (x) to remove, or use the input below to add custom keywords.",
                                fontSize = 11.sp,
                                color = Slate600,
                                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                            )

                            // Keyword chips flow with tap to edit & remove
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                state.extractedKeywords.forEach { kw ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(IceBlueLight)
                                            .border(1.dp, IceBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                            .clickable {
                                                keywordToEdit = kw
                                                editedKeywordText = kw
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = kw,
                                                color = IceBlueDark,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit keyword",
                                                tint = IceBluePrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove keyword",
                                                tint = IceBlueDark,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { onRemoveKeyword(kw) }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Add custom keyword field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newKeywordInput,
                                    onValueChange = { newKeywordInput = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("add_custom_keyword_field"),
                                    placeholder = { Text("Add custom target keyword...", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (newKeywordInput.isNotBlank()) {
                                            onAddKeyword(newKeywordInput)
                                            newKeywordInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IceBluePrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onStepChange(2) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Confirm Keywords & Configure Settings ->", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // STEP 2: Exam Settings
        if (state.step == 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "2. Exam Settings",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Text(
                            text = "Set question counts, time limit, and linked Google Sheet ID:",
                            fontSize = 11.sp,
                            color = Slate600,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // Objective Count
                        Text(
                            text = "Objective Questions: ${state.objectiveCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                        Slider(
                            value = state.objectiveCount.toFloat(),
                            onValueChange = {
                                onSettingsChange(it.toInt(), state.subjectiveCount, state.timeLimitMinutes, state.googleSheetId)
                            },
                            valueRange = 1f..8f,
                            steps = 6,
                            colors = SliderDefaults.colors(thumbColor = NavyPrimary, activeTrackColor = NavyPrimary)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subjective Count
                        Text(
                            text = "Subjective Questions: ${state.subjectiveCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                        Slider(
                            value = state.subjectiveCount.toFloat(),
                            onValueChange = {
                                onSettingsChange(state.objectiveCount, it.toInt(), state.timeLimitMinutes, state.googleSheetId)
                            },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(thumbColor = IceBluePrimary, activeTrackColor = IceBluePrimary)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Time Limit
                        Text(
                            text = "Total Exam Time Limit: ${state.timeLimitMinutes} minutes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                        Slider(
                            value = state.timeLimitMinutes.toFloat(),
                            onValueChange = {
                                onSettingsChange(state.objectiveCount, state.subjectiveCount, it.toInt(), state.googleSheetId)
                            },
                            valueRange = 5f..60f,
                            steps = 10,
                            colors = SliderDefaults.colors(thumbColor = Slate800, activeTrackColor = Slate800)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Google Sheet ID
                        OutlinedTextField(
                            value = state.googleSheetId,
                            onValueChange = {
                                onSettingsChange(state.objectiveCount, state.subjectiveCount, state.timeLimitMinutes, it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_sheet_id_input"),
                            label = { Text("Target Google Sheet ID") },
                            placeholder = { Text("e.g. 1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = SuccessGreen)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tally Pro Flow & Security Controls Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate100),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IceBluePrimary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tally Pro Flow & Access Controls",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Master Passcode Protection
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Password Protection", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                        Text("Require passcode to attempt exam", fontSize = 10.sp, color = Slate600)
                                    }
                                    Switch(
                                        checked = state.isPasswordProtected,
                                        onCheckedChange = { checked ->
                                            onProSettingsChange(
                                                checked, state.masterPassword, state.limitOnePerRollNo,
                                                state.hasNegativeMarking, state.negativeMarkingPenalty,
                                                state.scoreMultiplier, state.retentionDays, state.webhookUrl
                                            )
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                                    )
                                }

                                if (state.isPasswordProtected) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = state.masterPassword,
                                        onValueChange = { pass ->
                                            onProSettingsChange(
                                                state.isPasswordProtected, pass, state.limitOnePerRollNo,
                                                state.hasNegativeMarking, state.negativeMarkingPenalty,
                                                state.scoreMultiplier, state.retentionDays, state.webhookUrl
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Master Passcode", fontSize = 11.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                                // 1 Submission per Roll Number
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Limit 1 Per Roll Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                        Text("Prevent multiple submissions per student", fontSize = 10.sp, color = Slate600)
                                    }
                                    Switch(
                                        checked = state.limitOnePerRollNo,
                                        onCheckedChange = { checked ->
                                            onProSettingsChange(
                                                state.isPasswordProtected, state.masterPassword, checked,
                                                state.hasNegativeMarking, state.negativeMarkingPenalty,
                                                state.scoreMultiplier, state.retentionDays, state.webhookUrl
                                            )
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                                // Negative Marking Rules
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Negative Marking Penalty", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                        Text("Deduct marks for wrong objective answers", fontSize = 10.sp, color = Slate600)
                                    }
                                    Switch(
                                        checked = state.hasNegativeMarking,
                                        onCheckedChange = { checked ->
                                            onProSettingsChange(
                                                state.isPasswordProtected, state.masterPassword, state.limitOnePerRollNo,
                                                checked, state.negativeMarkingPenalty,
                                                state.scoreMultiplier, state.retentionDays, state.webhookUrl
                                            )
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = NavyPrimary)
                                    )
                                }

                                if (state.hasNegativeMarking) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Deduction per wrong MCQ: -${state.negativeMarkingPenalty} marks", fontSize = 11.sp, color = NavyPrimary)
                                    Slider(
                                        value = state.negativeMarkingPenalty.toFloat(),
                                        onValueChange = { penalty ->
                                            onProSettingsChange(
                                                state.isPasswordProtected, state.masterPassword, state.limitOnePerRollNo,
                                                state.hasNegativeMarking, penalty.toDouble(),
                                                state.scoreMultiplier, state.retentionDays, state.webhookUrl
                                            )
                                        },
                                        valueRange = 0.25f..1.5f,
                                        steps = 4
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                                // Data Retention Policy
                                Text("Data Retention Compliance", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                Text("Auto-archive student submission records after period", fontSize = 10.sp, color = Slate600)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(30, 60, 90).forEach { days ->
                                        val isSelected = state.retentionDays == days
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) NavyPrimary else Slate200)
                                                .clickable {
                                                    onProSettingsChange(
                                                        state.isPasswordProtected, state.masterPassword, state.limitOnePerRollNo,
                                                        state.hasNegativeMarking, state.negativeMarkingPenalty,
                                                        state.scoreMultiplier, days, state.webhookUrl
                                                    )
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "$days Days",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else Slate700
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Automated Webhook URL
                                OutlinedTextField(
                                    value = state.webhookUrl,
                                    onValueChange = { url ->
                                        onProSettingsChange(
                                            state.isPasswordProtected, state.masterPassword, state.limitOnePerRollNo,
                                            state.hasNegativeMarking, state.negativeMarkingPenalty,
                                            state.scoreMultiplier, state.retentionDays, url
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Result Notification Webhook / Email URL", fontSize = 11.sp) },
                                    placeholder = { Text("https://script.google.com/macros/s/...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onGenerateQuiz,
                            enabled = !state.isGeneratingExam,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_quiz_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (state.isGeneratingExam) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing Questions with Gemini...")
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Questions with Gemini AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // STEP 3: Review and Edit All Questions
        if (state.step == 3) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "3. Educator Exam Review & Edit",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Inspect and customize every question, answer key, and required keyword before saving.",
                                    fontSize = 11.sp,
                                    color = IceBlueLight
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IceBluePrimary)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                val totalMax = state.generatedObjectiveQuestions.sumOf { it.maxMarks } +
                                        state.generatedSubjectiveQuestions.sumOf { it.maxMarks }
                                Text(
                                    text = "Total: $totalMax Marks",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Objective Section Header & Add Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Objective Questions (${state.generatedObjectiveQuestions.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    // Add question quick menu
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SmallAddButton("+ MCQ") { onAddObjectiveQuestion(QuestionType.MCQ) }
                        SmallAddButton("+ T/F") { onAddObjectiveQuestion(QuestionType.TRUE_FALSE) }
                        SmallAddButton("+ Fill") { onAddObjectiveQuestion(QuestionType.FILL_BLANK) }
                    }
                }
            }

            // Objective Question Cards with Inline Edit & Delete
            itemsIndexed(state.generatedObjectiveQuestions) { idx, q ->
                EditableObjectiveCard(
                    index = idx,
                    question = q,
                    onEditClick = { editingObjectiveIndex = idx },
                    onDeleteClick = { onRemoveObjectiveQuestion(idx) }
                )
            }

            // Subjective Section Header & Add Action
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subjective Questions (${state.generatedSubjectiveQuestions.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    SmallAddButton("+ Subjective Q") { onAddSubjectiveQuestion() }
                }
            }

            // Subjective Question Cards with Inline Edit & Delete
            itemsIndexed(state.generatedSubjectiveQuestions) { idx, sq ->
                EditableSubjectiveCard(
                    index = idx,
                    question = sq,
                    onEditClick = { editingSubjectiveIndex = idx },
                    onDeleteClick = { onRemoveSubjectiveQuestion(idx) }
                )
            }

            // Save / Publish Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val published = onPublishExam()
                        Toast.makeText(
                            context,
                            "Exam Saved! Access Code: ${published.accessCode}",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("publish_exam_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.editingExamId != null) "Save Changes & Update Exam" else "Publish Exam & Generate Access Code",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Dialog for Editing a Keyword
    if (keywordToEdit != null) {
        AlertDialog(
            onDismissRequest = { keywordToEdit = null },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateKeyword(keywordToEdit!!, editedKeywordText)
                        keywordToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save Keyword")
                }
            },
            dismissButton = {
                TextButton(onClick = { keywordToEdit = null }) { Text("Cancel") }
            },
            title = { Text("Edit Core Keyword", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editedKeywordText,
                    onValueChange = { editedKeywordText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Keyword / Concept") },
                    singleLine = true
                )
            }
        )
    }

    // Dialog for Editing Objective Question
    if (editingObjectiveIndex != null) {
        val idx = editingObjectiveIndex!!
        val originalQ = state.generatedObjectiveQuestions.getOrNull(idx)
        if (originalQ != null) {
            EditObjectiveQuestionDialog(
                question = originalQ,
                onDismiss = { editingObjectiveIndex = null },
                onSave = { updated ->
                    onUpdateObjectiveQuestion(idx, updated)
                    editingObjectiveIndex = null
                }
            )
        }
    }

    // Dialog for Editing Subjective Question
    if (editingSubjectiveIndex != null) {
        val idx = editingSubjectiveIndex!!
        val originalSq = state.generatedSubjectiveQuestions.getOrNull(idx)
        if (originalSq != null) {
            EditSubjectiveQuestionDialog(
                question = originalSq,
                onDismiss = { editingSubjectiveIndex = null },
                onSave = { updated ->
                    onUpdateSubjectiveQuestion(idx, updated)
                    editingSubjectiveIndex = null
                }
            )
        }
    }
}

@Composable
private fun SmallAddButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(IceBlueLight)
            .border(1.dp, IceBluePrimary, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IceBlueDark)
    }
}

@Composable
private fun EditableObjectiveCard(
    index: Int,
    question: Question,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(IceBlueLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Q${index + 1} • ${question.type.name} • ${question.maxMarks} Marks",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IceBlueDark
                    )
                }

                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit question", tint = NavyPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete question", tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = question.question,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            if (question.type == QuestionType.MCQ) {
                Spacer(modifier = Modifier.height(6.dp))
                question.options.forEach { opt ->
                    Text(
                        text = "• $opt",
                        fontSize = 11.sp,
                        color = if (opt == question.correctAnswer) SuccessGreen else Slate700,
                        fontWeight = if (opt == question.correctAnswer) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Key: ${question.correctAnswer} (${question.explanation})",
                fontSize = 11.sp,
                color = SuccessGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EditableSubjectiveCard(
    index: Int,
    question: Question,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IceBlueBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IceBlueLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(IceBlueLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SQ${index + 1} • SUBJECTIVE • ${question.maxMarks} Marks",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IceBlueDark
                    )
                }

                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit subjective question", tint = NavyPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete subjective question", tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = question.question,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Target Keywords: ${question.requiredKeywords.joinToString(", ")}",
                fontSize = 11.sp,
                color = IceBlueDark,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ideal Answer: ${question.idealAnswer}",
                fontSize = 10.sp,
                color = Slate700
            )
        }
    }
}

@Composable
private fun EditObjectiveQuestionDialog(
    question: Question,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var prompt by remember { mutableStateOf(question.question) }
    var correctAnswer by remember { mutableStateOf(question.correctAnswer) }
    var explanation by remember { mutableStateOf(question.explanation) }
    var marks by remember { mutableIntStateOf(question.maxMarks) }
    var optionsText by remember { mutableStateOf(question.options.joinToString("\n")) }

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
                            explanation = explanation,
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
            Text(
                text = "Edit Objective Question (${question.type.name})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Question Prompt") }
                    )
                }

                if (question.type == QuestionType.MCQ) {
                    item {
                        OutlinedTextField(
                            value = optionsText,
                            onValueChange = { optionsText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Options (one per line)") }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = correctAnswer,
                        onValueChange = { correctAnswer = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Correct Answer") }
                    )
                }

                item {
                    OutlinedTextField(
                        value = explanation,
                        onValueChange = { explanation = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Explanation") }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Max Marks: $marks", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

@Composable
private fun EditSubjectiveQuestionDialog(
    question: Question,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var prompt by remember { mutableStateOf(question.question) }
    var idealAnswer by remember { mutableStateOf(question.idealAnswer) }
    var keywordsText by remember { mutableStateOf(question.requiredKeywords.joinToString(", ")) }
    var marks by remember { mutableIntStateOf(question.maxMarks) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val kws = keywordsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onSave(
                        question.copy(
                            question = prompt,
                            requiredKeywords = kws,
                            idealAnswer = idealAnswer,
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
            Text(
                text = "Edit Subjective Question",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Subjective Question Prompt") }
                    )
                }

                item {
                    OutlinedTextField(
                        value = keywordsText,
                        onValueChange = { keywordsText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Required Target Keywords (comma separated)") },
                        placeholder = { Text("e.g. Three-way Handshake, Latency, Bandwidth") }
                    )
                }

                item {
                    OutlinedTextField(
                        value = idealAnswer,
                        onValueChange = { idealAnswer = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ideal Answer Benchmark") },
                        minLines = 3
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Max Marks: $marks", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row {
                            SmallAddButton("-") { if (marks > 2) marks -= 2 }
                            Spacer(modifier = Modifier.width(6.dp))
                            SmallAddButton("+") { marks += 2 }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun StepperHeader(
    currentStep: Int,
    onStepSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepPill(step = 1, title = "1. Material", isActive = currentStep == 1, isDone = currentStep > 1, onClick = { onStepSelect(1) })
        HorizontalDivider(modifier = Modifier.width(18.dp), color = Slate300)
        StepPill(step = 2, title = "2. Settings", isActive = currentStep == 2, isDone = currentStep > 2, onClick = { onStepSelect(2) })
        HorizontalDivider(modifier = Modifier.width(18.dp), color = Slate300)
        StepPill(step = 3, title = "3. Review & Edit", isActive = currentStep == 3, isDone = false, onClick = { onStepSelect(3) })
    }
}

@Composable
private fun StepPill(
    step: Int,
    title: String,
    isActive: Boolean,
    isDone: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isDone -> SuccessGreen
        isActive -> NavyPrimary
        else -> Slate200
    }
    val textColor = when {
        isDone || isActive -> Color.White
        else -> Slate700
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun PresetChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(IceBlueLight)
            .border(1.dp, IceBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = IceBlueDark)
    }
}
