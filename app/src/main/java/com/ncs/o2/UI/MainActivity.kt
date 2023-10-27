package com.ncs.o2.UI

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.lifecycle.MutableLiveData
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressGone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressVisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.Later
import com.ncs.o2.HelperClasses.Navigator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.UI.Notifications.NotificationsActivity
import com.ncs.o2.UI.Tasks.Sections.TaskSectionFragment
import com.ncs.o2.UI.Tasks.Sections.TaskSectionViewModel
import com.ncs.o2.UI.Tasks.TasksHolderFragment
import com.ncs.o2.UI.Tasks.TasksHolderViewModel
import com.ncs.o2.UI.UIComponents.Adapters.ListAdapter
import com.ncs.o2.UI.UIComponents.Adapters.ProjectCallback
import com.ncs.o2.UI.UIComponents.BottomSheets.AddProjectBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.FieldPosition
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ProjectCallback,SegmentSelectionBottomSheet.SegmentSelectionListener{
    private val viewmodel: TaskSectionViewModel by viewModels()
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var search:LinearLayout
    val segmentText = MutableLiveData<String>()

    private val easyElements: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }
    private lateinit var toggle: ActionBarDrawerToggle
    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpViews()
    }

    @Later("1. Select a default segment if no segment was selected , or select the previously chosed one")
    private fun setUpViews() {

        setUpProjects()
        setUpActionBar()
        setUpViewsOnClicks()
    }

    private fun setUpViewsOnClicks() {
        binding.gioActionbar.createTaskButton.setOnClickThrottleBounceListener {
            navigator.startSingleTopActivity(CreateTaskActivity::class.java)
        }
    }


    private fun setUpActionBar() {
        PrefManager.initialize(this)
        search=binding.gioActionbar.searchCont
        binding.gioActionbar.tabLayout.height


        Handler(Looper.getMainLooper()).postDelayed({
            binding.gioActionbar.createTaskButton.rotate180(this)
        }, 1000)

        val drawerLayout = binding.drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.gioActionbar.titleTv.animFadein(this, 500)
        binding.gioActionbar.titleTv.text=PrefManager.getcurrentsegment()

        binding.gioActionbar.segmentParent.setOnClickThrottleBounceListener {
            val segment = SegmentSelectionBottomSheet()
            segment.segmentSelectionListener = this
            segment.show(supportFragmentManager, "Segment Selection")
            binding.gioActionbar.switchSegmentButton.rotate180(this)
        }

        binding.gioActionbar.btnHam.setOnClickThrottleBounceListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else drawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.gioActionbar.notifications.setOnClickThrottleBounceListener {
                navigator.startSingleTopActivity(NotificationsActivity::class.java)
                this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left)
        }
        val add_button: AppCompatButton = drawerLayout.findViewById(R.id.add_project_btn)
        add_button.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val project = AddProjectBottomSheet()
            project.show(supportFragmentManager, " Add Project ")

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpProjects() {

        binding.lottieProgressInclude.progressbarStrip.visible()
        binding.lottieProgressInclude.progressbarBlock.gone()

        viewModel.fetchUserProjectsFromRepository()

        viewModel.showprogressLD.observe(this) { show ->
            if (show) {
                binding.lottieProgressInclude.progressLayout.progressVisible(this, 600)
            } else {
                binding.lottieProgressInclude.progressLayout.progressGone(this, 400)
            }
        }

        viewModel.showDialogLD.observe(this) { data ->
            easyElements.dialog(data[0], data[1], {}, {})
        }

        viewModel.projectListLiveData.observe(this) { projectList ->

            val projectListAdapter = ListAdapter(this, projectList!!)
            binding.drawerheaderfile.projectlistView.adapter = projectListAdapter
        }
    }

    override fun onClick(projectID: String,position: Int) {
        Toast.makeText(this, "Clicked $projectID", Toast.LENGTH_SHORT).show()
        PrefManager.initialize(this)
        PrefManager.setcurrentProject(projectID)
        PrefManager.setRadioButton(position)
        PrefManager.selectedPosition.value = position
        val drawerLayout = binding.drawer
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun refreshActivity() {
        finish()
        startActivity(intent)
    }
    override fun onSegmentSelected(segmentName: String) {
        binding.gioActionbar.titleTv.text=segmentName
        segmentText.value=segmentName
    }

}