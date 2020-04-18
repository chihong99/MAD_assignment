package com.example.ezplay.database.Adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.ezplay.R
import com.example.ezplay.database.Entity.Cart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.math.BigDecimal
import java.math.RoundingMode


class CartAdapter(val ctx: Context, val layoutResID: Int, val cartList: List<Cart>)
    : ArrayAdapter<Cart>(ctx, layoutResID, cartList){

    lateinit var ref: DatabaseReference
    val mAuth = FirebaseAuth.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
        val view: View = layoutInflater.inflate(layoutResID, null)

        val cartMealName = view.findViewById<TextView>(R.id.cartMealNameTextView)
        val cartMealPrice = view.findViewById<TextView>(R.id.cartMealPriceTextView)
        val cartMealQuantity = view.findViewById<TextView>(R.id.quantityTextView)
        val minusBtn = view.findViewById<TextView>(R.id.minusQuantityTextView)
        val plusBtn = view.findViewById<TextView>(R.id.plusQuantityTextView)
        val order = view.findViewById<LinearLayout>(R.id.cartMealListContent)

        ref = FirebaseDatabase.getInstance().getReference("users").child(mAuth.currentUser!!.uid).child("cart")

        val cart = cartList[position]
        cartMealName.text = cart.mealName
        cartMealPrice.text = "RM " + BigDecimal(cart.mealPrice.times(cart.mealQuantity))
            .setScale(2, RoundingMode.HALF_EVEN).toString()
        cartMealQuantity.text = cart.mealQuantity.toString()

        minusBtn.setOnClickListener {
            if (cart.mealQuantity == 1) {
                ref.child(cart.mealID.toString()).removeValue()
                order.visibility = View.GONE
                Toast.makeText(ctx, "This meal is removed from your order", Toast.LENGTH_SHORT).show()

                val sharedPreferences: SharedPreferences = ctx.getSharedPreferences(mAuth.currentUser!!.uid, Context.MODE_PRIVATE)
                val editor: SharedPreferences.Editor =  sharedPreferences.edit()
                editor.remove("meal${cart.mealID}")
                editor.remove("meal_name${cart.mealID}")
                editor.remove("meal_price${cart.mealID}")
                editor.remove("meal_quantity${cart.mealID}")
                editor.putInt("meal_count", sharedPreferences.getInt("meal_count", 0) - 1)
                val orderlist = sharedPreferences.getString("order_list", "0")
                val firstReplace = orderlist!!.replace("${cart.mealID}","")
                val newOrderList = firstReplace!!.replace(",,",",")
                editor.putString("order_list", newOrderList)
                editor.commit()
            } else {
                val minusQty = cart.mealQuantity - 1
                val updateCart = Cart(cart.mealID, cart.mealName, minusQty, cart.mealPrice, mAuth.currentUser!!.uid)
                ref.child(cart.mealID.toString()).setValue(updateCart)
                cartMealPrice.text = "RM " + BigDecimal(cart.mealPrice.times(minusQty))
                    .setScale(2, RoundingMode.HALF_EVEN).toString()
                cartMealQuantity.text = minusQty.toString()

                val sharedPreferences: SharedPreferences = ctx.getSharedPreferences(mAuth.currentUser!!.uid, Context.MODE_PRIVATE)
                val editor: SharedPreferences.Editor =  sharedPreferences.edit()
                editor.putInt("meal_count", sharedPreferences.getInt("meal_count", 0) - 1)
                editor.putInt("meal_quantity${cart.mealID}", sharedPreferences.getInt("meal_quantity${cart.mealID}", 0) - 1)
                editor.commit()
            }
        }

        plusBtn.setOnClickListener {
            val plusQty = cart.mealQuantity + 1
            val updateCart = Cart(cart.mealID, cart.mealName, plusQty, cart.mealPrice, mAuth.currentUser!!.uid)
            ref.child(cart.mealID.toString()).setValue(updateCart)
            cartMealPrice.text = "RM " + BigDecimal(cart.mealPrice.times(plusQty))
                .setScale(2, RoundingMode.HALF_EVEN).toString()
            cartMealQuantity.text = plusQty.toString()

            val sharedPreferences: SharedPreferences = ctx.getSharedPreferences(mAuth.currentUser!!.uid, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor =  sharedPreferences.edit()
            editor.putInt("meal_count", sharedPreferences.getInt("meal_count", 0) + 1)
            editor.putInt("meal_quantity${cart.mealID}", sharedPreferences.getInt("meal_quantity${cart.mealID}", 0) + 1)
            editor.commit()
        }

        return view
    }

}