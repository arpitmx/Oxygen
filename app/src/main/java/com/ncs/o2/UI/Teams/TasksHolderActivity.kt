package com.ncs.o2.UI.Teams

import TaskListAdapter
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Notifications.NotificationsViewModel
import com.ncs.o2.UI.Report.ShakeDetectedActivity
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.databinding.ActivityTasksHolderBinding
import com.ncs.o2.databinding.ActivityTeamsBinding
import com.ncs.versa.Constants.Endpoints
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
    private lateinit var shakeDetector: ShakeDetector

    private lateinit var tasks: ArrayList<Task>
    @Inject
    lateinit var db: TasksDatabase
    @Inject
    lateinit var util: GlobalUtils.EasyElements
    var type: String? =null
    var index: String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        type=intent.getStringExtra("type")
        index=intent.getStringExtra("index")



        if (type!=null){
            when(type) {
                "Favs" -> {
                    binding.title.text = "Favourite"

                    if (PrefManager.getProjectFavourites(PrefManager.getcurrentProject()).isEmpty()) {
                        binding.layout.gone()
                        binding.recyclerView.gone()
                        binding.progressbarBlock.gone()
                        binding.placeholder.visible()
                    } else {
                        for (id in PrefManager.getProjectFavourites(PrefManager.getcurrentProject())) {
                            fetchTasksforID(id)
                        }
                    }
                }

                "Pending" -> {
                    binding.title.text = "Pending"

                    FetchTasksforState(1)
                    FetchTasksforState(2)
                }

                "Ongoing" -> {
                    binding.title.text = "Ongoing"

                    FetchTasksforState(3)

                }

                "Review" -> {
                    binding.title.text = "Review"

                    FetchTasksforState(4)

                }

                "Completed" -> {
                    binding.title.text = "Completed"

                    FetchTasksforState(5)

                }

                "WorkspaceAssigned" -> {
                    binding.title.text = "Assigned"
                    FetchTasksforStateandAssignee(2)

                }

                "WorkspaceWorking" ->{
                    binding.title.text = "Working"
                    FetchTasksforStateandAssignee(3)
                }

                "WorkspaceReview" ->{
                    binding.title.text = "Under Review"
                    FetchTasksforStateandAssignee(4)
                }

                "WorkspaceCompleted" ->{
                    binding.title.text = "Completed"
                    FetchTasksforStateandAssignee(5)
                }

                "moderating" ->{
                    binding.title.text = "Moderating"
                    FetchTasksforModerators()

                }

                "opened" ->{
                    binding.title.text = "Opened by me"
                    FetchTasksforAssigner()
                }

                else -> {
                    finish()
                }
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            syncCache(PrefManager.getcurrentProject())
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

    private fun setUpOnSuccessRV(list: MutableList<Task>){
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
            taskItems.addAll(list.map { task ->
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
            }.toMutableList())
            Log.d("tasksFetch",taskItems.toString())
            val taskadapter = TaskListAdapter(
                firestoreRepository,
                this,
                taskItems.sortedByDescending { it.last_updated }.toMutableList(),
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
    private fun FetchTasksforStateandAssignee(status:Int){
        viewModel.getTasksforSegmentsforAssignee(
            PrefManager.getcurrentProject(),PrefManager.getCurrentUserEmail(),status
        ) { result ->
            when (result) {
                is DBResult.Success -> {

                    setUpOnSuccessRV(result.data.toMutableList())

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

    private fun FetchTasksforState(status:Int){
        viewModel.getTasksForStateFromDB(
            PrefManager.getcurrentProject(),status
        ) { result ->
            when (result) {
                is DBResult.Success -> {

                    setUpOnSuccessRV(result.data.toMutableList())



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

    private fun FetchTasksforModerators(){
        viewModel.getTasksforModerators(
            PrefManager.getcurrentProject(),
        ) { result ->
            when (result) {
                is DBResult.Success -> {
                    val allTasks=result.data
                    val list:MutableList<Task> = mutableListOf()

                    CoroutineScope(Dispatchers.IO).launch {
                        for (task in allTasks){
                            if (task.moderators.contains(PrefManager.getCurrentUserEmail())){
                                list.add(task)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            setUpOnSuccessRV(list.toMutableList())
                        }
                    }



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

    private fun FetchTasksforAssigner(){
        viewModel.getTasksOpenedBy(
            PrefManager.getcurrentProject(),PrefManager.getCurrentUserEmail()
        ) { result ->
            when (result) {
                is DBResult.Success -> {

                    setUpOnSuccessRV(result.data.toMutableList())



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

    private fun fetchTasksforID(taskID:String){
        viewModel.getTasksForID(
            PrefManager.getcurrentProject(),taskID
        ) { result ->
            when (result) {
                is DBResult.Success -> {

                    setUpOnSuccessRV(listOf(result.data).toMutableList())

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
                        binding.swiperefresh.isRefreshing=false

                    }

                }

            } catch (e: java.lang.Exception) {
                Timber.tag(TaskDetailsFragment.TAG).e(e)
                progressDialog.dismiss()


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
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)

    }

    override fun onStop() {
        super.onStop()
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onPause() {
        super.onPause()
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onResume() {
        super.onResume()
        if (PrefManager.getShakePref()){
            initShake()
            shakeDetector.registerListener()
        }
    }
    private fun initShake(){
        val shakePref=PrefManager.getShakePref()
        Log.d("shakePref",shakePref.toString())
        if (shakePref){

            val sensi=PrefManager.getShakeSensitivity()
            when(sensi){
                1->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultLightSensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
                2->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultMediumSensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
                3->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultHeavySensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
            }
        }
    }

    fun takeScreenshot(activity: Activity) {
        Log.e("takeScreenshot", activity.localClassName)
        val rootView = activity.window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = rootView.drawingCache
        val currentTime = Timestamp.now().seconds
        val filename = "screenshot_$currentTime.png"
        val internalStorageDir = activity.filesDir
        val file = File(internalStorageDir, filename)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            rootView.isDrawingCacheEnabled = false
            moveToReport(filename)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun moveToReport(filename: String) {
        val intent = Intent(this, ShakeDetectedActivity::class.java)
        intent.putExtra("filename", filename)
        intent.putExtra("type","report")
        startActivity(intent)
    }

    fun moveToShakeSettings() {
        val intent = Intent(this, ShakeDetectedActivity::class.java)
        intent.putExtra("type","settings")
        startActivity(intent)
    }






}