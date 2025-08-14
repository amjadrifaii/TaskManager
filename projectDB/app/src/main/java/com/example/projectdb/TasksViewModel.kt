package com.example.projectdb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Calendar

class TasksViewModel(val dao: TaskDao) : ViewModel() {
    val newTaskName = MutableLiveData<String>()
    val newTaskDate = MutableLiveData<Long?>()
    val newTaskDesc = MutableLiveData<String>()
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> get() = _tasks
    private val _taskAddedMessage = MutableLiveData<Boolean>()
    val taskAddedMessage: LiveData<Boolean> get() = _taskAddedMessage

    val filteredTasks = MutableLiveData<List<Task>>()

    val allTasks: LiveData<List<Task>> = dao.getAll()

    init {
        allTasks.observeForever { taskList ->
            _tasks.value = taskList
        }
    }

    fun addTask() {
        viewModelScope.launch {
            val task = Task()
            task.taskName = newTaskName.value ?: ""
            task.taskDesc = newTaskDesc.value ?: ""

            val calendar = Calendar.getInstance().apply {
                timeInMillis = newTaskDate.value ?: System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            task.taskDate = calendar.timeInMillis

            dao.insert(task)

            newTaskName.value = ""
            newTaskDate.value = null
            newTaskDesc.value = ""

            _taskAddedMessage.value = true
        }
    }

    fun resetTaskAddedMessage() {
        _taskAddedMessage.value = false
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            dao.update(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.delete(task)
        }
    }
    fun deleteTasks(tasks: List<Task>) {
        viewModelScope.launch {
            tasks.forEach { dao.delete(it) }
        }
    }
    fun filterTasksByDate(selectedDate: Long) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.HOUR_OF_DAY, 0) // Normalize to start of day (midnight)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        dao.getTasksByDate(startOfDay, endOfDay).observeForever { filteredTasksList ->
            filteredTasks.value = filteredTasksList
        }
    }

    fun getTasksByDate(selectedDate: Long): LiveData<List<Task>> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        return dao.getTasksByDate(startOfDay, endOfDay)
    }
}