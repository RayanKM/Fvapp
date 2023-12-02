package com.fanzverse.fvapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Usr
import com.bumptech.glide.Glide

class ProfilesAdapter2(private val context: Context, private val Post:List<Usr>) :
    RecyclerView.Adapter<UserViewHolder42>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder42 {
        return UserViewHolder42(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rounded, parent, false),mListenerA
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder42, position: Int) {
        Log.i("MyAmplifyApp", "fqsdqdsq")
        holder.pfp.clipToOutline = true

        val request = Post[position]
        holder.profile.text = request.username

        if (request.news != request.bio){
            holder.ln.setBackgroundResource(R.drawable.pfpcircle)
        }else{
            holder.ln.setBackgroundResource(R.drawable.pfpcircle2)
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

class UserViewHolder42(itemView: View, listener: ProfilesAdapter2.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val profile: TextView = itemView.findViewById(R.id.textName)
    val pfp: ImageView = itemView.findViewById(R.id.imageProfile)
    val ln: LinearLayout = itemView.findViewById(R.id.imageProfileContainer)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
            ln.setBackgroundResource(R.drawable.pfpcircle2)
        }
    }
}
