package com.ncs.o2.UI.UIComponents.BottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.UI.UIComponents.Adapters.UserListAdapter
import com.ncs.o2.databinding.DeveloperListBottomSheetBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker


/*
File : ProfileBottomSheet.kt -> com.ncs.o2.BottomSheets
Description : Profile bottom sheet  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 3:14 am on 05/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

class UserlistBottomSheet : BottomSheetDialogFragment(), UserListAdapter.OnClickCallback {

    lateinit var binding: DeveloperListBottomSheetBinding
    private val recyclerView: RecyclerView by lazy {
        binding.recyclerViewDevelopers
    }

    private val faker: Faker by lazy { Faker() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DeveloperListBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()


    }

    private fun setViews() {


        setBottomSheetConfig()

        CoroutineScope(Dispatchers.IO).launch {

            val userList = mutableListOf<User>()

            repeat(100000) {
                val user = User(
                    username = faker.funnyName().name(),
                    post = faker.pokemon().name(),
                    url = "https://picsum.photos/4${it}"
                )
                synchronized(userList) {
                    userList.add(user)
                }
            }

            withContext(Dispatchers.Main) {
                setRecyclerView(userList)
            }
        }

        binding.closeBtn.setOnClickThrottleBounceListener {
            dismissNow()
        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = false
    }

    private fun setRecyclerView(userList: MutableList<User>) {

        val adapter = UserListAdapter(userList, this@UserlistBottomSheet)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
        binding.progressbar.gone()


    }

    override fun onClick(contributor: User, position: Int) {

    }
}


//        thread {
//            repeat(100) {
//                val user = ProjectUsers(
//                    faker.funnyName().name(),
//                    faker.pokemon().name(),
//                    "https://picsum.photos/4${it}"
//                )
//                synchronized(userList) {
//                    userList.add(user)
//                }
//            }

//           Handler(Looper.getMainLooper()).post {
//                val adapter = UserListAdapter(userList, this)
//                val linearLayoutManager = LinearLayoutManager(requireContext())
//                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
//                recyclerView.layoutManager = linearLayoutManager
//                recyclerView.adapter = adapter
//                recyclerView.visible()
//                binding.progressbar.gone()
//            }








