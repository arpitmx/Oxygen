package com.ncs.o2.UI.Tasks.TaskPage.Checklist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ncs.o2.databinding.FragmentTaskChatBinding
import com.ncs.o2.databinding.FragmentTaskChecklistBinding


class TaskCheckListFragment : Fragment() {

    lateinit var binding: FragmentTaskChecklistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskChecklistBinding.inflate(inflater, container, false)
        return binding.root
    }


}