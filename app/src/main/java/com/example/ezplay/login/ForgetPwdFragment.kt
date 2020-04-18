package com.example.ezplay.login

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.databinding.FragmentForgetpwdBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.user_navbar.view.*

class ForgetPwdFragment : Fragment() {

    lateinit var Email: EditText
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentForgetpwdBinding>(inflater,
            R.layout.fragment_forgetpwd,container,false)
        Email = binding.emailEditText

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
        if(email.isEmpty()){
            Email.setError("Please enter your email")
        }
        else {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity, "Email sent", Toast.LENGTH_SHORT).show()
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("The reset password link is sent! Please Check your Email now!")
                        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                            view?.findNavController()?.popBackStack()
                        }
                        builder.show()
//                        view!!.findNavController().navigate(R.id.action_forgetPwdFragment_to_emailSendSuccessful)
                    }
                    if (!task.isSuccessful) {
                        Toast.makeText(activity, "Email entered does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        // [END send_password_reset]
    }

    companion object {
        private const val TAG = "EmailPassword"
    }

}

