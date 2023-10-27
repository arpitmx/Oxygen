package com.ncs.o2.UI.Auth.SignupScreen.UserDetailsScreen

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.databinding.FragmentSignUpBinding
import com.ncs.o2.databinding.FragmentUserDetailsBinding

@Suppress("DEPRECATION")
class UserDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = UserDetailsFragment()
    }
    lateinit var binding: FragmentUserDetailsBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var viewModel: UserDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        sharedPref = requireContext().getSharedPreferences("userDetails", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPref.edit()
        binding.btnLogin.setOnClickThrottleBounceListener{
            val name=binding.etName.text.toString()
            val designation=binding.etDesignation.text.toString()
            val bio=binding.etBio.text.toString()

            val userData = mapOf(
                "USERNAME" to name,
                "DESIGNATION" to designation,
                "BIO" to bio,
                "ROLE" to 1,
                "DETAILS_ADDED" to true,
                "PHOTO_ADDED" to false,
                )
            editor.putString("USERNAME", name)
            editor.putString("DESIGNATION", designation)
            editor.putString("BIO", bio)
            editor.putString("EMAIL", FirebaseAuth.getInstance().currentUser?.email!!)
            editor.putInt("ROLE", 1)
            editor.apply()
            editor.commit()
            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser?.email!!)
                .update(userData)
                .addOnSuccessListener {
                    findNavController().navigate(R.id.action_userDetailsFragment_to_profilePictureSelectionFragment)
                }
                .addOnFailureListener { e ->

                }
        }
        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[UserDetailsViewModel::class.java]

        disableBackPress()

    }

    private fun disableBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            Toast.makeText(requireContext(), "Registration details are immutable once registered.", Toast.LENGTH_SHORT).show()
        }
    }

}