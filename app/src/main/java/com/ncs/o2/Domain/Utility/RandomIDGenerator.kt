package com.ncs.o2.Domain.Utility

import com.ncs.o2.HelperClasses.PrefManager

/*
File : RandomIDGenerator -> com.ncs.o2.Domain.Utility
Description : Object for generating random IDs 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 5:14 am on 02/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

object RandomIDGenerator {
    private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789*^%$#@!"
    private const val Numbers = "0123456789"
    private const val ID_LENGTH = 15

    fun generateRandomId(): String {
        val random = java.util.Random()
        val idBuilder = StringBuilder()

        repeat(ID_LENGTH) {
            val randomIndex = random.nextInt(CHARACTERS.length)
            val randomChar = CHARACTERS[randomIndex]
            idBuilder.append(randomChar)
        }

        return idBuilder.toString()
    }

    fun generateRandomTaskId(length : Int): String {
        val random = java.util.Random()
        val idBuilder = StringBuilder()

        repeat(length) {
            val randomIndex = random.nextInt(Numbers.length)
            val randomChar = Numbers[randomIndex]
            idBuilder.append(randomChar)
        }

        return idBuilder.toString()
    }

    fun generateTaskID(projectName: String):String{
        return "#${PrefManager.getProjectAliasCode(projectName)}-${generateRandomTaskId(4)}"
    }

}

