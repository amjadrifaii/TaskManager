package com.example.projectdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM task_table WHERE taskId = :key")
    fun get(key: Long): LiveData<Task>

    @Query("SELECT * FROM task_table ORDER BY task_date DESC")
    fun getAll(): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE task_date >= :startOfDay AND task_date < :endOfDay ORDER BY task_date ASC")
    fun getTasksByDate(startOfDay: Long, endOfDay: Long): LiveData<List<Task>>
}