package com.example.data

import androidx.room.*

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Arjun",
    val streakCount: Int = 27,
    val lastActiveMillis: Long = System.currentTimeMillis(),
    val completedQuota: Int = 67, // visual progress ring %, e.g., 67%
    val selectedStream: String = "JEE", // "JEE" or "NEET"
    val preferredExamType: String = "JEE Main", // "JEE Main", "JEE Advanced", "NEET UG"
    val selectedSubjects: String = "Physics, Chemistry, Maths", // Comma-separated list
    val selectedTestType: String = "Full Syllabus Test",
    val selectedDifficulty: String = "Medium",
    val points: Int = 1250,
    val badges: String = "Streak Master, Accuracy Ace, AI Solver Pro", // Comma separated tags
    val weakChapters: String = "Rotational Motion, Vector 3D, Chemical Bonding"
)

@Entity(tableName = "study_plan")
data class StudyPlanItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timeLabel: String,         // e.g. "9:00 AM - 11:00 AM"
    val subjectName: String,       // e.g. "Physics"
    val topicName: String,         // e.g. "Rotational Motion"
    val isCompleted: Boolean = false,
    val hexColor: String = "0xFF7F4FFF"
)

@Entity(tableName = "test_items")
data class TestItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val examType: String,
    val subjects: String,
    val questionsCount: Int = 180,
    val marks: Int = 540,
    val durationMins: Int = 180,
    val accuracyPercent: Int,      // average or historical benchmark, e.g. 92
    val studentsCount: String,     // e.g. "12.5K"
    val badgeTag: String = ""      // "Recommended", "Trending", "High Accuracy", or empty
)

@Entity(tableName = "test_results")
data class TestResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val testTitle: String,
    val examType: String,
    val category: String, // e.g. "Full Syllabus"
    val correctCount: Int,
    val totalCount: Int,
    val percentage: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "doubt_threads")
data class DoubtThread(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idString: String = java.util.UUID.randomUUID().toString(),
    val authorName: String,
    val authorAvatarUrl: String = "",
    val subject: String, // "Physics", "Chemistry", "Maths", "Biology"
    val title: String,
    val content: String,
    val repliesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "thread_replies")
data class ThreadReply(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val threadId: Int,
    val authorName: String,
    val content: String,
    val isAi: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
