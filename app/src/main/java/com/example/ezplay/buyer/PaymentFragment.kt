package com.example.ezplay.buyer

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.UserActivity
import com.example.ezplay.database.Entity.*
import com.example.ezplay.databinding.FragmentPaymentBinding
import com.example.ezplay.getCurrentDateTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.user_navbar.view.*

class PaymentFragment : Fragment() {

    lateinit var args: PaymentFragmentArgs
    lateinit var account: EditText
    lateinit var password: EditText
    lateinit var nameOnCard: EditText
    lateinit var cardNumber: EditText
    lateinit var mmyy: EditText
    lateinit var cvc: EditText
    private var accountText: String = ""
    private var passwordText: String = ""
    private var nameOnCardText: String = ""
    private var cardNumberText: String = ""
    private var mmyyText: String = ""
    private var cvcText: String = ""
    lateinit var onlineBankingRef: DatabaseReference
    lateinit var visaCardRef: DatabaseReference
    lateinit var bookingRef: DatabaseReference
    lateinit var paymentRef: DatabaseReference
    lateinit var cartRef: DatabaseReference
    lateinit var mealListRef: DatabaseReference
    val mAuth = FirebaseAuth.getInstance()
    private var bookingNo = 0
    private var meallistNo = 0
    private var paymentNo = 0
    private var cartMealCount = 0
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentPaymentBinding>(
            inflater, R.layout.fragment_payment, container, false
        )
        binding.customNavbar.back.setOnClickListener {
            view?.findNavController()?.popBackStack()
        }
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.sellerNavbar.visibility = View.GONE

