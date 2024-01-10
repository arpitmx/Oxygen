package com.ncs.o2.UI

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.Navigator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Assigned.AssignedFragment
import com.ncs.o2.UI.Auth.AuthScreenActivity
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.UI.EditProfile.EditProfileActivity
import com.ncs.o2.UI.Notifications.NotificationsActivity
import com.ncs.o2.UI.SearchScreen.SearchFragment
import com.ncs.o2.UI.Setting.SettingsActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Tasks.TasksHolderFragment
import com.ncs.o2.UI.UIComponents.Adapters.ListAdapter
import com.ncs.o2.UI.UIComponents.Adapters.ProjectCallback
import com.ncs.o2.UI.UIComponents.BottomSheets.AddProjectBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.MoreProjectOptionsBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ProjectCallback, SegmentSelectionBottomSheet.SegmentSelectionListener,AddProjectBottomSheet.ProjectAddedListener  {
    lateinit var projectListAdapter: ListAdapter
    private var projects: MutableList<String> = mutableListOf()
    lateinit var bottmNav: BottomNavigationView
    private var dynamicLinkHandled = false
    private var doubleBackPress = false

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


    @Inject
    lateinit var db:TasksDatabase

    // Navigation drawer toggle
    private lateinit var toggle: ActionBarDrawerToggle

    // Data binding
    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // Dependency Injection
    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        projects=ArrayList()

        if (FirebaseAuth.getInstance().currentUser!=null) {
            if (savedInstanceState == null) {
                handleDynamicLink(intent)
                dynamicLinkHandled = true
            }
        }
        else{

            FirebaseAuth.getInstance().signOut()
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
        manageViews()
        setUpViews()

        viewModel.currentSegment.observe(this, Observer { newSegment ->
            updateUIBasedOnSegment(newSegment)
        })
        val searchFragment = intent.getStringExtra("search")
        if (searchFragment != null) {
            when (searchFragment) {
                "GoToSearch" -> movetosearch(intent.getStringExtra("tagText"))
            }
        }
    }
    private fun manageViews(){
        if (PrefManager.getcurrentsegment()== "Select Segment") {
            binding.placeholderText.visible()
            binding.navHostFragmentActivityMain.gone()
            binding.gioActionbar.tabLayout.gone()
            binding.gioActionbar.searchCont.gone()
            binding.gioActionbar.line.gone()
            binding.bottomNavParent.gone()
            binding.gioActionbar.tabLayout.gone()
            binding.gioActionbar.searchCont.gone()
            binding.gioActionbar.actionbar.visible()
            binding.gioActionbar.constraintLayout2.visible()
            binding.gioActionbar.constraintLayoutsearch.gone()
            binding.gioActionbar.constraintLayoutworkspace.gone()
        } else {
            binding.placeholderText.gone()
            binding.navHostFragmentActivityMain.visible()
            binding.gioActionbar.tabLayout.visible()
            binding.gioActionbar.searchCont.visible()
            binding.gioActionbar.line.visible()
            binding.bottomNavParent.visible()
            binding.gioActionbar.tabLayout.visible()
            binding.gioActionbar.searchCont.visible()
            binding.gioActionbar.actionbar.visible()
            binding.gioActionbar.constraintLayout2.visible()
            binding.gioActionbar.constraintLayoutsearch.gone()
            binding.gioActionbar.constraintLayoutworkspace.gone()
        }
    }

    private fun updateUIBasedOnSegment(newSegment: String) {
        if (newSegment == "Select Segment") {
            binding.placeholderText.visible()
            binding.navHostFragmentActivityMain.gone()
            binding.gioActionbar.tabLayout.gone()
            binding.gioActionbar.searchCont.gone()
            binding.gioActionbar.line.gone()
            binding.bottomNavParent.gone()
            binding.gioActionbar.tabLayout.gone()
            binding.gioActionbar.searchCont.gone()
            binding.gioActionbar.actionbar.visible()
            binding.gioActionbar.constraintLayout2.visible()
            binding.gioActionbar.constraintLayoutsearch.gone()
            binding.gioActionbar.constraintLayoutworkspace.gone()
        } else {
            binding.placeholderText.gone()
            binding.navHostFragmentActivityMain.visible()
            binding.gioActionbar.tabLayout.visible()
            binding.gioActionbar.searchCont.visible()
            binding.gioActionbar.line.visible()
            binding.bottomNavParent.visible()
            binding.gioActionbar.tabLayout.visible()
            binding.gioActionbar.searchCont.visible()
            binding.gioActionbar.actionbar.visible()
            binding.gioActionbar.constraintLayout2.visible()
            binding.gioActionbar.constraintLayoutsearch.gone()
            binding.gioActionbar.constraintLayoutworkspace.gone()
        }
    }



    private fun setUpViews() {

        // Set up various views and components
        setUpProjects()
        setUpActionBar()
        setBottomNavBar()
        setUpViewsOnClicks()
        setupProjectsList()

    }

    private fun setUpViewsOnClicks() {

        // Set up click listeners for specific UI elements
        binding.gioActionbar.createTaskButton.setOnClickThrottleBounceListener {
            navigator.startSingleTopActivity(CreateTaskActivity::class.java)
        }

    }


    override fun onResume() {
        super.onResume()
        setNotificationCountOnActionBar()
    }


    private fun setUpActionBar() {
        binding.gioActionbar.actionbar.visible()
        binding.gioActionbar.btnMore.visible()

        // Set up the action bar, navigation drawer, and other UI components

        search = binding.gioActionbar.searchCont
        setNotificationCountOnActionBar()


        val drawerLayout = binding.drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.gioActionbar.titleTv.animFadein(this, 500)
        binding.gioActionbar.titleTv.text = PrefManager.getcurrentsegment()

        binding.gioActionbar.btnMore.setOnClickListener {
            val moreProjetcOptionBottomSheet =
                MoreProjectOptionsBottomSheet()
            moreProjetcOptionBottomSheet.show(supportFragmentManager, "more")
        }

        binding.gioActionbar.segmentParent.setOnClickThrottleBounceListener {

            val segment = SegmentSelectionBottomSheet()
            segment.segmentSelectionListener = this

            // Show a segment selection bottom sheet
            this.performHapticFeedback()
            segment.show(supportFragmentManager, "Segment Selection")
            binding.gioActionbar.switchSegmentButton.rotate180(this)

        }

        binding.gioActionbar.btnHam.setOnClickThrottleBounceListener {

            // Toggle the navigation drawer
            val gravity = if (!drawerLayout.isDrawerOpen(GravityCompat.START)) GravityCompat.START else GravityCompat.END
            drawerLayout.openDrawer(gravity)

        }


        binding.gioActionbar.notifications.setOnClickThrottleBounceListener {

            // Start the notifications activity with a slide animation
            navigator.startSingleTopActivity(NotificationsActivity::class.java)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)

        }

        binding.gioActionbar.newChangesBtn.setOnClickThrottleBounceListener {
            navigator.startSingleTopActivity(NewChanges::class.java)
            overridePendingTransition(R.anim.faster_slide_bottom_to_up, R.anim.faster_slide_bottom_to_up)
        }

        // Add project button click listener
        val add_button: AppCompatButton = drawerLayout.findViewById(R.id.add_project_btn)
        add_button.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val project = AddProjectBottomSheet()
            project.projectAddedListener = this
            project.show(supportFragmentManager, "Add Project")
        }

        //Setting button click listener
        val setting_btn: AppCompatButton = drawerLayout.findViewById(R.id.setting_btn)
        setting_btn.setOnClickListener {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)        }

        // setting up Edit Profile
        binding.drawerheaderfile.ibEditProfile.setOnClickListener {

            val intent = Intent(this@MainActivity, EditProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            drawerLayout.closeDrawer(GravityCompat.START)

        }
        binding.gioActionbar.searchBar.setOnClickThrottleBounceListener {
            replaceFragment(SearchFragment())
            bottmNav.selectedItemId = R.id.bottom_search
        }
    }

    private fun setNotificationCountOnActionBar() {
        val notificationCount = PrefManager.getNotificationCount()
        if (notificationCount>0){

            binding.gioActionbar.notificationCountET.text = notificationCount.toString()
            binding.gioActionbar.notificationCountET.visible()

        }else {
            binding.gioActionbar.notificationCountET.gone()
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

        // Set up the list of projects and related UI components
//        binding.lottieProgressInclude.progressbarStrip.visible()
//        binding.lottieProgressInclude.progressbarBlock.gone()

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


        viewModel.showDialogLD.observe(this) { data ->
            easyElements.singleBtnDialog(data[0], data[1],"OK",{})
        }

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
    private fun setupProjectsList(){


        //Version tag setup
        binding.drawerheaderfile.versionCode.text = "Oxygen v${getVersionName(this)} alpha"

        projects=PrefManager.getProjectsList().toMutableList()
        val list=ArrayList<String>()
        list.addAll(projects)
        projectListAdapter=ListAdapter(this,list)
        binding.drawerheaderfile.projectlistView.adapter=projectListAdapter
    }

    override fun onClick(projectID: String, position: Int) {

        // Handle click on a project in the list
        Toast.makeText(this, "Clicked $projectID", Toast.LENGTH_SHORT).show()
        PrefManager.setcurrentsegment("Select Segment")
        viewModel.updateCurrentSegment("Select Segment")

        binding.gioActionbar.titleTv.text=PrefManager.getcurrentsegment()
        PrefManager.setcurrentProject(projectID)
        PrefManager.setRadioButton(position)
        PrefManager.selectedPosition.value = position
        val drawerLayout = binding.drawer
        drawerLayout.closeDrawer(GravityCompat.START)

    }



    override fun onSegmentSelected(segmentName: String) {

        // Handle segment selection
        binding.gioActionbar.titleTv.text = segmentName
        segmentText.value = segmentName
        viewModel.updateCurrentSegment(segmentName)
        if (segmentName=="Select Segment"){
            binding.gioActionbar.tabLayout.gone()
            binding.gioActionbar.searchCont.gone()
        }
        else{
            movetotaskspage()
        }

    }



    override fun onProjectAdded(userProjects: ArrayList<String>) {
        projects.clear()
        projects.addAll(userProjects)
        projectListAdapter.notifyDataSetChanged()
    }

    fun setBottomNavBar() {

        bottmNav = binding.bottomNav
        bottmNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.assigned_item -> {
                    replaceFragment(AssignedFragment())
                    true
                }

                R.id.bottom_search -> {
                    replaceFragment(SearchFragment())
                    true
                }

                R.id.task_item -> {
                    replaceFragment(TasksHolderFragment())
                    true
                }

                else ->{
                    startActivity(Intent(this,CreateTaskActivity::class.java))
                    false
                }
            }


        }

    }
    private fun replaceFragment(fragment: Fragment) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, fragment)
        fragmentTransaction.commit()

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
            if (pathSegments.size>=3){
                project=pathSegments[2]
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
                                PrefManager.setcurrentsegment("Select Segment")
                                viewModel.updateCurrentSegment("Select Segment")
                                binding.gioActionbar.titleTv.text = PrefManager.getcurrentsegment()
                                val list = PrefManager.getProjectsList()
                                var position:Int=0
                                for (i in 0 until list.size){
                                    if (list[i]==project){
                                        position=i
                                    }
                                }
                                Log.d("position",position.toString())
                                PrefManager.setcurrentProject(project)
                                PrefManager.setRadioButton(position)
                                PrefManager.selectedPosition.value = position
                                projectListAdapter.notifyDataSetChanged()
                                setupProjectsList()
                                val intent =
                                    Intent(this@MainActivity, TaskDetailActivity::class.java)
                                intent.putExtra("task_id", taskId)
                                startActivity(intent)
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
        GlobalScope.launch(Dispatchers.Main) {
            val result = firestoreRepository.addProjectToUser(projectLink)
            when (result) {
                is ServerResult.Success -> {
                    projects.clear()
                    projects.addAll(result.data)
                    Log.d("result",result.data.toString())
                    PrefManager.putProjectsList(result.data)
                    projectListAdapter.notifyDataSetChanged()
                    setupProjectsList()
                    easyElements.showSnackbar(binding.root,"Successfully joined this project",2000)
                }
                is ServerResult.Failure -> {
                    Toast.makeText(this@MainActivity, "Failed to add project: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }
    private fun movetosearch(tagText: String?) {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = SearchFragment()
        val bundle = Bundle()
        bundle.putString("tagText", tagText)
        fragment.arguments = bundle

        transaction.replace(R.id.nav_host_fragment_activity_main, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        binding.bottomNav.menu.getItem(2).isChecked = true
        binding.bottomNav.menu.getItem(2).setIcon(R.drawable.ic_searchico)
    }
    private fun movetotaskspage() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = TasksHolderFragment()
        transaction.replace(R.id.nav_host_fragment_activity_main, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        binding.bottomNav.menu.getItem(0).isChecked = true
        binding.bottomNav.menu.getItem(0).setIcon(R.drawable.baseline_article_24)
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
}
