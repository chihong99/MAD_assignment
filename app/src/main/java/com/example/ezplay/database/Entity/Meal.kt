package com.example.ezplay.database.Entity

class Meal(
    val mealID: Int,
    val mealImage: String,
    val mealName: String,
    val mealPrice: Double,
    val staffID: String
) {
    constructor() : this(0,"","",0.0,"")
}