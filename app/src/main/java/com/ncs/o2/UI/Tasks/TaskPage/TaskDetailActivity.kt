package com.ncs.o2.UI.Tasks.TaskPage

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.NetworkChangeReceiver
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.UIComponents.BottomSheets.BottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.MoreOptionsBottomSheet
import com.ncs.o2.databinding.ActivityTaskDetailBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity(), TaskDetailsFragment.ViewVisibilityListner,
    NetworkChangeReceiver.NetworkChangeCallback {
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    val binding: ActivityTaskDetailBinding by lazy {
        ActivityTaskDetailBinding.inflate(layoutInflater)
    }
    lateinit var taskId:String
     var type: String? =null
    var index:String?=null
    lateinit var isworkspace:String
    var users:MutableList<UserInMessage> = mutableListOf()
    val sharedViewModel: SharedViewModel by viewModels()
    var moderatorsList: MutableList<String> = mutableListOf()
    var moderators: MutableList<User> = mutableListOf()
    var assignee:String=""
    private val networkChangeReceiver = NetworkChangeReceiver(this,this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        registerReceiver(true)

            taskId = intent.getStringExtra("task_id")!!
            val _type=intent.getStringExtra("type")

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
                newList.add(taskId)
                Log.d("favsss",newList.toString())
                favs=newList
                PrefManager.saveProjectFavourites(PrefManager.getcurrentProject(),newList)
                binding.gioActionbar.btnFav.setImageDrawable(resources.getDrawable(R.drawable.star_filled))
                toast("Task added to favourites")
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
    }


    override fun onDestroy() {
        super.onDestroy()
        registerReceiver(false)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(true)
    }

    override fun onStop() {
        super.onStop()
        registerReceiver(false)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(true)
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
}

