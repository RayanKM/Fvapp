package com.example.fvapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.fvapp.databinding.FragmentHomeBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment(R.layout.fragment_home) {
    private var player: SimpleExoPlayer? = null
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
        fetchPosts(n!!)
        binding.mainRecyclerview.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = HomeAdapter(requireContext(), postListWithComments).apply {
                setOnItemClickListener(object : HomeAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        Log.i("MyAmplifyApp", "CLICKED")
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
            createPost(author!!, content, tp.toString())
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
    fun fetch(id: String) {
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.AUTHOR.contains(id)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    val postId = post.id
                    val postAuthor = post.author
                    val postContent = post.content
                    val postType = post.typ
                    val postMedia: String = post.media ?: ""

                    runBlocking {
                        val cm = async { cmt(postId) }
                        val lk = async { lks(postId) }
                        val pf = async { getPfp(id) }
                        // Create a PostWithComments object and add it to the list
                        val postWithComments =
                            PosDataModel(postContent, postAuthor, postId, postMedia,pf.await(),postType, cm.await(), lk.await())
                        postListWithComments.add(postWithComments)
                    }
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
    fun fetchPosts(id: String) {
        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
            { response ->
                response.data.forEach { existingUser ->
                    if (existingUser.following != null) {
                        val following = existingUser.following
                        for (i in following) {
                            fetch(i)
                        }
                    } else {
                    }
                }
            },
            { Log.e("MyAmplifyApp", "Query failure", it) }
        )
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
    fun createPost(username: String, content: String, typ:String) {
        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(username)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    val post = Post.builder()
                        .content(content)
                        .author(username)
                        .media("https://media172200-yandev.s3.ap-south-1.amazonaws.com/$downloadUri")
                        .typ(typ)
                        .build()
                    Amplify.API.mutate(
                        ModelMutation.create(post),
                        { response ->
                            Log.e("MyAmplifyApp", "${response.data.id}",)
                            // This block is executed when the mutation is successful
                            activity?.runOnUiThread {
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
                }
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
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
}