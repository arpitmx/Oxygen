package com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.UseCases.CreateSegmentUseCase
import com.ncs.o2.Constants.Errors
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.HelperClasses.ServerExceptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/*
File : CreateSegmentViewModel.kt -> com.ncs.o2.UI.UIComponents.BottomSheets.CreateSegment
Description : ViewModel for Segment creation 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 10:35 am on 11/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :

*/


@HiltViewModel
class CreateSegmentViewModel @Inject constructor(
    val usecase : CreateSegmentUseCase,
    @FirebaseRepository val repository: Repository,
    ):
    ViewModel(){


    private val _segmentValidityLiveData = MutableLiveData<String>()
    val segmentValidityLiveData : LiveData<String> get() = _segmentValidityLiveData


    private val _segmencreationLiveData = MutableLiveData<String>()
    val segmentcreationLiveData : LiveData<String> get() = _segmencreationLiveData

    private val _showprogressLD = MutableLiveData<Boolean>()
    val showprogressLD: LiveData<Boolean> get() = _showprogressLD

    fun createSegment(segment: Segment){
            usecase.doCheckAndCreateSegment(segment) { callback ->
                    when (callback){
                        is ServerResult.Failure -> {

                            Handler(Looper.getMainLooper()).postDelayed({
                                _showprogressLD.postValue(false)
                            },400)

                            when (callback.exception) {
                                ServerExceptions.projectDoesNotExists -> {
                                    _segmentValidityLiveData.postValue(ServerExceptions.projectDoesNotExists.exceptionDescription)
                                }
                                ServerExceptions.duplicateNameException -> {
                                    _segmentValidityLiveData.postValue(ServerExceptions.duplicateNameException.exceptionDescription)
                                }
                                ServerExceptions.keywordDetectedException->{
                                    _segmentValidityLiveData.postValue(ServerExceptions.keywordDetectedException.exceptionDescription)
                                }
                                else -> {
                                    _segmentValidityLiveData.postValue(callback.exception.message.toString())
                                }
                            }
                        }

                        is ServerResult.Progress -> {
                            _showprogressLD.postValue(true)
                        }

                        is ServerResult.Success -> {

                            Handler(Looper.getMainLooper()).postDelayed({
                                _showprogressLD.postValue(false)
                            },400)

                            //Valid
//                            if (callback.data == 200){
//                                repository.createSegment(segment){result->
//                                    when (result) {
//
//                                        is ServerResult.Failure -> {
//                                        }
//
//                                        ServerResult.Progress -> {
//                                        }
//
//                                        is ServerResult.Success -> {
//                                            _segmencreationLiveData.postValue(ServerExceptions.segement_created.exceptionDescription)
//                                        }
//
//                                    }
//
//                                }
                               _showprogressLD.postValue(false)
                                _segmentValidityLiveData.postValue(Errors.Status.VALID_INPUT)
                            }
                        }
                    }
            }


}