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
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.R
import com.ncs.o2.databinding.MentionUserRvItemBinding

class MentionUsersAdapter constructor(
    private var contriList: MutableList<User>,private var callback:onUserClick
) : RecyclerView.Adapter<MentionUsersAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            MentionUserRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder,position:Int) {
        val contributor = contriList[position]

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

        holder.binding.userName.text=contributor.username
        holder.binding.parent.setOnClickThrottleBounceListener{
            callback.onClick(contributor)
        }
    }

    override fun getItemCount(): Int {
        return contriList.size
    }

    fun updateList(newList: List<User>) {
        contriList = newList.toMutableList()
        notifyDataSetChanged()
    }
    inner class ViewHolder(val binding: MentionUserRvItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface onUserClick{
        fun onClick(user: User)
    }

}
