package com.ncs.o2.UI

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ncs.o2.Domain.Delegates.firestoreDelegate
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.Navigator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Assigned.AssignedFragment
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.UI.DoneScreen.DoneFragment
import com.ncs.o2.UI.Notifications.NotificationsActivity
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.UIComponents.Adapters.ListAdapter
import com.ncs.o2.UI.UIComponents.Adapters.ProjectCallback
import com.ncs.o2.UI.UIComponents.BottomSheets.AddProjectBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.UI.EditProfile.EditProfileActivity
import com.ncs.o2.UI.Setting.SettingsActivity
import com.ncs.o2.UI.Tasks.TasksHolderFragment
import com.ncs.o2.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ProjectCallback, SegmentSelectionBottomSheet.SegmentSelectionListener,AddProjectBottomSheet.ProjectAddedListener  {
    private lateinit var projectListAdapter: ListAdapter
    private var projects: MutableList<String> = mutableListOf()
    lateinit var bottmNav: BottomNavigationView

    // ViewModels
    private val viewmodel: TaskSectionViewModel by viewModels()
    private val viewModel: MainActivityViewModel by viewModels()

    // Views and data
    private lateinit var search: LinearLayout
    val segmentText = MutableLiveData<String>()

    // Utils
    private val easyElements: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }

    // Navigation drawer toggle
    private lateinit var toggle: ActionBarDrawerToggle

    // Data binding
    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    //Delegation
    var userRole : Int by firestoreDelegate("slowfast@hackncs.in", this)

    // Dependency Injection
    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide keyboard at startup
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        setContentView(binding.root)
        manageViews()
        PrefManager.initialize(this)
        setUpViews()





        viewModel.currentSegment.observe(this, Observer { newSegment ->
            updateUIBasedOnSegment(newSegment)
        })
    }
    private fun manageViews(){
        if (PrefManager.getcurrentsegment()== "Select Segment") {
            binding.placeholderText.visible()
            binding.navHostFragmentActivityMain.gone()
            binding.gioActionbar.tabLayout.gone()
            binding.gioActionbar.searchCont.gone()
            binding.gioActionbar.line.gone()
        } else {
            binding.placeholderText.gone()
            binding.navHostFragmentActivityMain.visible()
            binding.gioActionbar.tabLayout.visible()
            binding.gioActionbar.searchCont.visible()
            binding.gioActionbar.line.visible()
        }
    }

    private fun updateUIBasedOnSegment(newSegment: String) {
        if (newSegment == "Select Segment") {
            binding.placeholderText.visible()
            binding.navHostFragmentActivityMain.gone()
            binding.gioActionbar.tabLayout.gone()
            binding.gioActionbar.searchCont.gone()
            binding.gioActionbar.line.gone()
        } else {
            binding.placeholderText.gone()
            binding.navHostFragmentActivityMain.visible()
            binding.gioActionbar.tabLayout.visible()
            binding.gioActionbar.searchCont.visible()
            binding.gioActionbar.line.visible()
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

//    private fun makeFullScreen() {
//        // Hide the status bar
//        window.decorView.systemUiVisibility = (
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        or View.SYSTEM_UI_FLAG_FULLSCREEN
//                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                )
//    }


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

        // Set up the action bar, navigation drawer, and other UI components

        search = binding.gioActionbar.searchCont
        setNotificationCountOnActionBar()

        // Rotate animation for CreateTaskButton
        Handler(Looper.getMainLooper()).postDelayed({
            binding.gioActionbar.createTaskButton.rotate180(this)
        }, 1000)


        val drawerLayout = binding.drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.gioActionbar.titleTv.animFadein(this, 500)
        binding.gioActionbar.titleTv.text = PrefManager.getcurrentsegment()



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

//        viewModel.showprogressLD.observe(this) { show ->
//            if (show) {
//                binding.lottieProgressInclude.progressLayout.progressVisible(this, 600)
//            } else {
//                binding.lottieProgressInclude.progressLayout.progressGone(this, 400)
//            }
//        }

        viewModel.showDialogLD.observe(this) { data ->
            easyElements.singleBtnDialog(data[0], data[1],"OK",{})
        }

//        viewModel.projectListLiveData.observe(this) { projectList ->
//
//            projects=projectList!!.toMutableList()
//            projectListAdapter = ListAdapter(this, projects)
//            binding.drawerheaderfile.projectlistView.adapter = projectListAdapter
//        }




    }
    private fun setupProjectsList(){
        projects=PrefManager.getProjectsList().toMutableList()
        projectListAdapter=ListAdapter(this,projects)
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

    private fun refreshActivity() {

        // Refresh the activity by recreating it
        startActivity(intent)
        finish()

    }

    override fun onSegmentSelected(segmentName: String) {

        // Handle segment selection
        binding.gioActionbar.titleTv.text = segmentName
        segmentText.value = segmentName
        viewModel.updateCurrentSegment(segmentName)


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

                R.id.project_stats_item -> {
                    replaceFragment(DoneFragment())
                    true
                }

                else -> {
                    replaceFragment(TasksHolderFragment())
                    true
                }
            }


        }

    }
    private fun replaceFragment(fragment: Fragment) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, fragment)
        fragmentTransaction.commit()

    }

}
