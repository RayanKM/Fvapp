package com.fanzverse.fvapp

import android.os.Parcel
import android.os.Parcelable
import com.amplifyframework.datastore.generated.model.Comment
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Post

data class GalleryModel(val date: String, val gallery: MutableList<PosDataModelLite>)
