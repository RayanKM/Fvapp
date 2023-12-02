package com.fanzverse.fvapp

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.FollowRequest
import com.amplifyframework.datastore.generated.model.FollowRequestStatus
import com.bumptech.glide.Glide

class RequestsAdapter(private val context: Context, private val Post: MutableList<ReqDataModel>) :
    RecyclerView.Adapter<UserViewHolder3>() {

    private lateinit var mListenerA: onItemClickListener
    private lateinit var onAcceptItemClickListener: onItemClickListener
    private lateinit var onDeclineItemClickListener: onItemClickListener
    interface onItemClickListener {
        fun onItemClick(position: Int)
        fun onAcceptClick(position: Int)
        fun onDeclineClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListenerA = listener
        onAcceptItemClickListener = listener
        onDeclineItemClickListener = listener


    }
    override fun getItemCount(): Int {
        return Post.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder3 {
        return UserViewHolder3(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.requests, parent, false),mListenerA,onAcceptItemClickListener,onDeclineItemClickListener
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder3, position: Int) {
        Log.i("MyAmplifyApp", "fqsdqdsq")
        holder.pfp.clipToOutline = true
        val requests: List<FollowRequest> = Post[position].requests
        val Request: FollowRequest = requests[position]
        if (Request.status == FollowRequestStatus.ACCEPTED){
            holder.postcontent.setTextColor(Color.parseColor("#09AD2D")) // Set the text color to green
            holder.postcontent.text = "User '${Request.fromUser}' Accepted"
            holder.accept.visibility = View.GONE
            holder.decline.visibility = View.GONE

        }else if (Request.status == FollowRequestStatus.ACCEPTED){
            holder.postcontent.setTextColor(Color.parseColor("##D11127")) // Set the text color to green
            holder.postcontent.text = "User '${Request.fromUser}' Rejected"
            holder.accept.visibility = View.GONE
            holder.decline.visibility = View.GONE
        }
        else{
            holder.postcontent.text = "User '${Request.fromUser}' has sent you a follow request"
        }
        if (Post[position].pfp != ""){
            Glide.with(context)
                .load(Post[position].pfp)
                .into(holder.pfp)
        }
    }
}

class UserViewHolder3(itemView: View, listener: RequestsAdapter.onItemClickListener,
                      acceptListener: RequestsAdapter.onItemClickListener,
                      declineListener: RequestsAdapter.onItemClickListener) :
    RecyclerView.ViewHolder(itemView) {
    val postcontent: TextView = itemView.findViewById(R.id.content)
    val accept: AppCompatButton = itemView.findViewById(R.id.accept)
    val decline: AppCompatButton = itemView.findViewById(R.id.decline)
    val pfp: ImageView = itemView.findViewById(R.id.rpfp)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
        // Add click listeners for the "Accept" and "Decline" buttons
        accept.setOnClickListener {
            // Handle the "Accept" button click
            acceptListener.onAcceptClick(adapterPosition)
        }

        decline.setOnClickListener {
            // Handle the "Decline" button click
            declineListener.onDeclineClick(adapterPosition)
        }
    }
}
