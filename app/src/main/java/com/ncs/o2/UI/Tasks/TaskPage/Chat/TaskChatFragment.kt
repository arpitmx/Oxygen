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
import androidx.room.Room
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
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.Tasks.TaskPage.Chat.Adapters.ChatAdapter
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailViewModel
import com.ncs.o2.databinding.FragmentTaskChatBinding
import com.ncs.versa.Constants.Endpoints
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TaskChatFragment : Fragment() {
    @Inject
    @FirebaseRepository
    lateinit var repository: Repository
    lateinit var binding: FragmentTaskChatBinding
    lateinit var messageDatabase: MessageDatabase
    lateinit var db: UsersDao
    private val viewModel: TaskDetailViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    lateinit var task: Task
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
        setdetails(activityBinding.taskId)
        messageDatabase = Room.databaseBuilder(
            requireContext(),
            MessageDatabase::class.java,
            Endpoints.ROOM.MESSAGES.USERLIST_DB
        ).build()
        db = messageDatabase.usersDao()

        binding.inputBox.btnSend.setOnClickThrottleBounceListener {

            if (binding.inputBox.editboxMessage.text.toString().isNotEmpty()) {

                val message = Message(
                    messageId = RandomIDGenerator.generateRandomId(),
                    senderId = PrefManager.getcurrentUserdetails().EMAIL,
                    content = binding.inputBox.editboxMessage.text.trim().toString(),
                    messageType = MessageType.NORMAL_MSG,
                    timestamp = Timestamp.now()
                )
                postMessage(message)

            } else {
                toast("Message can't be empty")
            }
        }

    }

    private fun setMessages() {
        val recyclerView = binding.chatRecyclerview

        chatViewModel.getMessages(PrefManager.getcurrentProject(), task.id) { result ->
            when (result) {
                is ServerResult.Success -> {

                    val chatAdapter = ChatAdapter(
                        firestoreRepository,
                        result.data.toMutableList(),
                        requireContext()
                    )
                    val layoutManager =
                        LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    layoutManager.reverseLayout = false
                    with(recyclerView) {
                        this.layoutManager = layoutManager
                        adapter = chatAdapter
                        edgeEffectFactory = BounceEdgeEffectFactory()
                    }
                    recyclerView.scrollToPosition(result.data.size - 1)

                }

                is ServerResult.Failure -> {
                    binding.progress.gone()
                    val errorMessage = result.exception.message
                    GlobalUtils.EasyElements(requireContext())
                        .singleBtnDialog(
                            "Failure",
                            "Failed to load messages with error : $errorMessage",
                            "Okay"
                        ) {
                            requireActivity().finish()
                        }
                }

                is ServerResult.Progress -> {
                    binding.progress.visible()
                }

            }

        }

    }

    fun postMessage(message: Message) {
        CoroutineScope(Dispatchers.Main).launch {

            repository.postMessage(
                projectName = task.project_ID,
                taskId = task.id,
                message = message
            ) { result ->

                when (result) {

                    is ServerResult.Failure -> {
                        binding.progress.gone()
                    }

                    ServerResult.Progress -> {
                        binding.progress.visible()

                    }

                    is ServerResult.Success -> {
                        binding.progress.gone()
                        binding.inputBox.editboxMessage.text.clear()

                    }

                }
            }
        }
    }

    private fun setdetails(id: String) {

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

//                utils.singleBtnDialog(
//                    "Failure", "Failure in Task exception : ${e.message}", "Okay"
//                ) {
//                    requireActivity().finish()
//                }

            }

        }
    }

}