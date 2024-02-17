package com.ncs.o2.UI.Teams

import TaskListAdapter
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.runDelayed
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.slideDownAndGone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.slideUpAndVisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.Report.ShakeDetectedActivity
import com.ncs.o2.UI.SearchScreen.SearchViewModel
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.BottomSheets.FilterBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.FilterTagsBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.MoreOptionsTasksBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.UserListBottomSheet
import com.ncs.o2.databinding.ActivityTasksHolderBinding
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
class TasksHolderActivity : AppCompatActivity(),TaskListAdapter.OnClickListener, FilterBottomSheet.SendText,
    UserListBottomSheet.getassigneesCallback, UserListBottomSheet.updateAssigneeCallback,SegmentSelectionBottomSheet.SegmentSelectionListener,
    FilterTagsBottomSheet.getSelectedTagsCallback,
    SegmentSelectionBottomSheet.sendSectionsListListner,MoreOptionsTasksBottomSheet.OnArchive {
    val binding: ActivityTasksHolderBinding by lazy {
        ActivityTasksHolderBinding.inflate(layoutInflater)
    }
    private val viewModel: TaskSectionViewModel by viewModels()
    private val viewModel1: SearchViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    var taskItems: MutableList<TaskItem> = mutableListOf()
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    val state = arrayOf(1)
    private lateinit var shakeDetector: ShakeDetector
    @Inject
    lateinit var db: TasksDatabase
    @Inject
    lateinit var util: GlobalUtils.EasyElements
    var _type: String? =null
    var index: String? =null

    private var list:MutableList<String> = mutableListOf()
    private var OList: MutableList<User> = mutableListOf()
    private var OList2: MutableList<User> = mutableListOf()
    private var tagIdList: ArrayList<String> = ArrayList()
    private val selectedAssignee:MutableList<User> = mutableListOf()
    private val selectedAssignee2:MutableList<User> = mutableListOf()
    private var selectedSegment=""
    private var TagList: MutableList<Tag> = mutableListOf()
    private val selectedTags = mutableListOf<Tag>()
    private  var taskList: MutableList<Task> = mutableListOf()
    private var isFilterVisible=false
    lateinit var taskadapter:TaskListAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        _type=intent.getStringExtra("type")
        index=intent.getStringExtra("index")




        binding.swiperefresh.setOnRefreshListener {
            syncCache(PrefManager.getcurrentProject())
        }

        binding.btnBack.setOnClickThrottleBounceListener{
            onBackPressed()
        }

        filterButtons()


        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchQuery(s?.toString()!!)
            }
        })
        binding.searchButton.setOnClickThrottleBounceListener {
            searchQuery(binding.searchBar.text?.toString()!!)
        }
        binding.clear.setOnClickThrottleBounceListener {
            selectedAssignee.clear()
            selectedAssignee2.clear()
            selectedTags.clear()
            tagIdList.clear()
            setDefault()
            binding.searchBar.text!!.clear()
            if (_type!="moderating"){
                searchQuery(binding.searchBar.text?.toString()!!)
            }
            else{
                taskList.clear()
                performTaskFetch(_type!!)
                runDelayed(800) {
                    when(viewModel.currentSelected){
                        "all"->{
                            setSelectedColor(binding.all)
                            searchQuery(binding.searchBar.text.toString())
                        }
                        "pending"->{
                            setSelectedColor(binding.pending)
                            taskList=taskList.filter { it.status==1 || it.status==2 }.sortedByDescending { it.time_STAMP }.toMutableList()
                            runDelayed(800) {
                                searchQuery(binding.searchBar.text.toString())
                            }
                        }
                        "working"->{
                            setSelectedColor(binding.working)
                            taskList=taskList.filter { it.status==3 }.sortedByDescending { it.time_STAMP }.toMutableList()
                            runDelayed(800) {
                                searchQuery(binding.searchBar.text.toString())
                            }
                        }
                        "review"->{
                            setSelectedColor(binding.review)

                            taskList=taskList.filter { it.status==4 }.sortedByDescending { it.time_STAMP }.toMutableList()
                            runDelayed(800) {
                                searchQuery(binding.searchBar.text.toString())
                            }
                        }
                        "completed"->{
                            setSelectedColor(binding.completed)
                            taskList=taskList.filter { it.status==5 }.sortedByDescending { it.time_STAMP }.toMutableList()
                            runDelayed(800) {
                                searchQuery(binding.searchBar.text.toString())
                            }
                        }
                    }
                }
            }
        }
        binding.filters.setOnClickThrottleBounceListener {
            if (isFilterVisible){
                isFilterVisible=false
                binding.filtersParent.slideDownAndGone(100)
            }
            else{
                isFilterVisible=true
                binding.filtersParent.slideUpAndVisible(100)
            }
        }

        binding.all.setOnClickThrottleBounceListener {
            viewModel.currentSelected="all"
            setSelectedColor(binding.all)
            list.clear()
            FetchTasksforModerators()
            setUpOnSuccessRV(taskList)
        }

        binding.pending.setOnClickThrottleBounceListener {
            viewModel.currentSelected="pending"
            setSelectedColor(binding.pending)
            val filter = taskList.filter { (it.status == 1 || it.status==2)}.toMutableList()
            setUpOnSuccessRV(filter.sortedByDescending { it.time_STAMP }.toMutableList())

        }

        binding.working.setOnClickThrottleBounceListener {
            viewModel.currentSelected="working"
            setSelectedColor(binding.working)
            val filter = taskList.filter { it.status==3}.toMutableList()
            setUpOnSuccessRV(filter.sortedByDescending { it.time_STAMP }.toMutableList())
        }

        binding.review.setOnClickThrottleBounceListener {
            viewModel.currentSelected="review"
            setSelectedColor(binding.review)
            val filter = taskList.filter { it.status==4}.toMutableList()
            setUpOnSuccessRV(filter.sortedByDescending { it.time_STAMP }.toMutableList())
        }

        binding.completed.setOnClickThrottleBounceListener {
            viewModel.currentSelected="completed"
            setSelectedColor(binding.completed)
            val filter = taskList.filter { it.status==5}.toMutableList()
            setUpOnSuccessRV(filter.sortedByDescending { it.time_STAMP }.toMutableList())
        }


    }

    private fun searchQuery(text:String){
        binding.recyclerView.gone()
        binding.placeholder.gone()
        binding.progressBar.visible()
        binding.resultsParent.visible()

        val state =
            SwitchFunctions.getNumStateFromStringState(binding.state.text.toString())
        val type =
            SwitchFunctions.getNumTypeFromStringType(binding.type.text.toString())
        var assignee = ""
        var creator = ""
        var segment = ""

        if (binding.assignee.text == "Assignee") {
            assignee = ""
        } else {
            assignee = selectedAssignee[0].firebaseID!!
        }

        if (binding.segment.text == "Segment") {
            segment = ""
        } else {
            segment = selectedSegment
        }
        if (!binding.searchBar.text?.toString().isNullOrEmpty()) {
            binding.clear.visible()
        }


        if (binding.created.text == "Created by") {
            creator = ""
        } else {
            creator = selectedAssignee2[0].firebaseID!!
        }

//        val filter=taskList.filter {task ->
//            val taskState=task.status
//            val taskassignee=task.assignee
//            val tasksegment=task.segment
//            val tasktype=task.type
//            val createBy=task.assigner
//
//                    (state==0 || state==taskState)&&
//                    (assignee.isEmpty() || assignee==taskassignee)&&
//                    (segment.isEmpty() || segment==tasksegment)&&
//                    (type==0 || type==tasktype)&&
//                    (creator.isEmpty() || creator==createBy)
//        }

        var filter: MutableList<Task> = mutableListOf()

        if ( _type=="Pending"){


            filter = taskList.filter {
                (state == 0 || it.status == 1 || it.status==2)&&
                        (assignee.isEmpty() || it.assignee == assignee) &&
                        (segment.isEmpty() || it.segment == segment) &&
                        (type == 0 || it.type == type) &&
                        (creator.isEmpty() || it.assigner == creator)&&
                        (it.id.contains(text, ignoreCase = true) || it.description.contains(text, ignoreCase = true))

            }.toMutableList()


            Log.d("listsize",filter.size.toString())


        }
        else{
            filter = taskList.filter {
                (state == 0 || it.status == state) &&
                        (assignee.isEmpty() || it.assignee == assignee) &&
                        (segment.isEmpty() || it.segment == segment) &&
                        (type == 0 || it.type == type) &&
                        (creator.isEmpty() || it.assigner == creator)&&
                        (it.id.contains(text, ignoreCase = true) || it.description.contains(text, ignoreCase = true))

            }.toMutableList()
        }



        var finalFilter:MutableList<Task> = mutableListOf()
        if (selectedTags.isNotEmpty()){
            for (i in 0 until filter.size){
                if (filter[i].tags.contains(selectedTags[0].tagID)){
                    finalFilter.add(filter[i])
                }
            }
        }
        else{
            finalFilter=filter
        }


        Log.d("filterActivity", finalFilter.toString())

        setUpOnSuccessRV(finalFilter.sortedByDescending { it.time_STAMP }.toMutableList())



    }

    private fun setSelectedColor(button: AppCompatButton) {
        val list= listOf(
            binding.all,binding.pending,binding.working,binding.review,binding.completed
        ).toMutableList()
        list.remove(button)
        setDefault()
        button.setBackgroundResource(R.drawable.item_bg_curve_selected)
        val drawable: Drawable? = button.compoundDrawables[2]?.mutate()
        drawable?.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(this, R.color.primary_bg),
            PorterDuff.Mode.SRC_IN
        )
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        button.setTextColor(ContextCompat.getColor(this, R.color.primary_bg))
        for (element in list){
            setUnSelectedColor(element)
        }
    }


    private fun setUnSelectedColor(button: AppCompatButton){
        button.setBackgroundDrawable(resources.getDrawable(R.drawable.item_bg_curve))
        val drawable: Drawable? = button.compoundDrawables[2]?.mutate()
        drawable?.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(this, R.color.better_white),
            PorterDuff.Mode.SRC_IN
        )
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        button.setTextColor(resources.getColor(R.color.better_white))
    }


    private fun performTaskFetch(type:String){
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
                binding.state.gone()
                FetchTasksforState(1)

            }

            "Ongoing" -> {
                binding.title.text = "Ongoing"
                binding.state.gone()
                FetchTasksforState(3)

            }

            "Review" -> {
                binding.title.text = "Review"
                FetchTasksforState(4)
                binding.state.gone()


            }

            "Completed" -> {
                binding.title.text = "Completed"
                FetchTasksforState(5)
                binding.state.gone()

            }

            "WorkspaceAssigned" -> {
                binding.title.text = "Assigned"
                FetchTasksforStateandAssignee(2)
                binding.state.gone()
                binding.assignee.gone()

            }

            "WorkspaceWorking" ->{
                binding.title.text = "Working"
                FetchTasksforStateandAssignee(3)
                binding.state.gone()
                binding.assignee.gone()
            }

            "WorkspaceReview" ->{
                binding.title.text = "Under Review"
                FetchTasksforStateandAssignee(4)
                binding.state.gone()
                binding.assignee.gone()
            }

            "WorkspaceCompleted" ->{
                binding.title.text = "Completed"
                FetchTasksforStateandAssignee(5)
                binding.state.gone()
                binding.assignee.gone()
            }

            "moderating" ->{
                binding.title.text = "Moderating"
                FetchTasksforModerators()
                binding.state.gone()

            }

            "opened" ->{
                binding.title.text = "Opened by me"
                FetchTasksforAssigner()
                binding.created.gone()
            }

            else -> {
                finish()
            }
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
        val filteredList = filterTasks(list)

        if (filteredList.isEmpty()){
            binding.layout.gone()
            binding.recyclerView.gone()
            binding.progressbarBlock.gone()
            binding.placeholder.visible()
            binding.progressBar.gone()
            binding.results.visible()
            binding.results.text="Matches 0 tasks"
        }
        else{
            Log.d("lissttt",filteredList.size.toString())
            Log.d("lissttt",filteredList.toString())
            binding.layout.visible()
            binding.recyclerView.visible()
            binding.progressbarBlock.gone()
            binding.progressBar.gone()
            binding.placeholder.gone()
            binding.results.visible()
            binding.results.text="Matches ${filteredList.size.toString()} tasks"
            if (ifDefault()){
                binding.clear.gone()
            }
            else{
                binding.clear.visible()
            }
            recyclerView = binding.recyclerView
            taskItems.clear()
            taskItems.addAll(filteredList.map { task ->
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
            taskadapter = TaskListAdapter(
                firestoreRepository,
                this,
                taskItems.sortedByDescending { it.timestamp }.toMutableList(),
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
                    taskList.clear()
                    taskList.addAll(filterTasks(result.data.toMutableList()))
                    taskList.sortedByDescending { it.time_STAMP }

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
        if (status==1){
            for (i in 1..2){
                viewModel.getTasksForStateFromDB(
                    PrefManager.getcurrentProject(),i
                ) { result ->
                    when (result) {
                        is DBResult.Success -> {
                            taskList.addAll(filterTasks(result.data.toMutableList()))
                            taskList.sortedByDescending { it.time_STAMP }

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

        }
        else{
            viewModel.getTasksForStateFromDB(
                PrefManager.getcurrentProject(),status
            ) { result ->
                when (result) {
                    is DBResult.Success -> {
                        taskList.clear()
                        taskList.addAll(filterTasks(result.data.toMutableList()))
                        taskList.sortedByDescending { it.time_STAMP }

                        setUpOnSuccessRV(taskList)



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
                            taskList.clear()
                            taskList.addAll(filterTasks(list.toMutableList()))
                            taskList.sortedByDescending { it.time_STAMP }

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
                    taskList.clear()
                    taskList.addAll(filterTasks(result.data.toMutableList()))

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
                    taskList.addAll(filterTasks(listOf(result.data)))
                    taskList.sortedByDescending { it.time_STAMP }

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
                        performTaskFetch(_type!!)
                        taskadapter.setTasks(taskList)
                        taskadapter.notifyDataSetChanged()
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

    override fun onLongClick(position: Int, task: TaskItem) {
        this.performHapticFeedback()
        val moreOptionsTasksBottomSheet = MoreOptionsTasksBottomSheet(task,this)
        moreOptionsTasksBottomSheet.show(supportFragmentManager, "More Task Options")
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
        if (_type!=null ){
            if (this::taskadapter.isInitialized){
                if (ifDefault()) {
                    Log.d("ifDefault","if")
                    if (_type!="moderating") {
                        taskList.clear()
                        performTaskFetch(_type!!)
                        runDelayed(800) {
                            binding.results.text = "Matches ${taskList.size} tasks"
                            taskadapter.setTasks(taskList.sortedByDescending { it.time_STAMP }
                                .toMutableList())
                            taskadapter.notifyDataSetChanged()
                        }
                    }
                    else{
                        binding.state.text="State"
                        taskList.clear()
                        performTaskFetch(_type!!)
                        runDelayed(800) {
                            when(viewModel.currentSelected){
                                "all"->{
                                    setSelectedColor(binding.all)
                                    searchQuery(binding.searchBar.text.toString())
                                }
                                "pending"->{
                                    setSelectedColor(binding.pending)

                                    taskList=taskList.filter { it.status==1 || it.status==2 }.sortedByDescending { it.time_STAMP }.toMutableList()
                                    runDelayed(800) {
                                        searchQuery(binding.searchBar.text.toString())
                                    }
                                }
                                "working"->{
                                    setSelectedColor(binding.working)
                                    taskList=taskList.filter { it.status==3 }.sortedByDescending { it.time_STAMP }.toMutableList()
                                    runDelayed(800) {
                                        searchQuery(binding.searchBar.text.toString())
                                    }
                                }
                                "review"->{
                                    setSelectedColor(binding.review)

                                    taskList=taskList.filter { it.status==4 }.sortedByDescending { it.time_STAMP }.toMutableList()
                                    runDelayed(800) {
                                        searchQuery(binding.searchBar.text.toString())
                                    }
                                }
                                "completed"->{
                                    setSelectedColor(binding.completed)
                                    taskList=taskList.filter { it.status==5 }.sortedByDescending { it.time_STAMP }.toMutableList()
                                    runDelayed(800) {
                                        searchQuery(binding.searchBar.text.toString())
                                    }
                                }
                            }
                        }
                    }
                }
                else{
                    Log.d("ifDefault","else")
                    if (_type!="moderating") {
                        taskList.clear()
                        performTaskFetch(_type!!)
                        runDelayed(800) {
                            searchQuery(binding.searchBar.text.toString())
                        }
                    }
                    else{
                        binding.state.text="State"
                        taskList.clear()
                        performTaskFetch(_type!!)
                        runDelayed(800) {
                            Log.d("tasklistcheck",taskList.toString())
                            when(viewModel.currentSelected){
                                "all"->{
                                    setSelectedColor(binding.all)
                                    searchQuery(binding.searchBar.text.toString())
                                }
                                "pending"->{
                                    setSelectedColor(binding.pending)

                                    taskList=taskList.filter { it.status==1 || it.status==2 }.sortedByDescending { it.time_STAMP }.toMutableList()
                                    runDelayed(800) {
                                        searchQuery(binding.searchBar.text.toString())
                                    }
                                }
                                "working"->{
                                    setSelectedColor(binding.working)

                                    taskList=taskList.filter { it.status==3 }.sortedByDescending { it.time_STAMP }.toMutableList()
                                    runDelayed(800) {
                                        searchQuery(binding.searchBar.text.toString())
                                    }
                                }
                                "review"->{
                                    setSelectedColor(binding.review)
                                    taskList=taskList.filter { it.status==4 }.sortedByDescending { it.time_STAMP }.toMutableList()
                                    runDelayed(800) {
                                        searchQuery(binding.searchBar.text.toString())
                                    }
                                }
                                "completed"->{
                                    setSelectedColor(binding.completed)
                                    taskList=taskList.filter { it.status==5 }.sortedByDescending { it.time_STAMP }.toMutableList()
                                    runDelayed(800) {
                                        searchQuery(binding.searchBar.text.toString())
                                    }
                                }
                            }
                        }
                    }
                }

            }
            else{
                if (_type=="moderating"){
                    binding.moderatorFilter.visible()
                }
                else{
                    binding.moderatorFilter.gone()

                }
                taskList.clear()
                performTaskFetch(_type!!)
                runDelayed(800){
                    setUpOnSuccessRV(taskList.sortedByDescending { it.time_STAMP }.toMutableList())
                }
            }
        }

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


    private fun setDefault(){
        binding.tags.text="Tags"
        binding.state.text="State"
        binding.assignee.text="Assignee"
        binding.segment.text="Segment"
        binding.type.text="Type"
        binding.created.text="Created by"

        setUnSelectedButtonColor(binding.tags)
        setUnSelectedButtonColor(binding.state)
        setUnSelectedButtonColor(binding.assignee)
        setUnSelectedButtonColor(binding.segment)
        setUnSelectedButtonColor(binding.type)
        setUnSelectedButtonColor(binding.created)

        binding.clear.gone()

    }

    private fun ifDefault() : Boolean{
        return  binding.tags.text=="Tags"&&
                binding.state.text=="State" &&
                binding.assignee.text=="Assignee" &&
                binding.segment.text=="Segment" &&
                binding.type.text=="Type"&&
                binding.created.text=="Created by"
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
        intent.putExtra("type", "settings")
        startActivity(intent)
    }

    private fun filterButtons(){
        binding.state.setOnClickThrottleBounceListener{
            list.clear()
            list.addAll(listOf("Submitted","Open","Working","Review","Completed"))
            val filterBottomSheet =
                FilterBottomSheet(list,"STATE",this)
            filterBottomSheet.show(supportFragmentManager, "PRIORITY")
        }
        binding.type.setOnClickThrottleBounceListener{
            list.clear()
            list.addAll(listOf("Bug","Feature","Feature request","Task","Exception","Security","Performance"))
            val filterBottomSheet =
                FilterBottomSheet(list,"TYPE",this)
            filterBottomSheet.show(supportFragmentManager, "PRIORITY")
        }
        binding.assignee.setOnClickThrottleBounceListener {
            if (PrefManager.getAppMode()== Endpoints.ONLINE_MODE) {

                val assigneeListBottomSheet =
                    UserListBottomSheet(OList, selectedAssignee, this, this, "ASSIGNEE")
                assigneeListBottomSheet.show(supportFragmentManager, "assigneelist")
            }
            else{
                toast("Can't fetch Assignee")
            }
        }
        binding.created.setOnClickThrottleBounceListener {
            if (PrefManager.getAppMode()== Endpoints.ONLINE_MODE) {

                val assigneeListBottomSheet =
                    UserListBottomSheet(OList2, selectedAssignee2, this, this, "CREATED BY")
                assigneeListBottomSheet.show(supportFragmentManager, "assigneelist")
            }
            else{
                toast("Can't fetch Creators")
            }

        }
        binding.segment.setOnClickThrottleBounceListener {
            val segment = SegmentSelectionBottomSheet("Search")
            segment.segmentSelectionListener = this
            segment.sectionSelectionListener = this
            segment.show(supportFragmentManager, "Segment Selection")
        }
        binding.tags.setOnClickThrottleBounceListener {
            val filterTagsBottomSheet =
                FilterTagsBottomSheet(TagList, this, selectedTags)
            filterTagsBottomSheet.show(supportFragmentManager, "OList")
        }
    }


    override fun onAssigneeTListUpdated(TList: MutableList<User>) {
    }

    override fun updateAssignee(assignee: User) {
    }

    override fun onSegmentSelected(segmentName: String) {
        binding.clear.visible()
        binding.segment.text=segmentName
        selectedSegment=segmentName
        setSelectedButtonColor(binding.segment)
    }

    override fun sendSectionsList(list: MutableList<String>) {
    }

    override fun onSelectedTags(tag: Tag, isChecked: Boolean) {
        binding.clear.visible()
        if (isChecked) {
            selectedTags.add(tag)
            tag.tagID?.let { tagIdList.add(it) }
            binding.tags.text=tag.tagText
            setSelectedButtonColor(binding.tags)
        } else {
            selectedTags.remove(tag)
            tag.tagID?.let { tagIdList.remove(it) }
            binding.tags.text="Tags"
            setUnSelectedButtonColor(binding.tags)

        }

    }

    override fun onTagListUpdated(tagList: MutableList<Tag>) {
        TagList.clear()
        TagList = tagList
    }

    private fun setSelectedButtonColor(button: AppCompatButton) {
        button.setBackgroundResource(R.drawable.item_bg_curve_selected)
        val drawable: Drawable? = button.compoundDrawables[2]?.mutate()
        drawable?.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(this, R.color.primary_bg),
            PorterDuff.Mode.SRC_IN
        )
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        button.setTextColor(ContextCompat.getColor(this, R.color.primary_bg))
    }


    private fun setUnSelectedButtonColor(button: AppCompatButton){
        button.setBackgroundDrawable(resources.getDrawable(R.drawable.item_bg_curve))
        val drawable: Drawable? = button.compoundDrawables[2]?.mutate()
        drawable?.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(this, R.color.better_white),
            PorterDuff.Mode.SRC_IN
        )
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        button.setTextColor(resources.getColor(R.color.better_white))
    }

    override fun stringtext(text: String, type: String) {
        binding.clear.visible()
        when (type) {
            "STATE" -> {
                binding.state.text = text
                setSelectedButtonColor(binding.state)
            }

            "TYPE" -> {
                binding.type.text = text
                setSelectedButtonColor(binding.type)
            }
        }
    }

    override fun sendassignee(assignee: User, isChecked: Boolean, position: Int, type: String) {
        if (isChecked) {
            if (type=="ASSIGNEE"){
                binding.clear.visible()
                selectedAssignee.clear()
                selectedAssignee.add(assignee)
                binding.assignee.text=assignee.username
                setSelectedButtonColor(binding.assignee)


            }
            if (type=="CREATED BY"){
                binding.clear.visible()
                selectedAssignee2.clear()
                selectedAssignee2.add(assignee)
                binding.created.text=assignee.username
                setSelectedButtonColor(binding.created)
            }
        }
        else{
            if (type=="ASSIGNEE"){
//                selectedAssignee.remove(assignee)
                binding.assignee.text="Assignee"
                setUnSelectedButtonColor(binding.assignee)
            }
            if (type=="CREATED BY"){
//                selectedAssignee2.remove(assignee)
                binding.created.text="Created by"
                setUnSelectedButtonColor(binding.created)
            }

        }
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
        onResume()
    }

}