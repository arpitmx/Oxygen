package com.ncs.o2.UI.Auth.SignupScreen.ProfilePictureScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ncs.o2.R

class ProfilePictureSelectionFragment : Fragment() {

    companion object {
        fun newInstance() = ProfilePictureSelectionFragment()
    }

    private lateinit var viewModel: ProfilePictureSelectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_picture_selection, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfilePictureSelectionViewModel::class.java)
        // TODO: Use the ViewModel
    }

}