package com.example.ezplay.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.databinding.FragmentSearchBinding
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.user_navbar.view.*

class SearchFragment : Fragment() {

    lateinit var ref: DatabaseReference
    lateinit var themeparkNameAdapter: ArrayAdapter<String>
    lateinit var spinnerList: ArrayList<String>
    lateinit var selectedThemePark: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentSearchBinding>(inflater,
            R.layout.fragment_search,container,false)
        binding.customNavbar.back.visibility = View.INVISIBLE
        binding.customNavbar.sellerNavbar.visibility = View.GONE
        binding.customNavbar.favouriteBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_searchFragment_to_favouriteFragment)}
        binding.customNavbar.settingsBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_searchFragment_to_settingsFragment)}

        // initiate the firebase database
        ref = FirebaseDatabase.getInstance().getReference("theme park")
        spinnerList = ArrayList()
        themeparkNameAdapter = ArrayAdapter(context!!, R.layout.themepark_spinner, spinnerList)
        binding.themeparkSpinner.adapter = themeparkNameAdapter
        ref.orderByChild("themeParkName").addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (i in p0.children) {
                    spinnerList.add(i.child("themeParkName").getValue().toString())
                    themeparkNameAdapter.notifyDataSetChanged()
                }
            }
        })

        binding.themeparkSpinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                selectedThemePark = binding.themeparkSpinner.selectedItem.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // TODO Auto-generated method stub
            }
        })

        binding.goButton.setOnClickListener {view: View ->
            view.findNavController().navigate(SearchFragmentDirections
                .actionSearchFragmentToThemeParkInfoFragment(selectedThemePark))
        }

        return binding.root
    }

}
