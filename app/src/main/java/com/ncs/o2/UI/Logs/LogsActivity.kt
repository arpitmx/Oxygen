package com.ncs.o2.UI.Logs

import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.NetworkChangeReceiver
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.Adapters.BottomSheetAdapter
import com.ncs.o2.UI.UIComponents.Adapters.LogsAdapter
import com.ncs.o2.databinding.ActivityLogsBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LogsActivity : AppCompatActivity() ,NetworkChangeReceiver.NetworkChangeCallback{
    private lateinit var binding: ActivityLogsBinding
    private val networkChangeReceiver = NetworkChangeReceiver(this,this)
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)

        setContentView(binding.root)
        setUpViews()

    }
    private fun setUpViews(){
        binding.gioActionbar.titleTv.text = "Logs"
        binding.gioActionbar.btnFav.gone()
        binding.gioActionbar.btnRequestWork.gone()

        binding.gioActionbar.btnBack.setOnClickListener {
            onBackPressed()
        }

        val allTimeCounts=PrefManager.getAllTimeReadCount()
        val readCounts=PrefManager.getReadCount()

        val list= listOf(
            LogsItem(title = "No. of Reads :", count = readCounts.toString()),
            LogsItem(title = "No. of All Time Reads :", count = allTimeCounts.toString()))

        setRecyclerView(list)
    }
    fun setRecyclerView(dataList: List<LogsItem>){
        val recyclerView=binding.rvLogs
        val adapter = LogsAdapter(dataList.toMutableList())
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
    data class LogsItem(
        val title:String,
        val count:String,
    )
    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
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
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
    }
}