package com.example.ezplay

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.ezplay.database.Entity.Users
import com.example.ezplay.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    lateinit var ref: DatabaseReference
    val mAuth = FirebaseAuth.getInstance()
    val isLogin = mAuth.currentUser

    @Suppress("UNUSED_VARIABLE")
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        // initiate the firebase database
        ref = FirebaseDatabase.getInstance().getReference("users")

        // determine the user account is logging or not
        if (isLogin != null) {
            val loading = ProgressDialog(this)
            loading.setMessage("Loading...")
            loading.show()

            // determine the logined user is customer or staff
            checkUserRole(loading)
        }
    }

    private fun checkUserRole(loading: ProgressDialog) {
        // get the logined user ID
        val uid = isLogin!!.uid

        ref.child(uid).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val urole = p0.getValue(Users::class.java)

                if (urole != null) {
                    if (urole.userRole.equals("Customer")) {
                        // redirect to the customer page
                        val intent = Intent(this@MainActivity, UserActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        // redirect to the seller page
                        val intent = Intent(this@MainActivity, SellerActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
                loading.dismiss()
            }
        })
    }

}
