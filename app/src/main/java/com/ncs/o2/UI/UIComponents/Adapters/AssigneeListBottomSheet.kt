package com.ncs.o2.UI.UIComponents.Adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
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
class AssigneeListBottomSheet(
    private var OList: MutableList<User>,
    private var selectedAssignee: MutableList<User>,
    private val callbacks: getassigneesCallback
) : BottomSheetDialogFragment(),AssigneeListAdpater.OnAssigneeClickCallback {

    private val UserListviewModel: UserlistViewModel by viewModels()
    private lateinit var jsonString: String
    private var TList: MutableList<User> = mutableListOf()

    @Inject
    lateinit var firestoreRepository: FirestoreRepository


    val binding: ContributorListBottomSheetBinding by lazy {
        ContributorListBottomSheetBinding.inflate(layoutInflater)
    }
    private val recyclerView: RecyclerView by lazy {
        binding.recyclerViewDevelopers
    }

    private val faker: Faker by lazy { Faker() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.submitBtn.setOnClickListener {
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchContributors(PrefManager.getcurrentProject())
        binding.sheetTitle.text="Assignee"
        initView()

    }

    private fun setViews() {
        setBottomSheetConfig()
        val job = CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                setRecyclerView(TList)
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

    private fun initView(){
        binding.closeBtn.setOnClickThrottleBounceListener {
            dismiss()
        }


        binding.submitBtn.setOnClickThrottleBounceListener {
            dismiss()
        }
    }





    private fun setBottomSheetConfig() {
        this.isCancelable = false
    }

    private fun setRecyclerView(userList: MutableList<User>) {
        val adapter = AssigneeListAdpater(userList, this@AssigneeListBottomSheet)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.visible()
        binding.progressbar.gone()

    }

    override fun onAssigneeClick(contributor: User, position: Int, isChecked: Boolean) {
        if (isChecked) {
            TList[position].isChecked = isChecked

        } else {
            TList[position].isChecked = isChecked
        }
        callbacks.sendassignee(contributor,isChecked,position)
        callbacks.onAssigneeTListUpdated(TList)
    }

    interface getassigneesCallback {

        fun sendassignee(assignee:User,isChecked: Boolean,position: Int)
        fun onAssigneeTListUpdated(TList: MutableList<User>)

    }
    private fun fetchContributors(projectName: String){
        firestoreRepository.getContributors(projectName) { serverResult ->
            when (serverResult) {
                is ServerResult.Success -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val contributorList = serverResult.data
                        if (contributorList.isNotEmpty()) {
                            getContributorsDetails(contributorList)
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

    fun getContributorsDetails(idContributors: List<String>){

        val list: MutableList<User> = mutableListOf()
        for (contributor in idContributors) {

            UserListviewModel.getContDetails(contributor) { result ->

                when (result) {

                    is ServerResult.Success -> {
                        binding.progressbar.gone()
                        val user = result.data
                        list.add(user!!)
                        OList = (selectedAssignee+ OList + list).distinctBy { it.firebaseID }.toMutableList()
                        TList = OList
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                                setRecyclerView(OList)
                            }
                        }

                    }


                    is ServerResult.Failure -> {
                        binding.progressbar.gone()
                    }

                    is ServerResult.Progress -> {
                        binding.progressbar.visible()

                    }
                }
            }
        }

    }


}







