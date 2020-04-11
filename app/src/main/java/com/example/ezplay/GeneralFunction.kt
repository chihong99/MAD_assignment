package com.example.ezplay

import java.text.SimpleDateFormat
import java.util.*

fun getCurrentDateTime(): String {
    var date = Date();
    val formatter = SimpleDateFormat("MMM dd yyyy HH:mma")
    val current: String = formatter.format(date)
    return  current
}

fun isNotEmpty(text: String): Boolean {
    return text != ""
}


