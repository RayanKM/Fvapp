package com.example.fvapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Usr
import com.example.fvapp.databinding.FragmentSearchProfileBinding


class SearchProfile : Fragment(R.layout.fragment_search_profile) {
    private lateinit var communicator: Communicator
    private var users = mutableListOf<Usr>()

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
        val n = MainActivity.userN
        Log.e("MyAmplifyApp", "SUIIIIII ${n}")
        communicator = activity as Communicator
        val searchView = view.findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    fetch(newText)
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
                        communicator.passid(id)
                    }
                })
            }
        }

    }
    fun fetch(id: String) {
        if (users != null){
            users.clear()
        }
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
}