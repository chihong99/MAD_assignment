package com.example.ezplay

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

fun getCurrentDateTime(): String {
    var date = Date();
    val formatter = SimpleDateFormat("MMM dd yyyy HH:mma")
    val current: String = formatter.format(date)
    return  current
}

fun getCurrentDate(): String {
    var date = Date();
    val formatter = SimpleDateFormat("dd-MM-yyyy")
    val current: String = formatter.format(date)
    return  current
}

class ThemeParkViewHolder(var themeparkView: View): RecyclerView.ViewHolder(themeparkView) {}

class MealViewHolder(var mealView: View): RecyclerView.ViewHolder(mealView) {}

class PaymentViewHolder(var paymentView: View): RecyclerView.ViewHolder(paymentView) {}