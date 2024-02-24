package com.ncs.o2.UI.Tasks.Sections

import TaskListAdapter
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.runDelayed
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TasksHolderFragment
import com.ncs.o2.UI.UIComponents.BottomSheets.MoreOptionsTasksBottomSheet
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentTaskSectionBinding
import com.ncs.o2.databinding.MoreOptionBottomSheetBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class TaskSectionFragment() : Fragment(), TaskListAdapter.OnClickListener,MoreOptionsTasksBottomSheet.OnArchive {

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
    lateinit var db: TasksDatabase
    private lateinit var viewModel: TaskSectionViewModel
    private lateinit var binding: FragmentTaskSectionBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var taskList: ArrayList<TaskItem>
    private var taskItems: MutableList<TaskItem>  = mutableListOf()

    private lateinit var tasks: ArrayList<Task>
    private var taskList2: MutableList<Task> = mutableListOf()
    private lateinit var projectName: String
    private lateinit var segmentName: String
    val state = arrayOf(1)

    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    val sectionName: String? by lazy {
        viewModel.sectionName
    }

    private val activityBinding: MainActivity by lazy {
        (requireActivity() as MainActivity)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskSectionBinding.inflate(inflater, container, false)
        return binding.root
    }




    private val searchCont by lazy {
        activityBinding.binding.gioActionbar.searchCont
    }
    private val segmentText by lazy {
        activityBinding.binding.gioActionbar.titleTv
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
        manageViews()
    }


    private fun manageViews(){
        activityBinding.binding.gioActionbar.tabLayout.visible()
        activityBinding.binding.gioActionbar.actionbar.visible()
        activityBinding.binding.gioActionbar.constraintLayout2.visible()
        activityBinding.binding.gioActionbar.searchCont.gone()
        activityBinding.binding.gioActionbar.constraintLayoutsearch.gone()
        activityBinding.binding.gioActionbar.constraintLayoutworkspace.gone()
        activityBinding.binding.gioActionbar.constraintLayoutTeams.gone()
    }

    private fun setupViews() {

        binding.parent.gone()
        binding.parent.animFadein(requireContext(),300)
        binding.parent.visible()

        showLoader(1)
        setupRecyclerView()

    }

    private fun showLoader(show: Int) {

        if (show == 1) {

            // Tasks loading

            binding.layout.visible()
            binding.progressbarBlock.visible()

            binding.recyclerView.gone()
            binding.placeholder.gone()

        } else if (show == 0) {

            //Tasks loaded

            binding.layout.visible()
            binding.recyclerView.visible()

            binding.progressbarBlock.gone()
            binding.placeholder.gone()

        } else if (show == -1) {

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
            Log.d("fetch", "fetching from firestore")
            viewModel.getTasksItemsForSegment(
                projectName,
                segmentName,
                viewModel.sectionName!!
            ) { result ->
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
                            taskListAdapter =
                                TaskListAdapter(firestoreRepository, requireContext(), taskList, db)
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
        } else {

            tasks = ArrayList()
            Log.d("fetch", "fetching from DB")
            fetchfromdb()
        }

        binding.swiperefresh.setOnRefreshListener {
            syncCache(PrefManager.getcurrentProject())
        }
        activityBinding.binding.gioActionbar.refresh.setOnClickThrottleBounceListener {
        }


    }

    private fun syncCache(projectName:String){
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Please wait, Syncing Tasks")
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main).launch {
            try {

                val taskResult = withContext(Dispatchers.IO) {
                    firestoreRepository.getTasksinProjectAccordingtoTimeStamp(projectName)
                }

                Timber.tag(TaskDetailsFragment.TAG).d("Fetched task result : ${taskResult}")

                when (taskResult) {

                    is ServerResult.Failure -> {
                        progressDialog.dismiss()
                        binding.swiperefresh.isRefreshing=false
                    }

                    is ServerResult.Progress -> {
                        progressDialog.show()
                        progressDialog.setMessage("Please wait, Syncing Tasks")
                    }

                    is ServerResult.Success -> {

                        val tasks = taskResult.data
                        if (tasks.isNotEmpty()){
                            val newList=taskResult.data.toMutableList().sortedByDescending { it.time_STAMP }
                            PrefManager.setLastTaskTimeStamp(projectName,newList[0].last_updated!!)
                            for (task in tasks) {
                                db.tasksDao().insert(task)
                            }
                        }
                        progressDialog.dismiss()
                        toast("Page refreshed")
                        requireActivity().recreate()
                        binding.swiperefresh.isRefreshing=false
                    }

                }

            } catch (e: java.lang.Exception) {
                Timber.tag(TaskDetailsFragment.TAG).e(e)
                progressDialog.dismiss()


            }

        }
    }

    fun fetchfromdb() {

        viewModel.getTasksForSegmentFromDB(
            projectName,
            segmentName,
            viewModel.sectionName!!
        ) { result ->
            when (result) {
                is DBResult.Success -> {

                    val filteredList = filterTasks(result.data)

                    showLoader(1)

                    val task = filteredList
                    Log.d("fetch", task.toString())

                    tasks.clear()

                    for (element in task) {
                        tasks.add(element)
                    }

                    if (filteredList.isEmpty()) {
                        showLoader(-1)
                    } else {

                        recyclerView = binding.recyclerView
                        taskItems= filteredList.map { task ->
                            TaskItem(
                                title = task.title!!,
                                id = task.id,
                                assignee_id = task.assignee!!,
                                difficulty = task.difficulty!!,
                                timestamp = task.time_STAMP,
                                completed = if (SwitchFunctions.getStringStateFromNumState(task.status!!) == "Completed") true else false,
                                tagList = task.tags,
                                last_updated = task.last_updated
                            )
                        }.toMutableList()
                        taskListAdapter = TaskListAdapter(
                            firestoreRepository,
                            requireContext(),
                            taskItems.sortedByDescending { it.timestamp }.toMutableList(),
                            db
                        )
                        taskListAdapter.setOnClickListener(this)
                        val layoutManager =
                            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                        layoutManager.reverseLayout = false
                        with(recyclerView) {
                            this.layoutManager = layoutManager
                            adapter = taskListAdapter
                            edgeEffectFactory = BounceEdgeEffectFactory()
                        }
                        taskListAdapter.notifyDataSetChanged()

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

    fun fetchNewTasks() {
        viewModel.getTasksForSegmentFromDB(
            projectName,
            segmentName,
            viewModel.sectionName!!
        ) { result ->
            when (result) {
                is DBResult.Success -> {
                    val filteredList = filterTasks(result.data)
                    val newtaskItems= filteredList.map { task ->
                        TaskItem(
                            title = task.title!!,
                            id = task.id,
                            assignee_id = task.assignee!!,
                            difficulty = task.difficulty!!,
                            timestamp = task.time_STAMP,
                            completed = if (SwitchFunctions.getStringStateFromNumState(task.status!!) == "Completed") true else false,
                            tagList = task.tags,
                            last_updated = task.last_updated
                        )
                    }.toMutableList()
                    val diffResult = calculateDiff(taskItems, newtaskItems)
                    diffResult.dispatchUpdatesTo(taskListAdapter)
                }

                is DBResult.Failure -> {
                    val errorMessage = result.exception.message
                    // Handle failure if needed
                }

                is DBResult.Progress -> {
                    // Handle progress if needed
                }
            }
        }
    }

    private fun calculateDiff(oldList: List<TaskItem>, newList: List<TaskItem>): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        })
    }

    private fun showSearch() {
    }

    private fun hideSearch() {
        searchCont.gone()
    }

    override fun onCLick(position: Int, task: TaskItem) {
        viewModel.isReturning=true
        viewModel.scrollPosition=position
        val intent = Intent(requireContext(), TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    override fun onLongClick(position: Int, task: TaskItem) {
        requireContext().performHapticFeedback()
        val moreOptionsTasksBottomSheet = MoreOptionsTasksBottomSheet(task,this)
        moreOptionsTasksBottomSheet.show(requireFragmentManager(), "More Task Options")

    }

    private fun movetotaskspage() {
        val transaction = fragmentManager?.beginTransaction()!!
        val fragment = TasksHolderFragment()
        transaction.replace(R.id.nav_host_fragment_activity_main, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        activityBinding.binding.bottomNav.menu.getItem(0).isChecked = true
        activityBinding.binding.bottomNav.menu.getItem(0).setIcon(R.drawable.baseline_article_24)
    }
    fun filterTasks(data: List<Task>): List<Task> {
        var list = mutableListOf<Task>()
        list.addAll(data)
        val segments = PrefManager.getProjectSegments(PrefManager.getcurrentProject())
        list = list.filter { task ->
            val segmentName = task.segment
            val segment = segments.find { it.segment_NAME == segmentName }
            segment?.archived != true
        }.toMutableList()
        val sortedList = list.sortedByDescending { it.time_STAMP }.filter { !it.archived }
        return sortedList
    }

    override fun onTaskArchive(taskItem: TaskItem) {
//        fetchfromdb()
        requireActivity().recreate()
    }

}