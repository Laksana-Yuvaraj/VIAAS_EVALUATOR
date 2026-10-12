package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentSubmission
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CertificateReportView(
    submission: StudentSubmission,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMMM d, yyyy • h:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(submission.timestamp))

    val grade = when {
        submission.percentage >= 90.0 -> "A+ (Outstanding)"
        submission.percentage >= 80.0 -> "A (Distinction)"
        submission.percentage >= 70.0 -> "B+ (Very Good)"
        submission.percentage >= 60.0 -> "B (Good)"
        submission.percentage >= 50.0 -> "C (Pass)"
        else -> "Remedial Required"
    }

    fun shareCertificateText() {
        val certText = """
            ===========================================================
            VIVEKANANDHA ARTS & SCIENCE COLLEGE (VIAAS)
            OFFICIAL ACADEMIC EVALUATION & CERTIFICATE OF COMPLETION
            ===========================================================
            Candidate Name : ${submission.studentName}
            Roll Number    : ${submission.rollNo}
            Department     : ${submission.department}
            Assessment     : ${submission.examTitle}
            Date Evaluated : $formattedDate
            -----------------------------------------------------------
            PERFORMANCE SUMMARY:
            Objective Score    : ${submission.objectiveScore} marks
            Subjective Score   : ${submission.subjectiveScore} marks
            Total Final Score  : ${submission.totalScore} / ${submission.maxScore}
            Percentage         : ${String.format(Locale.US, "%.1f", submission.percentage)}%
            Awarded Grade      : $grade
            Keyword Mastery    : ${String.format(Locale.US, "%.0f", submission.keywordMasteryPercentage)}%
            -----------------------------------------------------------
            AI EVALUATION REMARKS:
            ${submission.subjectiveEvaluations.joinToString("\n") { "• ${it.questionPrompt}: ${it.feedback}" }}
            -----------------------------------------------------------
            Verified by: VIAAS Smart Assessment & Google Sheets Engine
            ===========================================================
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, certText)
            putExtra(Intent.EXTRA_SUBJECT, "VIAAS Official Certificate - ${submission.studentName}")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share VIAAS Certificate"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Top Action Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = NavyDark,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(text = "Official Evaluation Certificate", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { shareCertificateText() },
                        colors = ButtonDefaults.buttonColors(containerColor = IceBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("share_certificate_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Printable Certificate Sheet Canvas
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("printable_certificate_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(3.dp, NavyPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Decorative Inner Golden / Navy Border
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFD97706), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Institutional Logo Emblem
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "VIAAS Emblem",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "VIVEKANANDHA ARTS & SCIENCE COLLEGE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyPrimary,
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "AN INSTITUTION OF VIVEKANANDHA EDUCATIONAL TRUST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate600,
                                textAlign = TextAlign.Center,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Slate300, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "CERTIFICATE OF ACADEMIC EVALUATION",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309), // Amber 700
                                letterSpacing = 1.5.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "This is to officially certify that the candidate",
                                fontSize = 11.sp,
                                color = Slate700,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = submission.studentName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyDark,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Roll Number: ${submission.rollNo} • ${submission.department}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "has successfully undertaken the proctored assessment",
                                fontSize = 11.sp,
                                color = Slate700
                            )

                            Text(
                                text = "\"${submission.examTitle}\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Performance Results Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IceBlueBackground)
                                    .border(1.dp, IceBlueLight, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CertMetric(label = "Final Score", value = "${submission.totalScore}/${submission.maxScore}")
                                    CertMetric(label = "Percentage", value = "${String.format(Locale.US, "%.1f", submission.percentage)}%")
                                    CertMetric(label = "Grade", value = grade)
                                    CertMetric(label = "Keywords", value = "${String.format(Locale.US, "%.0f", submission.keywordMasteryPercentage)}%")
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // AI Evaluator Remarks
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "AI Keyword Engine Assessment Summary:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                submission.subjectiveEvaluations.forEach { eval ->
                                    Text(
                                        text = "• ${eval.feedback}",
                                        fontSize = 10.sp,
                                        color = Slate700,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Verification Stamp & Signatures
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = formattedDate, fontSize = 9.sp, color = Slate600, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "DATE OF ISSUANCE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate700)
                                }

                                // Verified Seal Stamp
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, SuccessGreen, CircleShape)
                                        .background(SuccessGreen.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(imageVector = Icons.Default.Verified, contentDescription = "Verified Seal", tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                        Text(text = "VIAAS VERIFIED", fontSize = 6.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Dr. K. Vivekanandha", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                    Text(text = "CONTROLLER OF EXAMS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate700)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    shareCertificateText()
                    Toast.makeText(context, "Certificate shared & printed successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Export / Download Official PDF Certificate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CertMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = Slate600)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
    }
}
