package com.ncs.o2.HelperClasses

class ServerLogger() {
    public fun addRead(count: Int){
        PrefManager.setReadCount(count)
    }
}