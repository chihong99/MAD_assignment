package com.example.ezplay.database.Entity

class Booking(
    val bookingID: Int,
    val bookingDate: String,
    val adultTicketQuantity: Int,
    val childTicketQuantity: Int,
    val themeParkID: Int,
    val userID: String
) {
    constructor(): this(0,"",0,0,0,"")
}