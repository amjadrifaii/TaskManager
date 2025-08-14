package com.example.projectdb
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.text.InputFilter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskItemAdapter(
    private val updateTaskDone: (Task) -> Unit,
    private val deleteTask: (Task) -> Unit
) : RecyclerView.Adapter<TaskItemAdapter.TaskItemViewHolder>() {
    private val selectedItems = mutableSetOf<Long>()
    var data = listOf<Task>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onSelectionChanged: (() -> Unit)? = null

    fun selectAll(select: Boolean) {
        if (select) {
            selectedItems.addAll(data.map { it.taskId })
        } else {
            selectedItems.clear()
        }
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
    }

    fun getSelectedCount(): Int = selectedItems.size

    fun getSelectedTasks(): List<Task> {
        return data.filter { selectedItems.contains(it.taskId) }
    }

    override fun getItemId(position: Int): Long {
        return data[position].taskId
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
        return TaskItemViewHolder.inflateFrom(parent, updateTaskDone, deleteTask, this)
    }

    override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item, selectedItems.contains(item.taskId))
    }

    class TaskItemViewHolder(
        private val rootView: CardView,
        private val updateTaskDone: (Task) -> Unit,
        private val deleteTask: (Task) -> Unit,
        private val adapter: TaskItemAdapter
    ) : RecyclerView.ViewHolder(rootView) {

        private val taskName: TextView = rootView.findViewById(R.id.task_name)
        private val taskDesc: TextView = rootView.findViewById(R.id.task_desc)
        private val taskDone: CheckBox = rootView.findViewById(R.id.task_done)
        private val taskDate: TextView = rootView.findViewById(R.id.task_date)
        private val taskSelected: CheckBox = rootView.findViewById(R.id.task_selected)

        companion object {
            fun inflateFrom(
                parent: ViewGroup,
                updateTaskDone: (Task) -> Unit,
                deleteTask: (Task) -> Unit,
                adapter: TaskItemAdapter
            ): TaskItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.task_item, parent, false) as CardView
                return TaskItemViewHolder(view, updateTaskDone, deleteTask, adapter)
            }
        }

        fun bind(item: Task, isSelected: Boolean) {
            taskName.text = item.taskName
            taskDesc.text = item.taskDesc

            // Set selection state
            taskSelected.isChecked = isSelected
            taskSelected.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    adapter.selectedItems.add(item.taskId)
                } else {
                    adapter.selectedItems.remove(item.taskId)
                }
                adapter.onSelectionChanged?.invoke()
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = if (item.taskDate > 0) {
                dateFormat.format(Date(item.taskDate))
            } else {
                "No Date"
            }
            taskDate.text = formattedDate

            taskName.setOnClickListener {
                showEditDialog(item, "Edit Task Name", item.taskName, 30) { newName ->
                    item.taskName = newName
                    updateTaskDone(item)
                }
            }

            taskDesc.setOnClickListener {
                showEditDialog(item, "Edit Task Description", item.taskDesc, 250) { newDesc ->
                    item.taskDesc = newDesc
                    updateTaskDone(item)
                }
            }

            taskDate.setOnClickListener {
                showSimpleDatePicker(item)
            }

            taskDone.setOnCheckedChangeListener(null)
            taskDone.isChecked = item.taskDone
            taskDone.setOnCheckedChangeListener { _, isChecked ->
                item.taskDone = isChecked
                updateTaskDone(item)
            }

            rootView.setOnLongClickListener {
                AlertDialog.Builder(rootView.context)
                    .setTitle("Delete Task")
                    .setMessage("Do you want to delete this task?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteTask(item)
                    }
                    .setNegativeButton("No", null)
                    .show()
                true
            }
        }

        private fun showEditDialog(item: Task, title: String, currentText: String, maxLength: Int, onSave: (String) -> Unit) {
            val input = EditText(rootView.context).apply {
                setText(currentText)
                filters = arrayOf(InputFilter.LengthFilter(maxLength))
            }

            AlertDialog.Builder(rootView.context)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("OK") { _, _ ->
                    input.text.toString().trim().takeIf { it.isNotEmpty() }?.let(onSave)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun showSimpleDatePicker(task: Task) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                rootView.context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    updateTaskDone(task.copy(taskDate = selectedDate))
                    Toast.makeText(rootView.context, "Updated Task Successfully", Toast.LENGTH_SHORT).show()
                },
                year, month, day
            ).apply {
                datePicker.minDate = System.currentTimeMillis() - 1
            }.show()
        }
    }
}