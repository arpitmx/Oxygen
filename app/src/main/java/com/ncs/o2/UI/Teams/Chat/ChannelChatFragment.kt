package com.ncs.o2.UI.Teams.Chat

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import com.ncs.o2.BuildConfig
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Data.Room.MessageRepository.MessageProjectAssociation
import com.ncs.o2.Data.Room.MessageRepository.UsersDao
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.Enums.MessageType
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.state.SegmentItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.appendTextAtCursor
import com.ncs.o2.Domain.Utility.ExtensionsUtil.appendTextAtCursorMiddleCursor
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.slideDownAndGone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.slideUpAndVisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.NotificationsUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Chat.ChatViewModel
import com.ncs.o2.UI.Tasks.TaskPage.Chat.ExampleGrammarLocator
import com.ncs.o2.UI.Tasks.TaskPage.Details.ImageViewerActivity
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailViewModel
import com.ncs.o2.UI.Teams.ChannelHolderActivity
import com.ncs.o2.UI.UIComponents.Adapters.MentionUsersAdapter
import com.ncs.o2.UI.UIComponents.Adapters.RecyclerViewAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.ChatMoreOptions
import com.ncs.o2.databinding.FragmentTeamsChatBinding
import com.ncs.versa.Constants.Endpoints
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.data.DataUriSchemeHandler
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.regex.Pattern
import javax.inject.Inject


