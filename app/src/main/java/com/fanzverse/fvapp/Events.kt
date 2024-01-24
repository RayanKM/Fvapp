package com.fanzverse.fvapp

import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Event
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.datastore.generated.model.Usr
import com.bumptech.glide.Glide
import com.fanzverse.fvapp.databinding.EditbioBinding
import com.fanzverse.fvapp.databinding.EntercodeBinding
import com.fanzverse.fvapp.databinding.FragmentEventsBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Events : Fragment(R.layout.fragment_events) {
    private lateinit var binding: FragmentEventsBinding
    private lateinit var communicator: Communicator
    var allPosts = mutableListOf<Event>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val n = MainActivity.userN
        communicator = activity as Communicator
        fetch()
        // Notify the adapter that the data has changed
        binding.mainRecyclerview.apply {
            layoutManager = GridLayoutManager(requireActivity().applicationContext, 3)
            adapter = EventAdapter(requireContext(), allPosts).apply {
                setOnItemClickListener(object : EventAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val post = allPosts[position]
                        if (post.privacy && post.members == null || post.privacy && !post.members.contains(n)) {

                        }
                        else{
                            val bundle = Bundle()
                            bundle.putString("id", post.id) // Replace "post" with the key you want to use
                            // Perform the fragment transaction
                            val transaction = activity!!.supportFragmentManager.beginTransaction()
                            val frg = EventPage()
                            frg.arguments = bundle
                            transaction.replace(R.id.mn, frg)
                            transaction.addToBackStack(null)
                            transaction.commit()

                        }
                    }
                })
            }
        }
        binding.swiper.setOnRefreshListener {
            fetch()
        }

        var searchHandler: Handler? = null
        val searchView = view.findViewById<SearchView>(R.id.srchv)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                searchHandler?.removeCallbacksAndMessages(null)
                searchHandler = Handler()
                if (!newText.isNullOrBlank()) {
                    // Delayed search when there is a non-empty query
                    searchHandler?.postDelayed({
                        searchbyLocation(newText.orEmpty())
                    }, 300) // Adjust the delay as needed
                }else{
                    fetch()
                }
                return true
            }
        })
        binding.searchbyDate.setOnClickListener {
            startDatePickerDialog()
        }
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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
                searchbyDate(formattedDate)
            },
            currentYear,
            currentMonth,
            currentDay
        )

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    fun fetch(){
        allPosts.clear()
        Amplify.API.query(
            ModelQuery.list(Event::class.java, Event.BACKGROUND.contains("https")),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                // Get the post IDs of the first three posts
                sortedPosts.forEach { postId ->
                    allPosts.add(postId)
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

    fun searchbyLocation(location:String){
        allPosts.clear()
        binding.mainRecyclerview.adapter?.notifyDataSetChanged()
        Amplify.API.query(
            ModelQuery.list(Event::class.java, Event.BACKGROUND.contains("https")),
            { postResponse ->
                allPosts.clear()
                activity?.runOnUiThread {
                    // Notify the adapter that the data has changed
                    binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                }
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                // Get the post IDs of the first three posts
                sortedPosts.forEach { postId ->
                    if (postId.location.toLowerCase().contains(location.toLowerCase())){
                        allPosts.add(postId)
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
        binding.swiper.isRefreshing = false
    }
    fun searchbyDate(datePick:String){
        allPosts.clear()
        binding.mainRecyclerview.adapter?.notifyDataSetChanged()
        Amplify.API.query(
            ModelQuery.list(Event::class.java, Event.START_DATE.contains(datePick)),
            { postResponse ->
                val sortedPosts = postResponse.data.sortedByDescending { it.createdAt } // Sort by createdAt in descending order
                // Get the post IDs of the first three posts
                sortedPosts.forEach { postId ->
                    allPosts.add(postId)
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