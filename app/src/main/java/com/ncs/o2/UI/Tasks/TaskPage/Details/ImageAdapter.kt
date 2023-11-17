package com.ncs.o2.UI.Tasks.TaskPage.Details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ncs.o2.R


// Todo : Use interface to implement onclick @Mohit
class ImageAdapter (private var images:MutableList<String>): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = images[position]

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}