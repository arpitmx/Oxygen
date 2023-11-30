package com.ncs.o2.UI.Tasks.TaskPage.Details

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import br.tiagohm.markdownview.css.InternalStyleSheet
import br.tiagohm.markdownview.css.styles.Github
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.Timestamp
import com.ncs.o2.Constants.Errors
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.loadProfileImg
import com.ncs.o2.Domain.Utility.ExtensionsUtil.runDelayed
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.Later
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailViewModel
import com.ncs.o2.UI.Tasks.TaskPage.TasksDetailsHolderFragment
import com.ncs.o2.UI.UIComponents.BottomSheets.AssigneeListBottomSheet
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.UI.UIComponents.Adapters.TagAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.BottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.ProfileBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.Userlist.UserlistBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.ModeratorsBottomSheet
import com.ncs.o2.databinding.FragmentTaskDetailsFrgamentBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject


@AndroidEntryPoint
class TaskDetailsFragment : Fragment(), ContributorAdapter.OnProfileClickCallback,
    ImageAdapter.ImagesListner, AssigneeListBottomSheet.getassigneesCallback,
    AssigneeListBottomSheet.updateAssigneeCallback, BottomSheet.SendText,
    ModeratorsBottomSheet.getContributorsCallback {
    ImageAdapter.ImagesListner, AssigneeListBottomSheet.getassigneesCallback,
    AssigneeListBottomSheet.updateAssigneeCallback,BottomSheet.SendText, ModeratorsBottomSheet.getContributorsCallback{

    @Inject
    lateinit var utils: GlobalUtils.EasyElements

    private var _binding: FragmentTaskDetailsFrgamentBinding? = null
    private val binding get() = _binding!!
    private var moderators: MutableList<User> = mutableListOf()
    var moderatorsList: MutableList<String> = mutableListOf()

    private var OList: MutableList<User> = mutableListOf()
    private val selectedAssignee: MutableList<User> = mutableListOf()

    lateinit var adapter: ContributorAdapter

    private var contributorList: MutableList<String> = mutableListOf()
    private var contributorDpList: MutableList<String> = mutableListOf()
    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }

    private val tasksHolderBinding: TasksDetailsHolderFragment by lazy {
        (requireParentFragment() as TasksDetailsHolderFragment)
    }

    private var isModerator: Boolean = false

    private var list: MutableList<String> = mutableListOf()

    private val viewModel: TaskDetailViewModel by viewModels()
    private lateinit var taskDetails: Task

    var tags: MutableList<Tag> = mutableListOf()
    var users: MutableList<User> = mutableListOf()
    private val TextViewList = mutableListOf<TextView>()

    private lateinit var markwon: Markwon
    private lateinit var mdEditor: MarkwonEditor
    private lateinit var activityViewListner: ViewVisibilityListner

    companion object {
        const val TAG = "TaskDetailsFragment"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailsFrgamentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is ViewVisibilityListner) {
            activityViewListner = context
        } else {
            throw ClassCastException("$context must implement DataPassListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews()
        runDelayed(100) {
            setDetails(activityBinding.taskId)
        }
        binding.assignee.isEnabled = false
        binding.activity.setOnClickThrottleBounceListener {
            val viewpager = tasksHolderBinding.binding.viewPager2
            val next = viewpager.currentItem + 1
            if (next < 2) {
                viewpager.currentItem = next
            }
        }
        binding.taskDetailLinLay.setOnClickThrottleBounceListener {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }


    @Later("1. Check if the request has already made, if made then set text and clickability on the button accordingly")
    private fun setUpViews() {
        val currentUser = PrefManager.getcurrentUserdetails()

        binding.progressBar.visible()

        activityBinding.binding.gioActionbar.btnRequestWork.setOnClickSingleTimeBounceListener {
            activityBinding.binding.gioActionbar.btnRequestWork.animFadein(requireContext())
            sendRequestNotification()
        }

        handleRequestNotificationResult()

        activityBinding.binding.gioActionbar.btnModerator.setOnClickThrottleBounceListener {
            toast("You are a moderator, can edit this task")
        }
        binding.assignee.setOnClickThrottleBounceListener {
            Log.d("assigne", "passing this list ${selectedAssignee.toString()}")
            val assigneeListBottomSheet =
                AssigneeListBottomSheet(OList, selectedAssignee, this, this)
            assigneeListBottomSheet.show(requireFragmentManager(), "assigneelist")
        }

        binding.addContributorsBtn.setOnClickThrottleBounceListener {
            for (i in 0 until moderators.size) {
                if (moderatorsList.contains(moderators[i].firebaseID)) {
                    moderators[i].isChecked = true
                }
            }
            val moderatorsListBottomSheet = ModeratorsBottomSheet(moderators, this)
            moderatorsListBottomSheet.show(requireFragmentManager(), "contributer list")
        }

        binding.becomeModerator.setOnClickThrottleBounceListener {
            binding.becomeModerator.gone()
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        viewModel.updateModerators(
                            taskID = activityBinding.taskId,
                            projectName = PrefManager.getcurrentProject(),
                            moderator = currentUser.EMAIL
                        )

                    }

                    when (result) {

                        is ServerResult.Failure -> {

                            utils.singleBtnDialog(
                                "Failure",
                                "Failure in Updating: ${result.exception.message}",
                                "Okay"
                            ) {
                                requireActivity().finish()
                            }
                            binding.becomeModerator.visible()
                            binding.progressBar.gone()

                        }

                        is ServerResult.Progress -> {
                            binding.progressBar.visible()
                        }

                        is ServerResult.Success -> {
                            activityBinding.binding.gioActionbar.btnModerator.visible()
                            binding.progressBar.gone()
                            toast("You are now a moderator")
                            requireActivity().recreate()

                        }

                    }

                } catch (e: Exception) {

                    Timber.tag(TAG).e(e)
                    binding.progressBar.gone()

                }
            }

        }

    }

    private fun setDefaultViews(task: Task) {


        binding.projectNameET.text = task.project_ID
        viewModel.task = task
        val priority = when (task.priority) {
            1 -> "Low"
            2 -> "Medium"
            3 -> "High"
            4 -> "Critical"
            else -> "Undefined"
        }

        val type = when (task.type) {
            1 -> "Bug"
            2 -> "Feature"
            3 -> "Feature request"
            4 -> "Task"
            5 -> "Exception"
            6 -> "Security"
            7 -> "Performance"
            else -> "Undefined"
        }
        val status = when (task.status) {
            1 -> "Submitted"
            2 -> "Open"
            3 -> "Working"
            4 -> "Review"
            5 -> "Completed"
            else -> "Undefined"
        }
        val difficulty = when (task.difficulty) {
            1 -> "Easy"
            2 -> "Medium"
            3 -> "Hard"
            else -> "Undefined"
        }

        //Priority
        binding.priorityInclude.tagIcon.text = priority.substring(0, 1)
        binding.priorityInclude.tagText.text = priority

        //Type task
        binding.typeInclude.tagIcon.text = type.substring(0, 1)
        binding.typeInclude.tagText.text = type

        // Assigner
        fetchUserbyId(task.assigner) {
            binding.asigneerDp.loadProfileImg(it?.profileDPUrl.toString())
        }

        // Assignee
        if (task.assignee != Endpoints.TaskDetails.EMPTY_MODERATORS) {

            fetchUserbyId(task.assignee) { user ->
                user?.isChecked = true
                selectedAssignee.add(user!!)
                Timber.tag("assigne").d(selectedAssignee.toString())

                binding.assigneeInclude.tagIcon.loadProfileImg(user.profileDPUrl.toString())



                binding.assigneeInclude.tagIcon.loadProfileImg(user.profileDPUrl.toString())
                binding.assigneeInclude.normalET.text = user.username

                //Add assignee to Notification receiving list
                pushToReceiver(user)

            }
        } else {

            binding.assigneeInclude.tagIcon.setImageResource(R.drawable.profile_pic_placeholder)
            binding.assigneeInclude.normalET.text = "Unassigned"
        }

        //State
        binding.stateInclude.tagIcon.text = status.substring(0, 1)
        binding.stateInclude.tagText.text = status

        //Difficulty
        binding.difficultyInclude.tagIcon.text = difficulty.substring(0, 1)
        binding.difficultyInclude.tagText.text = difficulty

        when (task.difficulty) {
            1 -> binding.difficultyInclude.tagIcon.background =
                resources.getDrawable(R.drawable.label_cardview_green)

            2 -> binding.difficultyInclude.tagIcon.background =
                resources.getDrawable(R.drawable.label_cardview_yellow)

            3 -> binding.difficultyInclude.tagIcon.background =
                resources.getDrawable(R.drawable.label_cardview_red)
        }

        //Task duration
        binding.taskDurationET.text = task.duration
    }


    private fun sendRequestNotification() {

        val requestNotification: Notification = createRequestNotification()
        val receiverFCMToken: String = getReceiverFCMToken()

        CoroutineScope(Dispatchers.Main).launch {
            viewModel.sendNotificationToReceiverFirebase(
                requestNotification,
                receiverFCMToken
            )
        }
    }

    private fun manageState(task: Task) {
        if (PrefManager.getcurrentUserdetails().EMAIL == task.assignee) {
            binding.status.setOnClickThrottleBounceListener {
                list.clear()
                list.addAll(listOf("Assigned", "Working", "Review"))
                val priorityBottomSheet =
                    BottomSheet(list, "STATE", this)
                priorityBottomSheet.show(requireFragmentManager(), "STATE")
            }
        } else if (isModerator) {
            binding.status.setOnClickThrottleBounceListener {
                list.clear()
                list.addAll(listOf("Submitted", "Open", "Working", "Review", "Completed"))
                val priorityBottomSheet =
                    BottomSheet(list, "STATE", this)
                priorityBottomSheet.show(requireFragmentManager(), "STATE")
            }
        }
    }

    private fun manageAddModeratorsView(isModerator: Boolean) {
        if (isModerator) {
            binding.addContributorsBtn.visible()
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
                        {})
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

        for (moderator in list) {
            if (moderator.firebaseID == PrefManager.getcurrentUserdetails().EMAIL) {
                isModerator = true
                binding.assignee.isEnabled = true
                activityBinding.binding.gioActionbar.btnModerator.visible()
            }
            manageState(taskDetails)
            manageAddModeratorsView(isModerator)
        }
        val contriRecyclerView = binding.contributorsRecyclerView
        contriRecyclerView.visible()
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        contriRecyclerView.layoutManager = layoutManager

        adapter = ContributorAdapter(list, this, false)
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
        val num = list.size
        val parentLayout = binding.linksCont
        val inflater = LayoutInflater.from(requireContext())
        TextViewList.clear()

        for (i in 0 until num) {
            val text = inflater.inflate(
                R.layout.links_item, parentLayout, false
            ) as TextView
            TextViewList.add(text)
            parentLayout.addView(text)
            text.text = list[i]
        }

        for (i in 0 until num) {

            TextViewList[i].setOnClickThrottleBounceListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TextViewList[i].text.toString()))
                startActivity(intent)
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


    fun setTaskDetails(task: Task) {

        binding.progressBar.gone()
        binding.parentScrollview.visible()

        taskDetails = task
        setTags()
        fetchUsers()

        binding.titleTv.text = task.title

        runDelayed(500) {
            setUpTaskDescription(taskDetails.description)
        }

        setCreator(task)
    }


    val script = """
    var allPreTags = document.querySelectorAll('pre');

    allPreTags.forEach(function(preTag) {
      preTag.addEventListener('click', function() {
        var clickedText = preTag.textContent;
        var languageType = preTag.getAttribute('language');
        send.sendCode(clickedText, languageType);
       
      });
    });
    
    var allImgTags = document.querySelectorAll('img');
    var imgArray = [];

    allImgTags.forEach(function(imgTag) {
        if (imgTag.tagName.toLowerCase() === 'img' && imgTag.parentElement.tagName.toLowerCase() !== 'pre') {
        imgTag.addEventListener('click', function() {
            send.sendsingleImage(imgTag.src);
        });
        imgArray.push(imgTag.src);
    }
    });

    send.sendImages(imgArray);

"""


    private fun fetchUsers() {

        Timber.d("Moderators list : ${taskDetails.moderators}")

        if (taskDetails.moderators.isEmpty()) {
            //toast("Contributor empty")
            binding.contributorsRecyclerView.gone()
            binding.noContributors.visible()

        } else if (taskDetails.moderators[0] == Endpoints.TaskDetails.EMPTY_MODERATORS) {
            // toast("Contributor not empty None")
            binding.contributorsRecyclerView.gone()
            binding.noContributors.visible()

        } else {

            binding.contributorsRecyclerView.visible()
            binding.noContributors.gone()

            for (contributors in taskDetails.moderators) {

                viewModel.getUserbyId(contributors) { result ->

                    when (result) {

                        is ServerResult.Success -> {
                            binding.progressBar.gone()
                            binding.parentScrollview.visible()

                            val user = result.data
                            users.add(user!!)
                            moderators.add(user)
                            moderatorsList.add(user.firebaseID!!)
                            setContributors(users)
                        }

                        is ServerResult.Failure -> {

                            utils.singleBtnDialog(
                                "Failure",
                                "Failure in fetching Moderators : ${result.exception.message}",
                                "Okay"
                            ) {
                                requireActivity().finish()
                            }
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




    interface ViewVisibilityListner {
        fun showProgressbar(show: Boolean)
    }

    private fun setUpTaskDescription(description: String) {

        val css: InternalStyleSheet = Github()

        with(css) {
            addFontFace(
                "o2font",
                "normal",
                "normal",
                "normal",
                "url('file:///android_res/font/sfregular.ttf')"
            )
            addRule("body", "font-family:o2font")
            addRule("body", "font-size:16px")
            addRule("body", "line-height:21px")
            addRule("body", "background-color: #222222")
            addRule("body", "color: #fff")
            addRule("body", "padding: 0px 0px 0px 0px")
            addRule("a", "color: #86ff7c")
            addRule("pre", "border: 1px solid #000;")
            addRule("pre", "border-radius: 4px;")
            addRule("pre", "max-height: 400px;")
            addRule("pre", "overflow:auto")
            addRule("pre", "white-space: pre-line")

        }

        binding.markdownView.settings.javaScriptEnabled = true
        binding.markdownView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        binding.markdownView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)

        binding.markdownView.addStyleSheet(css)
        binding.markdownView.addJavascriptInterface(AndroidToJsInterface(), "send")

        binding.markdownView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.evaluateJavascript(script) {}
                activityViewListner.showProgressbar(false)
            }


//            override fun shouldInterceptRequest(
//                view: WebView?,
//                request: WebResourceRequest?
//            ): WebResourceResponse? {
//
//                val url = request?.url.toString()
//
//                if (url == null) {
//                    return super.shouldInterceptRequest(view, url as String)
//                }
//                return if (url.toLowerCase(Locale.ROOT)
//                        .contains(".jpg") || url.toLowerCase(Locale.ROOT).contains(".jpeg")
//                ) {
//                    val bitmap =
//                        Glide.with(view!!.rootView).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .load(url).submit().get()
//                    WebResourceResponse(
//                        "image/jpg", "UTF-8", getBitmapInputStream(
//                            bitmap,
//                            Bitmap.CompressFormat.JPEG
//                        )
//                    )
//                } else if (url.toLowerCase(Locale.ROOT).contains(".png")) {
//                    val bitmap =
//                        Glide.with(view!!.rootView).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .load(url).submit().get()
//                    WebResourceResponse(
//                        "image/png", "UTF-8", getBitmapInputStream(
//                            bitmap,
//                            Bitmap.CompressFormat.PNG
//                        )
//                    )
//                } else if (url.toLowerCase(Locale.ROOT).contains(".webp")) {
//                    val bitmap =
//                        Glide.with(view!!.rootView).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .load(url).submit().get()
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        WebResourceResponse(
//                            "image/webp", "UTF-8", getBitmapInputStream(
//                                bitmap,
//                                Bitmap.CompressFormat.WEBP_LOSSY
//                            )
//                        )
//                    } else {
//                        WebResourceResponse(
//                            "image/webp", "UTF-8", getBitmapInputStream(
//                                bitmap,
//                                Bitmap.CompressFormat.PNG
//                            )
//                        )
//                    }
//                } else {
//                    super.shouldInterceptRequest(view, url)
//                }
//
//
//            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val intent = Intent(Intent.ACTION_VIEW, request?.url)
                startActivity(intent)
                return true
            }

        }

        binding.markdownView.loadMarkdown(description)

        binding.descriptionProgressbar.gone()
        binding.markdownView.visible()
        //binding.markdownView.animFadein(requireActivity(), 500)

    }

    private fun getBitmapInputStream(
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat
    ): InputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(compressFormat, 80, byteArrayOutputStream)
        val bitmapData: ByteArray = byteArrayOutputStream.toByteArray()
        return ByteArrayInputStream(bitmapData)
    }


    inner class AndroidToJsInterface {
        @JavascriptInterface
        fun sendCode(codeText: String, language: String?) {
            requireActivity().runOnUiThread {

                val codeViewerIntent =
                    Intent(requireActivity(), CodeViewerActivity::class.java)
                codeViewerIntent.putExtra(
                    Endpoints.CodeViewer.CODE,
                    codeText.trimIndent().trim()
                )
                codeViewerIntent.putExtra(
                    Endpoints.CodeViewer.LANG,
                    language?.trimIndent()?.trim()
                )

                startActivity(codeViewerIntent)
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_left
                )
            }
        }

        @JavascriptInterface
        fun sendImages(imageUrls: Array<String>) {
            requireActivity().runOnUiThread {
                val recyclerView = binding.imageRecyclerView
                recyclerView.layoutManager =
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                Log.d("list", imageUrls.toMutableList().toString())
                val adapter =
                    ImageAdapter(imageUrls.toMutableList(), this@TaskDetailsFragment)
                recyclerView.adapter = adapter
            }
        }

        @JavascriptInterface
        fun sendsingleImage(imageUrl: String) {
            requireActivity().runOnUiThread {
                onImageClicked(0, mutableListOf(imageUrl))
            }
        }

    }


    private fun setCreator(task: Task) {

        val timeAgo = DateTimeUtils.getTimeAgo(task.time_STAMP!!.seconds)
        fetchUserbyId(task.assigner) {

            val fullText = "${it?.username} opened this $timeAgo"
            val spannableString = SpannableString(fullText)

            val startIndex = 0
            val endIndex = startIndex + it?.username?.length!!

            //Color
            val colorSpan = ForegroundColorSpan(resources.getColor(R.color.primary))
            spannableString.setSpan(
                colorSpan,
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // Bold
//            spannableString.setSpan(
//                StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
            binding.openedBy.text = spannableString

            //Adding to NotificationReceiverList
            pushToReceiver(it)

        }
    }

    fun pushToReceiver(user: User) {
        user.fcmToken?.let { token ->
            if (token != Errors.AccountErrors.ACCOUNT_FIELDS_NULL.code) {
                activityBinding.sharedViewModel.pushReceiver(token)
            }
        }
    }

    private fun setDetails(id: String) {

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val taskResult = withContext(Dispatchers.IO) {
                    viewModel.getTasksById(id, PrefManager.getcurrentProject())
                }

                Timber.tag(TAG).d("Fetched task result : ${taskResult}")

                when (taskResult) {

                    is ServerResult.Failure -> {

                        utils.singleBtnDialog(
                            "Failure",
                            "Failure in task fetching : ${taskResult.exception.message}",
                            "Okay"
                        ) {
                            requireActivity().finish()
                        }

                        binding.progressBar.gone()

                    }

                    is ServerResult.Progress -> {
                        binding.progressBar.visible()
                    }

                    is ServerResult.Success -> {

                        binding.progressBar.gone()
                        setDefaultViews(taskResult.data)
                        setTaskDetails(taskResult.data)

                        val currentUser = PrefManager.getcurrentUserdetails()

                        if (!taskResult.data.moderators.contains(currentUser.EMAIL) && currentUser.ROLE >= 2 && taskResult.data.moderators.isEmpty()) {
                            binding.becomeModerator.visible()
                        }

                    }

                }

            } catch (e: Exception) {

                Timber.tag(TAG).e(e)
                binding.progressBar.gone()

//                utils.singleBtnDialog(
//                    "Failure", "Failure in Task exception : ${e.message}", "Okay"
//                ) {
//                    requireActivity().finish()
//                }

            }

        }
    }


    private fun setTags() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                for (i in 0 until taskDetails.tags.size) {
                    viewModel.getTagsbyId(
                        taskDetails.tags[i], PrefManager.getcurrentProject()
                    ) { result ->
                        when (result) {
                            is ServerResult.Success -> {
                                binding.progressBar.gone()
                                binding.parentScrollview.visible()
                                CoroutineScope(Dispatchers.IO).launch {
                                    val tag = result.data
                                    tags.add(tag)
                                }
                                setTagsView(tags)

                            }

                            is ServerResult.Failure -> {
                                val errorMessage = result.exception.message
                                utils.showSnackbar(
                                    binding.root,
                                    "Failure in loading tags",
                                    5000
                                )
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


    private fun fetchModerators() {

        Timber.d("Moderators list : ${taskDetails.moderators}")

        if (taskDetails.moderators.isEmpty()) {
            //toast("Contributor empty")
            binding.contributorsRecyclerView.gone()
            binding.noContributors.visible()
            return

        } else if (taskDetails.moderators[0] == Endpoints.TaskDetails.EMPTY_MODERATORS) {
            // toast("Contributor not empty None")
            binding.contributorsRecyclerView.gone()
            binding.noContributors.visible()
            return

        }

        binding.contributorsRecyclerView.visible()
        binding.noContributors.gone()

        for (contributors in taskDetails.moderators) {

            viewModel.getUserbyId(contributors) { result ->
                binding.contributorsRecyclerView.visible()
                binding.noContributors.gone()
                for (contributors in taskDetails.moderators) {
                    viewModel.getUserbyId(contributors) { result ->

                        when (result) {

                            is ServerResult.Success -> {
                                binding.progressBar.gone()
                                binding.parentScrollview.visible()

                                val user = result.data
                                user?.let {

                                    users.add(user)

                                    //Adding user to notification list
                                    pushToReceiver(user)
                                    val user = result.data
                                    users.add(user!!)
                                    moderators.add(user)
                                    moderatorsList.add(user.firebaseID!!)
                                    setContributors(users)
                                }
                            }

                            is ServerResult.Failure -> {

                                utils.singleBtnDialog(
                                    "Failure",
                                    "Failure in fetching Moderators : ${result.exception.message}",
                                    "Okay"
                                ) {
                                    requireActivity().finish()
                                }
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


    private fun fetchUserbyId(id: String, callback: (User?) -> Unit) {

        viewModel.getUserbyId(id) { result ->

            when (result) {
                is ServerResult.Success -> {
                    binding.progressBar.gone()
                    binding.parentScrollview.visible()
                    val user = result.data!!
                    callback(user)
                }

                is ServerResult.Failure -> {
                    utils.singleBtnDialog(
                        "Failure",
                        "Failure in fetching User object : ${result.exception.message}",
                        "Okay"
                    ) {
                        requireActivity().finish()
                    }
                    binding.progressBar.gone()
                    callback(null)
                }

                is ServerResult.Progress -> {
                    binding.progressBar.visible()
                }
            }


        }
    }


    override fun onImageClicked(position: Int, imageList: MutableList<String>) {
        val imageViewerIntent =
            Intent(requireActivity(), ImageViewerActivity::class.java)
        imageViewerIntent.putExtra("position", position)
        imageViewerIntent.putStringArrayListExtra("images", ArrayList(imageList))
        startActivity(
            ImageViewerActivity.createIntent(
                requireContext(),
                ArrayList(imageList),
                position,
            ),
        )

    }

    override fun sendassignee(assignee: User, isChecked: Boolean, position: Int) {
        if (isChecked) {
            selectedAssignee.add(assignee)
            binding.assigneeInclude.normalET.text = assignee.username
            Glide.with(this)
                .load(assignee.profileDPUrl)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .encodeQuality(80)
                .override(40, 40)
                .apply(
                    RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

                )
                .error(R.drawable.profile_pic_placeholder)
                .into(binding.assigneeInclude.tagIcon)

        } else {
            selectedAssignee.remove(assignee)
            binding.assigneeInclude.normalET.text = "Unassigned"
            binding.assigneeInclude.tagIcon.setImageResource(R.drawable.profile_pic_placeholder)

        }

    }


    override fun onAssigneeTListUpdated(TList: MutableList<User>) {

    }

    override fun updateAssignee(assignee: User) {
        viewLifecycleOwner.lifecycleScope.launch {

            try {
                val result = withContext(Dispatchers.IO) {
                    viewModel.updateTask(
                        taskID = activityBinding.taskId,
                        projectName = PrefManager.getcurrentProject(),
                        NewAssignee = if (assignee.firebaseID == "") "None" else assignee.firebaseID!!,
                        OldAssignee = if (taskDetails.assignee == "None") "None" else taskDetails.assignee
                    )
                }

                when (result) {

                    is ServerResult.Failure -> {

                        utils.singleBtnDialog(
                            "Failure",
                            "Failure in Updating: ${result.exception.message}",
                            "Okay"
                        ) {
                            requireActivity().finish()
                        }

                        binding.progressBar.gone()

                    }

                    is ServerResult.Progress -> {
                        binding.progressBar.visible()
                    }

                    is ServerResult.Success -> {

                        binding.progressBar.gone()
                        toast("Updated Assignee")

                    }

                }

            } catch (e: Exception) {

                Timber.tag(TAG).e(e)
                binding.progressBar.gone()

            }
        }
    }

    override fun stringtext(text: String, type: String) {
        binding.stateInclude.tagText.text = text
        binding.stateInclude.tagIcon.text = text.substring(0, 1)
        viewLifecycleOwner.lifecycleScope.launch {

            try {
                val result = withContext(Dispatchers.IO) {
                    viewModel.updateState(
                        taskID = activityBinding.taskId,
                        userID = taskDetails.assignee,
                        newState = text
                    )
                }
                when (result) {

                    is ServerResult.Failure -> {

                        utils.singleBtnDialog(
                            "Failure",
                            "Failure in Updating: ${result.exception.message}",
                            "Okay"
                        ) {
                            requireActivity().finish()
                        }

                        binding.progressBar.gone()

                    }

                    is ServerResult.Progress -> {
                        binding.progressBar.visible()
                    }

                    is ServerResult.Success -> {

                        binding.progressBar.gone()
                        toast("Updated Task State")

                    }

                }

            } catch (e: Exception) {

                Timber.tag(TAG).e(e)
                binding.progressBar.gone()

            }

        }


    }

    override fun onSelectedContributors(contributor: User, isChecked: Boolean) {
        if (isChecked) {
            if (!adapter.isUserAdded(contributor)) {
                contributorList.add(contributor.firebaseID!!)
                contributorDpList.add(contributor.profileIDUrl.toString())
            }
        } else {
            contributorList.remove(contributor.firebaseID)
            contributorDpList.remove(contributor.profileIDUrl)
        }
    }

    override fun onTListUpdated(TList: MutableList<User>) {
        UserlistBottomSheet.DataHolder.users.clear()
        UserlistBottomSheet.DataHolder.users = TList
    }

    override fun savenewModerators(
        selected: MutableList<String>,
        unselected: MutableList<String>
    ) {

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    viewModel.addNewModerators(
                        taskID = activityBinding.taskId,
                        projectName = PrefManager.getcurrentProject(),
                        moderator = selected,
                        unselected = unselected
                    )

                }

                when (result) {

                    is ServerResult.Failure -> {

                        utils.singleBtnDialog(
                            "Failure",
                            "Failure in Updating: ${result.exception.message}",
                            "Okay"
                        ) {
                            requireActivity().finish()
                        }
                        binding.becomeModerator.visible()
                        binding.progressBar.gone()

                    }

                    is ServerResult.Progress -> {
                        binding.progressBar.visible()
                    }

                    is ServerResult.Success -> {
                        activityBinding.binding.gioActionbar.btnModerator.visible()
                        binding.progressBar.gone()
                        toast("Updated new Moderators")
                        requireActivity().recreate()

                    }

                }

            } catch (e: Exception) {

                Timber.tag(TAG).e(e)
                binding.progressBar.gone()

            }
        }
    }
}


//Code for getting html
//                view?.evaluateJavascript(
//                    "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"
//                ) { html ->
//                    Timber.tag("HTML").d(html!!)
//
//                }
