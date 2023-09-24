package com.example.fvapp

import android.os.Parcel
import android.os.Parcelable
import com.amplifyframework.datastore.generated.model.FollowRequest
import com.amplifyframework.datastore.generated.model.Like

class ReqDataModel (val pfp: String,val requests: List<FollowRequest>) :
    Parcelable {
    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        TODO("Not yet implemented")
    }
}