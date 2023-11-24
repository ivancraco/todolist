package com.ivandev.todolist.core.common

import androidx.appcompat.view.ActionMode

interface ISearchable {
    fun performSearch(query: String)
    fun clearList()
    fun completeList()
    fun actionMode(amc: ActionMode.Callback)
}