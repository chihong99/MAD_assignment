package com.example.ezplay.buyer

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
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
    var day = 0
    var month = 0
    var year = 0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentBookingBinding>(inflater,
            R.layout.fragment_booking,container,false)
        binding.customNavbar.back.setOnClickListener {
            view?.findNavController()?.popBackStack() }
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE

        val args = BookingFragmentArgs.fromBundle(arguments!!)
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
            var currentDate = dateText.text.toString()
            var delimeter = "-"
            var dayMonthYear = currentDate.split(delimeter)
            datePickerDialog.updateDate(dayMonthYear[2].toInt(), dayMonthYear[1].toInt() - 1, dayMonthYear[0].toInt())
            // set the minimum date can be selected
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            // set the maximum date can be selected, which are the range from current to next 3 months
            var maxDate = Calendar.getInstance()
            maxDate.set(dayMonthYear[2].toInt(), dayMonthYear[1].toInt() + 2, dayMonthYear[0].toInt())
            datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
            datePickerDialog.show()
        }

        return binding.root
    }

}
