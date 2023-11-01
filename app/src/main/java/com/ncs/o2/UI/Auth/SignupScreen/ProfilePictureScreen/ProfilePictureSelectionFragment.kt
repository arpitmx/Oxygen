package com.ncs.o2.UI.Auth.SignupScreen.ProfilePictureScreen

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.FragmentProfilePictureSelectionBinding
import com.ncs.o2.databinding.FragmentUserDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.InputStream

@AndroidEntryPoint
class ProfilePictureSelectionFragment : Fragment() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentProfilePictureSelectionBinding.inflate(inflater, container, false)

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
        binding.next.setOnClickThrottleBounceListener {
//            binding.layout.gone()
//            binding.progressBar.visible()
//            if (bitmap!=null) {
//                uploadImageToFirebaseStorage(bitmap!!)
//            }
//            else{
//                Toast.makeText(requireContext(),"Profile Pic can't be empty",Toast.LENGTH_LONG).show()
//            }
            val userData = mapOf(
                "PHOTO_ADDED" to true,
                "PROJECTS" to listOf("NCSOxygen")
            )

            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser?.email!!)
                .update(userData)
                .addOnSuccessListener {
                    FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser?.email!!)
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val document = task.result
                                if (document != null && document.exists()) {
                                    val bio=document.getString("BIO")
                                    val designation=document.getString("DESIGNATION")
                                    val email=document.getString("EMAIL")
                                    val username=document.getString("USERNAME")
                                    val role=document.get("ROLE")
                                    PrefManager.initialize(requireContext())
                                    PrefManager.setcurrentUserdetails(CurrentUser(EMAIL = email!!, USERNAME = username!!, BIO = bio!!, DESIGNATION = designation!!, ROLE = role.toString().toInt()))
                                    requireActivity().startActivity(Intent(requireContext(), MainActivity::class.java))
                                    requireActivity().finish()
                                }
                            } else {
                                val exception = task.exception
                                exception?.printStackTrace()
                            }
                        }
                }
                .addOnFailureListener { e ->

                }


        }

        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfilePictureSelectionViewModel::class.java)

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
                    binding.picPreview?.setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    bitmap=uriToBitmap(requireContext().contentResolver,selectedImage!!)
                    binding.picPreview?.setImageURI(selectedImage)
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
        val imageFileName = "${FirebaseAuth.getInstance().currentUser?.email}+_profilepic"
        val imageRef = storageReference.child(imageFileName)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            getImageDownloadUrl(imageRef)
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getImageDownloadUrl(imageRef: StorageReference) {
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            addImageUrlToFirestore(imageUrl)
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Failed to get image download URL", Toast.LENGTH_SHORT).show()
        }
    }
    private fun addImageUrlToFirestore(imageUrl: String) {
        FirebaseFirestore.getInstance().collection("User").document(FirebaseAuth.getInstance().currentUser?.email!!)
            .update("profilePicUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Successfully Saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                // Handle failed Firestore update
                Toast.makeText(requireContext(), "Failed to add Image", Toast.LENGTH_SHORT).show()
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