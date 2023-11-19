package com.ncs.o2.UI.Tasks.TaskPage.Details

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
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
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.runDelayed
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.Later
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailViewModel
import com.ncs.o2.UI.Tasks.TaskPage.TasksDetailsHolderFragment
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.UI.UIComponents.Adapters.TagAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.ProfileBottomSheet
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
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class TaskDetailsFragment : Fragment(), ContributorAdapter.OnProfileClickCallback,
    ImageAdapter.ImagesListner {

    @Inject
    lateinit var utils: GlobalUtils.EasyElements

    private var _binding: FragmentTaskDetailsFrgamentBinding? = null
    private val binding get() = _binding!!


    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }

    private val tasksHolderBinding: TasksDetailsHolderFragment by lazy {
        (requireParentFragment() as TasksDetailsHolderFragment)
    }


    private val viewModel: TaskDetailViewModel by viewModels()
    private lateinit var taskDetails: Task

    var tags: MutableList<Tag> = mutableListOf()
    var users: MutableList<User> = mutableListOf()
    private val TextViewList = mutableListOf<TextView>()

    private lateinit var markwon: Markwon
    private lateinit var mdEditor: MarkwonEditor

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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews()
        setdetails(activityBinding.taskId)

        binding.activity.setOnClickThrottleBounceListener {
            val viewpager = tasksHolderBinding.binding.viewPager2
            val next = viewpager.currentItem + 1
            if (next < 2) {
                viewpager.currentItem = next
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setUpMarkwonMarkdown() {

//        val activity = requireActivity()
//        markwon = Markwon.builder(activity)
//            .usePlugin(ImagesPlugin.create())
//            .usePlugin(GlideImagesPlugin.create(activity))
//            .usePlugin(TablePlugin.create(activity))
//            .usePlugin(TaskListPlugin.create(activity))
//            .usePlugin(HtmlPlugin.create())
//            .usePlugin(StrikethroughPlugin.create())
//            .usePlugin(object : AbstractMarkwonPlugin() {
//                override fun configure(registry: MarkwonPlugin.Registry) {
//                    registry.require(ImagesPlugin::class.java) { imagesPlugin ->
//                        imagesPlugin.addSchemeHandler(DataUriSchemeHandler.create())
//                    }
//                }
//            })
//            .build()
//
//        mdEditor = MarkwonEditor.create(markwon)

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }


    @Later("1. Check if the request has already made, if made then set text and clickability on the button accordingly")
    private fun setUpViews() {

        setUpMarkwonMarkdown()

        activityBinding.binding.gioActionbar.btnRequestWork.setOnClickSingleTimeBounceListener {
            activityBinding.binding.gioActionbar.btnRequestWork.animFadein(requireContext())
            sendRequestNotification()
        }

        handleRequestNotificationResult()
    }

    private fun setDefaultViews(task: Task) {
        binding.projectNameET.text = task.project_ID

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
            1 -> "Unassigned"
            2 -> "Ongoing"
            3 -> "Open"
            4 -> "Review"
            5 -> "Testing"
            else -> "Undefined"
        }
        val difficulty = when (task.difficulty) {
            1 -> "Easy"
            2 -> "Medium"
            3 -> "Hard"
            else -> "Undefined"
        }

        binding.priorityInclude.tagIcon.text = priority.substring(0, 1)
        binding.priorityInclude.tagText.text = priority

        binding.typeInclude.tagIcon.text = type.substring(0, 1)
        binding.typeInclude.tagText.text = type

        if (task.assignee[0] != Endpoints.TaskDetails.EMPTY_MODERATORS) {

            fetchUserbyId(task.assignee[0]) { user ->
                Glide.with(requireContext())
                    .load(user?.profileDPUrl)
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

                binding.assigneeInclude.normalET.text = user?.username
            }
        } else {

            binding.assigneeInclude.tagIcon.setImageResource(R.drawable.profile_pic_placeholder)
            binding.assigneeInclude.normalET.text = "No Assignee"

        }

        binding.stateInclude.tagIcon.text = status.substring(0, 1)
        binding.stateInclude.tagText.text = status

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

        binding.taskDurationET.text = task.duration
        binding.taskDurationET.text = task.duration

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
            addRule("body", "font-size:17px")
            addRule("body", "line-height:21px")
            addRule("body", "background-color: #131313")
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
        binding.markdownView.addStyleSheet(css)
        binding.markdownView.addJavascriptInterface(AndroidToJsInterface(), "send")

        binding.markdownView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.evaluateJavascript(script) {}

//                view?.evaluateJavascript(
//                    "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"
//                ) { html ->
//                    Timber.tag("HTML").d(html!!)
//
//                }
            }
        }

        binding.markdownView.loadMarkdown(description)

        binding.descriptionProgressbar.gone()
        binding.markdownView.visible()
        binding.markdownView.animFadein(requireActivity(),500)

    }


    inner class AndroidToJsInterface {
        @JavascriptInterface
        fun sendCode(codeText: String, language : String?) {
            requireActivity().runOnUiThread {

                val codeViewerIntent = Intent(requireActivity(), CodeViewerActivity::class.java)
                codeViewerIntent.putExtra(Endpoints.CodeViewer.CODE, codeText.trimIndent().trim())
                codeViewerIntent.putExtra(Endpoints.CodeViewer.LANG, language?.trimIndent()?.trim())

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
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                Log.d("list", imageUrls.toMutableList().toString())
                val adapter = ImageAdapter(imageUrls.toMutableList(), this@TaskDetailsFragment)
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

        val timeDifference = Date().time - task.time_STAMP!!.toDate().time
        val minutes = (timeDifference / (1000 * 60)).toInt()
        val hours = minutes / 60
        val days = hours / 24
        val years = days / 365

        val timeAgo: String = when {
            years > 0 -> "$years years ago"
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> "just now"
        }

        val fullText = "${task.assigner} created this task $timeAgo"
        val spannableString = SpannableString(fullText)


        val startIndex = 0
        val endIndex = startIndex + task.assigner.length

        // Bold
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.openedBy.text = spannableString
    }

    private fun setdetails(id: String) {

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
                        setTaskDetails(taskResult.data)
                        setDefaultViews(taskResult.data)
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


    private fun typingAnimation(view: TextView, text: String, length: Int) {
        var delay = 200L
        if (Character.isWhitespace(text.elementAt(length - 1))) {
            delay = 600L
        }
        view.text = text.substring(0, length)
        when (length) {
            text.length -> return
            else -> Handler(Looper.getMainLooper()).postDelayed({
                typingAnimation(view, text, length + 1)
            }, delay)
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

    private fun fetchUsers() {

        Timber.d("Contributor list : ${taskDetails.contributors}")

        if (taskDetails.contributors.isEmpty()) {
            //toast("Contributor empty")
            binding.contributorsRecyclerView.gone()
            binding.noContributors.visible()

        } else if (taskDetails.contributors[0] == Endpoints.TaskDetails.EMPTY_MODERATORS) {
           // toast("Contributor not empty None")
            binding.contributorsRecyclerView.gone()
            binding.noContributors.visible()

        } else {

            binding.contributorsRecyclerView.visible()
            binding.noContributors.gone()

            for (contributors in taskDetails.contributors) {

                viewModel.getUserbyId(contributors) { result ->

                    when (result) {

                        is ServerResult.Success -> {
                            binding.progressBar.gone()
                            binding.parentScrollview.visible()

                            val user = result.data
                            users.add(user!!)
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
                    callback(null) // or handle error case accordingly
                }

                is ServerResult.Progress -> {
                    binding.progressBar.visible()
                }
            }


        }
    }


    override fun onImageClicked(position: Int, imageList: MutableList<String>) {
        val imageViewerIntent = Intent(requireActivity(), ImageViewerActivity::class.java)
        imageViewerIntent.putExtra("position", position)
        imageViewerIntent.putStringArrayListExtra("images", ArrayList(imageList))
        startActivity(
            ImageViewerActivity.createIntent(
                requireContext(),
                ArrayList(imageList),
                position
            ),
        )

    }


}


