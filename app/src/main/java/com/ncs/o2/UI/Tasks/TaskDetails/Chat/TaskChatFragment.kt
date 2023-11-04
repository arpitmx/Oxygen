package com.ncs.o2.UI.Tasks.TaskDetails.Chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ncs.o2.R
import com.ncs.o2.databinding.FragmentTaskChatBinding
import com.ncs.o2.databinding.FragmentTaskDetailsFrgamentBinding


class TaskChatFragment : Fragment() {

    lateinit var binding: FragmentTaskChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskChatBinding.inflate(inflater, container, false)
        return binding.root
    }


}