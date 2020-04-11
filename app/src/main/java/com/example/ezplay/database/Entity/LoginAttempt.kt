package com.example.ezplay.database.Entity

class LoginAttempt(
    val currentAttempt: Int
) {
    constructor() : this(0)
}