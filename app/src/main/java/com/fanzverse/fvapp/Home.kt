package com.fanzverse.fvapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Comment
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.datastore.generated.model.Usr
import com.fanzverse.fvapp.databinding.FragmentHomeBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
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
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class Home : Fragment(R.layout.fragment_home) {
    private var player: SimpleExoPlayer? = null
    var allPosts = mutableListOf<PosDataModel>()
    var videoUrlLiveData = MutableLiveData<String>()
    var downloadUri : String? = null?:""
    var tp : String? = null?:""
    lateinit var getVideo : ActivityResultLauncher<String>
    private var postListWithComments = mutableListOf<PosDataModel>()
    private lateinit var communicator: Communicator
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        val n = MainActivity.userN
        val mainActivity = activity as? MainActivity
        mainActivity?.setSelectedItem(R.id.Home)
        player = SimpleExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player
        communicator = activity as Communicator
        fetchAll(n!!)
        binding.mainRecyclerview.apply {
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
                        likePost(n,postid,to)
                        // Notify the adapter that the data has changed
                    }
                })
            }
        }
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
    fun fetchAll(id: String) {

        allPosts.clear()
        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
            { response ->
                response.data.forEach { existingUser ->
                    if (existingUser.following != null) {
                        val following = existingUser.following
                        for (i in following) {
                            fetchPostsForUser(i)
                            Log.e("MyAmplifyApp", "$i")
                        }
                    }
                }
            },
            { responseError ->
                Log.e("MyAmplifyApp", "Query failure", responseError)
            }
        )
    }
    fun fetchPostsForUser(username: String) {
        Log.e("MyAmplifyApp5", "$username")

        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.AUTHOR.contains(username)),
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
                        val pf = async { getPfp(username) }

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
                    val post = Post.builder()
                        .content(content)
                        .author(username)
                        .media("https://media172200-yandev.s3.ap-south-1.amazonaws.com/$downloadUri")
                        .typ(typ)
                        .createdAt(formattedDatetime)
                        .build()
                    Amplify.API.mutate(
                        ModelMutation.create(post),
                        { response ->
                            Log.e("MyAmplifyApp", "${response.data.id}",)
                            // This block is executed when the mutation is successful
                            runBlocking {
                                val pf = async { getPfp(username) }
                                postListWithComments.add(0, PosDataModel(response.data.content,response.data.author,response.data.id,response.data.media,pf.await(),response.data.typ,calculateTimeAgo(response.data.createdAt),
                                    listOf(),
                                    listOf(),response.data.createdAt))}

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
            },
            { error ->
                // This block is executed when there's an error during the mutation
                Log.e("MyAmplifyApp", "Create failed", error)
                // Handle the error appropriately
            }
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
    }
    private suspend fun cmt(postId: String): MutableList<Comment> {
        val comments = mutableListOf<Comment>()
        val deferred = CompletableDeferred<MutableList<Comment>>() // Create a CompletableDeferred
        Log.e("sqdqds", "cmt", )

        Amplify.API.query(
            ModelQuery.list(Comment::class.java, Comment.POST_ID.contains(postId)),
            { commentResponse ->
                Log.e("sqdqds", "ysmct", )

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
        Log.e("sqdqds", "yesspsdt", )

        Amplify.API.query(
            ModelQuery.list(Like::class.java, Like.POST_ID.contains(postId)),
            { commentResponse ->
                Log.e("sqdqds", "yesspsdt", )

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
        Log.e("sqdqds", "ysspf", )

        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
            { postResponse ->
                Log.e("sqdqds", "yesssuse", )
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

    fun calculateTimeAgo(createdAt: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = Calendar.getInstance().time
        val postDate = sdf.parse(createdAt)
        val timeDifferenceMillis = abs(currentDate.time - postDate.time)

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
}