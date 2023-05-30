package com.ncs.o2.UI.Assigned

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ncs.o2.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AssignedFragment : Fragment() {

    companion object {
        fun newInstance() = AssignedFragment()
    }

    private lateinit var viewModel: AssignedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_assigned, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AssignedViewModel::class.java)
        // TODO: Use the ViewModel
    }

}