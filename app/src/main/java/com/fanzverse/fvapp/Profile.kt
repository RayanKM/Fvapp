package com.fanzverse.fvapp

import android.animation.ValueAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Comment
import com.amplifyframework.datastore.generated.model.FollowRequest
import com.amplifyframework.datastore.generated.model.FollowRequestStatus
import com.amplifyframework.datastore.generated.model.Like
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.datastore.generated.model.Usr
import com.bumptech.glide.Glide
import com.fanzverse.fvapp.databinding.EditbioBinding
import com.fanzverse.fvapp.databinding.FragmentProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import gen._base._base_java__assetres.srcjar.R.id.async
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class Profile : Fragment(R.layout.fragment_profile) {
    var bio = ""
    private lateinit var communicator: Communicator
    private var postListWithComments = mutableListOf<PosDataModel>()
    val n = MainActivity.userN
    val User = MainActivity.userID
    private lateinit var binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ppgfp.clipToOutline = true


        // Initialize the switch based on the user's preference
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        binding.switchDarkMode.isChecked = sharedPreferences.getBoolean("dark_mode_enabled", false)


        val bottomSheetBehavior = BottomSheetBehavior.from(binding.sheet2)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.isDraggable = false

        communicator = activity as Communicator
        var post: String? = arguments?.getString("id")

        if (post == null || post == n){
            fetch(n!!)
            fetchLikes(n!!)

        }
        else{
            fetch(post)
            fetchPost(post)
            fetchLikes(post)
            binding.more.visibility = View.GONE
            binding.onit.visibility = View.GONE
            checkfollow(post.toString(),n!!)
        }

        binding.down.setOnClickListener {
            // Calculate the screen height for the animation.
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels

            // Create an animator for the animation of the first bottom sheet.
            val animator = ValueAnimator.ofInt(screenHeight, 0)
            animator.addUpdateListener { valueAnimator ->
                // Update the peek height of the first bottom sheet during the animation.
                val height = valueAnimator.animatedValue as Int
                bottomSheetBehavior.peekHeight = height
            }
            animator.duration = 500  // Set the duration of the animation.
            animator.start()  // Start the animation.

        }
        binding.more.setOnClickListener {
            binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                val mainActivity = activity as? MainActivity
                mainActivity?.setSelectedItem(R.id.Home)
                val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("dark_mode_enabled", isChecked).apply()

                // Apply the theme change
                ThemeManager.applyTheme(isChecked, requireActivity() as AppCompatActivity)
            }
            // Calculate the screen height for the animation.
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            // Create an animator for the animation of the first bottom sheet.
            val animator = ValueAnimator.ofInt(0, screenHeight)
            animator.addUpdateListener { valueAnimator ->
                // Update the peek height of the first bottom sheet during the animation.
                val height = valueAnimator.animatedValue as Int
                bottomSheetBehavior.peekHeight = height
            }
            animator.duration = 500  // Set the duration of the animation.
            animator.start()  // Start the animation.
        }
        binding.edtpfp.setOnClickListener {
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
        binding.edtbio.setOnClickListener {
            val binding = EditbioBinding.inflate(layoutInflater)
            val view = binding.root
            val builder = AlertDialog.Builder(requireActivity())
            builder.setView(view)
            val dialog = builder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)
            binding.biotxt.setText(bio)
            binding.snd.setOnClickListener {
                val nbio = binding.biotxt.text.toString()
                Amplify.API.query(
                    ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(n)),
                    { response ->
                        response.data.forEach { existingUser ->
                            val updatedUser = existingUser.copyOfBuilder().id(existingUser.id)
                                .bio(nbio)
                                .build()
                            // Perform the update mutation with the modified user object
                            Amplify.API.mutate(
                                ModelMutation.update(updatedUser),
                                { updateResponse ->
                                    fetch(n!!)
                                    activity?.runOnUiThread {
                                        val dpToPx = resources.displayMetrics.density
                                        val animator = ValueAnimator.ofInt((250*dpToPx).toInt(), 0)
                                        animator.addUpdateListener { valueAnimator ->
                                            val height = valueAnimator.animatedValue as Int
                                            bottomSheetBehavior.peekHeight = height
                                        }
                                        animator.duration = 500
                                        animator.start()
                                        // Handle the successful update
                                        dialog.dismiss()
                                    }

                                    Log.i("Amplify", "User updated: ${updateResponse.data}")
                                },
                                { error ->
                                    // Handle the error
                                    dialog.dismiss()
                                    Log.e("Amplify", "Error updating user", error)
                                }
                            )
                        }
                    },
                    { Log.e("MyAmplifyApp", "Query failure", it)
                        dialog.dismiss()}
                )
            }
            binding.cn.setOnClickListener {
                dialog.dismiss()
            }
        }
        binding.posts.setOnClickListener {
            binding.filter.visibility = View.GONE
            fetchPost(n)
            binding.mainRecyclerview.visibility = View.VISIBLE
        }
        binding.gallery.setOnClickListener {
            fetchGalleryAll()
            binding.filter.visibility = View.VISIBLE
            binding.mainRecyclerview.visibility = View.VISIBLE
        }
        binding.logout.setOnClickListener {
            val options = AuthSignOutOptions.builder()
                .globalSignOut(true)
                .build()
            Amplify.Auth.signOut(options) { signOutResult ->
                when(signOutResult) {
                    is AWSCognitoAuthSignOutResult.CompleteSignOut -> {
                        val intent = Intent(requireContext(), Login::class.java)
                        startActivity(intent)
                    }
                    is AWSCognitoAuthSignOutResult.PartialSignOut -> {
                        // handle partial sign out
                    }
                    is AWSCognitoAuthSignOutResult.FailedSignOut -> {
                        // handle failed sign out
                    }
                }
            }
        }
        binding.dltacc.setOnClickListener {
            showDeleteConfirmationDialog(0)
        }
        binding.dltmedia.setOnClickListener {
            showDeleteConfirmationDialog(1)
        }
        binding.dltdata.setOnClickListener {
            showDeleteConfirmationDialog(2)
        }
        binding.snd.setOnClickListener {
            sendFollowRequest(n,post.toString())
        }
        binding.filter.setOnClickListener { view ->
            showDropdownMenu(view)
        }

    }

    private fun showDeleteConfirmationDialog(case:Int) {
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
                // Handle confirm (delete) button click
                when (case) {
                    0 -> {
                        deleteAccount(User)
                    }
                    1 -> {
                        deleteMedia(User)
                    }
                    else -> {
                        deleteData(User)
                    }
                }
            }

        sweetAlertDialog.show()
    }
    fun fetch(id: String) {
        Log.e("azzzz", "fetch $id")
        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
            { postResponse ->
                Log.e("azzzz", "respo")
                postResponse.data.forEach { post ->
                    Log.e("azzzz", "${post.pfp}")
                    if (isAdded){
                        activity?.runOnUiThread {
                            binding.tag.text = "@${post.username}"
                            binding.author.text = post.fullname
                            binding.bio.text = post.bio?:""
                            bio = post.bio?:""
                            binding.followers.text = "${post.followers?.size!! - 1}"
                            binding.following.text = "${post.following?.size!! - 1}"
                            if (post.pfp != ""){
                                Glide.with(this)
                                    .load(post.pfp)
                                    .into(binding.ppgfp)
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
    fun sendFollowRequest(from:String, to:String){
        val request = FollowRequest.builder()
            .fromUser(from)
            .toUser(to)
            .status(FollowRequestStatus.PENDING)
            .build()
        Amplify.API.mutate(
            ModelMutation.create(request),
            { response ->
                activity?.runOnUiThread {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Follow request sent successfully",
                        "Wait for the user to accept",
                        MotionToastStyle.SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(),
                            www.sanju.motiontoast.R.font.helvetica_regular
                        )
                    )
                }
                // This block is executed when the mutation is successful
                // Handle any other logic you need here for a successful mutation
            },
            { error ->
                // This block is executed when there's an error during the mutation
                Log.e("MyAmplifyApp", "Create failed", error)
                // Handle the error appropriately
            }
        )
    }
    fun fetchPost(id: String) {
        binding.mainRecyclerview.apply {
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
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.AUTHOR.contains(id)),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order

                sortedPosts?.forEach { post ->
                    val postId = post.id
                    val postAuthor = post.author
                    val postContent = post.content
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
                    binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                }
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
    fun checkfollow(user:String,n:String){
        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(n)),
            { response ->
                response.data.forEach { existingUser ->
                    if (existingUser.following != null){
                        val following = existingUser.following
                        if (following.any{ it.name == user} && isAdded) {
                            activity?.runOnUiThread {
                                binding.mainRecyclerview.apply {
                                    layoutManager = LinearLayoutManager(this.context)
                                    adapter = HomeAdapter(requireContext(), postListWithComments).apply {
                                        setOnItemClickListener(object : HomeAdapter.onItemClickListener {
                                            override fun onItemClick(position: Int) {
                                                val post = postListWithComments[position]
                                                communicator.passid(post.postID, post.postAuthor)
                                            }
                                            override fun onLike(position: Int) {

                                            }
                                        })
                                    }
                                }
                            }
                        }else{
                            activity?.runOnUiThread {
                                binding.snd.visibility = View.VISIBLE
                                binding.onit.visibility = View.GONE

                            }
                        }
                    }
                    else{
                        activity?.runOnUiThread {
                            binding.snd.visibility = View.VISIBLE
                            binding.onit.visibility = View.GONE

                        }
                    }
                }
            },
            { Log.e("MyAmplifyApp", "Query failure", it) }
        )
    }
    fun fetchLikes(user:String){
        Amplify.API.query(
            ModelQuery.list(Like::class.java, Like.TO.contains(user)),
            { response ->
                var likeCount = 0
                for (item in response.data.items) {
                    likeCount++
                }
                binding.likes.text = "$likeCount"
            },
            { Log.e("MyAmplifyApp", "Query failure", it) }
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
                    Amplify.API.query(
                        ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(n)),
                        { response ->
                            response.data.forEach { existingUser ->
                                val updatedUser = existingUser.copyOfBuilder().id(existingUser.id)
                                    .pfp("https://media172200-yandev.s3.ap-south-1.amazonaws.com/${result.key}")
                                    .build()
                                // Perform the update mutation with the modified user object
                                Amplify.API.mutate(
                                    ModelMutation.update(updatedUser),
                                    { updateResponse ->
                                        fetch(n!!)
                                        progressDialog.dismiss()
                                        // Handle the successful update
                                        Log.i("Amplify", "User updated: ${updateResponse.data}")
                                    },
                                    { error ->
                                        // Handle the error
                                        progressDialog.dismiss()
                                        Log.e("Amplify", "Error updating user", error)
                                    }
                                )
                            }
                        },
                        { Log.e("MyAmplifyApp", "Query failure", it) }
                    )
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
    data class AfterDelete(val User: Int, val Comment: Int, val Like: Int)
    private suspend fun DeleteUser(id: Usr): AfterDelete {
        val deferred = CompletableDeferred<AfterDelete>() // Create a CompletableDeferred
        // Initialize values to 0
        var likeValue = 0
        var commentValue = 0
        var userValue = 0
        Amplify.API.query(
            ModelQuery.list(Like::class.java, Like.USERNAME.contains(id.username)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    Amplify.API.mutate(
                        ModelMutation.delete(post),
                        {
                        },
                        { Log.e("MyAmplifyApp", "Delete failed", it) }
                    )
                }
                likeValue = 1
                if (likeValue == 1 && commentValue == 1 && userValue == 1) {
                    // All operations are completed, resolve the deferred
                    deferred.complete(AfterDelete(likeValue, commentValue, userValue))
                }
                Log.e("MyAmplifyApp5", "DELETED")
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
            }
        )
        Amplify.API.query(
            ModelQuery.list(Comment::class.java, Comment.AUTHOR.contains(id.username)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    Amplify.API.mutate(
                        ModelMutation.delete(post),
                        {
                        },
                        { Log.e("MyAmplifyApp", "Delete failed", it) }
                    )
                }
                commentValue = 1
                if (likeValue == 1 && commentValue == 1 && userValue == 1) {
                    // All operations are completed, resolve the deferred
                    deferred.complete(AfterDelete(likeValue, commentValue, userValue))
                }
                Log.e("MyAmplifyApp5", "DELETED")
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
            }
        )
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.AUTHOR.contains(id.username)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    Amplify.API.mutate(
                        ModelMutation.delete(post),
                        {
                        },
                        { Log.e("MyAmplifyApp", "Delete failed", it) }
                    )
                }
                userValue = 1
                if (likeValue == 1 && commentValue == 1 && userValue == 1) {
                    // All operations are completed, resolve the deferred
                    deferred.complete(AfterDelete(likeValue, commentValue, userValue))
                }
                //here we should put value User = 1
                Log.e("MyAmplifyApp5", "DELETED")

            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
            }
        )

        return deferred.await()
    }
    fun deleteAccount(id: Usr){
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.sheet2)
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        // Create an animator for the animation of the first bottom sheet.
        val animator = ValueAnimator.ofInt(screenHeight, 0)
        animator.addUpdateListener { valueAnimator ->
            // Update the peek height of the first bottom sheet during the animation.
            val height = valueAnimator.animatedValue as Int
            bottomSheetBehavior.peekHeight = height
        }
        animator.duration = 500  // Set the duration of the animation.
        animator.start()  // Start the animation.
        binding.loading.visibility = View.VISIBLE
        runBlocking {
            val Check = async { DeleteUser(id) }

            if (Check.await().Like == 1 && Check.await().Comment == 1 && Check.await().User == 1){
                Amplify.API.mutate(
                    ModelMutation.delete(id),
                    {
                        Amplify.Auth.deleteUser(
                            {
                                activity?.runOnUiThread {
                                    binding.loading.visibility = View.GONE
                                    Log.i("AuthQuickStart", "ALL DONE DELETE")
                                    val intent = Intent(requireContext(), Login::class.java)
                                    startActivity(intent)
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
        }

    }
    fun deleteMedia(id: Usr){
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.sheet2)
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        // Create an animator for the animation of the first bottom sheet.
        val animator = ValueAnimator.ofInt(screenHeight, 0)
        animator.addUpdateListener { valueAnimator ->
            // Update the peek height of the first bottom sheet during the animation.
            val height = valueAnimator.animatedValue as Int
            bottomSheetBehavior.peekHeight = height
        }
        animator.duration = 500  // Set the duration of the animation.
        animator.start()  // Start the animation.
        binding.loading.visibility = View.VISIBLE
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.AUTHOR.contains(id.username)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    if (post.media.equals("https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/")){
                        Amplify.API.mutate(
                            ModelMutation.delete(post),
                            {
                            },
                            { Log.e("MyAmplifyApp", "Delete failed", it) }
                        )
                    }
                }
                binding.loading.visibility = View.GONE
                Log.e("MyAmplifyApp5", "afterpost")
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
            }
        )
    }
    fun deleteData(id: Usr){
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.sheet2)
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        // Create an animator for the animation of the first bottom sheet.
        val animator = ValueAnimator.ofInt(screenHeight, 0)
        animator.addUpdateListener { valueAnimator ->
            // Update the peek height of the first bottom sheet during the animation.
            val height = valueAnimator.animatedValue as Int
            bottomSheetBehavior.peekHeight = height
        }
        animator.duration = 500  // Set the duration of the animation.
        animator.start()  // Start the animation.
        binding.loading.visibility = View.VISIBLE
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.AUTHOR.contains(id.username)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    Amplify.API.mutate(
                        ModelMutation.delete(post),
                        {
                        },
                        { Log.e("MyAmplifyApp", "Delete failed", it) }
                    )
                }
                binding.loading.visibility = View.GONE
                Log.e("MyAmplifyApp5", "afterpost")
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
            }
        )
    }
    fun fetchGalleryAll(){
        var allPosts = mutableListOf<PosDataModelLite>()
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
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.AUTHOR.contains(n)),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                // Get the post IDs of the first three posts
                sortedPosts.forEach { postId ->
                    if (postId.media.contains("https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/")){
                        val postWithComments = PosDataModelLite(postId.author, postId.id, postId.media,postId.typ)
                        allPosts.add(postWithComments)
                        activity?.runOnUiThread {
                            // Notify the adapter that the data has changed
                            binding.mainRecyclerview.adapter?.notifyDataSetChanged()
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
    fun fetchGalleryMonthly(){
        val galleryList: MutableList<GalleryModel> = mutableListOf()
        binding.mainRecyclerview.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = GalleryAdapter(requireContext(), galleryList).apply {
                setOnItemClickListener(object : GalleryAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                    }
                })
            }
        }
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.AUTHOR.contains(n)),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)


                // Iterate over the posts and group them by month
                sortedPosts.forEach { postId ->
                    if (postId.media.contains("https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/")){
                        val date = inputFormat.parse(postId.createdAt)
                        val formattedDate = outputFormat.format(date)

                        // Find or create a GalleryModel for the current month
                        val galleryModel = galleryList.find { it.date == formattedDate }
                            ?: GalleryModel(formattedDate, mutableListOf()).also { galleryList.add(it) }


                        // Add the current post to the list for the current month
                        galleryModel.gallery.add(PosDataModelLite(postId.author, postId.id, postId.media,postId.typ))
                        activity?.runOnUiThread {
                            // Notify the adapter that the data has changed
                            binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                        }
                    }
                }

                // Iterate over the list of GalleryModel instances
                Log.e("datecheck", "afterpost")
            },
            { postError ->
                Log.e("datecheck", "Query post failure", postError)
            }
        )
    }
    fun fetchGalleryYearly(){
        val galleryList: MutableList<GalleryModel> = mutableListOf()
        binding.mainRecyclerview.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = GalleryAdapter(requireContext(), galleryList).apply {
                setOnItemClickListener(object : GalleryAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                    }
                })
            }
        }
        Amplify.API.query(
            ModelQuery.list(Post::class.java, Post.AUTHOR.contains(n)),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)


                // Iterate over the posts and group them by month
                sortedPosts.forEach { postId ->
                    if (postId.media.contains("https://media172200-yandev.s3.ap-south-1.amazonaws.com/public/")){
                        val date = inputFormat.parse(postId.createdAt)
                        val formattedDate = outputFormat.format(date)

                        // Find or create a GalleryModel for the current month
                        val galleryModel = galleryList.find { it.date == formattedDate }
                            ?: GalleryModel(formattedDate, mutableListOf()).also { galleryList.add(it) }


                        // Add the current post to the list for the current month
                        galleryModel.gallery.add(PosDataModelLite(postId.author, postId.id, postId.media,postId.typ))
                        activity?.runOnUiThread {
                            // Notify the adapter that the data has changed
                            binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                        }
                    }
                }

                // Iterate over the list of GalleryModel instances
                Log.e("datecheck", "afterpost")
            },
            { postError ->
                Log.e("datecheck", "Query post failure", postError)
            }
        )
    }
    private fun showDropdownMenu(anchorView: View) {
        val popupMenu = PopupMenu(requireContext(), anchorView)
        val menuInflater = popupMenu.menuInflater
        menuInflater.inflate(R.menu.nav3, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.all -> {
                    fetchGalleryAll()
                    true
                }
                R.id.monthly -> {
                    fetchGalleryMonthly()
                    true
                }
                R.id.yearly -> {
                    fetchGalleryYearly()
                    true
                }
                // Add more items as needed
                else -> false
            }
        }

        popupMenu.show()
    }
}
