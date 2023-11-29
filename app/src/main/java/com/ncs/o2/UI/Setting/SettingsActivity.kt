package com.ncs.o2.UI.Setting

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
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.AuthScreenActivity
import com.ncs.o2.UI.EditProfile.EditProfileActivity
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.NewChanges
import com.ncs.o2.databinding.ActivitySettingsBinding
import timber.log.Timber

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
        binding.gioActionbar.titleTv.text = "Setting"
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
                startActivity(Intent(this, AuthScreenActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                Toast.makeText(applicationContext, "LogOut", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Timber.tag("MainHostActivity").d("onClick: Exception + ${e.message}")
            }
        }
    }
}