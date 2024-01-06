package com.ncs.o2.UI.Tasks.Sections

import TaskListAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TasksHolderFragment
import com.ncs.o2.UI.UIComponents.Adapters.TagAdapter
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentTaskSectionBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class TaskSectionFragment : Fragment(), TaskListAdapter.OnClickListener {

    companion object {
        fun newInstance(sectionName: String): TaskSectionFragment {
            val fragment = TaskSectionFragment()
            val args = Bundle()
            args.putString("sectionName", sectionName)
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var util: GlobalUtils.EasyElements
    @Inject
    lateinit var db:TasksDatabase

    private lateinit var viewModel: TaskSectionViewModel
    private lateinit var binding: FragmentTaskSectionBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var taskList: ArrayList<TaskItem>
    private  lateinit var tasks: ArrayList<Task>
    private lateinit var taskList2: ArrayList<Task>
    private lateinit var projectName: String
    private lateinit var segmentName: String
    val state = arrayOf(1)

    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    val sectionName: String? by lazy {
        viewModel.sectionName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskSectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val activityBinding: ActivityMainBinding by lazy {
        (requireActivity() as MainActivity).binding
    }




    private val searchCont by lazy {
        activityBinding.gioActionbar.searchCont
    }
    private val segmentText by lazy {
        activityBinding.gioActionbar.titleTv
    }


    override fun onResume() {
        super.onResume()

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(TaskSectionViewModel::class.java)
        val sectionName = arguments?.getString("sectionName", "") ?: ""

        projectName = PrefManager.getcurrentProject()
        segmentName = PrefManager.getcurrentsegment()
        viewModel.sectionName = sectionName

        setupViews()

    }

    private fun setupViews() {

        showLoader(1)
        setupRecyclerView()
//        if (segmentName == "Select Segment") {
//            activityBinding.placeholderText.visible()
//            activityBinding.navHostFragmentActivityMain.gone()
//            activityBinding.gioActionbar.tabLayout.gone()
//            activityBinding.gioActionbar.searchCont.gone()
//            activityBinding.gioActionbar.line.gone()
//        } else {
//            activityBinding.placeholderText.gone()
//            activityBinding.navHostFragmentActivityMain.visible()
//            activityBinding.gioActionbar.tabLayout.visible()
//            activityBinding.gioActionbar.searchCont.visible()
//            activityBinding.gioActionbar.line.visible()
//            setupRecyclerView()
//        }
    }

    private fun showLoader(show : Int){

        if (show == 1){

            // Tasks loading

            binding.layout.visible()
            binding.progressbarBlock.visible()

            binding.recyclerView.gone()
            binding.placeholder.gone()

        }else if (show == 0){

            //Tasks loaded

            binding.layout.visible()
            binding.recyclerView.visible()

            binding.progressbarBlock.gone()
            binding.placeholder.gone()

        }else if (show == -1){

            //Empty tasks

            binding.layout.gone()
            binding.recyclerView.gone()
            binding.progressbarBlock.gone()

            binding.placeholder.visible()

        }
    }


    private fun setupRecyclerView() {

        if (db.tasksDao().isNull) {
            taskList = ArrayList()
            Log.d("fetch","fetching from firestore")
            viewModel.getTasksItemsForSegment(projectName, segmentName, viewModel.sectionName!!) { result ->
                when (result) {
                    is ServerResult.Success -> {

                        showLoader(1)

                        val task = result.data
                        taskList.clear()

                        for (element in task) {
                            taskList.add(element)
                        }

                        if (taskList.isEmpty()) {

                            showLoader(-1)

                        } else {

                            recyclerView = binding.recyclerView
                            taskListAdapter = TaskListAdapter(firestoreRepository, requireContext(),taskList,db)
                            taskListAdapter.setOnClickListener(this)
                            val layoutManager =
                                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                            layoutManager.reverseLayout = false
                            with(recyclerView) {
                                this.layoutManager = layoutManager
                                adapter = taskListAdapter
                                edgeEffectFactory = BounceEdgeEffectFactory()
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

                            taskListAdapter.setTaskList(taskList)
                            taskListAdapter.notifyDataSetChanged()
                            showLoader(0)


                        }

                    }

                    is ServerResult.Failure -> {
                        val errorMessage = result.exception.message
                        showLoader(-1)
                        util.singleBtnDialog(
                            "Failure",
                            "Failure in loading tasks, try again : ${errorMessage}", "Reload"
                        ) {
                            setupRecyclerView()
                        }
                    }

                    is ServerResult.Progress -> {
                        showLoader(1)
                    }

                }

            }
        }
        else{

            tasks = ArrayList()
            Log.d("fetch","fetching from DB")
            fetchfromdb()
        }



        activityBinding.gioActionbar.refresh.setOnClickThrottleBounceListener {
            requireActivity().recreate()
        }


    }



    fun fetchfromdb(){
        viewModel.getTasksForSegmentFromDB(projectName, segmentName, viewModel.sectionName!!) { result ->
            when (result) {
                is DBResult.Success -> {

                    showLoader(1)

                    val task = result.data
                    Log.d("fetch", task.toString())

                    tasks.clear()

                    for (element in task) {
                        tasks.add(element)
                    }

                    if (result.data.isEmpty()) {
                        showLoader(-1)
                    } else {

                        recyclerView = binding.recyclerView
                        val taskItems: List<TaskItem> = result.data.map { task ->
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
                        val taskadapter = TaskListAdapter(
                            firestoreRepository,
                            requireContext(),
                            taskItems.toMutableList(),
                            db
                        )
                        taskadapter.setOnClickListener(this)
                        val layoutManager =
                            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                        layoutManager.reverseLayout = false
                        with(recyclerView) {
                            this.layoutManager = layoutManager
                            adapter = taskadapter
                            edgeEffectFactory = BounceEdgeEffectFactory()
                        }
                        taskadapter.notifyDataSetChanged()

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
                        showLoader(0)

                    }

                }

                is DBResult.Failure -> {
                    val errorMessage = result.exception.message
                    showLoader(-1)
                    util.singleBtnDialog(
                        "Failure",
                        "Failure in loading tasks, try again : ${errorMessage}", "Reload"
                    ) {
                        setupRecyclerView()
                    }
                }

                is DBResult.Progress -> {
                    showLoader(1)
                }

            }
        }
    }
    private fun showSearch() {
        searchCont.visible()
    }

    private fun hideSearch() {
        searchCont.gone()
    }

    override fun onCLick(position: Int, task: TaskItem) {
        val intent = Intent(requireContext(), TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }
    private fun movetotaskspage() {
        val transaction = fragmentManager?.beginTransaction()!!
        val fragment = TasksHolderFragment()
        transaction.replace(R.id.nav_host_fragment_activity_main, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        activityBinding.bottomNav.menu.getItem(0).isChecked = true
        activityBinding.bottomNav.menu.getItem(0).setIcon(R.drawable.baseline_article_24)
    }
}