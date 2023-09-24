package com.example.fvapp

interface Communicator {
    fun passdata(post : PosDataModel)
    fun passid(id : String)
}