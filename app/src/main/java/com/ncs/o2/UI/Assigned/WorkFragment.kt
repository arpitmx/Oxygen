package com.ncs.o2.UI.Assigned

import TaskListAdapter
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.TodayTasks
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.runDelayed
import com.ncs.o2.Domain.Utility.ExtensionsUtil.set180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Teams.TasksHolderActivity
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentAssignedBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class WorkFragment : Fragment() , TaskListAdapter.OnClickListener {

    companion object {
        fun newInstance() = WorkFragment()
    }
    lateinit var binding: FragmentAssignedBinding
    private val viewModel: AssignedViewModel by viewModels()
    private lateinit var taskListAdapter: TaskListAdapter
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    private val activityBinding: ActivityMainBinding by lazy {
        (requireActivity() as MainActivity).binding
    }
    var todays:MutableList<TodayTasks> = mutableListOf()

    @Inject
    lateinit var db:TasksDatabase
    var isGridVisible=true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAssignedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpViews()
        manageviews()
        setUpUserWorkspace()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }


    override fun onCLick(position: Int, task: TaskItem) {
        val intent = Intent(requireContext(), TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    override fun onLongClick(position: Int, task: TaskItem) {
        TODO("Not yet implemented")
    }

    private fun setUpViews(){


        binding.parentScrollview.gone()
        binding.parentScrollview.animFadein(requireContext(),300)
        binding.parentScrollview.visible()


        OverScrollDecoratorHelper.setUpOverScroll(binding.parentScrollview)
        OverScrollDecoratorHelper.setUpOverScroll(binding.extendedAssigned)

        binding.assigned.statParent.setOnClickThrottleBounceListener {
            startActivity("WorkspaceAssigned")
        }
        binding.ongoing.statParent.setOnClickThrottleBounceListener {
            startActivity("WorkspaceWorking")
        }
        binding.review.statParent.setOnClickThrottleBounceListener {
            startActivity("WorkspaceReview")
        }
        binding.completed.statParent.setOnClickThrottleBounceListener {
            startActivity("WorkspaceCompleted")
        }
        binding.moderating.setOnClickThrottleBounceListener {
            startActivity("moderating")
        }
        binding.openedBy.setOnClickThrottleBounceListener {
            startActivity("opened")
        }
        binding.favs.setOnClickThrottleBounceListener {
            startActivity("Favs")
        }
        binding.today.setOnClickThrottleBounceListener {
            val intent = Intent(requireContext(), TodayActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }

        binding.assignedGrid.setOnClickListener {
            binding.arrowStats.set180(requireContext())
            if(!isGridVisible){
                isGridVisible=true
                binding.extendedAssigned.visible()
            }
            else{
                isGridVisible=false
                binding.extendedAssigned.gone()
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            syncCache(PrefManager.getcurrentProject())
        }

    }

    override fun onResume() {
        super.onResume()
        setUpUserWorkspace()
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


                when (taskResult) {

                    is ServerResult.Failure -> {
                        progressDialog.dismiss()
                        binding.swiperefresh.isRefreshing=false
                        requireActivity().recreate()

                    }

                    is ServerResult.Progress -> {
                        progressDialog.show()
                        progressDialog.setMessage("Please wait, Syncing Tasks")

                    }

                    is ServerResult.Success -> {

                        val tasks = taskResult.data
                        if (tasks.isNotEmpty()){
                            val newList=taskResult.data.toMutableList().sortedByDescending { it.last_updated }
                            PrefManager.setLastTaskTimeStamp(projectName,newList[0].last_updated!!)
                            for (task in tasks) {
                                db.tasksDao().insert(task)
                            }
                        }
                        progressDialog.dismiss()
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


    private fun startActivity(type:String){
        val intent = Intent(requireContext(), TasksHolderActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("index", "1")
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    private fun manageviews(){
        val drawerLayout = activityBinding.drawer
        activityBinding.gioActionbar.btnHamWorkspace.setOnClickThrottleBounceListener {
            val gravity = if (!drawerLayout.isDrawerOpen(GravityCompat.START)) GravityCompat.START else GravityCompat.END
            drawerLayout.openDrawer(gravity)
        }
        activityBinding.gioActionbar.tabLayout.gone()
        activityBinding.gioActionbar.actionbar.visible()
        activityBinding.gioActionbar.constraintLayout2.gone()
        activityBinding.gioActionbar.constraintLayoutsearch.gone()
        activityBinding.gioActionbar.constraintLayoutTeams.gone()
        activityBinding.gioActionbar.searchCont.gone()
        activityBinding.gioActionbar.constraintLayoutworkspace.visible()
    }
    private fun setUpUserWorkspace(){
        CoroutineScope(Dispatchers.IO).launch {

            val assignedTasks=db.tasksDao().getTasksInProjectforStateForAssignee(projectId = PrefManager.getcurrentProject(), state = 2, assignee = PrefManager.getCurrentUserEmail()).filter { !it.archived }
            val workingTasks=db.tasksDao().getTasksInProjectforStateForAssignee(projectId = PrefManager.getcurrentProject(), state = 3, assignee = PrefManager.getCurrentUserEmail()).filter { !it.archived }
            val reviewTasks=db.tasksDao().getTasksInProjectforStateForAssignee(projectId = PrefManager.getcurrentProject(), state = 4, assignee = PrefManager.getCurrentUserEmail()).filter { !it.archived }
            val completedTasks=db.tasksDao().getTasksInProjectforStateForAssignee(projectId = PrefManager.getcurrentProject(), state = 5, assignee = PrefManager.getCurrentUserEmail()).filter { !it.archived }

            withContext(Dispatchers.Main){
                binding.assigned.statIcon.setImageDrawable(resources.getDrawable(R.drawable.baseline_active_24))
                binding.assigned.statTitle.text="Assigned"
                binding.assigned.statCount.text="${assignedTasks.size} tasks"

                binding.ongoing.statIcon.setImageDrawable(resources.getDrawable(R.drawable.baseline_ongoing_24))
                binding.ongoing.statTitle.text="Working on"
                binding.ongoing.statCount.text="${workingTasks.size} tasks"

                binding.review.statIcon.setImageDrawable(resources.getDrawable(R.drawable.baseline_review_24))
                binding.review.statTitle.text="Reviewing"
                binding.review.statCount.text="${reviewTasks.size} tasks"

                binding.completed.statIcon.setImageDrawable(resources.getDrawable(R.drawable.round_task_alt_24))
                binding.completed.statTitle.text="Completed"
                binding.completed.statCount.text="${completedTasks.size} tasks"

            }
        }
        todays.clear()
        todays=PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()
        for (today in todays) {
            fetchTasksForID(today.taskID)
        }
        runDelayed(500){
            PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(),todays)
            if (todays.isNotEmpty()){
                binding.todaytasksCount.visible()
                binding.todayProgress.visible()
                binding.percentageCount.visible()

                val completed=todays.count{ it.isCompleted }
                val total=todays.size
                binding.todaytasksCount.text="${completed}/${total}"

                val progressFraction: Float = if (total > 0) {
                    completed.toFloat() / total
                } else {
                    0f
                }
                val progressPercentage: Int = (progressFraction * 100).roundToInt()
                binding.todayProgress.progress = progressPercentage
                binding.percentageCount.text="$progressPercentage%"
            }
            else{
                binding.todaytasksCount.gone()
                binding.todayProgress.gone()
                binding.percentageCount.gone()
            }
        }
    }

    private fun fetchTasksForID(
        taskID: String,
    ) {
        viewModel.getTasksForID(PrefManager.getcurrentProject(), taskID) { result ->
            when (result) {
                is DBResult.Success -> {
                    filterTasks(result.data)
                }

                is DBResult.Failure -> {
                }

                is DBResult.Progress -> {
                }
            }
        }
    }

    fun filterTasks(data: Task) {
        val segments = PrefManager.getProjectSegments(PrefManager.getcurrentProject())
        for (i in 0 until todays.size) {
            val task = todays[i]
            if (segments.any { it.segment_NAME == data.segment && it.archived } && task.taskID == data.id ) {
                todays.removeAt(i)
            }
            else if (task.taskID == data.id && data.archived){
                todays.removeAt(i)

            }
        }
    }
}