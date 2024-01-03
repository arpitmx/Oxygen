package com.ncs.o2.UI.createProject

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.Userlist.UserlistBottomSheet
import com.ncs.o2.databinding.ActivityCreateProjectBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class CreateProject : AppCompatActivity(), ContributorAdapter.OnProfileClickCallback,
    UserlistBottomSheet.getContributorsCallback {

    @Inject
    lateinit var util: GlobalUtils.EasyElements

    private var OList: MutableList<User> = mutableListOf()

    private var bitmap: Bitmap? = null
    private lateinit var viewModel: createProjectViewModel

    lateinit var moderatorAdapter: ContributorAdapter
//    private val moderatorsrecycler: RecyclerView by lazy {
//        binding.moderatorsRecyclerView
//    }
    val binding: ActivityCreateProjectBinding by lazy {
        ActivityCreateProjectBinding.inflate(layoutInflater)
    }


    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(createProjectViewModel::class.java)

        val desc = binding.projectDesc.text
        val image = binding.image
        binding.cardView.setOnClickListener {
            pickImage()
        }
        binding.gioActionbar.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
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

//        binding.addModeratorsBtn.setOnClickThrottleBounceListener {
//            val userListBottomSheet = UserlistBottomSheet(this)
//            userListBottomSheet.show(supportFragmentManager, "OList")
//        }

        binding.gioActionbar.btnNext.setOnClickThrottleBounceListener {
            if (binding.projectTitle.text.toString().isNotEmpty() && !bitmap.isNull) {
                val project_id =
                    "${title}${System.currentTimeMillis().toString().substring(8, 12).trim()}"
                val _title = binding.projectTitle.text.toString()
                val __title = _title.replace(" ", "")
                val title = __title.toLowerCase().capitalize()
                val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse("https://oxy2.page.link/join/${title.toLowerCase().trim()}"))
                    .setDomainUriPrefix("https://oxy2.page.link")
                    .setAndroidParameters(
                        DynamicLink.AndroidParameters.Builder("com.ncs.o2")
                            .setMinimumVersion(1)
                            .build()
                    )
                    .buildDynamicLink()
                FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLongLink(dynamicLink.uri)
                    .buildShortDynamicLink()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val shortLink = task.result?.shortLink
                            val previewLink = task.result?.previewLink
                            val projectData = hashMapOf(
                                "PROJECT_NAME" to title.trim(),
                                "PROJECT_ID" to "${title}${
                                    System.currentTimeMillis().toString().substring(8, 12).trim()
                                }",
                                "PROJECT_LINK" to shortLink,
                                "PROJECT_DEEPLINK" to "https://oxy2.page.link/join/${
                                    title.toLowerCase().trim()
                                }",
                                "PROJECT_DESC" to desc.toString().trim(),
                                "last_updated" to Timestamp.now(),
                                "contributors" to listOf(PrefManager.getCurrentUserEmail())
                            )
                            Log.d("checking image size", bitmap!!.byteCount.toLong().toString())

                            if (title.isNotEmpty()) {

                                if (bitmap == null) {
                                    util.singleBtnDialog(
                                        "Select a photo",
                                        "Project Photo cannot be kept empty",
                                        "Okay",
                                        {})
                                } else {
                                    binding.progressBar.visible()


                                    FirebaseFirestore.getInstance().collection("Projects")
                                        .document(title)
                                        .get()
                                        .addOnSuccessListener { documentSnapshot ->
                                            if (documentSnapshot.exists()) {
                                                Toast.makeText(
                                                    this,
                                                    "Project with this title already exists",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                binding.progressBar.gone()
                                            } else {
                                                FirebaseFirestore.getInstance()
                                                    .collection("Projects")
                                                    .document(title)
                                                    .set(projectData)
                                                    .addOnSuccessListener {

                                                        uploadImageToFirebaseStorage(
                                                            bitmap!!,
                                                            project_id,
                                                            title
                                                        )

                                                        FirebaseFirestore.getInstance()
                                                            .collection("Users")
                                                            .document(FirebaseAuth.getInstance().currentUser?.email!!)
                                                            .update(
                                                                "PROJECTS",
                                                                FieldValue.arrayUnion(title)
                                                            )
                                                            .addOnSuccessListener {
                                                                val userProjects = PrefManager.getProjectsList()
                                                                Log.d("userProjects(OLD)", userProjects.toString())
                                                                val mutableUserProjects = userProjects.toMutableList()
                                                                mutableUserProjects.add(title.trim())
                                                                Log.d("userProjects(NEW)", mutableUserProjects.toString())
                                                                PrefManager.putProjectsList(mutableUserProjects)

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
                                }
                            } else {
                                if (bitmap == null) {
                                    util.singleBtnDialog(
                                        "Select a photo",
                                        "Project Photo cannot be kept empty",
                                        "Okay",
                                        {})
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Project Title can't be empty",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            println("Error creating short link: ${task.exception}")
                        }
                    }


            }
            else{
                toast("Title and Project Icon are required")
            }
            // setUpViews()
        }

    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap, project_id: String, title: String) {

        viewModel.uploadIconthroughRepository(bitmap, project_id).observe(this) { result ->

            when(result){
                is ServerResult.Failure -> {
                    }
                is ServerResult.Progress -> {
                    }
                is ServerResult.Success -> {
                    val imgStorageReference = result.data

                    getIconDownloadUrl(imgStorageReference, title)
                }
            }
        }
    }

    private fun getIconDownloadUrl(imageRef: StorageReference, title: String) {

        viewModel.getIconUrlThroughRepository(imageRef).observe(this) { result ->

            when(result){
                is ServerResult.Failure -> {
                    util.singleBtnDialog_InputError("Upload Errors",
                        "There was an issue in uploading the profile picture, ${result.exception.message},\n\nplease retry",
                        "Retry"
                    ) {
                        imageRef.delete()
                    }
                }
                ServerResult.Progress -> {
                }
                is ServerResult.Success -> {
                    addIconUrlToFirestore(result.data ,title)

                }
            }
        }
    }

    private fun addIconUrlToFirestore(imageUrl: String, title: String) {

        viewModel.storeIconUrlToFirestore(imageUrl , title).observe(this) { data ->
            if (data) {
                Toast.makeText(this, "Project Created", Toast.LENGTH_SHORT)
                    .show()
                binding.projectTitle.setText("")
                binding.projectDesc.setText("")
                binding.progressBar.gone()
                startActivity(Intent(this, MainActivity::class.java))
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
                    bitmap = imageBitmap
                    binding.image.setImageBitmap(imageBitmap)
                }

                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    bitmap = uriToBitmap(this.contentResolver, selectedImage!!)
                    binding.image.setImageURI(selectedImage)
                }
            }
        }
    }

