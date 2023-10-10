package com.fanzverse.fvapp

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

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
        val timeAgo = calculateTimeAgo(comment.createdAt)
        holder.author.text = comment.author
        holder.ago.text = timeAgo
        holder.postcontent.text = comment.content
        Log.i("qsdqsddq", "CLICKED2qsdqsq ${Post[position].pfp}")

        if (Post[position].pfp != ""){
            Glide.with(context)
                .load(Post[position].pfp)
                .into(holder.pfp)
        }

    }
    fun calculateTimeAgo(createdAt: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = Calendar.getInstance().time
        val postDate = sdf.parse(createdAt)
        val timeDifferenceMillis = Math.abs(currentDate.time - postDate.time)

        val minuteMillis: Long = 60 * 1000
        val hourMillis: Long = 60 * minuteMillis
        val dayMillis: Long = 24 * hourMillis
        val weekMillis: Long = 7 * dayMillis
        val monthMillis: Long = 30 * dayMillis

        return when {
            timeDifferenceMillis < minuteMillis -> "${timeDifferenceMillis / 1000} secs ago"
            timeDifferenceMillis < hourMillis -> "${timeDifferenceMillis / minuteMillis} mins ago"
            timeDifferenceMillis < dayMillis -> "${timeDifferenceMillis / hourMillis} hours ago"
            timeDifferenceMillis < weekMillis -> "${timeDifferenceMillis / dayMillis} days ago"
            timeDifferenceMillis < monthMillis -> "${timeDifferenceMillis / weekMillis} weeks ago"
            else -> "${timeDifferenceMillis / monthMillis} months ago"
        }
    }
}


class UserViewHolder2(itemView: View, listener: CommentsAdapter.onItemClickListener) : RecyclerView.ViewHolder(itemView) {

    val author: TextView = itemView.findViewById(R.id.author)
    val ago: TextView = itemView.findViewById(R.id.date)
    val pfp: ImageView = itemView.findViewById(R.id.cmntpfp)
    val postcontent: TextView = itemView.findViewById(R.id.content)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
    }
}
