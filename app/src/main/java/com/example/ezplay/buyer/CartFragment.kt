package com.example.ezplay.buyer

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.database.Adapter.CartAdapter
import com.example.ezplay.database.Entity.Cart
import com.example.ezplay.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.math.BigDecimal
import java.math.RoundingMode

class CartFragment : Fragment() {

    lateinit var cartRecycler: ListView
    lateinit var qtyAdult: TextView
    lateinit var totalAdult: TextView
    lateinit var qtyChild: TextView
    lateinit var totalChild: TextView
    lateinit var totalAmount: TextView
    lateinit var ref: DatabaseReference
    val mAuth = FirebaseAuth.getInstance()
    lateinit var cartList: MutableList<Cart>
    private var count = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentCartBinding>(inflater,
            R.layout.fragment_cart,container,false)
        binding.customNavbar.back.setOnClickListener {
            view?.findNavController()?.popBackStack() }
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE

        ref = FirebaseDatabase.getInstance().getReference("users").child(mAuth.currentUser!!.uid).child("cart")
        cartRecycler = binding.cartListView
        qtyAdult = binding.adultTicketQuantityTextView
        totalAdult = binding.adultTicketPriceTextView
        qtyChild = binding.childTicketQuantityTextView
        totalChild = binding.childTicketPriceTextView
        totalAmount = binding.totalAmountPriceTextView

        // set the meal cart layout height
        val params: ViewGroup.LayoutParams = binding.cartListView.getLayoutParams()
        val sharedPreferences: SharedPreferences = activity!!.getSharedPreferences(mAuth.currentUser!!.uid, Context.MODE_PRIVATE)
        val orderlist = sharedPreferences.getString("order_list", "0")
        val delimeter = ","
        count = 0
        if (!orderlist.equals("0")) {
            val removeLast = orderlist!!.substring(0, orderlist.length - 1)
            val list = removeLast!!.split(delimeter)

            // save the meal order into cart
            for (i in list) {
                if (i != "") {
                    count++
                }
            }
        }
        params.height = 155 * count
        binding.cartListView.setLayoutParams(params)
        if (count == 0)
            binding.emptyOrderCardView.visibility = View.VISIBLE
        else
            binding.emptyOrderCardView.visibility = View.GONE
        cartList = mutableListOf()

        displayPrice()

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    cartList.clear()
                    for (j in p0.children) {
                        val cart = j.getValue(Cart::class.java)
                        cartList.add(cart!!)
                    }
                    // display the meal order list
                    val adapter = context?.let { CartAdapter(it, R.layout.order_list, cartList) }
                    cartRecycler.adapter = adapter
                }
            }
        })

        binding.minusAdultTicketQuantityTextView.setOnClickListener {
            if (qtyAdult.text.toString().toInt() == 1) {
                Toast.makeText(activity, "Adult Ticket must minimum 1", Toast.LENGTH_SHORT).show()
            } else {
                val editor: SharedPreferences.Editor =  sharedPreferences.edit()
                editor.putInt("adultTicketQuantity", qtyAdult.text.toString().toInt() - 1)
                editor.commit()
                displayPrice()
            }
        }

        binding.plusAdultTicketQuantityTextView.setOnClickListener {
            val editor: SharedPreferences.Editor =  sharedPreferences.edit()
            editor.putInt("adultTicketQuantity", qtyAdult.text.toString().toInt() + 1)
            editor.commit()
            displayPrice()
        }

        binding.minusChildTicketQuantityTextView.setOnClickListener {
            if (qtyChild.text.toString().toInt() > 0) {
                val editor: SharedPreferences.Editor =  sharedPreferences.edit()
                editor.putInt("childTicketQuantity", qtyChild.text.toString().toInt() - 1)
                editor.commit()
                displayPrice()
            }
        }

        binding.plusChildTicketQuantityTextView.setOnClickListener {
            val editor: SharedPreferences.Editor =  sharedPreferences.edit()
            editor.putInt("childTicketQuantity", qtyChild.text.toString().toInt() + 1)
            editor.commit()
            displayPrice()
        }

        binding.totalAmountCardView.setOnClickListener {
            if (binding.onlineBankingButton.isChecked) {
                view!!.findNavController().navigate(CartFragmentDirections
                    .actionCartFragmentToPaymentFragment(
                        "Online Banking",
                        totalAmount.text.toString()
                    ))
            } else {
                view!!.findNavController().navigate(CartFragmentDirections
                    .actionCartFragmentToPaymentFragment(
                        "Visa Card",
                        totalAmount.text.toString()
                    ))
            }
        }

        return binding.root
    }

    private fun displayPrice() {
        val sharedPreferences: SharedPreferences = activity!!.getSharedPreferences(mAuth.currentUser!!.uid, Context.MODE_PRIVATE)
        val adultPrice = BigDecimal(sharedPreferences.getString(
            "adultTicketPrice", "0")!!.toDouble().times(
            sharedPreferences.getInt("adultTicketQuantity", 1)))
            .setScale(2, RoundingMode.HALF_EVEN)
        val childPrice = BigDecimal(sharedPreferences.getString(
            "childTicketPrice", "0")!!.toDouble().times(
            sharedPreferences.getInt("childTicketQuantity", 1)))
            .setScale(2, RoundingMode.HALF_EVEN)
        var totalPrice: BigDecimal = BigDecimal(0)

        qtyAdult.text = sharedPreferences.getInt("adultTicketQuantity", 1).toString()
        totalAdult.text = "RM " + adultPrice.toString()
        qtyChild.text = sharedPreferences.getInt("childTicketQuantity", 0).toString()
        totalChild.text = "RM " + childPrice.toString()

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    totalPrice = adultPrice + childPrice
                    for (j in p0.children) {
                        val cart = j.getValue(Cart::class.java)
                        totalPrice += BigDecimal(cart!!.mealPrice.times(cart.mealQuantity))
                    }
                    totalAmount.text = "RM " + totalPrice.setScale(2, RoundingMode.HALF_EVEN)
                } else {
                    totalPrice = adultPrice + childPrice
                    totalAmount.text = "RM " + totalPrice.setScale(2, RoundingMode.HALF_EVEN)
                }
            }
        })
    }

}
