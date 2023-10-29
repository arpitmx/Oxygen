package com.ncs.o2.UI.Tasks.TaskDetails

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.firestore.FieldValue
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.UI.UIComponents.Adapters.TagAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.ProfileBottomSheet
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Utility.Colors
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.Later
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.databinding.ActivityTaskDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.datafaker.Faker
import javax.inject.Inject


@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity(), ContributorAdapter.OnProfileClickCallback {

    @Inject
    lateinit var utils : GlobalUtils.EasyElements

    private val binding: ActivityTaskDetailBinding by lazy {
        ActivityTaskDetailBinding.inflate(layoutInflater)
    }

    private val viewModel: TaskDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpViews()


    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
    }

    @Later("1. Check if the request has already made, if made then set text and clickability on the button accordingly")
    private fun setUpViews() {


        setTagsView()
        setContributors()
        setActionbar()

        binding.gioActionbar.btnRequestWork.setOnClickSingleTimeBounceListener {

            binding.gioActionbar.btnRequestWork.animFadein(this)
            sendRequestNotification()

        }

        handleRequestNotificationResult()

        binding.taskStatus.setOnClickThrottleBounceListener {}
        binding.duration.setOnClickThrottleBounceListener {}
        binding.difficulty.setOnClickThrottleBounceListener {}

        binding.gioActionbar.btnBack.setOnClickThrottleBounceListener {
            onBackPressed()
        }
    }

    private fun sendRequestNotification() {

        val requestNotification : Notification = createRequestNotification()
        val receiverFCMToken : String = getReceiverFCMToken()

        CoroutineScope(Dispatchers.Main).launch {
            viewModel.sendNotificationToReceiverFirebase(requestNotification, receiverFCMToken)
        }
    }
    private fun handleRequestNotificationResult() {
        viewModel.notificationStatusLiveData.observe(this){ result ->
            when(result){
                is ServerResult.Failure -> {

                    binding.progressBar.gone()
                    utils.dialog("Request Failed",
                        "Try retrying as request sending was failed to server due to ${result.exception.message.toString()}",
                        getString(R.string.retry),
                        getString(R.string.cancel)
                        ,
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


                    binding.gioActionbar.btnRequestWork.isClickable = false
                    binding.gioActionbar.btnRequestWork.alpha= 0.7f

                    binding.progressBar.gone()
                    utils.showSnackbar(binding.root,"Request sent", 5000)
                }
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
            notificationType = NotificationType.TASK_REQUEST_RECIEVED_NOTIFICATION.toString(),
            taskID = Faker().number().randomDigit().toString(),
            title = "Armax wants to work on 12345",
            message = Faker().backToTheFuture().quote().toString(),
            timeStamp = FieldValue.serverTimestamp(),
            fromUser = Faker().funnyName().name().toString(),
            toUser = "userid1"
        )
    }


    private fun setActionbar() {
        binding.gioActionbar.titleTv.text = getString(R.string.details)
        binding.gioActionbar.doneItem.gone()
    }

    private fun setContributors() {

        val contriRecyclerView = binding.contributorsRecyclerView
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        contriRecyclerView.layoutManager = layoutManager


        val dataList = listOf(
            User("https://yt3.googleusercontent.com/xIPexCvioEFPIq_nuEOOsv129614S3K-AblTK2P1L9GvVIZ6wmhz7VyCT-aENMZfCzXU-qUpaA=s900-c-k-c0x00ffffff-no-rj"),
            User("https://hips.hearstapps.com/hmg-prod/images/apple-ceo-steve-jobs-speaks-during-an-apple-special-event-news-photo-1683661736.jpg?crop=0.800xw:0.563xh;0.0657xw,0.0147xh&resize=1200:*"),
            User("https://picsum.photos/200"),
            User("https://picsum.photos/300"),
            User("https://picsum.photos/350"),
            User("https://picsum.photos/450"),
            User("https://picsum.photos/230"),
            User("https://picsum.photos/231"),
            User("https://picsum.photos/202"),
            User("https://picsum.photos/234")
        )

        val adapter = ContributorAdapter(dataList as MutableList<User>, this,false)
        contriRecyclerView.adapter = adapter
    }

    private fun setTagsView() {

        val tagsRecyclerView = binding.tagRecyclerView
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        tagsRecyclerView.layoutManager = layoutManager

        val dataList = listOf(
            Tag("Critical", Colors.WHITE, Colors.BLACK,""),
            Tag("Bug", Colors.RED, Colors.WHITE,""),
            Tag("Feature", Colors.BLUE, Colors.WHITE,""),
            Tag("New", Colors.GREEN, Colors.BLACK,""),
            Tag("Critical", Colors.WHITE, Colors.BLACK,""),

            )
        val adapter = TagAdapter(dataList)
        tagsRecyclerView.adapter = adapter
    }

    override fun onProfileClick(user: User, position: Int) {
        val bottomSheet = ProfileBottomSheet(user.profileDPUrl)
        bottomSheet.show(supportFragmentManager, "bottomsheet")
    }

    override fun removeClick(user: User, position: Int) {
        TODO("Not yet implemented")
    }


}

