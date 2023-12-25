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
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.CheckList
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
import com.ncs.o2.UI.UIComponents.BottomSheets.AssigneeListBottomSheet
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
    sectionDisplayBottomSheet.SectionSelectionListener ,BottomSheet.SendText,
    AssigneeListBottomSheet.getassigneesCallback, AssigneeListBottomSheet.updateAssigneeCallback,ChecklistActivity.checkListListener{

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
    private var Unassigned:Boolean=false
    private var tagIdList: ArrayList<String> = ArrayList()
    private var list:MutableList<String> = mutableListOf()
    private var OList: MutableList<User> = mutableListOf()
    private val selectedTags = mutableListOf<Tag>()
    private var showsheet = false
    private var checkListArray : MutableList<CheckList> = mutableListOf()
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



    val description2 : String = """
        # bitscuit 🍪  [![](https://jitpack.io/v/arpitmx/bitscuit.svg)](https://jitpack.io/#arpitmx/bitscuit)

        bitscuit updater is an android library which the developers can hook into their project and use it to update their apps in just 3 lines of code. bitscuit is hosted at  <a href="https://jitpack.io/#arpitmx/bitscuit/1.0.5">Jitpack repository</a>

        ![Logo](https://github.com/arpitmx/bitscuit/assets/59350776/4b40f173-7f7c-4357-b0a0-43b7a6cb5733)



        ## Applications 
        bitscuit is suited for those application :
        - Apps which aren't hosted on any store like google play store
        - Distributing updates apps to testers
        - Apps which have more frequent updates cause app stores like playstore takes a lot of time (1-2 day) for verification of each update  
        - For updates involving minor upgrades in the app


        ## Features


        <br><img src="https://github.com/arpitmx/bitscuit/assets/59350776/77c7d735-1c1c-40e4-bb77-1f10c8a4c9c2" width="350"><br><br>


        - **Easy integration** : bitscuit can be easily integrated into any Android app with just three lines of code, handles permissions, configurations, version comparisions,etc. saving developers valuable time and effort, why recreating the wheel? right.
        <br><br>
        <p float="left">
        <img src="https://github.com/arpitmx/bitscuit/assets/59350776/3df5b3a9-3194-46f8-9013-8d1f48edf25b" width="350">
        <img src="https://github.com/arpitmx/bitscuit/assets/59350776/8c58fc11-fe35-4be0-90cd-cc7d9101ec8a)" width="350">
        </p><br><br>


        - **Seamless updates** : bitscuit ensures that app updates happen seamlessly, without interrupting user experience or requiring the user to manually update the app.

        <br><br>
        <img src="https://github.com/arpitmx/bitscuit/assets/59350776/a703cc37-19e0-4e33-9214-bb744fec87cb" width="350"><br><br>


        - **Error handling** : bitscuit handles errors and edge cases like connection problems gracefully, ensuring that the update process is as smooth and error-free as possible for both developers and users.


        ## Installation

        Installing bistcuit is very simple , you can install bitscuit using github release by downloading the latest jar file  

        ### Using Gradle 

        #### Step 1 : Use this in build.gradle(module: project)
        ```gradle
          allprojects {
        		repositories {
        			...
        			maven { url 'https://jitpack.io' }
        		}
        	}

        //For Gradle 7.0 and above add 'maven { url 'https://jitpack.io' }' in settings.gradle file


        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                google()
                mavenCentral()
                maven { url 'https://jitpack.io' }
            }
        }


        ```




        #### Step 2 : Use this in build.gradle(module: app)
        ```gradle
         dependencies {
        	    implementation 'com.github.arpitmx:bitscuit:1.0.5'
        	}
        ```

        ### Using Maven

        ```xml
        <repositories>
        	<repository>
        		<id>jitpack.io</id>
        		<url>https://jitpack.io</url>
        	</repository>
        </repositories>
        ```

        ```xml
        <dependency>
         	<groupId>com.github.arpitmx</groupId>
        	<artifactId>bitscuit</artifactId>
        	<version>1.0.6</version>
        </dependency>
        ```

        Do not forget to add internet permission in manifest if already not present
        ```xml
        <uses-permission android:name="android.permission.INTERNET" />
        ```



            
        ## Sample usage 

        ```kotlin
         ...

        //This data can be fetched from your database 
         val url = "https://example.com/update.apk"
         val latestVersion = "1.0.1"
         val changeLogs = "Change logs..."


        // Use the buitscuit builder to create the bitscuit instance 
        val bitscuit = Bitscuit.BitscuitBuilder(this)
                            .config(url = updatedURL,version="1.0.1",changeLogs="Change logs..")
                            .build() 
          
               
        // Use the listenUpdate() function to start listening for updates 
          bitscuit.listenUpdate()   

         ...                 
                            
        ```
        ## License
        ```
           Copyright (C) 2023 Alok Ranjan

           Licensed under the Apache License, Version 2.0 (the "License");
           you may not use this file except in compliance with the License.
           You may obtain a copy of the License at

               http://www.apache.org/licenses/LICENSE-2.0

           Unless required by applicable law or agreed to in writing, software
           distributed under the License is distributed on an "AS IS" BASIS,
           WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
           See the License for the specific language governing permissions and
           limitations under the License.

           
        ```

        ## Contributing to bitscuit
        All pull requests are welcome, make sure to follow the [contribution guidelines](Contribution.md)
        when you submit pull request.
        ## Links
        [![portfolio](https://img.shields.io/badge/my_portfolio-000?style=for-the-badge&logo=ko-fi&logoColor=white)](https://github.com/arpitmx/)
        [![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/alokandro/)
        [![twitter](https://img.shields.io/badge/twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://twitter.com/sudoarmax)


    """.trimIndent()
    val desc3 = """
        Enable debug logging for Performance Monitoring at build time by adding a <meta-data> element to your app's AndroidManifest.xml file, like so:

        ```xml
            <application>
                <meta-data
                  android:name="firebase_performance_logcat_enabled"
                  android:value="true" />
            </application>
        ```
        
        Check your log messages for any error messages.

        Performance Monitoring tags its log messages with FirebasePerformance. Using logcat filtering, you can specifically view duration trace and HTTP/S network request logging by running the following command:


        adb logcat -s FirebasePerformance
        Check for the following types of logs which indicate that Performance Monitoring is logging performance events:

        Logging trace metric: TRACE_NAME, FIREBASE_PERFORMANCE_CONSOLE_URL
        Logging network request trace: URL
    """.trimIndent()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (PrefManager.getcurrentUserdetails().ROLE>=3){
            manageViewsforModerators()
            setDefaultViewsforModerators()
        }
        if (PrefManager.getcurrentUserdetails().ROLE<3){
            manageViewsforNormalUsers()
            setDefaultViewsforNormalUsers()
        }
        manageBottomSheets()

        binding.description.setOnClickThrottleBounceListener {
            this.startActivity(Intent(this,DescriptionEditorActivity::class.java))
        }

        setUpViews()
        setUpLiveData()


    }

    private fun manageBottomSheets(){
        Codes.STRINGS.segmentText = ""
        binding.duration.setOnClickThrottleBounceListener {
            val durationBottomSheet = setDurationBottomSheet(this)
            durationBottomSheet.show(supportFragmentManager, "duration")

        }

        binding.addCheckListBtn.setOnClickThrottleBounceListener {
            ChecklistActivity.ListenerHolder.checkListListener = this
            val intent = Intent(this, ChecklistActivity::class.java)
            intent.putExtra("checkListArray", ArrayList(checkListArray))

            this.startActivity(intent)
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
                AddTagsBottomSheet(TagList, this@CreateTaskActivity, selectedTags,"create")
            addTagsBottomSheet.show(supportFragmentManager, "OList")
        }
        if (showsheet) {
            val addTagsBottomSheet =
                AddTagsBottomSheet(TagList, this@CreateTaskActivity, selectedTags,"create")
            addTagsBottomSheet.show(supportFragmentManager, "OList")
        }

        binding.taskType.setOnClickThrottleBounceListener {
            list.clear()
            list.addAll(listOf("Bug","Feature","Feature request","Task","Exception","Security","Performance"))
            val priorityBottomSheet =
                BottomSheet(list,"TYPE",this)
            priorityBottomSheet.show(supportFragmentManager, "TYPE")
        }

    }

    private fun manageViewsforNormalUsers(){
        binding.addContributorsBtn.setOnClickThrottleBounceListener {
            toast("You can't select moderators")
        }
        binding.assignee.setOnClickThrottleBounceListener {
            toast("You can't select an assignee")
        }
        binding.taskDuration.setOnClickThrottleBounceListener {
            toast("You can't set task duration")
        }
        binding.difficulty.setOnClickThrottleBounceListener {
            toast("You can't set task difficulty")
        }
        binding.status.setOnClickThrottleBounceListener {
            toast("You can't set task state")
        }
        binding.priority.setOnClickThrottleBounceListener {
            toast("You can't set task priority")
        }

    }

    private fun manageViewsforModerators(){
        binding.addContributorsBtn.isEnabled=true
        binding.addContributorsBtn.setOnClickThrottleBounceListener {
            val userListBottomSheet = UserlistBottomSheet(this)
            userListBottomSheet.show(supportFragmentManager, "contributer list")
        }
        binding.assignee.isEnabled=true
        binding.assignee.setOnClickThrottleBounceListener {
            val assigneeListBottomSheet = AssigneeListBottomSheet(OList, selectedAssignee,this@CreateTaskActivity,this)
            assigneeListBottomSheet.show(supportFragmentManager, "assigneelist")
        }
        binding.taskDuration.setOnClickThrottleBounceListener {
            val durationBottomSheet=setDurationBottomSheet(this)
            durationBottomSheet.show(supportFragmentManager,"TAG")
        }
        binding.difficulty.setOnClickThrottleBounceListener {
            list.clear()
            list.addAll(listOf("Easy","Medium","Hard"))
            val priorityBottomSheet =
                BottomSheet(list,"DIFFICULTY",this)
            priorityBottomSheet.show(supportFragmentManager, "DIFFICULTY")
        }
        binding.status.setOnClickThrottleBounceListener {
            list.clear()
            list.addAll(listOf("Submitted","Open","Working","Review","Completed"))
            val priorityBottomSheet =
                BottomSheet(list,"STATE",this)
            priorityBottomSheet.show(supportFragmentManager, "STATE")
        }
        binding.priority.setOnClickThrottleBounceListener {
            list.clear()
            list.addAll(listOf("Low","Medium","High","Critical"))
            val priorityBottomSheet =
                BottomSheet(list,"PRIORITY",this)
            priorityBottomSheet.show(supportFragmentManager, "PRIORITY")
        }


    }

    private fun postTask(task: Task,checkList: MutableList<CheckList>){
        val list:MutableList<CheckList> = mutableListOf()
        if (checkList.isNotEmpty()){
            list.addAll(checkList)
        }
        if(checkList.isEmpty()){
            list.add(CheckList(id = RandomIDGenerator.generateRandomTaskId(5),
                title = task.title, desc = task.description.substring(0,200), done = false, index = 0))
        }
        CoroutineScope(Dispatchers.Main).launch {

            repository.postTask(task,list) { result ->

                when (result) {

                    is ServerResult.Failure -> {
                        binding.progressBar.gone()
                    }

                    ServerResult.Progress -> {
                        binding.progressBar.visible()
                    }

                    is ServerResult.Success -> {
                        binding.progressBar.gone()
                        PrefManager.setcurrentsegment(binding.segment.text.toString())
                        toast("Task Created Successfully")
                        finish()
                        startActivity(intent)
                    }

                }
            }
        }
    }


    private fun setUpLiveData() {


    }

    private fun setUpViews() {
        createTask()
        setUpActionBar()
        setUpDatePicker()
        setUpClickListeners()
        setUpCallbacks()
        setupSelectedMembersRecyclerView()
    }

    private fun createTask(){
        binding.gioActionbar.btnDone.setOnClickThrottleBounceListener {
            if (binding.taskDurationET.text=="Select"){
                toast("Add Task Duration")
            }
            else if (binding.segment.text=="Segment"){
                toast("Select Task Segment")
            }
            else if (binding.section.text=="Section"){
                toast("Select Task Section")
            }
            else if (binding.title.text.isNullOrBlank()){
                toast("Enter Task Title")
            }
            else{
                val title=binding.title.text
                val difficulty= SwitchFunctions.getNumDifficultyFromStringDifficulty(binding.difficultyInclude.tagText.text.toString())
                val priority= SwitchFunctions.getNumPriorityFromStringPriority(binding.priorityInclude.tagText.text.toString())
                val status= SwitchFunctions.getNumStateFromStringState(binding.stateInclude.tagText.text.toString())

                val assigner=PrefManager.getcurrentUserdetails()
                val duration=binding.taskDurationET
                val tags=ArrayList<String>()
                for (i in 0 until selectedTags.size){
                    tags.add(selectedTags[i].tagID!!)
                }
                val segment=binding.segment.text
                val section=binding.section.text
                val type= SwitchFunctions.getNumTypeFromStringType(binding.typeInclude.tagText.text.toString())
                if (PrefManager.getcurrentUserdetails().ROLE>=2){
                    val assignee:String
                    if (selectedAssignee.isNotEmpty()) {
                        assignee = selectedAssignee[0].firebaseID!!
                    }
                    else{
                        assignee="None"
                    }
                    val task= Task(
                        title = title.toString(),
                        description = desc3,
                        id = "#T${RandomIDGenerator.generateRandomTaskId(5)}",
                        difficulty = difficulty,
                        priority = priority,
                        status = status,
                        assignee = assignee,
                        assigner = assigner.EMAIL,
                        duration = duration.text.toString(),
                        time_STAMP = Timestamp.now(),
                        tags = tags.toList().ifEmpty { emptyList() },
                        project_ID = PrefManager.getcurrentProject(),
                        segment = segment.toString(),
                        section = section.toString(),
                        completed = false,
                        type = type,
                        moderators = contributorList,
                        last_updated = Timestamp.now()
                    )
                    postTask(task,checkListArray)
                }
                else{
                    val task= Task(
                        title = title.toString(),
                        description = desc3,
                        id = "#T${RandomIDGenerator.generateRandomTaskId(5)}",
                        difficulty = difficulty,
                        priority = priority,
                        status = status,
                        assignee = "None",
                        assigner = assigner.EMAIL,
                        duration = duration.text.toString(),
                        time_STAMP = Timestamp.now(),
                        tags = tags.toList().ifEmpty { emptyList() },
                        project_ID = PrefManager.getcurrentProject(),
                        segment = segment.toString(),
                        section = section.toString(),
                        completed = false,
                        type = type,
                        moderators = emptyList(),
                        last_updated = Timestamp.now()
                    )
                    postTask(task,checkListArray)
                }
            }
        }
    }

    private fun setDefaultViewsforModerators(){
        binding.projectNameET.text=PrefManager.getcurrentProject()
        binding.priorityInclude.tagIcon.text="L"
        binding.priorityInclude.tagText.text="Low"

        binding.typeInclude.tagIcon.text="T"
        binding.typeInclude.tagText.text="Task"

        binding.stateInclude.tagIcon.text="S"
        binding.stateInclude.tagText.text="Submitted"

        binding.difficultyInclude.tagIcon.text="E"
        binding.difficultyInclude.tagIcon.background=resources.getDrawable(R.drawable.label_cardview_green)
        binding.difficultyInclude.tagText.text="Easy"

    }

    private fun setDefaultViewsforNormalUsers(){
        binding.projectNameET.text=PrefManager.getcurrentProject()
        binding.priorityInclude.tagIcon.text="L"
        binding.priorityInclude.tagText.text="Low"

        binding.typeInclude.tagIcon.text="T"
        binding.typeInclude.tagText.text="Task"

        binding.stateInclude.tagIcon.text="S"
        binding.stateInclude.tagText.text="Submitted"

        binding.difficultyInclude.tagIcon.text="E"
        binding.difficultyInclude.tagIcon.background=resources.getDrawable(R.drawable.label_cardview_green)
        binding.difficultyInclude.tagText.text="Easy"

        binding.taskDurationET.text="5 Hours"

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
                contributorList.add(contributor.firebaseID!!)

                if (contributor.profileIDUrl == null){
                    contributorDpList.add("")
                }else {
                    contributorDpList.add(contributor.profileIDUrl)

                }
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

        }
        else{
            selectedAssignee.remove(assignee)
            binding.assigneeInclude.normalET.text="Unassigned"
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

    override fun onSubmitClick() {
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

    override fun updateAssignee(assignee: User) {
    }

    override fun sendcheckListarray(list: MutableList<CheckList>) {
        checkListArray.clear()
        checkListArray.addAll(list)
        Log.d("checkListArray",checkListArray.toString())
        if (list.size>0) {
            binding.checkListCount.visible()
            binding.checkListCount.text = "(${(list.size).toString()})"
        }else{
            binding.checkListCount.gone()
        }
    }


}