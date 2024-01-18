package com.fanzverse.fvapp

import android.R.attr.label
import android.app.Activity
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Comment
import com.amplifyframework.datastore.generated.model.Event
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Usr
import com.amplifyframework.datastore.generated.model.eventPost
import com.bumptech.glide.Glide
import com.fanzverse.fvapp.databinding.FragmentEventPageBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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


class EventPage : Fragment(R.layout.fragment_event_page) {
    private lateinit var binding: FragmentEventPageBinding
    var eventID: String? = null
    val n = MainActivity.userN

    var allGallery = mutableListOf<PosDataModelLite>()
    private var player: SimpleExoPlayer? = null
    var allPosts = mutableListOf<PosDataModel>()
    var videoUrlLiveData = MutableLiveData<String>()
    var downloadUri : String? = null?:""
    var tp : String? = null?:""
    private lateinit var communicator: Communicator
    lateinit var getVideo : ActivityResultLauncher<String>
    private var postListWithComments = mutableListOf<PosDataModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventPageBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getVideo = registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
            if (it != null) {
                Log.i("Amplifyy", "Selected video: $it")
                val progressDialog = ProgressDialog(requireContext())
                progressDialog.setMessage("Uploading File ...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val inputStream = requireContext().contentResolver.openInputStream(it)
                        val videoFile = File(requireContext().cacheDir, "video.mp4")

                        val outputStream = FileOutputStream(videoFile)

                        inputStream?.copyTo(outputStream)
                        inputStream?.close()
                        outputStream.close()
                        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
                        val now = Date()
                        val fileName = formatter.format(now)
                        val key = "videos/$fileName.mp4" // The S3 object key where the file will be stored

                        Amplify.Storage.uploadFile(
                            key,
                            videoFile,
                            { result ->
                                videoUrlLiveData.value = result.key
                                downloadUri = result.key
                                tp = "video"
                                progressDialog.dismiss()
                            },
                            { error ->
                                progressDialog.dismiss()
                                Log.e("Amplifyy", "Error uploading video", error)
                            }
                        )
                    } catch (e: Exception) {
                        // Handle exceptions
                    }
                }
            }
        })

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        communicator = activity as Communicator
        val n = MainActivity.userN
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.sheet3)
        bottomSheetBehavior.isDraggable = true
        val dpToPx = resources.displayMetrics.density
        bottomSheetBehavior.peekHeight = (60 * dpToPx).toInt()
        eventID = arguments?.getString("id")
        fetch(eventID!!)
        fetchEventPosts(eventID!!)
        binding.mainRecyclerview.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = HomeAdapter(requireContext(), postListWithComments).apply {
                setOnItemClickListener(object : HomeAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val post = postListWithComments[position]
                        communicator.passid(post.postID, post.postID)
                    }

                    override fun onLike(position: Int) {
                        val postid = postListWithComments[position].postID
                        likePost(n!!,postid,postid)
                        // Notify the adapter that the data has changed
                    }
                })
            }
        }
        // Notify the adapter that the data has changed
        binding.mainRecyclerview2.apply {
            layoutManager = GridLayoutManager(requireActivity().applicationContext, 3)
            adapter = MomentAdapter(requireContext(), allGallery).apply {
                setOnItemClickListener(object : MomentAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val post = allPosts[position]
                        communicator.passid(post.postID, post.postID)
                    }
                })
            }
        }
        binding.swiper.setOnRefreshListener {
            fetchAlbum()
        }

        binding.bottomNavigationView.selectedItemId = R.id.Posts
        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId){

                R.id.Posts -> fetchEventPosts(eventID!!)

                R.id.Album -> fetchAlbum()

                else ->{

                }
            }
            true
        }
        player = SimpleExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player
        binding.publish.setOnClickListener {
            val author = MainActivity.userN
            val content = binding.posttext.text.toString()
            if (content != "" || tp != ""){
                createPost(author!!, content, tp.toString())
            }else{
                MotionToast.createColorToast(
                    requireActivity(),
                    "Empty post",
                    "Please share something.",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(
                        requireActivity(),
                        www.sanju.motiontoast.R.font.helvetica_regular
                    )
                )
            }
        }
        binding.addimg.setOnClickListener {
            tp = ""
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
        binding.addvid.setOnClickListener {
            tp = ""
            getVideo.launch("video/*")
            videoUrlLiveData.observe(viewLifecycleOwner, Observer {
                if (it != null || it != "") {
                    binding.playerView.visibility = View.VISIBLE
                    val mediaItem = MediaItem.fromUri("https://media172200-yandev.s3.ap-south-1.amazonaws.com/$it")
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.play()
                }
            })

        }

    }

    fun fetch(id:String){
        Amplify.API.query(
            ModelQuery.list(Event::class.java, Event.ID.contains(id)),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                // Get the post IDs of the first three posts
                sortedPosts.forEach { postId ->
                    activity?.runOnUiThread {
                        Glide.with(this)
                            .load(postId.background) // Replace with the resource or URL of your image
                            .into(binding.background)
                        binding.location.text = postId.location
                        binding.title.text = postId.title
                        binding.description.text = postId.description
                        binding.date.text = formatDate(postId.startDate)
                        binding.time.text = postId.startTime
                        getEventStatus(postId.startDate,postId.startTime,postId.endDate,postId.endTime)
                        binding.more.setOnClickListener { view ->
                            if (postId.settings || postId.privacy){
                                showPopupMenu(view,0,postId.organizer,postId,postId.code)
                            }else if (!postId.settings || postId.privacy){
                                showPopupMenu(view,1,postId.organizer,postId,postId.code)
                            }else{
                                showPopupMenu(view,2,postId.organizer,postId,postId.code)
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
    fun getEventStatus(
        startDate: String,
        startTime: String,
        endDate: String,
        endTime: String
    ): Any {
        val dateFormat = SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault())

        val currentDate = Date()
        val startDateFormatted = dateFormat.parse("$startDate $startTime")
        val endDateFormatted = dateFormat.parse("$endDate $endTime")

        return when {
            currentDate.before(startDateFormatted) -> binding.status.text = "Upcoming"
            currentDate.after(endDateFormatted) -> binding.status.text = "Completed"
            else -> binding.status.text = "Ongoing"
        }
    }
    fun fetchEventPosts(eventId: String) {
        binding.albumview.visibility = View.GONE
        binding.postview.visibility = View.VISIBLE
        allPosts.clear()
        Amplify.API.query(
            ModelQuery.list(eventPost::class.java, eventPost.EVENT_ID.contains(eventId)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    val postId = post.id
                    val postAuthor = post.author
                    val postContent = post.content
                    Log.e("MyAmplifyApp5", "${postContent}")
                    val postType = post.typ
                    val postDate = post.createdAt
                    val timeAgo = calculateTimeAgo(postDate)
                    val postMedia: String = post.media ?: ""

                    runBlocking {
                        val cm = async { cmt(postId) }
                        val lk = async { lks(postId) }
                        val pf = async { getPfp(postAuthor) }

                        val postWithComments = PosDataModel(postContent, postAuthor, postId, postMedia, pf.await(), postType, timeAgo, cm.await(), lk.await(), postDate)
                        allPosts.add(postWithComments)
                        updateUI(allPosts)
                    }
                }
                Log.e("MyAmplifyApp5", "afterpost")
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
            }
        )
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
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun updateUI(posts: List<PosDataModel>) {
        // Sort the posts and update the UI
        val sortedPosts = posts.sortedByDescending { it.sort }

        postListWithComments.clear()
        postListWithComments.addAll(sortedPosts)

        activity?.runOnUiThread {
            // Notify the adapter that the data has changed
            binding.mainRecyclerview.adapter?.notifyDataSetChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val uri: Uri = data?.data!!
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Uploading File ...")
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
                    binding.main.visibility = View.VISIBLE
                    binding.main.setImageURI(uri)
                    Log.i("Amplifyy", "uppp")
                    downloadUri = result.key
                    Log.i("Amplifyy", "$downloadUri")
                    progressDialog.dismiss()
                    tp = "image"
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

    private suspend fun cmt(postId: String): MutableList<Comment> {
        val comments = mutableListOf<Comment>()
        val deferred = CompletableDeferred<MutableList<Comment>>() // Create a CompletableDeferred
        Log.e("sqdqds", "cmt")

        Amplify.API.query(
            ModelQuery.list(Comment::class.java, Comment.POST_ID.contains(postId)),
            { commentResponse ->
                Log.e("sqdqds", "ysmct")

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
        Log.e("sqdqds", "yesspsdt")

        Amplify.API.query(
            ModelQuery.list(Like::class.java, Like.POST_ID.contains(postId)),
            { commentResponse ->
                Log.e("sqdqds", "yesspsdt")

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
        Log.e("sqdqds", "ysspf")

        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
            { postResponse ->
                Log.e("sqdqds", "yesssuse")
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

    fun createPost(username: String, content: String, typ:String) {
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


                    hideKeyboard(binding.posttext)
                    val post = eventPost.builder()
                        .content(content)
                        .author(username)
                        .eventId(eventID)
                        .media("https://media172200-yandev.s3.ap-south-1.amazonaws.com/$downloadUri")
                        .typ(typ)
                        .createdAt(formattedDatetime)
                        .build()
                    Amplify.API.mutate(
                        ModelMutation.create(post),
                        { response ->
                            Log.e("MyAmplifyApp", "${response.data.id}")
                            runBlocking {
                                val pf = async { getPfp(username) }
                                postListWithComments.add(0, PosDataModel(response.data.content,response.data.author,response.data.id,response.data.media,pf.await(),response.data.typ,calculateTimeAgo(response.data.createdAt),
                                    listOf(),
                                    listOf(),response.data.createdAt))}
                            Amplify.API.query(
                                ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(username)),
                                { response ->
                                    response.data.forEach { existingUser ->
                                        val updatedUser = existingUser.copyOfBuilder()
                                            .id(existingUser.id)
                                            .news(UUID.randomUUID().toString())
                                            .build()
                                        // Perform the update mutation with the modified user object
                                        Amplify.API.mutate(
                                            ModelMutation.update(updatedUser),
                                            { updateResponse ->
                                                // This block is executed when the mutation is successful

                                                activity?.runOnUiThread {
                                                    binding.mainRecyclerview.adapter?.notifyDataSetChanged()

                                                    binding.posttext.text.clear()
                                                    downloadUri = ""
                                                    tp = ""
                                                    videoUrlLiveData.value = ""
                                                    binding.playerView.visibility = View.GONE
                                                    binding.main.visibility = View.GONE

                                                    MotionToast.createColorToast(
                                                        requireActivity(),
                                                        "Post created Successfully",
                                                        "You can see it on your profile",
                                                        MotionToastStyle.SUCCESS,
                                                        MotionToast.GRAVITY_BOTTOM,
                                                        MotionToast.LONG_DURATION,
                                                        ResourcesCompat.getFont(
                                                            requireActivity(),
                                                            www.sanju.motiontoast.R.font.helvetica_regular
                                                        )
                                                    )
                                                }
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
                            // Handle any other logic you need here for a successful mutation
                        },
                        { error ->
                            // This block is executed when there's an error during the mutation
                            Log.e("MyAmplifyApp", "Create failed", error)
                            // Handle the error appropriately
                        }
                    )
                    // Now you have the "utc_datetime" value
                    // You can use it as needed
                }
            }
        })

    }
    fun likePost(id: String, postid: String,to:String) {
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
                activity?.runOnUiThread {
                    updateUI(allPosts)
                }
            },
            { error ->
                // This block is executed when there's an error during the mutation
                Log.e("MyAmplifyApp", "Create failed", error)
                // Handle the error appropriately
            }
        )
    }

    fun fetchAlbum(){
        binding.albumview.visibility = View.VISIBLE
        binding.postview.visibility = View.GONE

        allGallery.clear()
        Amplify.API.query(
            ModelQuery.list(eventPost::class.java, eventPost.MEDIA.contains("https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/")),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                // Get the post IDs of the first three posts
                sortedPosts.forEach { postId ->
                    val postWithComments = PosDataModelLite(postId.author, postId.id, postId.media,postId.typ)
                    allGallery.add(postWithComments)
                    activity?.runOnUiThread {
                        // Notify the adapter that the data has changed
                        binding.mainRecyclerview2.adapter?.notifyDataSetChanged()
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
    private fun showPopupMenu(view: View, case: Int, organiser:String, id:Event, code:String) {
        val popupMenu = PopupMenu(requireContext(), view)
        if (case == 0){
            // Add menu items using a list
            val menuItems = listOf("Delete Event","Copy Code")

            for ((index, title) in menuItems.withIndex()) {
                popupMenu.menu.add(0, index, index, title)
            }

            // Set an item click listener for the menu items
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                // Handle item click
                when (item.itemId) {
                    0 -> {
                        if (n == organiser) {
                            val sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Are you sure?")
                                .setContentText("This action cannot be undone.")
                                .setCancelText("Cancel")
                                .setConfirmText("Delete")
                                .showCancelButton(true)
                                .setCancelClickListener { sDialog ->
                                    sDialog.dismissWithAnimation()
                                    // Handle cancel button click
                                }
                                .setConfirmClickListener { sDialog ->
                                    sDialog.dismissWithAnimation()
                                    deleteEvent(id)
                                }

                            sweetAlertDialog.show()

                            true
                        } else {
                            false
                        }
                    }
                    1 -> {
                        copyToClipboard(code)
                        true
                        // Handle Item 2 click

                    }
                    // Add more cases for other items as needed
                    else -> false
                }
            }

            // Show the PopupMenu
            popupMenu.show()
        }
        else if(case == 1){
            // Add menu items using a list
            val menuItems = listOf("Delete Event","Copy Code")

            for ((index, title) in menuItems.withIndex()) {
                popupMenu.menu.add(0, index, index, title)
            }

            // Set an item click listener for the menu items
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                // Handle item click
                when (item.itemId) {
                    0 -> {
                        if (n == organiser) {
                            val sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Are you sure?")
                                .setContentText("This action cannot be undone.")
                                .setCancelText("Cancel")
                                .setConfirmText("Delete")
                                .showCancelButton(true)
                                .setCancelClickListener { sDialog ->
                                    sDialog.dismissWithAnimation()
                                    // Handle cancel button click
                                }
                                .setConfirmClickListener { sDialog ->
                                    sDialog.dismissWithAnimation()
                                    deleteEvent(id)
                                }

                            sweetAlertDialog.show()
                            true
                        } else {
                            false
                        }
                    }
                    1 -> {
                        copyToClipboard(code)
                        true
                        // Handle Item 2 click

                    }
                    // Add more cases for other items as needed
                    else -> false
                }
            }

            // Show the PopupMenu
            popupMenu.show()
        }
        else if(case == 2){
            // Add menu items using a list
            val menuItems = listOf("Delete Event")

            for ((index, title) in menuItems.withIndex()) {
                popupMenu.menu.add(0, index, index, title)
            }

            // Set an item click listener for the menu items
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                // Handle item click
                when (item.itemId) {
                    0 -> {
                        if (n == organiser) {
                            val sweetAlertDialog = SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Are you sure?")
                                .setContentText("This action cannot be undone.")
                                .setCancelText("Cancel")
                                .setConfirmText("Delete")
                                .showCancelButton(true)
                                .setCancelClickListener { sDialog ->
                                    sDialog.dismissWithAnimation()
                                    // Handle cancel button click
                                }
                                .setConfirmClickListener { sDialog ->
                                    sDialog.dismissWithAnimation()
                                    deleteEvent(id)
                                }

                            sweetAlertDialog.show()
                            true
                        } else {
                            false
                        }
                    }
                    // Add more cases for other items as needed
                    else -> false
                }
            }

            // Show the PopupMenu
            popupMenu.show()
        }
    }
    private fun copyToClipboard(textToCopy: String) {
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Copied Text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        MotionToast.createColorToast(
            requireActivity(),
            "Code copied to clipboard",
            textToCopy,
            MotionToastStyle.INFO,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(
                requireActivity(),
                www.sanju.motiontoast.R.font.helvetica_regular
            )
        )
    }
    private fun deleteEvent(id:Event){
        Amplify.API.mutate(
            ModelMutation.delete(id),
            {
                Amplify.Auth.deleteUser(
                    {
                        activity?.runOnUiThread {

                        }
                    },
                    { error ->
                        // Handle the error
                        Log.e("AuthQuickStart", "Error deleting user", error)
                    }
                )

            },
            { Log.e("MyAmplifyApp", "Delete failed", it) }
        )
    }
    override fun onResume() {
        super.onResume()
        binding.bottomNavigationView.selectedItemId = R.id.Posts


    }

}