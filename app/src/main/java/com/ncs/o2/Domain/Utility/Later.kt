package com.ncs.o2.Domain.Utility

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.EXPRESSION)
annotation class Later(val todo : String)
annotation class Version(val version : String)



