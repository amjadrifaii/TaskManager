package com.example.projectdb

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.projectdb.databinding.TaskFragBinding
import java.util.Calendar

class TasksFragment : Fragment() {
    private var _binding: TaskFragBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TasksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = TaskFragBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireNotNull(this.activity).application
        val dao = TaskDatabase.getInstance(application).taskDao
        val viewModelFactory = TasksViewModelFactory(dao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(TasksViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.showTasks.setOnClickListener {
            view.findNavController().navigate(R.id.action_tasksFragment_to_taskShow)
        }

        val currentDate = System.currentTimeMillis()
        binding.calendarView.date = currentDate
        binding.calendarView.minDate = currentDate // Set the minimum date to today (no past dates allowed)

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDate = calendar.timeInMillis
            viewModel.newTaskDate.value = selectedDate
        }

        binding.filter.setOnClickListener {
            showDatePickerDialog(viewModel)
        }

        binding.saveButton.setOnClickListener {
            val taskName = binding.taskName.text.toString().trim()
            val taskDesc = binding.taskDesc.text.toString().trim()

            if (taskName.isEmpty() || taskDesc.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a task name and description.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.newTaskName.value = taskName
                viewModel.newTaskDesc.value = taskDesc
                viewModel.addTask()
            }
        }

        viewModel.taskAddedMessage.observe(viewLifecycleOwner) { taskAdded ->
            if (taskAdded == true) {

                Toast.makeText(requireContext(), "Successfully added task", Toast.LENGTH_SHORT).show()

                viewModel.resetTaskAddedMessage()
            }
        }

    }


    // Function to show the DatePickerDialog
     fun showDatePickerDialog(viewModel: TasksViewModel) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Normalize the selected date
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                viewModel.filterTasksByDate(selectedDate)

                val bundle = Bundle().apply {
                    putLong("selectedDate", selectedDate)
                }
                view?.findNavController()?.navigate(R.id.action_tasksFragment_to_taskShow, bundle)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}