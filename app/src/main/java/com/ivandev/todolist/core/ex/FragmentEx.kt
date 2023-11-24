package com.ivandev.todolist.core.ex

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ivandev.todolist.R

fun Fragment.detailAlertDialog(context: Context, name: String): AlertDialog {
    val builder = AlertDialog.Builder(context)
    val inflater = layoutInflater
    val view = inflater.inflate(R.layout.task_detail_alert_dialog, null)
    val detail = view.findViewById<TextView>(R.id.detailTitle)
    val acceptBtn = view.findViewById<Button>(R.id.acceptBtn)

    detail.text = name
    builder.setView(view)
    val alertDialog = builder.create()

    alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    acceptBtn.setOnClickListener {
        alertDialog.dismiss()
    }
    return alertDialog
}