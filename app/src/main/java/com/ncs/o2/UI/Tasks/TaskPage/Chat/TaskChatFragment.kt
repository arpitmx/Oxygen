package com.ncs.o2.UI.Tasks.TaskPage.Chat

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Data.Room.MessageRepository.UsersDao
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Enums.MessageType
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Chat.Adapters.ChatAdapter
import com.ncs.o2.UI.Tasks.TaskPage.Details.ImageViewerActivity
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailViewModel
import com.ncs.o2.databinding.FragmentTaskChatBinding
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
import io.noties.prism4j.Prism4j
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.util.concurrent.Executors
import javax.inject.Inject


@AndroidEntryPoint
class TaskChatFragment : Fragment(), ChatAdapter.onChatDoubleClickListner,
    ChatAdapter.onImageClicked {
    @Inject
    @FirebaseRepository
    lateinit var repository: Repository

    @Inject
    lateinit var utils: GlobalUtils.EasyElements
    lateinit var binding: FragmentTaskChatBinding
    lateinit var messageDatabase: MessageDatabase
    lateinit var db: UsersDao
    private val viewModel: TaskDetailViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    lateinit var task: Task
    private lateinit var markwon: Markwon
    private lateinit var mdEditor: MarkwonEditor
    lateinit var chatAdapter: ChatAdapter


    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val CAMERA_PERMISSION_REQUEST = 100
    private var bitmap: Bitmap? = null
    lateinit var imageUri: Uri

    @Inject
    lateinit var util: GlobalUtils.EasyElements
    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDetails(activityBinding.taskId)
        setUpChatbox()
        initViews()
    }

    private fun initViews() {

        binding.inputBox.btnSend.setOnClickThrottleBounceListener {

            if (binding.inputBox.editboxMessage.text.toString().trim().isNotEmpty()) {

                sendMessageProcess()

                binding.inputBox.editboxMessage.text?.clear()
            } else if (bitmap != null) {
                uploadImageToFirebaseStorage(bitmap!!, PrefManager.getcurrentProject(), task.id)
            } else {
                toast("Message can't be empty")
            }
        }

    }

    private fun sendMessageProcess() {

        val message = Message(
            messageId = RandomIDGenerator.generateRandomId(),
            senderId = PrefManager.getcurrentUserdetails().EMAIL,
            content = binding.inputBox.editboxMessage.text?.trim().toString(),
            messageType = MessageType.NORMAL_MSG,
            timestamp = Timestamp.now()
        )
        postMessage(message)
    }

    private fun setUpChatbox() {
        val markdownEditor = MarkwonEditor.builder(markwon).build()
        binding.inputBox.editboxMessage.addTextChangedListener(
            MarkwonEditorTextWatcher.withPreRender(
                markdownEditor,
                Executors.newCachedThreadPool(),
                binding.inputBox.editboxMessage
            )
        )
        val message = Message(
            messageId = RandomIDGenerator.generateRandomId(),
            senderId = PrefManager.getcurrentUserdetails().EMAIL,
            content = binding.inputBox.editboxMessage.text.trim().toString(),
            messageType = MessageType.NORMAL_MSG,
            timestamp = Timestamp.now()
        )
        postMessage(message)
        binding.inputBox.editboxMessage.text.clear()
    }
    else if (bitmap!=null)
    {
        uploadImageToFirebaseStorage(bitmap!!, PrefManager.getcurrentProject(), task.id)
    }
    else
    {
        toast("Message can't be empty")
    }
}
    binding.inputBox.btnAttachImage.setOnClickThrottleBounceListener {
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

private fun uploadImageToFirebaseStorage(bitmap: Bitmap, projectId: String, taskId: String) {

    chatViewModel.uploadImage(bitmap, projectId, taskId).observe(viewLifecycleOwner) { result ->

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
        timestamp = Timestamp.now()
    )
    postMessage(message)

}


private fun selectImage() {
    if (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    } else {
        pickImage()
    }
}

private fun pickImage() {

    val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
    val builder = AlertDialog.Builder(requireContext())
    builder.setTitle("Choose an option")
    builder.setItems(options) { dialog, item ->
        when (options[item]) {
            "Take Photo" -> {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
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

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == Activity.RESULT_OK) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                val imageBitmap = data?.extras?.get("data") as Bitmap
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

override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
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
    val recyclerView = binding.chatRecyclerview
    chatAdapter = ChatAdapter(firestoreRepository, mutableListOf(), requireContext(), this,this,activityBinding.users, markwon)
    val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    layoutManager.reverseLayout = false
    layoutManager.stackFromEnd = true

    with(recyclerView) {
        this.layoutManager = layoutManager
        adapter = chatAdapter
        edgeEffectFactory = BounceEdgeEffectFactory()
    }

    chatViewModel.getMessages(PrefManager.getcurrentProject(), task.id) { result ->
        when (result) {
            is ServerResult.Success -> {
                if (result.data.isEmpty()) {
                    binding.progress.gone()
                    recyclerView.gone()
                    binding.placeholder.visible()
                } else {
                    chatAdapter.appendMessages(result.data)
                    binding.progress.gone()
                    recyclerView.visible()
                    binding.placeholder.gone()
                    recyclerView.scrollToPosition(result.data.size - 1)
                }
            }

            is ServerResult.Failure -> {
                binding.progress.gone()
                val errorMessage = result.exception.message
                GlobalUtils.EasyElements(requireContext())
                    .singleBtnDialog(
                        "Failure",
                        "Failed to load messages with error: $errorMessage",
                        "Okay"
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
}


fun postMessage(message: Message) {

    val recyclerView = binding.chatRecyclerview
    CoroutineScope(Dispatchers.Main).launch {

        repository.postMessage(
            projectName = task.project_ID,
            taskId = task.id,
            message = message
        ) { result ->

            when (result) {

                is ServerResult.Failure -> {
                    binding.inputBox.btnSend.visible()
                    binding.inputBox.progressBarSendMsg.gone()
                }

                ServerResult.Progress -> {
                    binding.inputBox.btnSend.gone()
                    binding.inputBox.progressBarSendMsg.visible()
                }

                is ServerResult.Success -> {
                    if (message.messageType == MessageType.NORMAL_MSG) {
                        binding.inputBox.btnSend.visible()
                        binding.inputBox.progressBarSendMsg.gone()
                        binding.inputBox.editboxMessage.text.clear()
                    }
                    if (message.messageType == MessageType.IMAGE_MSG) {
                        bitmap = null
                        binding.btnSelectImageFromStorage.gone()
                        binding.inputBox.selectedImageView.gone()
                        binding.inputBox.msgBox.visible()
                        binding.inputBox.btnSend.visible()
                        binding.inputBox.progressBarSendMsg.gone()
                        binding.inputBox.editboxMessage.text.clear()
                    }
                    recyclerView.scrollToPosition(chatAdapter.itemCount - 1)


                    // sendNotification(receiverStack)

                }

            }
        }
    }
}

    private fun sendNotification(receiverList : Set<String>) {

        try {
            CoroutineScope(Dispatchers.IO).launch {
                for (receiverToken in receiverList){
                    NotificationsUtils.sendFCMNotification(receiverToken)
                }

            }


        }catch (exception : Exception){
            Timber.tag("")
            utils.showSnackbar(binding.root,"Failure in sending notifications",5000)
        }

    }

    private fun setDetails(id: String) {

    viewLifecycleOwner.lifecycleScope.launch {

        try {

            val taskResult = withContext(Dispatchers.IO) {
                viewModel.getTasksById(id, PrefManager.getcurrentProject())
            }

            Timber.tag(TaskDetailsFragment.TAG).d("Fetched task result : ${taskResult}")

            when (taskResult) {

                is ServerResult.Failure -> {
                    binding.progress.gone()
                }

                is ServerResult.Progress -> {
                    binding.progress.visible()
                }

                is ServerResult.Success -> {
                    binding.progress.gone()
                    task = taskResult.data
                    setMessages()
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


private val markwon: Markwon by lazy {

    // *NOTE @O2 team : If ExampleGrammarLocator class is not found after pull, // just hit run, this class is built at compile time*

    val prism4j = Prism4j(ExampleGrammarLocator())

    // *NOTE*

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
                    .blockQuoteColor(requireContext().getColor(R.color.primary))
                    .linkColor(requireContext().getColor(R.color.primary))
                    .codeBlockTextSize(30)
            }
        })

        .build()
    }

}

override fun onDoubleClickListner(msg: Message, senderName: String) {
    val replyFormat =
        """
            >${msg.content}
          
            **@${senderName}**
            
            """.trimIndent()
    binding.inputBox.editboxMessage.setText(replyFormat)
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




}