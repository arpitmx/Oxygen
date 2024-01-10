package com.ncs.o2.UI.UIComponents.BottomSheets

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.databinding.ProfileBottomSheetBinding
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale


/*
File : ProfileBottomSheet.kt -> com.ncs.o2.BottomSheets
Description : Profile bottom sheet  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 3:14 am on 05/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
class ProfileBottomSheet (
    var user: User
) :BottomSheetDialogFragment() {

    lateinit var binding: ProfileBottomSheetBinding

    override fun onCreateView(
         inflater: LayoutInflater,
        container: ViewGroup?,
       savedInstanceState: Bundle?
    ): View {
        binding = ProfileBottomSheetBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated( view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext())
            .load(user.profileDPUrl)
            .listener(object : RequestListener<Drawable> {



                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressbar.gone()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressbar.gone()
                    return false
                }
            })
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).override(100, 100)

            )
            .into(binding.roomDp)
        binding.roomNameBs.text=user.username
        binding.hostNameBs.text=user.firebaseID
        val timestamp = user.timestamp?.seconds
        binding.time.text= "Joined ${DateTimeUtils.getTimeAgo(timestamp!!)}"
        binding.totalMembersBs.text=user.designation
    }
}