package com.ivandev.todolist.ui.introduction

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivandev.todolist.data.USER_KEY
import com.ivandev.todolist.domain.Repository
import com.ivandev.todolist.domain.UserTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class IntroductionViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private lateinit var _userKey: String
    lateinit var dateTime: Calendar
    val userKey: String get() = _userKey
    var actionMode: androidx.appcompat.view.ActionMode? = null

    private val _toDoList = MutableLiveData<List<UserTask>>()
    val toDoList: LiveData<List<UserTask>> get() = _toDoList

    private val _dataReady = MutableLiveData<Boolean>()
    val dataReady: LiveData<Boolean> get() = _dataReady
    private val _keyReady = MutableLiveData<Boolean>()
    val keyReady: LiveData<Boolean> get() = _keyReady

    /* App */
    private val _taskToday = MutableLiveData<UserTask>()
    val taskToday: LiveData<UserTask> get() = _taskToday
    private val _taskTomorrow = MutableLiveData<UserTask>()
    val taskTomorrow: LiveData<UserTask> get() = _taskTomorrow
    private val _taskOther = MutableLiveData<UserTask>()
    val taskOther: LiveData<UserTask> get() = _taskOther

    /* FireBase */
    private val _taskTodayList = MutableLiveData<List<UserTask>>()
    val taskTodayList: LiveData<List<UserTask>> get() = _taskTodayList
    private val _taskTomList = MutableLiveData<List<UserTask>>()
    val taskTomList: LiveData<List<UserTask>> get() = _taskTomList
    private val _taskOthList = MutableLiveData<List<UserTask>>()
    val taskOthList: LiveData<List<UserTask>> get() = _taskOthList

    fun onCreate(dataStore: DataStore<Preferences>) {
        _dataReady.postValue(false)
        viewModelScope.launch {
            getKey(dataStore)
        }
    }

    private suspend fun getKey(dataStore: DataStore<Preferences>) {
        val preferenceKey = dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(USER_KEY)].orEmpty()
        }

        preferenceKey.collect { key ->
            if (key.isEmpty()) {
                // generate a new random key
                _userKey = UUID.randomUUID().toString()
                dataStore.edit { preferences ->
                    preferences[stringPreferencesKey(USER_KEY)] = userKey
                }
            } else {
                _userKey = key
            }
            _keyReady.postValue(true)
        }
    }

    fun cancelActionMode() {
        if (actionMode != null) {
            actionMode!!.finish()
        }
    }

    fun setDateTime() {
        dateTime = Calendar.getInstance()
    }

    fun getAllTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllTasks(userKey) {
                setListToDo(it)
            }
        }
    }

    private fun setListToDo(list: List<UserTask>) {
        _toDoList.postValue(list)
        _dataReady.postValue(true)
    }

    fun addTaskOnFireBase(task: UserTask) {
        repository.saveTask(task, userKey)
    }

    fun deleteTaskOnFireBase(task: UserTask) {
        repository.deleteTask(userKey, task)
    }

    fun updateCheck(task: UserTask) {
        repository.updateCheck(userKey, task)
    }

    fun updateNotify(task: UserTask) {
        repository.updateNotify(userKey, task.id)
    }

    fun updateToday(task: UserTask) {
        _taskToday.postValue(task)
    }

    fun updateTodList(taskList: List<UserTask>) {
        _taskTodayList.postValue(taskList)
    }

    fun updateTomorrow(task: UserTask) {
        _taskTomorrow.postValue(task)
    }

    fun updateTomList(taskList: List<UserTask>) {
        _taskTomList.postValue(taskList)
    }

    fun updateOther(task: UserTask) {
        _taskOther.postValue(task)
    }

    fun updateOthList(list: List<UserTask>) {
        _taskOthList.postValue(list)
    }
}