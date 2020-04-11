package com.example.ezplay.database.Entity

class ThemePark(
    val themeParkID: Int,
    val themeParkImage: String,
    val themeParkName: String,
    val themeParkBusinessHours: String,
    val adultPrice: Double,
    val childPrice: Double,
    val staffID: String
) {
    constructor() : this(0, "", "", "", 0.0, 0.0, "")
}