package com.ncs.o2.UI.Auth.SignupScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SignUpScreenFragment @Inject constructor() : Fragment() {

    companion object {
        const val TAG = "SignUpScreenFragment"
        fun newInstance() = SignUpScreenFragment()
    }

    private val viewModel: SignUpViewModel by viewModels()
    lateinit var binding: FragmentSignUpBinding

    lateinit var authResource: LiveData<ServerResult<FirebaseUser>?>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews()
        setUpValidation()

    }

    private fun setUpValidation() {
        handleValidation()
        authResource = viewModel.signupLiveData
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewModel.validationEvents.collect { event ->
                when (event) {
                    SignUpViewModel.ValidationEvent.Success -> {

                        authResource.let { liveData ->
                            liveData.observe(viewLifecycleOwner) { result ->

                                when (result) {
                                    is ServerResult.Failure -> {

                                        binding.progressbar.gone()
                                        binding.btnSignup.isEnabled = true
                                        binding.btnSignup.isClickable = true
                                        binding.btnSignup.text = getString(R.string.register)
                                        Toast.makeText(
                                            activity,
                                            "Registration Failed : ${result.exception.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Timber.tag(TAG)
                                            .d("Registration Failed : ${result.exception.message}")

                                    }

                                    ServerResult.Progress -> {

                                        binding.progressbar.visible()
                                        binding.btnSignup.isEnabled = false
                                        binding.btnSignup.isClickable = false
                                        binding.btnSignup.text = getString(R.string.hold_on)

                                    }

                                    is ServerResult.Success -> {

                                        binding.progressbar.gone()
                                        Toast.makeText(
                                            activity,
                                            "Registration success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Timber.tag(TAG).d(
                                            "Registration success : ${result.data.uid}  ${result.data}"
                                        )
                                    }

                                    else -> {}
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private fun handleValidation() {
        binding.btnSignup.setOnClickThrottleBounceListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPass.text.toString()
            val confirmPass = binding.etConfirmationPass.text.toString()
            viewModel.validateInput(
                email = email,
                password = pass,
                repeatedPassword = confirmPass
            )
        }
    }


    private fun setUpViews() {

        setVisibilities()

        binding.login.setOnClickListener {
            findNavController().navigate(R.id.action_signUpScreenFragment_to_loginScreenFragment)
        }

        binding.toolbar.btnBack.setOnClickThrottleBounceListener {
            findNavController().navigate(R.id.action_signUpScreenFragment_to_chooserFragment)
        }

        viewModel.emailError.observe(requireActivity()) { error ->
            binding.etEmail.error = error
        }

        viewModel.passwordError.observe(requireActivity()) { error ->
            binding.etPass.error = error
        }

        viewModel.repeatpasswordError.observe(requireActivity()) { error ->
            binding.etConfirmationPass.error = error
        }

    }

    private fun setVisibilities() {
        binding.progressbar.gone()
    }


}