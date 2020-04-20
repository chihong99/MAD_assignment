package com.example.ezplay.buyer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ezplay.MealListViewHolder
import com.example.ezplay.R
import com.example.ezplay.database.Adapter.MealOrderAdapter
import com.example.ezplay.database.Entity.Booking
import com.example.ezplay.database.Entity.Meal
import com.example.ezplay.database.Entity.MealList
import com.example.ezplay.database.Entity.ThemePark
import com.example.ezplay.databinding.FragmentTransactionDetailBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.meal_order_history.view.*
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.math.BigDecimal
import java.math.RoundingMode

class TransactionDetailFragment : Fragment() {

    lateinit var bookingRef: DatabaseReference
    lateinit var themeparkRef: DatabaseReference
    lateinit var mealRef: DatabaseReference
    lateinit var mealOrderListView: ListView
    lateinit var params: ViewGroup.LayoutParams

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentTransactionDetailBinding>(inflater,
            R.layout.fragment_transaction_detail,container,false)
        binding.customNavbar.back.setOnClickListener {
            view?.findNavController()?.popBackStack() }
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE

        mealOrderListView = binding.mealOrderListView
        params = binding.mealOrderListView.getLayoutParams()
        val args = TransactionDetailFragmentArgs.fromBundle(arguments!!)
        mealRef = FirebaseDatabase.getInstance().getReference("meal")
        themeparkRef = FirebaseDatabase.getInstance().getReference("theme park")
        bookingRef = FirebaseDatabase.getInstance().getReference("booking").child(args.bookingID.toString())
        bookingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val booking = p0.getValue(Booking::class.java)!!
                binding.bookingdateTextView.text = booking.bookingDate
                binding.adultTicketText.text = "Adult Ticket x${booking.adultTicketQuantity}"
                binding.childTicketText.text = "Child Ticket x${booking.childTicketQuantity}"

                themeparkRef.child(booking.themeParkID.toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p1: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(p1: DataSnapshot) {
                            binding.textView16.text = p1.getValue(ThemePark::class.java)!!.themeParkName
                            Picasso.with(context).load(p1.getValue(ThemePark::class.java)!!.themeParkImage)
                                .fit().centerCrop().into(binding.themeparkImage)
                            binding.adultTicketPriceTextView.text = "RM " + BigDecimal(
                                p1.getValue(ThemePark::class.java)!!.adultPrice.times(booking.adultTicketQuantity))
                                .setScale(2, RoundingMode.HALF_EVEN).toString()
                            binding.childTicketPriceTextView.text = "RM " + BigDecimal(
                                p1.getValue(ThemePark::class.java)!!.childPrice.times(booking.childTicketQuantity))
                                .setScale(2, RoundingMode.HALF_EVEN).toString()
                        }

                    })

                if (booking.mealListID != 0)
                    loadMealOrder(booking.mealListID)
                else
                    binding.emptyOrder.visibility = View.VISIBLE
            }
        })
        binding.totalAmountText.text = args.paymentAmount

        return binding.root
    }

    private fun loadMealOrder(mealListID: Int) {
        var cartList: MutableList<MealList> = mutableListOf()
        val mealListRef = FirebaseDatabase.getInstance().getReference("meal list")
        mealListRef.child(mealListID.toString()).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                cartList.clear()
                for (i in p0.children) {
                    cartList.add(i.getValue(MealList::class.java)!!)
                }
                params.height = 100 * p0.childrenCount.toInt()
                mealOrderListView.setLayoutParams(params)
                val adapter = MealOrderAdapter(context!!, R.layout.meal_order_history, cartList)
                mealOrderListView.adapter = adapter
            }
        })
    }

}
