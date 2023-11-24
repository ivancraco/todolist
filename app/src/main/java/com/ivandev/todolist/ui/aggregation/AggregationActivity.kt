package com.ivandev.todolist.ui.aggregation

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognizerIntent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ivandev.todolist.R
import com.ivandev.todolist.core.common.Key
import com.ivandev.todolist.core.common.TaskDateTime
import com.ivandev.todolist.databinding.ActivityAggregationBinding
import java.util.*

class AggregationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAggregationBinding
    private lateinit var inputTaskName: TextInputEditText
    private lateinit var inputDate: TextInputEditText
    private lateinit var inputTime: TextInputEditText
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var switchCompat: SwitchCompat
    private lateinit var btnCreateTask: Button
    private lateinit var btnBackToActivity: View
    private lateinit var imm: InputMethodManager
    private var ampm = ""
    private val localDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateView()
        initUi()
    }

    private fun initUi() {
        localDate.set(Calendar.SECOND, 0)
        setBinding()
        textInputRequestFocus()
        setImputMethodManager()
        textImputLayoutIconListener()
        btnBackToActivityListener()
        btnCreateTaskListener()
        dateFieldListener()
        timeFiledLisener()
    }

    private fun textInputRequestFocus() {
        inputTaskName.requestFocus()
    }

    private fun timeFiledLisener() {
        inputTime.setOnClickListener {
            timeListener()
        }
    }

    private fun dateFieldListener() {
        inputDate.setOnClickListener {
            dateListener()
        }
    }

    private fun btnCreateTaskListener() {
        btnCreateTask.setOnClickListener {
            hideSoftInputFromWindow(inputTaskName.windowToken)

            if (inputTaskName.text.toString().trim().isEmpty() ||
                inputDate.text.toString().isEmpty() ||
                inputTime.text.toString().isEmpty()
            ) {
                Toast.makeText(this, getString(R.string.error_params), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isTimeCorrect(localDate)) return@setOnClickListener

            val intent = Intent()
            val key = Key.genKey()
            intent.putExtra(getString(R.string.task_key), key)
            intent.putExtra(getString(R.string.task_name), inputTaskName.text.toString().trim())
            intent.putExtra(getString(R.string.task_date), localDate.timeInMillis)
            intent.putExtra(getString(R.string.task_ampm), ampm)
            intent.putExtra(getString(R.string.task_notify), switchCompat.isChecked)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun hideSoftInputFromWindow(windowToken: IBinder?) {
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setImputMethodManager() {
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun btnBackToActivityListener() {
        btnBackToActivity.setOnClickListener {
            hideSoftInputFromWindow(inputTaskName.windowToken)
            finish()
        }
    }

    private fun textImputLayoutIconListener() {
        textInputLayout.setEndIconOnClickListener {
            inputTaskName.setText("")
            voiceListener()
        }
    }

    private fun setBinding() {
        inputDate = binding.date
        inputTime = binding.time
        textInputLayout = binding.taskNameLayout
        inputTaskName = binding.taskName
        switchCompat = binding.notificationSwitch
        btnCreateTask = binding.addTaskBtn
        btnBackToActivity = binding.backIcon
    }

    private fun inflateView() {
        binding = ActivityAggregationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun timeListener() {
        hideSoftInputFromWindow(inputTaskName.windowToken)
        val calendar = Calendar.getInstance()
        val hourCal = calendar.get(Calendar.HOUR_OF_DAY)
        val minuteCal = calendar.get(Calendar.MINUTE)

        val picker = TimePickerDialog(
            this@AggregationActivity,
            R.style.PickerDialog,
            { _, hourOfDay, minute ->
                val min = TaskDateTime.getMinute(minute)
                val hour = TaskDateTime.getHourOfDay(hourOfDay)

                ampm = if (hourOfDay >= 12) {
                    "PM"
                } else {
                    "AM"
                }

                localDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                localDate.set(Calendar.MINUTE, minute)

                val str = "$hour:$min $ampm"
                inputTime.setText(str)
            }, hourCal, minuteCal, false
        )

        picker.show()
    }

    private fun dateListener() {
        hideSoftInputFromWindow(inputTaskName.windowToken)
        val calendarDate = Calendar.getInstance()
        val cDay = calendarDate.get(Calendar.DAY_OF_MONTH)
        val cMonth = calendarDate.get(Calendar.MONTH)
        val cYear = calendarDate.get(Calendar.YEAR)

        val datePickerDialog = DatePickerDialog(
            this@AggregationActivity,
            R.style.PickerDialog,
            { _, year, month, dayOfMonth ->
                val str = "${dayOfMonth}/${month + 1}/${year}"

                localDate.set(year, month, dayOfMonth)

                inputDate.setText(str)
            }, cYear, cMonth, cDay
        )

        datePickerDialog.datePicker.minDate = calendarDate.timeInMillis
        datePickerDialog.show()
    }

    private fun voiceListener() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult.launch(intent)
        } else {
            Toast.makeText(this, getString(R.string.error_message), Toast.LENGTH_SHORT).show()
        }
    }

    private val startActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val spokenText: String =
                    it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        .let { results ->
                            results?.get(0) ?: ""
                        }
                inputTaskName.setText(capitalizeFirstLetter(spokenText))
                inputTaskName.setSelection(
                    inputTaskName.text.toString().length,
                    inputTaskName.text.toString().length
                )
            }
        }

    private fun isTimeCorrect(calendar: Calendar): Boolean {
        val calendarAux = Calendar.getInstance()
        if (calendar.timeInMillis < calendarAux.timeInMillis) {
            Toast.makeText(this, getString(R.string.wrong_time), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun capitalizeFirstLetter(value: String): String {
        return value[0].uppercaseChar() + value.slice(IntRange(1, value.length - 1))
    }
}