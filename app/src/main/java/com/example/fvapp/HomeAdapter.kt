package com.example.fvapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Like
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class HomeAdapter (private val context: Context, private val Post: MutableList<PosDataModel>) :
    RecyclerView.Adapter<UserViewHolder>() {
    private var player: SimpleExoPlayer? = null
    private var playbackPosition = 0L


    private lateinit var mListenerA: onItemClickListener
    private lateinit var mListenerB: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
        fun onLike(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListenerA = listener
        mListenerB = listener
    }

    override fun getItemCount(): Int {
        return Post.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.posts, parent, false),mListenerA,mListenerB
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        player = SimpleExoPlayer.Builder(context).build()
        holder.vid.player = player
        val n = MainActivity.userN
        holder.img.clipToOutline = true

        val post = Post[position]
        val commentNumbers = post.comments.size.toString()
        val likesNumbers = post.likes.size.toString()
        val checklike : List<Like> = post.likes

        val hasLikeWithUsername = checklike.any { it.username == n }
        if (hasLikeWithUsername) {
            holder.likesbtn.setImageResource(R.drawable.liked)
            holder.likesbtn.isClickable = false
        }

        holder.author.text = post.postAuthor
        holder.postcontent.text = post.postContent
        holder.comments.text = "$commentNumbers Comments"
        holder.likes.text = "$likesNumbers Likes"

        if (post.postType == "image"){
            holder.vid.visibility = View.GONE
            holder.media.visibility = View.VISIBLE
            Glide.with(context)
                .load(post.postMedia)
                .into(holder.media)
        }else if (post.postType == "video"){
            holder.media.visibility = View.GONE
            holder.vid.visibility = View.VISIBLE
            val mediaItem = MediaItem.fromUri(post.postMedia)
            player?.setMediaItem(mediaItem)
            player?.prepare()
        }
        if (post.postUser != ""){
            Glide.with(context)
                .load(post.postUser)
                .into(holder.img)
        }
    }
}

class UserViewHolder(
    itemView: View, listener: HomeAdapter.onItemClickListener,
    like: HomeAdapter.onItemClickListener
) : RecyclerView.ViewHolder(itemView) {

    val author: TextView = itemView.findViewById(R.id.author)
    val postcontent: TextView = itemView.findViewById(R.id.content)
    val media: ImageView = itemView.findViewById(R.id.main)
    val comments: TextView = itemView.findViewById(R.id.cmnts)
    val likes: TextView = itemView.findViewById(R.id.lks)
    val likesbtn: ImageView = itemView.findViewById(R.id.btn_like)
    val img: ImageView = itemView.findViewById(R.id.postpfp)
    val vid: PlayerView = itemView.findViewById(R.id.playvid)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
        likesbtn.setOnClickListener {
            like.onLike(adapterPosition)
            likesbtn.setImageResource(R.drawable.liked)
            likesbtn.isClickable = false
        }
    }
}
