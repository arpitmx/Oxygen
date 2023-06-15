package com.ncs.o2.UI.CreateTask

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.BottomSheets.UserlistBottomSheet
import com.ncs.o2.databinding.ActivityCreateTaskBinding
import dagger.hilt.android.AndroidEntryPoint
import net.datafaker.Faker
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class CreateTaskActivity : AppCompatActivity() {

    private val binding: ActivityCreateTaskBinding by lazy {
        ActivityCreateTaskBinding.inflate(layoutInflater)
    }

    private val viewmodel: CreateTaskViewModel by viewModels()
    private val easyElements : GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }

    @Inject lateinit var calendar : Calendar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val testTask = Task(
            Faker().animal().scientificName().toString(),
            Faker().code().asin(), ID= "", 1, emptyList(), 1, 1, emptyList(),
            "userid1", "01/04/2023", DURATION = "3Hr+", PROJECT_ID =  "Versa", SEGMENT = "Development", SECTION = "TaskSection4",
            )

       // Activity -> Viewmodel -> PostUsecase + GetUsecase -> Repository(DB)-> Firestore db


        binding.duration.setOnClickThrottleBounceListener {
            Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
            viewmodel.createTask(testTask)
        }

        binding.addContributorsBtn.setOnClickThrottleBounceListener {
            val userListBottomSheet = UserlistBottomSheet()
            userListBottomSheet.show(supportFragmentManager, "userlist")
        }

        setUpViews()
    }

    private fun setUpViews() {
        setUpActionBar()
        setUpDatePicker()
        setUpClickListeners()
        setUpCallbacks()
    }

    private fun setUpCallbacks() {
        viewmodel.serverExceptionLiveData.observe(this){exceptionMsg->
            easyElements.dialog("Server error",exceptionMsg,{},{})
        }
    }

    private fun setUpClickListeners() {
        binding.startDate.setOnClickThrottleBounceListener {
            startdatePickerDialog.show()
        }

        binding.endDate.setOnClickThrottleBounceListener{
            enddatePickerDialog.show()
        }
    }


    lateinit var startdatePickerDialog : DatePickerDialog
    lateinit var enddatePickerDialog : DatePickerDialog
    private fun setUpDatePicker() {

        startdatePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Update the calendar with the selected date
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Update the text on the button with the selected date
                val selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
                binding.startDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        enddatePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Update the calendar with the selected date
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Update the text on the button with the selected date
                val selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
                binding.endDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )


        startdatePickerDialog.datePicker.minDate = calendar.timeInMillis
        enddatePickerDialog.datePicker.minDate = calendar.timeInMillis

    }

    private fun setUpActionBar() {

        binding.gioActionbar.titleTv.visible()
        binding.gioActionbar.titleTv.text = getString(R.string.new_task)

        binding.gioActionbar.btnBack.setOnClickThrottleBounceListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
    }


}