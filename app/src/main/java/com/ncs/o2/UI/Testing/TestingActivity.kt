package com.ncs.o2.UI.Testing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import com.google.firebase.Timestamp
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Constants.TestingConfig
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.Later
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.Room.NotificationRepository.NotificationDatabase
import com.ncs.o2.databinding.ActivityTestingBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.datafaker.Faker
import javax.inject.Inject
import kotlin.random.Random


@AndroidEntryPoint
class TestingActivity: AppCompatActivity() {

    @Inject
    @FirebaseRepository
    lateinit var repository: Repository

    @Inject
    lateinit var db : NotificationDatabase

    private val binding : ActivityTestingBinding by lazy {
        ActivityTestingBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        PrefManager.initialize(this)

        binding.goBtn.setOnClickThrottleBounceListener{
            binding.logs.text = "Logs will appear here..."
            val mode: Int = Integer.parseInt(binding.modeEt.text.toString())
            setMode(mode)

        }
    }

    private fun setMode(mode : Int) {
        when (mode){
            1->{
                binding.testTitleTv.text = TestingConfig.TestModes.ADD_NOTIFICATIONS.toString()
                val quantity = Integer.parseInt(binding.quantityEt.text.toString())
                setUpAddNotifications(quantity)
                binding.modeEt.text.clear()
                binding.quantityEt.text.clear()
            }
            2->{
                binding.testTitleTv.text = TestingConfig.TestModes.ADD_TASKS.toString()
                val quantity = Integer.parseInt(binding.quantityEt.text.toString())
                postTasks(quantity)
                binding.modeEt.text.clear()
                binding.quantityEt.text.clear()
            }
            3->{
                binding.testTitleTv.text = TestingConfig.TestModes.ADD_NOTIFICATION_ROOM.toString()
                val quantity = Integer.parseInt(binding.quantityEt.text.toString())

                CoroutineScope(Dispatchers.Main).launch {
                    setUpAddNotificationRoomDB(quantity)
                }

                binding.modeEt.text.clear()
                binding.quantityEt.text.clear()
            }
            else ->{
                binding.modeEt.text.clear()
                binding.quantityEt.text.clear()
                toast("Invalid mode")
            }
        }
    }

    @Later("Remove Timestamp.now()")
    private fun postTasks(quantity: Int){
        for (i in 1..quantity){
            val task= Task(
                title = Faker().howIMetYourMother().quote().toString(),
                description = Faker().howIMetYourMother().quote().toString(),
                id ="#T${i}${Random(System.currentTimeMillis()).nextInt(1000,9999)}",
                difficulty = Random(System.currentTimeMillis()).nextInt(1,4),
                priority =  Random(System.currentTimeMillis()).nextInt(1,4),
                status = Random(System.currentTimeMillis()).nextInt(0,3),
                assigner = Faker().funnyName().name().toString(),
                deadline = "${Random(System.currentTimeMillis()).nextInt(1,5)} days",
                project_ID = "NCSOxygen",
                segment = "Version 1.0", //change segments here //like Design
                section = "Section 2",  //Testing // Completed //Ready //Ongoing
                assignee_DP_URL = "https://picsum.photos/200",
                completed = false,
                duration = Random(System.currentTimeMillis()).nextInt(1,5).toString(),
                time_STAMP = Timestamp.now()
            )

            CoroutineScope(Dispatchers.Main).launch {

                repository.postTask(task) { result ->

                    when (result) {

                        is ServerResult.Failure -> {
                            binding.progress.gone()
                            binding.logs.text = binding.logs.text.toString().plus("\n> Error on task ${i}: ${result.exception.message}")
                        }

                        ServerResult.Progress -> {
                            binding.progress.visible()
                            binding.logs.text = binding.logs.text.toString().plus("\n> Running task ${i}")
                        }

                        is ServerResult.Success -> {
                            binding.progress.gone()
                            binding.logs.text = binding.logs.text.toString().plus("\n> Task ${i} is success.")
                        }

                    }
                }
            }

        }
    }

    private suspend fun setUpAddNotificationRoomDB(quantity: Int){
        binding.logs.text = binding.logs.text.toString().plus("Testing notification room.")
        val notifDAO = db.notificationDao()

        for (task in 1..quantity){

            val notification  = Notification(
                notificationID = RandomIDGenerator.generateRandomId(),
                notificationType = NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.toString(),
                taskID = Faker().number().randomDigit().toString(),
                title = "Mention",
                message = Faker().backToTheFuture().quote().toString(),
                timeStamp = Timestamp.now().seconds,
                fromUser = Faker().funnyName().name().toString(),
                toUser = PrefManager.getCurrentUserEmail()
            )

            val job = CoroutineScope(Dispatchers.IO).launch {
                notifDAO.getNotificationById("cWq4UDL%bu^i39G")
            }

            try {
                job.join()
                binding.logs.text = binding.logs.text.toString().plus("\n-> Added ${task} to Notification Database")
                binding.logs.text = binding.logs.text.toString().plus("\n-> Added at Time : ${DateTimeUtils.formatTime(notification.timeStamp)}")


            }catch (except : Exception){
                binding.logs.text = binding.logs.text.toString().plus("\n-> Failed at ${task} due to ${except.message}")
            }
        }
    }


    private fun setUpAddNotifications(quantity : Int) {
        binding.logs.text = binding.logs.text.toString().plus("Sending to ${PrefManager.getCurrentUserEmail()}")

        for (task in 1..quantity){

            val notification  = Notification(
                notificationID = RandomIDGenerator.generateRandomId(),
                notificationType = NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.toString(),
                taskID = Faker().number().randomDigit().toString(),
                title = "Mention",
                message = Faker().backToTheFuture().quote().toString(),
                timeStamp = Timestamp.now().seconds,
                fromUser = Faker().funnyName().name().toString(),
                toUser = PrefManager.getCurrentUserEmail()
                )


            CoroutineScope(Dispatchers.Main).launch {

                repository.postNotification(notification) { result ->

                    when (result) {

                        is ServerResult.Failure -> {
                            binding.progress.gone()
                            binding.logs.text = binding.logs.text.toString().plus("\n> Error on task ${task}: ${result.exception.message}")
                        }

                        ServerResult.Progress -> {
                            binding.progress.visible()
                            binding.logs.text = binding.logs.text.toString().plus("\n> Running task ${task}")
                        }

                        is ServerResult.Success -> {
                            binding.progress.gone()
                            binding.logs.text = binding.logs.text.toString().plus("\n> Task ${task} is success.")
                        }

                    }
                }
            }

        }

    }
}