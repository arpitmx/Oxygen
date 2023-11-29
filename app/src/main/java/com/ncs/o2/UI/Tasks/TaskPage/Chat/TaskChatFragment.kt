package com.ncs.o2.UI.Tasks.TaskPage.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
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
import com.ncs.o2.Domain.Utility.NotificationsUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Chat.Adapters.ChatAdapter
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
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject



@AndroidEntryPoint
class TaskChatFragment : Fragment(), ChatAdapter.onChatDoubleClickListner {
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
    private lateinit var mdEditor: MarkwonEditor
    lateinit var chatAdapter: ChatAdapter


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

//        messageDatabase = Room.databaseBuilder(
//            requireContext(),
//            MessageDatabase::class.java,
//            Endpoints.ROOM.MESSAGES.USERLIST_DB
//        ).build()
//
//        db = messageDatabase.usersDao()


        setUpChatbox()

        binding.inputBox.btnSend.setOnClickThrottleBounceListener {

            if (binding.inputBox.editboxMessage.text.toString().trim().isNotEmpty()) {

                sendMessageProcess()

                binding.inputBox.editboxMessage.text?.clear()
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
                binding.inputBox.editboxMessage)
            )

    }

    private fun setMessages() {
        val recyclerView = binding.chatRecyclerview
        chatAdapter =
            ChatAdapter(firestoreRepository, mutableListOf(), requireContext(), this, markwon)
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
                        binding.inputBox.btnSend.visible()
                        binding.inputBox.progressBarSendMsg.gone()
                        binding.inputBox.editboxMessage.text?.clear()
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


    override fun onDoubleClickListner(msg: Message, senderName: String) {
        val replyFormat =
            """
            >${msg.content}
          
            **@${senderName}**
            
            """.trimIndent()
        binding.inputBox.editboxMessage.setText(replyFormat)
    }


}