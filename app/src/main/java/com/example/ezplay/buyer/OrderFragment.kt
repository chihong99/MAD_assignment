package com.example.ezplay.buyer

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ezplay.MealViewHolder
import com.example.ezplay.R
import com.example.ezplay.database.Entity.Cart
import com.example.ezplay.database.Entity.Meal
import com.example.ezplay.databinding.FragmentOrderBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.staff_meal_list.view.*
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.math.BigDecimal
import java.math.RoundingMode

class OrderFragment : Fragment() {

    lateinit var orderMealRecycler: RecyclerView
    lateinit var mealCount: TextView
    lateinit var mealPrice: TextView
    lateinit var ref: DatabaseReference
    lateinit var FirebaseRecyclerAdapter : FirebaseRecyclerAdapter<Meal, MealViewHolder>
    lateinit var args: OrderFragmentArgs
    val mAuth = FirebaseAuth.getInstance()
    lateinit var sharedPreferences: SharedPreferences
    private var totalPrice: Double = 0.0
    private var meal_count = 0
    private var count = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentOrderBinding>(inflater,
            R.layout.fragment_order,container,false)
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE
        binding.customNavbar.back.setOnClickListener { view?.findNavController()?.popBackStack() }

        sharedPreferences = activity!!.getSharedPreferences(mAuth.currentUser!!.uid, Context.MODE_PRIVATE)
        args = OrderFragmentArgs.fromBundle(arguments!!)
        ref = FirebaseDatabase.getInstance().getReference("meal")
        orderMealRecycler = binding.orderMealRecyclerView
        mealCount = binding.mealCountTextView
        mealPrice = binding.totalMealPriceTextView

        if (savedInstanceState != null) {
            if (sharedPreferences.getInt("meal_count", 0) == 0) {
                meal_count = savedInstanceState.getString("meal_count")!!.toInt()
                mealCount.text = meal_count.toString()
            } else {
                meal_count = sharedPreferences.getInt("meal_count", 0)
                mealCount.text = meal_count.toString()
            }
        } else {
            meal_count = sharedPreferences.getInt("meal_count", 0)
            mealCount.text = meal_count.toString()
        }

        orderMealRecycler.setHasFixedSize(true)
        orderMealRecycler.layoutManager = LinearLayoutManager(context)
        totalPrice = 0.0
        displayOrderMealList()

        binding.viewCartBtn.setOnClickListener {
            // save the meal into cart
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
                        val mealNames = sharedPreferences.getString("meal_name$i", "0")
                        val mealQuantitys = sharedPreferences.getInt("meal_quantity$i", 0)
                        val mealPrices = sharedPreferences.getString("meal_price$i", "0")
                        if (mealNames != "0" && mealQuantitys != 0 && mealPrices != "0") {
                            val cart = Cart(
                                i.toInt(),
                                mealNames!!,
                                mealQuantitys,
                                mealPrices!!.toDouble(),
                                mAuth.currentUser!!.uid
                            )
                            FirebaseDatabase.getInstance().getReference("users")
                                .child(mAuth.currentUser!!.uid).child("cart").child(i).setValue(cart)
                            count++
                        }
                    }
                }
            }
            view!!.findNavController().navigate(OrderFragmentDirections.actionOrderFragmentToPaymentFragment(count))
        }

        return binding.root
    }

    private fun displayOrderMealList() {
        val query = ref.orderByChild("staffID").equalTo(args.selectedThemeParkSellerID)
        FirebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Meal, MealViewHolder>(
            Meal::class.java,
            R.layout.staff_meal_list,
            MealViewHolder::class.java,
            query
        ) {
            override fun populateViewHolder(viewHolder: MealViewHolder?, model: Meal?, position: Int) {
                Picasso.with(context).load(model?.mealImage).fit().centerCrop()
                    .into(viewHolder!!.mealView.mealImageView)
                viewHolder.mealView.mealNameTextView.setText(model?.mealName)
                viewHolder.mealView.mealPriceTextView.setText("RM" +
                        BigDecimal(model!!.mealPrice).setScale(2, RoundingMode.HALF_EVEN).toString())
                viewHolder.mealView.setOnClickListener {
                    val count = mealCount.text.toString().toInt() + 1
                    mealCount.text = count.toString()
                    Toast.makeText(activity, "Meal is added to your order list", Toast.LENGTH_SHORT).show()

                    val currentMealID = "meal" + model.mealID
                    val currentMealName = "meal_name" + model.mealID
                    val currentMealQuantity = "meal_quantity" + model.mealID
                    val currentMealPrice = "meal_price" + model.mealID
                    val matchMealID = sharedPreferences.getInt(currentMealID, 0)
                    val editor: SharedPreferences.Editor =  sharedPreferences.edit()
                    val currentOrderList = sharedPreferences.getString("order_list", ",")
                    if (matchMealID == 0) {
                        editor.putInt(currentMealID, model.mealID)
                        editor.putInt(currentMealQuantity, 1)
                        editor.putString(currentMealName, model.mealName)
                        editor.putString(currentMealPrice, BigDecimal(model!!.mealPrice)
                            .setScale(2, RoundingMode.HALF_EVEN).toString())
                        if (currentOrderList == ",")
                            editor.putString("order_list", model.mealID.toString() + ",")
                        else
                            editor.putString("order_list", currentOrderList + model.mealID + ",")
                    } else {
                        val mealQuantity = sharedPreferences.getInt(currentMealQuantity, 0) + 1
                        editor.putInt(currentMealQuantity, mealQuantity)
                    }
                    editor.putInt("meal_count", count)
                    editor.commit()

                    // display the current total meal price
                    totalPrice += 1.times(model.mealPrice)
                    mealPrice.text = "RM " + BigDecimal(totalPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
                }
                // calculate total meal price
                val currentMealIDs = "meal" + model.mealID
                val currentMealQuantitys = "meal_quantity" + model.mealID
                val matchMealIDs = sharedPreferences.getInt(currentMealIDs, 0)
                if (matchMealIDs != 0) {
                    totalPrice += sharedPreferences.getInt(currentMealQuantitys, 0).times(model.mealPrice)
                    mealPrice.text = "RM " + BigDecimal(totalPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
                }
            }
        }
        orderMealRecycler.adapter = FirebaseRecyclerAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("meal_count", meal_count.toString())
    }

}
