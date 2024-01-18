package com.fanzverse.fvapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Comment
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.datastore.generated.model.Usr
import com.fanzverse.fvapp.databinding.FragmentMomentsBinding
import com.fanzverse.fvapp.databinding.FragmentSearchProfileBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class Moments : Fragment(R.layout.fragment_search_profile) {
    private lateinit var binding: FragmentMomentsBinding
    private lateinit var communicator: Communicator
    var allPosts = mutableListOf<PosDataModelLite>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMomentsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        communicator = activity as Communicator
        fetch()
        // Notify the adapter that the data has changed
        binding.mainRecyclerview.apply {
            layoutManager = GridLayoutManager(requireActivity().applicationContext, 3)
            adapter = MomentAdapter(requireContext(), allPosts).apply {
                setOnItemClickListener(object : MomentAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val post = allPosts[position]
                        communicator.passid(post.postID, post.postAuthor)
                    }
                })
            }
        }
        binding.swiper.setOnRefreshListener {
            fetch()
        }
    }

    fun fetch(){
        allPosts.clear()
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.MEDIA.contains("https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/")),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                // Get the post IDs of the first three posts
                sortedPosts.forEach { postId ->
                    val postWithComments = PosDataModelLite(postId.author, postId.id, postId.media,postId.typ)
                    allPosts.add(postWithComments)
                    activity?.runOnUiThread {
                        // Notify the adapter that the data has changed
                        binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                    }
                }
                Log.e("MyAmplifyApp7", "afterpost")
            },
            { postError ->
                Log.e("MyAmplifyApp7", "Query post failure", postError)
            }
        )
        binding.swiper.isRefreshing = false
    }
}