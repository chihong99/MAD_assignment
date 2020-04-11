package com.example.ezplay.database.Entity

class Payment(
    val paymentID: Int,
    val paymentDate: String,
    val paymentMethod: String,
    val bookingID: Int
) {
    constructor() : this(0,"","",0)
}