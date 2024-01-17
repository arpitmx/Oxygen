package com.ncs.o2.UI.EditProfile

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.UserInfo
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.NetworkChangeReceiver
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.StartScreen.StartScreen
import com.ncs.o2.databinding.ActivityEditProfileBinding
import com.ncs.o2.databinding.ChooseDesignationBottomSheetBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileActivity : AppCompatActivity() , NetworkChangeReceiver.NetworkChangeCallback{

    private val TAG= "EditProfileActivity"

    @Inject
    lateinit var util : GlobalUtils.EasyElements

    private val viewModel: EditProfileViewModel by viewModels()
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userInfo: UserInfo
    private lateinit var newUserInfo: UserInfo
    private lateinit var bottomSheetBinding: ChooseDesignationBottomSheetBinding

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val CAMERA_PERMISSION_REQUEST = 100
    private lateinit var userDPRef : StorageReference

    private var bitmap: Bitmap?=null
    private var newImageUrl: String?= null
    private var newBio: String?= null
    var newUsername: String?= null
    private val networkChangeReceiver = NetworkChangeReceiver(this,this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
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


            binding.etName.text?.trim()?.let {
                newUsername= binding.etName.text.toString()
            }

            val newDesignation= binding.etDesignation.text.toString() ?: null

            binding.etBio.text?.trim()?.let {
                newBio= binding.etBio.text.toString()
            }

            newUserInfo= UserInfo(
                newUsername ?: userInfo.USERNAME,
                newBio ?: userInfo.BIO,
                newDesignation ?: userInfo.DESIGNATION,
                newImageUrl ?: userInfo.DP_URL
            )

            if(bitmap!= null){

                showProgressBar()
                binding.btnEdit.isEnabled = false
                binding.btnEdit.isClickable = false
                binding.btnEdit.setText(R.string.hold_on)
                uploadImageToFirebaseStorage(bitmap!!)
            }else{
                editUserDetails(newUserInfo)
            }
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

        userInfo.DP_URL?.let { url ->
            try {

                Glide.with(this)
                    .load(url)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .fitCenter()
                    .placeholder(R.drawable.profile_pic_placeholder)
                    .error(R.drawable.ncs)
                    .into(binding.ivPicPreview)

            }catch (e: Exception){
                Timber.tag(TAG).e("An Errors occurred: %s", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
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

                            val bio=response.BIO
                            val designation=response.DESIGNATION
                            val username=response.USERNAME
                            val currentUser= PrefManager.getcurrentUserdetails()
                            val  role= currentUser.ROLE
                            val email=currentUser.EMAIL
                            val new_dp_url : String? = response.DP_URL

                            Timber.tag("Profile").d("Bio : ${bio}\n Designation : ${designation}\n Email : ${email} \n Username : ${username}\n Role : ${role}")
                            PrefManager.putProjectsList(listOf("NCSOxygen"))
                            PrefManager.setcurrentUserdetails(CurrentUser(EMAIL = email, USERNAME = username!!, BIO = bio!!, DESIGNATION = designation!!, ROLE = role))
                            PrefManager.setDpUrl(new_dp_url)

                            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT)
                                .show()

                            startActivity(Intent(this,StartScreen::class.java))
                            finishAffinity()

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
            Toast.makeText(this, "No changes", Toast.LENGTH_SHORT)
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


        binding.ivPicPreview.setOnClickThrottleBounceListener{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST
                )
            } else {
                pickImage()
            }
        }


        val backBtn = binding.toolbar.btnBackEditProfile
        backBtn.setOnClickThrottleBounceListener {
            onBackPressed()

        }
    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap) {

        viewModel.uploadDPthroughRepository(bitmap).observe(this) { result ->

            when(result){
                is ServerResult.Failure -> {
                    util.singleBtnDialog_InputError("Upload Errors",
                        "There was an issue in uploading the profile picture, ${result.exception.message} \n\nplease retry",
                        "Retry"
                    ) {
                        hideProgressBar()
                        editUserDetails(newUserInfo)
                    }

                }
                ServerResult.Progress -> {
                    showProgressBar()
                    binding.btnEdit.isEnabled = false
                    binding.btnEdit.isClickable = false
                    binding.btnEdit.setText(R.string.hold_on)

                }
                is ServerResult.Success -> {
                    val imgStorageReference = result.data
                    Timber.tag("userDpIMageCheck").d(imgStorageReference.path)
                    getImageDownloadUrl(imgStorageReference)
                }
            }



        }
    }
    private fun getImageDownloadUrl(imageRef: StorageReference) {

        viewModel.getDPUrlThroughRepository(imageRef).observe(this) { result ->

            when(result){
                is ServerResult.Failure -> {
                    util.singleBtnDialog_InputError("Upload Errors",
                        "There was an issue in uploading the profile picture, ${result.exception.message},\n\nplease retry",
                        "Retry"
                    ) {
                        hideProgressBar()
                        imageRef.delete()
                        editUserDetails(newUserInfo)
                    }
                }
                ServerResult.Progress -> {
                    showProgressBar()
                    binding.btnEdit.isEnabled = false
                    binding.btnEdit.isClickable = false
                    binding.btnEdit.setText(R.string.hold_on)

                }
                is ServerResult.Success -> {

                    PrefManager.setDpUrl(result.data)
                    newImageUrl= result.data
                    editUserDetails(newUserInfo)
                }
            }
        }
    }


    private fun pickImage() {

        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Take Photo" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
                "Choose from Gallery" -> {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, REQUEST_IMAGE_PICK)
                }
                "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    bitmap=imageBitmap
                    binding.ivPicPreview?.setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    bitmap=uriToBitmap(this.contentResolver,selectedImage!!)
                    binding.ivPicPreview?.setImageURI(selectedImage)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage()
            } else {
                Toast.makeText(this,"Camera Permission Denied, can't take photo",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uriToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
    }

    override fun onOnlineModePositiveSelected() {
        PrefManager.setAppMode(Endpoints.ONLINE_MODE)
        util.restartApp()
    }

    override fun onOfflineModePositiveSelected() {
        startActivity(intent)
        PrefManager.setAppMode(Endpoints.OFFLINE_MODE)
    }

    override fun onOfflineModeNegativeSelected() {
        networkChangeReceiver.retryNetworkCheck()
    }
}