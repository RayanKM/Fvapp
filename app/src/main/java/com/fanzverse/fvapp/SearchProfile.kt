package com.fanzverse.fvapp

import android.animation.ValueAnimator
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Comment
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.datastore.generated.model.Usr
import com.fanzverse.fvapp.databinding.FragmentSearchProfileBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class SearchProfile : Fragment(R.layout.fragment_search_profile) {
    private lateinit var communicator: Communicator
    private var users = mutableListOf<Usr>()
    private var users2 = mutableListOf<Usr>()
    private var triggered = false
    private var who = ""
    private var postListWithComments = mutableListOf<PosDataModel>()
    val n = MainActivity.userN

    private lateinit var binding: FragmentSearchProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("MyAmplifyApp", "SUIIIIII ${n}")
        communicator = activity as Communicator
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.sheet3)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.isDraggable = false
        fetchAll(n.toString())
        var searchHandler: Handler? = null
        val searchView = view.findViewById<SearchView>(R.id.srchv)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchHandler?.removeCallbacksAndMessages(null)
                searchHandler = Handler()
                searchHandler?.postDelayed({
                    fetch(newText.orEmpty())
                }, 300) // Adjust the delay as needed (300 milliseconds in this example)
                return true
            }

        })
        binding.mainRecyclerview.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = ProfilesAdapter(requireContext(), users).apply {
                setOnItemClickListener(object : ProfilesAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        Log.i("MyAmplifyApp", "CLICKED")
                        val id = users[position].username
                        communicator.passid(id)
                    }
                })
            }
        }
        binding.mainRecyclerview2.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ProfilesAdapter2(requireContext(), users2).apply {
                setOnItemClickListener(object : ProfilesAdapter2.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        fetchPost(users2[position].username)
                        binding.mainRecyclerviewdt.apply {
                            layoutManager = LinearLayoutManager(this.context)
                            adapter = HomeAdapter(requireContext(), postListWithComments).apply {
                                setOnItemClickListener(object : HomeAdapter.onItemClickListener {
                                    override fun onItemClick(position: Int) {
                                        val post = postListWithComments[position]
                                        communicator.passdata(post)
                                    }
                                    override fun onLike(position: Int) {
                                        val postid = postListWithComments[position].postID
                                        val to = postListWithComments[position].postAuthor
                                        likePost(n!!, postid, to)
                                    }
                                })
                            }
                        }

                        val bottomSheetBehavior = BottomSheetBehavior.from(binding.sheet3)
                        val dpToPx = resources.displayMetrics.density
                        val statusBarHeight = getStatusBarHeight()
                        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
                        val horizLayoutHeight = (200 * dpToPx).toInt()
                        val desiredHeightPx = screenHeight - statusBarHeight - horizLayoutHeight
                        if (!triggered){
                            triggered = true
                            who = users2[position].username
                            val animator = ValueAnimator.ofInt(0, desiredHeightPx)
                            animator.addUpdateListener { valueAnimator ->
                                val height = valueAnimator.animatedValue as Int
                                bottomSheetBehavior.peekHeight = height
                            }
                            animator.duration = 500
                            animator.start()
                        }else{
                            if (who == users2[position].username){
                                triggered = false
                                val animator = ValueAnimator.ofInt(desiredHeightPx, 0)
                                animator.addUpdateListener { valueAnimator ->
                                    val height = valueAnimator.animatedValue as Int
                                    bottomSheetBehavior.peekHeight = height
                                }
                                animator.duration = 500
                                animator.start()
                            }else{
                                who = users2[position].username
                            }
                        }


                        if (users2[position].news != users2[position].bio){
                            Amplify.API.query(
                                ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(n)),
                                { response ->
                                    response.data.forEach { existingUser ->
                                        val following = existingUser.following.toMutableList()
                                        for (i in 0 until following.size) {
                                            val entry = following[i]
                                            if (entry.name == users2[position].username) {
                                                val updatedEntry = entry.copyOfBuilder().news(users2[position].news).build()
                                                following[i] = updatedEntry

                                                break
                                            }
                                        }

                                        val updatedUser = existingUser.copyOfBuilder()
                                            .id(existingUser.id)
                                            .following(following)
                                            .build()
                                        // Perform the update mutation with the modified user object
                                        Amplify.API.mutate(
                                            ModelMutation.update(updatedUser),
                                            { updateResponse ->
                                                // Handle the successful update
                                                Log.i("Amplify", "User updated: ${updateResponse.data}")
                                            },
                                            { error ->
                                                // Handle the error
                                                Log.e("Amplify", "Error updating user", error)
                                            }
                                        )
                                    }
                                },
                                { Log.e("MyAmplifyApp", "Query failure", it) }
                            )
                        }
                    }
                })
            }
        }

    }
    fun fetchAll(id: String) {
        users2.clear()
        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
            { postResponse ->
                postResponse.data.forEach { posts ->
                    posts.following?.forEach { post ->
                        fetch2(post.name)
                        // Use the 'news' value as needed
                    }

                }
            },
            { responseError ->
                Log.e("MyAmplifyApp", "Query failure", responseError)
            }
        )
    }
    fun fetch2(id: String) {
        users.clear()
        if (id != null){
            Amplify.API.query(
                ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
                { postResponse ->
                    postResponse.data.forEach { post ->
                        if (post.username == n){
                            val modifiedPost = post.copyOfBuilder().id(post.id).bio(post.news).build()
                            users2.add(modifiedPost)
                            activity?.runOnUiThread {
                                // Notify the adapter that the data has changed
                                binding.mainRecyclerview2.adapter?.notifyDataSetChanged()
                            }
                        }else{
                            runBlocking {
                                val pf = async { getnews(n!!,post.username) }
                                val modifiedPost = post.copyOfBuilder().id(post.id).bio(pf.await()).build()
                                users2.add(modifiedPost)
                                activity?.runOnUiThread {
                                    // Notify the adapter that the data has changed
                                    binding.mainRecyclerview2.adapter?.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                },
                { postError ->
                    Log.e("MyAmplifyApp", "Query post failure", postError)
                }
            )
        }
    }

    fun fetch(id: String) {
        users.clear()
        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    users.add(post)
                    activity?.runOnUiThread {
                        // Notify the adapter that the data has changed
                        binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                    }
                }
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
            }
        )
    }
    private suspend fun getnews(id: String,id2: String): String {
        var pfp = ""
        val deferred = CompletableDeferred<String>() // Create a CompletableDeferred
        Log.e("sqdqds", "ysspf", )

        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
            { response ->
                response.data.forEach { existingUser ->
                    if (existingUser.following != null) {
                        val following = existingUser.following
                        for (i in following) {
                            if (i.name == id2){
                                pfp = i.news ?: ""
                                // Resolve the deferred with the pfp once the query is done
                                deferred.complete(pfp)
                            }
                        }
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
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun fetchPost(id: String) {
        postListWithComments.clear()
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.AUTHOR.contains(id)),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order

                sortedPosts?.forEach { post ->
                    val postId = post.id
                    val postAuthor = post.author
                    val postContent = post.content
                    Log.e("qsfqdqdqqdq", "qsdqre4 ${postContent}")
                    val postType = post.typ
                    val postDate = post.createdAt
                    val timeAgo = calculateTimeAgo(postDate)
                    val postMedia: String = post.media ?: ""
                    var pfp = ""
                    runBlocking {
                        val cm = async { cmt(postId) }
                        val lk = async { lks(postId) }
                        val pf = async { getPfp(id) }

                        // Create a PostWithComments object and add it to the list
                        val postWithComments =
                            PosDataModel(postContent, postAuthor, postId, postMedia, pf.await(), postType, timeAgo, cm.await(), lk.await(), postDate)
                        Log.e("qsfqdqdqqdq", "qsdqre3 ${pfp}")
                        postListWithComments.add(postWithComments)
                    }
                }
                activity?.runOnUiThread {
                    // Notify the adapter that the data has changed
                    binding.mainRecyclerviewdt.adapter?.notifyDataSetChanged()
                }
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
            }
        )
    }
    fun likePost(id: String, postid: String,to: String) {
        val post = Like.builder()
            .username(id)
            .postId(postid)
            .to(to)
            .build()
        Amplify.API.mutate(
            ModelMutation.create(post),
            { response ->
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
    private suspend fun cmt(postId: String): MutableList<Comment> {
        val comments = mutableListOf<Comment>()
        val deferred = CompletableDeferred<MutableList<Comment>>() // Create a CompletableDeferred

        Amplify.API.query(
            ModelQuery.list(Comment::class.java, Comment.POST_ID.contains(postId)),
            { commentResponse ->
                commentResponse.data.forEach { comment ->
                    comments.add(comment)
                }
                // Resolve the deferred with the comments once the loop is done
                deferred.complete(comments)
            },
            { commentError ->
                Log.e("MyAmplifyApp", "Query comment failure", commentError)
                // Complete the deferred with an empty list if there's an error
                deferred.complete(mutableListOf())
            }
        )
        return deferred.await() // Suspend until the deferred is completed
    }
    private suspend fun lks(postId: String): MutableList<Like> {
        val likes = mutableListOf<Like>()
        val deferred = CompletableDeferred<MutableList<Like>>() // Create a CompletableDeferred

        Amplify.API.query(
            ModelQuery.list(Like::class.java, Like.POST_ID.contains(postId)),
            { commentResponse ->
                commentResponse.data.forEach { comment ->
                    likes.add(comment)
                }
                // Resolve the deferred with the comments once the loop is done
                deferred.complete(likes)
            },
            { commentError ->
                Log.e("MyAmplifyApp", "Query comment failure", commentError)
                // Complete the deferred with an empty list if there's an error
                deferred.complete(mutableListOf())
            }
        )
        return deferred.await() // Suspend until the deferred is completed
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
                        binding.mainRecyclerview.adapter?.notifyDataSetChanged()
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