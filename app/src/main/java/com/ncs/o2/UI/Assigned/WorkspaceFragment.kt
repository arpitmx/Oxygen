package com.ncs.o2.UI.Assigned

import TaskListAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.WorkspaceTaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentWorkspaceBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class WorkspaceFragment : Fragment(), TaskListAdapter.OnClickListener {

    companion object {
        fun newInstance(sectionName: String): WorkspaceFragment {
            val fragment = WorkspaceFragment()
            val args = Bundle()
            args.putString("sectionName", sectionName)
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var util: GlobalUtils.EasyElements
    @Inject
    lateinit var db: TasksDatabase
    private lateinit var viewModel: AssignedViewModel
    private lateinit var binding: FragmentWorkspaceBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var taskList: ArrayList<TaskItem>
    val sectionName: String? by lazy {
        viewModel.sectionName
    }
    private var taskIdsList:MutableList<WorkspaceTaskItem> = mutableListOf()
    private lateinit var projectName: String
    val state = arrayOf(1)
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWorkspaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val activityBinding: ActivityMainBinding by lazy {
        (requireActivity() as MainActivity).binding
    }

    private val searchCont by lazy {
        activityBinding.gioActionbar.searchCont
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(AssignedViewModel::class.java)
        val sectionName = arguments?.getString("sectionName", "") ?: ""
        viewModel.sectionName = sectionName

        PrefManager.initialize(requireContext())
        projectName = PrefManager.getcurrentProject()
        getTaskIdsList()

        activityBinding.gioActionbar.refresh.setOnClickThrottleBounceListener(1000) {
        }

        binding.swiperefresh.setOnRefreshListener {
            ExtensionsUtil.runDelayed(1000) {
                toast("Page refreshed")
                requireActivity().recreate()
                binding.swiperefresh.isRefreshing = false
            }
        }

    }



    private fun getTaskIdsList(){
        val _sectionName=when(sectionName){
            "Working On" -> "Working"
            "Reviewing" -> "Review"
            else -> sectionName
        }
        viewModel.getUserTasksId(_sectionName!!, projectName = PrefManager.getcurrentProject()) { result ->
            when (result) {
                is ServerResult.Success -> {
                    binding.lottieProgressInclude.progressbarBlock.gone()
                    val task = result.data
                    if (task!!.isEmpty()){
                        binding.layout.gone()
                        binding.placeholder.visible()
                    }
                    else {
                        taskIdsList = task!!.toMutableList()
                        Log.d("workspacecheck",taskIdsList.toString())
                        getTasks()
                    }
                }

                is ServerResult.Failure -> {
                    val errorMessage = result.exception.message
                    binding.lottieProgressInclude.progressbarBlock.gone()
                    util.singleBtnDialog("Failure",
                        "Failure in loading tasks, try again","Reload"
                    ) {
                        setupRecyclerView()
                    }
                }

                is ServerResult.Progress -> {
                    binding.lottieProgressInclude.progressbarBlock.visible()
                }
            }
        }
    }

    private fun getTasks() {
        taskList = ArrayList()
        binding.recyclerView.gone()
        if (db.tasksDao().isNull) {
            CoroutineScope(Dispatchers.Main).launch {
                for (i in 0 until taskIdsList.size) {
                    viewModel.getTasksItembyId(
                        id = taskIdsList[i].id,
                        projectName = projectName
                    ) { result ->
                        when (result) {
                            is ServerResult.Success -> {
                                binding.lottieProgressInclude.progressbarBlock.gone()

                                taskList.add(result.data)
                                Log.d("workspacecheck",taskList.toString())

                                if (taskList.isEmpty()) {
                                    binding.layout.gone()
                                    binding.placeholder.visible()

                                } else {
                                    binding.layout.visible()
                                    binding.lottieProgressInclude.progressbarBlock.gone()
                                    binding.placeholder.gone()
                                    recyclerView = binding.recyclerView

                                    taskListAdapter = TaskListAdapter(
                                        firestoreRepository,
                                        requireContext(),
                                        taskList.toMutableList(),
                                        db
                                    )
                                    taskListAdapter.setTaskList(taskList)
                                    taskListAdapter.notifyDataSetChanged()
                                    taskListAdapter.setOnClickListener(this@WorkspaceFragment)
                                    val layoutManager =
                                        LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                                    layoutManager.reverseLayout = false
                                    with(recyclerView) {
                                        this.layoutManager = layoutManager
                                        adapter = taskListAdapter
                                        edgeEffectFactory = BounceEdgeEffectFactory()
                                        visibility = View.VISIBLE
                                    }

                                    recyclerView.addOnScrollListener(object :
                                        RecyclerView.OnScrollListener() {
                                        override fun onScrollStateChanged(
                                            recyclerView: RecyclerView,
                                            newState: Int
                                        ) {
                                            super.onScrollStateChanged(recyclerView, newState)
                                            state[0] = newState
                                        }

                                        override fun onScrolled(
                                            recyclerView: RecyclerView,
                                            dx: Int,
                                            dy: Int
                                        ) {
                                            super.onScrolled(recyclerView, dx, dy)
                                            if (dy > 0 && (state[0] == 0 || state[0] == 2)) {
                                                hideSearch()
                                            } else if (dy < -10) {
                                                showSearch()
                                            }
                                        }
                                    })
                                }
                            }

                            is ServerResult.Failure -> {
                                val errorMessage = result.exception.message
                                binding.lottieProgressInclude.progressbarBlock.gone()
                                util.singleBtnDialog(
                                    "Failure",
                                    "Failure in loading tasks, try again", "Reload"
                                ) {
                                    setupRecyclerView()
                                }

                            }

                            is ServerResult.Progress -> {
                                binding.lottieProgressInclude.progressbarBlock.visible()
                            }

                        }
                    }
                }
            }
        }
        else{
            Log.d("fetchfrom","DB")
            fetchfromdb()
        }

    }
    fun fetchfromdb(){
        val tasks: MutableList<Task> = mutableListOf()
        for (i in 0 until taskIdsList.size) {
            viewModel.getTasksbyIdFromDB(
                projectName = projectName,
                taskId = taskIdsList[i].id,
                ) { result ->
                when (result) {
                    is DBResult.Success -> {
                        binding.lottieProgressInclude.progressbarBlock.gone()
                        tasks.add(result.data)
                        if (tasks.isEmpty()) {
                            binding.layout.gone()
                            binding.placeholder.visible()

                        } else {
                            val taskItems: List<TaskItem> = tasks.map { task ->
                                TaskItem(
                                    title = task.title!!,
                                    id = task.id,
                                    assignee_id = task.assignee!!,
                                    difficulty = task.difficulty!!,
                                    timestamp = task.time_STAMP,
                                    completed = if (SwitchFunctions.getStringStateFromNumState(task.status!!)=="Completed") true else false,
                                    tagList = task.tags
                                )
                            }
                            binding.layout.visible()
                            binding.lottieProgressInclude.progressbarBlock.gone()
                            binding.placeholder.gone()
                            recyclerView = binding.recyclerView
                            taskListAdapter = TaskListAdapter(
                                firestoreRepository,
                                requireContext(),
                                taskItems.toMutableList(),
                                db
                            )
                            taskListAdapter.notifyDataSetChanged()
                            taskListAdapter.setOnClickListener(this@WorkspaceFragment)
                            val layoutManager =
                                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                            layoutManager.reverseLayout = false
                            with(recyclerView) {
                                this.layoutManager = layoutManager
                                adapter = taskListAdapter
                                edgeEffectFactory = BounceEdgeEffectFactory()
                                visibility = View.VISIBLE
                            }

                            recyclerView.addOnScrollListener(object :
                                RecyclerView.OnScrollListener() {
                                override fun onScrollStateChanged(
                                    recyclerView: RecyclerView,
                                    newState: Int
                                ) {
                                    super.onScrollStateChanged(recyclerView, newState)
                                    state[0] = newState
                                }

                                override fun onScrolled(
                                    recyclerView: RecyclerView,
                                    dx: Int,
                                    dy: Int
                                ) {
                                    super.onScrolled(recyclerView, dx, dy)
                                    if (dy > 0 && (state[0] == 0 || state[0] == 2)) {
                                        hideSearch()
                                    } else if (dy < -10) {
                                        showSearch()
                                    }
                                }
                            })
                        }
                    }

                    is DBResult.Failure -> {
                        val errorMessage = result.exception.message
                        binding.lottieProgressInclude.progressbarBlock.gone()
                        util.singleBtnDialog(
                            "Failure",
                            "Failure in loading tasks, try again", "Reload"
                        ) {
                            setupRecyclerView()
                        }

                    }

                    is DBResult.Progress -> {
                        binding.lottieProgressInclude.progressbarBlock.visible()
                    }

                }

            }

        }
    }
    private fun setupRecyclerView(){

    }

    private fun showSearch() {
    }

    private fun hideSearch() {
    }

    override fun onCLick(position: Int, task: TaskItem) {
        val intent = Intent(requireContext(), TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }
}