package com.ncs.o2.UI.UIComponents.BottomSheets
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.UI.CreateProject
import com.ncs.o2.UI.UIComponents.Adapters.SegmentListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentBottomSheet
import com.ncs.o2.databinding.ProjectAddBottomSheetBinding
import org.json.JSONArray
import org.json.JSONException

class AddProjectBottomSheet : BottomSheetDialogFragment(){

    lateinit var binding:ProjectAddBottomSheetBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProjectAddBottomSheetBinding.inflate(inflater, container, false)
        sharedPref = requireContext().getSharedPreferences("userDetails", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPref.edit()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private fun setViews() {


        setBottomSheetConfig()
        setActionbar()

    }

    private fun setActionbar() {
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }

        binding.addProject.setOnClickThrottleBounceListener {
            startActivity(Intent(requireContext(),CreateProject::class.java))
            dismiss()
        }
        binding.submitLink.setOnClickThrottleBounceListener {
            val link=binding.projectLink.text.toString()
            if (link.isNotEmpty()){
                binding.submitLink.gone()
                binding.progressBar.visible()

                FirebaseFirestore.getInstance().collection("Projects").whereEqualTo("PROJECT_LINK", link)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            var projectData=""
                            val sharedPrefData = sharedPref.getString("PROJECTS", "[]")

                            for (document in documents) {
                                val project = document.data
                                projectData= project.get("PROJECT_ID").toString()
                                Log.d("project",project.toString())
                            }
                            FirebaseFirestore.getInstance().collection("Users")
                                .document(FirebaseAuth.getInstance().currentUser?.email!!)
                                .update("PROJECTS", FieldValue.arrayUnion(projectData))
                                .addOnSuccessListener {
                                    try {
                                        val projectsArray = JSONArray(sharedPrefData)
                                        projectsArray.put(projectData)
                                        val editor = sharedPref.edit()
                                        editor.putString("PROJECTS", projectsArray.toString())
                                        editor.apply()
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                    Toast.makeText(requireContext(), "Project Added Successfully", Toast.LENGTH_SHORT).show()
                                    dismiss()
                                }
                                .addOnFailureListener { e ->
                                }
                        } else {
                            Toast.makeText(requireContext(), "Project not found, please check the link", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                    }
            }
            else{
                Toast.makeText(requireContext(),"Project Link can't be empty",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }

}