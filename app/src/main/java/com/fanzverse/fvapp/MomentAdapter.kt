package com.fanzverse.fvapp

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Post
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class MomentAdapter(private val context: Context, private val Post: MutableList<PosDataModelLite>) :
    RecyclerView.Adapter<UserViewHolderM>() {


    private lateinit var mListenerA: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListenerA = listener
    }
    fun Vidimg(url:String,holder: UserViewHolderM){
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(url, HashMap())
        // Capture bitmap at a specific time (e.g., 1 second into the video)
        val timeUs = 1000000L // 1 second in microseconds
        val bitmap: Bitmap? = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        Glide.with(context).load(bitmap).into(holder.media)
        holder.typ.visibility = View.VISIBLE

        // Release the MediaMetadataRetriever when done
        retriever.release()
    }

    override fun getItemCount(): Int {
        return Post.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolderM {
        return UserViewHolderM(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.moments, parent, false),mListenerA
        )
    }

    override fun onBindViewHolder(holder: UserViewHolderM, position: Int) {
        val post = Post[position]

        if (post.postType == "image"){
            Glide.with(context).load(post.postMedia)
                .into(holder.media)
        }else {
            Vidimg(post.postMedia, holder)
        }

    }
}

class UserViewHolderM(
    itemView: View, listener: MomentAdapter.onItemClickListener
) : RecyclerView.ViewHolder(itemView) {

    val typ: ImageView = itemView.findViewById(R.id.imgt)
    val media: ImageView = itemView.findViewById(R.id.img)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
    }
}
