package com.ncs.o2.UI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.protobuf.Internal
import com.ncs.o2.ProjectCallback
import com.ncs.o2.R
import com.ncs.o2.Utility.ExtensionsUtil.animFadeOut
import com.ncs.o2.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Utility.ExtensionsUtil.animSlideLeft
import com.ncs.o2.Utility.ExtensionsUtil.animSlideUp
import com.ncs.o2.Utility.ExtensionsUtil.gone
import com.ncs.o2.Utility.ExtensionsUtil.progressGone
import com.ncs.o2.Utility.ExtensionsUtil.progressVisible
import com.ncs.o2.Utility.ExtensionsUtil.toast
import com.ncs.o2.Utility.ExtensionsUtil.visible
import com.ncs.o2.Utility.GlobalUtils
import com.ncs.o2.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ProjectCallback {

    val viewModel : MainActivityViewModel by viewModels()
    val easyElements : GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }
    lateinit var toggle:ActionBarDrawerToggle
    val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpViews()
    }

    private fun setUpViews() {
        setUpProjects()
        setUpActionBar()

    }

    private fun setUpActionBar() {


        val drawerLayout = binding.drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.gioActionbar.titleTv.animFadein(this,500)
        binding.gioActionbar.btnHam.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)){
                drawerLayout.openDrawer(GravityCompat.START)
                Toast.makeText(this, "Opened", Toast.LENGTH_SHORT).show()
            }
            else drawerLayout.closeDrawer(GravityCompat.END)
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

      viewModel.showprogressLD.observe(this){ show->
       if (show) {
           binding.progressbar.progressVisible(this,600)
       }
       else {
           binding.progressbar.progressGone(this, 400)
       }
      }

      viewModel.showDialogLD.observe(this){data ->
         easyElements.dialog(data[0],data[1],{},{})
      }

      viewModel.projectListLiveData.observe(this){projectList ->

          val projectListAdapter = com.ncs.o2.ListAdapter(this,projectList!!)
          binding.drawerheaderfile.projectlistView.adapter = projectListAdapter
      }
    }

    override fun onClick(projectID: String) {
        Toast.makeText(this, "Clicked $projectID", Toast.LENGTH_SHORT).show()
    }

}