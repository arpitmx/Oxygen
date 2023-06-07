package com.ncs.o2.UI.CreateTask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.BottomSheets.UserlistBottomSheet
import com.ncs.o2.databinding.ActivityCreateTaskBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import kotlin.random.Random


@AndroidEntryPoint
class CreateTaskActivity : AppCompatActivity() {

    private val binding:ActivityCreateTaskBinding by lazy {
        ActivityCreateTaskBinding.inflate(layoutInflater)
    }

    private val viewmodel : CreateTaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val testTask = Task(
            "Test Title1",
            "Test Description1", "#12345", 1, emptyList(),1,1, emptyList(),
            "userid1","01/04/2023", "01/03/2023","3Hr+", emptyList(),
            "Versa","Development"
        )

        binding.duration.setOnClickThrottleBounceListener{
            Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
            viewmodel.createTask(testTask)
        }

        binding.addContributorsBtn.setOnClickThrottleBounceListener{
            val userListBottomSheet = UserlistBottomSheet()
            userListBottomSheet.show(supportFragmentManager,"userlist")
        }
    }


}