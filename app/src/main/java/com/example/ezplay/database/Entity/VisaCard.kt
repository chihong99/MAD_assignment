package com.example.ezplay.database.Entity

class VisaCard(
    val nameOnCard: String,
    val cardNumber: String,
    val mmyy: String,
    val cvc: String
) {
    constructor() : this("","","","")
}