package com.ncs.o2.UI.CreateTask

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toUpperCase
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.tiagohm.markdownview.css.InternalStyleSheet
import br.tiagohm.markdownview.css.styles.Github
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
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Constants.SwitchFunctions
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.loadProfileImg
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.NotificationsUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Details.CodeViewerActivity
import com.ncs.o2.UI.Tasks.TaskPage.Details.ImageAdapter
import com.ncs.o2.UI.Tasks.TaskPage.Details.ImageViewerActivity
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.UIComponents.BottomSheets.AssigneeListBottomSheet
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.AddTagsBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.BottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.Userlist.UserlistBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.sectionDisplayBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.setDurationBottomSheet
import com.ncs.o2.databinding.ActivityCreateTaskBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
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
    AssigneeListBottomSheet.getassigneesCallback, AssigneeListBottomSheet.updateAssigneeCallback,ChecklistActivity.checkListListener,ImageAdapter.ImagesListner{

    private var contriList: MutableList<User> = mutableListOf()
    @Inject
    lateinit var firestoreRepository:FirestoreRepository
    @Inject
    @FirebaseRepository
    lateinit var repository: Repository
    @Inject
    lateinit var db: TasksDatabase
    private var selectedAssignee:MutableList<User> = mutableListOf()
    private var contributorList: MutableList<String> = mutableListOf()
    private var contributorDpList: MutableList<String> = mutableListOf()
    private val viewModel: CreateTaskViewModel by viewModels()
    private var TagList: MutableList<Tag> = mutableListOf()
    private var Unassigned:Boolean=false
    private var tagIdList: ArrayList<String> = ArrayList()
    private var list:MutableList<String> = mutableListOf()
    private var OList: MutableList<User> = mutableListOf()
    private val selectedTags = mutableListOf<Tag>()
    lateinit var draft:Task
    private var showsheet = false
    @Inject
    lateinit var utils: GlobalUtils.EasyElements
    private var checkListArray : MutableList<CheckList> = mutableListOf()
    private val binding: ActivityCreateTaskBinding by lazy {
        ActivityCreateTaskBinding.inflate(layoutInflater)
    }
    private var description:String?=null


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
        draft=PrefManager.getDraftTask()!!


        if (PrefManager.getcurrentUserdetails().ROLE>=3){
            manageViewsforModerators()
            setDefaultViewsforModerators()
        }
        if (PrefManager.getcurrentUserdetails().ROLE<3){
            manageViewsforNormalUsers()
            setDefaultViewsforNormalUsers()
        }
        manageBottomSheets()


        setUpViews()
        setUpLiveData()
        if (description.isNull){
            binding.description.isClickable=true
            binding.description.setOnClickThrottleBounceListener {
                val intent = Intent(this, DescriptionEditorActivity::class.java)
                intent.putExtra("summary", description)
                startActivityForResult(intent,1)
            }
        }
        else{
            binding.description.isClickable=false
            binding.EditSummary.setOnClickThrottleBounceListener {
                val intent = Intent(this, DescriptionEditorActivity::class.java)
                intent.putExtra("summary", description)
                startActivityForResult(intent,1)
            }
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val summary = data?.getStringExtra("summary")
            if (!summary.isNull){
                description=summary!!.trimIndent()
                draft.description=description!!
                PrefManager.putDraftTask(draft)

                if (summary.length>400){

                    setUpTaskDescription(summary!!.trimIndent().substring(0,400))
                }
                else{
                    setUpTaskDescription(summary!!.trimIndent())
                }
                binding.description.isClickable=false
                binding.EditSummary.setOnClickThrottleBounceListener {
                    val intent = Intent(this, DescriptionEditorActivity::class.java)
                    intent.putExtra("summary", description)
                    startActivityForResult(intent,1)
                }
            }
        }
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

            val segment = SegmentSelectionBottomSheet("Create Task")
            segment.segmentSelectionListener = this
            segment.sectionSelectionListener = this
            segment.show(supportFragmentManager, "Segment Selection")
        }

        binding.section.setOnClickThrottleBounceListener {

            if (PrefManager.getDraftTask()?.segment!! == "") {
                Toast.makeText(this, "First please select segment", Toast.LENGTH_SHORT).show()
            } else {
                val sections = sectionDisplayBottomSheet(PrefManager.getDraftTask()?.segment!!)
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
            if (task.description?.length!!>=200){
                list.add(CheckList(id = RandomIDGenerator.generateRandomTaskId(5),
                    title = task?.title!!, desc = task?.description!!.substring(0,200), done = false, index = 0))
            }
            else{
                list.add(CheckList(id = RandomIDGenerator.generateRandomTaskId(5),
                    title = task.title!!, desc = task.description!!, done = false, index = 0))
            }
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
                        PrefManager.putDraftTask(Task())
                        PrefManager.putDraftCheckLists(emptyList())
                        binding.progressBar.gone()
                        PrefManager.setcurrentsegment(binding.segment.text.toString())
                        toast("Task Created Successfully")
                        if (task.assignee!="None"){
                            val notification = composeNotification(
                                NotificationType.TASK_ASSIGNED_NOTIFICATION,
                                message = "You are assigned as an assignee in the task ${task.id} in the project ${PrefManager.getcurrentProject()}",
                                assignee = task.assignee!!,
                                taskID = task.id
                            )
                            repository.insertNotification(selectedAssignee[0].firebaseID!!, notification = notification!!) { res ->
                                when (res) {
                                    is ServerResult.Success -> {
                                        binding.progressBar.gone()
                                        notification.let {
                                            sendNotification(
                                                listOf(selectedAssignee[0].fcmToken!!).toMutableList(),
                                                notification
                                            )
                                        }
                                    }

                                    is ServerResult.Failure -> {
                                        binding.progressBar.gone()
                                        val errorMessage = res.exception.message
                                        GlobalUtils.EasyElements(this@CreateTaskActivity)
                                            .singleBtnDialog(
                                                "Failure",
                                                "Failed in sending notification: $errorMessage",
                                                "Okay"
                                            ) {
                                                recreate()
                                            }
                                    }

                                    is ServerResult.Progress -> {
                                        binding.progressBar.visible()
                                    }
                                }
                            }
                        }
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
            else if(description.isNull){
                toast("Task Summary is required")
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
                val id= generateTaskID(PrefManager.getcurrentProject())
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
                        description = description!!,
                        id = id,
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
                        type = type,
                        moderators = contributorList,
                        last_updated = Timestamp.now()
                    )
                    postTask(task,checkListArray)
                }
                else{
                    val task= Task(
                        title = title.toString(),
                        description = description!!,
                        id = id,
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
        if (PrefManager.getDraftTask()== Task()){

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

            binding.taskDurationET.text="Select"

            binding.segment.text="Segment"

            binding.section.text="Section"

            binding.title.text!!.clear()

            description=""
            binding.markdownView.gone()
            binding.textView2.visible()
            checkListArray.clear()
            binding.checkListCount.gone()
        }
        else{
            Log.d("projectIDIssueDraft",draft.project_ID)
            Log.d("projectIDIssue",PrefManager.getcurrentProject())
            if (draft.project_ID!=PrefManager.getcurrentProject()){
                PrefManager.putDraftTask(Task(project_ID = PrefManager.getcurrentProject()))
                PrefManager.putDraftCheckLists(emptyList())
                finish()
                startActivity(intent)
            }
            else {
                draft.project_ID = PrefManager.getcurrentProject()
                PrefManager.putDraftTask(draft)
                val handler = Handler()

                binding.title.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        charSequence: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        charSequence: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(editable: Editable?) {
                        val inputText = editable.toString()
                        draft.title = inputText
                        handler.removeCallbacksAndMessages(null)
                        handler.postDelayed({
                            PrefManager.putDraftTask(draft)
                        }, 2000)
                    }
                })
                val draftTask = PrefManager.getDraftTask()

                val projectName = draftTask?.project_ID
                if (projectName != "") {
                    binding.projectNameET.text = draftTask?.project_ID
                } else {
                    binding.projectNameET.text = PrefManager.getcurrentProject()
                }

                val _priority = draftTask?.priority
                if (_priority != 0) {
                    val priority =
                        SwitchFunctions.getStringPriorityFromNumPriority(draftTask?.priority!!)
                    binding.priorityInclude.tagIcon.text = priority.substring(0, 1)
                    binding.priorityInclude.tagText.text = priority
                } else {
                    binding.priorityInclude.tagIcon.text = "L"
                    binding.priorityInclude.tagText.text = "Low"
                }

                val _type = draftTask?.type
                if (_type != 0) {
                    val type = SwitchFunctions.getStringTypeFromNumType(draftTask.type!!)
                    binding.typeInclude.tagIcon.text = type.substring(0, 1)
                    binding.typeInclude.tagText.text = type
                } else {
                    binding.typeInclude.tagIcon.text = "T"
                    binding.typeInclude.tagText.text = "Task"
                }

                val _state = draftTask?.status
                if (_state != -1) {
                    val state = SwitchFunctions.getStringStateFromNumState(draftTask.status!!)
                    binding.stateInclude.tagIcon.text = state.substring(0, 1)
                    binding.stateInclude.tagText.text = state
                } else {
                    binding.stateInclude.tagIcon.text = "S"
                    binding.stateInclude.tagText.text = "Submitted"
                }

                val _difficulty = draftTask?.difficulty
                if (_difficulty != 0) {
                    val difficulty =
                        SwitchFunctions.getStringDifficultyFromNumDifficulty(draftTask.difficulty!!)
                    val difficultyDrawable = SwitchFunctions.getDrawableDifficultyFromNumDifficulty(
                        draftTask.difficulty!!,
                        this
                    )
                    binding.difficultyInclude.tagIcon.text = difficulty.substring(0, 1)
                    binding.difficultyInclude.tagIcon.background = difficultyDrawable
                    binding.difficultyInclude.tagText.text = difficulty
                } else {
                    binding.difficultyInclude.tagIcon.text = "E"
                    binding.difficultyInclude.tagIcon.background =
                        resources.getDrawable(R.drawable.label_cardview_green)
                    binding.difficultyInclude.tagText.text = "Easy"
                }

                val _duration = draftTask?.duration
                if (_duration != "") {
                    val duration = draftTask.duration
                    binding.taskDurationET.text = duration
                } else {
                    binding.taskDurationET.text = "Select"
                }

                val _segment = draftTask?.segment
                if (_segment != "") {
                    binding.segment.text = draftTask.segment
                    Codes.STRINGS.segmentText = draftTask.segment
                } else {
                    binding.segment.text = "Segment"
                }

                val _section = draftTask?.section
                if (_section != "") {
                    binding.section.text = draftTask.section
                } else {
                    binding.section.text = "Section"
                }

                val _title = draftTask?.title
                if (_title != "") {
                    binding.title.setText(draftTask.title)
                } else {
                    binding.title.text?.clear()
                }

                val _desc = draftTask?.description
                if (_desc != "") {
                    val summary = draftTask.description
                    description = summary
                    setUpTaskDescription(summary!!)
                } else {
                    binding.markdownView.gone()
                    binding.textView2.visible()
                }


                val checkLists = PrefManager.getDraftCheckLists()
                if (checkLists.isNullOrEmpty()) {
                    binding.checkListCount.gone()
                } else {
                    binding.checkListCount.visible()
                    binding.checkListCount.text = "(${(checkLists.size).toString()})"
                    checkListArray = checkLists.toMutableList()
                }


            }
        }

    }

    private fun setDefaultViewsforNormalUsers(){

        if (PrefManager.getDraftTask()== Task()){
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

            binding.segment.text="Segment"

            binding.section.text="Section"

            binding.title.text!!.clear()

            description=""
            binding.markdownView.gone()
            binding.textView2.visible()
            checkListArray.clear()
            binding.checkListCount.gone()
        }
        else{
            Log.d("projectIDIssueDraft",draft.project_ID)
            Log.d("projectIDIssue",PrefManager.getcurrentProject())

            if (draft.project_ID!=PrefManager.getcurrentProject()){
                PrefManager.putDraftTask(Task(project_ID = PrefManager.getcurrentProject()))
                PrefManager.putDraftCheckLists(emptyList())
                finish()
                startActivity(intent)
            }
            else{
            draft.project_ID=PrefManager.getcurrentProject()
            PrefManager.putDraftTask(draft)
            val handler = Handler()

            binding.title.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
                    val inputText = editable.toString()
                    draft.title = inputText
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        PrefManager.putDraftTask(draft)
                    }, 2000)
                }
            })
            val draftTask=PrefManager.getDraftTask()

            val projectName=draftTask?.project_ID
            if (projectName!=""){
                binding.projectNameET.text=draftTask?.project_ID
            }
            else{
                binding.projectNameET.text=PrefManager.getcurrentProject()
            }

            val _priority=draftTask?.priority
            if (_priority!=0){
                val priority=SwitchFunctions.getStringPriorityFromNumPriority(draftTask?.priority!!)
                binding.priorityInclude.tagIcon.text=priority.substring(0,1)
                binding.priorityInclude.tagText.text=priority
            }
            else{
                binding.priorityInclude.tagIcon.text="L"
                binding.priorityInclude.tagText.text="Low"
            }

            val _type=draftTask?.type
            if (_type!=0){
                val type=SwitchFunctions.getStringTypeFromNumType(draftTask.type!!)
                binding.typeInclude.tagIcon.text=type.substring(0,1)
                binding.typeInclude.tagText.text=type
            }
            else{
                binding.typeInclude.tagIcon.text="T"
                binding.typeInclude.tagText.text="Task"
            }

            val _state=draftTask?.status
            if (_state!=-1){
                val state=SwitchFunctions.getStringStateFromNumState(draftTask.status!!)
                binding.stateInclude.tagIcon.text=state.substring(0,1)
                binding.stateInclude.tagText.text=state
            }
            else{
                binding.stateInclude.tagIcon.text="S"
                binding.stateInclude.tagText.text="Submitted"
            }

            val _difficulty=draftTask?.difficulty
            if (_difficulty!=0){
                val difficulty=SwitchFunctions.getStringDifficultyFromNumDifficulty(draftTask.difficulty!!)
                val difficultyDrawable=SwitchFunctions.getDrawableDifficultyFromNumDifficulty(draftTask.difficulty!!,this)
                binding.difficultyInclude.tagIcon.text=difficulty.substring(0,1)
                binding.difficultyInclude.tagIcon.background=difficultyDrawable
                binding.difficultyInclude.tagText.text=difficulty
            }
            else{
                binding.difficultyInclude.tagIcon.text="E"
                binding.difficultyInclude.tagIcon.background=resources.getDrawable(R.drawable.label_cardview_green)
                binding.difficultyInclude.tagText.text="Easy"
            }

            val _duration=draftTask?.duration
            if (_duration!=""){
                val duration=draftTask.duration
                binding.taskDurationET.text=duration
            }
            else{
                binding.taskDurationET.text="5 Hours"
            }

            val _segment=draftTask?.segment
            if (_segment!=""){
                binding.segment.text=draftTask.segment
                Codes.STRINGS.segmentText= draftTask.segment

            }
            else{
                binding.segment.text="Segment"
            }

            val _section=draftTask?.section
            if (_section!=""){
                binding.section.text=draftTask.section
            }
            else{
                binding.section.text="Section"
            }

            val _title=draftTask?.title
            if (_title!=""){
                binding.title.setText(draftTask.title)
            }
            else{
                binding.title.text?.clear()
            }

            val _desc=draftTask?.description
            if (_desc!=""){
                val summary=draftTask.description
                description=summary
                setUpTaskDescription(summary!!)
            }
            else{
                binding.markdownView.gone()
                binding.textView2.visible()
            }



            val checkLists=PrefManager.getDraftCheckLists()

            if (checkLists.isNullOrEmpty()){
                binding.checkListCount.gone()
            }
            else{
                binding.checkListCount.visible()
                binding.checkListCount.text= "(${(checkLists.size).toString()})"
                checkListArray=checkLists.toMutableList()
            }


            val tags=draftTask?.tags
            if (!tags.isNullOrEmpty()){
                CoroutineScope(Dispatchers.IO).launch{
                    for (tagId in tags){
                        val tag=db.tagsDao().getTagbyId(tagId)

                        if (!tag.isNull) {
                            tag?.checked = true
                            selectedTags.add(tag!!)
                        }
                    }
                    withContext(Dispatchers.Main){
                        updateChipGroup()
                    }
                }
            }

            }
        }

    }


    private fun setupSelectedMembersRecyclerView() {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        contriRecyclerView.visible()
        contriRecyclerView.layoutManager = layoutManager
        contriAdapter = ContributorAdapter(mutableListOf(), this)
        contriRecyclerView.adapter = contriAdapter

    }

    private fun setUpModerators(list: MutableList<User>){

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

    fun generateProjectNamePrefix(projectName: String): String {
        val words = projectName.split(" ")
        return when {
            words.size == 1 -> {
                if (projectName.length >= 2) {
                    "${projectName.substring(0, 2)}${projectName.last()}"
                } else {
                    projectName
                }
            }
            words.size >= 2 -> {
                val firstWord = words[0]
                val secondWord = words[1]
                if (firstWord.length >= 2) {
                    "${firstWord.substring(0, 2)}${secondWord.first()}${secondWord.last()}"
                } else {
                    projectName
                }
            }
            else -> projectName
        }.toUpperCase()
    }

    fun generateTaskID(projectName: String):String{
        return "#${generateProjectNamePrefix(projectName)}-${RandomIDGenerator.generateRandomTaskId(4)}"
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
            UserlistBottomSheet.DataHolder.users.clear()
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
    }

    override fun onBackPressed() {
        UserlistBottomSheet.DataHolder.users.clear()
        onBackPressedDispatcher.onBackPressed()
        finish()
        super.onBackPressed()
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
            draft.assignee=assignee.firebaseID!!
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
            draft.assignee="None"
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
        draft.segment=segmentName
        draft.section="Section"
        PrefManager.putDraftTask(draft)
        Codes.STRINGS.segmentText = segmentName
        binding.section.text="Section"
    }

    override fun sendSectionsList(list: MutableList<String>) {
    }

    override fun onDurationAdded(duration: String) {
        draft.duration=duration
        PrefManager.putDraftTask(draft)

        binding.taskDurationET.text = duration
    }

    override fun onSectionSelected(sectionName: String) {
        binding.section.text = sectionName
        draft.section=sectionName
        PrefManager.putDraftTask(draft)

    }

    override fun stringtext(text: String,type:String) {
        when(type){
            "PRIORITY" -> {
                val numPriority=SwitchFunctions.getNumPriorityFromStringPriority(text)
                draft.priority = numPriority
                PrefManager.putDraftTask(draft)

                binding.priorityInclude.tagIcon.text=text.substring(0,1)
                binding.priorityInclude.tagText.text=text
            }
            "STATE" ->  {
                val numState=SwitchFunctions.getNumStateFromStringState(text)
                draft.status = numState
                PrefManager.putDraftTask(draft)

                binding.stateInclude.tagIcon.text=text.substring(0,1)
                binding.stateInclude.tagText.text=text
            }
            "TYPE" -> {
                val numType=SwitchFunctions.getNumTypeFromStringType(text)
                draft.type=numType
                PrefManager.putDraftTask(draft)

                binding.typeInclude.tagIcon.text=text.substring(0,1)
                binding.typeInclude.tagText.text=text
            }
            "DIFFICULTY" -> {
                val numDifficulty=SwitchFunctions.getNumDifficultyFromStringDifficulty(text)
                draft.difficulty=numDifficulty
                PrefManager.putDraftTask(draft)

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

    val script = """
    var allPreTags = document.querySelectorAll('pre');

    allPreTags.forEach(function(preTag) {
      preTag.addEventListener('click', function() {
        var clickedText = preTag.textContent;
        var languageType = preTag.getAttribute('language');
        send.sendCode(clickedText, languageType);
       
      });
    });
    
    var allImgTags = document.querySelectorAll('img');
    var imgArray = [];

    allImgTags.forEach(function(imgTag) {
        if (imgTag.tagName.toLowerCase() === 'img' && imgTag.parentElement.tagName.toLowerCase() !== 'pre') {
        imgTag.addEventListener('click', function() {
            send.sendsingleImage(imgTag.src);
        });
        imgArray.push(imgTag.src);
    }
    });

    send.sendImages(imgArray);

"""
    private fun setUpTaskDescription(description: String) {
        binding.textView2.gone()
        binding.EditSummary.visible()
        val css: InternalStyleSheet = Github()

        with(css) {
            addFontFace(
                "o2font",
                "normal",
                "normal",
                "normal",
                "url('file:///android_res/font/sfregular.ttf')"
            )
            addRule("body", "font-family:o2font")
            addRule("body", "font-size:16px")
            addRule("body", "line-height:21px")
            addRule("body", "background-color: #E3E1E1")
            addRule("body", "color: #222222")
            addRule("body", "padding: 0px 0px 0px 0px")
            addRule("a", "color: #0000FF")
            addRule("pre", "border: 1px solid #000;")
            addRule("pre", "border-radius: 4px;")
            addRule("pre", "max-height: 400px;")
            addRule("pre", "overflow:auto")
            addRule("pre", "white-space: pre-line")

        }

        binding.markdownView.settings.javaScriptEnabled = true
        binding.markdownView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        binding.markdownView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)

        binding.markdownView.addStyleSheet(css)
        binding.markdownView.addJavascriptInterface(AndroidToJsInterface(), "send")

        binding.markdownView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.evaluateJavascript(script) {}
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val intent = Intent(Intent.ACTION_VIEW, request?.url)
                startActivity(intent)
                return true
            }

        }

        binding.markdownView.loadMarkdown(description)
        binding.markdownView.visible()

    }
    inner class AndroidToJsInterface {
        @JavascriptInterface
        fun sendCode(codeText: String, language: String?) {
            this@CreateTaskActivity.runOnUiThread {

                val codeViewerIntent =
                    Intent(this@CreateTaskActivity, CodeViewerActivity::class.java)
                codeViewerIntent.putExtra(
                    Endpoints.CodeViewer.CODE,
                    codeText.trimIndent().trim()
                )
                codeViewerIntent.putExtra(
                    Endpoints.CodeViewer.LANG,
                    language?.trimIndent()?.trim()
                )

                startActivity(codeViewerIntent)
                this@CreateTaskActivity.overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_left
                )
            }
        }

        @JavascriptInterface
        fun sendImages(imageUrls: Array<String>) {
            this@CreateTaskActivity.runOnUiThread {
                val recyclerView = binding.imageRecyclerView
                recyclerView.layoutManager =
                    LinearLayoutManager(
                        this@CreateTaskActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                Log.d("list", imageUrls.toMutableList().toString())
                val adapter =
                    ImageAdapter(imageUrls.toMutableList(), this@CreateTaskActivity)
                recyclerView.adapter = adapter
            }
        }

        @JavascriptInterface
        fun sendsingleImage(imageUrl: String) {
            this@CreateTaskActivity.runOnUiThread {
                onImageClicked(0, mutableListOf(imageUrl))
            }
        }

    }

    override fun updateAssignee(assignee: User) {
    }

    override fun sendcheckListarray(list: MutableList<CheckList>) {
        checkListArray.clear()
        checkListArray.addAll(list)
        PrefManager.putDraftCheckLists(list)
        Log.d("checkListArray",checkListArray.toString())
        if (list.size>0) {
            binding.checkListCount.visible()
            binding.checkListCount.text = "(${(list.size).toString()})"
        }else{
            binding.checkListCount.gone()
        }
    }
    override fun onImageClicked(position: Int, imageList: MutableList<String>) {
        val imageViewerIntent =
            Intent(this, ImageViewerActivity::class.java)
        imageViewerIntent.putExtra("position", position)
        imageViewerIntent.putStringArrayListExtra("images", ArrayList(imageList))
        startActivity(
            ImageViewerActivity.createIntent(
                this,
                ArrayList(imageList),
                position,
            ),
        )

    }
    private fun composeNotification(type: NotificationType, message: String, assignee: String,taskID:String): Notification? {

        if (type == NotificationType.TASK_ASSIGNED_NOTIFICATION) {
            return Notification(
                notificationID = RandomIDGenerator.generateRandomTaskId(6),
                notificationType = NotificationType.TASK_ASSIGNED_NOTIFICATION.name,
                taskID = taskID,
                message = message,
                title = "You are assigned in the task $taskID",
                fromUser = PrefManager.getcurrentUserdetails().EMAIL,
                toUser = assignee ,
                timeStamp = Timestamp.now().seconds,
                projectID = PrefManager.getcurrentProject(),

                )
        }
        return null
    }
    private fun sendNotification(receiverList: MutableList<String>, notification: Notification) {

        try {
            CoroutineScope(Dispatchers.IO).launch {
                for (receiverToken in receiverList) {
                    NotificationsUtils.sendFCMNotification(
                        receiverToken,
                        notification = notification
                    )
                }

            }

        } catch (exception: Exception) {
            Timber.tag("")
            utils.showSnackbar(binding.root, "Failure in sending notifications", 5000)
        }

    }
    private fun fetchUserDetails(assigneeId: String, onUserFetched: (User) -> Unit) {
        if (assigneeId!="None" && assigneeId!="") {
            repository.getUserInfobyId(assigneeId) { result ->
                when (result) {
                    is ServerResult.Success -> {
                        val user = result.data
                        if (user != null) {
                            onUserFetched(user)
                        }
                    }

                    is ServerResult.Failure -> {

                    }

                    is ServerResult.Progress -> {

                    }
                }
            }
        }else{
            onUserFetched(User(
                firebaseID = null,
                profileDPUrl = null,
                profileIDUrl = null,
                post = null,
                username = null,
                role = null,
                timestamp = null,
                designation = null,
                fcmToken = null,
                isChecked = false
            ))
        }
    }
}