        sharedPreferences = activity!!.getSharedPreferences(mAuth.currentUser!!.uid, Context.MODE_PRIVATE)
        onlineBankingRef = FirebaseDatabase.getInstance().getReference("online banking")
        visaCardRef = FirebaseDatabase.getInstance().getReference("visa card")
        bookingRef = FirebaseDatabase.getInstance().getReference("booking")
        paymentRef = FirebaseDatabase.getInstance().getReference("payment")
        mealListRef = FirebaseDatabase.getInstance().getReference("meal list")
        cartRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.currentUser!!.uid).child("cart")

        bookingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                bookingNo = p0.childrenCount.toInt()
            }
        })

        paymentRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                paymentNo = p0.childrenCount.toInt()
            }
        })

        mealListRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                meallistNo = p0.childrenCount.toInt()
            }
        })

        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                cartMealCount = p0.childrenCount.toInt()
            }
        })

        account = binding.accountEditText
        password = binding.passwordEditText
        nameOnCard = binding.nameOnCardEditText
        cardNumber = binding.cardNumberEditText
        mmyy = binding.mmyyEditText
        cvc = binding.cvcEditText

        accountText = account.text.toString()
        passwordText = password.text.toString()
        nameOnCardText = nameOnCard.text.toString()
        cardNumberText = cardNumber.text.toString()
        mmyyText = mmyy.text.toString()
        cvcText = cvc.text.toString()

        if (savedInstanceState != null) {
            account.setText(savedInstanceState.getString("account"))
            password.setText(savedInstanceState.getString("password"))
            nameOnCard.setText(savedInstanceState.getString("nameOnCard"))
            cardNumber.setText(savedInstanceState.getString("cardNumber"))
            mmyy.setText(savedInstanceState.getString("mmyy"))
            cvc.setText(savedInstanceState.getString("cvc"))
        }

        args = PaymentFragmentArgs.fromBundle(arguments!!)

        if (args.paymentMethod == "Online Banking") {
            binding.onlineBankingCardView.visibility = View.VISIBLE
            binding.visaCardCardView.visibility = View.GONE
        } else {
            binding.onlineBankingCardView.visibility = View.GONE
            binding.visaCardCardView.visibility = View.VISIBLE
        }

        var isBackspaceClicked = false
        mmyy.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (!isBackspaceClicked) {
                    if (s.length == 2)
                        s.append('/')
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                isBackspaceClicked = after < count
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.onlineBankingPayButton.setOnClickListener {
            if (verifyOnlineBanking()) {
                onlineBankingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            val onlinebanking = p0.getValue(OnlineBanking::class.java)
                            if (onlinebanking!!.account == account.text.toString() &&
                                onlinebanking.password == password.text.toString()) {
                                completePayment()
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Transaction completed")
                                builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                                    val intent = Intent(activity, UserActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                                builder.show()
                            } else {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Invalid account info!")
                                builder.setPositiveButton(android.R.string.ok) { dialog, which -> }
                                builder.show()
                            }
                        }
                    }
                })
            }
        }

        binding.visaCardPayButton.setOnClickListener {
            if (verifyVisaCard()) {
                visaCardRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            val visacard = p0.getValue(VisaCard::class.java)
                            if (visacard!!.nameOnCard == nameOnCard.text.toString() &&
                                visacard.cardNumber == cardNumber.text.toString() &&
                                visacard.mmyy == mmyy.text.toString() &&
                                visacard.cvc == cvc.text.toString()) {
                                completePayment()
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Transaction completed")
                                builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                                    val intent = Intent(activity, UserActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                                builder.show()
                            } else {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Invalid visa card info!")
                                builder.setPositiveButton(android.R.string.ok) { dialog, which -> }
                                builder.show()
                            }
                        }
                    }
                })
            }
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("account", accountText)
        outState.putString("password", passwordText)
        outState.putString("nameOnCard", nameOnCardText)
        outState.putString("cardNumber", cardNumberText)
        outState.putString("mmyy", mmyyText)
        outState.putString("cvc", cvcText)
    }

    private fun verifyOnlineBanking(): Boolean {
        var isValid = true
        if (account.text.isEmpty()) {
            isValid = false
            account.setError("Account cannot be blank")
        }
        if (password.text.isEmpty()) {
            isValid = false
            password.setError("Password cannot be blank")
        }
        return isValid
    }

    private fun verifyVisaCard(): Boolean {
        var isValid = true
        if (nameOnCard.text.isEmpty()) {
            isValid = false
            nameOnCard.setError("Name on card cannot be blank")
        }
        if (cardNumber.text.isEmpty()) {
            isValid = false
            cardNumber.setError("Card number cannot be blank")
        }
        if (mmyy.text.isEmpty()) {
            isValid = false
            mmyy.setError("MM/YY cannot be blank")
        }
        if (cvc.text.isEmpty()) {
            isValid = false
            cvc.setError("CVC cannot be blank")
        }
        return isValid
    }

    private fun completePayment() {
        saveBookingDetails()
        saveMealListDetails()
        savePaymentDetails()
    }

    private fun saveBookingDetails() {
        val bookingID = bookingNo + 1
        var meallistID = 0
        if (cartMealCount == 0) {
            meallistID = 0
        } else {
            meallistID = meallistNo + 1
        }
        val booking = Booking(
            bookingID,
            sharedPreferences.getString("bookingDate", "").toString(),
            sharedPreferences.getInt("adultTicketQuantity", 1),
            sharedPreferences.getInt("childTicketQuantity", 0),
            meallistID,
            sharedPreferences.getInt("themeParkID", 0),
            mAuth.currentUser!!.uid
        )
        bookingRef.child(bookingID.toString()).setValue(booking)
    }

    private fun saveMealListDetails() {
        var meallistID = 0
        if (cartMealCount == 0) {
            meallistID = 0
        } else {
            meallistID = meallistNo + 1
        }
        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (i in p0.children) {
                        val cart = i.getValue(Cart::class.java)
                        val meallist = MealList(cart!!.mealID, cart.mealQuantity)
                        mealListRef.child(meallistID.toString()).child(cart.mealID.toString()).setValue(meallist)
                    }
                }
            }
        })
    }

    private fun savePaymentDetails() {
        val bookingID = bookingNo + 1
        val paymentID = paymentNo + 1
        val payment = Payment(
            paymentID,
            getCurrentDateTime(),
            args.paymentMethod,
            args.totalAmount.substring(3, args.totalAmount.length).toDouble(),
            bookingID
        )
        paymentRef.child(paymentID.toString()).setValue(payment)
    }

}
