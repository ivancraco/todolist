package com.ivandev.todolist.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.ivandev.todolist.domain.Repository
import com.ivandev.todolist.domain.UserTask
import java.util.*
import javax.inject.Inject

class RepositoryImp @Inject constructor(
    private val db: FirebaseFirestore
) : Repository {

    override fun deleteTask(userKey: String, task: UserTask) {
        db.collection(USER_DB).document(userKey)
            .collection(USER_TASKS)
            .document(task.id)
            .delete()
    }

    override fun getAllTasks(userKey: String, callback: (List<UserTask>) -> Unit) {
        val userTaskList = mutableListOf<UserTask>()
        db.collection(USER_DB).document(userKey)
            .collection(USER_TASKS)
            .get()
            .addOnSuccessListener {
                getAllTasks(it, userTaskList)
                callback(userTaskList)
            }.addOnFailureListener {
                callback(userTaskList)
            }
    }

    private fun getAllTasks(docs: QuerySnapshot, userTaskList: MutableList<UserTask>) {
        docs.forEach { doc ->
            val id = doc.get("id") as String
            val name = doc.get("name") as String
            val date = doc.get("date") as Long
            val ampm = doc.get("ampm") as String
            val made = doc.get("made") as Boolean
            val notify = doc.get("notify") as Boolean

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date

            val toDo = UserTask(id, name, calendar, ampm, made, notify, false)

            userTaskList.add(toDo)
        }
    }

    override fun saveTask(task: UserTask, userKey: String) {
        val timeInMillis = task.date.timeInMillis
        db.collection(USER_DB).document(userKey)
            .collection(USER_TASKS)
            .document(task.id)
            .set(
                hashMapOf(
                    "id" to task.id,
                    "name" to task.name,
                    "date" to timeInMillis,
                    "ampm" to task.ampm,
                    "made" to task.made,
                    "notify" to task.notify
                )
            )
    }

    override fun updateCheck(userKey: String, task: UserTask) {
        db.collection(USER_DB).document(userKey)
            .collection(USER_TASKS)
            .document(task.id)
            .update("made", true)
    }

    override fun updateNotify(userKey: String, taskKey: String) {
        db.collection(USER_DB).document(userKey)
            .collection(USER_TASKS)
            .document(taskKey)
            .update("notify", false)
    }
}