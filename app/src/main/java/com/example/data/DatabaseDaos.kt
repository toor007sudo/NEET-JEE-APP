package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)
}

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plan ORDER BY id ASC")
    fun getStudyPlanFlow(): Flow<List<StudyPlanItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StudyPlanItem>)

    @Update
    suspend fun updateItem(item: StudyPlanItem)

    @Query("DELETE FROM study_plan")
    suspend fun clearAll()
}

@Dao
interface TestItemDao {
    @Query("SELECT * FROM test_items")
    fun getTestItemsFlow(): Flow<List<TestItem>>

    @Query("SELECT * FROM test_items WHERE examType = :examType")
    fun getTestsForExamFlow(examType: String): Flow<List<TestItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TestItem>)

    @Query("DELETE FROM test_items")
    suspend fun clearAll()
}

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllResultsFlow(): Flow<List<TestResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TestResult)
}

@Dao
interface DoubtThreadDao {
    @Query("SELECT * FROM doubt_threads ORDER BY timestamp DESC")
    fun getDoubtThreadsFlow(): Flow<List<DoubtThread>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: DoubtThread)

    @Query("UPDATE doubt_threads SET repliesCount = repliesCount + 1 WHERE id = :id")
    suspend fun incrementReplyCount(id: Int)
}

@Dao
interface ThreadReplyDao {
    @Query("SELECT * FROM thread_replies WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getRepliesForThreadFlow(threadId: Int): Flow<List<ThreadReply>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: ThreadReply)
}
