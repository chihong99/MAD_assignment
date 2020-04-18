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
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.databinding.FragmentBookingBinding
import com.example.ezplay.getCurrentDate
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

class BookingFragment : Fragment() {

    lateinit var totalAmount: TextView
    lateinit var adultTicketQty: TextView
    lateinit var childTicketQty: TextView
    lateinit var dateText: EditText
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
    lateinit var calendar: Calendar
    private var day = 0
    private var month = 0
    private var year = 0
    private var adultTicketQuantity: Int = 1  // adult ticket minimum must be 1
    private var childTicketQuantity: Int = 0
    private var bookingDate: String = getCurrentDate()
    val mAuth = FirebaseAuth.getInstance()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentBookingBinding>(inflater,
            R.layout.fragment_booking,container,false)
        binding.customNavbar.back.setOnClickListener {
            view?.findNavController()?.popBackStack() }
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE

        val sharedPreferences: SharedPreferences = activity!!.getSharedPreferences(mAuth.currentUser!!.uid, Context.MODE_PRIVATE)
        val args = BookingFragmentArgs.fromBundle(arguments!!)
        binding.HeaderText.text = args.selectedThemeParkName + " Ticket"
        binding.AdultPriceTagText.text = "Adult Price (RM " + args.selectedThemeParkAdultPrice + ")"
        binding.ChildPriceTagText.text = "Child Price (RM " + args.selectedThemeParkChildPrice + ")"
        adultTicketQty = binding.adultTicketQuantityText
        childTicketQty = binding.childTicketQuantityText
        totalAmount = binding.totalAmountTextView
        dateText = binding.datePickerText

        if (savedInstanceState != null) {
            adultTicketQuantity = savedInstanceState.getInt("AdultTicketQuantity", 1)
            childTicketQuantity = savedInstanceState.getInt("ChildTicketQuantity", 0)
        } else {
            adultTicketQuantity = sharedPreferences.getInt("adultTicketQuantity", 1)
            childTicketQuantity = sharedPreferences.getInt("childTicketQuantity", 0)
            bookingDate = sharedPreferences.getString("bookingDate", getCurrentDate()).toString()
        }

        dateText.setText(bookingDate)
        adultTicketQty.text = adultTicketQuantity.toString()
        childTicketQty.text = childTicketQuantity.toString()
        calculateTicketPrice(args)

        calendar = Calendar.getInstance()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)

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
            if (adultTicketQty.text.toString().toInt() > 1) {
                adultTicketQuantity = adultTicketQty.text.toString().toInt() - 1
                adultTicketQty.text = adultTicketQuantity.toString()
                calculateTicketPrice(args)
            }
        }

        binding.plusAdultQuantityBtn.setOnClickListener {
            adultTicketQuantity = adultTicketQty.text.toString().toInt() + 1
            adultTicketQty.text = adultTicketQuantity.toString()
            calculateTicketPrice(args)
        }

        binding.minusChildQuantityBtn.setOnClickListener {
            if (childTicketQty.text.toString().toInt() > 0) {
                childTicketQuantity = childTicketQty.text.toString().toInt() - 1
                childTicketQty.text = childTicketQuantity.toString()
                calculateTicketPrice(args)
            }
        }

        binding.plusChildQuantityBtn.setOnClickListener {
            childTicketQuantity = childTicketQty.text.toString().toInt() + 1
            childTicketQty.text = childTicketQuantity.toString()
            calculateTicketPrice(args)
        }

        binding.goToOrderFoodBtn.setOnClickListener {
            val editor: SharedPreferences.Editor =  sharedPreferences.edit()
            editor.putString("bookingDate", dateText.text.toString())
            editor.putInt("adultTicketQuantity", adultTicketQty.text.toString().toInt())
            editor.putInt("childTicketQuantity", childTicketQty.text.toString().toInt())
            editor.putInt("themeParkID", args.selectedThemeParkID.toInt())
            editor.putString("adultTicketPrice", args.selectedThemeParkAdultPrice)
            editor.putString("childTicketPrice", args.selectedThemeParkChildPrice)
            editor.commit()

            view!!.findNavController().navigate(BookingFragmentDirections
                .actionBookingFragmentToOrderFragment(args.selectedThemeParkSellerID))
        }

        return binding.root
    }

    private fun calculateTicketPrice(args: BookingFragmentArgs) {
        val total = args.selectedThemeParkAdultPrice.toDouble().times(adultTicketQty.text.toString().toInt()) +
                args.selectedThemeParkChildPrice.toDouble().times(childTicketQty.text.toString().toInt())
        totalAmount.text = "RM " + BigDecimal(total).setScale(2, RoundingMode.HALF_EVEN).toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("AdultTicketQuantity", adultTicketQuantity)
        outState.putInt("ChildTicketQuantity", childTicketQuantity)
    }

}
