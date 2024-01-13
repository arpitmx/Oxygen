package com.ncs.o2.UI.Auth

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.Issue
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.ChooserScreen.ChooserFragment
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.ActivityAuthScreenBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthScreenActivity @Inject constructor() : AppCompatActivity() {

    private val binding: ActivityAuthScreenBinding by lazy {
        ActivityAuthScreenBinding.inflate(layoutInflater)
    }


    @Issue("Fragment duplicate on configuration change, implement that.")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val isDetailsAdded = intent.getStringExtra("isDetailsAdded")
        val isPhotoAdded = intent.getStringExtra("isPhotoAdded")
        val isEmailVerified = intent.getStringExtra("isEmailVerified")

        val showchooser = intent.getStringExtra("showchooser")

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navhost) as NavHostFragment
        val navController = navHostFragment.navController
        if (isEmailVerified=="false"){
            val user=FirebaseAuth.getInstance().currentUser
            Log.d("userCheck",user!!.email.toString())
            if (user.isEmailVerified){
                navController.navigate(R.id.userDetailsFragment)
            }
            else if(!user.isEmailVerified){
                navController.navigate(R.id.loginScreenFragment)
            }
            else{
                sendVerificationEmail(user,navController)
            }
        }
        else{
            if (isDetailsAdded=="false" && showchooser=="false"){
                navController.navigate(R.id.userDetailsFragment)
            }
            else if(isPhotoAdded=="false" && showchooser=="false"){
                navController.navigate(R.id.profilePictureSelectionFragment)
            }
            else if(isPhotoAdded=="false" && isDetailsAdded=="false" && showchooser=="false"){
                navController.navigate(R.id.userDetailsFragment)
            }
            else if (isPhotoAdded=="false" && isDetailsAdded=="false" && showchooser=="true"){
                navController.navigate(R.id.chooserFragment)
            }
        }
        setUpViews()

    }
    private fun sendVerificationEmail(user: FirebaseUser?,navController:NavController) {

        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this, "Verification email sent",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate(R.id.emailConfirmationFragment)

                } else {
                    Toast.makeText(
                        this, "Failed to send verification email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun setUpViews() {

    }

}