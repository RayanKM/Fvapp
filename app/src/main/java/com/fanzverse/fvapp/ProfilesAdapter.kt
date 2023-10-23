package com.fanzverse.fvapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Usr
import com.bumptech.glide.Glide

class ProfilesAdapter(private val context: Context, private val Post:List<Usr>) :
    RecyclerView.Adapter<UserViewHolder4>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder4 {
        return UserViewHolder4(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.profiles, parent, false),mListenerA
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder4, position: Int) {
        Log.i("MyAmplifyApp", "fqsdqdsq")
        holder.pfp.clipToOutline = true

        val request = Post[position]
        holder.profile.text = request.username
        if (request.pfp != ""){
            Glide.with(context)
                .load(request.pfp)
                .into(holder.pfp)
        }else{
            holder.pfp.setImageResource(R.drawable.pfp)
        }
    }
}

class UserViewHolder4(itemView: View, listener: ProfilesAdapter.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val profile: TextView = itemView.findViewById(R.id.profile)
    val pfp: ImageView = itemView.findViewById(R.id.spfp)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
    }
}
