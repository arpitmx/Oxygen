package com.ncs.o2.UI.Auth.SignupScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.ncs.o2.Domain.Utility.ExtensionsUtil.invisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.runDelayed
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.databinding.FragmentResetPasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ResetPasswordFragment @Inject constructor() : Fragment() {

    companion object {
        const val TAG = "ResetPasswordFragment"
        fun newInstance() = ResetPasswordFragment()
    }



    lateinit var binding: FragmentResetPasswordBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        setUpBackPress()
        super.onActivityCreated(savedInstanceState)
    }


    private fun setUpViews(){
        binding.resend.setOnClickThrottleBounceListener {
            binding.resend.invisible()
            binding.instructions.invisible()
            binding.containerLayout2.visible()
        }
        binding.check.setOnClickThrottleBounceListener {
            if (binding.etEmail.text!!.isNotBlank()){
                binding.progressbar.visible()
                sendResetPasswordEmail(binding.etEmail.text.toString())

            }else{
                toast("Email is required")
            }
        }
        binding.login.setOnClickThrottleBounceListener {
            moveBacktoLogin()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun setUpBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            moveBacktoLogin()
        }
    }
    private fun sendResetPasswordEmail(email: String) {
        val auth=FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.progressbar.invisible()
                    binding.containerLayout2.invisible()
                    binding.instructions.visible()
                    binding.instructions.text="Sent a verification mail to \n $email \n \n Follow instructions in mail to reset your password"
                    runDelayed(60*1000){
                        binding.resend.visible()
                    }
                    toast("Reset password email sent")
                } else {
                    toast("Failed to send reset email. Please check your email and try again")
                }
            }
    }
    private fun moveBacktoLogin(){
        findNavController().navigate(R.id.action_resetPasswordFragment_to_loginScreenFragment)
    }

}