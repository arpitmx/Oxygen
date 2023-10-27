package com.ncs.o2.UI.Auth.LoginScreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.showKeyboard
import com.ncs.o2.Domain.Utility.ExtensionsUtil.showKeyboardB
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.SignupScreen.SignUpScreenFragment
import com.ncs.o2.UI.Auth.SignupScreen.SignUpViewModel
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.FragmentLoginScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@Suppress("DEPRECATION")
@AndroidEntryPoint
class LoginScreenFragment @Inject constructor(): Fragment() {

    companion object {
        fun newInstance() = LoginScreenFragment()
    }

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
            viewModel.validationEvents.collect{ event->
                when(event){
                    LoginScreenViewModel.ValidationEvent.Success -> {
                        //requireActivity().startActivity(Intent(requireContext(),MainActivity::class.java))
                        authResource.let { liveData ->
                            liveData.observe(viewLifecycleOwner){ result ->
                                handleLoginResult(result)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleLoginResult(result: ServerResult<FirebaseUser>?) {
                    when(result){
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
                            Toast.makeText(activity, "Authenticated", Toast.LENGTH_SHORT).show()
                            binding.progressbar.gone()
                            Toast.makeText(
                                activity,
                                "Login success ",
                                Toast.LENGTH_SHORT
                            ).show()
                            Timber.tag(SignUpScreenFragment.TAG).d(
                                "Login success : ${result.data.uid}"
                            )
                            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser?.email!!)
                                .get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val document = task.result
                                        if (document != null && document.exists()) {
                                            val isDetailsAdded = document.getBoolean("DETAILS_ADDED")
                                            val isPhotoAdded = document.getBoolean("PHOTO_ADDED")

                                            if (isDetailsAdded==true && isPhotoAdded==true) {
                                                startActivity(
                                                    Intent(
                                                        requireContext(),
                                                        MainActivity::class.java
                                                    )
                                                )
                                            } else if (isDetailsAdded == false) {
                                                findNavController().navigate(R.id.action_loginScreenFragment_to_userDetailsFragment)
                                            }
                                            else if (isPhotoAdded==false){
                                                findNavController().navigate(R.id.action_loginScreenFragment_to_profilePictureSelectionFragment)
                                            }
                                        }
                                    } else {
                                        val exception = task.exception
                                        exception?.printStackTrace()
                                    }
                                }

                        }
                        null -> {

                        }
                    }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpBackPress()
    }

    private fun setUpBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            findNavController().navigate(R.id.action_loginScreenFragment_to_chooserFragment)
        }
    }


    private fun setUpViews() {

        setUpVisibilities()

        binding.signUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreenFragment_to_signUpScreenFragment)

            viewModel.emailError.observe(requireActivity()){error ->
                binding.etEmail.error = error
            }

            viewModel.passwordError.observe(requireActivity()){error->
                binding.etPass.error = error
            }

        }


        binding.toolbar.btnBack.setOnClickThrottleBounceListener{
            findNavController().navigate(R.id.action_loginScreenFragment_to_chooserFragment)
        }

        binding.btnLogin.setOnClickThrottleBounceListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPass.text.toString()
            viewModel.validateInput(
                email = email,
                password = pass)
        }

    }

    private fun setUpVisibilities() {
        binding.progressbar.gone()
    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.hideSoftInputFromWindow(view!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun showKeyboardThis(activity: Activity) {
        val view = activity.currentFocus
        val methodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

}