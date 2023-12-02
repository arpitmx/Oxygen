package com.ncs.o2.UI.CreateTask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Chat.ExampleGrammarLocator
import com.ncs.o2.UI.UIComponents.Adapters.CheckListAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.CheckListBottomSheet
import com.ncs.o2.databinding.ActivityChecklistBinding
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


@AndroidEntryPoint
class ChecklistActivity() : AppCompatActivity(),CheckListBottomSheet.checkListItemListener,CheckListAdapter.CheckListItemListener {

    private val binding: ActivityChecklistBinding by lazy {
        ActivityChecklistBinding.inflate(layoutInflater)
    }
    private val checkList_rv: RecyclerView by lazy {
        binding.checkListRecyclerview
    }
    private lateinit var listener: checkListListener

    private var list:MutableList<CheckList> = mutableListOf()
    lateinit var checkListAdapter:CheckListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        listener = ListenerHolder.checkListListener!!
        val dataList = intent.getSerializableExtra("checkListArray") as ArrayList<CheckList>?
        list.addAll(dataList!!)
        initViews()
    }
    private fun initViews(){
        list.distinctBy { it.id }
        if (list?.isEmpty()!!){
            binding.checkListRecyclerview.gone()
            binding.noCheckList.visible()
            binding.addText.text="Add a new checkList"
        }
        if (list.isNotEmpty()){
            binding.checkListRecyclerview.visible()
            binding.noCheckList.gone()
            binding.addText.text="Add More"
            setCheckListRecyclerView(list)
        }
        binding.addCheckList.setOnClickThrottleBounceListener{
            val number=list.size+1
            val checkListBottomSheet = CheckListBottomSheet(count = number,this, CheckList(id = ""))
            checkListBottomSheet.show(supportFragmentManager, "checkList")
        }
        binding.gioActionbar.btnNext.setOnClickListener {
            listener.sendcheckListarray(list)
            finish()
        }
        binding.gioActionbar.btnCross.setOnClickListener {
            finish()
        }
    }

    private fun setCheckListRecyclerView(list: MutableList<CheckList>) {
        checkListAdapter = CheckListAdapter(list = list,markwon= markwon,this,true)
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        with(checkList_rv) {
            this.layoutManager = layoutManager
            adapter = checkListAdapter
            edgeEffectFactory = BounceEdgeEffectFactory()
        }
    }

    override fun checkListItem(checkList: CheckList, isEdited: Boolean) {
        val existingIndex = list.indexOfFirst { it.id == checkList.id }

        if (existingIndex != -1) {
            list[existingIndex] = checkList
        } else {
            list.add(checkList)
        }

        initViews()
    }

    interface checkListListener{
        fun sendcheckListarray(list: MutableList<CheckList>)
    }

    object ListenerHolder {
        var checkListListener: checkListListener? = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    private val markwon: Markwon by lazy {
        val prism4j = Prism4j(ExampleGrammarLocator())
        val activity = this
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

    override fun removeCheckList(position: Int) {
        list.removeAt(position)
        toast("Removed")
        initViews()
    }

    override fun onClick(position: Int) {
        val number=position+1
        val checkListBottomSheet = CheckListBottomSheet(count = number,this, checkList = list[position])
        checkListBottomSheet.show(supportFragmentManager, "checkList")
    }

    override fun onCheckBoxClick(id: String, isChecked: Boolean, position: Int) {

    }
}