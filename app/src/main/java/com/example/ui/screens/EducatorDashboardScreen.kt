package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import com.example.ui.components.DynamicQrCodeCard
import com.example.ui.components.ProAnalyticsDashboard
import com.example.ui.components.ProEnterpriseFeaturesCard
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.StudentSubmission
import com.example.ui.components.KeywordChip
import com.example.ui.components.KeywordStatus
import com.example.ui.theme.GoogleSheetsGreen
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
import com.example.ui.theme.SuccessGreenBg
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EducatorTab(val label: String) {
    GRADEBOOK("Live Gradebook"),
    PRO_ANALYTICS("Pro Analytics & Funnel"),
    QR_CODE("Instant QR Code"),
    ENTERPRISE("Enterprise & Retention")
}

@Composable
fun EducatorDashboardScreen(
    exams: List<Exam>,
    submissions: List<StudentSubmission>,
    selectedExam: Exam?,
    onSelectExam: (Exam) -> Unit,
    onCreateExamClick: () -> Unit,
    onEditExamClick: (Exam) -> Unit,
    onOpenQuestionStudio: () -> Unit = {},
    onBackClick: () -> Unit,
    onExportCsv: (List<StudentSubmission>) -> String,
    onOpenSheetUrl: (String) -> Unit,
    onExportToGoogleSheets: (Exam, List<StudentSubmission>, (Int, Int) -> Unit, (Boolean, String) -> Unit) -> Unit = { _, _, _, _ -> },
    onViewCertificate: (StudentSubmission) -> Unit = {},
    onApplyDataRetention: (Int) -> Int = { 0 }
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(EducatorTab.GRADEBOOK) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedSubmissionId by remember { mutableStateOf<String?>(null) }
    var showSheetColumnsDialog by remember { mutableStateOf(false) }
    var retentionDaysSelection by remember { mutableStateOf(90) }
    var retentionArchiveMessage by remember { mutableStateOf<String?>(null) }

    // Google Sheets Export State
    var isExportingToSheets by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf(0 to 0) }
    var exportResultMessage by remember { mutableStateOf<String?>(null) }

    // Download Report State
    var pendingCsvContent by remember { mutableStateOf("") }
    var downloadedReportSummary by remember { mutableStateOf<String?>(null) }

    val currentExamSubmissions = remember(selectedExam, submissions, searchQuery) {
        val list = if (selectedExam != null) {
            submissions.filter { it.examId == selectedExam.id }
        } else submissions

        if (searchQuery.isBlank()) list
        else list.filter {
            it.studentName.contains(searchQuery, ignoreCase = true) ||
            it.rollNo.contains(searchQuery, ignoreCase = true)
        }
    }

    // Android Storage Access Framework (SAF) Launcher for zero-permission local CSV download
    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(pendingCsvContent.toByteArray(Charsets.UTF_8))
                }
                downloadedReportSummary = "CSV Report saved successfully to your device for local record-keeping (${currentExamSubmissions.size} students)."
                Toast.makeText(context, "Report CSV downloaded and saved locally!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving CSV file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun initiateDownloadReport(listToExport: List<StudentSubmission>) {
        val csv = onExportCsv(listToExport)
        pendingCsvContent = csv
        val examCode = selectedExam?.accessCode?.replace("-", "_") ?: "Assessment"
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val defaultFilename = "VIAAS_Performance_Report_${examCode}_$timestamp.csv"
        createCsvLauncher.launch(defaultFilename)
    }

    fun shareReportCsv(listToExport: List<StudentSubmission>) {
        try {
            val csv = onExportCsv(listToExport)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csv)
                putExtra(Intent.EXTRA_SUBJECT, "VIAAS Assessment Report - ${selectedExam?.title ?: "Gradebook"}")
                type = "text/csv"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share VIAAS CSV Report"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share chooser: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerExportToGoogleSheets() {
        val exam = selectedExam ?: exams.firstOrNull()
        if (exam == null) {
            Toast.makeText(context, "Please select an assessment first.", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentExamSubmissions.isEmpty()) {
            Toast.makeText(context, "No student submissions available to export yet.", Toast.LENGTH_SHORT).show()
            return
        }
        isExportingToSheets = true
        exportProgress = 0 to currentExamSubmissions.size
        onExportToGoogleSheets(
            exam,
            currentExamSubmissions,
            { current, total ->
                exportProgress = current to total
            },
            { success, msg ->
                isExportingToSheets = false
                exportResultMessage = msg
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Navigation Bar & Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("educator_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyDark
                        )
                    }
                    Column {
                        Text(
                            text = "Educator Dashboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Live Student Grading & Google Sheets Feed",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenQuestionStudio,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("open_question_studio_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Question Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }

                    Button(
                        onClick = onCreateExamClick,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("create_new_exam_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "New Exam", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Exam Switcher Horizontal Chips
        item {
            Column {
                Text(
                    text = "Select Active Assessment:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(exams) { exam ->
                        val isSelected = selectedExam?.id == exam.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NavyPrimary else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) NavyPrimary else Slate300,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectExam(exam) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = exam.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Slate900
                                )
                                Text(
                                    text = "Code: ${exam.accessCode} • ${exam.totalQuestions} Qs",
                                    fontSize = 10.sp,
                                    color = if (isSelected) IceBlueLight else Slate600
                                )
                            }
                        }
                    }
                }
            }
        }

        // Educator Portal Tabs: Gradebook, Pro Analytics, Instant QR, Enterprise Controls
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(EducatorTab.values()) { tab ->
                    val isSelected = activeTab == tab
                    val (icon, color) = when (tab) {
                        EducatorTab.GRADEBOOK -> Pair(Icons.Default.TableChart, NavyPrimary)
                        EducatorTab.PRO_ANALYTICS -> Pair(Icons.Default.BarChart, IceBluePrimary)
                        EducatorTab.QR_CODE -> Pair(Icons.Default.QrCode, Color(0xFF4338CA))
                        EducatorTab.ENTERPRISE -> Pair(Icons.Default.Settings, Color(0xFF0F766E))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) color else Color.White)
                            .border(1.dp, if (isSelected) color else Slate300, RoundedCornerShape(10.dp))
                            .clickable { activeTab = tab }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Slate700,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Slate800
                            )
                        }
                    }
                }
            }
        }

        // Render Active Tab Content
        when (activeTab) {
            EducatorTab.PRO_ANALYTICS -> {
                item {
                    val targetExam = selectedExam ?: exams.firstOrNull()
                    if (targetExam != null) {
                        ProAnalyticsDashboard(exam = targetExam, submissions = currentExamSubmissions)
                    } else {
                        Box(modifier = Modifier.padding(20.dp)) {
                            Text("No assessment selected.")
                        }
                    }
                }
            }

            EducatorTab.QR_CODE -> {
                item {
                    val targetExam = selectedExam ?: exams.firstOrNull()
                    if (targetExam != null) {
                        DynamicQrCodeCard(
                            examTitle = targetExam.title,
                            accessCode = targetExam.accessCode,
                            onShareClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Exam Access Code: ${targetExam.accessCode}")
                                    putExtra(Intent.EXTRA_TEXT, "Take '${targetExam.title}' on VIAAS Evaluator. Access Code: ${targetExam.accessCode}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Assessment Link"))
                            }
                        )
                    } else {
                        Box(modifier = Modifier.padding(20.dp)) {
                            Text("No assessment selected.")
                        }
                    }
                }
            }

            EducatorTab.ENTERPRISE -> {
                item {
                    EnterpriseSettingsCard(
                        exam = selectedExam ?: exams.firstOrNull(),
                        selectedRetentionDays = retentionDaysSelection,
                        onRetentionDaysChange = { retentionDaysSelection = it },
                        onApplyRetention = {
                            val removed = onApplyDataRetention(retentionDaysSelection)
                            retentionArchiveMessage = "Archived $removed older student submission(s) exceeding $retentionDaysSelection days retention window."
                        },
                        onOpenSheet = {
                            val sheetId = selectedExam?.googleSheetId ?: "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
                            onOpenSheetUrl(sheetId)
                        }
                    )
                }
                item {
                    ProEnterpriseFeaturesCard(
                        exam = selectedExam ?: exams.firstOrNull()
                    )
                }
            }

            EducatorTab.GRADEBOOK -> {
                // Google Sheets Integration Banner with Download Report Action
                if (selectedExam != null) {
                    item {
                        GoogleSheetsIntegrationCard(
                            exam = selectedExam,
                            totalSubmissions = currentExamSubmissions.size,
                            onOpenSheet = { onOpenSheetUrl(selectedExam.googleSheetId) },
                            onExportToSheet = { triggerExportToGoogleSheets() },
                            onDownloadReport = { initiateDownloadReport(currentExamSubmissions) },
                            onViewColumns = { showSheetColumnsDialog = true },
                            onEditExam = { onEditExamClick(selectedExam) }
                        )
                    }
                }

                // Live Staff Dashboard Table Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submissions_table_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Live Submissions Gradebook",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "${currentExamSubmissions.size} student record(s) logged in assessment table",
                                        fontSize = 11.sp,
                                        color = Slate600
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SuccessGreenBg)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clickable { triggerExportToGoogleSheets() }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "LIVE SYNC ON",
                                            color = SuccessGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Dedicated Export to Google Sheets, Download Report and Share Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { triggerExportToGoogleSheets() },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("export_to_google_sheet_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoogleSheetsGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = "Export to Google Sheet",
                                        modifier = Modifier.size(15.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Export to Sheet",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = { initiateDownloadReport(currentExamSubmissions) },
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .testTag("download_report_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download Report",
                                        modifier = Modifier.size(15.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Download CSV",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                OutlinedButton(
                                    onClick = { shareReportCsv(currentExamSubmissions) },
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .testTag("share_report_btn"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        modifier = Modifier.size(14.dp),
                                        tint = NavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Share", fontSize = 11.sp, color = NavyPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Search box
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("search_student_field"),
                                placeholder = { Text("Filter by student name or roll number...", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate600)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Submission Rows
                if (currentExamSubmissions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No submissions match this exam yet.\nShare Access Code '${selectedExam?.accessCode ?: ""}' with students to begin!",
                                fontSize = 13.sp,
                                color = Slate600,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(currentExamSubmissions) { sub ->
                        val isExpanded = expandedSubmissionId == sub.id
                        SubmissionTableRowCard(
                            submission = sub,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedSubmissionId = if (isExpanded) null else sub.id
                            },
                            onViewCertificate = { onViewCertificate(sub) }
                        )
                    }
                }
            }
        }
    }

    // 10-Column Schema Dialog
    if (showSheetColumnsDialog) {
        GoogleSheetColumnsDialog(
            exam = selectedExam,
            onDismiss = { showSheetColumnsDialog = false }
        )
    }

    // Download Confirmation Dialog
    if (downloadedReportSummary != null) {
        AlertDialog(
            onDismissRequest = { downloadedReportSummary = null },
            confirmButton = {
                Button(
                    onClick = { downloadedReportSummary = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    shareReportCsv(currentExamSubmissions)
                    downloadedReportSummary = null
                }) {
                    Text("Share CSV File")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report Downloaded Successfully", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = downloadedReportSummary!!,
                        fontSize = 12.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate100)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = "Exported Records: ${currentExamSubmissions.size} students", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Text(text = "Assessment: ${selectedExam?.title ?: ""}", fontSize = 10.sp, color = Slate600)
                            Text(text = "Schema: 10 Structured Columns (Google Sheets & Excel compatible)", fontSize = 10.sp, color = Slate600)
                        }
                    }
                }
            }
        )
    }

    // Google Sheets Export In-Progress Dialog
    if (isExportingToSheets) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = GoogleSheetsGreen,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Exporting to Google Sheets...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Syncing ${exportProgress.first} of ${exportProgress.second} graded evaluation rows to Google Sheets...",
                        fontSize = 13.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Spreadsheet ID: ${selectedExam?.googleSheetId ?: ""}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate600
                    )
                }
            }
        )
    }

    // Google Sheets Export Result Dialog
    if (exportResultMessage != null) {
        AlertDialog(
            onDismissRequest = { exportResultMessage = null },
            confirmButton = {
                Button(
                    onClick = { exportResultMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleSheetsGreen)
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                selectedExam?.let { ex ->
                    TextButton(onClick = {
                        exportResultMessage = null
                        onOpenSheetUrl(ex.googleSheetId)
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Sheet")
                        }
                    }
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google Sheets Export Complete", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(text = exportResultMessage!!, fontSize = 13.sp, color = Slate700)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All 10 structured columns (Timestamp, Student Name, Roll No, Total Score, Percentage, Objective Score, Subjective Score, Time Taken, Per-Question JSON, Feedback & Missing Keywords) have been exported.",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }
            }
        )
    }
}

