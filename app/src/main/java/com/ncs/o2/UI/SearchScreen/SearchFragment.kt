package com.ncs.o2.UI.SearchScreen

import TaskListAdapter
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
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
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.BottomSheets.AddTagsBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.FilterBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.FilterTagsBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.UserListBottomSheet
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.FragmentSearchBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(),FilterBottomSheet.SendText,UserListBottomSheet.getassigneesCallback,UserListBottomSheet.updateAssigneeCallback,
    TaskListAdapter.OnClickListener,SegmentSelectionBottomSheet.SegmentSelectionListener,FilterTagsBottomSheet.getSelectedTagsCallback,
    SegmentSelectionBottomSheet.sendSectionsListListner{
    private var list:MutableList<String> = mutableListOf()
    private var OList: MutableList<User> = mutableListOf()
    private var OList2: MutableList<User> = mutableListOf()
    private var tagIdList: ArrayList<String> = ArrayList()
    private val selectedAssignee:MutableList<User> = mutableListOf()
    private val selectedAssignee2:MutableList<User> = mutableListOf()
    private var selectedSegment=""
    private var TagList: MutableList<Tag> = mutableListOf()
    private val selectedTags = mutableListOf<Tag>()
    lateinit var binding: FragmentSearchBinding
    private  var taskList: MutableList<TaskItem> = mutableListOf()
    private val tasks:MutableList<Task> = mutableListOf()
    private lateinit var viewModel: SearchViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskListAdapter: TaskListAdapter
    val state = arrayOf(1)
    private var argumentsLoaded = false

    @Inject
    lateinit var db:TasksDatabase

    private val activityBinding: ActivityMainBinding by lazy {
        (requireActivity() as MainActivity).binding
    }

    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        if (!argumentsLoaded) {
            val tagText = arguments?.getString("tagText")
            if (tagText != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val list = db.tagsDao().getAllTag()
                    var tag: Tag
                    for (i in 0 until list?.size!!) {
                        if (list[i].tagID == tagText) {
                            list[i].checked=true
                            selectedTags.add(list[i])
                            searchQuery("")
                            binding.tags.text = list[i].tagText
                            setSelectedButtonColor(binding.tags)
                            binding.clear.visible()
                        }
                    }
                }
                argumentsLoaded = false            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        Handler().postDelayed({
            requestFocusAndShowKeyboard(binding.searchBar)
        }, 200)
        requestFocusAndShowKeyboard(binding.searchBar)
        manageviews()
        defaultButtons()
        filterButtons()
        initViews()
        argumentsLoaded = true

    }

    private fun manageviews(){
        val drawerLayout = activityBinding.drawer
        activityBinding.gioActionbar.btnHamSearch.setOnClickThrottleBounceListener {
            val gravity = if (!drawerLayout.isDrawerOpen(GravityCompat.START)) GravityCompat.START else GravityCompat.END
            drawerLayout.openDrawer(gravity)
        }
        activityBinding.gioActionbar.tabLayout.gone()
        activityBinding.gioActionbar.searchCont.gone()
        activityBinding.gioActionbar.actionbar.visible()
        activityBinding.gioActionbar.constraintLayout2.gone()
        activityBinding.gioActionbar.constraintLayoutsearch.visible()
        activityBinding.gioActionbar.constraintLayoutworkspace.gone()
    }
    private fun initViews(){
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
    }
    private fun setRecyclerView(){
        binding.results.visible()
        binding.results.text="Matches ${taskList.size.toString()} tasks"
        recyclerView = binding.recyclerView
        taskListAdapter = TaskListAdapter(firestoreRepository,requireContext(),taskList)
        taskListAdapter.setOnClickListener(this)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        layoutManager.reverseLayout = false
        with(recyclerView) {
            this.layoutManager = layoutManager
            adapter = taskListAdapter
            edgeEffectFactory = BounceEdgeEffectFactory()
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                state[0] = newState
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && (state[0] == 0 || state[0] == 2)) {
                    hideSearch()
                } else if (dy < -10) {
                    showSearch()
                }
            }
        })
        taskListAdapter.notifyDataSetChanged()
    }
    private fun searchQuery(text:String){
        binding.searchPlaceholder.gone()
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

        if (db.tasksDao().isNull) {
            Log.d("SearchPage", "Performing search from Firestore")

            CoroutineScope(Dispatchers.Main).launch {

                viewModel.getSearchTasked(
                    projectName = PrefManager.getcurrentProject(),
                    assignee = assignee,
                    type = type,
                    state = state,
                    text = text,
                    creator = creator
                ) { result ->
                    when (result) {
                        is ServerResult.Success -> {
                            binding.searchPlaceholder.gone()
                            binding.progressBar.gone()
                            val task = result.data
                            taskList.clear()
                            for (element in task) {
                                taskList.add(element)
                            }
                            if (taskList.isEmpty()) {
                                binding.placeholder.visible()
                                binding.results.gone()
                            } else {
                                binding.recyclerView.visible()
                                binding.placeholder.gone()
                                setRecyclerView()
                            }
                        }

                        is ServerResult.Failure -> {
                            val errorMessage = result.exception.message
                            toast(errorMessage!!)
                            binding.recyclerView.visible()
                            binding.searchPlaceholder.gone()
                            binding.placeholder.gone()
                            binding.progressBar.gone()
                        }

                        is ServerResult.Progress -> {
                            binding.searchPlaceholder.gone()
                            binding.recyclerView.gone()
                            binding.placeholder.gone()
                            binding.progressBar.visible()
                        }

                    }
                }
            }
        } else {
            Log.d("SearchPage", "Performing search from DB")
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getSearchTasksFromDB(
                    projectName = PrefManager.getcurrentProject(),
                    assignee = assignee,
                    type = type,
                    state = state,
                    text = text,
                    creator = creator,
                    segment = segment,
                ) { result ->
                    when (result) {
                        is DBResult.Success -> {
                            binding.searchPlaceholder.gone()
                            binding.progressBar.gone()
                            val task = result.data
                            tasks.clear()
                            for (element in task) {
                                if (selectedTags.isNotEmpty()) {
                                    if (element.tags.contains(selectedTags[0].tagID)) {
                                        tasks.add(element)
                                    }
                                }
                                else{
                                    tasks.add(element)
                                }
                            }
                            if (tasks.isEmpty()) {
                                binding.placeholder.visible()
                                binding.results.gone()
                            } else {
                                taskList = tasks.map { task ->
                                    TaskItem(
                                        title = task.title,
                                        id = task.id,
                                        assignee_id = task.assignee,
                                        difficulty = task.difficulty,
                                        timestamp = task.time_STAMP,
                                        completed = task.completed
                                    )
                                }.toMutableList()
                                binding.recyclerView.visible()
                                binding.placeholder.gone()
                                setRecyclerView()
                            }
                        }

                        is DBResult.Failure -> {
                            val errorMessage = result.exception.message
                            toast(errorMessage!!)
                            binding.recyclerView.visible()
                            binding.searchPlaceholder.gone()
                            binding.placeholder.gone()
                            binding.progressBar.gone()
                        }

                        is DBResult.Progress -> {
                            binding.searchPlaceholder.gone()
                            binding.recyclerView.gone()
                            binding.placeholder.gone()
                            binding.progressBar.visible()
                        }

                    }
                }
            }
        }
    }
    private fun defaultButtons(){
        binding.clear.setOnClickThrottleBounceListener {
            movetosearch()
        }
    }
    private fun filterButtons(){
        binding.state.setOnClickThrottleBounceListener{
            list.clear()
            list.addAll(listOf("Submitted","Open","Working","Review","Completed"))
            val filterBottomSheet =
                FilterBottomSheet(list,"STATE",this)
            filterBottomSheet.show(requireFragmentManager(), "PRIORITY")
        }
        binding.type.setOnClickThrottleBounceListener{
            list.clear()
            list.addAll(listOf("Bug","Feature","Feature request","Task","Exception","Security","Performance"))
            val filterBottomSheet =
                FilterBottomSheet(list,"TYPE",this)
            filterBottomSheet.show(requireFragmentManager(), "PRIORITY")
        }
        binding.assignee.setOnClickThrottleBounceListener {
            val assigneeListBottomSheet = UserListBottomSheet(OList, selectedAssignee,this,this,"ASSIGNEE")
            assigneeListBottomSheet.show(requireFragmentManager(), "assigneelist")
        }
        binding.created.setOnClickThrottleBounceListener {
            val assigneeListBottomSheet = UserListBottomSheet(OList2, selectedAssignee2,this,this,"CREATED BY")
            assigneeListBottomSheet.show(requireFragmentManager(), "assigneelist")
        }
        binding.segment.setOnClickThrottleBounceListener {
            val segment = SegmentSelectionBottomSheet()
            segment.segmentSelectionListener = this
            segment.sectionSelectionListener = this
            segment.show(requireFragmentManager(), "Segment Selection")
        }
        binding.tags.setOnClickThrottleBounceListener {
            val filterTagsBottomSheet =
                FilterTagsBottomSheet(TagList, this@SearchFragment, selectedTags)
            filterTagsBottomSheet.show(requireFragmentManager(), "OList")
        }
    }


    private fun requestFocusAndShowKeyboard(view: View) {
        view.requestFocus()
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
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

    private fun setSelectedButtonColor(button: AppCompatButton) {
        button.setBackgroundResource(R.drawable.item_bg_curve_selected)
        val drawable: Drawable? = button.compoundDrawables[2]?.mutate()
        drawable?.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(requireContext(), R.color.darkbg_main),
            PorterDuff.Mode.SRC_IN
        )
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkbg_main))
    }


    private fun setUnSelectedButtonColor(button:AppCompatButton){
        button.setBackgroundDrawable(resources.getDrawable(R.drawable.item_bg_curve))
        val drawable: Drawable? = button.compoundDrawables[2]?.mutate()
        drawable?.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(requireContext(), R.color.better_white),
            PorterDuff.Mode.SRC_IN
        )
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        button.setTextColor(resources.getColor(R.color.better_white))
    }
    override fun sendassignee(assignee: User, isChecked: Boolean, position: Int, type: String) {
        if (isChecked) {
            if (type=="ASSIGNEE"){
                binding.clear.visible()
                selectedAssignee.add(assignee)
                binding.assignee.text=assignee.username
                setSelectedButtonColor(binding.assignee)


            }
            if (type=="CREATED BY"){
                binding.clear.visible()
                selectedAssignee2.add(assignee)
                binding.created.text=assignee.username
                setSelectedButtonColor(binding.created)
            }
        }
        else{
            if (type=="ASSIGNEE"){
                selectedAssignee.remove(assignee)
                binding.assignee.text="Assignee"
                setUnSelectedButtonColor(binding.assignee)
            }
            if (type=="CREATED BY"){
                selectedAssignee2.remove(assignee)
                binding.created.text="Assignee"
                setUnSelectedButtonColor(binding.created)
            }

        }
    }

    private fun showSearch() {
        binding.searchParent.visible()
    }

    private fun hideSearch() {
        binding.searchParent.gone()
    }

    override fun onAssigneeTListUpdated(TList: MutableList<User>) {
    }

    override fun updateAssignee(assignee: User) {
    }

    override fun onCLick(position: Int, task: TaskItem) {
        val intent = Intent(requireContext(), TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
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
    private fun movetosearch() {
        val transaction = fragmentManager?.beginTransaction()
        val fragment = SearchFragment()
        transaction?.replace(R.id.nav_host_fragment_activity_main, fragment)
        transaction?.addToBackStack(null)
        transaction?.commit()

        activityBinding.bottomNav.menu.getItem(2).isChecked = true
        activityBinding.bottomNav.menu.getItem(2).setIcon(R.drawable.ic_searchico)
    }
}

