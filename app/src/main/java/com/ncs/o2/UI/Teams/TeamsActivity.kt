package com.ncs.o2.UI.Teams

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.SharedViewModel
import com.ncs.o2.UI.Teams.Chat.TeamsChatFragment
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.ActivityTeamsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamsActivity : AppCompatActivity() {

    val binding: ActivityTeamsBinding by lazy {
        ActivityTeamsBinding.inflate(layoutInflater)
    }
    val sharedViewModel: SharedViewModel by viewModels()

    lateinit var channelName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        channelName = intent.getStringExtra("channel_name")!!



        binding.actionBar.channelName.text="# $channelName"


        attachFragment()
        setUpViews()
    }
    private fun setUpViews(){
        binding.actionBar.btnBack.setOnClickThrottleBounceListener{
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("index", "2")
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
        super.onBackPressed()
    }

    private fun attachFragment(){
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = TeamsChatFragment()
        transaction.replace(R.id.teams_chat_fragment_container, fragment)
        transaction.commit()
    }
}