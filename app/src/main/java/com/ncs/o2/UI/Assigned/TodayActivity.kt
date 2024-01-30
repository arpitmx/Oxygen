package com.ncs.o2.UI.Assigned

import android.app.ProgressDialog
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
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.TodayTasks
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.ActivityTasksHolderBinding
import com.ncs.o2.databinding.ActivityTodayBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class TodayActivity : AppCompatActivity(),TodayTasksAdpater.OnClickListener,TodayTasksAdpater.SwipeListener {

    val binding: ActivityTodayBinding by lazy {
        ActivityTodayBinding.inflate(layoutInflater)
    }
    private val viewModel: TaskSectionViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    var taskItems: MutableList<TaskItem> = mutableListOf()
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    val state = arrayOf(1)
    @Inject
    lateinit var db: TasksDatabase
    private val util: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this@TodayActivity)
    }
    var type: String? =null
    var index: String? =null
    val tasks:MutableList<Task> = mutableListOf()
    lateinit var taskadapter:TodayTasksAdpater
    var todays:MutableList<TodayTasks> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnBack.setOnClickThrottleBounceListener{
            onBackPressed()
        }
        binding.btnDelete.setOnClickThrottleBounceListener {
            util.twoBtnDialog(title = "Clear All", msg = "Are you sure you want to clear your Today section?",
                positiveBtnText = "Clear", negativeBtnText = "Cancel", positive = {
                    PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(), emptyList())
                    setTasks()
                }, negative = {})
        }
        binding.swiperefresh.setOnRefreshListener {
            syncCache(PrefManager.getcurrentProject())
        }
        setTasks()


    }
    private fun setTasks(){
        if (PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).isEmpty()) {
            binding.layout.gone()
            binding.recyclerView.gone()
            binding.progressbarBlock.gone()
            binding.placeholder.visible()
        } else {
            todays=PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()
            setUpOnSuccessRV(todays)
        }
    }

    private fun syncCache(projectName:String){
        val progressDialog = ProgressDialog(this)
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
                        startActivity(intent)
                        finish()
                        binding.swiperefresh.isRefreshing=false

                    }

                }

            } catch (e: java.lang.Exception) {
                Timber.tag(TaskDetailsFragment.TAG).e(e)
                progressDialog.dismiss()


            }

        }
    }
    private fun setUpOnSuccessRV(list: MutableList<TodayTasks>){
        Log.d("Todays",list.toString())
        if (list.isEmpty()){
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
             taskadapter = TodayTasksAdpater(
                firestoreRepository,
                this,
                list.sortedBy { it.isCompleted }.distinctBy { it.taskID }.toMutableList(),
                db,
                this,
                recyclerView,
                 viewModel
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
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
    }

    override fun onCLick(position: Int, task: TodayTasks) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.taskID)
        startActivity(intent)
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    override fun onleftSwipe(task: TodayTasks, position: Int) {
        this.performHapticFeedback()
        if (!task.isCompleted){
            val _todays = PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()

            for (_today in _todays) {
                if (_today.taskID == task.taskID) {
                    _today.isCompleted = true
                    break
                }
            }

            for (today in todays) {
                if (today.taskID == task.taskID) {
                    today.isCompleted = true
                    break
                }
            }


//            val toUpdate = todays[position]
//            toUpdate.isCompleted = true
//            todays[position]=toUpdate
            PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(),_todays)
            taskadapter.notifyDataSetChanged()
            taskadapter.setTasks(_todays.sortedBy { it.isCompleted }.distinctBy { it.taskID }.toMutableList())
            util.showSnackbar(binding.root,"Marked as Completed for Today",2000)
        }
        else{
            val _todays = PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()

            for (today in _todays) {
                if (today.taskID == task.taskID) {
                    today.isCompleted = false
                    break
                }
            }

            for (today in todays) {
                if (today.taskID == task.taskID) {
                    today.isCompleted = false
                    break
                }
            }

//            val toUpdate = todays[position]
//            toUpdate.isCompleted = false
//            todays[position]=toUpdate
            PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(),_todays)
            taskadapter.notifyDataSetChanged()
            taskadapter.setTasks(_todays.sortedBy { it.isCompleted }.distinctBy { it.taskID }.toMutableList())
//            setTasks()
            util.showSnackbar(binding.root,"Marked as not Completed ",2000)
        }


    }


    override fun onrightSwipe(task: TodayTasks,position: Int) {
        this.performHapticFeedback()
        val _todays=PrefManager.getProjectTodayTasks(PrefManager.getcurrentProject()).toMutableList()

        val iterator = _todays.iterator()
        while (iterator.hasNext()) {
            val today = iterator.next()
            if (task.taskID == today.taskID) {
                iterator.remove()
                break
            }
        }
        PrefManager.saveProjectTodayTasks(PrefManager.getcurrentProject(),_todays)

        for (i in 0 until todays.size) {
            if (todays[i].taskID==task.taskID){
                todays.removeAt(i)
                break
            }
        }

//        todays.removeAt(position)
//        taskadapter.notifyItemRemoved(position)
        taskadapter.setTasks(_todays.sortedBy { it.isCompleted }.distinctBy { it.taskID }.toMutableList())
        if (_todays.isEmpty()){
            setTasks()
        }
        util.showSnackbar(binding.root,"Removed from Today",2000)

    }


}