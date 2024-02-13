package com.ncs.o2.Domain.UseCases

import com.ncs.o2.Constants.Errors
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.HelperClasses.ServerExceptions
import javax.inject.Inject

/*
File : CreateSegmentUseCase.kt -> com.ncs.o2.Domain.UseCases
Description : Usecase for creating Segment 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 10:56 am on 11/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/

class CreateSegmentUseCase @Inject constructor(
    @FirebaseRepository val repository: Repository
) {

    fun doCheckAndCreateSegment(segment: Segment, callback: (ServerResult<Int>) -> Unit) {
        if (segment.segment_NAME.trim().trimStart()=="Select Segment"){
            callback(ServerResult.Failure(ServerExceptions.keywordDetectedException))
        }
        else {
            repository.checkIfSegmentNameExists(
                fieldName = segment.segment_NAME, projectID = segment.project_ID
            ) { result ->


                when (result) {
                    is ServerResult.Success -> {

                        if (result.data) {
                            //True : Same segment name found
                            callback(ServerResult.Failure(ServerExceptions.duplicateNameException))
                        } else {
                            //False : Not a duplicate, safe to create new segment
                            callback(ServerResult.Success(Errors.Status.RESULT_OK))
                        }

                    }

                    is ServerResult.Failure -> {

                        if (result.exception == ServerExceptions.projectDoesNotExists) {
                            callback(ServerResult.Failure(ServerExceptions.projectDoesNotExists))
                            return@checkIfSegmentNameExists
                        } else {
                            callback(ServerResult.Failure(result.exception))
                        }
                    }

                    is ServerResult.Progress -> {
                        callback(ServerResult.Progress)
                    }

                }
            }
        }

    }


}