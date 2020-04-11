package com.example.ezplay.database.Entity

class Meal(
    val mealID: Int,
    val mealName: String,
    val mealPrice: Double,
    val staffID: String
) {
    constructor() : this(0,"",0.0,"")
}