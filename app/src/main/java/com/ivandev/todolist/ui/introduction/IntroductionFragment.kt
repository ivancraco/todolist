package com.ivandev.todolist.ui.introduction

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.ivandev.todolist.R
import com.ivandev.todolist.core.adapter.UserTaskAdapter
import com.ivandev.todolist.core.common.ISearchable
import com.ivandev.todolist.core.ex.detailAlertDialog
import com.ivandev.todolist.databinding.FragmentIntroductionBinding
import com.ivandev.todolist.domain.TaskDay
import com.ivandev.todolist.domain.UserTask
import kotlinx.coroutines.launch
import java.util.*


class IntroductionFragment : Fragment(), ISearchable {
    private lateinit var binding: FragmentIntroductionBinding
    private lateinit var adapter: UserTaskAdapter
    private lateinit var taskList: List<UserTask>
    private lateinit var noToDoMessage: View
    private lateinit var noTasksFoundMessage: View
    private lateinit var recyclerView: RecyclerView
    private var toDoDay: Number? = null
    private var taskListToDelete: MutableList<UserTask>? = null
    private var filteredList: List<UserTask>? = null
    private var selectedItems = 0
    private val viewModel: IntroductionViewModel by activityViewModels()

    companion object {
        private const val ARG_SECTTION_NUMBER = "SECTION_NUMBER"

        @JvmStatic
        fun newInstance(sectionNumber: Int): IntroductionFragment {
            return IntroductionFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (::binding.isInitialized) return binding.root
        inflateView(inflater, container)
        initUI()
        return binding.root
    }

    private fun inflateView(inflater: LayoutInflater, container: ViewGroup?) {
        binding = FragmentIntroductionBinding.inflate(inflater, container, false)
    }

    private fun initUI() {
        setBinding()
        getArgs()
    }

    private fun setBinding() {
        noToDoMessage = binding.noTaskLayout
        noTasksFoundMessage = binding.tvNoTasksFound
        recyclerView = binding.recyclerView
    }

    private fun getArgs() {
        val arg = arguments
        toDoDay = arg?.getInt(ARG_SECTTION_NUMBER)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        when (toDoDay) {
            TaskDay.TODAY.day -> {
                getTasksFromRepository(viewModel.taskTodayList)
                addSingleToDo(viewModel.taskToday)
            }
            TaskDay.TOMORROW.day -> {
                getTasksFromRepository(viewModel.taskTomList)
                addSingleToDo(viewModel.taskTomorrow)
            }
            TaskDay.OTHERS.day -> {
                getTasksFromRepository(viewModel.taskOthList)
                addSingleToDo(viewModel.taskOther)
            }
        }
    }

    private fun setAdapter() {
        taskList = listOf()
        adapter = UserTaskAdapter(
            { userTask -> taskDetail(userTask) },
            { userTask -> onCheckListener(userTask) },
            { position, userTaskList -> onClickItemListener(position, userTaskList) },
            { position, userTaskList -> onLongClickItemListener(position, userTaskList) }
        )
        recyclerView.adapter = adapter
    }

    private fun addSingleToDo(liveData: LiveData<UserTask>) {
        liveData.observe(viewLifecycleOwner) { task ->
            hiddeNoToDoMessage()
            taskList = taskList.plus(task).sortedBy { it.date.timeInMillis }
            val position = taskList.indexOf(task)
            updateAdapter(taskList)
            recyclerView.scrollToPosition(position)
        }
    }

    private fun deleteListToDo(mTaskList: List<UserTask>?) {
        if (mTaskList.isNullOrEmpty()) {
            Toast.makeText(
                activity?.applicationContext,
                getString(R.string.no_tasks_selected),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        mTaskList.forEach {
            viewModel.deleteTaskOnFireBase(it)
            WorkManager.getInstance(requireContext()).cancelWorkById(UUID.fromString(it.id))
            adapter.deleteViewHolder(it.id)
            taskList = taskList.minus(it)
            if (filteredList != null && filteredList!!.isNotEmpty()) {
                filteredList = filteredList!!.minus(it)
            }
        }
    }

    private fun taskDetail(task: UserTask) {
        val name = task.name
        detailAlertDialog(activity?.window?.context!!, name).show()
    }

    private fun getTasksFromRepository(liveData: LiveData<List<UserTask>>) {
        liveData.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                showNoToDoMessage()
            } else {
                taskList = list.sortedBy { it.date.timeInMillis }
                adapter.submitList(taskList)
            }
            liveData.removeObservers(viewLifecycleOwner)
        }
    }

    private fun onCheckListener(task: UserTask) {
        viewModel.updateCheck(task)
        viewModel.updateNotify(task)
    }

    private fun showActionMode() {
        val am = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.menu_action_mode, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                if (item == null) return false
                return when (item.itemId) {
                    R.id.delete -> {
                        deleteListToDo(taskListToDelete)
                        clearList(taskListToDelete)
                        if (filteredList != null) {
                            if (filteredList!!.isEmpty()) {
                                updateAdapter(taskList)
                                if (taskList.isEmpty()) {
                                    showNoToDoMessage()
                                }
                                cancelExpandedSearchView()
                                mode?.finish()
                                return true
                            }
                            updateAdapter(filteredList!!)
                            setSelectedItems(0)
                            setSelectedItemsInActionMode(filteredList!!.size)
                            return true
                        }
                        updateAdapter(taskList)
                        if (taskList.isEmpty()) {
                            cancelExpandedSearchView()
                            showNoToDoMessage()
                            mode?.finish()
                            return true
                        }
                        setSelectedItems(0)
                        setSelectedItemsInActionMode(taskList.size)
                        true
                    }

                    R.id.selectAll -> {
                        if (!filteredList.isNullOrEmpty()) {
                            if (taskListToDelete?.size == filteredList!!.size) {
                                taskListToDelete?.clear()
                                setUserTaskSelectedFieldTo(filteredList!!, false)
                                updateViewHolderItemTo(false)
                                setSelectedItems(0)
                                setSelectedItemsInActionMode(filteredList!!.size)
                                return true
                            }
                            clearList(taskListToDelete)
                            taskListToDelete?.addAll(filteredList!!)
                            setUserTaskSelectedFieldTo(filteredList!!, true)
                            updateViewHolderItemTo(true)
                            setSelectedItems(filteredList!!.size)
                            setSelectedItemsInActionMode(filteredList!!.size)
                            return true
                        }

                        if (taskListToDelete?.size == taskList.size) {
                            clearList(taskListToDelete)
                            setUserTaskSelectedFieldTo(taskList, false)
                            updateViewHolderItemTo(false)
                            setSelectedItems(0)
                            setSelectedItemsInActionMode(taskList.size)
                            return true
                        }

                        clearList(taskListToDelete)
                        taskListToDelete?.addAll(taskList)
                        setUserTaskSelectedFieldTo(taskList, true)
                        updateViewHolderItemTo(true)
                        setSelectedItems(taskList.size)
                        setSelectedItemsInActionMode(taskList.size)
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                lifecycleScope.launch {
                    setSelectedItems(0)
                    viewModel.actionMode = null
                    taskListToDelete = null
                    if(!isActionSearchExpanded()) {
                        if(filteredList != null) {
                            filteredList = null
                        }
                    }
                    setUserTaskSelectedFieldTo(taskList, false)
                    updateViewHolderItemTo(false)
                }
            }
        }
        actionMode(am)
    }

    private fun clearList(list: MutableList<UserTask>?) {
        if (!list.isNullOrEmpty()) {
            list.clear()
        }
    }

    private fun showNoToDoMessage() {
        if (!noToDoMessage.isVisible) {
            noToDoMessage.isVisible = true
        }
    }

    private fun hiddeNoToDoMessage() {
        if (noToDoMessage.isVisible) {
            noToDoMessage.isVisible = false
        }
    }

    private fun showNoTasksFoundMessage() {
        if (!noToDoMessage.isVisible) {
            noTasksFoundMessage.isVisible = true
        }
    }

    private fun setSelectedItems(value: Int) {
        selectedItems = value
    }

    private fun setSelectedItemsInActionMode(value: Int) {
        viewModel.actionMode!!.title =
            "$selectedItems/$value"
    }

    private fun setUserTaskSelectedFieldTo(list: List<UserTask>, value: Boolean) {
        if (value) {
            list.forEach {
                if (!it.isSelected) {
                    it.isSelected = true
                }
            }
        } else {
            list.forEach {
                if (it.isSelected) {
                    it.isSelected = false
                }
            }
        }
    }

    private fun updateViewHolderItemTo(value: Boolean) {
        adapter.updateViewHolderItemBackground(value)
    }

    private fun updateAdapter(list: List<UserTask>) {
        adapter.submitList(list)
    }

    private fun cancelExpandedSearchView() {
        val activity = activity
        if (activity is IntroductionActivity) {
            activity.cancelExpandedSearchView()
        }
    }

    private fun onClickItemListener(position: Int, mTasklist: List<UserTask>): Boolean {
        if (viewModel.actionMode == null) return false
        onClick(position, mTasklist)
        return true
    }

    private fun onClick(position: Int, mTasklist: List<UserTask>) {
        if(taskListToDelete == null) {
            taskListToDelete = mutableListOf()
        }
        val task = mTasklist[position]
        val isSelected = task.isSelected
        mTasklist[position].isSelected = !isSelected
        if (mTasklist[position].isSelected) {
            taskListToDelete!!.add(task)
            setSelectedItems(++selectedItems)
            setSelectedItemsInActionMode()
        } else {
            taskListToDelete!!.remove(task)
            setSelectedItems(--selectedItems)
            setSelectedItemsInActionMode()
        }
    }

    private fun onLongClickItemListener(position: Int, mTasklist: List<UserTask>) {
        if (viewModel.actionMode == null) {
            showActionMode()
            onClick(position, mTasklist)
        } else {
            onClick(position, mTasklist)
        }
    }

    private fun setSelectedItemsInActionMode() {
        if (filteredList.isNullOrEmpty()) {
            setSelectedItemsInActionMode(taskList.size)
        } else {
            setSelectedItemsInActionMode(filteredList!!.size)
        }
    }

    private fun isActionSearchExpanded(): Boolean {
        val activity = activity
        if (activity is IntroductionActivity) {
            return activity.isActionSearchExpanded()
        }
        return false
    }

    override fun performSearch(query: String) {
        if (query.isEmpty()) {
            updateAdapter(emptyList())
            showNoTasksFoundMessage()
            return
        }
        // There aren´t tasks in the Fragment
        if (adapter.differ.currentList.isEmpty() && noToDoMessage.isVisible) return

        filteredList = taskList.filter {
            it.name.contains(query, true)
        }
        updateAdapter(filteredList!!)
        noTasksFoundMessage.isVisible = filteredList!!.isEmpty()
    }

    override fun completeList() {
        updateAdapter(taskList)
        noTasksFoundMessage.isVisible = false
        updateViewHolderItemTo(false)
        if (filteredList != null) {
            filteredList = null
        }
    }

    override fun clearList() {
        adapter.submitList(emptyList())
        showNoTasksFoundMessage()
    }

    override fun actionMode(amc: ActionMode.Callback) {
        val activity = activity
        if (activity is IntroductionActivity) {
            viewModel.actionMode = activity.startSupportActionMode(amc)
        }
    }
}