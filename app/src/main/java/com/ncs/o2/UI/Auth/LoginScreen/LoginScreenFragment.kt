package com.ncs.o2.UI.Auth.LoginScreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.messaging.FirebaseMessaging
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.state.SegmentItem
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.SignupScreen.SignUpScreenFragment
import com.ncs.o2.UI.StartScreen.StartScreen
import com.ncs.o2.databinding.FragmentLoginScreenBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject


@Suppress("DEPRECATION")
@AndroidEntryPoint
class LoginScreenFragment @Inject constructor() : Fragment() {

    companion object {
        fun newInstance() = LoginScreenFragment()
    }

    @Inject
    lateinit var util : GlobalUtils.EasyElements
    private val viewModel: LoginScreenViewModel by viewModels()
    lateinit var binding: FragmentLoginScreenBinding
    lateinit var authResource: LiveData<ServerResult<FirebaseUser>?>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginScreenBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setUpValidation()

    }


    private fun setUpValidation() {
        authResource = viewModel.loginLiveData
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.validationEvents.collect { event ->
                when (event) {
                    LoginScreenViewModel.ValidationEvent.Success -> {
                        //requireActivity().startActivity(Intent(requireContext(),MainActivity::class.java))
                        authResource.let { liveData ->
                            liveData.observe(viewLifecycleOwner) { result ->
                                handleLoginResult(result)
                            }
                        }
                    }

                }
            }
        }
    }

    private fun handleLoginResult(result: ServerResult<FirebaseUser>?) {
        when (result) {
            is ServerResult.Failure -> {
                binding.progressbar.gone()
                binding.btnLogin.isEnabled = true
                binding.btnLogin.isClickable = true
                binding.btnLogin.text = getString(R.string.login)
                Toast.makeText(
                    activity,
                    "Registration Failed : ${result.exception.message}",
                    Toast.LENGTH_SHORT
                ).show()

                Timber.tag(SignUpScreenFragment.TAG)
                    .d("Registration Failed : ${result.exception.message}")

            }

            ServerResult.Progress -> {


                binding.progressbar.visible()
                binding.btnLogin.isEnabled = false
                binding.btnLogin.isClickable = false
                binding.btnLogin.text = getString(R.string.hold_on)


            }

            is ServerResult.Success -> {

                FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().currentUser?.email!!)
                    .get(Source.SERVER)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {



                            val document = task.result
                            if (document != null && document.exists()) {

                                val isDetailsAdded = document.getBoolean(Endpoints.User.DETAILS_ADDED)
                                val isPhotoAdded = document.getBoolean(Endpoints.User.PHOTO_ADDED)
                                var isEmailVerified=document.getBoolean(Endpoints.User.EMAIL_VERIFIED)
                                if (isEmailVerified==null){
                                    isEmailVerified=true
                                }

                                CoroutineScope(Dispatchers.IO).launch {

                                    withContext(Dispatchers.Main){



                                        if (isDetailsAdded == true && isPhotoAdded == true && isEmailVerified==true) {

                                            val bio = document.getString(Endpoints.User.BIO)
                                            val designation = document.getString(Endpoints.User.DESIGNATION)
                                            val email = document.getString(Endpoints.User.EMAIL)
                                            val username = document.getString(Endpoints.User.USERNAME)
                                            val role = document.getLong(Endpoints.User.ROLE)
                                            val dp: String? = document.getString(Endpoints.User.DP_URL)
                                            var fcmToken = document.getString(Endpoints.User.FCM_TOKEN)
                                            val projects = document.get("PROJECTS") as List<String>
                                            for (project in projects){
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val list=getProjectSegments(project)
                                                    saveProjectIconUrls(projectName = project)
                                                    val newList=list.toMutableList().sortedByDescending { it.creation_DATETIME }
                                                    PrefManager.saveProjectSegments(project,list)
                                                    if (newList.isNotEmpty()){
                                                        PrefManager.setLastSegmentsTimeStamp(project,newList[0].creation_DATETIME!!)
                                                    }
                                                }

                                            }
                                            val notification_timestamp=document.getLong("NOTIFICATION_LAST_SEEN")
                                            if (fcmToken == null) {
                                                fcmToken { token->
                                                    fcmToken = token
                                                }
                                            }
                                            with(PrefManager){

                                                initialize(requireContext())
                                                setLastSeenTimeStamp(notification_timestamp!!)
                                                setProjectTimeStamp(PrefManager.getcurrentProject(),notification_timestamp)
                                                setLatestNotificationTimeStamp(0)
                                                setNotificationCount(0)

                                                setcurrentUserdetails(
                                                    CurrentUser(
                                                        EMAIL = email!!,
                                                        USERNAME = username!!,
                                                        BIO = bio!!,
                                                        DESIGNATION = designation!!,
                                                        ROLE = role!!,
                                                        DP_URL = dp,
                                                        FCM_TOKEN = fcmToken!!
                                                    )
                                                )
                                            }



                                            Toast.makeText(activity, "Power to you now", Toast.LENGTH_SHORT).show()
                                            binding.progressbar.gone()

                                            Timber.tag(SignUpScreenFragment.TAG).d(
                                                "Login success : ${result.data.uid}"
                                            )

                                            startActivity(
                                                Intent(
                                                    requireContext(),
                                                    StartScreen::class.java
                                                )
                                            )

                                            requireActivity().finishAffinity()

                                        }else if (isEmailVerified==false){
                                            if (FirebaseAuth.getInstance().currentUser!!.isEmailVerified){
                                                findNavController().navigate(R.id.action_loginScreenFragment_to_userDetailsFragment)
                                            }
                                            else{
                                                sendVerificationEmail(FirebaseAuth.getInstance().currentUser)
                                            }
                                        }
                                        else if (isDetailsAdded == false) {
                                            findNavController().navigate(R.id.action_loginScreenFragment_to_userDetailsFragment)
                                        }
                                        else if (isPhotoAdded == false) {
                                            findNavController().navigate(R.id.action_loginScreenFragment_to_profilePictureSelectionFragment)
                                        }
                                    }
                                }


                            }
                        }
                        else {

                            val exception = task.exception
                            exception?.printStackTrace()
                            util.singleBtnDialog_ServerError(msg = exception!!.message.toString()) {
                                findNavController().navigate(R.id.action_loginScreenFragment_to_chooserFragment)
                            }

                        }
                    }

            }

            null -> {

            }
        }
    }

    private fun saveProjectIconUrls(projectName:String){

        FirebaseFirestore.getInstance().collection(Endpoints.PROJECTS).document(projectName).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val imageUrl = documentSnapshot.data?.get("ICON_URL")?.toString()
                    PrefManager.setProjectIconUrl(projectName,imageUrl!!)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("failCheck", exception.toString())
            }

    }

    private fun sendVerificationEmail(user: FirebaseUser?) {

        user?.sendEmailVerification()
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(), "Verification email sent",
                        Toast.LENGTH_SHORT
                    ).show()
                    val userData = hashMapOf(
                        Endpoints.User.EMAIL to binding.etEmail.text.toString(),
                        Endpoints.User.DETAILS_ADDED to false,
                        Endpoints.User.PHOTO_ADDED to false,
                        Endpoints.User.DP_URL to "",
                        Endpoints.User.EMAIL_VERIFIED to false,
                    )

                    fcmToken { token->
                        userData.set(Endpoints.User.FCM_TOKEN,token)

                        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser?.email!!)
                            .set(userData)
                            .addOnSuccessListener {
                                findNavController().navigate(R.id.action_loginScreenFragment_to_EmailConfirmationFragment)
                            }
                            .addOnFailureListener { e ->

                            }
                    }
                } else {
                    Toast.makeText(
                        requireContext(), "Please wait a moment",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.buttonParent.gone()
                    binding.timerParent.visible()
                    val countdownTimer = object : CountDownTimer(60*1000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val secondsRemaining = millisUntilFinished / 1000
                            binding.timer.text="$secondsRemaining sec"
                        }

                        override fun onFinish() {
                            binding.buttonParent.visible()
                            binding.timerParent.gone()
                            binding.progressbar.gone()
                            binding.btnLogin.text="Login"
                            binding.btnLogin.isEnabled=true
                            binding.btnLogin.isClickable=true
                        }
                    }

                    countdownTimer.start()
                }
            }
    }

    suspend fun getProjectSegments(project: String): List<SegmentItem> {
        val projectsCollection =  FirebaseFirestore.getInstance().collection(Endpoints.PROJECTS)
        val list = mutableListOf<SegmentItem>()

        try {
            val projectsSnapshot = projectsCollection.get().await()
            for (projectDocument in projectsSnapshot.documents) {
                val projectName = projectDocument.id
                val segmentsCollection = projectsCollection.document(project).collection(Endpoints.Project.SEGMENT)
                val segmentsSnapshot = segmentsCollection.get().await()
                for (segmentDocument in segmentsSnapshot.documents) {
                    val segmentName = segmentDocument.id
                    val sections=segmentDocument.get("sections") as MutableList<String>
                    val segment_ID= segmentDocument.getString("segment_ID")
                    val creation_DATETIME= segmentDocument.get("creation_DATETIME") as Timestamp

                    list.add(SegmentItem(segment_NAME = segmentName, sections = sections, segment_ID = segment_ID!!, creation_DATETIME = creation_DATETIME!!))
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return list
    }


    private fun fcmToken(token: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.tag(SignUpScreenFragment.TAG)
                    .w(task.exception, "Fetching FCM registration token failed")
                return@addOnCompleteListener
            }

            val tokenFCM = task.result
            Timber.tag(SignUpScreenFragment.TAG).d("FCM registration token: %s", token)
            Timber.tag("FCM TOKEN").d("FCM registration %s", token)
            token(tokenFCM)
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpBackPress()
    }

    private fun setUpBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_loginScreenFragment_to_chooserFragment)
        }
    }


    private fun setUpViews() {

        setUpVisibilities()

        binding.forgotPassword.setOnClickThrottleBounceListener {
            findNavController().navigate(R.id.action_loginScreenFragment_to_resetPasswordFragment)
        }

        binding.signUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreenFragment_to_signUpScreenFragment)

            viewModel.emailError.observe(requireActivity()) { error ->
                binding.etEmail.error = error
            }

            viewModel.passwordError.observe(requireActivity()) { error ->
                binding.etPass.error = error
            }

        }


        binding.toolbar.btnBack.setOnClickThrottleBounceListener {
            findNavController().navigate(R.id.action_loginScreenFragment_to_chooserFragment)
        }

        binding.btnLogin.setOnClickThrottleBounceListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPass.text.toString()
            viewModel.validateInput(
                email = email,
                password = pass
            )
        }

    }



    private fun setUpVisibilities() {
        binding.progressbar.gone()
    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        val methodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.hideSoftInputFromWindow(
            view!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    fun showKeyboardThis(activity: Activity) {
        val view = activity.currentFocus
        val methodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

}