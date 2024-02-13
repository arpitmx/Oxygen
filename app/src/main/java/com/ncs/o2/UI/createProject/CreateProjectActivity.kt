package com.ncs.o2.UI.createProject

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.ncs.o2.BuildConfig
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.NetworkChangeReceiver
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Report.ShakeDetectedActivity
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.Userlist.UserlistBottomSheet
import com.ncs.o2.databinding.ActivityCreateProjectBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class CreateProjectActivity : AppCompatActivity(), ContributorAdapter.OnProfileClickCallback,
    UserlistBottomSheet.getContributorsCallback , NetworkChangeReceiver.NetworkChangeCallback{

    @Inject
    lateinit var util: GlobalUtils.EasyElements
    private var bitmap: Bitmap? = null
    private lateinit var viewModel: createProjectViewModel

    lateinit var moderatorAdapter: ContributorAdapter

    val binding: ActivityCreateProjectBinding by lazy {
        ActivityCreateProjectBinding.inflate(layoutInflater)
    }
    private val intentFilter by lazy{
        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }

    private lateinit var shakeDetector: ShakeDetector

    private val networkChangeReceiver = NetworkChangeReceiver(this,this)

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

    private val TAG = "CreateProject"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(createProjectViewModel::class.java)
        registerReceiver(true)

        val desc = binding.projectDesc.text
        binding.cardView.setOnClickListener {
            pickImage()
        }
        binding.image.setOnClickListener {
            pickImage()
        }
        binding.gioActionbar.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.gioActionbar.btnNext.setOnClickThrottleBounceListener {
            val alias=binding.projectAlias.text.toString().trim().trimStart().toUpperCase()
            binding.projectAlias.setText(alias)
            if (validateString(alias)==null) {
                isProjectAliasAvailable(alias) {
                    if (it) {
                        val projectTitle=validateProjectTitle(binding.projectTitle.text.toString())
                        if ( !projectTitle.isNull && !bitmap.isNull) {

                            val _title = binding.projectTitle.text.toString()
                            val title = projectTitle!!
                            binding.projectTitle.setText(title)
                            val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                .setLink(
                                    Uri.parse(
                                        "${BuildConfig.DYNAMIC_LINK_HOST}/${
                                            title.toLowerCase().trim()
                                        }"
                                    )
                                )
                                .setDomainUriPrefix(BuildConfig.DYNAMIC_LINK_HOST)
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
                                                System.currentTimeMillis().toString()
                                                    .substring(8, 12)
                                                    .trim()
                                            }",
                                            "PROJECT_LINK" to shortLink,
                                            "PROJECT_DEEPLINK" to "${BuildConfig.DYNAMIC_LINK_HOST}/${
                                                title.toLowerCase().trim()
                                            }",
                                            "PROJECT_DESC" to desc.toString().trim(),
                                            "last_updated" to Timestamp.now(),
                                            "contributors" to listOf(PrefManager.getCurrentUserEmail()),
                                            "PROJECT_ALIAS" to alias
                                        )
                                        Timber.tag("checking image size")
                                            .d(bitmap!!.byteCount.toLong().toString())

                                        if (title.isNotEmpty()) {

                                            if (bitmap == null) {
                                                util.singleBtnDialog(
                                                    "Select a photo",
                                                    "Project Photo cannot be kept empty",
                                                    "Okay",
                                                    {})
                                            } else {
                                                binding.progressBar.visible()


                                                FirebaseFirestore.getInstance()
                                                    .collection("Projects")
                                                    .document(title)
                                                    .get()
                                                    .addOnSuccessListener { documentSnapshot ->
                                                        if (documentSnapshot.exists()) {
                                                            Toast.makeText(
                                                                this,
                                                                "Project with this title already exists",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            binding.errorTextTitle.visible()
                                                            binding.errorTextTitle.setTextColor(resources.getColor(R.color.redx))
                                                            binding.errorTextTitle.text="Project with this title already exists"
                                                            binding.progressBar.gone()
                                                        } else {
                                                            binding.errorTextTitle.visible()
                                                            binding.errorTextTitle.setTextColor(resources.getColor(R.color.green))
                                                            binding.errorTextTitle.text="This Title is Available"
                                                            binding.progressBar.gone()
                                                            FirebaseFirestore.getInstance()
                                                                .collection("Projects")
                                                                .document(title)
                                                                .set(projectData)
                                                                .addOnSuccessListener {

                                                                    uploadImageToFirebaseStorage(
                                                                        bitmap!!,
                                                                        title.trim(),
                                                                        title
                                                                    )

                                                                    FirebaseFirestore.getInstance()
                                                                        .collection("Users")
                                                                        .document(FirebaseAuth.getInstance().currentUser?.email!!)
                                                                        .update(
                                                                            "PROJECTS",
                                                                            FieldValue.arrayUnion(
                                                                                title
                                                                            )
                                                                        )
                                                                        .addOnSuccessListener {
                                                                            val userProjects =
                                                                                PrefManager.getProjectsList()
                                                                            Log.d(
                                                                                "userProjects(OLD)",
                                                                                userProjects.toString()
                                                                            )
                                                                            val mutableUserProjects =
                                                                                userProjects.toMutableList()
                                                                            mutableUserProjects.add(
                                                                                title.trim()
                                                                            )
                                                                            Log.d(
                                                                                "userProjects(NEW)",
                                                                                mutableUserProjects.toString()
                                                                            )
                                                                            PrefManager.putProjectsList(
                                                                                mutableUserProjects
                                                                            )
                                                                            PrefManager.setProjectAliasCode(title.trim(),alias)
                                                                            PrefManager.setProjectDeepLink(
                                                                                title.trim(),
                                                                                shortLink.toString()
                                                                            )

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
                                            }
                                        }
                                    } else {
                                        toast("Error creating short link")
                                    }
                                }


                        } else {
                            if (bitmap==null){
                                toast("Project Icon is required")
                            }
                        }
                    }
                }
            }
            else{
                binding.errorText.text=validateString(binding.projectAlias.text.toString())
            }
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
    private var receiverRegistered = false

    fun registerReceiver(flag : Boolean){
        if (flag){
            if (!receiverRegistered) {
                registerReceiver(networkChangeReceiver,intentFilter)
                receiverRegistered = true
            }
        }else{
            if (receiverRegistered){
                unregisterReceiver(networkChangeReceiver)
                receiverRegistered = false
            }
        }
    }
    private fun addIconUrlToFirestore(imageUrl: String, title: String) {

        viewModel.storeIconUrlToFirestore(imageUrl , title).observe(this) { data ->
            if (data) {
                PrefManager.setProjectIconUrl(title,imageUrl)
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

    fun isProjectAliasAvailable(alias: String, aliasResult: (Boolean) -> Unit) {
        binding.progressBar.visible()
        FirebaseFirestore.getInstance().collection(Endpoints.PROJECTS)
            .whereEqualTo("PROJECT_ALIAS", alias)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    aliasResult(false)
                    binding.progressBar.gone()
                    binding.errorText.text= getString(R.string.this_alias_is_already_in_use)
                    binding.errorText.visible()
                    binding.errorText.setTextColor(resources.getColor(R.color.redx))

                } else {
                    aliasResult(true)
                    binding.progressBar.gone()
                    binding.errorText.text= getString(R.string.this_alias_is_available)
                    binding.errorText.visible()
                    binding.errorText.setTextColor(resources.getColor(R.color.green))

                }
            }
            .addOnFailureListener { exception ->
                binding.progressBar.gone()
                aliasResult(false)
            }
    }
    fun validateString(input: String): String? {
        val trimmedInput = input.trim()
        if (trimmedInput.length !in 2..4) {
            binding.errorText.visible()
            binding.errorText.setTextColor(resources.getColor(R.color.redx))

            return getString(R.string.should_be_2_to_4_characters_long)
        }
        if (!trimmedInput.matches(Regex("[a-zA-Z0-9]+"))) {
            binding.errorText.visible()
            binding.errorText.setTextColor(resources.getColor(R.color.redx))
            return getString(R.string.only_alphanumeric_characters_allowed)
        }
        if (containsEmoji(trimmedInput)) {
            binding.errorText.visible()
            binding.errorText.setTextColor(resources.getColor(R.color.redx))

            return getString(R.string.emojis_are_not_allowed)
        }
        if (Character.isDigit(trimmedInput[0])) {
            binding.errorText.visible()
            binding.errorText.setTextColor(resources.getColor(R.color.redx))
            return getString(R.string.cannot_start_with_a_number)
        }
        if (trimmedInput.count { it.isLetter() } < 2) {
            binding.errorText.visible()
            binding.errorText.setTextColor(resources.getColor(R.color.redx))
            return getString(R.string.should_contain_at_least_2_alphabetical_characters)
        }
        if (hasSpacesinStartin(trimmedInput)) {
            binding.errorText.visible()
            binding.errorText.setTextColor(resources.getColor(R.color.redx))
            return getString(R.string.spaces_not_allowed_in_starting)
        }
        return null
    }
    fun containsEmoji(input: String): Boolean {
        val regex = Regex("[\\x{1F600}-\\x{1F64F}\\x{1F300}-\\x{1F5FF}\\x{1F680}-\\x{1F6FF}\\x{1F700}-\\x{1F77F}\\x{1F780}-\\x{1F7FF}\\x{1F800}-\\x{1F8FF}\\x{1F900}-\\x{1F9FF}\\x{1FA00}-\\x{1FA6F}\\x{1FA70}-\\x{1FAFF}\\x{2600}-\\x{26FF}\\x{2700}-\\x{27BF}\\x{2B50}]")
        return regex.containsMatchIn(input)
    }
    fun hasSpacesinStartin(input: String): Boolean {
        return input.startsWith("  ")
    }

    fun validateProjectTitle(input: String): String? {
        val trimmedInput = input.trim()

        if (trimmedInput.isEmpty()){
            binding.errorTextTitle.visible()
            binding.errorTextTitle.setTextColor(resources.getColor(R.color.redx))
            binding.errorTextTitle.text = getString(R.string.title_cannot_be_empty)
            return null
        }
        else {
            if (trimmedInput.lowercase()=="none") {
                binding.errorTextTitle.visible()
                binding.errorTextTitle.setTextColor(resources.getColor(R.color.redx))
                binding.errorTextTitle.text = "This title is not allowed"
                return null
            }
            if (containsEmoji(trimmedInput)) {
                binding.errorTextTitle.visible()
                binding.errorTextTitle.setTextColor(resources.getColor(R.color.redx))
                binding.errorTextTitle.text = getString(R.string.emojis_are_not_allowed)
                return null
            }
            if (Character.isDigit(trimmedInput[0])) {
                binding.errorTextTitle.visible()
                binding.errorTextTitle.setTextColor(resources.getColor(R.color.redx))
                binding.errorTextTitle.text = getString(R.string.cannot_start_with_a_number)
                return null
            }
            if (trimmedInput.count { it.isLetter() } < 2) {
                binding.errorTextTitle.visible()
                binding.errorTextTitle.setTextColor(resources.getColor(R.color.redx))
                binding.errorTextTitle.text = "Should contain at least 2 alphabetical characters"
                return null
            }
            if (hasSpacesinStartin(trimmedInput)) {
                binding.errorTextTitle.visible()
                binding.errorTextTitle.setTextColor(resources.getColor(R.color.redx))
                binding.errorTextTitle.text = "Spaces not allowed in starting"
                return null
            }
            if (trimmedInput.length > 16) {
                binding.errorTextTitle.visible()
                binding.errorTextTitle.setTextColor(resources.getColor(R.color.redx))
                binding.errorTextTitle.text = "At maximum 16 characters allowed"
                return null
            }
            val words = trimmedInput.split(" ")
            if (words.size > 2) {
                binding.errorTextTitle.visible()
                binding.errorTextTitle.setTextColor(resources.getColor(R.color.redx))
                binding.errorTextTitle.text = "Only two words are allowed"
                return null
            }
            val formattedString = words.joinToString("") {
                it.toLowerCase().capitalize()
            }

            return formattedString
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    bitmap = imageBitmap
                    binding.image.setImageBitmap(imageBitmap)
                    binding.cardView.gone()
                }

                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    bitmap = uriToBitmap(this.contentResolver, selectedImage!!)
                    binding.image.setImageURI(selectedImage)
                    binding.cardView.gone()
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
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }


    override fun onStart() {
        super.onStart()
        registerReceiver(true)
    }

    override fun onStop() {
        super.onStop()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onPause() {
        super.onPause()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
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
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
        if (PrefManager.getShakePref()){
            initShake()
            shakeDetector.registerListener()
        }
    }



    private fun initShake(){
        val shakePref=PrefManager.getShakePref()
        Log.d("shakePref",shakePref.toString())
        if (shakePref){

            val sensi=PrefManager.getShakeSensitivity()
            when(sensi){
                1->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultLightSensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
                2->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultMediumSensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
                3->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultHeavySensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
            }
        }
    }

    fun takeScreenshot(activity: Activity) {
        Log.e("takeScreenshot", activity.localClassName)
        val rootView = activity.window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = rootView.drawingCache
        val currentTime = Timestamp.now().seconds
        val filename = "screenshot_$currentTime.png"
        val internalStorageDir = activity.filesDir
        val file = File(internalStorageDir, filename)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            rootView.isDrawingCacheEnabled = false
            moveToReport(filename)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun moveToReport(filename: String) {
        val intent = Intent(this, ShakeDetectedActivity::class.java)
        intent.putExtra("filename", filename)
        intent.putExtra("type","report")
        startActivity(intent)
    }

    fun moveToShakeSettings() {
        val intent = Intent(this, ShakeDetectedActivity::class.java)
        intent.putExtra("type","settings")
        startActivity(intent)
    }





}