@AndroidEntryPoint
class ChannelChatFragment : Fragment(), ChannelChatAdapter.onChatDoubleClickListner,
    ChannelChatAdapter.onImageClicked, MentionUsersAdapter.onUserClick,ChannelChatAdapter.OnMessageLongPress,ChatMoreOptions.OnReplyClick,ChannelChatAdapter.OnLinkPreviewClick{

    lateinit var binding: FragmentTeamsChatBinding
    private val activityBinding: ChannelHolderActivity by lazy {
        (requireActivity() as ChannelHolderActivity)
    }
    @Inject
    @FirebaseRepository
    lateinit var repository: Repository

    @Inject
    lateinit var utils: GlobalUtils.EasyElements
    @Inject
    lateinit var tasksDB: TasksDatabase
    @Inject
    lateinit var messageDatabase: MessageDatabase
    lateinit var db: UsersDao
    private val viewModel: TaskDetailViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var mdEditor: MarkwonEditor
    lateinit var chatAdapter: ChannelChatAdapter
    lateinit var mentionAdapter: MentionUsersAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var mentionUserRv: RecyclerView
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val CAMERA_PERMISSION_REQUEST = 100
    private var bitmap: Bitmap? = null
    lateinit var imageUri: Uri
    private val CAMERA_PERMISSION_CODE = 101
    private var currentPhotoPath: String? = null
    private var replyingTo: String? = null
    var contributors: MutableList<String> = mutableListOf()
    var contributorsData: MutableList<User> = mutableListOf()
    private var mentionedUsers = mutableListOf<User>()
    private val clipboardManager: ClipboardManager by lazy {
        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    private var linkPreviewMetaData:Map<String,String> = emptyMap()

    @Inject
    lateinit var util: GlobalUtils.EasyElements

    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamsChatBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (PrefManager.getAppMode()== Endpoints.ONLINE_MODE) {
            binding.inputBox.segmentParent.visible()

        }
        else{
            binding.inputBox.segmentParent.gone()

        }
        setUpRecyclerview()
        setMessages()
        setUpChatbox()
        initViews()
    }

    private fun setUpRecyclerview() {
        recyclerView = binding.chatRecyclerview
        mentionUserRv = binding.mentionUserRv
    }




    private fun initViews() {

        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.inputBox.editboxMessage, InputMethodManager.SHOW_IMPLICIT)

        binding.inputBox.bottomTab.gone()
        mentionedUsers.clear()
        mentionAdapter = MentionUsersAdapter(emptyList<User>().toMutableList(), this)
        binding.inputBox.progressBarSendMsg.gone()


        binding.inputBox.editboxMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()

                val links = extractLinks(input)
                if (links.isEmpty() || links=="") {
                    processLinks("")
                }
                else{
                    processLinks(links)
                }
                if (' ' in input) {
                    val links = extractLinks(input)
                    Log.d("linksaterext",links)
                    if (links.isEmpty() || links == "") {
                        processLinks("")
                    } else {
                        processLinks(links)
                    }
                }

                val lastAtSymbolIndex = input.lastIndexOf('@')

                if (lastAtSymbolIndex != -1 && lastAtSymbolIndex == input.length - 1) {
                    slideUpAnimation(binding.mentionUsersView)

                    if (contributors.isEmpty()) {
                        fetchContributors()
                    } else {
                        if (contributorsData.isEmpty() || contributorsData.size != contributors.size) {
                            fetchContributors()
                        } else {
                            filterList(input.substring(lastAtSymbolIndex + 1), mentionedUsers)
                        }
                    }
                } else if ('@' in input) {
                    if (contributors.isEmpty()) {
                        fetchContributors()
                    } else {
                        if (contributorsData.isEmpty() || contributorsData.size != contributors.size) {
                            fetchContributors()
                        } else {
                            val mentions = input.split('@').drop(1)
                            for (mention in mentions) {
                                for (cont in contributorsData) {
                                    if (cont.username.equals(mention.trim(), ignoreCase = true)) {
                                        mentionedUsers.add(cont)
                                        val list = mentionedUsers.distinctBy { it.firebaseID }.toMutableList()
                                        Timber.tag("listcheck").d(list.toString())
                                    }
                                }
                                filterList(mention, mentionedUsers)
                            }
                        }
                    }
                } else {
                    slideDownAnimation(binding.mentionUsersView)
                    mentionAdapter.updateList(emptyList())
                }
            }
        })
        binding.inputBox.btnSend.setOnClickThrottleBounceListener {

            if (binding.inputBox.editboxMessage.text.toString().trim().isNotEmpty()) {
                sendMessageProcess()

            } else if (bitmap != null) {
                binding.inputBox.progressBarSendMsg.visible()
                uploadImageToFirebaseStorage(bitmap!!, PrefManager.getcurrentProject())
                clearReplying()

            } else {
                util.showSnackbar(binding.root, "Message can't be empty", 500)
            }


        }

        binding.btnPaste.setOnClickThrottleBounceListener {
            pasteFromClipboard()
        }

        binding.btnCodeBlock.setOnClickThrottleBounceListener {

            binding.inputBox.editboxMessage.appendTextAtCursor(
                " ``` Code_Lang \n Code \n``` "
            )

        }

        binding.btnAttachBlockQuote.setOnClickThrottleBounceListener {
            binding.inputBox.editboxMessage.appendTextAtCursor(
                ">"
            )
        }

        binding.btnAttachBold.setOnClickThrottleBounceListener {
            binding.inputBox.editboxMessage.appendTextAtCursorMiddleCursor(
                "****", type = 4
            )

        }

        binding.btnAttachItalics.setOnClickThrottleBounceListener {
            binding.inputBox.editboxMessage.appendTextAtCursorMiddleCursor(
                "__", type = 2
            )
        }

        binding.btnChecklist.setOnClickThrottleBounceListener {
            binding.inputBox.editboxMessage.appendTextAtCursor(
                " - [ ] List_Text "
            )

        }

        binding.btnLink.setOnClickThrottleBounceListener {
            binding.inputBox.editboxMessage.appendTextAtCursor(
                " [Link Text](Link URL) "
            )
        }

        binding.btnBackTick.setOnClickThrottleBounceListener {

            binding.inputBox.editboxMessage.appendTextAtCursorMiddleCursor(
                "` `", type = 2
            )
        }
    }

    private fun extractLinks(input: String): String {
        val urlPattern = "([\\w+]+\\:\\/\\/)?([\\w\\d-]+\\.)*[\\w-]+[\\.\\:]\\w+([\\/\\?\\=\\&\\#\\.]?[\\w-]+)*\\/?".toRegex()
        val matches = urlPattern.findAll(input)
        val firstMatch = matches.firstOrNull()

        return firstMatch?.let {
            val link = it.value
            Log.d("linkkk",link.toString())
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                "https://$link"
            } else {
                link
            }
        } ?: ""
    }


    private fun processLinks(link: String) {
        if (link.isNotEmpty() || link!="") {
            CoroutineScope(Dispatchers.IO).launch {

//                "Title" to title,
//                "Description" to description,
//                "Open Graph Title" to ogTitle,
//                "Open Graph Description" to ogDescription,
//                "Open Graph Image" to ogImage

                var metadata = fetchMetadata(link)

                withContext(Dispatchers.Main) {
                    if (binding.inputBox.editboxMessage.text.isNullOrEmpty()){
                        binding.inputBox.linkPreviewSender.gone()
                        binding.inputBox.linkPreviewTitle.text="Getting link info..."
                        binding.inputBox.linkPreviewDesc.text="Please wait..."
                    }
                    else {
                        Log.d("metadatacheck",metadata.toString())
                        if (metadata.isNull) {
                            binding.inputBox.linkPreviewSender.gone()
                            binding.inputBox.linkPreviewTitle.text = "Getting link info..."
                            binding.inputBox.linkPreviewDesc.text = "Please wait..."
                        } else {
                            linkPreviewMetaData = metadata!!
                            binding.inputBox.linkPreviewSender.visible()

                            ExtensionsUtil.runDelayed(2000) {
                                if (metadata?.getValue("Type") == "normal") {
                                    binding.inputBox.linkPreviewTitle.text =
                                        metadata?.getValue("Title")

                                    binding.inputBox.linkPreviewDesc.text =
                                        if (metadata?.getValue("Open Graph Description")
                                                .isNullOrEmpty()
                                        ) link else metadata.getValue("Open Graph Description")

                                } else {
                                    binding.inputBox.linkPreviewTitle.text =
                                        metadata?.getValue("Title")
                                    binding.inputBox.linkPreviewDesc.text = link
                                }
                                binding.inputBox.linkPreviewSender.setOnClickThrottleBounceListener {
                                    openInBrowser(link)
                                }
                            }
                        }
                    }
                }
            }

        } else {
            binding.inputBox.linkPreviewSender.gone()
            binding.inputBox.linkPreviewTitle.text="Getting link info..."
            binding.inputBox.linkPreviewDesc.text="Please wait..."
        }
    }

    suspend fun fetchMetadata(url: String): Map<String, String>? {
        return try {
            val document = Jsoup.connect(url).get()

            val title = document.title()
            val description = document.select("meta[name=description]").attr("content")
            val ogTitle = document.select("meta[property=og:title]").attr("content")
            val ogDescription = document.select("meta[property=og:description]").attr("content")
            val ogImage = document.select("meta[property=og:image]").attr("content")

            mapOf(
                "Title" to title,
                "Description" to description,
                "Open Graph Title" to ogTitle,
                "Open Graph Description" to ogDescription,
                "Open Graph Image" to ogImage,
                "Type" to "normal",
                "Url" to url
            )
        } catch (e: IOException) {
            Log.d("metadatafetch",e.message.toString())
            val list=extractValues(e.message!!)
            Log.d("metadatafetch",list.toString())
            if (list.isEmpty()) {
                Log.d("metadatafetch",list.size.toString())
                null
            }
            else{
                if (list.size==4){
                    val taskID="#${list[3]}-${list[1]}"
                    val projectId=list[2]
                    val task=tasksDB.tasksDao().getTasksbyId(tasksId = taskID, projectId = projectId)

                    mapOf(
                        "Title" to "${list[0].capitalize()} | ${list[2]} | #${list[3]}-${list[1]}",
                        "Description" to "",
                        "Open Graph Title" to "",
                        "Open Graph Description" to "",
                        "Open Graph Image" to "",
                        "Type" to "o2",
                        "SubType" to "share",
                        "Url" to task!!.title,
                        "TaskID" to taskID,
                        "ProjectID" to projectId

                    )
                }
                else {
                    mapOf(
                        "Title" to "${list[0].capitalize()} - ${list[1].capitalize()}",
                        "Description" to "",
                        "Open Graph Title" to "",
                        "Open Graph Description" to "",
                        "Open Graph Image" to "",
                        "Type" to "o2",
                        "SubType" to "join",
                        "Url" to url,
                        "ProjectID" to list[1]

                    )
                }
            }
        }
    }
    fun extractValues(errorMessage: String): List<String> {
        val sharePattern = Pattern.compile("/share/(\\d+)/(\\w+)/(\\w+)")
        val joinPattern = Pattern.compile("/join/(\\w+)")

        val shareMatcher = sharePattern.matcher(errorMessage)
        val joinMatcher = joinPattern.matcher(errorMessage)

        val values = mutableListOf<String>()

        if (shareMatcher.find()) {
            values.add("Task")
            values.add(shareMatcher.group(1))
            values.add(shareMatcher.group(2))
            values.add(shareMatcher.group(3))
        } else if (joinMatcher.find()) {
            values.add("Join")
            values.add(joinMatcher.group(1))
        }

        return values
    }
    private fun openInBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun pasteFromClipboard() {
        val clipData: ClipData? = clipboardManager.primaryClip

        if (clipData != null && clipData.itemCount > 0) {
            val textToPaste = clipData.getItemAt(0).text.toString()
            binding.inputBox.editboxMessage.appendTextAtCursor(textToPaste)
        } else {
            toast("Nothing to paste..")
        }
    }

    private fun filterList(query: String, mentionedUsers: List<User>) {
        val filteredList = contributorsData.filter { contributor ->
            contributor.username?.contains(query, ignoreCase = true) == true ||
                    contributor.email?.contains(query, ignoreCase = true) == true ||
                    contributor.fullName?.contains(query, ignoreCase = true) == true
        }
        mentionAdapter.updateList(filteredList)
    }

    private fun sendMessageProcess() {

        if (replyingTo == null && linkPreviewMetaData.isEmpty()) {
            val message = Message(
                messageId = RandomIDGenerator.generateRandomId(),
                senderId = PrefManager.getcurrentUserdetails().EMAIL,
                content = binding.inputBox.editboxMessage.text?.trim().toString(),
                messageType = MessageType.NORMAL_MSG,
                timestamp = Timestamp.now(),
            )
            postMessage(message)
        }
        else if (replyingTo!=null) {
            val additionalData: HashMap<String, String> = hashMapOf(
                "replyingTo" to replyingTo!!,
            )

            val message = Message(
                messageId = RandomIDGenerator.generateRandomId(),
                senderId = PrefManager.getcurrentUserdetails().EMAIL,
                content = binding.inputBox.editboxMessage.text?.trim().toString(),
                messageType = MessageType.REPLY_MSG,
                timestamp = Timestamp.now(),
                additionalData = additionalData,

                )
            postMessage(message)
        }
        else{
            val message = Message(
                messageId = RandomIDGenerator.generateRandomId(),
                senderId = PrefManager.getcurrentUserdetails().EMAIL,
                content = binding.inputBox.editboxMessage.text?.trim().toString(),
                messageType = MessageType.LINK_MSG,
                timestamp = Timestamp.now(),
                additionalData = linkPreviewMetaData,

                )
            postMessage(message)
        }
    }



    private fun clearReplying() {
        binding.inputBox.replyViewParent.gone()
        replyingTo = null
    }

    private fun setUpChatbox() {

        binding.inputBox.btnCancelReply.setOnClickThrottleBounceListener {
            clearReplying()
        }

        binding.inputBox.replyViewParent.gone()
        val markdownEditor = MarkwonEditor.builder(markwon).build()

        binding.inputBox.editboxMessage.addTextChangedListener(
            MarkwonEditorTextWatcher.withPreRender(
                markdownEditor, Executors.newCachedThreadPool(), binding.inputBox.editboxMessage
            )
        )

        binding.btnAttachImage.setOnClickThrottleBounceListener {
            selectImage()
            if (bitmap != null) {
                binding.inputBox.msgBox.gone()
                binding.btnSelectImageFromStorage.visible()
                binding.inputBox.selectedImageView.visible()
            }
            if (bitmap == null) {
                binding.inputBox.msgBox.visible()
                binding.btnSelectImageFromStorage.gone()
                binding.inputBox.selectedImageView.gone()
            }
        }

        binding.inputBox.btnAttach.setOnClickThrottleBounceListener(100) {
            if (chatViewModel.CHAT_WINDOW_OPTION_BOX_STATUS) {
                toggleChatOptions(false)
            } else {
                toggleChatOptions(true)
            }
        }

        binding.crossBtnSelectPdf.setOnClickThrottleBounceListener {
            bitmap = null
            binding.btnSelectImageFromStorage.gone()
            binding.inputBox.selectedImageView.gone()
            binding.inputBox.msgBox.visible()
        }

        binding.inputBox.crossBtnSelectedImage.setOnClickThrottleBounceListener {
            bitmap = null
            binding.btnSelectImageFromStorage.gone()
            binding.inputBox.selectedImageView.gone()
            binding.inputBox.msgBox.visible()
        }
    }


    private fun toggleChatOptions(visibility: Boolean) {
        if (visibility) {
            binding.inputBox.bottomTab.slideUpAndVisible(100) {
                chatViewModel.CHAT_WINDOW_OPTION_BOX_STATUS = true
            }

        } else {
            binding.inputBox.bottomTab.slideDownAndGone(100) {
                chatViewModel.CHAT_WINDOW_OPTION_BOX_STATUS = false
            }

        }
    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap, projectId: String) {

        chatViewModel.uploadImageFromTeams(bitmap, projectId,activityBinding.channelName).observe(viewLifecycleOwner) { result ->

            when (result) {
                is ServerResult.Failure -> {
                    util.singleBtnDialog_InputError(
                        "Upload Errors",
                        "There was an issue in uploading the Image, ${result.exception.message} \n\nplease retry",
                        "Retry"
                    ) {
                        binding.inputBox.progressBarSendMsg.gone()
                    }
                }

                ServerResult.Progress -> {
                    binding.inputBox.progressBarSendMsg.visible()
                }

                is ServerResult.Success -> {
                    binding.inputBox.progressBarSendMsg.gone()
                    val imgStorageReference = result.data
                    getImageDownloadUrl(imgStorageReference)
                }
            }


        }
    }

    private fun getImageDownloadUrl(imageRef: StorageReference) {

        chatViewModel.getDPUrlThroughRepository(imageRef).observe(viewLifecycleOwner) { result ->

            when (result) {
                is ServerResult.Failure -> {
                    util.singleBtnDialog_InputError(
                        "Upload Errors",
                        "There was an issue in uploading the Image, ${result.exception.message},\n\nplease retry",
                        "Retry"
                    ) {
                        binding.inputBox.progressBarSendMsg.gone()
                        imageRef.delete()
                    }
                }

                ServerResult.Progress -> {
                    binding.inputBox.progressBarSendMsg.visible()
                }

                is ServerResult.Success -> {
                    binding.inputBox.progressBarSendMsg.gone()
                    addImageUrlToFirestore(result.data)

                }
            }
        }

    }

    private fun addImageUrlToFirestore(imageUrl: String) {

        val message = Message(
            messageId = RandomIDGenerator.generateRandomId(),
            senderId = PrefManager.getcurrentUserdetails().EMAIL,
            content = imageUrl,
            messageType = MessageType.IMAGE_MSG,
            timestamp = Timestamp.now(),
        )
        postMessage(message)
    }


    private fun selectImage() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST
            )
        } else {
            pickImage()
        }
    }


    private var capturedImageUri: Uri? = null
    private fun pickImage() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Take Photo" -> {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission is not granted, request it
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            100
                        )
                    } else {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                        // Generate a content URI using FileProvider
                        capturedImageUri = FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.provider",
                            createImageFile()
                        )

                        intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri)
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                    }
                }

                "Choose from Gallery" -> {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, REQUEST_IMAGE_PICK)
                }

                "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun createImageFile(): File {
        val dir: File = File(requireContext().externalCacheDir, "images")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "O2-Snap-${Timestamp.now().seconds}.jpeg")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap: Bitmap = BitmapFactory.decodeStream(
                        requireContext().contentResolver.openInputStream(capturedImageUri!!)
                    )
                    Timber.tag("Image bitmap name").d(createImageFile().name)

                    bitmap = imageBitmap
                    binding.inputBox.msgBox.gone()
                    binding.btnSelectImageFromStorage.visible()
                    binding.inputBox.selectedImageView.visible()
                    binding.imagePreview.setImageBitmap(imageBitmap)
                }


                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    imageUri = selectedImage!!
                    bitmap = uriToBitmap(requireContext().contentResolver, selectedImage!!)
                    binding.inputBox.msgBox.gone()
                    binding.btnSelectImageFromStorage.visible()
                    binding.inputBox.selectedImageView.visible()
                    binding.imagePreview.setImageURI(selectedImage)

                }
            }
        }
    }
    private fun fetchContributors() {
        firestoreRepository.getContributors(PrefManager.getcurrentProject()) { serverResult ->
            when (serverResult) {
                is ServerResult.Success -> {

                    val contributorList = serverResult.data
                    if (contributorList.isNotEmpty()) {
                        contributors.addAll(contributorList)
                        for (contributor in contributorList) {
                            viewModel.getUserbyId(contributor) { result ->
                                when (result) {
                                    is ServerResult.Success -> {
                                        binding.mentionProgressbar.gone()
                                        binding.mentionUserRv.visible()

                                        val user = result.data
                                        user?.email = contributor
                                        contributorsData.add(user!!)
                                        if (contributorsData.size == contributorList.size) {
                                            setMentionUsersRv(contributorsData)
                                        }
                                        Log.d("contributorsdata", contributorsData.toString())
                                    }

                                    is ServerResult.Failure -> {

                                        utils.singleBtnDialog(
                                            "Failure",
                                            "Failure in fetching users : ${result.exception.message}",
                                            "Okay"
                                        ) {
                                            requireActivity().finish()
                                        }
                                        binding.mentionProgressbar.gone()
                                        binding.mentionUserRv.visible()

                                    }

                                    is ServerResult.Progress -> {
                                        binding.mentionProgressbar.visible()
                                        binding.mentionUserRv.gone()
                                    }
                                }
                            }
                        }

                    }
                }

                is ServerResult.Failure -> {
                    binding.mentionUserRv.visible()
                    binding.mentionProgressbar.gone()
                    val exception = serverResult.exception
                    utils.showSnackbar(requireView(), "Couldn't fetch Users", 2000)

                }

                is ServerResult.Progress -> {
                    binding.mentionUserRv.gone()
                    binding.mentionProgressbar.visible()
                }
            }
        }
    }

    private fun setMentionUsersRv(list: MutableList<User>) {
        Log.d("rvList", list.toString())
        mentionAdapter = MentionUsersAdapter(list.toSet().toMutableList(), this)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mentionUserRv.layoutManager = linearLayoutManager
        mentionUserRv.adapter = mentionAdapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera Permission Denied, can't take photo",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setMessages() {
        chatAdapter = ChannelChatAdapter(
            activitybinding = activityBinding,
            repository = firestoreRepository,
            msgList = mutableListOf(),
            context = requireContext(),
            onchatDoubleClickListner = this,
            markwon = markwon,
            this,
            this
        )

        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        layoutManager.stackFromEnd = true

        with(recyclerView) {
            this.layoutManager = layoutManager
            adapter = chatAdapter
            edgeEffectFactory = BounceEdgeEffectFactory()
        }
        CoroutineScope(Dispatchers.IO).launch {
            if (PrefManager.getChannelTimestamp(PrefManager.getcurrentProject(),activityBinding.channelName).seconds.toInt()==0){
                Log.d("messageFetch","messageFetch from firebase")
                chatViewModel.getTeamsMessages(PrefManager.getcurrentProject(),activityBinding.channelName) { result ->
                    when (result) {
                        is ServerResult.Success -> {
                            if (result.data.isEmpty()) {
                                binding.progress.gone()
                                recyclerView.gone()
                                binding.placeholder.visible()
                            } else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    for (message in result.data){
                                        messageDatabase.teamsMessagesDao().insert(message)
                                        messageDatabase.teamsMessagesDao().insertAssociation(
                                            MessageProjectAssociation(
                                                messageId = message.messageId,
                                                projectId = PrefManager.getcurrentProject(),
                                                channelId = activityBinding.channelName)
                                        )
                                    }
                                    withContext(Dispatchers.Main){
                                        val messagedata=result.data.toMutableList().sortedByDescending { it.timestamp }
                                        chatAdapter.appendMessages(result.data)
                                        binding.progress.gone()
                                        recyclerView.visible()
                                        binding.placeholder.gone()
                                        recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                                        PrefManager.setChannelTimestamp(PrefManager.getcurrentProject(),activityBinding.channelName,messagedata[0].timestamp!!)

                                    }
                                }
                            }

                        }

                        is ServerResult.Failure -> {
                            binding.progress.gone()
                            val errorMessage = result.exception.message
                            GlobalUtils.EasyElements(requireContext()).singleBtnDialog(
                                "Failure", "Failed to load messages with error: $errorMessage", "Okay"
                            ) {
                                requireActivity().finish()
                            }
                        }

                        is ServerResult.Progress -> {
                            binding.progress.visible()
                            recyclerView.gone()
                        }
                    }

                }
            }else {
                Log.d("messageFetch", "messageFetch from db ")
                chatViewModel.getTeamsMessagesforProject(
                    PrefManager.getcurrentProject(),activityBinding.channelName
                ) { result ->
                    when (result) {
                        is DBResult.Success -> {
                            if (result.data.isEmpty()) {
                                binding.progress.gone()
                                recyclerView.gone()
                                binding.placeholder.visible()
                            } else {
                                val messagedata=result.data.toMutableList().sortedByDescending { it.timestamp }
                                chatAdapter.appendMessages(result.data)
                                binding.progress.gone()
                                recyclerView.visible()
                                binding.placeholder.gone()
                                recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                                PrefManager.setChannelTimestamp(PrefManager.getcurrentProject(),activityBinding.channelName,messagedata[0].timestamp!!)

                            }

                        }

                        is DBResult.Failure -> {
                            binding.progress.gone()
                            val errorMessage = result.exception.message
                            GlobalUtils.EasyElements(requireContext()).singleBtnDialog(
                                "Failure",
                                "Failed to load messages with error: $errorMessage",
                                "Okay"
                            ) {
                                requireActivity().finish()
                            }
                        }

                        is DBResult.Progress -> {
                            binding.progress.visible()
                            recyclerView.gone()
                        }

                        else -> {}
                    }

                }
            }

            getNewMessages()


        }


    }

    fun getNewMessages(){
        chatViewModel.getNewTeamsMessages(PrefManager.getcurrentProject(),activityBinding.channelName) { result ->
            when (result) {
                is ServerResult.Success -> {
                    if (result.data.isNotEmpty()){
                        val messagedata=result.data.toMutableList().sortedByDescending { it.timestamp }
                        chatAdapter.appendMessages(result.data)
                        recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                        PrefManager.setChannelTimestamp(PrefManager.getcurrentProject(),activityBinding.channelName,messagedata[0].timestamp!!)
                    }

                }

                is ServerResult.Failure -> {
                    binding.progress.gone()
                    val errorMessage = result.exception.message
                    GlobalUtils.EasyElements(requireContext()).singleBtnDialog(
                        "Failure", "Failed to load messages with error: $errorMessage", "Okay"
                    ) {
                        requireActivity().finish()
                    }
                }

                is ServerResult.Progress -> {

                }
            }

        }
    }


    fun postMessage(message: Message) {

        recyclerView = binding.chatRecyclerview

        CoroutineScope(Dispatchers.Main).launch {

            repository.postTeamsMessage(
                projectName = PrefManager.getcurrentProject(), message = message, channelID = activityBinding.channelName

            ) { result ->

                when (result) {

                    is ServerResult.Failure -> {
                        binding.inputBox.progressBarSendMsg.gone()
                    }

                    ServerResult.Progress -> {
                        binding.inputBox.progressBarSendMsg.visible()
                    }

                    is ServerResult.Success -> {

                        binding.inputBox.progressBarSendMsg.gone()
                        binding.inputBox.editboxMessage.text!!.clear()
                        binding.inputBox.editboxMessage.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

                        clearReplying()
                        binding.inputBox.linkPreviewSender.gone()
                        binding.inputBox.linkPreviewTitle.text="Getting link info..."
                        binding.inputBox.linkPreviewDesc.text="Please wait..."
                        linkPreviewMetaData= emptyMap()
                        recyclerView.visible()
                        binding.placeholder.gone()
                        chatAdapter.appendMessages(listOf(message))
                        recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)


                        if (message.messageType == MessageType.NORMAL_MSG || message.messageType == MessageType.REPLY_MSG || message.messageType==MessageType.LINK_MSG) {

                            Log.d("listcheck", mentionedUsers.toString())
                            val list = mentionedUsers.distinctBy { it.firebaseID }.toMutableList()
                            val regex = Regex("@(\\w+)")
                            val matches = regex.findAll(message.content)
                            val mentionedUsersName: MutableList<String> = mutableListOf()
                            Log.d("listcheckmatches", matches.toString())

                            for (user in list) {
                                user.username?.let {
                                    mentionedUsersName.add(it.toLowerCase())
                                }
                            }

                            Log.d("listcheckmentioned", mentionedUsersName.toString())

                            if (matches.any()) {
                                val mentioned =
                                    matches.map { it.groupValues[1].toLowerCase() }.toList()

                                Log.d("listcheckmentioned", mentioned.toString())

                                if (mentionedUsersName.containsAll(mentioned)) {
                                    binding.inputBox.progressBarSendMsg.gone()
                                    binding.inputBox.editboxMessage.text?.clear()

                                    var trimmedMsg = message.content.substring(
                                        0, message.content.length.coerceAtMost(150)
                                    )

                                    if (trimmedMsg.length == 150) trimmedMsg = "$trimmedMsg..."

                                    val notification = composeNotification(
                                        NotificationType.TEAMS_COMMENT_MENTION_NOTIFICATION,
                                        message = trimmedMsg
                                    )
                                    val mentionedUserTokenList: List<String> =
                                        list.map { it.fcmToken!! }
                                    Log.d("listcheck", list.toString())

                                    for (user in list) {

                                        chatViewModel.addNotificationToFirebase(
                                            user.firebaseID!!, notification = notification!!
                                        ) { res ->
                                            when (res) {
                                                is ServerResult.Success -> {
                                                    binding.progress.gone()
                                                    notification.let {
                                                        sendNotification(
                                                            listOf(user.fcmToken!!).toMutableList(),
                                                            notification
                                                        )
                                                    }
                                                }

                                                is ServerResult.Failure -> {
                                                    binding.progress.gone()
                                                    val errorMessage = res.exception.message
                                                    GlobalUtils.EasyElements(requireContext())
                                                        .singleBtnDialog(
                                                            "Failure",
                                                            "Failed in sending notification: $errorMessage",
                                                            "Okay"
                                                        ) {
                                                            requireActivity().recreate()
                                                        }
                                                }

                                                is ServerResult.Progress -> {
                                                    binding.progress.visible()
                                                }
                                            }
                                        }
                                    }
                                    list.clear()
                                    mentionedUsers.clear()
                                }
                            } else {
                                binding.inputBox.progressBarSendMsg.gone()
                                binding.inputBox.editboxMessage.text?.clear()

                                var trimmedMsg = message.content.substring(
                                    0, message.content.length.coerceAtMost(150)
                                )

                                if (trimmedMsg.length == 150) trimmedMsg = "$trimmedMsg..."

                                val notification = composeNotification(
                                    NotificationType.TEAMS_COMMENT_NOTIFICATION, message = trimmedMsg
                                )

                                if (activityBinding.channelName=="General"){
                                    val projectTopic = PrefManager.getcurrentProject().replace("\\s+".toRegex(), "_") + "_TOPIC_GENERAL"
                                    val notifmsg="${PrefManager.getcurrentUserdetails().USERNAME} : $trimmedMsg"

                                    val _notification =  Notification(
                                        notificationID = RandomIDGenerator.generateRandomTaskId(6),
                                        notificationType = NotificationType.TEAMS_COMMENT_NOTIFICATION.name,
                                        taskID = "",
                                        message = notifmsg,
                                        title = "${PrefManager.getcurrentProject()} | ${PrefManager.getcurrentUserdetails().USERNAME} commented in #${activityBinding.channelName}",
                                        fromUser = PrefManager.getcurrentUserdetails().EMAIL,
                                        toUser = "None",
                                        timeStamp = Timestamp.now().seconds,
                                        projectID = PrefManager.getcurrentProject(),
                                        channelID = activityBinding.channelName
                                    )

                                    sendNotificationtoTopic(
                                        projectTopic, _notification
                                    )

                                }
                                else{
                                    val filteredList = {
                                        val list = activityBinding.sharedViewModel.getList()
                                        if (list.contains(PrefManager.getUserFCMToken())) {
                                            list.remove(PrefManager.getUserFCMToken())
                                        }
                                        list

                                    }
                                    Log.d("fcmToken",filteredList.invoke().toString())

                                    notification?.let {
                                        sendNotification(
                                            filteredList.invoke(), notification
                                        )
                                    }
                                }


                            }


                        }
                        if (message.messageType == MessageType.IMAGE_MSG) {

                            bitmap = null
                            binding.btnSelectImageFromStorage.gone()
                            binding.inputBox.selectedImageView.gone()
                            binding.inputBox.msgBox.visible()
                            binding.inputBox.progressBarSendMsg.gone()
                            binding.inputBox.editboxMessage.text?.clear()

                            val trimmedMsg = "Shared a pic \uD83C\uDF01"
                            val notification = composeNotification(
                                NotificationType.TEAMS_COMMENT_NOTIFICATION, message = trimmedMsg
                            )

                            val filteredList = {
                                val list = activityBinding.sharedViewModel.getList()
                                if (list.contains(PrefManager.getUserFCMToken())) {
                                    list.remove(PrefManager.getUserFCMToken())
                                }
                                list

                            }
                            notification?.let {
                                sendNotification(
                                    filteredList.invoke(), notification
                                )
                            }
                        }


                    }
                }
            }
        }
    }

    private fun composeNotification(type: NotificationType, message: String): Notification? {

        if (type == NotificationType.TEAMS_COMMENT_NOTIFICATION) {
            val notifmsg="${PrefManager.getcurrentUserdetails().USERNAME} : $message"

            return Notification(
                notificationID = RandomIDGenerator.generateRandomTaskId(6),
                notificationType = NotificationType.TEAMS_COMMENT_NOTIFICATION.name,
                taskID = "",
                message = notifmsg,
                title = "${PrefManager.getcurrentProject()} | #${activityBinding.channelName} | New Comment",
                fromUser = PrefManager.getcurrentUserdetails().EMAIL,
                toUser = "None",
                timeStamp = Timestamp.now().seconds,
                projectID = PrefManager.getcurrentProject(),
                channelID = activityBinding.channelName
            )
        }
        if (type == NotificationType.TEAMS_COMMENT_MENTION_NOTIFICATION) {
            val mentionedUserNames: MutableList<String> = mutableListOf()
            val list = mentionedUsers.distinctBy { it.firebaseID }
            for (user in list) {
                mentionedUserNames.add("@${user.username!!}")
            }
            val usernames = mentionedUserNames.joinToString(", ")
            val notifmsg="${PrefManager.getcurrentUserdetails().USERNAME} : $message"

            return Notification(
                notificationID = RandomIDGenerator.generateRandomTaskId(6),
                notificationType = NotificationType.TEAMS_COMMENT_MENTION_NOTIFICATION.name,
                taskID = "",
                message = notifmsg,
                title = "${PrefManager.getcurrentProject()} | #${activityBinding.channelName} | Mentioned $usernames",
                fromUser = PrefManager.getcurrentUserdetails().EMAIL,
                toUser = "None",
                timeStamp = Timestamp.now().seconds,
                projectID = PrefManager.getcurrentProject(),
                channelID = activityBinding.channelName

            )
        }

        return null
    }

    private fun sendNotification(receiverList: MutableList<String>, notification: Notification) {

        try {
            CoroutineScope(Dispatchers.IO).launch {
                for (receiverToken in receiverList) {
                    NotificationsUtils.sendFCMNotification(
                        receiverToken, notification = notification
                    )
                }

            }

        } catch (exception: Exception) {
            Timber.tag("")
            utils.showSnackbar(binding.root, "Failure in sending notifications", 5000)
        }

    }

    private fun sendNotificationtoTopic(topic: String, notification: Notification) {

        try {
            CoroutineScope(Dispatchers.IO).launch {
                NotificationsUtils.sendFCMNotificationToTopic(
                    topic = topic, notification = notification
                )

            }

        } catch (exception: Exception) {
            Timber.tag("")
            utils.showSnackbar(binding.root, "Failure in sending notifications", 5000)
        }

    }


    private fun setDetails(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (tasksDB.tasksDao().getTasksbyId(tasksId = id, projectId = PrefManager.getcurrentProject()).isNull){
                Log.d("taskFetchTest","fetch from firebase")
                viewLifecycleOwner.lifecycleScope.launch {

                    try {

                        val taskResult = withContext(Dispatchers.IO) {
                            viewModel.getTasksById(id, PrefManager.getcurrentProject())
                        }

                        Timber.tag(TaskDetailsFragment.TAG).d("Fetched task result : ${taskResult}")

                        when (taskResult) {

                            is ServerResult.Failure -> {
                                binding.progress.gone()

                                utils.singleBtnDialog(
                                    "Failure",
                                    "Failure in Task exception : ${taskResult.exception.message}",
                                    "Okay"
                                ) {
                                    requireActivity().finish()
                                }


                            }

                            is ServerResult.Progress -> {
                                binding.progress.visible()
                            }

                            is ServerResult.Success -> {
                                binding.progress.gone()


                            }

                        }

                    } catch (e: Exception) {

                        Timber.tag(TaskDetailsFragment.TAG).e(e)
                        binding.progress.gone()

                        utils.singleBtnDialog(
                            "Failure", "Failure in Task exception : ${e.message}", "Okay"
                        ) {
                            requireActivity().finish()
                        }
                    }
                }
            }else{

                Log.d("taskFetchTest","fetch from db")
                viewLifecycleOwner.lifecycleScope.launch {

                    try {

                        viewModel.getTaskbyIdFromDB(PrefManager.getcurrentProject(),id){
                            when (it) {

                                is DBResult.Failure -> {
                                    binding.progress.gone()

                                    utils.singleBtnDialog(
                                        "Failure",
                                        "Failure in Task exception : ${it.exception.message}",
                                        "Okay"
                                    ) {
                                        requireActivity().finish()
                                    }


                                }

                                is DBResult.Progress -> {
                                    binding.progress.visible()
                                }

                                is DBResult.Success -> {
                                    binding.progress.gone()

                                }

                                else -> {}
                            }
                        }



                    } catch (e: Exception) {

                        Timber.tag(TaskDetailsFragment.TAG).e(e)
                        binding.progress.gone()

                        utils.singleBtnDialog(
                            "Failure", "Failure in Task exception : ${e.message}", "Okay"
                        ) {
                            requireActivity().finish()
                        }
                    }

                }
            }
        }
    }

    private val markwon: Markwon by lazy {
        val prism4j = Prism4j(ExampleGrammarLocator())
        val activity = requireActivity()
        Markwon.builder(activity)
            .usePlugin(ImagesPlugin.create())
            .usePlugin(GlideImagesPlugin.create(activity))
            .usePlugin(TablePlugin.create(activity))
            .usePlugin(TaskListPlugin.create(activity))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDarkula.create()))

            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configure(registry: MarkwonPlugin.Registry) {
                    registry.require(ImagesPlugin::class.java) { imagesPlugin ->
                        imagesPlugin.addSchemeHandler(DataUriSchemeHandler.create())
                    }
                }
            })
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .blockQuoteColor(resources.getColor(R.color.primary))
                        .linkColor(resources.getColor(R.color.light_blue_A200))
                        .codeBlockTextSize(30)
                }
            })

            .build()
    }


    override fun onDoubleClickListner(msg: Message, senderName: String) {
        binding.inputBox.replyingToUserTv.text = "Replying to @${senderName}"
        binding.inputBox.referenceMsgTv.text = msg.content
        binding.inputBox.replyViewParent.visible()
        binding.inputBox.replyViewParent.animFadein(requireContext(), 150)
        requireActivity().performHapticFeedback()
        replyingTo = msg.messageId
    }


    fun uriToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }


    override fun onImageClick(position: Int, imageUrls: List<String>) {
        val imageViewerIntent = Intent(requireActivity(), ImageViewerActivity::class.java)
        imageViewerIntent.putExtra("position", position)
        imageViewerIntent.putStringArrayListExtra("images", ArrayList(imageUrls))
        startActivity(
            ImageViewerActivity.createIntent(
                requireContext(),
                ArrayList(imageUrls),
                position,
            ),
        )
    }

    private fun slideUpAnimation(view: View) {
        view.visibility = View.VISIBLE
        val slideUp = ObjectAnimator.ofFloat(view, "translationY", view.height.toFloat(), 0f)
        slideUp.duration = 500
        slideUp.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
            }
        })
        slideUp.start()
    }

    private fun slideDownAnimation(view: View) {
        val slideDown = ObjectAnimator.ofFloat(view, "translationY", 0f, view.height.toFloat())
        slideDown.duration = 500
        slideDown.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
            }
        })
        slideDown.start()
    }

    override fun onClick(user: User) {
        mentionedUsers.add(user)
        val currentText = binding.inputBox.editboxMessage.text.toString()
        val lastAtSymbolIndex = currentText.lastIndexOf('@')
        val mentionedUser = "${user.username} "
        val newText = StringBuilder(currentText)
        if (lastAtSymbolIndex != -1) {
            newText.replace(lastAtSymbolIndex + 1, currentText.length, mentionedUser)
        } else {
            newText.append(mentionedUser)
        }
        binding.inputBox.editboxMessage.setText(newText.toString())
        binding.inputBox.editboxMessage.setSelection(newText.length)
    }

    override fun onLongPress(message: Message,senderName: String) {
        requireContext().performHapticFeedback()
        val moreOptionBottomSheet =
            ChatMoreOptions(message,"Channel Chat",this, senderName)
        moreOptionBottomSheet.show(requireFragmentManager(), "Options")
    }
    override fun onReplyClicked(message: Message,senderName: String) {
        binding.inputBox.replyingToUserTv.text = "Replying to @${senderName}"
        binding.inputBox.referenceMsgTv.text = message.content
        binding.inputBox.replyViewParent.visible()
        binding.inputBox.replyViewParent.animFadein(requireContext(), 150)
        requireActivity().performHapticFeedback()
        replyingTo = message.messageId
    }

    override fun onTaskClick(projectId: String, taskId: String) {
        if (PrefManager.getProjectsList().contains(projectId)){
            if (PrefManager.getcurrentProject()==projectId){
                val intent = Intent(requireContext(), TaskDetailActivity::class.java)
                intent.putExtra("task_id", taskId)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            }
            else{
                val segments=PrefManager.getUnArchivedProjectSegments(projectId)
                Log.d("segment check",segments.toString())

                if (segments.isNotEmpty()){
                    PrefManager.setcurrentsegment(segments[0].segment_NAME)
                    PrefManager.putsectionsList(segments[0].sections.distinct())
//                    viewModel.updateCurrentSegment(segments[0].segment_NAME)
                }
                else{
                    PrefManager.setcurrentsegment("Select Segment")
//                    viewModel.updateCurrentSegment("Select Segment")
                }
//                binding.gioActionbar.titleTv.text = PrefManager.getcurrentsegment()
                val list = PrefManager.getProjectsList()
                var position:Int=0
                for (i in 0 until list.size){
                    if (list[i]==projectId){
                        position=i
                    }
                }
                Log.d("position",position.toString())
                PrefManager.setcurrentProject(projectId)
                PrefManager.setRadioButton(position)
                PrefManager.selectedPosition.value = position
                val intent =
                    Intent(requireActivity(), TaskDetailActivity::class.java)
                intent.putExtra("task_id", taskId)
                intent.putExtra("type", "shareTask")
                startActivity(intent)
                requireActivity().finish()
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_left
                )
                toast("Project has been changed to $projectId")

            }
        }
        else{
            util.showSnackbar(binding.root, "You haven't joined this project so you cant view this task", 500)
        }
    }

    override fun onProjectClick(projectId: String) {
        val uri="${BuildConfig.DYNAMIC_LINK_HOST}/join/$projectId"
        if (PrefManager.getProjectsList().any { it.equals(projectId, ignoreCase = true) }) {
            util.showSnackbar(binding.root,"You have already joined this project",2000)
        }else{
            lifecycleScope.launch {
                val isValidLink = isValidProjectLink(uri.toString())
                if (isValidLink) {
                    showBottomSheetForJoinConfirmation(uri.toString(),projectId)
                } else {
                    util.showSnackbar(binding.root,"Invalid project link",2000)
                }
            }
        }
    }
    private suspend fun isValidProjectLink(projectLink: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = FirebaseFirestore.getInstance().collection("Projects")
                    .whereEqualTo("PROJECT_DEEPLINK", projectLink.toLowerCase())
                    .limit(1)
                    .get()
                    .await()

                return@withContext querySnapshot.documents.isNotEmpty()
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }
    private fun showBottomSheetForJoinConfirmation(projectLink: String,id:String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.deeplink_project_add_sheet, null)

        view.findViewById<Button>(R.id.btnYes).setOnClickThrottleBounceListener {
            addProjectToUser(projectLink)
            bottomSheetDialog.dismiss()
        }

        view.findViewById<TextView>(R.id.projectName).text=id
        view.findViewById<Button>(R.id.btnNo).setOnClickThrottleBounceListener {
            bottomSheetDialog.dismiss()
        }
        view.findViewById<AppCompatImageButton>(R.id.close_btn).setOnClickThrottleBounceListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun addProjectToUser(projectLink: String) {
        CoroutineScope(Dispatchers.Main).launch {
            when (val result = firestoreRepository.addProjectToUser(projectLink)) {
                is ServerResult.Success -> {
                    PrefManager.putProjectsList(result.data)

                    CoroutineScope(Dispatchers.IO).launch {
                        val list = getProjectSegments(PrefManager.getlastaddedproject())
                        val projectTopic = PrefManager.getlastaddedproject().replace("\\s+".toRegex(), "_") + "_TOPIC_GENERAL"

                        FirebaseMessaging.getInstance().subscribeToTopic(projectTopic)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("FCM", "Subscribed to topic successfully")
                                } else {
                                    Log.d("FCM", "Failed to subscribe to topic")
                                }
                            }
                        val newList=list.toMutableList().sortedByDescending { it.creation_DATETIME }
                        PrefManager.saveProjectSegments(PrefManager.getlastaddedproject(), list)
                        withContext(Dispatchers.Main){
                            util.showSnackbar(binding.root,"Successfully joined this project",2000)
                        }
                        if (newList.isNotEmpty()){
                            PrefManager.setLastSegmentsTimeStamp(PrefManager.getlastaddedproject(),newList[0].creation_DATETIME!!)
                        }

                    }
                }
                is ServerResult.Failure -> {
                    Toast.makeText(requireContext(), "Failed to add project: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
    suspend fun getProjectSegments(project: String): List<SegmentItem> {
        val projectsCollection =  FirebaseFirestore.getInstance().collection(Endpoints.PROJECTS)
        val list = mutableListOf<SegmentItem>()

        try {
            val projectsSnapshot = projectsCollection.get().await()
            for (projectDocument in projectsSnapshot.documents) {
                val projectName = projectDocument.id
                val segmentsCollection = projectsCollection.document(project).collection(Endpoints.Project.SEGMENT)
                val segmentsSnapshot = segmentsCollection.get().await()
                for (segmentDocument in segmentsSnapshot.documents) {
                    val segmentName = segmentDocument.id
                    val sections=segmentDocument.get("sections") as MutableList<String>
                    val segment_ID= segmentDocument.getString("segment_ID")
                    val creation_DATETIME= segmentDocument.get("creation_DATETIME") as Timestamp
                    val archived= if (segmentDocument.getBoolean("archived" ).isNull) false else segmentDocument.getBoolean("archived" )
                    val last_updated=  if (segmentDocument.get("last_updated").isNull) Timestamp.now() else segmentDocument.get("last_updated") as Timestamp

                    list.add(SegmentItem(segment_NAME = segmentName, sections = sections, segment_ID = segment_ID!!, creation_DATETIME = creation_DATETIME!!, archived = archived!!,last_updated = last_updated))
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return list
    }

}