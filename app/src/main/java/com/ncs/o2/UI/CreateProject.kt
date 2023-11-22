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

class CreateProject : AppCompatActivity(), ContributorAdapter.OnProfileClickCallback, UserlistBottomSheet.getContributorsCallback {
    private var OList: MutableList<User> = mutableListOf()

    lateinit var moderatorAdapter : ContributorAdapter
    private val moderatorsrecycler : RecyclerView by lazy {
        binding.moderatorsRecyclerView
    }
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
        OList = mutableListOf(
//            User("https://yt3.googleusercontent.com/xIPexCvioEFPIq_nuEOOsv129614S3K-AblTK2P1L9GvVIZ6wmhz7VyCT-aENMZfCzXU-qUpaA=s900-c-k-c0x00ffffff-no-rj","armax","android","url1"),
//            User("https://hips.hearstapps.com/hmg-prod/images/apple-ceo-steve-jobs-speaks-during-an-apple-special-event-news-photo-1683661736.jpg?crop=0.800xw:0.563xh;0.0657xw,0.0147xh&resize=1200:*"
//                ,"abhishek","android","url2" ),
//            User("https://picsum.photos/200","vivek","design","url3"),
//            User("https://picsum.photos/300","lalit","web","url4"),
//            User("https://picsum.photos/350","yogita","design","url5"),
//            User("https://picsum.photos/450","aditi","design","url6"),
        )
        binding.addModeratorsBtn.setOnClickThrottleBounceListener{
            val userListBottomSheet = UserlistBottomSheet(this)
            userListBottomSheet.show(supportFragmentManager, "OList")
        }

        binding.gioActionbar.btnNext.setOnClickThrottleBounceListener {
            val _title=binding.projectTitle.text.toString()
            val __title = _title.replace(" ", "")
            val title = __title.toLowerCase().capitalize()
            val project_id = "${title}${System.currentTimeMillis().toString().substring(8,12).trim()}"
            val projectData = hashMapOf(
                "PROJECT_NAME" to title.trim(),
                "PROJECT_ID" to project_id,
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
        setUpViews()

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
    private fun setUpViews() {
        setupSelectedMembersRecyclerView()
    }

    private fun setupSelectedMembersRecyclerView() {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        moderatorsrecycler.layoutManager = layoutManager
        moderatorAdapter = ContributorAdapter(mutableListOf(),this)
        moderatorsrecycler.adapter = moderatorAdapter
        moderatorsrecycler.visible()

    }

    override fun onProfileClick(user: User, position: Int) {
        TODO("Not yet implemented")
    }

    override fun removeClick(user: User, position: Int) {
        moderatorAdapter.removeUser(user)
        val pos =OList.indexOf(user)
        OList[pos].isChecked=false
    }

    override fun onSelectedContributors(contributor: User, isChecked: Boolean) {
        if (isChecked) {
            if (!moderatorAdapter.isUserAdded(contributor)) {
                moderatorAdapter.addUser(contributor)
            }
        } else {
            moderatorAdapter.removeUser(contributor)
        }    }

    override fun onTListUpdated(TList: MutableList<User>) {
        OList.clear()
        OList=TList
    }

}