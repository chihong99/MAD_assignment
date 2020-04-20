package com.example.ezplay.database.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.ezplay.R
import com.example.ezplay.database.Entity.Meal
import com.example.ezplay.database.Entity.MealList
import com.google.firebase.database.*
import java.math.BigDecimal
import java.math.RoundingMode

class MealOrderAdapter(val ctx: Context, val layoutResID: Int, val meallistList: List<MealList>)
    : ArrayAdapter<MealList>(ctx, layoutResID, meallistList){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
        val view: View = layoutInflater.inflate(layoutResID, null)

        val mealNameAndQuantity = view.findViewById<TextView>(R.id.mealNameAndQuantity)
        val mealprice = view.findViewById<TextView>(R.id.mealprice)

        val ref = FirebaseDatabase.getInstance().getReference("meal")
        val meallist = meallistList[position]
        ref.child(meallist.mealID.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                mealNameAndQuantity.text = p0.getValue(Meal::class.java)!!.mealName +
                        " x" + meallist.mealQuantity
                mealprice.text = "RM " + BigDecimal(p0.getValue(Meal::class.java)!!.mealPrice
                    .times(meallist.mealQuantity)).setScale(2, RoundingMode.HALF_EVEN).toString()
            }
        })

        return view
    }
}