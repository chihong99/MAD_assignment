package com.example.ezplay.database.Entity

class Cart (
    val mealID: Int,
    val mealName: String,
    val mealQuantity: Int,
    val mealPrice: Double,
    val userID: String
) {
    constructor() : this(0,"",0,0.0,"")
}