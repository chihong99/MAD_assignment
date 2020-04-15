package com.example.ezplay.buyer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.database.Entity.ThemePark
import com.example.ezplay.databinding.FragmentThemeparkInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.math.BigDecimal
import java.math.RoundingMode

class ThemeParkInfoFragment : Fragment() {

    lateinit var ref: DatabaseReference
    lateinit var favRef: DatabaseReference
    val mAuth = FirebaseAuth.getInstance()
    lateinit var themeparkImg: ImageView
    lateinit var addToFavouriteBtn: ImageButton
    lateinit var bookNowBtn: ImageButton
    lateinit var themeparkName: TextView
    lateinit var themeparkBusinessHour: TextView
    lateinit var themeparkAdultPrice: TextView
    lateinit var themeparkChildPrice: TextView
    lateinit var uid: String
    lateinit var selectedThemeParkID: String
    lateinit var selectedThemeParkName: String
    lateinit var selectedThemeParkAdultPrice: String
    lateinit var selectedThemeParkChildPrice: String
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
        themeparkImg = binding.themeparkImageView
        addToFavouriteBtn = binding.addToFavouriteButton
        bookNowBtn = binding.bookNowButton
        themeparkName = binding.themeparkNameTextView
        themeparkBusinessHour = binding.themeparkBusinessHourTextView
        themeparkAdultPrice = binding.themeparkAdultPriceTextView
        themeparkChildPrice = binding.themeparkChildPriceTextView
        uid = mAuth.currentUser!!.uid

        // display the selected theme park that pass from the search fragment
        favRef = FirebaseDatabase.getInstance().getReference("favourite")
        ref = FirebaseDatabase.getInstance().getReference("theme park")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (i in p0.children) {
                        val themepark = i.getValue(ThemePark::class.java)
                        if (args.selectedThemePark == themepark!!.themeParkName ||
                            args.selectedThemePark == themepark!!.themeParkID.toString()) {
                            Picasso.with(context!!).load(themepark!!.themeParkImage)
                                .into(themeparkImg)
                            selectedThemeParkID = themepark.themeParkID.toString()
                            selectedThemeParkName = themepark.themeParkName
                            selectedThemeParkAdultPrice = BigDecimal(themepark.adultPrice).setScale(
                                        2, RoundingMode.HALF_EVEN).toString()
                            selectedThemeParkChildPrice = BigDecimal(themepark.childPrice).setScale(
                                2, RoundingMode.HALF_EVEN).toString()
                            themeparkName.text = "Name: " + themepark.themeParkName
                            themeparkBusinessHour.text =
                                "Business Hours: " + themepark.themeParkBusinessHours
                            themeparkAdultPrice.text = "Adult Price: RM " +
                                    BigDecimal(themepark.adultPrice).setScale(
                                        2,
                                        RoundingMode.HALF_EVEN
                                    ).toString()
                            themeparkChildPrice.text = "Child Price: RM " +
                                    BigDecimal(themepark.childPrice).setScale(
                                        2,
                                        RoundingMode.HALF_EVEN
                                    ).toString()
                            isFavouriteThemePark(selectedThemeParkID)
                        }
                    }
                }
            }
        })

        addToFavouriteBtn.setOnClickListener {
            preventLoop = 0
            favRef.child(uid).child(selectedThemeParkID)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists() && preventLoop == 0) {
                            // remove from favourite
                            preventLoop = 1
                            favRef.child(uid).child(selectedThemeParkID).removeValue()
                            addToFavouriteBtn.setImageDrawable(resources.getDrawable(R.drawable.add_to_favourite_icon))
                            Toast.makeText(activity, "Theme Park is removed from your favourite", Toast.LENGTH_SHORT).show()
                            isFavouriteThemePark(selectedThemeParkID)
                        } else if (!p0.exists() && preventLoop == 0) {
                            // add into favourite
                            preventLoop = 1
                            favRef.child(uid).child(selectedThemeParkID).setValue(selectedThemeParkID.toInt())
                            addToFavouriteBtn.setImageDrawable(resources.getDrawable(R.drawable.added_to_favourite_icon))
                            Toast.makeText(activity, "Theme Park is added to your favourite", Toast.LENGTH_SHORT).show()
                            isFavouriteThemePark(selectedThemeParkID)
                        }
                    }
                })
        }

        bookNowBtn.setOnClickListener {view: View ->
            view.findNavController().navigate(
                ThemeParkInfoFragmentDirections.actionThemeParkInfoFragmentToBookingFragment(
                    selectedThemeParkID,
                    selectedThemeParkName,
                    selectedThemeParkAdultPrice,
                    selectedThemeParkChildPrice
                )
            )
        }

        return binding.root
    }

    private fun isFavouriteThemePark(parkID: String) {
        if (parkID != "") {
            // determine whether this user has added this theme park into favourite
            favRef.child(uid).child(parkID)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            addToFavouriteBtn.setImageDrawable(resources.getDrawable(R.drawable.added_to_favourite_icon))
                        } else {
                            addToFavouriteBtn.setImageDrawable(resources.getDrawable(R.drawable.add_to_favourite_icon))
                        }
                    }
                })
        }
    }

}
