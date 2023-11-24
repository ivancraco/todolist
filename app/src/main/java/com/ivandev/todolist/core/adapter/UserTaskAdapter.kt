package com.ivandev.todolist.core.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ivandev.todolist.R
import com.ivandev.todolist.databinding.ToDoItemBinding
import com.ivandev.todolist.domain.UserTask

class UserTaskAdapter(
    private val onDetail: (UserTask) -> Unit,
    private val onCheck: (UserTask) -> Unit,
    private val onClickItemListener: (Int, List<UserTask>) -> Boolean,
    private val onLongClickItemListener: (Int, List<UserTask>) -> Unit
) : RecyclerView.Adapter<UserTaskViewHolder>() {

    private val userTaskViewHolderList = linkedMapOf<String, UserTaskViewHolder>()

    private val diffCallback = object : DiffUtil.ItemCallback<UserTask>() {
        override fun areItemsTheSame(oldItem: UserTask, newItem: UserTask): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserTask, newItem: UserTask): Boolean {
            return oldItem == newItem
        }
    }

    private val _differ = AsyncListDiffer(this, diffCallback)
    val differ get() = _differ

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserTaskViewHolder {
        val binding = ToDoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserTaskViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: UserTaskViewHolder, position: Int) {
        val task = _differ.currentList[position]
        userTaskViewHolderList[task.id] = holder
        holder.bind(task, onCheck)
        setBackgroundResource(task, holder)

        holder.binding.taskLayout.setOnClickListener {
            val flag = onClickItemListener(holder.absoluteAdapterPosition, _differ.currentList)
            if (flag) {
                setBackgroundResource(task, holder)
            } else {
                onDetail(task)
            }
        }

        holder.binding.taskLayout.setOnLongClickListener {
            onLongClickItemListener(holder.absoluteAdapterPosition, _differ.currentList)
            setBackgroundResource(task, holder)
            true
        }
    }

    private fun setBackgroundResource(task: UserTask, holder: UserTaskViewHolder) {
        if (task.isSelected)
            holder.itemView.setBackgroundResource(R.drawable.item_view_selected_background)
        else
            holder.itemView.setBackgroundResource(R.drawable.item_view_background)
    }

    fun updateViewHolderItemBackground(flag: Boolean) {
        if (flag) {
            userTaskViewHolderList.forEach {
                it.value.itemView.setBackgroundResource(R.drawable.item_view_selected_background)
            }
        } else {
            userTaskViewHolderList.forEach {
                it.value.itemView.setBackgroundResource(R.drawable.item_view_background)
            }
        }
    }

    override fun getItemCount(): Int {
        return _differ.currentList.size
    }

    fun submitList(newList: List<UserTask>) {
        _differ.submitList(newList)
    }

    fun deleteViewHolder(key: String) {
        userTaskViewHolderList.remove(key)
    }
}