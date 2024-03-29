package com.ncs.o2.UI.UIComponents.BottomSheets.Userlist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.Issue
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.UIComponents.Adapters.AssigneeListAdpater
import com.ncs.o2.UI.UIComponents.Adapters.UserListAdapter
import com.ncs.o2.databinding.ContributorListBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker
import javax.inject.Inject


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

@AndroidEntryPoint
@Issue("1. Unknown behaviour when bottom sheet shows sometime : Back elements can be clickable when dismissed , Doesn't dismiss ")
class UserlistBottomSheet(
//    private val OList: MutableList<User>,
    private val callback: getContributorsCallback) : BottomSheetDialogFragment(), UserListAdapter.OnClickCallback {

    private val viewModel: UserlistViewModel by viewModels()

    private lateinit var jsonString: String
    lateinit var adapter:UserListAdapter

    @Inject
    lateinit var firestoreRepository: FirestoreRepository


    object DataHolder {
        var users: MutableList<User> = mutableListOf()
    }


    lateinit var binding: ContributorListBottomSheetBinding
    private val recyclerView: RecyclerView by lazy {
        binding.recyclerViewDevelopers
    }


    private val faker: Faker by lazy { Faker() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ContributorListBottomSheetBinding.inflate(inflater, container, false)
        binding.submitBtn.setOnClickListener {
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(DataHolder.users.isEmpty()) {
            fetchContributors(PrefManager.getcurrentProject())
        }
        setViews()

        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterList(s.toString())
            }
        })

    }
    private fun filterList(query: String) {
        val filteredList = DataHolder.users.filter { it.username!!.contains(query, ignoreCase = true) }
        adapter.updateList(filteredList)
    }
    private fun setViews() {
        setBottomSheetConfig()
        val job = CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                setRecyclerView(DataHolder.users)
            }
        }


        binding.closeBtn.setOnClickThrottleBounceListener {
            job.cancel()
            dismiss()
        }


        binding.submitBtn.setOnClickThrottleBounceListener {
            job.cancel()
            dismiss()
        }
    }

    private fun fetchContributors(projectName: String) {
        firestoreRepository.getContributors(projectName) { serverResult ->
            when (serverResult) {
                is ServerResult.Success -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val contributorList = serverResult.data
                        if (contributorList.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                getContributorsDetails(contributorList)
                            }
                        }

                    }
                }

                is ServerResult.Failure -> {
                    val exception = serverResult.exception
                }

                is ServerResult.Progress -> {
                }
            }
        }
    }

    fun getContributorsDetails(idContributors: List<String>) {

        for (contributor in idContributors) {

            viewModel.getContDetails(contributor) { result ->

                when (result) {

                    is ServerResult.Success -> {

                        binding.progressbar.gone()
                        val user = result.data
                        if (user?.role!! >= 3){
                            DataHolder.users.add(user!!)
                        }
                        jsonString = Gson().toJson(DataHolder.users)
                        setRecyclerView(DataHolder.users)

                    }


                    is ServerResult.Failure -> {

//                        utils.singleBtnDialog(
//                            "Failure",
//                            "Failure in fetching Contributors : ${result.exception.message}",
//                            "Okay"
//                        ) {
//                            requireActivity().finish()
//                        }

                    }

                    is ServerResult.Progress -> {

                        binding.progressbar.visible()
                    }
                }
            }
        }

    }

    private fun setBottomSheetConfig() {
        this.isCancelable = false
    }

    private fun setRecyclerView(userList: MutableList<User>) {

            adapter = UserListAdapter(userList, this@UserlistBottomSheet)
            val linearLayoutManager = LinearLayoutManager(requireContext())
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = linearLayoutManager
            recyclerView.adapter = adapter
            recyclerView.visible()
            binding.progressbar.gone()

    }

    override fun onClick(contributor: User, position: Int, isChecked: Boolean) {
        Toast.makeText(requireActivity(), "Clicked ${contributor.username}", Toast.LENGTH_SHORT)
            .show()

        if (isChecked) {
            DataHolder.users[position].isChecked = isChecked

        } else {
            DataHolder.users[position].isChecked = isChecked

        }
        callback.onSelectedContributors(contributor, isChecked)


    }


    interface getContributorsCallback {
        fun onSelectedContributors(contributor: User, isChecked: Boolean)
        fun onTListUpdated(TList: MutableList<User>)


    }

}







