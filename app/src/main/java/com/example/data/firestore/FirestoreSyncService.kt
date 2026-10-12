package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Exam
import com.example.data.model.StudentSubmission
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject

class FirestoreSyncService(context: Context) {

    private val db: FirebaseFirestore = try {
        val databaseId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(databaseId)
    } catch (e: Exception) {
        Log.w(TAG, "Failed to initialize Firestore with custom database ID: ${e.message}")
        FirebaseFirestore.getInstance()
    }

    private companion object {
        const val TAG = "FirestoreSyncService"
        const val EXAMS_COLLECTION = "exams"
        const val SUBMISSIONS_COLLECTION = "submissions"
    }

    /**
     * Syncs an Exam document to Firestore in real-time.
     */
    fun syncExam(exam: Exam, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val docData = hashMapOf(
            "id" to exam.id,
            "title" to exam.title,
            "description" to exam.description,
            "accessCode" to exam.accessCode,
            "sourceMaterial" to exam.sourceMaterial,
            "timeLimitMinutes" to exam.timeLimitMinutes,
            "googleSheetId" to exam.googleSheetId,
            "createdAt" to exam.createdAt,
            "isPasswordProtected" to exam.isPasswordProtected,
            "masterPassword" to exam.masterPassword,
            "limitOnePerRollNo" to exam.limitOnePerRollNo,
            "hasNegativeMarking" to exam.hasNegativeMarking,
            "negativeMarkingPenalty" to exam.negativeMarkingPenalty,
            "scoreMultiplier" to exam.scoreMultiplier,
            "institutionName" to exam.institutionName,
            "themePalette" to exam.themePalette,
            "totalQuestions" to exam.totalQuestions,
            "totalMaxMarks" to exam.totalMaxMarks
        )

        db.collection(EXAMS_COLLECTION).document(exam.id)
            .set(docData)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully synced exam ${exam.id} to Firestore")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error syncing exam ${exam.id} to Firestore: ${e.message}")
                onFailure(e)
            }
    }

    /**
     * Syncs a student submission or draft to Firestore for real-time educator analytics.
     */
    fun syncSubmission(submission: StudentSubmission, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val docData = hashMapOf(
            "id" to submission.id,
            "examId" to submission.examId,
            "examTitle" to submission.examTitle,
            "studentName" to submission.studentName,
            "rollNo" to submission.rollNo,
            "department" to submission.department,
            "totalScore" to submission.totalScore,
            "maxScore" to submission.maxScore,
            "percentage" to submission.percentage,
            "objectiveScore" to submission.objectiveScore,
            "subjectiveScore" to submission.subjectiveScore,
            "totalTimeTakenSeconds" to submission.totalTimeTakenSeconds,
            "googleSheetsSyncStatus" to submission.googleSheetsSyncStatus,
            "timestamp" to submission.timestamp,
            "isPartialDraft" to submission.isPartialDraft,
            "emailNotificationSent" to submission.emailNotificationSent,
            "appliedNegativePenalty" to submission.appliedNegativePenalty,
            "appliedMultiplier" to submission.appliedMultiplier
        )

        db.collection(SUBMISSIONS_COLLECTION).document(submission.id)
            .set(docData)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully synced submission ${submission.id} (draft=${submission.isPartialDraft}) to Firestore")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error syncing submission ${submission.id} to Firestore: ${e.message}")
                onFailure(e)
            }
    }

    /**
     * Real-time listener Flow for submissions to power educator dashboard analytics in real-time.
     */
    fun observeSubmissions(examId: String? = null): Flow<List<StudentSubmission>> = callbackFlow {
        val query = if (examId != null) {
            db.collection(SUBMISSIONS_COLLECTION).whereEqualTo("examId", examId)
        } else {
            db.collection(SUBMISSIONS_COLLECTION)
        }

        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing submissions snapshot: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getString("id") ?: doc.id
                        val eId = doc.getString("examId") ?: ""
                        val eTitle = doc.getString("examTitle") ?: ""
                        val studentName = doc.getString("studentName") ?: ""
                        val rollNo = doc.getString("rollNo") ?: ""
                        val department = doc.getString("department") ?: "Computer Science & Engineering"
                        val totalScore = doc.getLong("totalScore")?.toInt() ?: 0
                        val maxScore = doc.getLong("maxScore")?.toInt() ?: 100
                        val percentage = doc.getDouble("percentage") ?: 0.0
                        val objectiveScore = doc.getLong("objectiveScore")?.toInt() ?: 0
                        val subjectiveScore = doc.getLong("subjectiveScore")?.toInt() ?: 0
                        val totalTimeTakenSeconds = doc.getLong("totalTimeTakenSeconds") ?: 0L
                        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val isPartialDraft = doc.getBoolean("isPartialDraft") ?: false
                        val emailNotificationSent = doc.getBoolean("emailNotificationSent") ?: false
                        val appliedPenalty = doc.getDouble("appliedNegativePenalty") ?: 0.0
                        val appliedMultiplier = doc.getDouble("appliedMultiplier") ?: 1.0

                        StudentSubmission(
                            id = id,
                            examId = eId,
                            examTitle = eTitle,
                            studentName = studentName,
                            rollNo = rollNo,
                            department = department,
                            totalScore = totalScore,
                            maxScore = maxScore,
                            percentage = percentage,
                            objectiveScore = objectiveScore,
                            subjectiveScore = subjectiveScore,
                            totalTimeTakenSeconds = totalTimeTakenSeconds,
                            timestamp = timestamp,
                            isPartialDraft = isPartialDraft,
                            emailNotificationSent = emailNotificationSent,
                            appliedNegativePenalty = appliedPenalty,
                            appliedMultiplier = appliedMultiplier
                        )
                    } catch (ex: Exception) {
                        Log.w(TAG, "Failed parsing submission doc ${doc.id}: ${ex.message}")
                        null
                    }
                }
                trySend(list)
            }
        }

        awaitClose {
            registration.remove()
        }
    }
}
