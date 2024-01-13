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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.ncs.o2.Constants.Errors
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.SignupScreen.SignUpScreenFragment
import com.ncs.o2.databinding.ChooseDesignationBottomSheetBinding
import com.ncs.o2.databinding.FragmentUserDetailsBinding
import com.ncs.versa.Constants.Endpoints
import timber.log.Timber
import javax.inject.Inject

@Suppress("DEPRECATION")
class UserDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = UserDetailsFragment()
    }

    @Inject
    lateinit var util : GlobalUtils.EasyElements
    lateinit var binding: FragmentUserDetailsBinding
    private lateinit var viewModel: UserDetailsViewModel
    private lateinit var bottomSheetBinding: ChooseDesignationBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        setUpViews()
        return binding.root
    }

    private fun setUpViews(){
        binding.btnLogin.setOnClickThrottleBounceListener{
            val name=binding.etName.text.toString()
            val designation=binding.etDesignation.text.toString()
            val bio=binding.etBio.text.toString()

            val userData = mutableMapOf(
                Endpoints.User.USERNAME to name,
                Endpoints.User.DESIGNATION to designation,
                Endpoints.User.BIO to bio,
                Endpoints.User.ROLE to 1,
                Endpoints.User.EMAIL_VERIFIED to true,
                Endpoints.User.DETAILS_ADDED to true,
                Endpoints.User.PHOTO_ADDED to false,
                Endpoints.User.NOTIFICATION_TIME_STAMP to 0,
            )

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.tag(SignUpScreenFragment.TAG)
                        .w(task.exception, "Fetching FCM registration token failed")
                    userData[Endpoints.User.FCM_TOKEN] = Errors.AccountErrors.ACCOUNT_FIELDS_NULL.code
                    return@addOnCompleteListener
                }

                val token = task.result
                userData[Endpoints.User.FCM_TOKEN] = token
            }

            FirebaseFirestore.getInstance().collection(Endpoints.USERS).document(FirebaseAuth.getInstance().currentUser?.email!!)
                .update(userData.toMap())
                .addOnSuccessListener {
                    findNavController().navigate(R.id.action_userDetailsFragment_to_profilePictureSelectionFragment)
                }
                .addOnFailureListener { e ->
                    util.showSnackbar(binding.root,
                        "Failure is pushing data, try again",
                        15000
                    )
                }
        }
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