package com.ncs.o2.UI.Report

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Teams.Chat.TeamsChatFragment
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.ActivityShakeDetectedBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date


@AndroidEntryPoint
class ShakeDetectedActivity : AppCompatActivity() {
    val binding: ActivityShakeDetectedBinding by lazy {
        ActivityShakeDetectedBinding.inflate(layoutInflater)
    }

    private val easyElements: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }
    var fileName:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        manageViews()
        val type=intent.getStringExtra("type")
        fileName=intent.getStringExtra("filename")

        if (type=="settings"){
            shakePreferences()
        }
        if (type=="report"){
            reportingFragment()
        }

    }

    private fun manageViews(){
        binding.gioActionbar.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.gioActionbar.titleTv.text = "Shake to Report"
        binding.gioActionbar.btnClose.gone()
        binding.gioActionbar.btnBack.visible()
        binding.gioActionbar.btnFav.gone()
        binding.gioActionbar.btnRequestWork.gone()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
        finish()
    }

    private fun shakePreferences(){
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = ShakePrefrencesFragment()
        transaction.replace(R.id.shake_detected_fragment_container, fragment)
        transaction.commit()
    }

    private fun reportingFragment(){
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = ReportingFragment()
        transaction.replace(R.id.shake_detected_fragment_container, fragment)
        transaction.commit()
    }


}