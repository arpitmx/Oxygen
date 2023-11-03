package com.ncs.o2.UI.UIComponents.BottomSheets


import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.UseCases.CreateSegmentUseCase
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.HelperClasses.ServerExceptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateSectionViewModel @Inject constructor(
    val usecase : CreateSegmentUseCase,
    @FirebaseRepository val repository: Repository,
): ViewModel(){


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
                    if (callback.data == 200){
                        repository.createSegment(segment){result->
                            when (result) {

                                is ServerResult.Failure -> {
                                }

                                ServerResult.Progress -> {
                                }

                                is ServerResult.Success -> {
                                    _segmencreationLiveData.postValue(ServerExceptions.segement_created.exceptionDescription)
                                }

                            }

                        }
                        _showprogressLD.postValue(false)
                        _segmentValidityLiveData.postValue(Codes.Status.VALID_INPUT)
                    }

                }
            }
        }
    }

}