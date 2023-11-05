package com.ncs.o2.UI.EditProfile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.UserInfo
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityEditProfileBinding
import com.ncs.o2.databinding.ChooseDesignationBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() {

    private val TAG= "EditProfileActivity"

    private val viewModel: EditProfileViewModel by viewModels()
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userInfo: UserInfo
    private lateinit var newUserInfo: UserInfo
    private lateinit var bottomSheetBinding: ChooseDesignationBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpView()

        viewModel.getUserDetails()
        viewModel._getUserDetails.observe(this) { serverResult ->

            when (serverResult) {
                is ServerResult.Success -> {
                    serverResult.data.let { response ->

                        userInfo= serverResult.data
                        newUserInfo= userInfo
                        setOldUserDetails(response)
                        hideProgressBar()
                        Toast.makeText(this, response.USERNAME.toString(), Toast.LENGTH_SHORT)
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

            var newUsername: String?= null
            binding.etName.text?.trim()?.let {
                newUsername= binding.etName.toString()
            }

            val newDesignation= binding.etDesignation.text.toString() ?: null
            var newBio: String?= null
            binding.etBio.text?.trim()?.let {
                newBio= binding.etBio.toString()
            }

            val newImageUrl= "https://firebasestorage.googleapis.com/v0/b/ncso2app.appspot.com/o/quiz0.jpg?alt=media&token=d16f5af1-f85e-4ffb-9c7d-7e8acebd97b9"

            newUserInfo= UserInfo(
                newUsername ?: userInfo.USERNAME,
                newDesignation ?: userInfo.DESIGNATION,
                newBio ?: userInfo.BIO,
                newImageUrl ?: userInfo.DP_URL
            )

            editUserDetails(newUserInfo)
        }
    }

    private fun setOldUserDetails(userInfo: UserInfo){

        userInfo.USERNAME?.let {
            binding.etName.setText(it)
        }
        userInfo.DESIGNATION?.let {
            binding.etDesignation.setText(it)
        }
        userInfo.BIO?.let {
            binding.etBio.setText(it)
        }
        userInfo.DP_URL?.let {url ->
            try {

                Glide.with(this)
                    .load(url)
                    .fitCenter()
                    .placeholder(R.drawable.profile_pic_placeholder)
                    .error(R.drawable.ncs)
                    .into(binding.ivPicPreview)

            }catch (e: Exception){
                Timber.tag(TAG).e("An Errors occurred: %s", e)
            }
        }
    }

    private fun editUserDetails(newUserInfo: UserInfo){

        if(userInfo!= newUserInfo){
            viewModel.editUserDetails(newUserInfo)
            viewModel._editUserDetails.observe(this) { serverResult ->

                when (serverResult) {
                    is ServerResult.Success -> {
                        serverResult.data.let { response ->

                            // TODO back function
                            hideProgressBar()
                            binding.btnEdit.isEnabled = true
                            binding.btnEdit.isClickable = true
                            binding.btnEdit.setText(R.string.edit)
                            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT)
                                .show()

                            onBackPressed()

                        }
                    }

                    is ServerResult.Failure -> {
                        serverResult.exception.let {
                            hideProgressBar()
                            binding.btnEdit.isEnabled = false
                            binding.btnEdit.isClickable = false
                            binding.btnEdit.setText(R.string.edit)
                            Toast.makeText(this, "Errors" , Toast.LENGTH_SHORT)
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
        }else{
            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT)
                .show()
            onBackPressed()
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
        val dialog = BottomSheetDialog(this)


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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    private fun setUpView() {
        val backBtn = binding.toolbar.btnBackEditProfile
        backBtn.setOnClickThrottleBounceListener {
            onBackPressed()

        }
    }
}