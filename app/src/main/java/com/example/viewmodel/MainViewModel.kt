package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val authService = AuthService(application)

    // --- Firebase Authentication States ---
    private val _authStateLoading = MutableStateFlow(false)
    val authStateLoading: StateFlow<Boolean> = _authStateLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val currentFirebaseUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = flow {
        while (true) {
            emit(authService.currentUser)
            kotlinx.coroutines.delay(1500)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Sign Up with Email and Password (Progress is reset inside setupNewUserProfile dynamically)
    fun signUpWithEmail(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authStateLoading.value = true
            _authError.value = null
            try {
                authService.signUpWithEmail(email, password, name)
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Sign up failed"
            } finally {
                _authStateLoading.value = false
            }
        }
    }

    // Login with Email and Password
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authStateLoading.value = true
            _authError.value = null
            try {
                authService.loginWithEmail(email, password)
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Login failed"
            } finally {
                _authStateLoading.value = false
            }
        }
    }

    // Google Sign-In / Sign-Up
    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _authStateLoading.value = true
            _authError.value = null
            try {
                authService.signInWithGoogle(idToken)
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Google sign in failed"
            } finally {
                _authStateLoading.value = false
            }
        }
    }

    // Logout
    fun logout() {
        authService.logout()
    }

    // Force Cloud Backup (updates userProfile and tables in Firestore)
    fun forceCloudBackup() {
        viewModelScope.launch {
            try {
                authService.backupUserDataToCloud()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Backup failed", e)
            }
        }
    }

    // Force Cloud Data Restore
    fun forceCloudRestore() {
        viewModelScope.launch {
            try {
                authService.restoreUserDataFromCloud()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Restore failed", e)
            }
        }
    }

    // --- Flows from Room Database ---
    val userProfile: StateFlow<UserProfile?> = db.userProfileDao.getUserProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val studyPlan: StateFlow<List<StudyPlanItem>> = db.studyPlanDao.getStudyPlanFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val testItems: StateFlow<List<TestItem>> = db.testItemDao.getTestItemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val testResults: StateFlow<List<TestResult>> = db.testResultDao.getAllResultsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2000), emptyList())

    val doubtThreads: StateFlow<List<DoubtThread>> = db.doubtThreadDao.getDoubtThreadsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current active thread and its replies
    private val _activeThreadIdString = MutableStateFlow<String?>(null)
    val activeThreadIdString: StateFlow<String?> = _activeThreadIdString.asStateFlow()

    private val _activeThreadId = MutableStateFlow<Int?>(null)
    val activeThreadId: StateFlow<Int?> = _activeThreadId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeReplies: StateFlow<List<ThreadReply>> = _activeThreadId
        .flatMapLatest { id ->
            if (id != null) db.threadReplyDao.getRepliesForThreadFlow(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Step-by-Step Test Creation Flow States (1 -> 2 -> 3) ---
    val testFlowStep = mutableStateOf(1) // 1 = Stream & Type selection, 2 = Test listing, 3 = Test complete/confirm

    // Selected states matching screen 2 wizard
    val wizardStream = mutableStateOf("JEE") // "JEE" or "NEET"
    val wizardExamType = mutableStateOf("JEE Main") // "JEE Main", "JEE Advanced", "NEET UG"
    val wizardSubjects = mutableStateOf(setOf("Physics", "Chemistry", "Maths")) // Multi-select
    val wizardTestType = mutableStateOf("Full Syllabus Test") // "Full Syllabus", "Chapter", "Topic", "PYQ"
    val wizardDifficulty = mutableStateOf("Medium") // "Easy", "Medium", "Hard"

    // Filter state for Screen 3 Test Listing
    val testListFilter = mutableStateOf("All") // "All", "Recommended", "Trending", "High Accuracy"

    // --- AI Feature States ---
    // AI Doubt Solver Screen states
    val aiSolverQuery = mutableStateOf("")
    val aiSolverResponse = mutableStateOf("")
    val aiSolverLoading = mutableStateOf(false)
    val aiSolverImgBase64 = mutableStateOf<String?>(null) // Holder for base64 loaded image

    // AI study planner
    val aiPlanInput = mutableStateOf("")
    val aiGeneratedPlan = mutableStateOf("")
    val aiPlanLoading = mutableStateOf(false)

    // AI weak chapter analyzer
    val aiChapterReport = mutableStateOf("")
    val aiChapterLoading = mutableStateOf(false)

    init {
        // Prepopulate data on first run
        viewModelScope.launch {
            prepopulateDataIfNeeded()
        }
    }

    private suspend fun prepopulateDataIfNeeded() {
        val currentProfile = db.userProfileDao.getUserProfile()
        if (currentProfile == null) {
            // 1. Initial User
            db.userProfileDao.insertOrUpdate(UserProfile())

            // 2. Study Plan Items (matches Reference Screen 1)
            db.studyPlanDao.clearAll()
            db.studyPlanDao.insertAll(
                listOf(
                    StudyPlanItem(timeLabel = "9:00 AM\n11:00 AM", subjectName = "Physics", topicName = "Rotational Motion", isCompleted = false, hexColor = "0xFF7F4FFF"),
                    StudyPlanItem(timeLabel = "11:30 AM\n1:00 PM", subjectName = "Chemistry", topicName = "Chemical Bonding", isCompleted = false, hexColor = "0xFF00E5FF"),
                    StudyPlanItem(timeLabel = "2:30 PM\n4:00 PM", subjectName = "Maths", topicName = "Vector 3D", isCompleted = true, hexColor = "0xFFFFB300"),
                    StudyPlanItem(timeLabel = "7:00 PM\n8:30 PM", subjectName = "Biology", topicName = "Human Physiology", isCompleted = false, hexColor = "0xFF00E676")
                )
            )

            // 3. Test Items (matches Reference Screen 3)
            db.testItemDao.clearAll()
            db.testItemDao.insertAll(
                listOf(
                    TestItem(title = "Full Syllabus Test 01", examType = "JEE Main", subjects = "Physics, Chemistry, Maths", questionsCount = 180, marks = 540, durationMins = 180, accuracyPercent = 92, studentsCount = "12.5K", badgeTag = "Recommended"),
                    TestItem(title = "Full Syllabus Test 02", examType = "JEE Main", subjects = "Physics, Chemistry, Maths", questionsCount = 180, marks = 540, durationMins = 180, accuracyPercent = 89, studentsCount = "9.8K", badgeTag = "Trending"),
                    TestItem(title = "Full Syllabus Test 03", examType = "JEE Main", subjects = "Physics, Chemistry, Maths", questionsCount = 180, marks = 540, durationMins = 180, accuracyPercent = 94, studentsCount = "8.2K", badgeTag = "High Accuracy"),
                    TestItem(title = "Full Syllabus Test 04", examType = "JEE Main", subjects = "Physics, Chemistry, Maths", questionsCount = 180, marks = 540, durationMins = 180, accuracyPercent = 87, studentsCount = "6.4K", badgeTag = ""),
                    TestItem(title = "NEET Biology Chapter Test 1", examType = "NEET UG", subjects = "Biology", questionsCount = 90, marks = 360, durationMins = 90, accuracyPercent = 91, studentsCount = "14.2K", badgeTag = "Recommended"),
                    TestItem(title = "NEET Chemistry PYQ 2024", examType = "NEET UG", subjects = "Chemistry", questionsCount = 45, marks = 180, durationMins = 45, accuracyPercent = 85, studentsCount = "19.5K", badgeTag = "Trending"),
                    TestItem(title = "JEE Advanced Calculus Prep", examType = "JEE Advanced", subjects = "Maths", questionsCount = 30, marks = 120, durationMins = 60, accuracyPercent = 78, studentsCount = "5.1K", badgeTag = "High Accuracy")
                )
            )

            // 4. Initial Doubts / Discussions
            val t1 = DoubtThread(authorName = "Rohan Sharma", subject = "Physics", title = "Rotational Equilibrium Question", content = "If a rigid body is in rotational equilibrium but has translation acceleration, how does that affect torque calculations around non-fixed axes?")
            db.doubtThreadDao.insertThread(t1)
            val t2 = DoubtThread(authorName = "Anjali Raj", subject = "Chemistry", title = "Dipole Moment of NH3 vs NF3", content = "Why is the dipole moment of ammonia (1.47 D) significantly larger than nitrogen trifluoride (0.23 D), even though fluorine is much more electronegative?")
            db.doubtThreadDao.insertThread(t2)
        }
    }

    // --- Study Plan Actions ---
    fun toggleStudyPlanCompleted(item: StudyPlanItem) {
        viewModelScope.launch {
            db.studyPlanDao.updateItem(item.copy(isCompleted = !item.isCompleted))
            // Recalculate visual progress ring percentage based on items completed
            val list = studyPlan.value
            val completed = list.count { if (it.id == item.id) !item.isCompleted else it.isCompleted }
            val total = list.size
            val ratioPercent = if (total > 0) (completed * 100) / total else 67

            val profile = db.userProfileDao.getUserProfile()
            if (profile != null) {
                db.userProfileDao.updateProfile(profile.copy(completedQuota = ratioPercent))
            }
        }
    }

    // --- Student Streak Booster ---
    fun checkInAndBoostStreak() {
        viewModelScope.launch {
            val profile = db.userProfileDao.getUserProfile()
            if (profile != null) {
                val newStreak = profile.streakCount + 1
                val newCoins = profile.points + 50
                db.userProfileDao.updateProfile(
                    profile.copy(
                        streakCount = newStreak,
                        points = newCoins,
                        lastActiveMillis = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // --- Test Selection Actions ---
    fun setWizardStreamSelection(stream: String) {
        wizardStream.value = stream
        if (stream == "JEE") {
            wizardExamType.value = "JEE Main"
            wizardSubjects.value = setOf("Physics", "Chemistry", "Maths")
        } else {
            wizardExamType.value = "NEET UG"
            wizardSubjects.value = setOf("Physics", "Chemistry", "Biology")
        }
    }

    fun toggleSubjectSelected(subject: String) {
        val currentSet = wizardSubjects.value.toMutableSet()
        if (currentSet.contains(subject)) {
            currentSet.remove(subject)
        } else {
            currentSet.add(subject)
        }
        wizardSubjects.value = currentSet
    }

    // Move from screen 2 to screen 3
    fun continueToTestSelection() {
        testFlowStep.value = 2
    }

    // Back to screen 2
    fun backToWizardInput() {
        testFlowStep.value = 1
    }

    // Take mock test (Simulation results setup)
    fun takeActiveTest(testTitle: String, examType: String, category: String) {
        viewModelScope.launch {
            // Mock test scores and create TestResult persistent database log
            val correct = (130..170).random()
            val total = 180
            val percent = (correct * 100) / total
            val result = TestResult(
                testTitle = testTitle,
                examType = examType,
                category = category,
                correctCount = correct,
                totalCount = total,
                percentage = percent
            )
            db.testResultDao.insertResult(result)

            // Update user metrics
            val profile = db.userProfileDao.getUserProfile()
            if (profile != null) {
                val addedCoins = profile.points + 100
                db.userProfileDao.updateProfile(
                    profile.copy(
                        points = addedCoins
                    )
                )
            }
            testFlowStep.value = 3 // Confirmed done screen
        }
    }

    fun restartTestFlow() {
        testFlowStep.value = 1
    }

    // --- AI Doubt Solver API Call ---
    fun solveDoubtWithAI() {
        val userQuery = aiSolverQuery.value
        if (userQuery.trim().isEmpty()) {
            aiSolverResponse.value = "Please describe or write down your question first."
            return
        }

        aiSolverLoading.value = true
        aiSolverResponse.value = "Searching concepts..."

        viewModelScope.launch {
            val systemPrompt = """
                You are high-fidelity NEET and JEE specialist doubt solver, equipped with complete NCERT framework indices and complex engineering textbooks.
                Provide:
                1. Elegant, step-by-step conceptual answer to the user's doubt.
                2. State critical formulas clearly inside a box.
                3. Add brief, high-level tip to solve JEE/NEET questions related to this concept.
                
                Keep output concise, friendly, and structured.
            """.trimIndent()

            val response = GeminiClient.queryGemini(
                prompt = userQuery,
                systemPrompt = systemPrompt,
                base64Image = aiSolverImgBase64.value
            )
            aiSolverResponse.value = response
            aiSolverLoading.value = false
        }
    }

    // --- AI Custom Daily Study Planner API Call ---
    fun generatePersonalizedStudyPlan() {
        val userStream = wizardStream.value
        val examType = wizardExamType.value
        val itemsStr = wizardSubjects.value.joinToString(", ")
        val weakArea = userProfile.value?.weakChapters ?: "Thermodynamics, Vectors"

        aiPlanLoading.value = true
        aiGeneratedPlan.value = "Synthesizing schedule..."

        viewModelScope.launch {
            val prompt = """
                Generate custom, high-impact 7-day NEET/JEE preparation study schedule for $userStream ($examType) focusing on $itemsStr.
                The student is currently struggling with: $weakArea.
                For each day:
                - Morning revision focus topic & formulas.
                - Afternoon high-yield practice session topics.
                - Evening weakness remedy checklist.
                Make it look structured, professional and encouraging.
            """.trimIndent()

            val response = GeminiClient.queryGemini(
                prompt = prompt,
                systemPrompt = "You are a professional JEE and NEET academic dean."
            )
            aiGeneratedPlan.value = response
            aiPlanLoading.value = false
        }
    }

    // --- AI Weakness Analytical Review API Call ---
    fun analyzeWeaknessFromHistory() {
        val weakArea = userProfile.value?.weakChapters ?: "Rotational Motion, Vector 3D, Chemical Bonding"
        val solvedCount = testResults.value.size

        aiChapterLoading.value = true
        aiChapterReport.value = "Analyzing performance matrix..."

        viewModelScope.launch {
            val prompt = """
                Based on user's weak chapters: $weakArea, and total solved tests: $solvedCount.
                1. List exactly 3 conceptual traps students fall into for these chapters in JEE/NEET.
                2. Recommend 2 priority steps to take this week.
                3. Suggest some smart exam tricks.
            """.trimIndent()

            val response = GeminiClient.queryGemini(
                prompt = prompt,
                systemPrompt = "You are a super smart coaching analyst."
            )
            aiChapterReport.value = response
            aiChapterLoading.value = false
        }
    }

    // --- Community Forum Thread Routing & AI Reply Integration ---
    fun selectActiveThread(thread: DoubtThread) {
        _activeThreadId.value = thread.id
        _activeThreadIdString.value = thread.idString
    }

    fun clearActiveThread() {
        _activeThreadId.value = null
        _activeThreadIdString.value = null
    }

    fun submitNewDoubt(title: String, content: String, subject: String, autoAiAnswer: Boolean) {
        if (title.isEmpty() || content.isEmpty()) return

        viewModelScope.launch {
            val author = userProfile.value?.name ?: "Arjun"
            val newThread = DoubtThread(
                authorName = author,
                subject = subject,
                title = title,
                content = content
            )
            db.doubtThreadDao.insertThread(newThread)

            // Let's reload threads list. Flow automates this!
            if (autoAiAnswer) {
                // If checked, trigger immediate Gemini AI answer in background
                val savedList = db.doubtThreadDao.getDoubtThreadsFlow().first()
                val newlyAdded = savedList.firstOrNull { it.title == title && it.content == content }
                if (newlyAdded != null) {
                    // Launch background reply as AI
                    val systemContextPrompt = """
                        You are a specialist community mentor bot for NEET/JEE. You provide quick, highly specific answers to student posts.
                    """.trimIndent()

                    val aiReplyText = GeminiClient.queryGemini(
                        prompt = "Review this student doubt and provide an elegant 3-sentence expert solution:\nTitle: $title\nContent: $content",
                        systemPrompt = systemContextPrompt
                    )

                    db.threadReplyDao.insertReply(
                        ThreadReply(
                            threadId = newlyAdded.id,
                            authorName = "Gemini Expert AI Assist",
                            content = aiReplyText,
                            isAi = true
                        )
                    )
                    db.doubtThreadDao.incrementReplyCount(newlyAdded.id)
                }
            }
        }
    }

    fun replyToThread(threadId: Int, content: String) {
        if (content.isEmpty()) return
        viewModelScope.launch {
            val author = userProfile.value?.name ?: "Arjun"
            val newReply = ThreadReply(
                threadId = threadId,
                authorName = author,
                content = content,
                isAi = false
            )
            db.threadReplyDao.insertReply(newReply)
            db.doubtThreadDao.incrementReplyCount(threadId)
        }
    }
}
