package com.ncs.o2.UI.Auth.SignupScreen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.runDelayed
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.O2Application
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.FragmentEmailConfirmationBinding
import com.ncs.o2.databinding.FragmentSignUpBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class EmailConfirmationFragment @Inject constructor() : Fragment() {

    companion object {
        const val TAG = "EmailConfirmationFragment"
        fun newInstance() = EmailConfirmationFragment()
    }

    @Inject
    lateinit var utils : GlobalUtils.EasyElements

    lateinit var binding: FragmentEmailConfirmationBinding

    private val auth = FirebaseAuth.getInstance()
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var shouldSetUpAuthStateListener = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmailConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        binding.check.setOnClickThrottleBounceListener {
            setUpAuthStateListener()
        }
    }

    override fun onStop() {
        super.onStop()
        shouldSetUpAuthStateListener = true
    }

    override fun onStart() {
        super.onStart()
        if (shouldSetUpAuthStateListener) {
            setUpAuthStateListener()
            shouldSetUpAuthStateListener = false
        }
    }
    private fun setUpAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            user?.reload()?.addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful) {
                    checkAndNavigateIfEmailVerified(user)
                }
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        auth.removeAuthStateListener(authStateListener!!)
    }

    private fun checkAndNavigateIfEmailVerified(user: FirebaseUser?) {
        if (user != null && user.isEmailVerified) {
            Toast.makeText(
                activity,
                "Email Verified",
                Toast.LENGTH_SHORT
            ).show()

            FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().currentUser?.email!!)
                .update(Endpoints.User.EMAIL_VERIFIED, true)
                .addOnSuccessListener {
                     findNavController().navigate(R.id.action_emailConfirmationFragment_to_userDetailsFragment)
                }
                .addOnFailureListener { e ->

                }
        }
        else{
            toast("Email not verified yet, check link in your inbox")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpBackPress()
    }


    private fun setUpViews(){
        val user=FirebaseAuth.getInstance().currentUser
        if (user!=null){
            binding.emailLabel.text="Check your email ${user.email}\n for a confirmation mail"
            runDelayed(30*1000){
                binding.resend.visible()
            }
            runDelayed(10*1000){
                binding.check.visible()
            }
            binding.btnresend.setOnClickThrottleBounceListener {
                sendVerificationEmail(user)
            }
            binding.login.setOnClickThrottleBounceListener {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(R.id.action_emailConfirmationFragment_to_loginScreenFragment)

            }
            if (user.isEmailVerified){
                Toast.makeText(
                    activity,
                    "Email Verified",
                    Toast.LENGTH_SHORT
                ).show()


                FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser?.email!!)
                    .update(Endpoints.User.EMAIL_VERIFIED , true)
                    .addOnSuccessListener {
                        findNavController().navigate(R.id.action_emailConfirmationFragment_to_userDetailsFragment)
                    }
                    .addOnFailureListener { e ->

                    }
            }
        }
    }


    private fun setUpBackPress() {

    }
    private fun sendVerificationEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(), "Verification email sent",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(), "Failed to send verification email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}