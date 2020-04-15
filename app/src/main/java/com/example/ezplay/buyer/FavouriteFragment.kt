package com.example.ezplay.buyer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ezplay.R
import com.example.ezplay.ThemeParkViewHolder
import com.example.ezplay.database.Entity.ThemePark
import com.example.ezplay.databinding.FragmentFavouriteBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.favourite_themepark_list.view.*
import kotlinx.android.synthetic.main.user_navbar.view.*
import kotlin.collections.ArrayList

class FavouriteFragment : Fragment() {

    lateinit var favouriteThemeParkList: RecyclerView
    lateinit var favouriteList: ArrayList<Long>
    lateinit var themeparkList: MutableList<ThemePark>
    lateinit var ref: DatabaseReference
    lateinit var refTP: DatabaseReference
    lateinit var FirebaseRecyclerAdapter : FirebaseRecyclerAdapter<ThemePark, ThemeParkViewHolder>
    val mAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentFavouriteBinding>(inflater,
            R.layout.fragment_favourite,container,false)
        binding.customNavbar.back.visibility = View.INVISIBLE
        binding.customNavbar.sellerNavbar.visibility = View.GONE
        binding.customNavbar.searchBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_favouriteFragment_to_searchFragment)}
        binding.customNavbar.settingsBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_favouriteFragment_to_settingsFragment)}

        favouriteThemeParkList = binding.favouriteList
        favouriteList = ArrayList()
        themeparkList = mutableListOf()
        refTP = FirebaseDatabase.getInstance().getReference("theme park")
        ref = FirebaseDatabase.getInstance().getReference("favourite")
        ref.child(mAuth.currentUser!!.uid).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (i in p0.children) {
                        val favList = i.value
                        favouriteList.add(favList as Long)
                    }
                    displayFavouriteThemeParkList(favouriteList)
                }
            }
        })

        return binding.root
    }

    private fun displayFavouriteThemeParkList(recordsList: ArrayList<Long>) {
        favouriteThemeParkList.setHasFixedSize(true)
        favouriteThemeParkList.layoutManager = LinearLayoutManager(context)

        FirebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ThemePark, ThemeParkViewHolder>(
            ThemePark::class.java,
            R.layout.favourite_themepark_list,
            ThemeParkViewHolder::class.java,
            refTP
        ) {
            override fun populateViewHolder(viewHolder: ThemeParkViewHolder, model: ThemePark?, position: Int) {
                if (recordsList.isNotEmpty()) {
                    if (recordsList.remove(model?.themeParkID?.toLong())) {
                        viewHolder.themeparkView.themeparkImageView.visibility = View.VISIBLE
                        viewHolder.themeparkView.themeparkNameTextView.visibility = View.VISIBLE
                        viewHolder.themeparkView.listContent.visibility = View.VISIBLE
                        Picasso.with(context).load(model?.themeParkImage).fit().centerCrop()
                            .into(viewHolder.themeparkView.themeparkImageView)
                        viewHolder.themeparkView.themeparkNameTextView.setText(model?.themeParkName)
                        viewHolder.themeparkView.setOnClickListener {view: View ->
                            view.findNavController().navigate(FavouriteFragmentDirections
                                .actionFavouriteFragmentToThemeParkInfoFragment(model!!.themeParkName))
                        }
                    } else {
                        viewHolder.themeparkView.visibility = View.GONE
                        viewHolder.themeparkView.layoutParams = RecyclerView.LayoutParams(0, 0)
                    }
                }
            }
        }
        favouriteThemeParkList.adapter = FirebaseRecyclerAdapter
    }

}
