package com.example.ezplay.buyer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ezplay.R
import com.example.ezplay.ThemeParkViewHolder
import com.example.ezplay.database.Entity.ThemePark
import com.example.ezplay.databinding.FragmentSearchBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.search_themepark.view.*
import kotlinx.android.synthetic.main.user_navbar.view.*
import kotlinx.coroutines.flow.combine

class SearchFragment : Fragment() {

    lateinit var ref: DatabaseReference
    lateinit var searchBox: EditText
    lateinit var searchRecyclerView: RecyclerView
    lateinit var FirebaseRecyclerAdapter : FirebaseRecyclerAdapter<ThemePark, ThemeParkViewHolder>
    private var keywords: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentSearchBinding>(inflater,
            R.layout.fragment_search,container,false)
        binding.customNavbar.back.visibility = View.INVISIBLE
        binding.customNavbar.sellerNavbar.visibility = View.GONE
        binding.customNavbar.favouriteBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_searchFragment_to_favouriteFragment)}
        binding.customNavbar.settingsBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_searchFragment_to_settingsFragment)}

        // initiate the firebase database
        ref = FirebaseDatabase.getInstance().getReference("theme park")
        searchBox = binding.searchEditText
        keywords = searchBox.text.toString()
        searchRecyclerView = binding.searchResultRecyclerView
        searchRecyclerView.setHasFixedSize(true)
        searchRecyclerView.layoutManager = LinearLayoutManager(context)

        if (savedInstanceState != null) {
            searchBox.setText(savedInstanceState.getString("keyword"))
        }

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = searchBox.text.toString()
                displayThemePark(keyword)
            }
        })

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("keyword", keywords)
    }

    private fun displayThemePark(keyword: String) {
        var query: Query
        if (keyword == "")
            query = ref.orderByChild("themeParkName").equalTo(keyword)
        else
            query = ref.orderByChild("themeParkName").startAt(keyword).endAt(keyword + "\uf8ff")

        FirebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ThemePark, ThemeParkViewHolder>(
            ThemePark::class.java,
            R.layout.search_themepark,
            ThemeParkViewHolder::class.java,
            query
        ) {
            override fun populateViewHolder(viewHolder: ThemeParkViewHolder?, model: ThemePark?, position: Int) {
                Picasso.with(context).load(model?.themeParkImage).fit()
                    .centerCrop().into(viewHolder!!.themeparkView.themeparkImageView)
                viewHolder.themeparkView.themeparkNameTextView.text = model?.themeParkName
                viewHolder.themeparkView.setOnClickListener { view: View ->
                    view.findNavController().navigate(SearchFragmentDirections
                        .actionSearchFragmentToThemeParkInfoFragment(model?.themeParkName.toString()))
                }
            }

        }
        searchRecyclerView.adapter = FirebaseRecyclerAdapter
    }

}
