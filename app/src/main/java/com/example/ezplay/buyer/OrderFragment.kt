package com.example.ezplay.buyer

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.databinding.FragmentOrderBinding
import kotlinx.android.synthetic.main.user_navbar.view.*

class OrderFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentOrderBinding>(inflater,
            R.layout.fragment_order,container,false)
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE
        binding.customNavbar.back.setOnClickListener { view?.findNavController()?.popBackStack() }

        val sharedPreferences: SharedPreferences = activity!!.getSharedPreferences("return", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor =  sharedPreferences.edit()
        editor.putString("returnFromOrder", "yes")
        editor.commit()

        return binding.root
    }

}
