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
import com.example.ezplay.PaymentViewHolder
import com.example.ezplay.R
import com.example.ezplay.database.Entity.Booking
import com.example.ezplay.database.Entity.Payment
import com.example.ezplay.database.Entity.ThemePark
import com.example.ezplay.databinding.FragmentTransactionHistoryBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.transaction_history_recyclerview.view.*
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.math.BigDecimal
import java.math.RoundingMode

class TransactionHistoryFragment : Fragment() {

    lateinit var paymentHistory: RecyclerView
    lateinit var bookingHistory: ArrayList<Int>
    lateinit var bookingRef: DatabaseReference
    lateinit var themeparkRef: DatabaseReference
    lateinit var FirebaseRecyclerAdapter : FirebaseRecyclerAdapter<Payment, PaymentViewHolder>
    val mAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentTransactionHistoryBinding>(inflater,
            R.layout.fragment_transaction_history,container,false)
        binding.customNavbar.back.setOnClickListener {
            view?.findNavController()?.popBackStack() }
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE

        bookingHistory = ArrayList()
        paymentHistory = binding.paymentRecyclerView
        themeparkRef = FirebaseDatabase.getInstance().getReference("theme park")
        bookingRef = FirebaseDatabase.getInstance().getReference("booking")
        bookingRef.orderByChild("userID").equalTo(mAuth.currentUser!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (i in p0.children) {
                        bookingHistory.add(i.getValue(Booking::class.java)!!.bookingID)
                    }
                    displayPaymentHistory()
                }
            }
        })

        return binding.root
    }

    private fun displayPaymentHistory() {
        paymentHistory.setHasFixedSize(true)
        paymentHistory.layoutManager = LinearLayoutManager(context)
        val paymentRef = FirebaseDatabase.getInstance().getReference("payment")
        FirebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Payment, PaymentViewHolder>(
            Payment::class.java,
            R.layout.transaction_history_recyclerview,
            PaymentViewHolder::class.java,
            paymentRef
        ) {
            override fun populateViewHolder(p0: PaymentViewHolder?, p1: Payment?, p2: Int) {
                if (bookingHistory.isNotEmpty()) {
                    if (bookingHistory.remove(p1!!.bookingID)) {
                        bookingRef.child(p1.bookingID.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p5: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                            override fun onDataChange(p5: DataSnapshot) {
                                themeparkRef.child(p5.getValue(Booking::class.java)!!.themeParkID.toString())
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onCancelled(p6: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                        override fun onDataChange(p6: DataSnapshot) {
                                            Picasso.with(context)
                                                .load(p6.getValue(ThemePark::class.java)!!.themeParkImage)
                                                .fit()
                                                .centerCrop()
                                                .into(p0!!.paymentView.themeparkImageView)
                                            p0.paymentView.bookedThemeParkNameTextView.text =
                                                p6.getValue(ThemePark::class.java)!!.themeParkName
                                        }
                                    })
                            }
                        })

                        p0!!.paymentView.paymentDateTextView.text = p1.paymentDate
                        p0.paymentView.totalAmountTextView.text = "RM " +
                                BigDecimal(p1.paymentAmount).setScale(2,
                                    RoundingMode.HALF_EVEN).toString()
                        p0.paymentView.setOnClickListener {
                            view!!.findNavController().navigate(
                                TransactionHistoryFragmentDirections
                                    .actionTransactionHistoryFragmentToTransactionDetailFragment(
                                        p1.bookingID,
                                        p0.paymentView.totalAmountTextView.text.toString()
                                    )
                            )
                        }
                    } else {
                        p0!!.paymentView.visibility = View.GONE
                        p0.paymentView.layoutParams = RecyclerView.LayoutParams(0, 0)
                    }
                }
            }
        }
        paymentHistory.adapter = FirebaseRecyclerAdapter
    }

}
