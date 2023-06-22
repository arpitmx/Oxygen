package com.ncs.o2.UI.DoneScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ncs.o2.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DoneFragment : Fragment() {

    companion object {
        fun newInstance() = DoneFragment()
    }

    private lateinit var viewModel: DoneViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_done, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DoneViewModel::class.java)
        // TODO: Use the ViewModel
    }

}