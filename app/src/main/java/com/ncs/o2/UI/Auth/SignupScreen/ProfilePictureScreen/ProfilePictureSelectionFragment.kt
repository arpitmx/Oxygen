package com.ncs.o2.UI.Auth.SignupScreen.ProfilePictureScreen

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.popInfinity
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.FragmentProfilePictureSelectionBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class ProfilePictureSelectionFragment : Fragment() {

    @Inject
    lateinit var util : GlobalUtils.EasyElements
    companion object {
        fun newInstance() = ProfilePictureSelectionFragment()
    }
    lateinit var binding: FragmentProfilePictureSelectionBinding

    private lateinit var viewModel: ProfilePictureSelectionViewModel

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val CAMERA_PERMISSION_REQUEST = 100
    private val storageReference = FirebaseStorage.getInstance().reference

    private var bitmap:Bitmap?=null
    private lateinit var userDPRef : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(ProfilePictureSelectionViewModel::class.java)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentProfilePictureSelectionBinding.inflate(inflater, container, false)
        setupViews()
        return binding.root

    }

    private fun setupViews() {

        setUpBackPress()
        setUpOnclickListeners()


    }

    private fun setUpBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            requireActivity().toast("Cannot go back")
        }
    }


    fun setUpLoader( set : Boolean){
       if (set) {
           binding.loginWelcomeMsg.text = "Setting up your profile..."
           binding.layout.gone()
           binding.progressBar.visible()
           binding.picPreview.popInfinity(requireContext())
       }else {
           binding.loginWelcomeMsg.text = "Select profile picture"
           binding.layout.visible()
           binding.progressBar.gone()
           binding.picPreview.animation.cancel()
       }
    }

    private fun setUpOnclickListeners() {

        // For taking permissions (SET PHOTO BUTTON)

        binding.btnReselect.setOnClickThrottleBounceListener{
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST
                )
            } else {
                pickImage()
            }
        }

        // For getting the bitmap from local storage (NEXT BUTTON)

        binding.next.setOnClickThrottleBounceListener {

            if (bitmap==null) {
                Toast.makeText(requireContext(),"Profile Pic can't be empty",Toast.LENGTH_LONG).show()
                util.singleBtnDialog("Select a photo", "Profile Picture cannot be kept empty", "Okay",{})
                return@setOnClickThrottleBounceListener
            }

            Log.d("checking image size", bitmap!!.byteCount.toLong().toString())

            uploadImageToFirebaseStorage(bitmap!!)

        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        PrefManager.initialize(requireContext())
    }
    private fun pickImage() {

        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
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
                    binding.picPreview.setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    bitmap=uriToBitmap(requireContext().contentResolver,selectedImage!!)
                    val imageSize = bitmap?.byteCount
                    binding.picPreview.setImageURI(selectedImage)
                }
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage()
            } else {
                Toast.makeText(requireContext(),"Camera Permission Denied, can't take photo",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun uploadImageToFirebaseStorage(bitmap: Bitmap) {

        viewModel.uploadDPthroughRepository(bitmap).observe(viewLifecycleOwner) { result ->

            when(result){
                is ServerResult.Failure -> {
                    util.singleBtnDialog_InputError("Upload Errors",
                        "There was an issue in uploading the profile picture, ${result.exception.message} \n\nplease retry",
                        "Retry"
                    ) {
                        setUpLoader(false)
                    }

                }
                ServerResult.Progress -> {
                    setUpLoader(true)
                }
                is ServerResult.Success -> {
                    val imgStorageReference = result.data
                    Log.d("userDpIMageCheck", imgStorageReference.path)
                    getImageDownloadUrl(imgStorageReference)
                }
            }



        }
    }
    private fun getImageDownloadUrl(imageRef: StorageReference) {

        viewModel.getDPUrlThroughRepository(imageRef).observe(viewLifecycleOwner) { result ->

            when(result){
                is ServerResult.Failure -> {
                    util.singleBtnDialog_InputError("Upload Errors",
                        "There was an issue in uploading the profile picture, ${result.exception.message},\n\nplease retry",
                        "Retry"
                    ) {
                        setUpLoader(false)
                        imageRef.delete()
                    }
                }
                ServerResult.Progress -> {
                    setUpLoader(true)
                }
                is ServerResult.Success -> {

                    PrefManager.setDpUrl(result.data)
                    addImageUrlToFirestore(result.data)

                }
            }


        }

    }
    private fun addImageUrlToFirestore(imageUrl: String) {

        viewModel.storeDPUrlToFirestore(imageUrl).observe(viewLifecycleOwner) { data ->
            if (data) {
                Toast.makeText(requireContext(), "Successfully Saved", Toast.LENGTH_SHORT).show()

                val userData = mapOf(
                    "PHOTO_ADDED" to true,
                    "PROJECTS" to listOf("NCSOxygen"),
                    "TIMESTAMP" to Timestamp.now()
                )

                FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser?.email!!)
                    .update(userData)
                    .addOnSuccessListener {
                        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser?.email!!)
                            .get(Source.SERVER)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    val document = task.result
                                    if (document != null && document.exists()) {

                                        val bio=document.getString(Endpoints.User.BIO)
                                        val designation=document.getString(Endpoints.User.DESIGNATION)
                                        val email=document.getString(Endpoints.User.EMAIL)
                                        val username=document.getString(Endpoints.User.USERNAME)
                                        val role= document.getLong(Endpoints.User.ROLE)
                                        var fcm : String? = document.getString(Endpoints.User.FCM_TOKEN)

                                        if (fcm==null){
                                            fcm = ""
                                        }

                                        Timber.tag("Profile").d("Bio : ${bio}\n Designation : ${designation}\n Email : ${email} \n Username : ${username}\n Role : ${role}")

                                        with(PrefManager){

                                            putProjectsList(listOf("NCSOxygen"))

                                            setLastSeenTimeStamp(0)
                                            setLatestNotificationTimeStamp(0)

                                            setcurrentUserdetails(CurrentUser(
                                                EMAIL = email!!,
                                                USERNAME = username!!,
                                                BIO = bio!!,
                                                DESIGNATION = designation!!,
                                                ROLE = role!!,
                                                FCM_TOKEN = fcm,
                                            ))

                                        }

                                        requireActivity().startActivity(Intent(requireContext(), MainActivity::class.java))
                                        requireActivity().finish()
                                    }
                                } else {

                                    val exception = task.exception
                                    exception?.printStackTrace()

                                    util.singleBtnDialog_InputError("Errors",
                                        "There was an error : ${exception?.message} \nPlease retry",
                                        "Retry"
                                    ) {
                                        Toast.makeText(requireActivity(), "Retrying", Toast.LENGTH_SHORT).show()
                                        addImageUrlToFirestore(imageUrl)
                                    }
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        util.singleBtnDialog_InputError("Errors",
                            "There was an error : ${e.message} \nPlease retry",
                            "Retry"
                        ) {
                            Toast.makeText(requireActivity(), "Retrying", Toast.LENGTH_SHORT).show()
                           addImageUrlToFirestore(imageUrl)
                        }
                    }




            } else {

                    util.singleBtnDialog_InputError("Upload Errors",
                        "There was an issue in uploading the profile picture,\n\nplease retry",
                        "Retry"
                    ) {
                        setUpLoader(false)
                    }
                }

        }
    }

    fun uriToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
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
}