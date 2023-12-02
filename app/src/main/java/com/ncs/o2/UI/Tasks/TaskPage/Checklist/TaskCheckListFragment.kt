package com.ncs.o2.UI.Tasks.TaskPage.Checklist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Chat.ExampleGrammarLocator
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.UIComponents.Adapters.CheckListAdapter
import com.ncs.o2.databinding.FragmentTaskChecklistBinding
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.data.DataUriSchemeHandler
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class TaskCheckListFragment : Fragment() ,CheckListAdapter.CheckListItemListener{

    lateinit var binding: FragmentTaskChecklistBinding
    @Inject
    lateinit var utils: GlobalUtils.EasyElements
    @Inject
    lateinit var firestoreRepository: FirestoreRepository
    private val checkListArray:MutableList<CheckList> = mutableListOf()
    private val activityBinding: TaskDetailActivity by lazy {
        (requireActivity() as TaskDetailActivity)
    }
    lateinit var checkListAdapter:CheckListAdapter
    private val checkList_rv: RecyclerView by lazy {
        binding.checkListRecyclerview
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskChecklistBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
    }

    private fun initViews(){
        getCheckList()
    }

    private fun setCheckListRecyclerView(list: MutableList<CheckList>) {
        checkListAdapter = CheckListAdapter(list = list,markwon= markwon,this,false)
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        checkList_rv.visible()
        with(checkList_rv) {
            this.layoutManager = layoutManager
            adapter = checkListAdapter
            edgeEffectFactory = BounceEdgeEffectFactory()
        }
    }

    private fun getCheckList(){
        CoroutineScope(Dispatchers.Main).launch {

            firestoreRepository.getCheckList(
                projectName = PrefManager.getcurrentProject(), taskId = activityBinding.taskId) { result ->

                when (result) {

                    is ServerResult.Failure -> {
                        binding.progressbar.gone()
                        utils.singleBtnDialog(
                            "Failure", "Failure in CheckLists exception : ", "Okay"
                        ) {
                            requireActivity().finish()
                        }
                    }

                    ServerResult.Progress -> {
                        binding.progressbar.visible()
                    }

                    is ServerResult.Success -> {
                        checkListArray.clear()
                        checkListArray.addAll(result.data)
                        setCheckListRecyclerView(result.data.toMutableList())
                        binding.progressbar.gone()
                    }
                }
            }
        }
    }

    override fun removeCheckList(position: Int) {
    }

    override fun onClick(position: Int) {
    }

    override fun onCheckBoxClick(id: String, isChecked: Boolean,position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    firestoreRepository.updateCheckListCompletion(
                        taskId = activityBinding.taskId,
                        projectName = PrefManager.getcurrentProject(),
                        id = checkListArray[position].id, done = isChecked)

                }
                when (result) {
                    is ServerResult.Failure -> {

                        utils.singleBtnDialog(
                            "Failure",
                            "Failure in Updating: ${result.exception.message}",
                            "Okay"
                        ) {
                            requireActivity().finish()
                        }
                        binding.progressbar.gone()

                    }

                    is ServerResult.Progress -> {
                        binding.progressbar.visible()
                    }

                    is ServerResult.Success -> {
                        binding.progressbar.gone()
                        if (isChecked){
                            toast("Marked as completed")
                        }
                        if (!isChecked){
                            toast("Marked as not completed")

                        }

                    }

                }

            } catch (e: Exception) {

                Timber.tag(TaskDetailsFragment.TAG).e(e)
                binding.progressbar.gone()

            }
        }
    }

    private val markwon: Markwon by lazy {
        val prism4j = Prism4j(ExampleGrammarLocator())
        val activity = requireActivity()
        Markwon.builder(activity)
            .usePlugin(ImagesPlugin.create())
            .usePlugin(GlideImagesPlugin.create(activity))
            .usePlugin(TablePlugin.create(activity))
            .usePlugin(TaskListPlugin.create(activity))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDarkula.create()))

            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configure(registry: MarkwonPlugin.Registry) {
                    registry.require(ImagesPlugin::class.java) { imagesPlugin ->
                        imagesPlugin.addSchemeHandler(DataUriSchemeHandler.create())
                    }
                }
            })
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .blockQuoteColor(resources.getColor(R.color.primary))
                        .linkColor(resources.getColor(R.color.primary))
                        .codeBlockTextSize(30)
                }
            })

            .build()
    }

}