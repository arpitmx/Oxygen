package com.ncs.o2.UI.Teams

import TaskListAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
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
import com.ncs.o2.UI.Notifications.NotificationsViewModel
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.ActivityTasksHolderBinding
import com.ncs.o2.databinding.ActivityTeamsBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TasksHolderActivity : AppCompatActivity(),TaskListAdapter.OnClickListener {
    val binding: ActivityTasksHolderBinding by lazy {
        ActivityTasksHolderBinding.inflate(layoutInflater)
    }
    private val viewModel: TaskSectionViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var taskList: ArrayList<TaskItem>
    var taskItems: MutableList<TaskItem> = mutableListOf()
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    val state = arrayOf(1)

    private lateinit var tasks: ArrayList<Task>
    @Inject
    lateinit var db: TasksDatabase
    @Inject
    lateinit var util: GlobalUtils.EasyElements
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val type=intent.getStringExtra("type")

        when(type){
            "Favs" ->{
                binding.title.text="Favourite"

                if (PrefManager.getProjectFavourites(PrefManager.getcurrentProject()).isEmpty()){
                    binding.layout.gone()
                    binding.recyclerView.gone()
                    binding.progressbarBlock.gone()
                    binding.placeholder.visible()
                }
                else{
                    for (id in PrefManager.getProjectFavourites(PrefManager.getcurrentProject())){
                        fetchTasksforID(id)
                    }
                }
            }
            "Active"->{
                binding.title.text="Active"

                FetchTasksforState(1)
                FetchTasksforState(2)
            }
            "Ongoing"->{
                binding.title.text="Ongoing"

                FetchTasksforState(3)

            }
            "Review"->{
                binding.title.text="Review"

                FetchTasksforState(4)

            }
            "Completed"->{
                binding.title.text="Completed"

                FetchTasksforState(5)

            }
            else->{
                finish()
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            ExtensionsUtil.runDelayed(1000) {
                startActivity(intent)
                toast("Page refreshed")
                binding.swiperefresh.isRefreshing = false
            }
        }

        binding.btnBack.setOnClickThrottleBounceListener{
            onBackPressed()
        }

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
    private fun FetchTasksforState(status:Int){
        viewModel.getTasksForStateFromDB(
            PrefManager.getcurrentProject(),status
        ) { result ->
            when (result) {
                is DBResult.Success -> {

                    if (result.data.isEmpty()){
                        binding.layout.gone()
                        binding.recyclerView.gone()
                        binding.progressbarBlock.gone()
                        binding.placeholder.visible()
                    }
                    else{
                        binding.layout.visible()
                        binding.recyclerView.visible()
                        binding.progressbarBlock.gone()
                        binding.placeholder.gone()
                        recyclerView = binding.recyclerView
                         taskItems.addAll(result.data.map { task ->
                            TaskItem(
                                title = task.title!!,
                                id = task.id,
                                assignee_id = task.assignee!!,
                                difficulty = task.difficulty!!,
                                timestamp = task.time_STAMP,
                                completed = if (SwitchFunctions.getStringStateFromNumState(task.status!!) == "Completed") true else false,
                                tagList = task.tags
                            )
                        }.toMutableList())
                        Log.d("tasksFetch",taskItems.toString())
                        val taskadapter = TaskListAdapter(
                            firestoreRepository,
                            this,
                            taskItems.toMutableList(),
                            db
                        )
                        taskadapter.setOnClickListener(this)


                        val layoutManager =
                            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
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
                                } else if (dy < -10) {
                                }
                            }
                        })

                    }


                }

                is DBResult.Failure -> {
                    val errorMessage = result.exception.message
                    showLoader(-1)
                    util.singleBtnDialog(
                        "Failure",
                        "Failure in loading tasks, try again : ${errorMessage}", "Reload"
                    ) {
                        FetchTasksforState(status)
                    }
                }

                is DBResult.Progress -> {
                    showLoader(1)
                }

            }
        }
    }

    private fun fetchTasksforID(taskID:String){
        viewModel.getTasksForID(
            PrefManager.getcurrentProject(),taskID
        ) { result ->
            when (result) {
                is DBResult.Success -> {

                        binding.layout.visible()
                        binding.recyclerView.visible()
                        binding.progressbarBlock.gone()
                        binding.placeholder.gone()
                        recyclerView = binding.recyclerView
                        val data= listOf(result.data)
                        taskItems.addAll(data.map { task ->
                            TaskItem(
                                title = task.title!!,
                                id = task.id,
                                assignee_id = task.assignee!!,
                                difficulty = task.difficulty!!,
                                timestamp = task.time_STAMP,
                                completed = if (SwitchFunctions.getStringStateFromNumState(task.status!!) == "Completed") true else false,
                                tagList = task.tags
                            )
                        }.toMutableList())
                        Log.d("tasksFetch",taskItems.toString())
                        val taskadapter = TaskListAdapter(
                            firestoreRepository,
                            this,
                            taskItems.toMutableList(),
                            db
                        )
                        taskadapter.setOnClickListener(this)


                        val layoutManager =
                            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
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
                                } else if (dy < -10) {
                                }
                            }
                        })


                }

                is DBResult.Failure -> {
                    val errorMessage = result.exception.message
                    showLoader(-1)
                    util.singleBtnDialog(
                        "Failure",
                        "Failure in loading tasks, try again : ${errorMessage}", "Reload"
                    ) {
                    }
                }

                is DBResult.Progress -> {
                    showLoader(1)
                }

            }
        }
    }

    override fun onCLick(position: Int, task: TaskItem) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("index", "2")
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
        super.onBackPressed()
    }
}