package com.ncs.o2.UI.Logs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.Adapters.BottomSheetAdapter
import com.ncs.o2.UI.UIComponents.Adapters.LogsAdapter
import com.ncs.o2.databinding.ActivityLogsBinding

class LogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
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
}