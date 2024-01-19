package com.fanzverse.fvapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Usr
import com.bumptech.glide.Glide

class InviteAdapter(private val context: Context, private val Post:List<Usr>,private val Members:List<String>) :
    RecyclerView.Adapter<UserViewHolder5>() {

    private lateinit var mListenerA: onItemClickListener
    private lateinit var addListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
        fun onAddClick(position: Int)

    }
    fun setOnItemClickListener(listener: onItemClickListener){
        mListenerA = listener
        addListener = listener

    }
    override fun getItemCount(): Int {
        return Post.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder5 {
        return UserViewHolder5(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.profilesinvite, parent, false),mListenerA,addListener
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder5, position: Int) {
        Log.i("MyAmplifyApp", "fqsdqdsq")
        holder.pfp.clipToOutline = true

        val request = Post[position]
        val membrs = Members
        holder.profile.text = request.username

        if (membrs.contains(request.username)){
            holder.already.visibility = View.VISIBLE
            holder.add.visibility = View.GONE

        }else{
            holder.already.visibility = View.GONE
            holder.add.visibility = View.VISIBLE
        }

        if (request.pfp != ""){
            Glide.with(context)
                .load(request.pfp)
                .into(holder.pfp)
        }else{
            holder.pfp.setImageResource(R.drawable.pfp)
        }
    }
}

class UserViewHolder5(itemView: View, listener: InviteAdapter.onItemClickListener,addListener: InviteAdapter.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val profile: TextView = itemView.findViewById(R.id.profile)
    val pfp: ImageView = itemView.findViewById(R.id.spfp)
    val already : AppCompatButton = itemView.findViewById(R.id.already)
    val add : AppCompatButton = itemView.findViewById(R.id.add)
    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
        add.setOnClickListener {
            // Handle the "Accept" button click
            addListener.onAddClick(adapterPosition)
        }
    }
}
