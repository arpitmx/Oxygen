package com.ncs.o2.UI.CreateTask

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailViewModel
import com.ncs.o2.UI.UIComponents.Adapters.AssigneeListBottomSheet
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.AddTagsBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.BottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.Userlist.UserlistBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.sectionDisplayBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.setDurationBottomSheet
import com.ncs.o2.databinding.ActivityCreateTaskBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class CreateTaskActivity : AppCompatActivity(), ContributorAdapter.OnProfileClickCallback,
    UserlistBottomSheet.getContributorsCallback, AddTagsBottomSheet.getSelectedTagsCallback,
    SegmentSelectionBottomSheet.SegmentSelectionListener,
    SegmentSelectionBottomSheet.sendSectionsListListner,
    setDurationBottomSheet.DurationAddedListener,
    sectionDisplayBottomSheet.SectionSelectionListener ,BottomSheet.SendText,AssigneeListBottomSheet.getassigneesCallback{
    private var contriList: MutableList<User> = mutableListOf()
    @Inject
    lateinit var firestoreRepository:FirestoreRepository
    @Inject
    @FirebaseRepository
    lateinit var repository: Repository
    private val selectedAssignee:MutableList<User> = mutableListOf()
    private var contributorList: MutableList<String> = mutableListOf()
    private var contributorDpList: MutableList<String> = mutableListOf()
    private val viewModel: CreateTaskViewModel by viewModels()
    private var TagList: MutableList<Tag> = mutableListOf()
    private var tagIdList: ArrayList<String> = ArrayList()
    private var list:MutableList<String> = mutableListOf()
    private var OList: MutableList<User> = mutableListOf()
    private val selectedTags = mutableListOf<Tag>()
    private var showsheet = false
    private val binding: ActivityCreateTaskBinding by lazy {
        ActivityCreateTaskBinding.inflate(layoutInflater)
    }

//    private val viewmodel: CreateTaskViewModel by viewModels()

    private val easyElements: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }

    private val contriRecyclerView: RecyclerView by lazy {
        binding.contributorsRecyclerView
    }

    lateinit var contriAdapter: ContributorAdapter

    @Inject
    lateinit var calendar: Calendar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Codes.STRINGS.segmentText = ""

//
//        binding.gioActionbar.btnDone.setOnClickThrottleBounceListener {
//
//            if (binding.tvTitle.text!!.isEmpty() ||
//                binding.tvDescription.text!!.isEmpty() ||
//                binding.startDate.text == "Set Start Date" ||
//                binding.endDate.text == "Set Deadline Date" ||
//                binding.duration.text == "Set Duration" ||
//                binding.segment.text == "Set Segment" ||
//                binding.section.text == "Set Section" ||
//                binding.chipGroup.isEmpty() ||
//                binding.contributorsRecyclerView.isEmpty() ||
//                diffLevel == 0
//            ) {
//                val currentTimestamp = Timestamp.now()
//                Log.d("TimeStampCheck", currentTimestamp.toString())
//                Toast.makeText(this, "Pls complete the entries", Toast.LENGTH_SHORT).show()
//                return@setOnClickThrottleBounceListener
//            }
//
//            val currentUser = PrefManager.getcurrentUserdetails()
//            val task = Task(
//                id = "#T${RandomIDGenerator.generateRandomTaskId(5)}",
//                title = binding.tvTitle.text.toString(),
//                description = binding.tvDescription.text.toString(),
//                difficulty = diffLevel,
//                assignee = contributorList,
//                assigner = currentUser.USERNAME,
//                deadline = binding.endDate.text.toString(),
//                duration = binding.duration.text.toString(),
//                assigner_email = currentUser.EMAIL,
//                tags = tagIdList,
//                segment = binding.segment.text.toString(),
//                section = binding.section.text.toString(),
//                time_STAMP = Timestamp.now(),
//                completed = false,
//            )
//
//            viewModel.addTaskThroughRepository(task)
//
//            Log.d("taskCheck", task.toString())
//
//            Toast.makeText(this, "Task Created", Toast.LENGTH_SHORT).show()
//
//            onBackPressedDispatcher.onBackPressed()
//            finish()
//
//        }

        // Activity -> Viewmodel -> PostUsecase + GetUsecase -> Repository(DB)-> Firestore db


