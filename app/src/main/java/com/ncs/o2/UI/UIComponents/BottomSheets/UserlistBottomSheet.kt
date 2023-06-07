package com.ncs.o2.UI.UIComponents.BottomSheets

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.ProjectUsers
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.UI.UIComponents.Adapters.UserListAdapter
import com.ncs.o2.databinding.DeveloperListBottomSheetBinding
import com.ncs.o2.databinding.ProfileBottomSheetBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker
import kotlin.concurrent.thread


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
class UserlistBottomSheet (
) :BottomSheetDialogFragment(), UserListAdapter.OnClickCallback {

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
        return binding.getRoot()
    }

    override fun onViewCreated( view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()


    }

    private fun setViews() {

        CoroutineScope(Dispatchers.IO).launch {

            val userList = mutableListOf<ProjectUsers>()

            repeat(300) {
                val user = ProjectUsers(
                    faker.funnyName().name(),
                    faker.pokemon().name(),
                    "https://picsum.photos/4${it}"
                )
                synchronized(userList) {
                    userList.add(user)
                }
            }

            setRecyclerView(userList)
    }
    }

    suspend fun setRecyclerView(userList: MutableList<ProjectUsers>) {


        withContext(Dispatchers.Main){

            val adapter = UserListAdapter(userList, this@UserlistBottomSheet)
                val linearLayoutManager = LinearLayoutManager(requireContext())
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                recyclerView.layoutManager = linearLayoutManager
                recyclerView.adapter = adapter
                recyclerView.visible()
                binding.progressbar.gone()
        }
    }

    override fun onClick(contributor: ProjectUsers, position: Int) {

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








