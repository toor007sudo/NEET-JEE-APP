package com.example.data

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthService(private val context: Context) {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val db = AppDatabase.getInstance(context)

    val currentUser: FirebaseUser? get() = auth.currentUser

    // Helper utility to convert GMS Tasks to coroutine suspends
    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        this.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: RuntimeException("Firebase operation failed"))
            }
        }
    }

    /**
     * Complete first-signup flow. Sets progress properties to 0
     * and saves details to local database and Cloud Firestore.
     */
    private suspend fun setupNewUserProfile(user: FirebaseUser, customName: String?) = withContext(Dispatchers.IO) {
        val email = user.email ?: ""
        val displayName = customName ?: user.displayName ?: email.substringBefore("@")
        
        // On first signup: progress and points are set to 0 as requested
        val newProfile = UserProfile(
            id = 1,
            name = displayName,
            streakCount = 0,
            completedQuota = 0, // progress = 0 as requested
            points = 0,
            badges = "",
            weakChapters = "Not simulated yet"
        )

        // Save local Room db representation
        db.userProfileDao.insertOrUpdate(newProfile)

        // Also serialize & backup to Cloud Firestore users/{uid}
        val docRef = firestore.collection("users").document(user.uid)
        docRef.set(mapProfileToDocument(newProfile)).awaitTask()

        Log.d("AuthService", "Successfully initialized UserProfile in Firestore with Progress = 0")
    }

    /**
     * Sign up using custom email and password.
     */
    suspend fun signUpWithEmail(email: String, password: String, name: String): AuthResult {
        val result = auth.createUserWithEmailAndPassword(email, password).awaitTask()
        val user = result.user
        if (user != null) {
            setupNewUserProfile(user, name)
        }
        return result
    }

    /**
     * Log in with custom email and password.
     */
    suspend fun loginWithEmail(email: String, password: String): AuthResult {
        val result = auth.signInWithEmailAndPassword(email, password).awaitTask()
        val user = result.user
        if (user != null) {
            // Restore returning user data
            restoreUserDataFromCloud()
        }
        return result
    }

    /**
     * Sign up or Login with Google credential Token.
     */
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).awaitTask()
        val user = result.user
        if (user != null) {
            // Check if profile exists under users/{uid} collection
            val uid = user.uid
            val userDoc = firestore.collection("users").document(uid).get().awaitTask()
            if (userDoc.exists()) {
                // If user exists, restore profile
                restoreUserDataFromCloud()
            } else {
                // If new Google user, register as first signup with progress = 0
                setupNewUserProfile(user, user.displayName)
            }
        }
        return result
    }

    /**
     * Explicit log out function.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Backup existing local user details to Cloud Firestore.
     */
    suspend fun backupUserDataToCloud() = withContext(Dispatchers.IO) {
        val uid = currentUser?.uid ?: return@withContext
        
        // 1. User Profile
        val profile = db.userProfileDao.getUserProfile()
        if (profile != null) {
            firestore.collection("users").document(uid).set(mapProfileToDocument(profile)).awaitTask()
        }

        // 2. Study Plan Items (retrieves first dataset block)
        val studyPlanList = try {
            db.studyPlanDao.getStudyPlanFlow().first()
        } catch (e: Exception) {
            emptyList()
        }
        val studyPlansCol = firestore.collection("users").document(uid).collection("study_plans")
        for (item in studyPlanList) {
            val planMap = mapOf(
                "timeLabel" to item.timeLabel,
                "subjectName" to item.subjectName,
                "topicName" to item.topicName,
                "isCompleted" to item.isCompleted,
                "hexColor" to item.hexColor
            )
            studyPlansCol.document(item.id.toString()).set(planMap).awaitTask()
        }

        // 3. Test Results
        val testResultsList = try {
            db.testResultDao.getAllResultsFlow().first()
        } catch (e: Exception) {
            emptyList()
        }
        val testResultsCol = firestore.collection("users").document(uid).collection("test_results")
        for (item in testResultsList) {
            val resultMap = mapOf(
                "testTitle" to item.testTitle,
                "examType" to item.examType,
                "category" to item.category,
                "correctCount" to item.correctCount,
                "totalCount" to item.totalCount,
                "percentage" to item.percentage,
                "timestamp" to item.timestamp
            )
            testResultsCol.document(item.id.toString()).set(resultMap).awaitTask()
        }
    }

    /**
     * Restore cloud-stored progress metrics & details down into local Room DB.
     */
    suspend fun restoreUserDataFromCloud() = withContext(Dispatchers.IO) {
        val uid = currentUser?.uid ?: return@withContext
        
        // 1. Restore Profile details
        val profileDoc = firestore.collection("users").document(uid).get().awaitTask()
        if (profileDoc.exists()) {
            val cloudProfile = mapDocumentToProfile(profileDoc.data, UserProfile())
            db.userProfileDao.insertOrUpdate(cloudProfile)
        }

        // 2. Restore Study plans
        val studyPlansSnapshot = firestore.collection("users").document(uid).collection("study_plans").get().awaitTask()
        if (!studyPlansSnapshot.isEmpty) {
            db.studyPlanDao.clearAll()
            val newItems = studyPlansSnapshot.documents.map { doc ->
                StudyPlanItem(
                    id = doc.id.toIntOrNull() ?: 0,
                    timeLabel = doc.getString("timeLabel") ?: "",
                    subjectName = doc.getString("subjectName") ?: "",
                    topicName = doc.getString("topicName") ?: "",
                    isCompleted = doc.getBoolean("isCompleted") ?: false,
                    hexColor = doc.getString("hexColor") ?: "0xFF7F4FFF"
                )
            }
            db.studyPlanDao.insertAll(newItems)
        }

        // 3. Restore Test scores
        val testResultsSnapshot = firestore.collection("users").document(uid).collection("test_results").get().awaitTask()
        if (!testResultsSnapshot.isEmpty) {
            for (doc in testResultsSnapshot.documents) {
                val result = TestResult(
                    id = doc.id.toIntOrNull() ?: 0,
                    testTitle = doc.getString("testTitle") ?: "",
                    examType = doc.getString("examType") ?: "",
                    category = doc.getString("category") ?: "",
                    correctCount = (doc.getLong("correctCount") ?: 0L).toInt(),
                    totalCount = (doc.getLong("totalCount") ?: 0L).toInt(),
                    percentage = (doc.getLong("percentage") ?: 0L).toInt(),
                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                )
                db.testResultDao.insertResult(result)
            }
        }
    }

    // --- Helper DTO Data Mappings ---
    private fun mapDocumentToProfile(data: Map<String, Any>?, defaultProfile: UserProfile): UserProfile {
        if (data == null) return defaultProfile
        return UserProfile(
            id = 1,
            name = data["name"] as? String ?: defaultProfile.name,
            streakCount = (data["streakCount"] as? Long)?.toInt() ?: defaultProfile.streakCount,
            lastActiveMillis = data["lastActiveMillis"] as? Long ?: defaultProfile.lastActiveMillis,
            completedQuota = (data["completedQuota"] as? Long)?.toInt() ?: defaultProfile.completedQuota,
            selectedStream = data["selectedStream"] as? String ?: defaultProfile.selectedStream,
            preferredExamType = data["preferredExamType"] as? String ?: defaultProfile.preferredExamType,
            selectedSubjects = data["selectedSubjects"] as? String ?: defaultProfile.selectedSubjects,
            selectedTestType = data["selectedTestType"] as? String ?: defaultProfile.selectedTestType,
            selectedDifficulty = data["selectedDifficulty"] as? String ?: defaultProfile.selectedDifficulty,
            points = (data["points"] as? Long)?.toInt() ?: defaultProfile.points,
            badges = data["badges"] as? String ?: defaultProfile.badges,
            weakChapters = data["weakChapters"] as? String ?: defaultProfile.weakChapters
        )
    }

    private fun mapProfileToDocument(profile: UserProfile): Map<String, Any> {
        return mapOf(
            "name" to profile.name,
            "streakCount" to profile.streakCount,
            "lastActiveMillis" to profile.lastActiveMillis,
            "completedQuota" to profile.completedQuota,
            "selectedStream" to profile.selectedStream,
            "preferredExamType" to profile.preferredExamType,
            "selectedSubjects" to profile.selectedSubjects,
            "selectedTestType" to profile.selectedTestType,
            "selectedDifficulty" to profile.selectedDifficulty,
            "points" to profile.points,
            "badges" to profile.badges,
            "weakChapters" to profile.weakChapters
        )
    }
}
