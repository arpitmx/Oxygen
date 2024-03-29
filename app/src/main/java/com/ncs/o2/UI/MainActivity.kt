package com.ncs.o2.UI

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.ncs.o2.Data.Room.NotificationRepository.NotificationDatabase
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.Enums.MessageType
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.state.SegmentItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.set180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.Issue
import com.ncs.o2.HelperClasses.Navigator
import com.ncs.o2.HelperClasses.NetworkChangeReceiver
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.AuthScreenActivity
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.UI.EditProfile.EditProfileActivity
import com.ncs.o2.UI.Notifications.NotificationsActivity
import com.ncs.o2.UI.Report.ShakeDetectedActivity
import com.ncs.o2.UI.Setting.SettingsActivity
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.SharedViewModel
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.Adapters.ProjectCallback
import com.ncs.o2.UI.UIComponents.Adapters.RecyclerViewAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.AddProjectBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.AddQuickTaskBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.MoreProjectOptionsBottomSheet
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ProjectCallback,AddProjectBottomSheet.ProjectAddedListener,NetworkChangeReceiver.NetworkChangeCallback{
    lateinit var projectListAdapter: RecyclerViewAdapter
    private var projects: MutableList<String> = mutableListOf()
    lateinit var bottmNav: BottomNavigationView
    private var dynamicLinkHandled = false
    private var doubleBackPress = false
    private val networkChangeReceiver = NetworkChangeReceiver(this,this)
    val sharedViewModel: SharedViewModel by viewModels()
    var index:String?=null
    private val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1

    private lateinit var sensorManager: SensorManager

    private lateinit var shakeDetector: ShakeDetector
    // ViewModels
    private val viewModel: MainActivityViewModel by viewModels()

    // Views and data
    private lateinit var search: LinearLayout
    val segmentText = MutableLiveData<String>()

    // Utils
    private val easyElements: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    lateinit var navController: NavController
    private val viewModel2: TaskSectionViewModel by viewModels()


    @Inject
    lateinit var db:TasksDatabase
    @Inject
    lateinit var notifdb:NotificationDatabase

    // Navigation drawer toggle
    private lateinit var toggle: ActionBarDrawerToggle
    private val intentFilter by lazy{
        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }


    // Data binding
    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // Dependency Injection
    @Inject
    lateinit var navigator: Navigator

    var isProjectListVisible=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        projects=ArrayList()
        registerReceiver(true)

        if (PrefManager.getAppMode()==Endpoints.OFFLINE_MODE){
            binding.gioActionbar.offlineIndicator.visible()
        }
        else{
            binding.gioActionbar.offlineIndicator.gone()
        }
        binding.gioActionbar.offlineIndicator.setOnClickThrottleBounceListener {
            easyElements.twoBtnDialog("Offline Mode Active", msg = "As network is not available, offline mode is active, things may not be in sync with server","Check Network","Cancel",{
                networkChangeReceiver.retryNetworkCheck()
            },{})
        }

        if (FirebaseAuth.getInstance().currentUser!=null) {
            if (savedInstanceState == null) {
                handleDynamicLink(intent)
                dynamicLinkHandled = true
            }
        }
        else{

            FirebaseAuth.getInstance().signOut()
            deleteCache(this)
            val intent = Intent(this, AuthScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
            Toast.makeText(this, "Please log in first", Toast.LENGTH_LONG).show()

        }
        // Hide keyboard at startup
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(binding.root)
        PrefManager.initialize(this)
        setUpInitilisations()

        for (project in PrefManager.getProjectsList()){
            Log.d("projectCheck",PrefManager.getProjectIconUrl(project).toString())
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNav = binding.bottomNav
        bottomNav.setupWithNavController(navController)

        val search=intent.getStringExtra("search")
        if (search!=null && search=="GoToSearch") {
            val tagText = intent.getStringExtra("tagText")
            val userName = intent.getStringExtra("userName")

            if (tagText!=null){
                val bundle = bundleOf("tagID" to tagText)
                navController.navigate(R.id.bottom_search,bundle)
            }
            if (userName!=null){
                val bundle = bundleOf("userName" to userName)
                navController.navigate(R.id.bottom_search,bundle)
            }


        }
        binding.gioActionbar.refresh.setOnClickThrottleBounceListener {
            if (PrefManager.getAppMode()==Endpoints.ONLINE_MODE){
                startActivity(Intent(this,CreateTaskActivity::class.java))
            }
            else{
                easyElements.showSnackbar(binding.root,"Task Creation is not available in offline mode",3000)
            }
        }

        manageNoProject()

    }

    private fun manageNoProject(){
        if (PrefManager.getcurrentProject()=="None"){
            binding.projectPlaceholder.visible()
            binding.navHostFragmentActivityMain.gone()
            binding.bottomNavParent.gone()
            binding.gioActionbar.teamsSearch.gone()
            binding.gioActionbar.notifications.gone()
            binding.gioActionbar.btnMoreTeams.gone()
            binding.gioActionbar.searchCont.gone()
            PrefManager.setRadioButton(-1)
        }
        else{
            binding.navHostFragmentActivityMain.visible()
            binding.bottomNavParent.visible()
            binding.gioActionbar.teamsSearch.visible()
            binding.gioActionbar.notifications.visible()
            binding.gioActionbar.btnMoreTeams.visible()
            binding.projectPlaceholder.gone()

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

    private fun setUpInitilisations(){


        manageViews()
        setUpViews()

        viewModel.currentSegment.observe(this, Observer { newSegment ->
            updateUIBasedOnSegment(newSegment)
        })


        for (project in PrefManager.getProjectsList()){
            Log.d("projectSegments",PrefManager.getProjectSegments(project).toString())
        }
    }

    private fun manageViews(){
        setNotificationCountOnActionBar()
        binding.gioActionbar.tabLayout.gone()
//        movetoteamspage()
    }

    private fun updateUIBasedOnSegment(newSegment: String) {
        setNotificationCountOnActionBar()
        binding.gioActionbar.tabLayout.gone()
//        movetoteamspage()
    }



    private fun setUpViews() {

        // Set up various views and components
        setUpProjects()
        setUpViewsOnClicks()
        setupProjectsList()
        setBottomNavBar()
        setUpActionBar()

    }

    fun setBottomNavBar() {

        bottmNav = binding.bottomNav

        if (PrefManager.getAppMode()==Endpoints.ONLINE_MODE){
            bottmNav.setOnItemSelectedListener { menuItem ->

                when (menuItem.itemId) {

                    R.id.assigned_item -> {
                        navController.navigate(R.id.assigned_item)
                        true
                    }

                    R.id.bottom_search -> {
                        navController.navigate(R.id.bottom_search)
                        true
                    }

                    R.id.teams_page -> {
                        navController.navigate(R.id.teams_page)
                        true
                    }

                    R.id.task_item -> {
                        navController.navigate(R.id.task_item)
                        true
                    }
                    else ->{
                        startActivity(Intent(this,CreateTaskActivity::class.java))
                        false
                    }
                }

            }
        }else{
            bottmNav.setOnItemSelectedListener { menuItem ->

                when (menuItem.itemId) {

                    R.id.assigned_item -> {
                        easyElements.showSnackbar(binding.root,"Workspace is not available in offline mode",3000)
                        false
                    }

                    R.id.bottom_search -> {
                        navController.navigate(R.id.bottom_search)
                        true
                    }

                    R.id.teams_page -> {
                        navController.navigate(R.id.teams_page)
                        true
                    }

                    R.id.task_item -> {
                        navController.navigate(R.id.task_item)
                        true
                    }
                    else ->{
                        easyElements.showSnackbar(binding.root,"Task Creation is not available in offline mode",3000)
                        false
                    }
                }
            }
        }


    }

    private fun setUpViewsOnClicks() {

        // Set up click listeners for specific UI elements
        binding.gioActionbar.createTaskButton.setOnClickThrottleBounceListener {
            navigator.startSingleTopActivity(CreateTaskActivity::class.java)
        }

        binding.drawerheaderfile.yourProjects.setOnClickListener {
            binding.drawerheaderfile.arrow.set180(this)
            if (isProjectListVisible){
                isProjectListVisible=false
                binding.drawerheaderfile.extendedProjectsList.gone()
            }
            else{
                isProjectListVisible=true
                binding.drawerheaderfile.extendedProjectsList.visible()
            }

        }
    }

    private var receiverRegistered = false

    fun registerReceiver(flag : Boolean){
        if (flag){
            if (!receiverRegistered) {
                registerReceiver(networkChangeReceiver,intentFilter)
                receiverRegistered = true
            }
        }else{
            if (receiverRegistered){
                unregisterReceiver(networkChangeReceiver)
                receiverRegistered = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        manageNoProject()
        if (PrefManager.getShakePref()){
            initShake()
            shakeDetector.registerListener()
        }
//        else{
//            shakeDetector.unregisterListener()
//        }
        registerReceiver(true)
        setNotificationCountOnActionBar()
        setUpInitilisations()
    }


    private fun setUpActionBar() {
        binding.gioActionbar.actionbar.visible()
        binding.gioActionbar.btnMore.visible()
        binding.gioActionbar.btnQuickTask.visible()

        // Set up the action bar, navigation drawer, and other UI components

        setNotificationCountOnActionBar()


        val drawerLayout = binding.drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.gioActionbar.titleTv.animFadein(this, 500)
        binding.gioActionbar.titleTv.text = PrefManager.getcurrentsegment()

        binding.gioActionbar.btnMore.setOnClickThrottleBounceListener {
            val moreProjetcOptionBottomSheet =
                MoreProjectOptionsBottomSheet()
            moreProjetcOptionBottomSheet.show(supportFragmentManager, "more")
        }

        binding.gioActionbar.btnQuickTask.setOnClickThrottleBounceListener {
            if (PrefManager.getAppMode()==Endpoints.ONLINE_MODE){
                val quickTaskBottomSheet =
                    AddQuickTaskBottomSheet(Message(messageId = "", messageType = MessageType.NORMAL_MSG, timestamp = Timestamp.now(), senderId = "", additionalData = emptyMap(), content = ""))
                quickTaskBottomSheet.show(supportFragmentManager, "quickTask")
            }else {

                easyElements.showSnackbar(
                    binding.root,
                    "Task Creation is not available in offline mode",
                    3000
                )
            }
        }


        binding.gioActionbar.btnHam.setOnClickThrottleBounceListener {

            // Toggle the navigation drawer
            val gravity = if (!drawerLayout.isDrawerOpen(GravityCompat.START)) GravityCompat.START else GravityCompat.END
            drawerLayout.openDrawer(gravity)

        }


        binding.gioActionbar.notifications.setOnClickThrottleBounceListener {

            // Start the notifications activity with a slide animation
            startActivity(Intent(this,NotificationsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)

        }

        binding.gioActionbar.newChangesBtn.setOnClickThrottleBounceListener {
            navigator.startSingleTopActivity(NewChanges::class.java)
            overridePendingTransition(R.anim.faster_slide_bottom_to_up, R.anim.faster_slide_bottom_to_up)
        }

        // Add project button click listener
        val add_button: AppCompatButton = drawerLayout.findViewById(R.id.add_project_btn)
        add_button.setOnClickListener {
            if (PrefManager.getAppMode()==Endpoints.ONLINE_MODE){
                drawerLayout.closeDrawer(GravityCompat.START)
                val project = AddProjectBottomSheet(this)
                project.show(supportFragmentManager, "Add Project")
            }else{
                easyElements.showSnackbar(binding.root,"Projects can't be added in offline mode",2000)
            }
        }


        //Setting button click listener
        val setting_btn: AppCompatButton = drawerLayout.findViewById(R.id.setting_btn)
        setting_btn.setOnClickListener {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)        }

        // setting up Edit Profile
        binding.drawerheaderfile.ibEditProfile.setOnClickListener {

            if (PrefManager.getAppMode()==Endpoints.ONLINE_MODE) {

                val intent = Intent(this@MainActivity, EditProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            else{
                easyElements.showSnackbar(binding.root,"Profile edit not available",2000)

            }

        }

    }


    private fun setNotificationCountOnActionBar() {
        CoroutineScope(Dispatchers.IO).launch {
            val notifications=notifdb.notificationDao().getAllNotificationsForProject(PrefManager.getcurrentProject())
            var count=0
            for(notification in notifications){
                if (notification.timeStamp>PrefManager.getProjectTimeStamp(PrefManager.getcurrentProject())){
                    count++
                }
            }
            withContext(Dispatchers.Main){
                if (count>0){
                    binding.gioActionbar.notificationCountET.text = count.toString()
                    binding.gioActionbar.notificationCountET.visible()
                }else {
                    binding.gioActionbar.notificationCountET.gone()
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle options item clicks, including navigation drawer toggle
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpProjects() {

        viewModel.fetchUserProjectsFromRepository()
        val user=PrefManager.getcurrentUserdetails()
        binding.drawerheaderfile.username.text=user.USERNAME
        binding.drawerheaderfile.designation.text=user.DESIGNATION
        binding.drawerheaderfile.email.text=user.EMAIL

        Glide.with(this)
            .load(PrefManager.getDpUrl())
            .placeholder(R.drawable.profile_pic_placeholder)
            .error(R.drawable.profile_pic_placeholder)
            .override(200,200)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
            .into(binding.drawerheaderfile.userDp)


//        viewModel.showDialogLD.observe(this) { data ->
//            easyElements.singleBtnDialog(data[0], data[1],"OK",{})
//        }

    }


    fun getVersionName(context: Context): String? {
        return try {
            val packageInfo: PackageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }


    @Issue("Project name can be None, make sure projects cannot be created with these keywords")
    private fun setupProjectsList(){

        manageNoProject()

        //Version tag setup
        binding.drawerheaderfile.versionCode.text = "Oxygen v${getVersionName(this)}"

        projects=PrefManager.getProjectsList().toMutableList()
        val project_list=ArrayList<String>()
        project_list.addAll(projects)

        if (project_list.contains("None")){
            project_list.remove("None")
        }

        val recyclerView=binding.drawerheaderfile.projectRecyclerView
        if (project_list.isEmpty()){
            recyclerView.gone()
            binding.drawerheaderfile.projectCountTv.text = "0"
            binding.drawerheaderfile.projectPlaceholder.visible()
        }

        else{

            recyclerView.visible()
            binding.drawerheaderfile.projectPlaceholder.gone()
            projectListAdapter = RecyclerViewAdapter(this,project_list)
            val linearLayoutManager = LinearLayoutManager(this)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = linearLayoutManager
            recyclerView.adapter = projectListAdapter

            binding.drawerheaderfile.projectCountTv.text = project_list.size.toString()

        }

    }


    @Issue("Project name can be None, make sure projects cannot be created with these keywords")
    override fun onClick(projectID: String, position: Int) {

        // Handle click on a project in the list
        PrefManager.setcurrentsegment("Select Segment")
        viewModel2.updateCurrentSegment("Select Segment")
        navController.navigate(R.id.teams_page)
        binding.gioActionbar.titleTv.text=PrefManager.getcurrentsegment()
        PrefManager.setcurrentProject(projectID)
        PrefManager.setRadioButton(position)
        PrefManager.selectedPosition.value = position

        CoroutineScope(Dispatchers.Main).launch {
            val projectTopic = projectID.replace("\\s+".toRegex(), "_") + "_TOPIC_GENERAL"
            FirebaseMessaging.getInstance().subscribeToTopic(projectTopic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM", "Subscribed to topic successfully")
                    } else {
                        Log.d("FCM", "Failed to subscribe to topic")
                    }
                }
            setUpTasks(projectID)
            setUpTags(projectName = projectID)

        }
        manageNoProject()
    }





    private fun setUpTasks(projectName: String) {
        val dao = db.tasksDao()
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait, Syncing Tasks")
        progressDialog.setCancelable(false)
        progressDialog.show()

        if (PrefManager.getLastTaskTimeStamp(projectName).seconds.toInt()==0) {
            CoroutineScope(Dispatchers.Main).launch {
                try {

                    val taskResult = withContext(Dispatchers.IO) {
                        viewModel.getTasksinProject(projectName)
                    }

                    when (taskResult) {

                        is ServerResult.Failure -> {
                            progressDialog.dismiss()
                            val drawerLayout = binding.drawer
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }

                        is ServerResult.Progress -> {
                            progressDialog.show()
                            progressDialog.setMessage("Please wait, Syncing Tasks")

                        }

                        is ServerResult.Success -> {
                            val tasks = taskResult.data
                            val newList=taskResult.data.toMutableList().sortedByDescending { it.last_updated }
                            PrefManager.setLastTaskTimeStamp(projectName,newList[0].last_updated!!)
                            for (task in tasks) {
                                dao.insert(task)
                            }
                            progressDialog.dismiss()

                            val drawerLayout = binding.drawer
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }

                    }

                } catch (e: java.lang.Exception) {
                    Timber.tag(TaskDetailsFragment.TAG).e(e)

                    progressDialog.dismiss()

                }

            }
        }
        else{
            CoroutineScope(Dispatchers.Main).launch {
                try {

                    val taskResult = withContext(Dispatchers.IO) {
                        viewModel.getTasksinProjectAccordingtoTimeStamp(projectName)
                    }

                    Timber.tag(TaskDetailsFragment.TAG).d("Fetched task result : ${taskResult}")

                    when (taskResult) {

                        is ServerResult.Failure -> {
                            progressDialog.dismiss()
                            val drawerLayout = binding.drawer
                            drawerLayout.closeDrawer(GravityCompat.START)
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
                                    dao.insert(task)
                                }
                            }
                            progressDialog.dismiss()

                            val drawerLayout = binding.drawer
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }

                    }

                } catch (e: java.lang.Exception) {
                    Timber.tag(TaskDetailsFragment.TAG).e(e)
                    progressDialog.dismiss()


                }

            }
        }
    }
    private fun setUpTags(projectName: String) {
        val dao = db.tagsDao()
        if (PrefManager.getLastTagTimeStamp(projectName).seconds.toInt()==0) {

            CoroutineScope(Dispatchers.Main).launch {
                try {

                    val tagResult = withContext(Dispatchers.IO) {
                        viewModel.getTagsinProject(projectName)
                    }

                    Timber.tag(TaskDetailsFragment.TAG).d("Fetched Tag result : ${tagResult}")

                    when (tagResult) {

                        is ServerResult.Failure -> {
                        }

                        is ServerResult.Progress -> {
                        }

                        is ServerResult.Success -> {

                            val tags = tagResult.data
                            val newList=tagResult.data.toMutableList().sortedByDescending { it.last_tag_updated }
                            PrefManager.setLastTagTimeStamp(projectName,newList[0].last_tag_updated!!)
                            for (tag in tags) {
                                dao.insert(tag)
                            }
                        }
                    }

                } catch (e: java.lang.Exception) {

                    Timber.tag(TaskDetailsFragment.TAG).e(e)

                }

            }
        }
        else{
            Timber.tag(TaskDetailsFragment.TAG).d("for $projectName ${PrefManager.getLastTagTimeStamp(projectName)}")
            CoroutineScope(Dispatchers.Main).launch {
                try {

                    val tagResult = withContext(Dispatchers.IO) {
                        viewModel.getTagsinProjectAccordingtoTimeStamp(projectName)
                    }

                    Timber.tag(TaskDetailsFragment.TAG).d("Fetched Tag result : ${tagResult}")

                    when (tagResult) {

                        is ServerResult.Failure -> {
                        }

                        is ServerResult.Progress -> {
                        }

                        is ServerResult.Success -> {

                            val tags = tagResult.data
                            CoroutineScope(Dispatchers.IO).launch {
                                if (tags.isNotEmpty()) {
                                    val newList = tagResult.data.toMutableList()
                                        .sortedByDescending { it.last_tag_updated }
                                    PrefManager.setLastTagTimeStamp(
                                        projectName,
                                        newList[0].last_tag_updated!!
                                    )
                                    for (tag in tags) {
                                        dao.insert(tag)
                                    }
                                }

                            }


                        }
                    }

                } catch (e: java.lang.Exception) {

                    Timber.tag(TaskDetailsFragment.TAG).e(e)


                }

            }
        }
    }

    override fun onProjectAdded(userProjects: ArrayList<String>) {
        manageNoProject()
        Log.d("projectCheck",PrefManager.getProjectsList().toString())
        PrefManager.putProjectsList(userProjects)
        Log.d("projectCheck",PrefManager.getProjectsList().toString())
        setupProjectsList()
        if (this::projectListAdapter.isInitialized) {
            projectListAdapter.notifyDataSetChanged()
        }
        else{
            projectListAdapter = RecyclerViewAdapter(this,PrefManager.getProjectsList())
            projectListAdapter.notifyDataSetChanged()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (!dynamicLinkHandled) {
            handleDynamicLink(intent)
            dynamicLinkHandled = true
        }
    }

    private fun handleDynamicLink(intent: Intent?) {
        val dynamicLinkTask = FirebaseDynamicLinks.getInstance().getDynamicLink(intent!!)
        dynamicLinkTask.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val dynamicLink = task.result
                if (dynamicLink != null) {
                    handleDeepLink(dynamicLink.link!!)
                }
            } else {
                Log.e("DynamicLink", "Error getting dynamic link", task.exception)
            }
        }
    }


    private fun handleDeepLink(uri: Uri) {
        Log.d("shareLinkTest",uri.toString())
        val pathSegments = uri.pathSegments
        Log.d("shareLinkTest",pathSegments.toString())

        if (pathSegments.size >= 2) {
            val operationType = pathSegments[0]
            val id = pathSegments[1]
            var project:String=""
            var projectPrefix:String=""
            if (pathSegments.size>=3){
                project=pathSegments[2]
            }
            if (pathSegments.size>=4){
                projectPrefix=pathSegments[3]
            }
            Log.d("shareLinkTest",operationType.toString())
            Log.d("shareLinkTest",id.toString())

            when(operationType){
                "join" -> {
                    if (PrefManager.getProjectsList().any { it.equals(id, ignoreCase = true) }) {
                        easyElements.showSnackbar(binding.root,"You have already joined this project",2000)
                    }else{
                        lifecycleScope.launch {
                            val isValidLink = isValidProjectLink(uri.toString())
                            if (isValidLink) {
                                showBottomSheetForJoinConfirmation(uri.toString(),id)
                            } else {
                                easyElements.showSnackbar(binding.root,"Invalid project link",2000)
                            }
                        }
                    }
                }
                "share" -> {
                    if (pathSegments.size==3){
                        val taskId="#T${id}"
                        CoroutineScope(Dispatchers.Main).launch {
                            if (PrefManager.getProjectsList().contains(project)) {
                                if (db.tasksDao().getTasksbyId(taskId, project)?.isNull!!) {
                                    easyElements.showSnackbar(
                                        binding.root,
                                        "No Task found, check the link",
                                        2000
                                    )
                                } else {
                                    val segments=PrefManager.getUnArchivedProjectSegments(project)
                                    Log.d("segment check",segments.toString())

                                    if (segments.isNotEmpty()){
                                        PrefManager.setcurrentsegment(segments[0].segment_NAME)
                                        PrefManager.putsectionsList(segments[0].sections.distinct())
                                        viewModel.updateCurrentSegment(segments[0].segment_NAME)

                                    }
                                    else{
                                        PrefManager.setcurrentsegment("Select Segment")
                                        viewModel.updateCurrentSegment("Select Segment")
                                    }
                                    binding.gioActionbar.titleTv.text = PrefManager.getcurrentsegment()
                                    val list = PrefManager.getProjectsList()
                                    var position:Int=0
                                    for (i in 0 until list.size){
                                        if (list[i]==project){
                                            position=i
                                        }
                                    }
                                    Log.d("position",position.toString())

                                    if (this@MainActivity::projectListAdapter.isInitialized) {
                                        PrefManager.setcurrentProject(project)
                                        PrefManager.setRadioButton(position)
                                        PrefManager.selectedPosition.value = position
                                        projectListAdapter.notifyDataSetChanged()
                                    }
                                    else{
                                        projectListAdapter = RecyclerViewAdapter(this@MainActivity,PrefManager.getProjectsList())
                                        projectListAdapter.notifyDataSetChanged()
                                    }
                                    setupProjectsList()
                                    val intent =
                                        Intent(this@MainActivity, TaskDetailActivity::class.java)
                                    intent.putExtra("task_id", taskId)
                                    intent.putExtra("type", "shareTask")

                                    startActivity(intent)
                                    finish()
                                    overridePendingTransition(
                                        R.anim.slide_in_left,
                                        R.anim.slide_out_left
                                    )
                                }
                            }
                            else{
                                easyElements.showSnackbar(
                                    binding.root,
                                    "Can't view task you are not enrolled in this project",
                                    2000
                                )
                            }
                        }
                    }
                    if (pathSegments.size==4){
                        val taskId="#${projectPrefix}-${id}"
                        Log.d("shareLinkTest",taskId.toString())
                        CoroutineScope(Dispatchers.Main).launch {
                            if (PrefManager.getProjectsList().contains(project)) {
                                if (db.tasksDao().getTasksbyId(taskId, project)?.isNull!!) {
                                    easyElements.showSnackbar(
                                        binding.root,
                                        "No Task found, check the link",
                                        2000
                                    )
                                } else {
                                    val segments=PrefManager.getUnArchivedProjectSegments(project)
                                    Log.d("segment check",segments.toString())

                                    if (segments.isNotEmpty()){
                                        PrefManager.setcurrentsegment(segments[0].segment_NAME)
                                        PrefManager.putsectionsList(segments[0].sections.distinct())
                                        viewModel.updateCurrentSegment(segments[0].segment_NAME)
                                    }
                                    else{
                                        PrefManager.setcurrentsegment("Select Segment")
                                        viewModel.updateCurrentSegment("Select Segment")
                                    }
                                    binding.gioActionbar.titleTv.text = PrefManager.getcurrentsegment()
                                    val list = PrefManager.getProjectsList()
                                    var position:Int=0
                                    for (i in 0 until list.size){
                                        if (list[i]==project){
                                            position=i
                                        }
                                    }
                                    Log.d("position",position.toString())

                                    if (this@MainActivity::projectListAdapter.isInitialized) {
                                        PrefManager.setcurrentProject(project)
                                        PrefManager.setRadioButton(position)
                                        PrefManager.selectedPosition.value = position
                                        projectListAdapter.notifyDataSetChanged()
                                    }
                                    else{
                                        projectListAdapter = RecyclerViewAdapter(this@MainActivity,PrefManager.getProjectsList())
                                        projectListAdapter.notifyDataSetChanged()
                                    }
                                    setupProjectsList()
                                    val intent =
                                        Intent(this@MainActivity, TaskDetailActivity::class.java)
                                    intent.putExtra("task_id", taskId)
                                    intent.putExtra("type", "shareTask")

                                    startActivity(intent)
                                    finish()
                                    overridePendingTransition(
                                        R.anim.slide_in_left,
                                        R.anim.slide_out_left
                                    )
                                }
                            }
                            else{
                                easyElements.showSnackbar(
                                    binding.root,
                                    "Can't view task you are not enrolled in this project",
                                    2000
                                )
                            }
                        }
                    }
                }
            }

        }
    }
    private suspend fun isValidProjectLink(projectLink: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = FirebaseFirestore.getInstance().collection("Projects")
                    .whereEqualTo("PROJECT_DEEPLINK", projectLink.toLowerCase())
                    .limit(1)
                    .get()
                    .await()

                return@withContext querySnapshot.documents.isNotEmpty()
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }

    private fun showBottomSheetForJoinConfirmation(projectLink: String,id:String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.deeplink_project_add_sheet, null)

        view.findViewById<Button>(R.id.btnYes).setOnClickThrottleBounceListener {
            addProjectToUser(projectLink)
            bottomSheetDialog.dismiss()
        }

        view.findViewById<TextView>(R.id.projectName).text=id
        view.findViewById<Button>(R.id.btnNo).setOnClickThrottleBounceListener {
            bottomSheetDialog.dismiss()
        }
        view.findViewById<AppCompatImageButton>(R.id.close_btn).setOnClickThrottleBounceListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun addProjectToUser(projectLink: String) {
        CoroutineScope(Dispatchers.Main).launch {
            when (val result = firestoreRepository.addProjectToUser(projectLink)) {
                is ServerResult.Success -> {
                    projects.clear()
                    projects.addAll(result.data)
                    Timber.tag("result").d(result.data.toString())
                    PrefManager.putProjectsList(result.data)
                    if (this@MainActivity::projectListAdapter.isInitialized) {
                        projectListAdapter.notifyDataSetChanged()
                    }
                    else{
                        projectListAdapter = RecyclerViewAdapter(this@MainActivity,PrefManager.getProjectsList())
                        projectListAdapter.notifyDataSetChanged()
                    }
                    setupProjectsList()
                    CoroutineScope(Dispatchers.IO).launch {
                        val list = getProjectSegments(PrefManager.getlastaddedproject())
                        val projectTopic = PrefManager.getlastaddedproject().replace("\\s+".toRegex(), "_") + "_TOPIC_GENERAL"

                        FirebaseMessaging.getInstance().subscribeToTopic(projectTopic)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("FCM", "Subscribed to topic successfully")
                                } else {
                                    Log.d("FCM", "Failed to subscribe to topic")
                                }
                            }
                        val newList=list.toMutableList().sortedByDescending { it.creation_DATETIME }
                        PrefManager.saveProjectSegments(PrefManager.getlastaddedproject(), list)
                        withContext(Dispatchers.Main){
                            easyElements.showSnackbar(binding.root,"Successfully joined this project",2000)
                        }
                        if (newList.isNotEmpty()){
                            PrefManager.setLastSegmentsTimeStamp(PrefManager.getlastaddedproject(),newList[0].creation_DATETIME!!)
                        }

                    }
                }
                is ServerResult.Failure -> {
                    Toast.makeText(this@MainActivity, "Failed to add project: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun deleteCache(context: Context) {
        try {
            val dir: File = context.cacheDir
            if (!deleteDir(dir)){
                easyElements.singleBtnDialog("Failed to logout","Error in deleting the caches.., clear app data from settings manually","Okay"){}
                Timber.d("TAG", "Problem in clearing cache..")
            }else{
                Timber.i("TAG", "Cache is cleared..")
            }
        } catch (e: java.lang.Exception) {
            Timber.e("TAG", "Problem in clearing cache.., $e")
            easyElements.singleBtnDialog("Failed to logout, clear data from app settings manually",e.cause.toString(),"Okay"){}
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children: Array<String>? = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }


    override fun onBackPressed() {
        if (doubleBackPress) {
            super.onBackPressed()
            return
        }

        this.doubleBackPress = true
        easyElements.showSnackbar(binding.root,"Press back again to exit",2000)
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackPress = false
        }, 2000)
    }

    suspend fun getProjectSegments(project: String): List<SegmentItem> {
        val projectsCollection =  FirebaseFirestore.getInstance().collection(Endpoints.PROJECTS)
        val list = mutableListOf<SegmentItem>()

        try {
            val projectsSnapshot = projectsCollection.get().await()
            for (projectDocument in projectsSnapshot.documents) {
                val projectName = projectDocument.id
                val segmentsCollection = projectsCollection.document(project).collection(Endpoints.Project.SEGMENT)
                val segmentsSnapshot = segmentsCollection.get().await()
                for (segmentDocument in segmentsSnapshot.documents) {
                    val segmentName = segmentDocument.id
                    val sections=segmentDocument.get("sections") as MutableList<String>
                    val segment_ID= segmentDocument.getString("segment_ID")
                    val creation_DATETIME= segmentDocument.get("creation_DATETIME") as Timestamp
                    val archived= if (segmentDocument.getBoolean("archived" ).isNull) false else segmentDocument.getBoolean("archived" )
                    val last_updated=  if (segmentDocument.get("last_updated").isNull) Timestamp.now() else segmentDocument.get("last_updated") as Timestamp

                    list.add(SegmentItem(segment_NAME = segmentName, sections = sections, segment_ID = segment_ID!!, creation_DATETIME = creation_DATETIME!!, archived = archived!!,last_updated = last_updated))
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return list
    }


    override fun onStart() {
        super.onStart()
        registerReceiver(true)
    }

    override fun onStop() {
        super.onStop()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onPause() {
        super.onPause()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onOnlineModePositiveSelected() {
        PrefManager.setAppMode(Endpoints.ONLINE_MODE)
        easyElements.restartApp()
    }

    override fun onOfflineModePositiveSelected() {
        startActivity(intent)
        PrefManager.setAppMode(Endpoints.OFFLINE_MODE)
    }

    override fun onOfflineModeNegativeSelected() {
        networkChangeReceiver.retryNetworkCheck()
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
