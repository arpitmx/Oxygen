package com.ncs.o2.UI.Auth.SignupScreen.UserDetailsScreen

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.ncs.o2.Constants.Errors
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
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

            if (!validate(binding.etUsername)){
                binding.etUsername.error="Username can't be empty"
            }
            if (!validate(binding.etName)){
                binding.etName.error="Name can't be empty"
            }
            if (!validate(binding.etDesignation)){
                binding.etDesignation.error="Designation can't be empty"
            }
            if (!validate(binding.etBio)){
                binding.etBio.error="Bio can't be empty"
            }
            if (binding.error.text!="VALID"){
                binding.etUsername.error="Re-check your username"
            }
            else{

                isUsernameAvailable(binding.etUsername.text.toString()){
                    if (it){
                        val username=binding.etUsername.text.toString()
                        val name=binding.etName.text.toString()
                        val designation=binding.etDesignation.text.toString()
                        val bio=binding.etBio.text.toString()

                        val userData = mutableMapOf(
                            Endpoints.User.USERNAME to username.trim(),
                            Endpoints.User.FULLNAME to name.trim(),
                            Endpoints.User.DESIGNATION to designation.trim(),
                            Endpoints.User.BIO to bio.trim(),
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

            }
        }
        initViews()
    }
    fun isUsernameAvailable(username: String, usernameResult: (Boolean) -> Unit) {
        Log.d("usernameCheck",username)
        binding.progressbar.visible()
        FirebaseFirestore.getInstance().collection(Endpoints.USERS)
            .whereEqualTo("USERNAME", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    usernameResult(false)
                    binding.progressbar.gone()
                    binding.error.setTextColor(resources.getColor(R.color.redx))
                    binding.error.text="This username is already used"
                    binding.error.visible()

                } else {
                    usernameResult(true)
                    binding.progressbar.gone()
                    binding.error.text="This username is available"
                    binding.error.visible()
                    binding.error.setTextColor(resources.getColor(R.color.green))


                }
            }
            .addOnFailureListener { exception ->
                binding.progressbar.gone()
                usernameResult(false)
            }
    }

    private fun validate(et: EditText) : Boolean{
        return !et.text.isNullOrEmpty()
    }
    private fun initViews(){
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val containsSpecialChar = s?.any { it.isLetterOrDigit().not() && it != '_'} ?: false

                if (containsSpecialChar) {
                    binding.error.text = "Special characters are not allowed (except underscore)"
                    binding.error.visible()
                } else {
                    binding.error.gone()
                    binding.error.text="VALID"
                }

                val characterCount = s?.length ?: 0
                binding.count.text = "$characterCount / 16"
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
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