package com.ncs.o2.UI.UIComponents.BottomSheets
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.createProject.CreateProject
import com.ncs.o2.databinding.ProjectAddBottomSheetBinding
import com.ncs.versa.Constants.Endpoints

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
        binding.closeBtn.setOnClickThrottleBounceListener{
            dismiss()
        }

        binding.addProject.setOnClickThrottleBounceListener {
            startActivity(Intent(requireContext(), CreateProject::class.java))
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
                    .whereEqualTo("PROJECT_LINK", link)
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
                                                    val addCont=mapOf<String, Any>(
                                                        "contributors" to FieldValue.arrayUnion(FirebaseAuth.getInstance().currentUser?.email)
                                                    )
                                                    FirebaseFirestore.getInstance().collection(
                                                        Endpoints.PROJECTS).document(projectData!!).update(addCont)


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