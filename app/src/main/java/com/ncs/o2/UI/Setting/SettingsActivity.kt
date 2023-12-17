package com.ncs.o2.UI.Setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.AuthScreenActivity
import com.ncs.o2.UI.EditProfile.EditProfileActivity
import com.ncs.o2.UI.NewChanges
import com.ncs.o2.databinding.ActivitySettingsBinding
import timber.log.Timber
import java.io.File

class SettingsActivity : AppCompatActivity(), settingAdater.onSettingClick {

    private lateinit var binding:ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth
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
                auth.signOut()
                deleteCache(this)

                val intent = Intent(this, AuthScreenActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                Toast.makeText(applicationContext, "LogOut", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Timber.tag("MainHostActivity").d("onClick: Exception + ${e.message}")
            }
        }
    }

    fun deleteCache(context: Context) {
        try {
            val dir: File = context.getCacheDir()
            deleteDir(dir)
        } catch (e: java.lang.Exception) {
        }
    }

    fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory()) {
            val children: Array<String> = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile()) {
            dir.delete()
        } else {
            false
        }
    }
}