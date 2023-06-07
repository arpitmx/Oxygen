package com.ncs.o2.Domain.Utility

import javax.inject.Qualifier

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class annotationUtility(val str:String)

@Target(AnnotationTarget.FIELD)
annotation class Positive

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class FirebaseRepository

