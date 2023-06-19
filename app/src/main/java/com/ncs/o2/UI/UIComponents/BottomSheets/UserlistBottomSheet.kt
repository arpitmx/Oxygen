package com.ncs.o2.UI.UIComponents.BottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.snackbar
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.Issue
import com.ncs.o2.UI.UIComponents.Adapters.UserListAdapter
import com.ncs.o2.databinding.ContributorListBottomSheetBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker
import okhttp3.internal.userAgent
import timber.log.Timber


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

@Issue("1. Unknown behaviour when bottom sheet shows sometime : Back elements can be clickable when dismissed , Doesn't dismiss ")
class UserlistBottomSheet (private val callback : getContributorsListCallback, private var dataList : MutableList<User>): BottomSheetDialogFragment(), UserListAdapter.OnClickCallback {

    lateinit var binding: ContributorListBottomSheetBinding
    private val recyclerView: RecyclerView by lazy {
        binding.recyclerViewDevelopers
    }

    private val faker: Faker by lazy { Faker() }
    private var selectedContributerList : MutableList<User> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ContributorListBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()

    }

    private fun setViews() {

        setBottomSheetConfig()

       val job =  CoroutineScope(Dispatchers.IO).launch {

//            val userList = mutableListOf<User>()
//
//            repeat(100) {
//                val user = User(
//                    username = faker.funnyName().name(),
//                    post = faker.pokemon().name(),
//                    url = faker.avatar().image()
//                )
//                synchronized(userList) {
//                    userList.add(user)
//                }
//            }


            withContext(Dispatchers.Main) {
                setRecyclerView(dataList)
            }
        }

        binding.closeBtn.setOnClickThrottleBounceListener {
            job.cancel()
            dismiss()
        }

        binding.submitBtn.setOnClickThrottleBounceListener {
            //toast(selectedContributerList.toString())
            Timber.tag(TAG).d(dataList.toString())
            callback.onGetContributorsList(dataList)
            dismiss()
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

    override fun onClick(contributor: User, position: Int, isChecked : Boolean) {

        if (isChecked){
            selectedContributerList.add(contributor)
        }
        else {

        }

    }


    interface getContributorsListCallback{
        fun onGetContributorsList(selectedContributorsList : MutableList<User>)
    }


    companion object{
        const val TAG = "UserlistBottomSheet"
    }
}








