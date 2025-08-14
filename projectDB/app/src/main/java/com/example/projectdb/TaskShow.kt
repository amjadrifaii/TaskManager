package com.example.projectdb

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.projectdb.databinding.TaskShowBinding

class TaskShow : Fragment() {
    private var _binding: TaskShowBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TasksViewModel
    private lateinit var adapter: TaskItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TaskShowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel first
        val application = requireNotNull(activity).application
        val dao = TaskDatabase.getInstance(application).taskDao
        val viewModelFactory = TasksViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(TasksViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = TaskItemAdapter(
            updateTaskDone = { task -> viewModel.updateTask(task) },
            deleteTask = { task -> viewModel.deleteTask(task) }
        ).apply {
            onSelectionChanged = {
                updateSelectionUI()
            }
        }

        binding.tasksList.adapter = adapter

        binding.selectAll.setOnClickListener {
            val shouldSelectAll = adapter.getSelectedCount() != adapter.itemCount
            adapter.selectAll(shouldSelectAll)
        }

        binding.deleteAll.setOnClickListener {
            val selectedTasks = adapter.getSelectedTasks()
            if (selectedTasks.isNotEmpty()) {
                viewModel.deleteTasks(selectedTasks)
                Toast.makeText(context, "Deleted ${selectedTasks.size} tasks", Toast.LENGTH_SHORT).show()
                adapter.selectAll(false) // Clear selection after deletion
            } else {
                Toast.makeText(context, "No tasks selected", Toast.LENGTH_SHORT).show()
            }
        }

        // Load tasks
        val selectedDate = arguments?.getLong("selectedDate", -1) ?: -1
        if (selectedDate != -1L) {
            viewModel.getTasksByDate(selectedDate).observe(viewLifecycleOwner) { tasks ->
                tasks?.let { adapter.data = it }
            }
        } else {
            viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
                tasks?.let { adapter.data = it }
            }
        }
    }

    private fun updateSelectionUI() {
        val selectedCount = adapter.getSelectedCount()
        binding.selectAll.text = if (selectedCount == adapter.itemCount) "Deselect All" else "Select All"
        binding.deleteAll.isEnabled = selectedCount > 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}