package com.ivandev.todolist.core.common

import com.ivandev.todolist.domain.UserTask
import java.util.*

object TaskDateTime {
    fun getHourOfDay(hourOfDay: Int?): String {
        return when (hourOfDay) {
            0 -> "12"
            13 -> "1"
            14 -> "2"
            15 -> "3"
            16 -> "4"
            17 -> "5"
            18 -> "6"
            19 -> "7"
            20 -> "8"
            21 -> "9"
            22 -> "10"
            23 -> "11"
            else -> hourOfDay.toString()
        }
    }

    fun getMinute(minute: Int?): String {
        return when (minute) {
            0 -> "00"
            1 -> "01"
            2 -> "02"
            3 -> "03"
            4 -> "04"
            5 -> "05"
            6 -> "06"
            7 -> "07"
            8 -> "08"
            9 -> "09"
            else -> minute.toString()
        }
    }

    fun taskFormatDatetime(task: UserTask): String {
        val hour = getHourOfDay(task.date.get(Calendar.HOUR_OF_DAY))
        val min = getMinute(task.date.get(Calendar.MINUTE))
        val time = "$hour:$min ${task.ampm}"
        val day = task.date.get(Calendar.DAY_OF_MONTH)
        val month = task.date.get(Calendar.MONTH)
        val monthPlus = month.plus(1)
        val year = task.date.get(Calendar.YEAR)
        val date: String = if (Locale.getDefault().displayName == "English (United States)") {
            "$monthPlus/$day/$year $time"
        } else {
            "$day/$monthPlus/$year $time"
        }
        return date
    }
}