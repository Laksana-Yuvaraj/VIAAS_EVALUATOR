package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
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
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenBg

@Composable
fun ProEnterpriseFeaturesCard(
    exam: Exam?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 1. Custom Domains
    var customDomain by remember { mutableStateOf("exams.vivekanandha.ac.in") }
    var isDomainConfigured by remember { mutableStateOf(true) }

    // 2. Collaboration & Team Workspaces
    var teamEmails by remember { mutableStateOf("hod_cs@viaas.edu, dean_academics@viaas.edu") }
    var activeWorkspace by remember { mutableStateOf("VIAAS Computer Science Department") }
    var selectedFolder by remember { mutableStateOf("Semester VII Midterms") }

    // 3. Custom CSS & Advanced Styling
    var customCss by remember {
        mutableStateOf(
            """/* VIAAS Institutional Theme Overrides */
.viaas-brand-header { background: #0A192F; font-family: 'Inter', sans-serif; }
.exam-runner-card { border-radius: 14px; box-shadow: 0 4px 16px rgba(10,25,47,0.08); }
.question-badge { background-color: #38BDF8; color: #0A192F; font-weight: 700; }"""
        )
    }

    // 4. Custom Email & Custom Email Domains
    var senderEmailDomain by remember { mutableStateOf("notifications@viaas.edu.in") }
    var notificationSubject by remember { mutableStateOf("Your VIAAS Midterm Evaluation Certificate & Score Report") }
    var emailSyncEnabled by remember { mutableStateOf(true) }

    // 5. Link Preview (OG Metadata)
    var ogTitle by remember { mutableStateOf(exam?.title ?: "Computer Networks Midterm Examination") }
    var ogDescription by remember { mutableStateOf("Official online proctored evaluation hosted by Vivekanandha Arts & Science College.") }
    var ogImageUrl by remember { mutableStateOf("https://viaas.edu.in/assets/img/exam-banner-og.png") }
    var faviconUrl by remember { mutableStateOf("https://viaas.edu.in/favicon.ico") }

    // 6. Unlimited Uploads
    var unlimitedUploadsEnabled by remember { mutableStateOf(true) }

    // 7. Premium Integrations
    var googleAnalyticsId by remember { mutableStateOf("G-VIAAS702PRO") }
    var metaPixelId by remember { mutableStateOf("182736450912") }

    // 8. Version History
    var activeVersion by remember { mutableStateOf("v2.4 (Current - Published Oct 8, 2026)") }
    var versionRestoreMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
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
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Web, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Tally Pro & Enterprise Form-Builder Suite", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        Text("Institutional custom domains, CSS, unlimited uploads & analytics", fontSize = 11.sp, color = Slate600)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreenBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("PRO ACTIVE", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            // Section 1: Custom Domains & Link Previews
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Custom Domain & Branded Link Previews", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Text("Host forms on your verified institutional subdomain with personalized Open Graph metadata:", fontSize = 11.sp, color = Slate600, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

            OutlinedTextField(
                value = customDomain,
                onValueChange = { customDomain = it },
                label = { Text("Custom Domain / CNAME", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SuccessGreenBg)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text("SSL ACTIVE", color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ogTitle,
                    onValueChange = { ogTitle = it },
                    label = { Text("OG Title", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = faviconUrl,
                    onValueChange = { faviconUrl = it },
                    label = { Text("Favicon URL", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(IceBlueBackground)
                    .border(1.dp, IceBlueLight, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text("🔗 Live Form URL Preview:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IceBlueDark)
                    Text("https://$customDomain/eval/${exam?.accessCode ?: "VIA-702"}", fontSize = 12.sp, color = NavyPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("OG Description: $ogDescription", fontSize = 10.sp, color = Slate700)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            // Section 2: Collaboration & Workspaces
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Collaboration & Workspaces", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Text("Invite unlimited faculty members to collaborative exam drafting spaces and organized folders:", fontSize = 11.sp, color = Slate600, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = activeWorkspace,
                    onValueChange = { activeWorkspace = it },
                    label = { Text("Active Workspace", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = selectedFolder,
                    onValueChange = { selectedFolder = it },
                    label = { Text("Folder", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = teamEmails,
                onValueChange = { teamEmails = it },
                label = { Text("Faculty Collaborators (comma-separated emails)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            // Section 3: Custom CSS Injection & Design Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Custom CSS Injection & Styling Engine", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Text("Inject CSS rules to fully match college visual standards, button styling, and typography:", fontSize = 11.sp, color = Slate600, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

            OutlinedTextField(
                value = customCss,
                onValueChange = { customCss = it },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            // Section 4: Custom Email Domains & Auto Notifications
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Custom Email Notifications & Domain", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Text("Send personalized score certificates and feedback from your authenticated domain:", fontSize = 11.sp, color = Slate600, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

            OutlinedTextField(
                value = senderEmailDomain,
                onValueChange = { senderEmailDomain = it },
                label = { Text("Sender Domain / Return-Path", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Email Evaluation Sheets to Students", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                    Text("Sends PDF report link upon Gemini grading completion", fontSize = 10.sp, color = Slate600)
                }
                Switch(
                    checked = emailSyncEnabled,
                    onCheckedChange = { emailSyncEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NavyPrimary)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            // Section 5: Unlimited Uploads & Premium Analytics Integrations
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Insights, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Unlimited Uploads & Premium Analytics", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Text("Lift 10 MB file caps on student answer attachments and track conversion via Google Analytics & Meta Pixel:", fontSize = 11.sp, color = Slate600, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Unlimited File Uploads (No 10MB limit)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                    Text("Enables large handwritten scan PDFs and diagrams", fontSize = 10.sp, color = Slate600)
                }
                Switch(
                    checked = unlimitedUploadsEnabled,
                    onCheckedChange = { unlimitedUploadsEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SuccessGreen)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = googleAnalyticsId,
                    onValueChange = { googleAnalyticsId = it },
                    label = { Text("Google Analytics ID", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = metaPixelId,
                    onValueChange = { metaPixelId = it },
                    label = { Text("Meta Pixel ID", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            // Section 6: Version History (Up to 30 Days)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = NavyDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("30-Day Version History & Rollback", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Text("Audit question modifications, rubric edits, and restore past versions up to 30 days:", fontSize = 11.sp, color = Slate600, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

            val pastVersions = listOf(
                "v2.4 - Oct 8, 2026 08:30 AM (Current - Added remedial branch rule)",
                "v2.3 - Oct 7, 2026 03:15 PM (Added Section B essay questions)",
                "v2.2 - Oct 6, 2026 11:00 AM (Updated negative marking multiplier)",
                "v1.0 - Oct 1, 2026 09:00 AM (Initial assessment release)"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                pastVersions.forEach { ver ->
                    val isCurrent = ver.contains("Current")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrent) IceBlueBackground else Slate100)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ver, fontSize = 11.sp, color = if (isCurrent) NavyPrimary else Slate800, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                        }
                        if (!isCurrent) {
                            Button(
                                onClick = {
                                    activeVersion = ver.substringBefore(" -")
                                    versionRestoreMessage = "Restored assessment state to $activeVersion successfully."
                                    Toast.makeText(context, "Restored to $activeVersion", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Restore", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            if (versionRestoreMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = versionRestoreMessage!!,
                    fontSize = 11.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
