package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        StudyPlanItem::class,
        TestItem::class,
        TestResult::class,
        DoubtThread::class,
        ThreadReply::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val userProfileDao: UserProfileDao
    abstract val studyPlanDao: StudyPlanDao
    abstract val testItemDao: TestItemDao
    abstract val testResultDao: TestResultDao
    abstract val doubtThreadDao: DoubtThreadDao
    abstract val threadReplyDao: ThreadReplyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neet_jee_prep_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
