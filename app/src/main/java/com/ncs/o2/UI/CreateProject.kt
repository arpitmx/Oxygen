
package com.ncs.o2.UI

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.Userlist.UserlistBottomSheet
import com.ncs.o2.databinding.ActivityCreateProjectBinding

class CreateProject : AppCompatActivity() {
    val binding: ActivityCreateProjectBinding by lazy {
        ActivityCreateProjectBinding.inflate(layoutInflater)
    }
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val desc=binding.projectDesc.text
        val image=binding.image
        binding.cardView.setOnClickListener {
            pickImage()
        }
        binding.gioActionbar.btnBack.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }

        binding.gioActionbar.btnNext.setOnClickThrottleBounceListener {
            val _title=binding.projectTitle.text.toString()
            val __title = _title.replace(" ", "")
            val title = __title.toLowerCase().capitalize()
            val projectData = hashMapOf(
                "PROJECT_NAME" to title.trim(),
                "PROJECT_ID" to "${title}${System.currentTimeMillis().toString().substring(8,12).trim()}",
                "PROJECT_LINK" to "${title.toLowerCase().trim()}.ncs.in",
                "PROJECT_DESC" to desc.toString().trim(),
            )
            if (title.isNotEmpty()) {
                binding.progressBar.visible()

                FirebaseFirestore.getInstance().collection("Projects").document(title)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            Toast.makeText(this, "Project with this title already exists", Toast.LENGTH_SHORT).show()
                            binding.progressBar.gone()
                        } else {
                            FirebaseFirestore.getInstance().collection("Projects").document(title)
                                .set(projectData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Project Created", Toast.LENGTH_SHORT).show()
                                    binding.projectTitle.setText("")
                                    binding.projectDesc.setText("")
                                    binding.progressBar.gone()
                                    FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().currentUser?.email!!)
                                        .update("PROJECTS", FieldValue.arrayUnion(title))
                                        .addOnSuccessListener {
                                        }
                                        .addOnFailureListener { e ->
                                        }
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                    }
            } else {
                Toast.makeText(this, "Project Title can't be empty", Toast.LENGTH_SHORT).show()
            }

        }
    }
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.image.setImageBitmap(imageBitmap)

                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    binding.image.setImageURI(selectedImage)

                }
            }
        }
    }
}