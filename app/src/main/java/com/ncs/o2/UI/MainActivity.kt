package com.ncs.o2.UI

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ncs.o2.HelperClasses.Navigator
import com.ncs.o2.UI.UIComponents.Adapters.ProjectCallback
import com.ncs.o2.R
import com.ncs.o2.UI.CreateTask.CreateTaskActivity
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressGone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.progressVisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotate180
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.Later
import com.ncs.o2.UI.UIComponents.Adapters.ListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.SegmentSelectionBottomSheet
import com.ncs.o2.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ProjectCallback {

    private val viewModel: MainActivityViewModel by viewModels()
    private val easyElements: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }
    private lateinit var toggle: ActionBarDrawerToggle
    private val binding: ActivityMainBinding by lazy {
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
        binding.gioActionbar.createTaskButton.setOnClickThrottleBounceListener{
           navigator.startSingleTopActivity(CreateTaskActivity::class.java)
        }
    }


    private fun setUpActionBar() {

        Handler(Looper.getMainLooper()).postDelayed({
            binding.gioActionbar.createTaskButton.rotate180(this)
        },1000)

        val drawerLayout = binding.drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.gioActionbar.titleTv.animFadein(this, 500)

        binding.gioActionbar.segmentParent.setOnClickThrottleBounceListener {
           val segment = SegmentSelectionBottomSheet()
            segment.show(supportFragmentManager,"Segment Selection")
            binding.gioActionbar.switchSegmentButton.rotate180(this)
        }

        binding.gioActionbar.btnHam.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
                Toast.makeText(this, "Opened", Toast.LENGTH_SHORT).show()
            } else drawerLayout.closeDrawer(GravityCompat.END)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpProjects() {
        viewModel.fetchUserProjectsFromRepository()

        viewModel.showprogressLD.observe(this) { show ->
            if (show) {
                binding.progressbar.progressVisible(this, 600)
            } else {
                binding.progressbar.progressGone(this, 400)
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

    override fun onClick(projectID: String) {
        Toast.makeText(this, "Clicked $projectID", Toast.LENGTH_SHORT).show()
    }

}