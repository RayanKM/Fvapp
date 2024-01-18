package com.fanzverse.fvapp

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Post
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class GalleryAdapter(private val context: Context, private val Post: MutableList<GalleryModel>) :
    RecyclerView.Adapter<UserViewHolderG>() {

    private lateinit var communicator: Communicator

    private lateinit var mListenerA: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListenerA = listener
    }


    override fun getItemCount(): Int {
        return Post.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolderG {
        return UserViewHolderG(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.gallery, parent, false),mListenerA
        )
    }

    override fun onBindViewHolder(holder: UserViewHolderG, position: Int) {
        communicator = context as Communicator
        val theDate = Post[position].date
        val theGallery = Post[position].gallery
        holder.monthORyear.text = theDate
        holder.innerRecylerview.apply {
            layoutManager = GridLayoutManager(context.applicationContext, 3)
            adapter = MomentAdapter(context, theGallery).apply {
                setOnItemClickListener(object : MomentAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val post = theGallery[position]
                        communicator.passid(post.postID, post.postAuthor)
                    }
                })
            }
        }
    }
}

class UserViewHolderG(
    itemView: View, listener: GalleryAdapter.onItemClickListener
) : RecyclerView.ViewHolder(itemView) {

    val monthORyear: TextView = itemView.findViewById(R.id.date)
    val innerRecylerview: RecyclerView = itemView.findViewById(R.id.innerRecyclerview)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
    }
}
