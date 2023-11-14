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
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressGone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressVisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.Navigator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.UI.Notifications.NotificationsActivity
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.UIComponents.Adapters.ListAdapter
import com.ncs.o2.UI.UIComponents.Adapters.ProjectCallback
import com.ncs.o2.UI.UIComponents.BottomSheets.AddProjectBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.UI.EditProfile.EditProfileActivity
import com.ncs.o2.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ProjectCallback, SegmentSelectionBottomSheet.SegmentSelectionListener,AddProjectBottomSheet.ProjectAddedListener  {
    private lateinit var projectListAdapter: ListAdapter
    private var projects: MutableList<String> = mutableListOf()

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

    // Dependency Injection
    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide keyboard at startup
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        PrefManager.initialize(this)
        setContentView(binding.root)

        PrefManager.initialize(this)
        setUpViews()
    }

    private fun setUpViews() {

        // Set up various views and components
        setUpProjects()
        setUpActionBar()
        setUpViewsOnClicks()
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

            // Show a segment selection bottom sheet
            val segment = SegmentSelectionBottomSheet()
            segment.segmentSelectionListener = this
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

        // Add project button click listener
        val add_button: AppCompatButton = drawerLayout.findViewById(R.id.add_project_btn)
        add_button.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val project = AddProjectBottomSheet()
            project.projectAddedListener = this
            project.show(supportFragmentManager, "Add Project")
        }

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
        binding.lottieProgressInclude.progressbarStrip.visible()
        binding.lottieProgressInclude.progressbarBlock.gone()

        viewModel.fetchUserProjectsFromRepository()
        val user=PrefManager.getcurrentUserdetails()
        binding.drawerheaderfile.username.text=user.USERNAME
        binding.drawerheaderfile.designation.text=user.DESIGNATION
        binding.drawerheaderfile.email.text=user.EMAIL

        Glide.with(this)
            .load(PrefManager.getDpUrl())
            .placeholder(R.drawable.profile_pic_placeholder)
            .error(R.drawable.logogradhd)
            .override(200,200)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .into(binding.drawerheaderfile.userDp)

        viewModel.showprogressLD.observe(this) { show ->
            if (show) {
                binding.lottieProgressInclude.progressLayout.progressVisible(this, 600)
            } else {
                binding.lottieProgressInclude.progressLayout.progressGone(this, 400)
            }
        }

        viewModel.showDialogLD.observe(this) { data ->
            easyElements.singleBtnDialog(data[0], data[1],"OK",{})
        }

        viewModel.projectListLiveData.observe(this) { projectList ->

            projects=projectList!!.toMutableList()
            projectListAdapter = ListAdapter(this, projects)
            binding.drawerheaderfile.projectlistView.adapter = projectListAdapter
        }
    }

    override fun onClick(projectID: String, position: Int) {

        // Handle click on a project in the list
        Toast.makeText(this, "Clicked $projectID", Toast.LENGTH_SHORT).show()
        PrefManager.initialize(this)
        PrefManager.setcurrentsegment("Select Segment")
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

    }



    override fun onProjectAdded(userProjects: ArrayList<String>) {
        projects.clear()
        projects.addAll(userProjects)
        projectListAdapter.notifyDataSetChanged()
    }

}
