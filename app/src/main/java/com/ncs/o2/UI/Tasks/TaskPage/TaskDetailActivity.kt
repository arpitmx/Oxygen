package com.ncs.o2.UI.Tasks.TaskPage

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.NetworkChangeReceiver
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Report.ShakeDetectedActivity
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.UIComponents.BottomSheets.BottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.MoreOptionsBottomSheet
import com.ncs.o2.databinding.ActivityTaskDetailBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity(), TaskDetailsFragment.ViewVisibilityListner,
    NetworkChangeReceiver.NetworkChangeCallback {
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    val binding: ActivityTaskDetailBinding by lazy {
        ActivityTaskDetailBinding.inflate(layoutInflater)
    }
    var taskId:String?= null
    var taskTitle:String?=null
    lateinit var segmentName:String
    lateinit var sectionName:String
    var type: String? =null
    var index:String?=null
    lateinit var isworkspace:String
    var users:MutableList<UserInMessage> = mutableListOf()
    val sharedViewModel: SharedViewModel by viewModels()
    var moderatorsList: MutableList<String> = mutableListOf()
    var moderators: MutableList<User> = mutableListOf()
    var assignee:String=""
    private val networkChangeReceiver = NetworkChangeReceiver(this,this)
    private val viewModel: TaskDetailViewModel by viewModels()

    @Inject
    lateinit var db:TasksDatabase
    private lateinit var shakeDetector: ShakeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        registerReceiver(true)

            taskId = intent.getStringExtra("task_id")!!
            val _type=intent.getStringExtra("type")

            if (taskId != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val task = db.tasksDao().getTasksbyId(taskId!!, PrefManager.getcurrentProject())
                    if (task.isNull){
                        fetchTask(taskId!!)
                    }
                    else{
                        Log.d("taskFecthing","fetch from db")
                        segmentName = task!!.segment
                        sectionName = task.section
                        moderatorsList = task.moderators.toMutableList()
                        assignee = task.assignee
                        withContext(Dispatchers.Main){
                            val isArchived=PrefManager.isSegmentArchived(PrefManager.getcurrentProject(),segmentName)
                            if (isArchived){
                                toast("Can't open this task as segment was archived")
                                startActivity(Intent(this@TaskDetailActivity,MainActivity::class.java))
                            }
                        }
                    }
                }
                val oldRecents=PrefManager.getProjectRecents(PrefManager.getcurrentProject()).distinct().toMutableList()
                if (!oldRecents.contains(taskId)){
                    if (oldRecents.size>=10){
                        oldRecents.removeAt(oldRecents.size-1)
                        oldRecents.add(taskId!!)
                    }
                    else{
                        oldRecents.add(taskId!!)
                    }
                }
                PrefManager.saveProjectRecents(PrefManager.getcurrentProject(),oldRecents.distinct())
            }

            if (_type!=null){
                type=_type
            }

            val _index= intent.getStringExtra("index")
            if (_index!=null){
                index=_index
            }
            setActionbar()

            binding.gioActionbar.btnBack.setOnClickThrottleBounceListener {
                onBackPressed()
            }

            binding.gioActionbar.btnFav.setOnClickThrottleBounceListener{
                binding.gioActionbar.btnFav.setImageDrawable(resources.getDrawable(R.drawable.star_filled))
            }
            binding.gioActionbar.btnMore.setOnClickListener {
                val moreOptionBottomSheet =
                    MoreOptionsBottomSheet()
                moreOptionBottomSheet.show(supportFragmentManager, "more")
            }

        var favs=PrefManager.getProjectFavourites(PrefManager.getcurrentProject())
        Log.d("favsss",favs.toString())
        if (favs.contains(taskId)){
            binding.gioActionbar.btnFav.setImageDrawable(resources.getDrawable(R.drawable.star_filled))
        }
        else{
            binding.gioActionbar.btnFav.setImageDrawable(resources.getDrawable(R.drawable.star_unfilled))
        }

        binding.gioActionbar.btnFav.setOnClickThrottleBounceListener {
            if (favs.contains(taskId)){
                val newList=favs.toMutableList()
                newList.remove(taskId)
                favs=newList
                Log.d("favsss",newList.toString())
                PrefManager.saveProjectFavourites(PrefManager.getcurrentProject(),newList)
                binding.gioActionbar.btnFav.setImageDrawable(resources.getDrawable(R.drawable.star_unfilled))
                toast("Task removed from favourites")
            }
            else{
                val newList=favs.toMutableList()
                newList.add(taskId!!)
                Log.d("favsss",newList.toString())
                favs=newList
                PrefManager.saveProjectFavourites(PrefManager.getcurrentProject(),newList)
                binding.gioActionbar.btnFav.setImageDrawable(resources.getDrawable(R.drawable.star_filled))
                toast("Task added to favourites")
            }
        }

    }

    private fun fetchTask(taskId:String){
        Log.d("taskFecthing","fetch from firebase")
        this.lifecycleScope.launch {

            try {

                val taskResult = withContext(Dispatchers.IO) {
                    viewModel.getTasksById(taskId, PrefManager.getcurrentProject())
                }

                Timber.tag(TaskDetailsFragment.TAG).d("Fetched task result : ${taskResult}")

                when (taskResult) {

                    is ServerResult.Failure -> {

                        utils.singleBtnDialog(
                            "Failure",
                            "Failure in task fetching : ${taskResult.exception.message}",
                            "Okay"
                        ) {

                        }

                        binding.progressBar.gone()

                    }

                    is ServerResult.Progress -> {
                        binding.progressBar.visible()
                    }

                    is ServerResult.Success -> {
                        binding.progressBar.gone()
                        db.tasksDao().insert(taskResult.data)
                        segmentName = taskResult.data.segment
                        sectionName = taskResult.data.section
                        moderatorsList = taskResult.data.moderators.toMutableList()
                        assignee = taskResult.data.assignee
                        withContext(Dispatchers.Main){
                            val isArchived=PrefManager.isSegmentArchived(PrefManager.getcurrentProject(),segmentName)
                            if (isArchived){
                                toast("Can't open this task as segment was archived")
                                startActivity(Intent(this@TaskDetailActivity,MainActivity::class.java))
                            }
                        }
                    }
                }
            } catch (e: Exception) {

                Timber.tag(TaskDetailsFragment.TAG).e(e)
                binding.progressBar.gone()

//                utils.singleBtnDialog(
//                    "Failure", "Failure in Task exception : ${e.message}", "Okay"
//                ) {
//                    requireActivity().finish()
//                }

            }

        }
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (type=="shareTask" ||  type=="notifications" ){
            startActivity(Intent(this@TaskDetailActivity, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
        }
        else{
            overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
        }

    }
    private fun setActionbar() {
        binding.gioActionbar.titleTv.text = taskId
        binding.gioActionbar.doneItem.gone()


    }

    override fun showProgressbar(show: Boolean) {
        if (show) binding.progressbarBottomTab.visible()
        else binding.progressbarBottomTab.gone()
    }

    private var receiverRegistered = false

    private val intentFilter by lazy{
        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }

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

    override fun onPause() {
        super.onPause()
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

    override fun onResume() {
        super.onResume()
        registerReceiver(true)
        if (PrefManager.getShakePref()){
            initShake()
            shakeDetector.registerListener()
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

