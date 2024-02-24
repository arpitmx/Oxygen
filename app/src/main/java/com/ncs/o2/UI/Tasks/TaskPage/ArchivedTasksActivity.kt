package com.ncs.o2.UI.Tasks.TaskPage

import TaskListAdapter
import android.app.ProgressDialog
import android.content.Intent
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.ncs.o2.Domain.Utility.ExtensionsUtil.runDelayed
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.slideDownAndGone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.slideUpAndVisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.UIComponents.BottomSheets.FilterBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.FilterTagsBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.UserListBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.sectionDisplayBottomSheet
import com.ncs.o2.databinding.ActivityArchivedTasksBinding
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
class ArchivedTasksActivity : AppCompatActivity(),TaskListAdapter.OnClickListener,
    UserListBottomSheet.getassigneesCallback, UserListBottomSheet.updateAssigneeCallback,SegmentSelectionBottomSheet.SegmentSelectionListener,
    SegmentSelectionBottomSheet.sendSectionsListListner,sectionDisplayBottomSheet.SectionSelectionListener {

    val binding: ActivityArchivedTasksBinding by lazy {
        ActivityArchivedTasksBinding.inflate(layoutInflater)
    }
    private val util: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    private val viewModel: TaskSectionViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private var taskItems: MutableList<TaskItem>  = mutableListOf()
    private  var tasks: MutableList<Task> = mutableListOf()
    @Inject
    lateinit var db: TasksDatabase
    private var list:MutableList<String> = mutableListOf()
    private var OList: MutableList<User> = mutableListOf()
    private var OList2: MutableList<User> = mutableListOf()
    private var tagIdList: ArrayList<String> = ArrayList()
    private val selectedAssignee:MutableList<User> = mutableListOf()
    private val selectedAssignee2:MutableList<User> = mutableListOf()
    private val selectedTags = mutableListOf<Tag>()
    private var isFilterVisible=false
    private var selectedSegment=""
    private var selectedSection=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.swiperefresh.setOnRefreshListener {
            syncCache(PrefManager.getcurrentProject())
        }
        binding.btnBack.setOnClickThrottleBounceListener{
            onBackPressed()
        }
        runDelayed(1000) {
            fetchfromdb()
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
            searchQuery(binding.searchBar.text?.toString()!!)
        }
    }

    private fun setDefault(){
        binding.assignee.text="Assignee"
        binding.segment.text="Segment"
        binding.section.text="Section"
        binding.created.text="Created by"


        setUnSelectedButtonColor(binding.assignee)
        setUnSelectedButtonColor(binding.segment)
        setUnSelectedButtonColor(binding.created)
        setUnSelectedButtonColor(binding.section)

        binding.clear.gone()

    }




    private fun searchQuery(text:String){
        binding.recyclerView.gone()
        binding.placeholder.gone()
        binding.progressBar.visible()
        binding.resultsParent.visible()

        var assignee = ""
        var creator = ""
        var segment = ""
        var section= ""

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
        if (binding.section.text == "Section") {
            section = ""
        } else {
            section = selectedSection
        }
        if (!binding.searchBar.text?.toString().isNullOrEmpty()) {
            binding.clear.visible()
        }


        if (binding.created.text == "Created by") {
            creator = ""
        } else {
            creator = selectedAssignee2[0].firebaseID!!
        }


        var filter: MutableList<Task> = mutableListOf()

        filter = tasks.filter {
                            (assignee.isEmpty() || it.assignee == assignee) &&
                            (segment.isEmpty() || it.segment == segment) &&
                            (section.isEmpty() || it.section == section)&&
                            (creator.isEmpty() || it.assigner == creator)&&
                            (it.id.contains(text, ignoreCase = true) || it.description.contains(text, ignoreCase = true))

        }.toMutableList()


        Log.d("filterActivity", filter.toString())

        setUpOnSuccessRV(filter.sortedByDescending { it.time_STAMP }.toMutableList())



    }

    private fun filterButtons(){

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
        binding.section.setOnClickThrottleBounceListener {
            if (binding.segment.text=="Segment") {
                toast("First select segment")
            } else {
                val sections = sectionDisplayBottomSheet(binding.segment.text.toString())
                sections.sectionSelectionListener = this
                sections.show(supportFragmentManager, "Section Selection")
            }
        }
    }

    fun fetchfromdb() {

        viewModel.getTasksinProject(
            PrefManager.getcurrentProject(),
        ) { result ->
            when (result) {
                is DBResult.Success -> {

                    val filteredList = result.data.filter { it.archived }.sortedByDescending { it.time_STAMP }
                    tasks.clear()
                    tasks.addAll(filteredList)
                    setUpOnSuccessRV(filteredList)

                }

                is DBResult.Failure -> {
                    val errorMessage = result.exception.message
                    util.singleBtnDialog(
                        "Failure",
                        "Failure in loading tasks, try again : ${errorMessage}", "Reload"
                    ) {
                        fetchfromdb()
                    }
                }

                is DBResult.Progress -> {

                }
            }
        }
    }

    private fun setUpOnSuccessRV(filteredList:List<Task>){
        if (filteredList.isEmpty()) {
            binding.layout.gone()
            binding.recyclerView.gone()
            binding.progressbarBlock.gone()
            binding.placeholder.visible()
            binding.progressBar.gone()
            binding.results.visible()
            binding.results.text="Matches 0 tasks"
        } else {
            binding.layout.visible()
            binding.recyclerView.visible()
            binding.progressbarBlock.gone()
            binding.progressBar.gone()
            binding.placeholder.gone()
            binding.results.visible()
            binding.results.text="Matches ${filteredList.size.toString()} tasks"
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
            val taskadapter = TaskListAdapter(
                firestoreRepository,
                this,
                taskItems.sortedByDescending { it.timestamp }.toMutableList(),
                db
            )
            taskadapter.setOnClickListener(this)
            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            layoutManager.reverseLayout = false
            with(recyclerView) {
                this.layoutManager = layoutManager
                adapter = taskadapter
                edgeEffectFactory = BounceEdgeEffectFactory()
            }
            taskadapter.notifyDataSetChanged()

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
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
                        fetchfromdb()
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
        toast("Un-Archive the task to view it, long press to unarchive")
    }

    override fun onLongClick(position: Int, task: TaskItem) {
        util.twoBtn(title = "Unarchive Task",
            msg = "Do you want to un-archive this Task ?",
            positiveBtnText = "Un-Archive",
            negativeBtnText = "Cancel",
            positive = {
                handleTaskUnArchive(task)
            },
            negative = {
            })
    }
    private fun handleTaskUnArchive(_task: TaskItem){
        val taskID = _task.id
        CoroutineScope(Dispatchers.IO).launch {
            val task = db.tasksDao()
                .getTasksbyId(tasksId = taskID, projectId = PrefManager.getcurrentProject())
            task?.archived = false
            db.tasksDao().update(task!!)
            withContext(Dispatchers.Main) {
                updateTaskArchive(taskItem = _task, newArchive = false)
            }

        }
    }
    private fun updateTaskArchive(taskItem: TaskItem,newArchive:Boolean) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Un-Archiving the task")
        progressDialog.setCancelable(false)
        progressDialog.show()
        lifecycleScope.launch {

            try {
                val result = withContext(Dispatchers.IO) {
                    viewModel.updateArchive(
                        taskID = taskItem.id,
                        archive = newArchive,
                        projectName = PrefManager.getcurrentProject()
                    )
                }
                when (result) {

                    is ServerResult.Failure -> {
                        progressDialog.dismiss()

                        util.singleBtnDialog(
                            "Failure",
                            "Failure in Updating: ${result.exception.message}",
                            "Okay"
                        ) {

                        }

                    }

                    is ServerResult.Progress -> {
                        progressDialog.show()
                    }

                    is ServerResult.Success -> {
                        progressDialog.dismiss()
                        toast("Task Unarchived")
                        fetchfromdb()
                    }

                }

            } catch (e: Exception) {

                Timber.tag(TaskDetailsFragment.TAG).e(e)

            }

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

    override fun onSectionSelected(sectionName: String) {
        binding.clear.visible()
        binding.section.text=sectionName
        selectedSection=sectionName
        setSelectedButtonColor(binding.section)
    }

}