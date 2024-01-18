package com.fanzverse.fvapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Event
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import jp.wasabeef.glide.transformations.BlurTransformation
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter(private val context: Context, private val Post: List<Event>) :
    RecyclerView.Adapter<UserViewHolderE>() {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolderE {
        return UserViewHolderE(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.events, parent, false),mListenerA
        )
    }
    fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE dd MMM", Locale.getDefault())

        try {
            val date = inputFormat.parse(inputDate)
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return inputDate // Return the original input if parsing fails
    }


    override fun onBindViewHolder(holder: UserViewHolderE, position: Int) {
        val post = Post[position]

        if (post.privacy && post.members == null || post.privacy && !post.members.contains(n)) {
            holder.eventBG.visibility = View.GONE
            holder.eventLock.visibility = View.VISIBLE
            Glide.with(context)
                .load(post.background) // Replace with the resource or URL of your image
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .into(holder.eventLockBG)
            holder.eventLockName.text = post.title


        }
        else{
            Glide.with(context)
                .load(post.background)
                .centerCrop() // You can adjust the transformation as per your requirement
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                    ) {
                        holder.eventBG.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Do nothing here
                    }
                })
            holder.eventLock.visibility = View.GONE
            holder.eventBG.visibility = View.VISIBLE
            holder.eventDate.text = formatDate(post.startDate)
            holder.eventName.text = post.title
            holder.eventTime.text = post.startTime
        }
    }
}

class UserViewHolderE(
    itemView: View, listener: EventAdapter.onItemClickListener
) : RecyclerView.ViewHolder(itemView) {

    val eventBG: LinearLayout = itemView.findViewById(R.id.eventBackground)
    val eventDate: TextView = itemView.findViewById(R.id.eventDate)
    val eventName: TextView = itemView.findViewById(R.id.eventName)
    val eventTime: TextView = itemView.findViewById(R.id.eventTime)

    val eventLock: FrameLayout = itemView.findViewById(R.id.pvst)
    val eventLockBG: ImageView = itemView.findViewById(R.id.img)
    val eventLockName: TextView = itemView.findViewById(R.id.eventNamepv)


    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
    }
}
