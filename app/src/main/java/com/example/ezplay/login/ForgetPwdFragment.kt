package com.example.ezplay.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.databinding.FragmentForgetpwdBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.user_navbar.view.*

class ForgetPwdFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentForgetpwdBinding>(inflater,
            R.layout.fragment_forgetpwd,container,false)
        val Email = binding.emailEditText
        binding.SendEmailbtn.setOnClickListener{
            sendPasswordReset(Email.text.toString())
        }

        binding.customNavbar.back.setOnClickListener {
            view?.findNavController()?.popBackStack() }
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE

        return binding.root
    }
    private fun sendPasswordReset(email: String) {
        // [START send_password_reset]
        val auth = FirebaseAuth.getInstance()

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }
        // [END send_password_reset]
    }

    companion object {
        private const val TAG = "EmailPassword"
    }

}

