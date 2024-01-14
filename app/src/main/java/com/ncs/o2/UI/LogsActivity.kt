package com.ncs.o2.UI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
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
        binding.readcount.text=PrefManager.getReadCount().toString()

    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
}