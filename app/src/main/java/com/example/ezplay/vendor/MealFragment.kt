package com.example.ezplay.vendor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.databinding.FragmentMealStaffBinding
import kotlinx.android.synthetic.main.user_navbar.view.*

class MealFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentMealStaffBinding>(inflater,
            R.layout.fragment_meal_staff,container,false)
        binding.customNavbar.back.visibility = View.INVISIBLE
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.themeparkBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_mealFragment_to_themeParkFragment)}
        binding.customNavbar.seller_settingsBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_mealFragment_to_settingsStaffFragment)}

        return binding.root
    }

}
