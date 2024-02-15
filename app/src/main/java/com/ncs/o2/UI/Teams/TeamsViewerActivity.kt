package com.ncs.o2.UI.Teams

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.Channel
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.load
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Report.ShakeDetectedActivity
import com.ncs.o2.UI.UIComponents.BottomSheets.ProfileBottomSheet
import com.ncs.o2.databinding.ActivityTasksHolderBinding
import com.ncs.o2.databinding.ActivityTeamsViewerBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@AndroidEntryPoint
class TeamsViewerActivity : AppCompatActivity(),TeamsAdapter.OnUserClick {
    val binding: ActivityTeamsViewerBinding by lazy {
        ActivityTeamsViewerBinding.inflate(layoutInflater)
    }
    val userList:MutableList<User> = mutableListOf()
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())
    private val easyElements: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }
    private lateinit var shakeDetector: ShakeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchContributors()
        binding.btnBack.setOnClickThrottleBounceListener{
            onBackPressed()
        }
        binding.projectIcon.load(PrefManager.getProjectIconUrl(PrefManager.getcurrentProject()),resources.getDrawable(R.drawable.placeholder_image))

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchQuery(s?.toString()!!)
            }
        })
    }

    private fun searchQuery(text:String){
        var filter: MutableList<User> = mutableListOf()

        filter = userList.filter {
                    it.fullName!!.contains(text, ignoreCase = true)
        }.toMutableList()

        setRecyclerView(filter)

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
    private fun fetchContributors(){
        firestoreRepository.getProjectContributors(PrefManager.getcurrentProject()) { result ->
            when (result) {
                is ServerResult.Success -> {
                    val list=result.data
                    Log.d("resulttt",result.data.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        for (user in list){
                            fetchUserDetails(user){
                                userList.add(it)
                                setRecyclerView(userList.sortedByDescending { it.role })

                            }
                        }
                    }


                }

                is ServerResult.Failure -> {
                    binding.progressbar.gone()
                    binding.recyclerView.visible()
                }

                is ServerResult.Progress -> {
                    binding.progressbar.visible()
                    binding.recyclerView.gone()
                }
            }
        }
    }

    private fun fetchUserDetails(userID: String, onUserFetched: (User) -> Unit) {
        firestoreRepository.getUserInfobyId(userID) { result ->
            when (result) {
                is ServerResult.Success -> {
                    val user = result.data
                    if (user != null) {
                        onUserFetched(user)
                    }
                    binding.progressbar.gone()
                    binding.recyclerView.visible()
                }

                is ServerResult.Failure -> {
                    binding.progressbar.gone()
                    binding.recyclerView.visible()
                }

                is ServerResult.Progress -> {
                    binding.progressbar.visible()
                    binding.recyclerView.gone()
                }
            }
        }
    }
    fun setRecyclerView(dataList: List<User>){
        if (dataList.isEmpty()){
            binding.recyclerView.gone()
            binding.placeholder.visible()
        }
        else{
            val recyclerView=binding.recyclerView
            val adapter = TeamsAdapter(dataList.toMutableList().sortedBy { it.username }.toMutableList(),this)
            val linearLayoutManager = LinearLayoutManager(this)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = linearLayoutManager
            recyclerView.adapter = adapter
            recyclerView.visible()
            binding.placeholder.gone()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
    }

    override fun onStop() {
        super.onStop()
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onPause() {
        super.onPause()
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onResume() {
        super.onResume()
        if (PrefManager.getShakePref()){
            initShake()
            shakeDetector.registerListener()
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

    override fun onUserClicked(user: User, position: Int) {
        val bottomSheet = ProfileBottomSheet(user)
        bottomSheet.show(supportFragmentManager, "bottomsheet")
    }


}