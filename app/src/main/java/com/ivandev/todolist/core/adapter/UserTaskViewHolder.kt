package com.ivandev.todolist.core.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.ivandev.todolist.R
import com.ivandev.todolist.core.common.TaskDateTime
import com.ivandev.todolist.databinding.ToDoItemBinding
import com.ivandev.todolist.domain.UserTask
import java.util.*

class UserTaskViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    val binding = ToDoItemBinding.bind(view)

    fun bind(
        task: UserTask,
        onCheck: (UserTask) -> Unit
    ) {

        binding.taskTitle.text = task.name
        binding.taskDate.text = TaskDateTime.taskFormatDatetime(task)

        binding.checkBox.setOnClickListener {
            onChecked()
            WorkManager.getInstance(view.context).cancelWorkById(UUID.fromString(task.id))
            task.made = true
            binding.checkBox.isClickable = false
            binding.notificationIcon.isVisible = false
            onCheck(task)
        }

        binding.notificationIcon.isVisible = task.notify

        if (task.made) onChecked()
        else onUnchecked()
    }

    private fun onUnchecked() {
        binding.taskTitle.setTextColor(android.graphics.Color.parseColor("#DADADA"))
        binding.taskDate.setTextColor(android.graphics.Color.GRAY)
        binding.checkBox.setBackgroundResource(R.drawable.ic_check_box_unchecked)
    }

    private fun onChecked() {
        binding.taskTitle.setTextColor(android.graphics.Color.GRAY)
        binding.taskDate.setTextColor(android.graphics.Color.GRAY)
        binding.checkBox.setBackgroundResource(R.drawable.ic_check_box_checked)
    }
}