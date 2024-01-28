package com.ncs.o2.UI.Auth

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.Domain.Utility.Issue
import com.ncs.o2.HelperClasses.NetworkChangeReceiver
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.ChooserScreen.ChooserFragment
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Report.ShakeDetectedActivity
import com.ncs.o2.databinding.ActivityAuthScreenBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class AuthScreenActivity @Inject constructor() : AppCompatActivity(),NetworkChangeReceiver.NetworkChangeCallback {

    private val binding: ActivityAuthScreenBinding by lazy {
        ActivityAuthScreenBinding.inflate(layoutInflater)
    }
    private val networkChangeReceiver = NetworkChangeReceiver(this,this)
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    private val intentFilter by lazy{
        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }
    private lateinit var shakeDetector:ShakeDetector
    @Issue("Fragment duplicate on configuration change, implement that.")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        registerReceiver(true)

        if (PrefManager.getAppMode()==Endpoints.OFFLINE_MODE){
            utils.singleBtnDialog("No network","As network is not available and your were not logged in, you can't proceed further","EXIT") {
                finish()
                System.out.close()
            }
        }
        val isDetailsAdded = intent.getStringExtra("isDetailsAdded")
        val isPhotoAdded = intent.getStringExtra("isPhotoAdded")
        val isEmailVerified = intent.getStringExtra("isEmailVerified")

        val showchooser = intent.getStringExtra("showchooser")

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navhost) as NavHostFragment
        val navController = navHostFragment.navController
        if (isEmailVerified=="false"){
            val user=FirebaseAuth.getInstance().currentUser
            Log.d("userCheck",user!!.email.toString())
            if (user.isEmailVerified){
                navController.navigate(R.id.userDetailsFragment)
            }
            else if(!user.isEmailVerified){
                navController.navigate(R.id.loginScreenFragment)
            }
            else{
                sendVerificationEmail(user,navController)
            }
        }
        else{
            if (isDetailsAdded=="false" && showchooser=="false"){
                navController.navigate(R.id.userDetailsFragment)
            }
            else if(isPhotoAdded=="false" && showchooser=="false"){
                navController.navigate(R.id.profilePictureSelectionFragment)
            }
            else if(isPhotoAdded=="false" && isDetailsAdded=="false" && showchooser=="false"){
                navController.navigate(R.id.userDetailsFragment)
            }
            else if (isPhotoAdded=="false" && isDetailsAdded=="false" && showchooser=="true"){
                navController.navigate(R.id.chooserFragment)
            }
        }
        setUpViews()

    }
    private fun sendVerificationEmail(user: FirebaseUser?,navController:NavController) {

        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this, "Verification email sent",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate(R.id.emailConfirmationFragment)

                } else {
                    Toast.makeText(
                        this, "Failed to send verification email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
    private fun setUpViews() {

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

    override fun onStart() {
        super.onStart()
        registerReceiver(true)
    }

    override fun onStop() {
        super.onStop()
        registerReceiver(false)
        if (PrefManager.getShakePref()){
            shakeDetector.unregisterListener()
        }
    }

    override fun onPause() {
        super.onPause()
        registerReceiver(false)
        if (PrefManager.getShakePref()){
            shakeDetector.unregisterListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        registerReceiver(false)
        if (PrefManager.getShakePref()){
            shakeDetector.unregisterListener()
        }
    }


    override fun onOnlineModePositiveSelected() {
        PrefManager.setAppMode(Endpoints.ONLINE_MODE)
        utils.restartApp()
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
            utils.twoBtnDialog("Shake to report", msg = "Shake was detected, as a result screenshot of the previous screen was taken, do you want to report it as a bug/feature ? ","Report","Turn OFF",{
                moveToReport(filename)
            },{
                moveToShakeSettings()
            })
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

