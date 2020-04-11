package com.example.ezplay.vendor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ezplay.MainActivity
import com.example.ezplay.R
import com.example.ezplay.databinding.FragmentSettingsStaffBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.user_navbar.view.*

class SettingsFragment : Fragment() {

    val mAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentSettingsStaffBinding>(inflater,
            R.layout.fragment_settings_staff,container,false)
        binding.customNavbar.back.visibility = View.INVISIBLE
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.themeparkBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_settingsStaffFragment_to_themeParkFragment)}
        binding.customNavbar.mealBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_settingsStaffFragment_to_mealFragment)}

        binding.logoutBtn.setOnClickListener {
            mAuth.signOut()
            // redirect to the login page
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return binding.root
    }

}
