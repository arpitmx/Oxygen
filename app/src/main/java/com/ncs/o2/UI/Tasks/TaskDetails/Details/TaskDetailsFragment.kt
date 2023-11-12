package com.ncs.o2.UI.Tasks.TaskDetails.Details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.Colors
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.Later
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskDetails.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TaskDetails.TaskDetailViewModel
import com.ncs.o2.UI.Tasks.TaskDetails.TasksDetailsHolderFragment
import com.ncs.o2.UI.Tasks.TasksHolderFragment
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.UI.UIComponents.Adapters.LinkAdapter
import com.ncs.o2.UI.UIComponents.Adapters.TagAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.ProfileBottomSheet
import com.ncs.o2.databinding.FragmentTaskDetailsFrgamentBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class TaskDetailsFragment : Fragment(), ContributorAdapter.OnProfileClickCallback {

    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    lateinit var binding: FragmentTaskDetailsFrgamentBinding
    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }
    private val tasksHolderBinding:TasksDetailsHolderFragment by lazy {
        (requireParentFragment() as TasksDetailsHolderFragment)
    }
    private val viewModel: TaskDetailViewModel by viewModels()
    lateinit var taskDetails:Task
    var tags:MutableList<Tag> = mutableListOf()
    var users:MutableList<User> = mutableListOf()
    private val TextViewList = mutableListOf<TextView>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskDetailsFrgamentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setdetails(activityBinding.taskId)

        CoroutineScope(Dispatchers.Main).launch{
            withContext(Dispatchers.Main){
                binding.activity.setOnClickThrottleBounceListener {
                    val viewpager=tasksHolderBinding.binding.viewPager2
                    val next=viewpager.currentItem+1
                    if (next < 2) {
                        viewpager.currentItem=next
                    }
                }
            }
        }

    }


    @Later("1. Check if the request has already made, if made then set text and clickability on the button accordingly")
    private fun setUpViews() {


        activityBinding.binding.gioActionbar.btnRequestWork.setOnClickSingleTimeBounceListener {

            activityBinding.binding.gioActionbar.btnRequestWork.animFadein(requireContext())
            sendRequestNotification()

        }

        handleRequestNotificationResult()

        binding.taskStatus.setOnClickThrottleBounceListener {}
        binding.duration.setOnClickThrottleBounceListener {}
        binding.difficulty.setOnClickThrottleBounceListener {}


    }

    private fun sendRequestNotification() {

        val requestNotification: Notification = createRequestNotification()
        val receiverFCMToken: String = getReceiverFCMToken()

        CoroutineScope(Dispatchers.Main).launch {
            viewModel.sendNotificationToReceiverFirebase(requestNotification, receiverFCMToken)
        }
    }

    private fun handleRequestNotificationResult() {
        viewModel.notificationStatusLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ServerResult.Failure -> {

                    binding.progressBar.gone()
                    utils.dialog("Request Failed",
                        "Try retrying as request sending was failed to server due to ${result.exception.message.toString()}",
                        getString(R.string.retry),
                        getString(R.string.cancel),
                        {
                            sendRequestNotification()
                        },
                        {}
                    )
                }

                ServerResult.Progress -> {
                    binding.progressBar.visible()
                }

                is ServerResult.Success -> {


                    activityBinding.binding.gioActionbar.btnRequestWork.isClickable = false
                    activityBinding.binding.gioActionbar.btnRequestWork.alpha = 0.7f

                    binding.progressBar.gone()
                    utils.showSnackbar(binding.root, "Request sent", 5000)
                }

                else -> {}
            }
        }
    }

    private val test_fcmtokenMI =
        "cQiVebLLTPutKWl2t_13mY:APA91bH-fGRZ06pGDDMx70JwOqB3DI_n-CDbmEzcXGMGSOXrubXSTMx63T11TYFe5WnHT3Tc-wNTcpA7hIY4moZUNzglEjL8pe5Bm21WUh_u5-TaY_mkTxm5BIVlDHfOdTCPz4hL-45F"
    private val test_fcmtokenEmulator =
        "eC7Fa4TGTMWVcNeBFMWfvA:APA91bFNEbbUG-25U7bvKLX6nESaoYhSfETLZR49jSlDeBBC4c3iRTa-iXBmVxSR75dEZCwduN6JI_AioDSqdMF_DwYYQGyDB5vGROq293D4_zyM2j1qJ43YSnJeZs65S79HfYyWZLzt"


    private fun getReceiverFCMToken(): String {
        return test_fcmtokenEmulator
    }

    private fun createRequestNotification(): Notification {
        return Notification(
            notificationID = RandomIDGenerator.generateRandomId(),
            notificationType = NotificationType.TASK_REQUEST_RECIEVED_NOTIFICATION.toString(),
            taskID = Faker().number().randomDigit().toString(),
            title = "Armax wants to work on 12345",
            message = Faker().backToTheFuture().quote().toString(),
            timeStamp = Timestamp.now().seconds,
            fromUser = Faker().funnyName().name().toString(),
            toUser = "userid1"
        )
    }

    private fun setContributors(list: MutableList<User>) {
        val contriRecyclerView = binding.contributorsRecyclerView
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        contriRecyclerView.layoutManager = layoutManager
        val adapter = ContributorAdapter(list, this, false)
        contriRecyclerView.adapter = adapter
    }

    private fun setTagsView(list: MutableList<Tag>) {
        val tagsRecyclerView = binding.tagRecyclerView
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        tagsRecyclerView.layoutManager = layoutManager
        val adapter = TagAdapter(list)
        tagsRecyclerView.adapter = adapter
    }
    private fun setLinksView(list: MutableList<String>) {
        val num=list.size
        val parentLayout = binding.linksCont
        val inflater = LayoutInflater.from(requireContext())
        TextViewList.clear()
        for (i in 0 until num) {
            val text = inflater.inflate(
                R.layout.links_item,
                parentLayout,
                false
            ) as TextView
            TextViewList.add(text)
            parentLayout.addView(text)
            text.text=list[i]
        }
        for (i in 0 until num) {
            CoroutineScope(Dispatchers.IO).launch{
                withContext(Dispatchers.Main){
                    TextViewList[i] .setOnClickThrottleBounceListener{
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TextViewList[i].text.toString()))
                        startActivity(intent)
                    }
                }
            }

        }

    }

    override fun onProfileClick(user: User, position: Int) {
        val bottomSheet = ProfileBottomSheet(user)
        bottomSheet.show(childFragmentManager, "bottomsheet")
    }

    override fun removeClick(user: User, position: Int) {
        TODO("Not yet implemented")
    }
    fun setdetails(id: String) {
        viewModel.getTasksbyId(id, PrefManager.getcurrentProject()) { result ->
            when (result) {
                is ServerResult.Success -> {
                    binding.progressBar.gone()
                    binding.scrollView2.visible()
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                            val task = result.data
                            taskDetails = task
                            setTags()
                            fetchUsers()
                            binding.titleTv.text = task.title
                            binding.descriptionTv.text = taskDetails.description
                            when (task.status) {
                                0 -> binding.taskStatus.text = "Unassigned"
                                1 -> binding.taskStatus.text = "Assigned"
                                2 -> binding.taskStatus.text = "Finished"
                            }
                            binding.duration.text = "${task.duration}Hr+"
                            when (task.difficulty) {
                                1 -> binding.difficulty.text = "Easy"
                                2 -> binding.difficulty.text = "Medium"
                                3 -> binding.difficulty.text = "Difficult"
                            }
                            val timeDifference = Date().time - task.time_STAMP!!.toDate().time
                            val minutes = (timeDifference / (1000 * 60)).toInt()
                            val hours = minutes / 60
                            val days = hours / 24

                            val timeAgo: String = when {
                                days > 0 -> "$days days ago"
                                hours > 0 -> "$hours hours ago"
                                minutes > 0 -> "$minutes minutes ago"
                                else -> "just now"
                            }
                            binding.openedBy.text = "${task.assigner} created this task $timeAgo"
                            withContext(Dispatchers.Main) {
                                if (task.links.isEmpty()) {
                                    binding.link.gone()
                                    binding.linksCont.gone()
                                }
                                if (task.links.isNotEmpty()) {
                                    binding.link.visible()
                                    binding.linksCont.visible()
                                    setLinksView(task.links.toMutableList())
                                }
                            }
                        }
                    }
                }
                is ServerResult.Failure -> {
                    val errorMessage = result.exception.message
                    binding.progressBar.gone()
                }
                is ServerResult.Progress -> {
                    binding.progressBar.visible()
                }
            }
        }
    }

    fun setTags(){
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){
                for(i in 0 until taskDetails.tags.size) {
                    viewModel.getTagsbyId(taskDetails.tags[i], PrefManager.getcurrentProject()) { result ->
                        when (result) {
                            is ServerResult.Success -> {
                                binding.progressBar.gone()
                                binding.scrollView2.visible()
                                CoroutineScope(Dispatchers.IO).launch {
                                    val tag = result.data
                                    tags.add(tag)
                                }
                                setTagsView(tags)

                            }

                            is ServerResult.Failure -> {
                                val errorMessage = result.exception.message
                                binding.progressBar.gone()
                            }

                            is ServerResult.Progress -> {
                                binding.progressBar.visible()
                            }

                        }

                    }
                }
            }
        }
    }
    fun fetchUsers(){
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){
                for(i in 0 until taskDetails.assignee.size) {
                    viewModel.getUserbyId(taskDetails.assignee[i]) { result ->
                        when (result) {
                            is ServerResult.Success -> {
                                binding.progressBar.gone()
                                binding.scrollView2.visible()
                                CoroutineScope(Dispatchers.IO).launch {
                                    val user = result.data
                                    users.add(user!!)
                                }
                                setContributors(users)
                            }

                            is ServerResult.Failure -> {
                                val errorMessage = result.exception.message
                                binding.progressBar.gone()

                            }

                            is ServerResult.Progress -> {
                                binding.progressBar.visible()

                            }
                        }
                    }
                }
            }
        }
    }
}
