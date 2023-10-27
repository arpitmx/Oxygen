package com.ncs.o2.UI.Auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.ncs.o2.Domain.Utility.Issue
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.ChooserScreen.ChooserFragment
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.ActivityAuthScreenBinding
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
        val showchooser = intent.getStringExtra("showchooser")

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navhost) as NavHostFragment
        val navController = navHostFragment.navController
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
        setUpViews()

    }

    private fun setUpViews() {

    }
}