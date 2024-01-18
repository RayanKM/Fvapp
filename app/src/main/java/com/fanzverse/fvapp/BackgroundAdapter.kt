package com.fanzverse.fvapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BackgroundAdapter(private val context: Context, private val Images: List<ImageItem>) :
    RecyclerView.Adapter<UserViewHolderBG>() {

    private lateinit var mListenerA: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListenerA = listener
    }
    override fun getItemCount(): Int {
        return Images.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolderBG {
        return UserViewHolderBG(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.background, parent, false),mListenerA
        )
    }

    override fun onBindViewHolder(holder: UserViewHolderBG, position: Int) {
        val post = Images[position]

        Glide.with(context).load("https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/backgrounds/${post.imageUrl}")
            .into(holder.img)
    }
}

class UserViewHolderBG(
    itemView: View, listener: BackgroundAdapter.onItemClickListener
) : RecyclerView.ViewHolder(itemView) {

    val img: ImageView = itemView.findViewById(R.id.img)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
    }
}
