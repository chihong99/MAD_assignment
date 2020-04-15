package com.example.ezplay

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.ezplay.databinding.ActivitySellerBinding
import com.google.firebase.auth.FirebaseAuth

class SellerActivity : AppCompatActivity() {

    @Suppress("UNUSED_VARIABLE")
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivitySellerBinding>(this, R.layout.activity_seller)
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

}
