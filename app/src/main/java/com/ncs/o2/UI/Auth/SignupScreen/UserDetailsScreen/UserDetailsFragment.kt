package com.ncs.o2.UI.Auth.SignupScreen.UserDetailsScreen

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.databinding.ChooseDesignationBottomSheetBinding
import com.ncs.o2.databinding.FragmentUserDetailsBinding

@Suppress("DEPRECATION")
class UserDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = UserDetailsFragment()
    }
    lateinit var binding: FragmentUserDetailsBinding
    private lateinit var viewModel: UserDetailsViewModel
    private lateinit var bottomSheetBinding: ChooseDesignationBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserDetailsBinding.inflate(inflater, container, false)

        binding.btnLogin.setOnClickThrottleBounceListener{
            val name=binding.etName.text.toString()
            val designation=binding.etDesignation.text.toString()
            val bio=binding.etBio.text.toString()

            val userData = mapOf(
                "username" to name,
                "designation" to designation,
                "bio" to bio,
                "ROLE" to 1,
                "DETAILS_ADDED" to true,
                "PHOTO_ADDED" to false,
                )

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etDesignation.setOnClickListener {
            hideKeyboard()
            setUpBottomDialog()
        }
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

    private fun setUpBottomDialog(){
        bottomSheetBinding = ChooseDesignationBottomSheetBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())


        bottomSheetBinding.tvDesigner.setOnClickListener {
            binding.etDesignation.setText(R.string.designer)
            dialog.dismiss()
        }

        bottomSheetBinding.tvAndroidDeveloper.setOnClickListener {
            binding.etDesignation.setText(R.string.android_developer)
            dialog.dismiss()
        }

        bottomSheetBinding.tvWebDeveloper.setOnClickListener {
            binding.etDesignation.setText(R.string.web_developer)
            dialog.dismiss()
        }

        bottomSheetBinding.tvProgrammer.setOnClickListener {
            binding.etDesignation.setText(R.string.programmer)
            dialog.dismiss()
        }

        bottomSheetBinding.tvBackendDeveloper.setOnClickListener {
            binding.etDesignation.setText(R.string.backend_developer)
            dialog.dismiss()
        }

        bottomSheetBinding.closeBottmSheet.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(false)
        dialog.setContentView(bottomSheetBinding.root)
        dialog.show()

    }

    private fun hideKeyboard(){
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

}