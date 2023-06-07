package com.ncs.o2.UI.UIComponents.Adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.ncs.o2.Domain.Models.Contributor
import com.ncs.o2.R
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.databinding.ContriItemBinding

/*
File : ContributorAdapter.kt -> com.ncs.o2.Adapters
Description : Adapter for contributors  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 1:35 am on 05/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*//*
File : ContributorAdapter.kt -> com.ncs.o2.Adapters
Description : Adapter for contributors  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 1:35 am on 05/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
class ContributorAdapter constructor(val contriList: List<Contributor>, val onClickCallback: OnClickCallback) : RecyclerView.Adapter<ContributorAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ContriItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contributor = contriList[position]

       Glide.with(holder.itemView.context)
           .load(contributor.profileUrl)
           .listener(object : RequestListener<Drawable> {



               override fun onLoadFailed(
                   e: GlideException?,
                   model: Any?,
                   target: Target<Drawable>?,
                   isFirstResource: Boolean
               ): Boolean {
                   holder.binding.progressbar.gone()
                   return false
               }

               override fun onResourceReady(
                   resource: Drawable?,
                   model: Any?,
                   target: Target<Drawable>?,
                   dataSource: DataSource?,
                   isFirstResource: Boolean
               ): Boolean {
                   holder.binding.progressbar.gone()
                   return false
               }
           })
           .encodeQuality(50)
           .override(40,40)
           .apply(
               RequestOptions().
               diskCacheStrategy(DiskCacheStrategy.ALL)

           )
           .error(R.drawable.profile_pic_placeholder)
           .into(holder.binding.contriProfileImage)

        holder.binding.contriProfileImage.setOnClickThrottleBounceListener {
            onClickCallback.onClick(contributor, position)
        }

    }

    override fun getItemCount(): Int {
        return contriList.size
    }

    inner class ViewHolder(val binding: ContriItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnClickCallback{
        fun onClick(contributor : Contributor, position : Int)
    }
}
