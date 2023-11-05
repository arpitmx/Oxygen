package com.ncs.o2.UI.UIComponents.EditProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.UserInfo
import com.ncs.o2.R
import com.ncs.o2.databinding.ChooseDesignationBottomSheetBinding
import com.ncs.o2.databinding.FragmentEditProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private val TAG= "EditProfileFragment"

    private val viewModel: EditProfileViewModel by viewModels()
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var userInfo: UserInfo
    private lateinit var newUserInfo: UserInfo
    private lateinit var bottomSheetBinding:ChooseDesignationBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEditProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getUserDetails()
        viewModel._getUserDetails.observe(viewLifecycleOwner) { serverResult ->

            when (serverResult) {
                is ServerResult.Success -> {
                    serverResult.data.let { response ->

                        userInfo= serverResult.data
                        newUserInfo= userInfo
                        setOldUserDetails(response)
                        hideProgressBar()
                        Toast.makeText(activity, response.username.toString(), Toast.LENGTH_SHORT)
                            .show()

                        binding.btnEdit.isEnabled = true
                        binding.btnEdit.isClickable = true
                        binding.btnEdit.setText(R.string.edit)

                    }
                }

                else -> {}
            }
        }

        binding.etDesignation.setOnClickListener {
            setUpBottomDialog()
        }

        binding.btnEdit.setOnClickListener {

            val newUsername= binding.etName.text?.filter { it.isDigit() }.toString() ?: null
            val newDesignation= binding.etDesignation.text.toString() ?: null
            val newBio= binding.etBio.text?.filter { it.isLetterOrDigit() }.toString() ?: null
            val newImageUrl= "https://firebasestorage.googleapis.com/v0/b/ncso2app.appspot.com/o/quiz0.jpg?alt=media&token=d16f5af1-f85e-4ffb-9c7d-7e8acebd97b9"

            newUserInfo= UserInfo(
                newUsername ?: userInfo.username,
                newDesignation ?: userInfo.designation,
                newBio ?: userInfo.bio,
                newImageUrl ?: userInfo.profileDPUrl
            )

            editUserDetails(newUserInfo)
        }
    }


    private fun setOldUserDetails(userInfo: UserInfo){
        userInfo.username?.let {
            binding.etName.setText(it)
        }
        userInfo.designation?.let {
            binding.etDesignation.setText(it)
        }
        userInfo.bio?.let {
            binding.etBio.setText(it)
        }
        userInfo.profileDPUrl?.let {url ->
            try {

                Glide.with(this)
                    .load(url)
                    .fitCenter()
                    .placeholder(R.drawable.profile_pic_placeholder)
                    .error(R.drawable.purple)
                    .into(binding.ivPicPreview)

            }catch (e: Exception){
                Timber.tag(TAG).e("An Error occurred: %s", e)
            }
        }
    }

    private fun editUserDetails(newUserInfo: UserInfo){

        if(userInfo!= newUserInfo){
            viewModel.editUserDetails(newUserInfo)
            viewModel._editUserDetails.observe(viewLifecycleOwner) { serverResult ->

                when (serverResult) {
                    is ServerResult.Success -> {
                        serverResult.data.let { response ->

                            // TODO back function
                            hideProgressBar()
                            binding.btnEdit.isEnabled = true
                            binding.btnEdit.isClickable = true
                            binding.btnEdit.setText(R.string.edit)
                            Toast.makeText(activity, "Updated", Toast.LENGTH_SHORT)
                                .show()

                            requireActivity().onBackPressed()

                        }
                    }

                    is ServerResult.Failure -> {
                        serverResult.exception.let {
                            hideProgressBar()
                            binding.btnEdit.isEnabled = false
                            binding.btnEdit.isClickable = false
                            binding.btnEdit.setText(R.string.edit)
                            Toast.makeText(activity, "Error" , Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    is ServerResult.Progress -> {
                        showProgressBar()
                        binding.btnEdit.isEnabled = false
                        binding.btnEdit.isClickable = false
                        binding.btnEdit.setText(R.string.hold_on)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun hideProgressBar() {
        binding.progressbar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressbar.visibility = View.VISIBLE
    }

    private fun setUpBottomDialog(){
        bottomSheetBinding = ChooseDesignationBottomSheetBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())


        bottomSheetBinding.tvDesigner.setOnClickListener {
            binding.etDesignation.setText(R.string.designer)
            dialog.dismiss()
        }

        bottomSheetBinding.tvAndroidDeveloper.setOnClickListener {
            binding.etDesignation.setText(R.string.android_developer)
            dialog.dismiss()
        }

        bottomSheetBinding.tvWebDeveloper.setOnClickListener {
            binding.etDesignation.setText(R.string.web_developer)
            dialog.dismiss()
        }

        bottomSheetBinding.tvProgrammer.setOnClickListener {
            binding.etDesignation.setText(R.string.programmer)
            dialog.dismiss()
        }

        bottomSheetBinding.tvBackendDeveloper.setOnClickListener {
            binding.etDesignation.setText(R.string.backend_developer)
            dialog.dismiss()
        }

        bottomSheetBinding.closeBottmSheet.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(false)
        dialog.setContentView(bottomSheetBinding.root)
        dialog.show()

    }


}