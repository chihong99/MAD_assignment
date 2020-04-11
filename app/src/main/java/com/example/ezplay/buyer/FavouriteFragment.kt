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
import com.example.ezplay.database.Entity.Favourite
import com.example.ezplay.database.Entity.ThemePark
import com.example.ezplay.databinding.FragmentFavouriteBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.favourite_themepark_list.view.*
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.text.FieldPosition

class FavouriteFragment : Fragment() {

    lateinit var favouriteThemeParkList: RecyclerView
    lateinit var favouriteList: ArrayList<String>
    lateinit var themeparkList: ArrayList<ThemePark>
    lateinit var ref: DatabaseReference
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
        ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(mAuth.currentUser!!.uid).child("favourite")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        for (i in p0.children) {
                            //favouriteList.add( i.getValue(Favourite::class.java)!!.themeParkID.toString())
                        }
                        readThemeParkRecords(favouriteList)
                    }
                }
            })

        return binding.root
    }

    private fun readThemeParkRecords(recordsList: ArrayList<String>) {
        for (i in recordsList) {
            ref.child(mAuth.currentUser!!.uid).child("favourite")
                .child(i).addValueEventListener(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            for (j in p0.children) {
                                themeparkList.add(j.getValue(ThemePark::class.java)!!)
                            }
                        }
                    }
                })
        }
        favouriteThemeParkList.setHasFixedSize(true)
        favouriteThemeParkList.layoutManager = LinearLayoutManager(context)
        displayFavouriteThemeParkList()
    }

    private fun displayFavouriteThemeParkList() {
        val FirebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ThemePark, ThemeParkViewHolder>(
            ThemePark::class.java,
            R.layout.favourite_themepark_list,
            ThemeParkViewHolder::class.java,
            ref
        ) {
            override fun populateViewHolder(viewHolder: ThemeParkViewHolder, model: ThemePark?, position: Int) {
                Picasso.with(context).load(model?.themeParkImage).into(viewHolder.themeparkView.themeparkImageView)
                viewHolder.themeparkView.themeparkNameTextView.setText(model?.themeParkName)
            }
        }
        favouriteThemeParkList.adapter = FirebaseRecyclerAdapter
    }

    class ThemeParkViewHolder(var themeparkView: View): RecyclerView.ViewHolder(themeparkView) {}

}
