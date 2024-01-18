package com.fanzverse.fvapp

interface Communicator {
    fun passdata(post : PosDataModel)
    fun passid(id:String, to:String)
    fun passid2(id:String)

}