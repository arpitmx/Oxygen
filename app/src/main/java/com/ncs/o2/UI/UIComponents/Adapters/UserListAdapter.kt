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
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.R
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.databinding.ContributorListItemBinding

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

*/
class UserListAdapter constructor(
    private val contriList: MutableList<User>,
    private val onClickCallback: OnClickCallback
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ContributorListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var contributor = contriList[position]

        Glide.with(holder.itemView.context)
            .load(contributor.profileDPUrl)
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
            .encodeQuality(80)
            .override(40, 40)
            .apply(
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

            )
            .error(R.drawable.profile_pic_placeholder)
            .into(holder.binding.userDp)


        holder.binding.userName.text = contributor.username
        holder.binding.userPost.text = contributor.post

        holder.binding.checkbox.setOnCheckedChangeListener { btn, isChecked ->
            contriList[position].isChecked = isChecked
            onClickCallback.onClick(contributor, position, isChecked)
        }

        holder.binding.root.setOnClickListener {
            val isCheckedC = holder.binding.checkbox.isChecked
            if (isCheckedC) {
                holder.binding.checkbox.isChecked = false
                contributor.isChecked = false
                onClickCallback.onClick(contributor, position, false)
            } else {
                holder.binding.checkbox.isChecked = true
                contributor.isChecked = true
                onClickCallback.onClick(contributor, position, true)
            }
        }

        holder.binding.checkbox.isChecked = contributor.isChecked


    }

    override fun getItemCount(): Int {
        return contriList.size
    }

    inner class ViewHolder(val binding: ContributorListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnClickCallback {
        fun onClick(contributor: User, position: Int, isChecked: Boolean)
    }
}