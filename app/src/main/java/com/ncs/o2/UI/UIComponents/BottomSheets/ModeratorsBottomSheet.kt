package com.ncs.o2.UI.UIComponents.BottomSheets

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.Issue
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.UIComponents.Adapters.UserListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.Userlist.UserlistBottomSheet
import com.ncs.o2.UI.UIComponents.BottomSheets.Userlist.UserlistViewModel
import com.ncs.o2.databinding.ContributorListBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.Faker
import javax.inject.Inject

@AndroidEntryPoint
@Issue("1. Unknown behaviour when bottom sheet shows sometime : Back elements can be clickable when dismissed , Doesn't dismiss ")
class ModeratorsBottomSheet(
//    private val OList: MutableList<User>,
    private val selected:MutableList<User>,
    private val callback: getContributorsCallback
) : BottomSheetDialogFragment(), UserListAdapter.OnClickCallback {

    private val viewModel: UserlistViewModel by viewModels()

    private lateinit var jsonString: String

    lateinit var adapter:UserListAdapter
    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    var newList:MutableList<User> = mutableListOf()
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
        binding.sheetTitle.text="Moderators"
        Log.d("moderatoresCheck","selected sent to sheet ${selected.toString()}")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(DataHolder.users.isEmpty()) {
            DataHolder.users.addAll(selected)
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
        val list=(selected+DataHolder.users).distinctBy { it.firebaseID }
        val filteredList = list.filter { it.username!!.contains(query, ignoreCase = true) }
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
            val list:MutableList<String> = mutableListOf()
            val unselectedList:MutableList<String> = mutableListOf()
            for (i in 0 until DataHolder.users.size){
                if (DataHolder.users[i].isChecked){
                    list.add(DataHolder.users[i].firebaseID!!)
                }
                else{
                    unselectedList.add(DataHolder.users[i].firebaseID!!)
                }
            }
            callback.savenewModerators(list,unselectedList)
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
        val list = userList
            .sortedByDescending { it.isChecked }
            .distinctBy { it.firebaseID }
        Log.d("moderatoresCheck",list.toString())
        adapter = UserListAdapter(list.toMutableList(), this@ModeratorsBottomSheet)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
        binding.progressbar.gone()

    }

    override fun onClick(contributor: User, position: Int, isChecked: Boolean) {
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
        fun savenewModerators(selected:MutableList<String>,unselected:MutableList<String>)
    }

}







