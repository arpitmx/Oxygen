package com.ncs.o2.Domain.Utility

import javax.inject.Qualifier

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class annotationUtility(val str:String)

@Target(AnnotationTarget.FIELD)
annotation class Positive

@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE
)
@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class FirebaseRepository

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class FirebaseAuthorizationRepository



@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.EXPRESSION)
annotation class Issue(val issue : String)