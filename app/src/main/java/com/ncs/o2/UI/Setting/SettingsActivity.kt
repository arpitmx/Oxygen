package com.ncs.o2.UI.Setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.NetworkChangeReceiver
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.AuthScreenActivity
import com.ncs.o2.UI.EditProfile.EditProfileActivity
import com.ncs.o2.UI.Logs.LogsActivity
import com.ncs.o2.UI.NewChanges
import com.ncs.o2.UI.Report.ShakeDetectedActivity
import com.ncs.o2.UI.Teams.TeamsActivity
import com.ncs.o2.databinding.ActivitySettingsBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(), settingAdater.onSettingClick,NetworkChangeReceiver.NetworkChangeCallback {

    private lateinit var binding:ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth
    private val util: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this@SettingsActivity)
    }
    private val TAG = "SettingsActivity"
    private val networkChangeReceiver = NetworkChangeReceiver(this,this)
    private val  intentFilter by lazy {
        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }
    private lateinit var shakeDetector: ShakeDetector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        registerReceiver(true)
        setUpViews()
        setUpRecyclerView()
        setContentView(binding.root)
    }

    private fun setUpRecyclerView(){

        val items = listOf(

            settingTitle("profile"),
            settingOption("Edit Profile", R.drawable.round_edit_24, ""),

            settingTitle("what's new"),
            settingOption("What's New", R.drawable.baseline_info_24, "Version 24.1.24"),

            settingTitle("Report & Feedback"),
            settingOption("Feedback", R.drawable.baseline_feedback_24, ""),
            settingOption("Shake to Report", R.drawable.baseline_screen_rotation_24, ""),


            settingTitle("Logs"),
            settingOption("Logs", R.drawable.baseline_assistant_24, ""),

            settingTitle("Account"),
            settingOption("Log Out", R.drawable.logout, "") ,
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
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
        if (PrefManager.getShakePref()){
            initShake()
            shakeDetector.registerListener()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    override fun onClick(position: Int) {
        if (Codes.STRINGS.clickedSetting == "Edit Profile"){
            if (PrefManager.getAppMode()== Endpoints.ONLINE_MODE) {
                startActivity(Intent(this, EditProfileActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            }
            else{
                util.showSnackbar(binding.root,"Profile edit not available",2000)
            }
        }
        else if (Codes.STRINGS.clickedSetting == "What's New"){
            startActivity(Intent(this, NewChanges::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }
        else if (Codes.STRINGS.clickedSetting == "Logs"){
            startActivity(Intent(this, LogsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
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
        else if(Codes.STRINGS.clickedSetting == "Shake to Report"){
            val intent = Intent(this, ShakeDetectedActivity::class.java)
            intent.putExtra("type", "settings")
            startActivity(intent)
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }
        else if(Codes.STRINGS.clickedSetting == "Feedback"){
            val intent = Intent(this, ShakeDetectedActivity::class.java)
            intent.putExtra("type", "report")
            startActivity(intent)
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
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
    override fun onPause() {
        super.onPause()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(true)
    }

    override fun onStop() {
        super.onStop()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onOnlineModePositiveSelected() {
        PrefManager.setAppMode(Endpoints.ONLINE_MODE)
        util.restartApp()
    }


    private var receiverRegistered = false

    fun registerReceiver(flag : Boolean){
        if (flag){
            if (!receiverRegistered) {
                registerReceiver(networkChangeReceiver,intentFilter)
                receiverRegistered = true
            }
        }else{
            if (receiverRegistered){
                unregisterReceiver(networkChangeReceiver)
                receiverRegistered = false
            }
        }
    }

    override fun onOfflineModePositiveSelected() {
        startActivity(intent)
        PrefManager.setAppMode(Endpoints.OFFLINE_MODE)
    }

    override fun onOfflineModeNegativeSelected() {
        networkChangeReceiver.retryNetworkCheck()
    }


    private fun initShake(){
        val shakePref=PrefManager.getShakePref()
        Log.d("shakePref",shakePref.toString())
        if (shakePref){

            val sensi=PrefManager.getShakeSensitivity()
            when(sensi){
                1->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultLightSensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
                2->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultMediumSensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
                3->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultHeavySensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
            }
        }
    }

    fun takeScreenshot(activity: Activity) {
        Log.e("takeScreenshot", activity.localClassName)
        val rootView = activity.window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = rootView.drawingCache
        val currentTime = Timestamp.now().seconds
        val filename = "screenshot_$currentTime.png"
        val internalStorageDir = activity.filesDir
        val file = File(internalStorageDir, filename)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            rootView.isDrawingCacheEnabled = false
            moveToReport(filename)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun moveToReport(filename: String) {
        val intent = Intent(this, ShakeDetectedActivity::class.java)
        intent.putExtra("filename", filename)
        intent.putExtra("type","report")
        startActivity(intent)
    }

    fun moveToShakeSettings() {
        val intent = Intent(this, ShakeDetectedActivity::class.java)
        intent.putExtra("type","settings")
        startActivity(intent)
    }





}