//    private fun setUpViews() {
//        setupSelectedMembersRecyclerView()
//    }


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

//    private fun setupSelectedMembersRecyclerView() {
//        val layoutManager = FlexboxLayoutManager(this)
//        layoutManager.flexDirection = FlexDirection.ROW
//        layoutManager.flexWrap = FlexWrap.WRAP
//        moderatorsrecycler.layoutManager = layoutManager
//        moderatorAdapter = ContributorAdapter(mutableListOf(), this)
//        moderatorsrecycler.adapter = moderatorAdapter
//        moderatorsrecycler.visible()
//
//    }


    override fun onProfileClick(user: User, position: Int) {
        TODO("Not yet implemented")
    }

    override fun removeClick(user: User, position: Int) {
        moderatorAdapter.removeUser(user)
        val pos = OList.indexOf(user)
        OList[pos].isChecked = false
    }

    override fun onSelectedContributors(contributor: User, isChecked: Boolean) {
        if (isChecked) {
            if (!moderatorAdapter.isUserAdded(contributor)) {
                moderatorAdapter.addUser(contributor)
            }
        } else {
            moderatorAdapter.removeUser(contributor)
        }
    }

    override fun onTListUpdated(TList: MutableList<User>) {
        OList.clear()
        OList = TList
    }

}