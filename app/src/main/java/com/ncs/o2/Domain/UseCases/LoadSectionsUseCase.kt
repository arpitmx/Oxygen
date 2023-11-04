package com.ncs.o2.Domain.UseCases

import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.Later
import javax.inject.Inject

/*
File : LoadSectionsUseCase.kt -> com.ncs.o2.Domain.UseCases
Description : Use case for loading sections  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 9:26 am on 14/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
@Later("1.Set repository class , 2. Fetch sections from firebase , 3. Return to Viewmodel")
class LoadSectionsUseCase @Inject constructor(repository : Repository)  {

      fun loadSections(segment: Segment, result : (ServerResult<List<Task>>) -> Unit){

        }

}