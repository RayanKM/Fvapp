package com.fanzverse.fvapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Comment
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.datastore.generated.model.Usr
import com.bumptech.glide.Glide
import com.fanzverse.fvapp.databinding.FragmentPostDetailBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PostDetail : Fragment(R.layout.fragment_post_detail) {
    private var player: SimpleExoPlayer? = null
    private var comments = mutableListOf<CommentsDataModel>()
    private var likes = mutableListOf<Like>()
    private lateinit var postid: String
    private lateinit var binding: FragmentPostDetailBinding
    val n = MainActivity.userN
    var post: PosDataModel? = null
    var to : String = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = SimpleExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player
        post = arguments?.getParcelable("post")
        postid = post?.postID.toString()
        to = post?.postAuthor.toString()
        binding.postAu.clipToOutline = true
        fetch(postid)
        binding.cmnts.text = "${comments?.size} Comments"
        if (isAdded){
            binding.mainRecyclerview2.apply {
                layoutManager = LinearLayoutManager(this.context)
                adapter = CommentsAdapter(requireContext(), comments).apply {
                    setOnItemClickListener(object : CommentsAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            Log.i("MyAmplifyApp", "CLICKED2")

                        }
                    })
                }
            }
        }


        binding.sendButton.setOnClickListener {
            val comment = binding.commentEditText.text.toString()
            createComment(n!!,post?.postID.toString(),comment)
        }
    }

    fun createComment(id:String, postid: String,comment : String){
        // Create an instance of OkHttpClient
        val client = OkHttpClient()

        val url = "http://worldtimeapi.org/api/timezone/UTC"

        val request = Request.Builder()
            .url(url)
            .build()

        // Use OkHttp to enqueue the request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle network request failure here
            }

            override fun onResponse(call: Call, response: Response) {
                // Check if the response was successful
                if (!response.isSuccessful) {
                    // Handle the unsuccessful response here
                } else {
                    // Get the response body as a JSON string
                    val json = response.body?.string()

                    // Parse the JSON string
                    val jsonObject = JSONObject(json)

                    // Extract the "utc_datetime" value from the JSON
                    val utcDatetime = jsonObject.optString("utc_datetime")

                    val inputFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault())
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                    val date = inputFormat.parse(utcDatetime)

                    val outputFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    outputFormat.timeZone = TimeZone.getTimeZone("UTC")

                    val formattedDatetime = outputFormat.format(date)
                    hideKeyboard(binding.commentEditText)
                    val comment = Comment.builder()
                        .author(id)
                        .postId(postid)
                        .content(comment)
                        .createdAt(formattedDatetime)
                        .build()
                    Amplify.API.mutate(
                        ModelMutation.create(comment),
                        { response ->
                            // This block is executed when the mutation is successful
                            Log.i("MyAmplifyApp", "Todo with id: ${response.data.id}")

                            runBlocking {
                                val pf = async { getPfp(id) }
                                // Create a PostWithComments object and add it to the list
                                val Reqs = CommentsDataModel(pf.await(),comment)
                                comments.add(0,Reqs)
                                activity?.runOnUiThread {
                                    binding.mainRecyclerview2.adapter?.notifyDataSetChanged()
                                }
                            }
                            binding.commentEditText.text.clear()
                            // Handle any other logic you need here for a successful mutation
                        },
                        { error ->
                            // This block is executed when there's an error during the mutation
                            Log.e("MyAmplifyApp", "Create failed", error)
                            // Handle the error appropriately
                        }
                    )
                }
            }
        })
    }
    private fun fetchComments(postId:String){
        comments.clear()
        Amplify.API.query(
            ModelQuery.list(Comment::class.java, Comment.POST_ID.contains(postId)),
            { commentResponse ->
                val sortedComments = commentResponse.data.sortedByDescending { it.createdAt }
                sortedComments.forEach { comm ->
                    runBlocking {
                        val pf = async { getPfp(comm.author) }
                        // Create a PostWithComments object and add it to the list
                        val Reqs = CommentsDataModel(pf.await(),comm)
                        comments.add(Reqs)
                        activity?.runOnUiThread {
                            binding.cmnts.text = "${comments?.size} Comments"
                        }
                    }
                }
            },
            { commentError ->
                Log.e("MyAmplifyApp", "Query comment failure", commentError)
            }
        )
    }
    fun fetch(postid: String) {
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.ID.contains(postid)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    val postId = post.id
                    val postAuthor = post.author
                    val postContent = post.content
                    activity?.runOnUiThread {
                        binding.author.text = postAuthor
                        binding.content.text = postContent
                        if (post.typ == "image"){
                            binding.main.visibility = View.VISIBLE
                            Glide.with(requireActivity())
                                .load(post.media)
                                .into(binding.main)
                        }else if (post.typ == "video"){
                            binding.playerView.visibility = View.VISIBLE
                            val mediaItem = MediaItem.fromUri(post.media)
                            player?.setMediaItem(mediaItem)
                            player?.prepare()
                        }
                        if (post.typ != ""){
                            Glide.with(requireActivity())
                                .load(post.media)
                                .into(binding.main)
                        }
                        runBlocking {
                            val pf =async { getPfp(postAuthor) }
                            if (pf.await() != ""){
                                Glide.with(requireActivity())
                                    .load(pf.await())
                                    .into(binding.postAu)
                            }
                        }
                        fetchComments(postId)
                        fetchLikes(postId)
                        activity?.runOnUiThread {
                            // Notify the adapter that the data has changed
                            binding.mainRecyclerview2.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
            }
        )
    }
    private fun fetchLikes(postId:String){
        Amplify.API.query(
            ModelQuery.list(Like::class.java, Like.POST_ID.contains(postId)),
            { likeResponse ->
                likeResponse.data.forEach { like ->
                    likes.add(like)
                    activity?.runOnUiThread {
                        if (like.username == n) {
                            binding.btnLike.setImageResource(R.drawable.liked)
                            binding.btnLike.isClickable = false
                        }else{
                            binding.btnLike.setOnClickListener {
                                binding.btnLike.setImageResource(R.drawable.liked)
                                likePost(n!!,postid,to)
                                binding.btnLike.isClickable = false
                            }
                        }
                        // Notify the adapter that the data has changed
                        binding.mainRecyclerview2.adapter?.notifyDataSetChanged()
                    }
                }
                activity?.runOnUiThread {
                    binding.lksp.text = "${likes?.size} Likes"
                }

            },
            { commentError ->
                Log.e("MyAmplifyApp", "Query comment failure", commentError)
            }
        )
    }
    fun likePost(id:String, postid: String, to:String){
        val post = Like.builder()
            .username(id)
            .postId(postid)
            .to(to)
            .build()
        Amplify.API.mutate(
            ModelMutation.create(post),
            { response ->
                fetchLikes(postid)
                // This block is executed when the mutation is successful
                Log.i("MyAmplifyApp", "Todo with id: ${response.data.id}")
                // Handle any other logic you need here for a successful mutation
            },
            { error ->
                // This block is executed when there's an error during the mutation
                Log.e("MyAmplifyApp", "Create failed", error)
                // Handle the error appropriately
            }
        )
    }
    private suspend fun getPfp(id: String): String {
        var pfp = ""
        val deferred = CompletableDeferred<String>() // Create a CompletableDeferred

        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    pfp = post.pfp ?: ""
                    // Resolve the deferred with the pfp once the query is done
                    deferred.complete(pfp)

                    activity?.runOnUiThread {
                        // Notify the adapter that the data has changed
                        binding.mainRecyclerview2.adapter?.notifyDataSetChanged()
                    }
                }
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)

                // Complete the deferred with an empty string if there's an error
                deferred.complete("")
            }
        )

        return deferred.await() // Suspend until the deferred is completed and return the pfp
    }
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}