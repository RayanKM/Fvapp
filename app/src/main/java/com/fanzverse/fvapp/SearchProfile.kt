package com.fanzverse.fvapp

import android.animation.ValueAnimator
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Comment
import com.amplifyframework.datastore.generated.model.Event
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.datastore.generated.model.Usr
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.fanzverse.fvapp.databinding.EditbioBinding
import com.fanzverse.fvapp.databinding.EntercodeBinding
import com.fanzverse.fvapp.databinding.FragmentSearchProfileBinding
import com.fanzverse.fvapp.databinding.PickbgBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID


class SearchProfile : Fragment(R.layout.fragment_search_profile) {
    private lateinit var communicator: Communicator
    private lateinit var dialog: Dialog // Declare dialog as a property
    private var users = mutableListOf<Usr>()
    private var users2 = mutableListOf<Usr>()
    private var triggered = false
    private var backgroundURL = "https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/backgrounds/pexels-nadezhda-moryak-6063469.jpg"
    private var who = ""
    private var postListWithComments = mutableListOf<PosDataModel>()
    val n = MainActivity.userN
    private lateinit var binding: FragmentSearchProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("MyAmplifyApp", "SUIIIIII ${n}")
        communicator = activity as Communicator
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.sheet3)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.isDraggable = false
        val eventSheet = BottomSheetBehavior.from(binding.addeventsheet)
        eventSheet.peekHeight = 0
        eventSheet.isDraggable = false
        fetchAll(n.toString())
        fetchMomentPost()
        fetchEventPost()
        var searchHandler: Handler? = null
        val searchView = view.findViewById<SearchView>(R.id.srchv)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                searchHandler?.removeCallbacksAndMessages(null)
                searchHandler = Handler()
                binding.searchList.visibility = if (newText.isNullOrBlank()) View.GONE else View.VISIBLE
                binding.secondList.visibility = if (newText.isNullOrBlank()) View.VISIBLE else View.GONE
                if (!newText.isNullOrBlank()) {
                    // Delayed search when there is a non-empty query
                    searchHandler?.postDelayed({
                        fetch(newText.toLowerCase())
                    }, 300) // Adjust the delay as needed
                }
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
                        communicator.passid2(id)
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
                                        communicator.passid(post.postID, post.postAuthor)
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
        binding.openMoments.setOnClickListener {
            val transaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.mn, Moments()) // Replace R.id.fragment_container with your container ID
            transaction.addToBackStack(null)
            transaction.commit()
        }
        binding.openEvents.setOnClickListener {
            val transaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.mn, Events()) // Replace R.id.fragment_container with your container ID
            transaction.addToBackStack(null)
            transaction.commit()
        }
        binding.addEvent.setOnClickListener {
            val imageUrls = listOf(
                "pexels-aleksandar-pasaric-3629227.jpg", "pexels-anna-tarazevich-6712456.jpg",
                "pexels-artem-podrez-7233116.jpg", "pexels-bongani-nkwinika-4955984.jpg",
                "pexels-calebe-miranda-793166.jpg", "pexels-clément-proust-18372784.jpg",
                "pexels-clément-proust-18372923.jpg", "pexels-elevate-1267295.jpg",
                "pexels-gergely-badacsonyi-19191404.jpg", "pexels-gergely-badacsonyi-19191433.jpg",
                "pexels-ibrahim-hafeez-1319828.jpg", "pexels-jess-loiterton-5232856.jpg",
                "pexels-jess-loiterton-6388958.jpg", "pexels-kaushal-das-870802.jpg",
                "pexels-lil-artsy-1213447.jpg", "pexels-lucas-agustín-13579238.jpg",
                "pexels-nadezhda-moryak-6063469.jpg", "pexels-oday-hazeem-130621.jpg",
                "pexels-ona-buflod-bovollen-1259343.jpg", "pexels-pixabay-54308.jpg",
                "pexels-rachel-xiao-772429.jpg", "pexels-sami-abdullah-7151514.jpg",
                "pexels-sami-anas-5642191.jpg", "pexels-santiago-pagnotta-1702624.jpg",
                "pexels-saravandy-soeung-907889.jpg", "pexels-serhii-demchenko-8261215.jpg",
                "pexels-shonejai-1227497.jpg", "pexels-tembela-bohle-1884576.jpg",
                "pexels-vishnu-r-nair-1105666.jpg", "pexels-wendy-wei-2342413.jpg",
                "pexels-zaksheuskaya-1616403.jpg"
            )

            // Convert the list of URLs to a list of ImageItem instances
            val imageItems: List<ImageItem> = imageUrls.map { ImageItem(it) }

            // Calculate the screen height for the animation.
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            // Create an animator for the animation of the first bottom sheet.
            val animator = ValueAnimator.ofInt(0, screenHeight)
            animator.addUpdateListener { valueAnimator ->
                // Update the peek height of the first bottom sheet during the animation.
                val height = valueAnimator.animatedValue as Int
                eventSheet.peekHeight = height
            }
            animator.duration = 300  // Set the duration of the animation.
            animator.start()  // Start the animation.

            binding.cancelEvent.setOnClickListener {
                hideKeyboard(it)
                closeSheet(0)
            }
            binding.privacySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                // Handle the switch state change
                if (isChecked) {
                    binding.privacySwitch.text = "Private    "
                    binding.eventCode.visibility = View.VISIBLE
                } else {
                    binding.privacySwitch.text = "Public    "
                    binding.eventCode.setText("")
                    binding.eventCode.visibility = View.GONE
                }
            }
            binding.settingsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                // Handle the switch state change
                if (isChecked) {
                    binding.settingsSwitch.text = "Everyone    "
                } else {
                    binding.settingsSwitch.text = "Organizer    "
                }
            }
            binding.pickStartdate.setOnClickListener { startDatePickerDialog() }
            binding.pickStarttime.setOnClickListener { startTimePickerDialog() }
            binding.pickEnddate.setOnClickListener { endDatePickerDialog() }
            binding.pickEndtime.setOnClickListener { endTimePickerDialog() }
            val binding2 = PickbgBinding.inflate(layoutInflater)
            val view = binding2.root
            val builder = AlertDialog.Builder(requireActivity())
            builder.setView(view)
            dialog = builder.create()
            binding.setBg.setOnClickListener {
                dialog.show()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.setCancelable(false)
                binding2.bgRecyclerview.apply {
                    layoutManager = GridLayoutManager(requireActivity().applicationContext, 3)
                    adapter = BackgroundAdapter(requireContext(), imageItems).apply {
                        setOnItemClickListener(object : BackgroundAdapter.onItemClickListener {
                            override fun onItemClick(position: Int) {
                                val url = imageItems[position].imageUrl
                                backgroundURL = "https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/backgrounds/$url"
                                Glide.with(requireActivity()).load("https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/backgrounds/$url")
                                    .into(binding.bg)
                                dialog.dismiss()
                            }
                        })
                    }
                }
                binding2.selectfromgallery.setOnClickListener {
                    ImagePicker.with(this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start()
                }
                binding2.closebg.setOnClickListener {
                    dialog.dismiss()
                }
            }
            binding.createEvent.setOnClickListener {
                hideKeyboard(it)
                if (binding.title.text.toString().isNotEmpty() && binding.desc.text.toString().isNotEmpty() && binding.location.text.toString().isNotEmpty()){
                    if(binding.Startdate.text != "Pick Date" || binding.Enddate.text != "Pick Date" || binding.Starttime.text != "Pick Time" || binding.Endtime.text != "Pick Time"){
                        val privacy: Boolean = binding.privacySwitch.isChecked
                        val settings: Boolean = binding.settingsSwitch.isChecked
                        if (privacy){
                            crtEvent(n!!,binding.title.text.toString(),binding.desc.text.toString(),binding.location.text.toString(),binding.Startdate.text.toString(),binding.Starttime.text.toString(),binding.Enddate.text.toString(),binding.Endtime.text.toString(),privacy,binding.eventCode.text.toString(),settings,backgroundURL)
                        }else{
                            crtEvent(n!!,binding.title.text.toString(),binding.desc.text.toString(),binding.location.text.toString(),binding.Startdate.text.toString(),binding.Starttime.text.toString(),binding.Enddate.text.toString(),binding.Endtime.text.toString(),privacy,"",settings,backgroundURL)
                        }
                    }
                }
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
        users2.clear()
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
        Log.e("sqdqds", "ysspf")

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

                sortedPosts.forEach { post ->
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
    fun fetchMomentPost() {
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.MEDIA.contains("https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/")),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                // Get the post IDs of the first three posts
                val firstThreePostIds = sortedPosts.take(3).map { it }
                var num = 0
                firstThreePostIds.forEach { postId ->
                    num++
                    val nm = num
                    // Do something with postId
                    runOnUiThread {
                        when {
                            postId.typ == "image" && nm == 1 -> {Glide.with(this).load(postId.media)
                                .into(binding.img1)
                                binding.m1.setOnClickListener {
                                    communicator.passid(postId.id, postId.author)
                                }
                            }

                            postId.typ == "video" && nm == 1 -> {Vidimg(postId.media, nm)
                            binding.m1.setOnClickListener {
                                communicator.passid(postId.id, postId.author)
                            }}
                            postId.typ == "image" && nm == 2 -> {Glide.with(this).load(postId.media)
                                .into(binding.img2)
                                binding.m2.setOnClickListener {
                                    communicator.passid(postId.id, postId.author)
                                }
                            }

                            postId.typ == "video" && nm == 2 -> {Vidimg(postId.media, nm)
                                binding.m2.setOnClickListener {
                                    communicator.passid(postId.id, postId.author)
                                }}
                            postId.typ == "image" && nm == 3 -> {Glide.with(this).load(postId.media)
                                .into(binding.img3)
                                binding.m3.setOnClickListener {
                                    communicator.passid(postId.id, postId.author)
                                }
                            }

                            postId.typ == "video" && nm == 3 -> {Vidimg(postId.media, nm)
                                binding.m3.setOnClickListener {
                                    communicator.passid(postId.id, postId.author)
                                }}
                            else -> {
                            }
                        }
                    }
                }
                Log.e("MyAmplifyApp7", "afterpost")
            },
            { postError ->
                Log.e("MyAmplifyApp7", "Query post failure", postError)
            }
        )

    }
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun fetchEventPost() {
        Amplify.API.query(
            ModelQuery.list(Event::class.java, Event.BACKGROUND.contains("https")),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                // Get the post IDs of the first three posts
                val firstThreePostIds = sortedPosts.take(2).map { it }
                var num = 0
                firstThreePostIds.forEach { postId ->

                    num++
                    val nm = num
                    // Do something with postId
                    runOnUiThread {
                        if (nm == 1){
                            if (postId.privacy && postId.members == null || postId.privacy && !postId.members.contains(n)) {
                                binding.eventBackground.visibility = View.GONE
                                binding.pvst.visibility = View.VISIBLE
                                Glide.with(this)
                                    .load(postId.background)
                                    .centerCrop() // You can adjust the transformation as per your requirement
                                    .into(object : CustomTarget<Drawable>() {
                                        override fun onResourceReady(
                                            resource: Drawable,
                                            transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                                        ) {
                                            binding.imgf.background = resource
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            // Do nothing here
                                        }
                                    })
                                binding.eventNamepv.text = postId.title
                            }
                            else{
                                binding.eventBackground.visibility = View.VISIBLE
                                binding.pvst.visibility = View.GONE
                                Glide.with(this)
                                    .load(postId.background)
                                    .centerCrop() // You can adjust the transformation as per your requirement
                                    .into(object : CustomTarget<Drawable>() {
                                        override fun onResourceReady(
                                            resource: Drawable,
                                            transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                                        ) {
                                            binding.eventBackground.background = resource
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            // Do nothing here
                                        }
                                    })

                                binding.eventDate.text = formatDate(postId.startDate)
                                binding.eventName.text = postId.title
                                binding.eventTime.text = postId.startTime
                                binding.e1.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("id", postId.id) // Replace "post" with the key you want to use
                                    // Perform the fragment transaction
                                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                    val frg = EventPage()
                                    frg.arguments = bundle
                                    transaction.replace(R.id.mn, frg)
                                    transaction.addToBackStack(null)
                                    transaction.commit()
                                }
                            }
                        }
                        else if(nm == 2){
                            if (postId.privacy && postId.members == null || postId.privacy && !postId.members.contains(n)) {
                                binding.eventBackground2.visibility = View.GONE
                                binding.pvst2.visibility = View.VISIBLE
                                Glide.with(this)
                                    .load(postId.background)
                                    .centerCrop() // You can adjust the transformation as per your requirement
                                    .into(object : CustomTarget<Drawable>() {
                                        override fun onResourceReady(
                                            resource: Drawable,
                                            transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                                        ) {
                                            binding.imgf2.background = resource
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            // Do nothing here
                                        }
                                    })
                                binding.eventNamepv2.text = postId.title
                            }
                            else{
                                binding.eventBackground2.visibility = View.VISIBLE
                                binding.pvst2.visibility = View.GONE
                                Glide.with(this)
                                    .load(postId.background)
                                    .centerCrop() // You can adjust the transformation as per your requirement
                                    .into(object : CustomTarget<Drawable>() {
                                        override fun onResourceReady(
                                            resource: Drawable,
                                            transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                                        ) {
                                            binding.eventBackground2.background = resource
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            // Do nothing here
                                        }
                                    })

                                binding.eventDate2.text = formatDate(postId.startDate)
                                binding.eventName2.text = postId.title
                                binding.eventTime2.text = postId.startTime
                                binding.e2.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("id", postId.id) // Replace "post" with the key you want to use
                                    // Perform the fragment transaction
                                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                    val frg = EventPage()
                                    frg.arguments = bundle
                                    transaction.replace(R.id.mn, frg)
                                    transaction.addToBackStack(null)
                                    transaction.commit()
                                }
                            }
                        }
                    }
                }
                Log.e("MyAmplifyApp7", "afterpost")
            },
            { postError ->
                Log.e("MyAmplifyApp7", "Query post failure", postError)
            }
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

    fun Vidimg(url:String, nm:Int){
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(url, HashMap())
        // Capture bitmap at a specific time (e.g., 1 second into the video)
        val timeUs = 1000000L // 1 second in microseconds
        val bitmap: Bitmap? = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

        when {
            nm == 1 -> {Glide.with(this).load(bitmap).into(binding.img1)
                binding.img1t.visibility = View.VISIBLE}
            nm == 2 -> {Glide.with(this).load(bitmap).into(binding.img2)
                binding.img2t.visibility = View.VISIBLE}
            nm == 3 -> {Glide.with(this).load(bitmap).into(binding.img3)
                binding.img3t.visibility = View.VISIBLE}
            else -> {
            }
        }

        // Release the MediaMetadataRetriever when done
        retriever.release()
    }
    private fun crtEvent(organizer:String, title:String, description:String, location:String, startDate:String, startTime:String, endDate:String, endTime: String, privacy: Boolean, code:String, settings: Boolean, background:String){
        val client = OkHttpClient()

        val url = "http://worldtimeapi.org/api/timezone/UTC"

        val request = Request.Builder()
            .url(url)
            .build()

        // Use OkHttp to enqueue the request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle network request failure here
                Log.i("MyAmplifyApp2", "faild", e)

            }

            override fun onResponse(call: Call, response: Response) {
                // Check if the response was successful
                if (!response.isSuccessful) {
                    Log.i("MyAmplifyApp2", "unss")
                    // Handle the unsuccessful response here
                } else {
                    Log.i("MyAmplifyApp2", "yss")

                    // Get the response body as a JSON string
                    val json = response.body?.string()

                    // Parse the JSON string
                    val jsonObject = JSONObject(json)

                    // Extract the "utc_datetime" value from the JSON
                    val utcDatetime = jsonObject.optString("utc_datetime")
                    Log.i("MyAmplifyApp2", "$utcDatetime")


                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault())
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                    val date = inputFormat.parse(utcDatetime)

                    val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    outputFormat.timeZone = TimeZone.getTimeZone("UTC")

                    val formattedDatetime = outputFormat.format(date)

                    val post = Event.builder()
                        .organizer(organizer)
                        .title(title)
                        .description(description)
                        .location(location)
                        .startDate(startDate)
                        .startTime(startTime)
                        .endDate(endDate)
                        .endTime(endTime)
                        .privacy(privacy)
                        .settings(settings)
                        .background(background)
                        .status(false)
                        .code(code)
                        .createdAt(formattedDatetime)
                        .build()
                    Amplify.API.mutate(
                        ModelMutation.create(post),
                        { response ->
                            activity?.runOnUiThread {
                                closeSheet(1)
                            }

                        },
                        { error ->
                            activity?.runOnUiThread {
                                closeSheet(2)
                            }
                        }
                    )

                }
            }
        })
    }
    fun closeSheet(cs: Int){
        val eventSheet = BottomSheetBehavior.from(binding.addeventsheet)
        // Create an instance of OkHttpClient
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        // Create an animator for the animation of the first bottom sheet.
        val animator = ValueAnimator.ofInt(screenHeight, 0)
        animator.addUpdateListener { valueAnimator ->
            // Update the peek height of the first bottom sheet during the animation.
            val height = valueAnimator.animatedValue as Int
            eventSheet.peekHeight = height
        }
        animator.duration = 300  // Set the duration of the animation.
        animator.start()  // Start the animation.
        when (cs) {
            0 -> {
            }
            1 -> {
                fetchEventPost()
                MotionToast.createColorToast(
                    requireActivity(),
                    "Event created Successfully",
                    "Enjoy!",
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(
                        requireActivity(),
                        www.sanju.motiontoast.R.font.helvetica_regular
                    )
                )
            }
            2 -> {
                MotionToast.createColorToast(
                    requireActivity(),
                    "Event creation failed",
                    "Retry please",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(
                        requireActivity(),
                        www.sanju.motiontoast.R.font.helvetica_regular
                    )
                )
            }
        }
    }
    private fun startDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireActivity(), R.style.Picker,
            DatePickerDialog.OnDateSetListener { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Handle the selected date
                val selectedDateCalendar = Calendar.getInstance()
                selectedDateCalendar.set(year, monthOfYear, dayOfMonth)

                // Format the selected date
                val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDateCalendar.time)

                // Update the button text with the selected date
                binding.Startdate.text = formattedDate
            },
            currentYear,
            currentMonth,
            currentDay
        )

        // Set the minimum date to the current date
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

        // Show the DatePickerDialog
        datePickerDialog.show()
    }
    private fun endDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireActivity(), R.style.Picker,
            DatePickerDialog.OnDateSetListener { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Handle the selected date
                val selectedDateCalendar = Calendar.getInstance()
                selectedDateCalendar.set(year, monthOfYear, dayOfMonth)

                // Format the selected date
                val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDateCalendar.time)

                // Update the button text with the selected date
                binding.Enddate.text = formattedDate
            },
            currentYear,
            currentMonth,
            currentDay
        )
        // Set the minimum date to the current date
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        // Show the DatePickerDialog
        datePickerDialog.show()
    }
    private fun startTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireActivity(),R.style.Picker,
            TimePickerDialog.OnTimeSetListener { view: TimePicker, hourOfDay: Int, minute: Int ->
                // Handle the selected time
                val selectedTimeCalendar = Calendar.getInstance()
                selectedTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTimeCalendar.set(Calendar.MINUTE, minute)

                // Format the selected time
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(selectedTimeCalendar.time)

                // Log the formatted time
                println("Selected Time: $formattedTime")

                // Update the button text with the selected time
                binding.Starttime.text = formattedTime
            },
            currentHour,
            currentMinute,
            true // Set to true if you want 24-hour format, false for AM/PM format
        )
        // Show the TimePickerDialog
        timePickerDialog.show()
    }
    private fun endTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireActivity(),R.style.Picker,
            TimePickerDialog.OnTimeSetListener { view: TimePicker, hourOfDay: Int, minute: Int ->
                // Handle the selected time
                val selectedTimeCalendar = Calendar.getInstance()
                selectedTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTimeCalendar.set(Calendar.MINUTE, minute)

                // Format the selected time
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(selectedTimeCalendar.time)

                // Log the formatted time
                println("Selected Time: $formattedTime")

                // Update the button text with the selected time
                binding.Endtime.text = formattedTime
            },
            currentHour,
            currentMinute,
            true // Set to true if you want 24-hour format, false for AM/PM format
        )
        // Show the TimePickerDialog
        timePickerDialog.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val uri: Uri = data?.data!!
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Uploading Image ...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val outputFile = File(requireContext().cacheDir, "compressed_image.jpg")
            val outputStream = FileOutputStream(outputFile)
            Log.i("Amplifyy", "output: $outputFile")

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
            val now = Date()
            val fileName = formatter.format(now)
            val imageFile = File(outputFile.path) // Use 'outputFile' instead of 'ImageUri'
            val key = "images/$fileName.jpg" // The S3 object key where the file will be stored

            Amplify.Storage.uploadFile(
                key,
                imageFile,
                { result ->
                    val sd = result.key
                    backgroundURL = "https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/backgrounds/$sd"
                    Glide.with(requireActivity()).load(uri)
                        .into(binding.bg)
                    progressDialog.dismiss()
                    dialog.dismiss()
                },
                { error ->
                    progressDialog.dismiss()
                    Log.e("Amplifyy", "Error uploading image", error)
                }
            )
            // Use Uri object instead of File to avoid storage permissions
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(activity, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }


}