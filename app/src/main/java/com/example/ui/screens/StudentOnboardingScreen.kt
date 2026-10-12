package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IceBlueBackground
import com.example.ui.theme.IceBlueDark
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.StudentExamState

@Composable
fun StudentOnboardingScreen(
    state: StudentExamState,
    onNameChange: (String) -> Unit,
    onRollNoChange: (String) -> Unit,
    onDeptChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onPasscodeChange: (String) -> Unit = {},
    onStartExam: () -> Unit,
    onViewHistoryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("student_onboarding_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NavyDark
                )
            }
            Text(
                text = "Student Exam Onboarding",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Google Forms Styled Top Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Top Color Stripe (Ice Blue & Navy accent)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(IceBluePrimary)
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "VIAAS Online Assessment System",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "Complete your candidate profile to initialize your individualized test session and per-question telemetry.",
                        fontSize = 12.sp,
                        color = Slate600,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Error banner
                    if (state.errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = state.errorMessage,
                                color = ErrorRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Form Fields
                    OutlinedTextField(
                        value = state.studentName,
                        onValueChange = onNameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_name_field"),
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Jordan Miller") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = NavyPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.rollNo,
                        onValueChange = onRollNoChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_roll_field"),
                        label = { Text("Roll Number / Student ID *") },
                        placeholder = { Text("e.g. 24-CS-099") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = NavyPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.department,
                        onValueChange = onDeptChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_dept_field"),
                        label = { Text("Department / Major") },
                        placeholder = { Text("e.g. Computer Science") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CorporateFare, contentDescription = null, tint = NavyPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.accessCode,
                        onValueChange = onCodeChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_access_code_field"),
                        label = { Text("6-Digit Exam Access Code *") },
                        placeholder = { Text("e.g. VIA-702") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = IceBluePrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Quick access code suggestion pills
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to load available test codes:",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccessCodeChip(
                            label = "VIA-702 (Networks)",
                            onClick = { onCodeChange("VIA-702") }
                        )
                        AccessCodeChip(
                            label = "VIA-804 (Data Structures)",
                            onClick = { onCodeChange("VIA-804") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.enteredPasscode,
                        onValueChange = onPasscodeChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_passcode_field"),
                        label = { Text("Master Passcode (Optional / If Protected)") },
                        placeholder = { Text("Enter passcode (e.g. VIAAS2026)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = Slate600)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onStartExam,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_exam_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Begin Assessment Session",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onViewHistoryClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("view_exam_history_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Exam History & Past Results",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessCodeChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(IceBlueLight)
            .border(1.dp, IceBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = IceBlueDark
        )
    }
}
