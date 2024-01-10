package com.ncs.o2.UI.Setting

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ncs.o2.Domain.Interfaces.AuthRepository
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.AuthScreenActivity
import com.ncs.o2.UI.EditProfile.EditProfileActivity
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.NewChanges
import com.ncs.o2.databinding.ActivitySettingsBinding
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class SettingsActivity : AppCompatActivity(), settingAdater.onSettingClick {

    private lateinit var binding:ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth
    @Inject
    lateinit var util : GlobalUtils.EasyElements
    private val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        setUpViews()
        setUpRecyclerView()
        setContentView(binding.root)
    }

    private fun setUpRecyclerView(){

        val items = listOf(
            settingTitle("profile"),
            settingOption("Edit Profile", R.drawable.round_edit_24, ""),
            settingTitle("what's new"),
            settingOption("What's New", R.drawable.baseline_info_24, "Version 1.1.0"),
            settingTitle("Account"),
            settingOption("Log Out", R.drawable.logout, "")
        )


        val recyclerView = binding.settingRecycler
        val layoutManager= LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val adapter = settingAdater(items, this@SettingsActivity)
        recyclerView.adapter = adapter
    }
    private fun setUpViews(){
        binding.gioActionbar.titleTv.text = "Settings"
        binding.gioActionbar.btnFav.gone()
        binding.gioActionbar.btnRequestWork.gone()

        binding.gioActionbar.btnBack.setOnClickListener {
            onBackPressed()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    override fun onClick(position: Int) {
        if (Codes.STRINGS.clickedSetting == "Edit Profile"){
            startActivity(Intent(this,EditProfileActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }
        else if (Codes.STRINGS.clickedSetting == "What's New"){
            startActivity(Intent(this, NewChanges::class.java))
            overridePendingTransition(R.anim.faster_slide_bottom_to_up, R.anim.faster_slide_bottom_to_up)
        }
        else if (Codes.STRINGS.clickedSetting == "Log Out"){
            try {

                util.dialog("Log out","You really want to log out?", "Yes","No",
                    {
                        auth.signOut()
                        deleteCache(this)
                        val intent = Intent(this, AuthScreenActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                        toast("Logged out..")
                },{

                })

            } catch (e: Exception) {
                Timber.tag(TAG).e("onClick: Exception + ${e}")
            }
        }
    }

    private fun deleteCache(context: Context) {
        try {
            val dir: File = context.cacheDir
            if (!deleteDir(dir)){
                util.singleBtnDialog("Failed to logout","Error in deleting the caches.., clear app data from settings manually","Okay"){}
                Timber.d(TAG, "Problem in clearing cache..")
            }else{
                Timber.i(TAG, "Cache is cleared..")
            }
        } catch (e: java.lang.Exception) {
            Timber.e(TAG, "Problem in clearing cache.., $e")
            util.singleBtnDialog("Failed to logout, clear data from app settings manually",e.cause.toString(),"Okay"){}
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

}