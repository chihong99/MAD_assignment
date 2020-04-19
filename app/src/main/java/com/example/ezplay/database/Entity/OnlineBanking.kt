package com.example.ezplay.database.Entity

class OnlineBanking(
    val account: String,
    val password: String
) {
    constructor() : this("","")
}