package com.ncs.o2.UI.Teams

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.Channel
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.load
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.ActivityTasksHolderBinding
import com.ncs.o2.databinding.ActivityTeamsViewerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class TeamsViewerActivity : AppCompatActivity() {
    val binding: ActivityTeamsViewerBinding by lazy {
        ActivityTeamsViewerBinding.inflate(layoutInflater)
    }
    val userList:MutableList<User> = mutableListOf()
    val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchContributors()
        binding.btnBack.setOnClickThrottleBounceListener{
            onBackPressed()
        }
        binding.projectIcon.load(PrefManager.getProjectIconUrl(PrefManager.getcurrentProject()),resources.getDrawable(R.drawable.placeholder_image))
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
        val recyclerView=binding.recyclerView
        val adapter = TeamsAdapter(dataList.toMutableList())
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
    }
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("index", "2")
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
        super.onBackPressed()
    }
}