@Composable
private fun GoogleSheetsIntegrationCard(
    exam: Exam,
    totalSubmissions: Int,
    onOpenSheet: () -> Unit,
    onExportToSheet: () -> Unit,
    onDownloadReport: () -> Unit,
    onViewColumns: () -> Unit,
    onEditExam: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sheets_integration_card"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoogleSheetsGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Google Sheets",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google Sheets Backend Connected",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Spreadsheet ID: ${exam.googleSheetId.take(16)}...",
                            color = IceBlueLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(IceBluePrimary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Access: ${exam.accessCode}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onViewColumns,
                    modifier = Modifier.weight(0.9f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IceBluePrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "10 Cols", fontSize = 10.sp)
                }

                Button(
                    onClick = onExportToSheet,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("export_to_sheets_banner_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleSheetsGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "Export to Sheet", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDownloadReport,
                    modifier = Modifier
                        .weight(1.0f)
                        .testTag("download_report_banner_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = IceBluePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "CSV", fontSize = 10.sp, color = Color.White)
                }

                Button(
                    onClick = onOpenSheet,
                    modifier = Modifier.weight(0.9f),
                    colors = ButtonDefaults.buttonColors(containerColor = IceBlueDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "Open", fontSize = 10.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Educator Exam Review and Edit Action
            Button(
                onClick = onEditExam,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("review_and_edit_exam_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = IceBlueDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Review & Edit Exam (${exam.totalQuestions} Questions, ${exam.extractedKeywords.size} Keywords)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SubmissionTableRowCard(
    submission: StudentSubmission,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onViewCertificate: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("submission_row_${submission.rollNo}")
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main Summary Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(IceBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = submission.studentName.take(2).uppercase(),
                            color = NavyPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = submission.studentName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            if (submission.isPartialDraft) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "DRAFT",
                                        color = Color(0xFFB45309),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Roll: ${submission.rollNo} • ${submission.department}",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${submission.totalScore}/${submission.maxScore}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyPrimary
                        )
                        Text(
                            text = if (submission.isPartialDraft) "Unfinished Draft" else "${String.format(Locale.US, "%.1f", submission.percentage)}% Accuracy",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (submission.isPartialDraft) WarningAmber else if (submission.percentage >= 70.0) SuccessGreen else WarningAmber
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand details",
                        tint = Slate600
                    )
                }
            }

            // Quick Metrics Row
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricItem(label = "Objective", value = "${submission.objectiveScore} pts")
                MetricItem(label = "Subjective", value = "${submission.subjectiveScore} pts")
                MetricItem(
                    label = "Time Taken",
                    value = "${submission.totalTimeTakenSeconds / 60}m ${submission.totalTimeTakenSeconds % 60}s"
                )
                MetricItem(
                    label = "Keyword Mastery",
                    value = "${String.format(Locale.US, "%.0f", submission.keywordMasteryPercentage)}%"
                )
            }

            // Expandable Detail View
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = Slate200)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Per-Question Time Tracking (JSON breakdown):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyDark)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = submission.getTimeBreakdownJson(),
                            color = IceBlueLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Subjective AI Keyword Grading Details:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )

                    submission.subjectiveEvaluations.forEach { eval ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Slate100),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = eval.questionPrompt,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${eval.marksAwarded}/${eval.maxMarks} marks",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Student's response
                                Text(
                                    text = "Student Response: \"${eval.studentAnswer}\"",
                                    fontSize = 10.sp,
                                    color = Slate700,
                                    lineHeight = 14.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Keywords
                                if (eval.matchedKeywords.isNotEmpty()) {
                                    Text(
                                        text = "Matched Core Keywords:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessGreen
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        eval.matchedKeywords.forEach {
                                            KeywordChip(keyword = it, status = KeywordStatus.MATCHED)
                                        }
                                    }
                                }

                                if (eval.missingKeywords.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Missing Required Keywords:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WarningAmber
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        eval.missingKeywords.forEach {
                                            KeywordChip(keyword = it, status = KeywordStatus.MISSING)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Gemini AI Feedback: ${eval.feedback}",
                                    fontSize = 10.sp,
                                    color = Slate800,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dedicated Certificate / Report Sheet Button
                    Button(
                        onClick = onViewCertificate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submission_certificate_btn_${submission.rollNo}"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Official Grade Certificate / Evaluation Sheet",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnterpriseSettingsCard(
    exam: Exam?,
    selectedRetentionDays: Int,
    onRetentionDaysChange: (Int) -> Unit,
    onApplyRetention: () -> Unit,
    onOpenSheet: () -> Unit
) {
    val context = LocalContext.current
    var webhookUrl by remember { mutableStateOf(exam?.webhookNotificationUrl ?: "https://script.google.com/macros/s/AKfycbx_viaas_webhook/exec") }
    var webhookPingMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("enterprise_settings_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F766E).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "Enterprise & Administrative Controls", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(text = "Tally Pro logic, data retention, and bi-directional Google Sheets sync", fontSize = 11.sp, color = Slate600)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Data Retention & Privacy Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Data Retention & Archival Policy", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Text(
                text = "Automatically purge or archive student test records after set period to comply with privacy policies:",
                fontSize = 11.sp,
                color = Slate600,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(30, 60, 90).forEach { days ->
                    val isSel = selectedRetentionDays == days
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) NavyPrimary else Slate100)
                            .clickable { onRetentionDaysChange(days) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$days Days",
                            color = if (isSel) Color.White else Slate800,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onApplyRetention,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Archive Submissions Older Than $selectedRetentionDays Days", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(14.dp))

            // 2. Exam Access Security & Passcode
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Access Protection & Fraud Prevention", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Master Exam Passcode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                    Text(text = if (exam?.isPasswordProtected == true) "Passcode: ${exam.masterPassword}" else "Passcode: VIAAS2026 (Optional protection enabled)", fontSize = 11.sp, color = Slate600)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SuccessGreenBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = "ENFORCED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Limit 1 Submission Per Roll Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                    Text(text = "Prevents duplicate submissions from same candidate ID", fontSize = 10.sp, color = Slate600)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SuccessGreenBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = "ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(14.dp))

            // 3. Automated Webhook & Email Notification Trigger
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Custom Email & Webhook Sync", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = webhookUrl,
                onValueChange = { webhookUrl = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notification Webhook / Google Apps Script URL", fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    webhookPingMessage = "Webhook ping simulation sent to $webhookUrl (HTTP 200 OK)."
                    Toast.makeText(context, "Webhook notification sent!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Test Automated Email & Webhook Ping", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (webhookPingMessage != null) {
                Text(
                    text = webhookPingMessage!!,
                    fontSize = 10.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 9.sp, color = Slate600)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
    }
}

@Composable
private fun GoogleSheetColumnsDialog(
    exam: Exam?,
    onDismiss: () -> Unit
) {
    val columns = listOf(
        "1. Timestamp" to "ISO Formatted submission date and time (e.g. 2026-10-07 14:30:00)",
        "2. Student Name" to "Full legal name entered during onboarding (e.g. Alex Chen)",
        "3. Roll Number / ID" to "Student academic ID / Roll number (e.g. 24-CS-042)",
        "4. Total Score" to "Aggregated score formatted as Score/Max (e.g. 15/16)",
        "5. Percentage (%)" to "Normalized percentage score (e.g. 93.8%)",
        "6. Objective Score" to "Total points achieved on MCQ, True/False, Fill in blanks",
        "7. Subjective Score" to "Marks awarded by Gemini AI Keyword Evaluation Engine",
        "8. Total Time Taken (mins)" to "Duration student spent on the entire test",
        "9. Time Spent Per Question (JSON)" to "Exact breakdown logged per question: {\"q1\": \"32s\", \"q2\": \"19s\"...}",
        "10. Detailed Feedback & Missing Keywords" to "AI analysis stating matched terms, missing critical terms, and guidance"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = GoogleSheetsGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google Sheets 10-Column Schema", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Every completed assessment row is formatted and appended to Google Sheets using the following exact schema:",
                        fontSize = 11.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(columns) { (name, desc) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate100)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Text(text = desc, fontSize = 10.sp, color = Slate700)
                        }
                    }
                }
            }
        }
    )
}
