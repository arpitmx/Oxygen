package com.ncs.o2.UI.Tasks.TaskPage.Details

import android.annotation.SuppressLint
import android.app.SharedElementCallback
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.igreenwood.loupe.Loupe
import com.igreenwood.loupe.extensions.createLoupe
import com.igreenwood.loupe.extensions.setOnViewTranslateListener
import com.ncs.o2.Constants.Pref
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityImageViewerBinding
import com.ncs.o2.databinding.ViewpagerImageItemBinding

class ImageViewerActivity : AppCompatActivity() {

    companion object {
        private const val ARGS_IMAGE_URLS = "ARGS_IMAGE_URLS"
        private const val ARGS_INITIAL_POSITION = "ARGS_INITIAL_POSITION"


        fun createIntent(context: Context, urls: ArrayList<String>, initialPos: Int): Intent {
            return Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(ARGS_IMAGE_URLS, urls)
                putExtra(ARGS_INITIAL_POSITION, initialPos)
            }
        }
    }

    val binding: ActivityImageViewerBinding by lazy {
        ActivityImageViewerBinding.inflate(layoutInflater)
    }
    @Suppress("UNCHECKED_CAST")
    private val urls: List<String> by lazy { intent.getSerializableExtra(ARGS_IMAGE_URLS) as List<String> }
    private val initialPos: Int by lazy { intent.getIntExtra(ARGS_INITIAL_POSITION, 0) }
    private var adapter: ImageAdapter? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Pref.useSharedElements) {
            postponeEnterTransition()
        }
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        binding.btnBack.setOnClickThrottleBounceListener{
            onBackPressed()
        }

        initViewPager()


    }

    override fun onBackPressed() {
        adapter?.clear()
        super.onBackPressed()
    }

    override fun onDestroy() {
        adapter = null
        super.onDestroy()
    }

    private fun initViewPager() {
        binding.viewPager.visible()
        binding.cont.gone()
        adapter = ImageAdapter(this, urls)
        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = initialPos
    }


    override fun onSupportNavigateUp(): Boolean {
        if (Pref.useSharedElements) {
            supportFinishAfterTransition()
        } else {
            finish()
        }
        return true
    }

    override fun finish() {
        super.finish()
        if (!Pref.useSharedElements) {
            overridePendingTransition(0, R.anim.fade_out_fast)
        }
    }

    inner class ImageAdapter(var context: Context, var urls: List<String>) : PagerAdapter() {

        private var loupeMap = hashMapOf<Int, Loupe>()
        private var views = hashMapOf<Int, ImageView>()
        private var currentPos = 0

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val binding = ViewpagerImageItemBinding.inflate(LayoutInflater.from(context))
            container.addView(binding.root)
            loadImage(binding.image, binding.container, position,binding.progressBar)
            views[position] = binding.image
            return binding.root
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            super.setPrimaryItem(container, position, obj)
            this.currentPos = position
        }

        override fun getCount() = urls.size

        private fun loadImage(image: ImageView, container: ViewGroup, position: Int,progressBar: ProgressBar) {
            if (Pref.useSharedElements) {
                // shared elements

                    Glide.with(image.context)
                        .load(urls[position])
                        .onlyRetrieveFromCache(true)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                startPostponedEnterTransition()
                                progressBar.gone()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                progressBar.gone()
                                image.transitionName =
                                    context.getString(R.string.shared_image_transition, position)

                                val loupe = createLoupe(image, container) {
                                    useFlingToDismissGesture = !Pref.useSharedElements
                                    maxZoom = Pref.maxZoom
                                    flingAnimationDuration = Pref.flingAnimationDuration
                                    scaleAnimationDuration = Pref.scaleAnimationDuration
                                    overScaleAnimationDuration = Pref.overScaleAnimationDuration
                                    overScrollAnimationDuration = Pref.overScrollAnimationDuration
                                    dismissAnimationDuration = Pref.dismissAnimationDuration
                                    restoreAnimationDuration = Pref.restoreAnimationDuration
                                    viewDragFriction = Pref.viewDragFriction

                                    setOnViewTranslateListener(
                                        onStart = { },
                                        onRestore = { },
                                        onDismiss = { finishAfterTransition() }
                                    )
                                }

                                loupeMap[position] = loupe

                                if (position == initialPos) {
                                    setEnterSharedElementCallback(object : SharedElementCallback() {
                                        override fun onMapSharedElements(
                                            names: MutableList<String>?,
                                            sharedElements: MutableMap<String, View>?
                                        ) {
                                            names ?: return
                                            val view = views[currentPos] ?: return
                                            val currentPosition: Int = currentPos
                                            view.transitionName = context.getString(
                                                R.string.shared_image_transition,
                                                currentPosition
                                            )
                                            sharedElements?.clear()
                                            sharedElements?.put(view.transitionName, view)
                                        }
                                    })
                                    startPostponedEnterTransition()
                                }
                                return false
                            }

                        })
                        .error(R.drawable.placeholder_image)
                        .into(image)
            } else {
                // swipe to dismiss
                Glide.with(image.context).load(urls[position])
                    .onlyRetrieveFromCache(true)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.gone()
                            startPostponedEnterTransition()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.gone()
                            val loupe = createLoupe(image, container) {
                                useFlingToDismissGesture = !Pref.useSharedElements
                                maxZoom = Pref.maxZoom
                                flingAnimationDuration = Pref.flingAnimationDuration
                                scaleAnimationDuration = Pref.scaleAnimationDuration
                                overScaleAnimationDuration = Pref.overScaleAnimationDuration
                                overScrollAnimationDuration = Pref.overScrollAnimationDuration
                                dismissAnimationDuration = Pref.dismissAnimationDuration
                                restoreAnimationDuration = Pref.restoreAnimationDuration
                                viewDragFriction = Pref.viewDragFriction

                                setOnViewTranslateListener(
                                    onStart = {  },
                                    onRestore = {},
                                    onDismiss = { finish() }
                                )
                            }

                            loupeMap[position] = loupe
                            if (position == initialPos) {
                                startPostponedEnterTransition()
                            }
                            return false
                        }

                    })
                    .error(R.drawable.placeholder_image)
                    .into(image)
            }
        }

        fun clear() {
            loupeMap.forEach {
                val loupe = it.value
                loupe.cleanup()
            }
            loupeMap.clear()
        }
    }
}