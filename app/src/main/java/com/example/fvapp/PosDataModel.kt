package com.example.fvapp

import android.os.Parcel
import android.os.Parcelable
import com.amplifyframework.datastore.generated.model.Comment
import com.amplifyframework.datastore.generated.model.Like

class PosDataModel (val postContent: String,val postAuthor:String,val postID:String,val postMedia:String,val postUser:String,val postType:String, val comments: List<Comment>, val likes: List<Like>) :
    Parcelable {
    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        TODO("Not yet implemented")
    }
}