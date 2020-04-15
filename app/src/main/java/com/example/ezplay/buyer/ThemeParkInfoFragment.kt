package com.example.ezplay.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ezplay.R
import com.example.ezplay.ThemeParkViewHolder
import com.example.ezplay.database.Entity.ThemePark
import com.example.ezplay.databinding.FragmentThemeparkInfoBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_themepark_info.view.*
import kotlinx.android.synthetic.main.themepark_recyclerview.view.*
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.math.BigDecimal
import java.math.RoundingMode

class ThemeParkInfoFragment : Fragment() {

    lateinit var ref: DatabaseReference
    lateinit var favRef: DatabaseReference
    val mAuth = FirebaseAuth.getInstance()
    lateinit var themeparkView: RecyclerView
    lateinit var FirebaseRecyclerAdapter : FirebaseRecyclerAdapter<ThemePark, ThemeParkViewHolder>
    lateinit var uid: String
    var preventLoop = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentThemeparkInfoBinding>(inflater,
            R.layout.fragment_themepark_info,container,false)
        binding.customNavbar.back.setOnClickListener {
            view?.findNavController()?.popBackStack() }
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE

        val args = ThemeParkInfoFragmentArgs.fromBundle(arguments!!)
        themeparkView = binding.themeparkRecyclerView
        //bookNowBtn = binding.bookNowButton
        uid = mAuth.currentUser!!.uid

        // display the selected theme park that pass from the search fragment
        favRef = FirebaseDatabase.getInstance().getReference("favourite")
        ref = FirebaseDatabase.getInstance().getReference("theme park")
        themeparkView.setHasFixedSize(true)
        themeparkView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        displayThemePark(args)

        return binding.root
    }

    private fun displayThemePark(args: ThemeParkInfoFragmentArgs) {
        val query = ref.orderByChild("themeParkName").equalTo(args.selectedThemePark)
        FirebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ThemePark, ThemeParkViewHolder>(
            ThemePark::class.java,
            R.layout.themepark_recyclerview,
            ThemeParkViewHolder::class.java,
            query
        ) {
            override fun populateViewHolder(p0: ThemeParkViewHolder?, p1: ThemePark?, position: Int) {
                Picasso.with(context).load(p1!!.themeParkImage).into(p0!!.themeparkView.themeparkImageView)
                p0.themeparkView.themeparkNameTextView.text = "Name: " + p1.themeParkName
                p0.themeparkView.themeparkBusinessHourTextView.text = "Business Hours: " + p1.themeParkBusinessHours
                p0.themeparkView.themeparkAdultPriceTextView.text = "Adult Price: RM " +
                        BigDecimal(p1.adultPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
                p0.themeparkView.themeparkChildPriceTextView.text = "Child Price: RM " +
                        BigDecimal(p1.childPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
                //isFavouriteThemePark(selectedThemeParkID)
                if (p1.themeParkID.toString() != "") {
                    // determine whether this user has added this theme park into favourite
                    favRef.child(uid).child(p1.themeParkID.toString()).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(t0: DataSnapshot) {
                            if (t0.exists()) {
                                p0.themeparkView.addToFavouriteButton.setImageDrawable(resources.getDrawable(R.drawable.added_to_favourite_icon))
                            } else {
                                p0.themeparkView.addToFavouriteButton.setImageDrawable(resources.getDrawable(R.drawable.add_to_favourite_icon))
                            }
                        }
                    })
                }

                // click on add favourite button
                p0.themeparkView.addToFavouriteButton.setOnClickListener {
                    preventLoop = 0
                    favRef.child(uid).child(p1.themeParkID.toString()).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(t0: DataSnapshot) {
                            if (t0.exists() && preventLoop == 0) {
                                // remove from favourite
                                preventLoop = 1
                                favRef.child(uid).child(p1.themeParkID.toString()).removeValue()
                                p0.themeparkView.addToFavouriteButton.setImageDrawable(resources.getDrawable(R.drawable.add_to_favourite_icon))
                                Toast.makeText(activity, "Theme Park is removed from your favourite", Toast.LENGTH_SHORT).show()
                            } else if (!t0.exists() && preventLoop == 0) {
                                // add into favourite
                                preventLoop = 1
                                favRef.child(uid).child(p1.themeParkID.toString()).setValue(p1.themeParkID.toString().toInt())
                                p0.themeparkView.addToFavouriteButton.setImageDrawable(resources.getDrawable(R.drawable.added_to_favourite_icon))
                                Toast.makeText(activity, "Theme Park is added to your favourite", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }

                // click on the book now button
                p0.themeparkView.bookNowButton.setOnClickListener { view: View ->
                    view.findNavController().navigate(
                        ThemeParkInfoFragmentDirections.actionThemeParkInfoFragmentToBookingFragment(
                            p1.themeParkID.toString(),
                            p1.themeParkName,
                            BigDecimal(p1.adultPrice).setScale(2, RoundingMode.HALF_EVEN).toString(),
                            BigDecimal(p1.childPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
                        )
                    )
                }
            }
        }
        themeparkView.adapter = FirebaseRecyclerAdapter
    }

}
