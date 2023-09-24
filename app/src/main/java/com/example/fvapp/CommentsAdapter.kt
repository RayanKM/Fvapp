package com.example.fvapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Comment
import com.bumptech.glide.Glide

class CommentsAdapter(private val context: Context, private val Post: MutableList<CommentsDataModel>) :
    RecyclerView.Adapter<UserViewHolder2>() {
    val n = MainActivity.userN

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder2 {
        return UserViewHolder2(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.comments, parent, false),mListenerA
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder2, position: Int) {
        holder.pfp.clipToOutline = true

        val comments: List<Comment> = Post[position].comments
        val comment: Comment = comments[position]
        holder.author.text = comment.author
        holder.postcontent.text = comment.content
        Log.i("qsdqsddq", "CLICKED2qsdqsq ${Post[position].pfp}")

        if (Post[position].pfp != ""){
            Glide.with(context)
                .load(Post[position].pfp)
                .into(holder.pfp)
        }

    }
}

class UserViewHolder2(itemView: View, listener: CommentsAdapter.onItemClickListener) : RecyclerView.ViewHolder(itemView) {

    val author: TextView = itemView.findViewById(R.id.author)
    val pfp: ImageView = itemView.findViewById(R.id.cmntpfp)
    val postcontent: TextView = itemView.findViewById(R.id.content)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
    }
}
