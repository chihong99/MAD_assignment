package com.example.ezplay.buyer

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.databinding.FragmentBookingBinding
import com.example.ezplay.getCurrentDate
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.text.SimpleDateFormat
import java.util.*

class BookingFragment : Fragment() {

    lateinit var dateText: EditText
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
    lateinit var calendar: Calendar
    private var day = 0
    private var month = 0
    private var year = 0
    private var adultTicketQuantity: Int = 1  // adult ticket minimum must be 1
    private var childTicketQuantity: Int = 0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentBookingBinding>(inflater,
            R.layout.fragment_booking,container,false)
        binding.customNavbar.back.setOnClickListener {
            view?.findNavController()?.popBackStack() }
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE

        if (savedInstanceState != null) {
            adultTicketQuantity = savedInstanceState.getInt("AdultTicketQuantity", 1)
            childTicketQuantity = savedInstanceState.getInt("ChildTicketQuantity", 0)
            binding.adultTicketQuantityText.text = adultTicketQuantity.toString()
            binding.childTicketQuantityText.text = childTicketQuantity.toString()
        }

        val sharedPreferences: SharedPreferences = activity!!.getSharedPreferences("return", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor =  sharedPreferences.edit()
        val isReturn = sharedPreferences.getString("returnFromOrder", "no")
        if (isReturn.equals("yes")) {
            editor.remove("returnFromOrder")
            editor.commit()
            // restore the data when navigate from order to here

        }

        val args = BookingFragmentArgs.fromBundle(arguments!!)
        binding.HeaderText.text = args.selectedThemeParkName + " Ticket"
        binding.AdultPriceTagText.text = "Adult Price (RM " + args.selectedThemeParkAdultPrice + ")"
        binding.ChildPriceTagText.text = "Child Price (RM " + args.selectedThemeParkChildPrice + ")"
        dateText = binding.datePickerText

        calendar = Calendar.getInstance()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)
        dateText.setText(getCurrentDate())

        dateText.setOnClickListener {
            val datePickerDialog =
                DatePickerDialog(context!!,
                    OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        calendar.set(year, monthOfYear, dayOfMonth)
                        dateText.setText(dateFormat.format(calendar.time))
                    }, year, month, day
                )
            //show the previous selected date
            val currentDate = dateText.text.toString()
            val delimeter = "-"
            val dayMonthYear = currentDate.split(delimeter)
            datePickerDialog.updateDate(dayMonthYear[2].toInt(), dayMonthYear[1].toInt() - 1, dayMonthYear[0].toInt())
            // set the minimum date can be selected
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            // set the maximum date can be selected, which are the range from current to next 3 months
            val maxDate = Calendar.getInstance()
            maxDate.set(dayMonthYear[2].toInt(), dayMonthYear[1].toInt() + 2, dayMonthYear[0].toInt())
            datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
            datePickerDialog.show()
        }

        binding.minusAdultQuantityBtn.setOnClickListener {
            if (binding.adultTicketQuantityText.text.toString().toInt() > 1) {
                adultTicketQuantity = binding.adultTicketQuantityText.text.toString().toInt() - 1
                binding.adultTicketQuantityText.text = adultTicketQuantity.toString()
            }
        }

        binding.plusAdultQuantityBtn.setOnClickListener {
            adultTicketQuantity = binding.adultTicketQuantityText.text.toString().toInt() + 1
            binding.adultTicketQuantityText.text = adultTicketQuantity.toString()
        }

        binding.minusChildQuantityBtn.setOnClickListener {
            if (binding.childTicketQuantityText.text.toString().toInt() > 0) {
                childTicketQuantity = binding.childTicketQuantityText.text.toString().toInt() - 1
                binding.childTicketQuantityText.text = childTicketQuantity.toString()
            }
        }

        binding.plusChildQuantityBtn.setOnClickListener {
            childTicketQuantity = binding.childTicketQuantityText.text.toString().toInt() + 1
            binding.childTicketQuantityText.text = childTicketQuantity.toString()
        }

        binding.goToOrderFoodBtn.setOnClickListener {
            view!!.findNavController().navigate(BookingFragmentDirections
                .actionBookingFragmentToOrderFragment(args.selectedThemeParkID.toString()))
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("AdultTicketQuantity", adultTicketQuantity)
        outState.putInt("ChildTicketQuantity", childTicketQuantity)
    }

}
