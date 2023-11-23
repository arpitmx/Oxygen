package com.ncs.o2.UI.UIComponents.Adapters

import android.annotation.SuppressLint
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
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.R
import com.ncs.o2.databinding.ContributorListItemBinding

class AssigneeListAdpater constructor(
    private val contriList: MutableList<User>,
    private val onClickCallback: OnAssigneeClickCallback
) : RecyclerView.Adapter<AssigneeListAdpater.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ContributorListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position:Int) {
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

        holder.binding.checkbox.setOnCheckedChangeListener(null) // Remove previous listener to avoid triggering unwanted events


        holder.binding.checkbox.isChecked = contributor.isChecked

        holder.binding.checkbox.setOnCheckedChangeListener { btn, isChecked ->
            contriList[position].isChecked = isChecked

            if (isChecked) {
                for (i in contriList.indices) {
                    if (i != position) {
                        contriList[i].isChecked = false
                        notifyItemChanged(i)
                    }
                }
            }

            onClickCallback.onAssigneeClick(contributor, position, isChecked)
        }
        holder.binding.root.setOnClickListener {
            val isCheckedC = holder.binding.checkbox.isChecked
            if (isCheckedC) {
                holder.binding.checkbox.isChecked = false
                contributor.isChecked = false
                onClickCallback.onAssigneeClick(contributor, position, false)
            } else {
                holder.binding.checkbox.isChecked = true
                contributor.isChecked = true
                onClickCallback.onAssigneeClick(contributor, position, true)
            }
        }
    }

    override fun getItemCount(): Int {
        return contriList.size
    }

    inner class ViewHolder(val binding: ContributorListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnAssigneeClickCallback {
        fun onAssigneeClick(contributor: User, position: Int, isChecked: Boolean)
    }
}
