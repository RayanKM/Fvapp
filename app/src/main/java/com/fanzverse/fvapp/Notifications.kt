package com.fanzverse.fvapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.FollowRequest
import com.amplifyframework.datastore.generated.model.FollowRequestStatus
import com.amplifyframework.datastore.generated.model.Usr
import com.fanzverse.fvapp.databinding.FragmentNotificationsBinding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class Notifications : Fragment(R.layout.fragment_notifications) {
    private lateinit var communicator: Communicator
    private var Requests = mutableListOf<ReqDataModel>()
    private var Req = mutableListOf<FollowRequest>()
    private lateinit var binding: FragmentNotificationsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val n = MainActivity.userN

        communicator = activity as Communicator

        followRequests(n!!)
        binding.mainRecyclerview3.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = RequestsAdapter(requireContext(), Requests).apply {
                setOnItemClickListener(object : RequestsAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val from : List<FollowRequest> = Requests[position].requests
                        communicator.passid2(from[position].fromUser)
                    }
                    override fun onAcceptClick(position: Int) {
                        val from : List<FollowRequest> = Requests[position].requests
                        acceptFollowRequest(n,from[position].fromUser,from[position])
                    }
                    override fun onDeclineClick(position: Int) {
                        val from : List<FollowRequest> = Requests[position].requests
                        declineFollowRequest(n,from[position])
                    }
                })
            }
        }
    }

    fun followRequests(id:String){
        Req.clear()
        Requests.clear()
        Amplify.API.query(
            ModelQuery.list(FollowRequest::class.java, FollowRequest.TO_USER.contains(id)),
            { postResponse ->
                postResponse.data.forEach { post ->
                    Log.e("MyAmplifyApp", "notf ${post.fromUser}", )
                    Req.add(post)
                    activity?.runOnUiThread {
                        // Notify the adapter that the data has changed
                        binding.mainRecyclerview3.adapter?.notifyDataSetChanged()
                    }
                    runBlocking {

                        val pf = async { getPfp(post.fromUser) }

                        // Create a PostWithComments object and add it to the list
                        val Reqs = ReqDataModel(pf.await(),Req)
                        Requests.add(Reqs)
                    }
                }
            },
            { postError ->
                Log.e("MyAmplifyApp", "Query post failure", postError)
            }
        )
    }
    fun acceptFollowRequest(id:String, userid: String, dlt: FollowRequest){
        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(id)),
            { response ->
                response.data.forEach { existingUser ->
                    val following = existingUser.followers
                    following.add(userid)
                    val updatedUser = existingUser.copyOfBuilder().id(existingUser.id)
                        .followers(following)
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

        Amplify.API.query(
            ModelQuery.list(Usr::class.java, Usr.USERNAME.contains(userid)),
            { response ->
                response.data.forEach { existingUser ->
                    val following = existingUser.following
                    following.add(com.amplifyframework.datastore.generated.model.Person.builder().name(id).news("none").build())
                    val updatedUser = existingUser.copyOfBuilder().id(existingUser.id)
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

        val request = dlt.copyOfBuilder().status(FollowRequestStatus.ACCEPTED)
            .build()

        Amplify.API.mutate(
            ModelMutation.update(request),
            { response ->
                activity?.runOnUiThread {
                    followRequests(id)
                    // Notify the adapter that the data has changed
                    binding.mainRecyclerview3.adapter?.notifyDataSetChanged()
                }
                // This block is executed when the mutation is successful
                // Handle any other logic you need here for a successful mutation
                Log.i("Amplify", "User updated: ${response.data}")
            },
            { error ->
                // This block is executed when there's an error during the mutation
                Log.e("MyAmplifyApp", "delete failed", error)
                // Handle the error appropriately
            })
    }
    fun declineFollowRequest(id:String, dlt: FollowRequest){
        val request = dlt.copyOfBuilder().status(FollowRequestStatus.REJECTED)
            .build()

        Amplify.API.mutate(
            ModelMutation.update(request),
            { response ->
                activity?.runOnUiThread {
                    followRequests(id)
                    // Notify the adapter that the data has changed
                }
                // This block is executed when the mutation is successful
                // Handle any other logic you need here for a successful mutation
                Log.i("Amplify", "User updated: ${response.data}")
            },
            { error ->
                // This block is executed when there's an error during the mutation
                Log.e("MyAmplifyApp", "delete failed", error)
                // Handle the error appropriately
            })
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
                        binding.mainRecyclerview3.adapter?.notifyDataSetChanged()
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
}