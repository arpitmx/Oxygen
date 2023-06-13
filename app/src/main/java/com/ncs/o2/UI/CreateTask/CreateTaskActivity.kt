package com.ncs.o2.UI.CreateTask

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.BottomSheets.UserlistBottomSheet
import com.ncs.o2.databinding.ActivityCreateTaskBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CreateTaskActivity : AppCompatActivity() {

    private val binding: ActivityCreateTaskBinding by lazy {
        ActivityCreateTaskBinding.inflate(layoutInflater)
    }

    private val viewmodel: CreateTaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val testTask = Task(
            "Test Title1",
            "Test Description1", "#12345", 1, emptyList(), 1, 1, emptyList(),
            "userid1", "01/04/2023", "01/03/2023", "3Hr+", emptyList(),
            "Versa", "Development"
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