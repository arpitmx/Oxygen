package com.ncs.o2.UI.UIComponents.BottomSheets
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toLowerCase

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.CreateProject
import com.ncs.o2.UI.UIComponents.Adapters.SegmentListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment.CreateSegmentBottomSheet
import com.ncs.o2.databinding.ProjectAddBottomSheetBinding
import com.ncs.versa.Constants.Endpoints
import org.json.JSONArray
import org.json.JSONException

class AddProjectBottomSheet : BottomSheetDialogFragment(){

    lateinit var binding:ProjectAddBottomSheetBinding
    var projectAddedListener: ProjectAddedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProjectAddBottomSheetBinding.inflate(inflater, container, false)

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
        PrefManager.initialize(requireContext())
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }

        binding.addProject.setOnClickThrottleBounceListener {
            startActivity(Intent(requireContext(),CreateProject::class.java))
            dismiss()
        }
        binding.submitLink.setOnClickThrottleBounceListener {
            val link = binding.projectLink.text.toString()

            if (link.isNotEmpty()) {
                binding.submitLink.gone()
                binding.progressBar.visible()

                val userDocument = FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().currentUser?.email!!)

                var _projectData: String? = null
                var projectData: String? = null

                FirebaseFirestore.getInstance().collection("Projects")
                    .whereEqualTo("PROJECT_LINK", link.toLowerCase())
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            for (document in documents) {
                                val project = document.data
                                projectData = project.get("PROJECT_NAME").toString()
                            }

                            if (projectData != null) {
                                userDocument.get()
                                    .addOnSuccessListener { userSnapshot ->
                                        val userProjects = userSnapshot.get("PROJECTS") as ArrayList<String>?


                                        if (userProjects != null && userProjects.contains(projectData)) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Project already added in your account",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            dismiss()
                                        } else {
                                            userDocument.update("PROJECTS", FieldValue.arrayUnion(projectData))
                                                .addOnSuccessListener {
                                                    PrefManager.lastaddedproject(projectData!!)
                                                    userProjects?.add(projectData!!.trim())
                                                    sendcallBack(userProjects!!)
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Project Added Successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    projectAddedListener?.onProjectAdded(userProjects!!)
                                                    dismiss()
                                                }
                                                .addOnFailureListener { e ->
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                    }
                            } else {
                                Toast.makeText(requireContext(), "Project not found, please check the link", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Project not found, please check the link", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                    }
            } else {
                Toast.makeText(requireContext(), "Project Link can't be empty", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setBottomSheetConfig() {
        this.isCancelable = true
    }
    interface ProjectAddedListener {
        fun onProjectAdded(userProjects:ArrayList<String>)
    }
    fun sendcallBack(userProjects: ArrayList<String>){
        projectAddedListener?.onProjectAdded(userProjects)
    }

}