//        binding.diffChipGp.setOnCheckedChangeListener { group, checkedId ->
//            // Handle chip selection change
//            val selectedChip = findViewById<Chip>(checkedId)
//            // Do something with the selected chip
//            if (!selectedChip.isNull) {
//                if (selectedChip.text == "Easy") {
//                    diffLevel = 1
//                } else if (selectedChip.text == "Medium") {
//                    diffLevel = 2
//                } else if (selectedChip.text == "Difficult") {
//                    diffLevel = 3
//                }
//            }
//        }


        binding.duration.setOnClickThrottleBounceListener {
//            viewmodel.createTask(testTask)

            val durationBottomSheet = setDurationBottomSheet(this)
            durationBottomSheet.show(supportFragmentManager, "duration")

        }

        if (PrefManager.getcurrentUserdetails().ROLE<2){
            binding.addContributorsBtn.isEnabled=false
        }
        if (PrefManager.getcurrentUserdetails().ROLE>=2) {
            binding.addContributorsBtn.isEnabled=true
            binding.addContributorsBtn.setOnClickThrottleBounceListener {

                val userListBottomSheet = UserlistBottomSheet(this)
                userListBottomSheet.show(supportFragmentManager, "contributer list")

            }

        }



        binding.segment.setOnClickThrottleBounceListener {

            val segment = SegmentSelectionBottomSheet()
            segment.segmentSelectionListener = this
            segment.sectionSelectionListener = this
            segment.show(supportFragmentManager, "Segment Selection")
        }

        binding.section.setOnClickThrottleBounceListener {

            if (Codes.STRINGS.segmentText == "") {
                Toast.makeText(this, "First please select segment", Toast.LENGTH_SHORT).show()
            } else {
                val sections = sectionDisplayBottomSheet()
                sections.sectionSelectionListener = this
                sections.show(supportFragmentManager, "Section Selection")
            }
        }

        binding.addtags.setOnClickThrottleBounceListener {
            val addTagsBottomSheet =
                AddTagsBottomSheet(TagList, this@CreateTaskActivity, selectedTags)
            addTagsBottomSheet.show(supportFragmentManager, "OList")
        }
        if (showsheet) {
            val addTagsBottomSheet =
                AddTagsBottomSheet(TagList, this@CreateTaskActivity, selectedTags)
            addTagsBottomSheet.show(supportFragmentManager, "OList")
        }

        binding.priority.setOnClickThrottleBounceListener {
            list.clear()
            list.addAll(listOf("Low","Medium","High","Critical"))
            val priorityBottomSheet =
                BottomSheet(list,"PRIORITY",this)
            priorityBottomSheet.show(supportFragmentManager, "PRIORITY")
        }

        binding.status.setOnClickThrottleBounceListener {
            list.clear()
            list.addAll(listOf("Unassigned","Ongoing","Open","Review","Testing"))
            val priorityBottomSheet =
                BottomSheet(list,"STATE",this)
            priorityBottomSheet.show(supportFragmentManager, "STATE")
        }

        binding.taskType.setOnClickThrottleBounceListener {
            list.clear()
            list.addAll(listOf("Bug","Feature","Feature request","Task","Exception","Security","Performance"))
            val priorityBottomSheet =
                BottomSheet(list,"TYPE",this)
            priorityBottomSheet.show(supportFragmentManager, "TYPE")
        }

        binding.difficulty.setOnClickThrottleBounceListener {
            list.clear()
            list.addAll(listOf("Easy","Medium","Hard"))
            val priorityBottomSheet =
                BottomSheet(list,"DIFFICULTY",this)
            priorityBottomSheet.show(supportFragmentManager, "DIFFICULTY")
        }

        binding.taskDuration.setOnClickThrottleBounceListener {
            val durationBottomSheet=setDurationBottomSheet(this)
            durationBottomSheet.show(supportFragmentManager,"TAG")
        }

        binding.description.setOnClickThrottleBounceListener {
            this.startActivity(Intent(this,DescriptionEditorActivity::class.java))
        }

        if (PrefManager.getcurrentUserdetails().ROLE<2){
            binding.assignee.isEnabled=false
        }
        if (PrefManager.getcurrentUserdetails().ROLE>=2) {
            binding.assignee.isEnabled=true
            binding.assignee.setOnClickThrottleBounceListener {
                val assigneeListBottomSheet = AssigneeListBottomSheet(OList, selectedAssignee,this@CreateTaskActivity)
                assigneeListBottomSheet.show(supportFragmentManager, "assigneelist")
            }

        }
        binding.gioActionbar.btnDone.setOnClickThrottleBounceListener {
            var validate=true
            if (binding.taskDurationET.text=="Select"){
                toast("Add Task Duration")
                validate=false
            }
            if (binding.segment.text=="Segment"){
                toast("Select Task Segment")
                validate=false
            }
            if (binding.section.text=="Section"){
                toast("Select Task Section")
                validate=false
            }
            if (binding.title.text.isNullOrBlank()){
                toast("Enter Task Title")
                validate=false
            }
            if (binding.chipGroup.isEmpty()){
                toast("Tags are required")
                validate=false
            }
            if (validate){
                val title=binding.title.text
                val difficulty= when(binding.difficultyInclude.tagText.text){
                    "Easy" -> 1
                    "Medium" -> 2
                    "Hard" -> 3
                    else -> -1
                }
                val priority= when(binding.priorityInclude.tagText.text){
                    "Low" -> 1
                    "Medium" -> 2
                    "High" -> 3
                    "Critical" -> 4
                    else -> -1
                }
                val status= when(binding.stateInclude.tagText.text){
                    "Unassigned" -> 1
                    "Ongoing" -> 2
                    "Open" -> 3
                    "Review" -> 4
                    "Testing" -> 5
                    else -> -1
                }

                val assigner=PrefManager.getcurrentUserdetails()
                val duration=binding.taskDurationET
                val tags=ArrayList<String>()
                for (i in 0 until selectedTags.size){
                    tags.add(selectedTags[i].tagID!!)
                }
                val segment=binding.segment.text
                val section=binding.section.text
                val type= when(binding.typeInclude.tagText.text){
                    "Bug" -> 1
                    "Feature" -> 2
                    "Feature request" -> 3
                    "Task" -> 4
                    "Exception" -> 5
                    "Security" -> 6
                    "Performance" -> 7
                    else -> -1
                }
                if (PrefManager.getcurrentUserdetails().ROLE>=2){
                    val assignee=selectedAssignee[0].firebaseID
                    val task= Task(
                        title = title.toString(),
                        description = "",
                        id = "#T${RandomIDGenerator.generateRandomTaskId(5)}",
                        difficulty = difficulty,
                        priority = priority,
                        status = status,
                        assignee = assignee,
                        assigner = assigner.EMAIL,
                        duration = duration.text.toString(),
                        time_STAMP = Timestamp.now(),
                        tags = tags.toList(),
                        project_ID = PrefManager.getcurrentProject(),
                        segment = segment.toString(),
                        section = section.toString(),
                        completed = false,
                        type = type,
                        moderators = contributorList
                    )
                    postTask(task)
                }
                else{
                    val task= Task(
                        title = title.toString(),
                        description = "",
                        id = "#T${RandomIDGenerator.generateRandomTaskId(5)}",
                        difficulty = difficulty,
                        priority = priority,
                        status = status,
                        assignee = "None",
                        assigner = assigner.EMAIL,
                        duration = duration.text.toString(),
                        time_STAMP = Timestamp.now(),
                        tags = tags.toList(),
                        project_ID = PrefManager.getcurrentProject(),
                        segment = segment.toString(),
                        section = section.toString(),
                        completed = false,
                        type = type,
                        moderators = listOf("None")
                    )
                    postTask(task)
                }

            }




        }

        setUpViews()
        setUpLiveData()


    }

    private fun postTask(task: Task){
        CoroutineScope(Dispatchers.Main).launch {

            repository.postTask(task) { result ->

                when (result) {

                    is ServerResult.Failure -> {
                        binding.progressBar.gone()
                    }

                    ServerResult.Progress -> {
                        binding.progressBar.visible()


                    }

                    is ServerResult.Success -> {
                        binding.progressBar.gone()
                        toast("Task Created Successfully")
                        finish()
                        startActivity(intent)
                    }

                }
            }
        }
    }


    private fun setUpLiveData() {
        //Progress listener
//        viewmodel.progressLiveData.observe(this){ visibility ->
//            if (visibility){
//                binding.progressCircularInclude.root.visible()
//            }else {
//                binding.progressCircularInclude.root.gone()
//            }
//        }
//
//        viewmodel.successLiveData.observe(this){
//            if (it){
//                toast("Task Published...")
//                finish()
//            }
//        }

    }

    private fun setUpViews() {
        setUpActionBar()
        setUpDatePicker()
        setUpClickListeners()
        setUpCallbacks()
        setupSelectedMembersRecyclerView()
        setDefaultViews()
    }

    private fun setDefaultViews(){
        binding.projectNameET.text=PrefManager.getcurrentProject()

        binding.priorityInclude.tagIcon.text="L"
        binding.priorityInclude.tagText.text="Low"

        binding.typeInclude.tagIcon.text="T"
        binding.typeInclude.tagText.text="Task"

        binding.stateInclude.tagIcon.text="O"
        binding.stateInclude.tagText.text="Open"

        binding.difficultyInclude.tagIcon.text="E"
        binding.difficultyInclude.tagIcon.background=resources.getDrawable(R.drawable.label_cardview_green)
        binding.difficultyInclude.tagText.text="Easy"




    }

    private fun setupSelectedMembersRecyclerView() {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP

        contriRecyclerView.layoutManager = layoutManager
        contriAdapter = ContributorAdapter(mutableListOf(), this)
        contriRecyclerView.adapter = contriAdapter
        contriRecyclerView.visible()

    }

    private fun setUpCallbacks() {
//        viewmodel.serverExceptionLiveData.observe(this){exceptionMsg->
//            easyElements.dialog("Server error",exceptionMsg,{},{})
//        }
    }

    private fun setUpClickListeners() {
        binding.startDate.setOnClickThrottleBounceListener {
            startdatePickerDialog.show()
        }

        binding.endDate.setOnClickThrottleBounceListener {
            enddatePickerDialog.show()
        }
    }

    private fun updateChipGroup() {
        val chipGroup = binding.chipGroup
        chipGroup.removeAllViews()
        Log.d("select", selectedTags.toString())
        for (tag in selectedTags) {
            val chip = Chip(this)
            chip.text = tag.tagText
            chip.isCloseIconVisible = true
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(tag.bgColor)))
            chip.setTextColor(ColorStateList.valueOf(Color.parseColor(tag.textColor)))
            chip.setOnCloseIconClickListener {
                selectedTags.remove(tag)
                if (TagList.contains(tag)) {
                    val index = TagList.indexOf(tag)
                    TagList[index].checked = false
                }
                updateChipGroup()
            }
            chipGroup.addView(chip)
            chip.setOnClickListener {
                selectedTags.remove(tag)
                if (TagList.contains(tag)) {
                    val index = TagList.indexOf(tag)
                    TagList[index].checked = false
                }
                updateChipGroup()

            }
        }
    }


    lateinit var startdatePickerDialog: DatePickerDialog
    lateinit var enddatePickerDialog: DatePickerDialog
    private fun setUpDatePicker() {

        startdatePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Update the calendar with the selected date
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Update the text on the button with the selected date
                val selectedDate =
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
                binding.startDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        enddatePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Update the calendar with the selected date
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Update the text on the button with the selected date
                val selectedDate =
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
                binding.endDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        startdatePickerDialog.datePicker.minDate = calendar.timeInMillis
        enddatePickerDialog.datePicker.minDate = calendar.timeInMillis

    }

    private fun setUpActionBar() {

        binding.gioActionbar.titleTv.visible()
        binding.gioActionbar.titleTv.text = getString(R.string.new_task)

        binding.gioActionbar.btnDone.visible()
        binding.gioActionbar.btnFav.gone()
        binding.gioActionbar.btnRequestWork.gone()
        binding.gioActionbar.btnBack.setOnClickThrottleBounceListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
    }

    override fun onProfileClick(user: User, position: Int) {
    }

    override fun removeClick(user: User, position: Int) {
        contriList = UserlistBottomSheet.DataHolder.users
        contriAdapter.removeUser(user)
        val pos = UserlistBottomSheet.DataHolder.users.indexOf(user)
        UserlistBottomSheet.DataHolder.users[pos].isChecked = false
    }

    override fun onSelectedContributors(contributor: User, isChecked: Boolean) {
        if (isChecked) {
            if (!contriAdapter.isUserAdded(contributor)) {
                contriAdapter.addUser(contributor)
                contributorList.add(contributor.firebaseID)
                contributorDpList.add(contributor.profileIDUrl)
            }
        } else {
            contriAdapter.removeUser(contributor)
            contributorList.remove(contributor.firebaseID)
            contributorDpList.remove(contributor.profileIDUrl)
        }
    }

    override fun sendassignee(assignee: User, isChecked: Boolean,position: Int) {
        if (isChecked) {
            selectedAssignee.add(assignee)
                binding.assigneeInclude.normalET.text=assignee.username
                Glide.with(this)
                    .load(assignee.profileDPUrl)
                    .listener(object : RequestListener<Drawable> {

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .encodeQuality(80)
                    .override(40, 40)
                    .apply(
                        RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

                    )
                    .error(R.drawable.profile_pic_placeholder)
                    .into(binding.assigneeInclude.tagIcon)

        } else {
            selectedAssignee.remove(assignee)
            binding.assigneeInclude.normalET.text="Select"
            binding.assigneeInclude.tagIcon.setImageResource(R.drawable.profile_pic_placeholder)

        }

    }

    override fun onAssigneeTListUpdated(TList: MutableList<User>) {

    }

    override fun onTListUpdated(TList: MutableList<User>) {
        UserlistBottomSheet.DataHolder.users.clear()
        UserlistBottomSheet.DataHolder.users = TList
    }



    override fun onSelectedTags(tag: Tag, isChecked: Boolean) {
        if (isChecked) {
            selectedTags.add(tag)
            tag.tagID?.let { tagIdList.add(it) }
            updateChipGroup()
        } else {
            selectedTags.remove(tag)
            tag.tagID?.let { tagIdList.remove(it) }
            updateChipGroup()
        }

        Log.d("checktagIds", tagIdList.toString())

    }

    override fun onTagListUpdated(tagList: MutableList<Tag>) {
        TagList.clear()
        TagList = tagList
    }

    override fun onSegmentSelected(segmentName: String) {
        binding.segment.text = segmentName
        Codes.STRINGS.segmentText = segmentName
    }

    override fun sendSectionsList(list: MutableList<String>) {
    }

    override fun onDurationAdded(duration: String) {
        binding.taskDurationET.text = duration
    }

    override fun onSectionSelected(sectionName: String) {
        binding.section.text = sectionName
    }

    override fun stringtext(text: String,type:String) {
        when(type){
            "PRIORITY" -> {
                binding.priorityInclude.tagIcon.text=text.substring(0,1)
                binding.priorityInclude.tagText.text=text
            }
            "STATE" ->  {
                binding.stateInclude.tagIcon.text=text.substring(0,1)
                binding.stateInclude.tagText.text=text
            }
            "TYPE" -> {
                binding.typeInclude.tagIcon.text=text.substring(0,1)
                binding.typeInclude.tagText.text=text
            }
            "DIFFICULTY" -> {
                when(text){
                    "Easy"->binding.difficultyInclude.tagIcon.background=this.resources.getDrawable(R.drawable.label_cardview_green)
                    "Medium"->binding.difficultyInclude.tagIcon.background=this.resources.getDrawable(R.drawable.label_cardview_yellow)
                    "Hard"->binding.difficultyInclude.tagIcon.background=this.resources.getDrawable(R.drawable.label_cardview_red)
                }
                binding.difficultyInclude.tagIcon.text=text.substring(0,1)
                binding.difficultyInclude.tagText.text=text
            }
        }
    }


}