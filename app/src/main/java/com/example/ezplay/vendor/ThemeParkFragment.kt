package com.example.ezplay.vendor

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.ezplay.R
import com.example.ezplay.database.Entity.ThemePark
import com.example.ezplay.databinding.FragmentThemeparkStaffBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class ThemeParkFragment : Fragment() {

    lateinit var uploadImageView: ImageView

    // view fields
    lateinit var themeparkInfo: CardView
    lateinit var themeparkNameTextView: TextView
    lateinit var themeparkBusinessHourTextView: TextView
    lateinit var themeparkAdultPriceTextView: TextView
    lateinit var themeparkChildPriceTextView: TextView

    // edit fields
    lateinit var themeparkEdit: CardView
    lateinit var themeparkNameEditText: EditText
    lateinit var themeparkBusinessHourEditText: EditText
    lateinit var themeparkAdultPriceEditText: EditText
    lateinit var themeparkChildPriceEditText: EditText

    // button fields
    lateinit var editButton: Button
    lateinit var updateButton: Button
    lateinit var cancelButton: Button

    lateinit var ref: DatabaseReference
    val mAuth = FirebaseAuth.getInstance()
    var uri: Uri? = null
    var preventLoop = 0
    var isChange = 0
    var currentThemParkID = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentThemeparkStaffBinding>(inflater,
            R.layout.fragment_themepark_staff,container,false)
        binding.customNavbar.back.visibility = View.INVISIBLE
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.mealBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_themeParkFragment_to_mealFragment)}
        binding.customNavbar.seller_settingsBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_themeParkFragment_to_settingsStaffFragment)}

        // initiate the firebase database
        ref = FirebaseDatabase.getInstance().getReference("theme park")

        uploadImageView = binding.uploadImageView

        themeparkInfo = binding.themeparkInfoTemplate
        themeparkNameTextView = binding.themeparkNameTextView
        themeparkBusinessHourTextView = binding.themeparkBusinessHourTextView
        themeparkAdultPriceTextView = binding.themeparkAdultPriceTextView
        themeparkChildPriceTextView = binding.themeparkChildPriceTextView

        themeparkEdit = binding.themeparkInfoEditTemplate
        themeparkNameEditText = binding.themeparkNameEditText
        themeparkBusinessHourEditText = binding.themeparkBusinessHourEditText
        themeparkAdultPriceEditText = binding.themeparkAdultPriceEditText
        themeparkChildPriceEditText = binding.themeparkChildPriceEditText

        editButton = binding.editButton
        updateButton = binding.updateButton
        cancelButton = binding.cancelButton

        displayCurrentThemePark(1)

        uploadImageView.setOnClickListener {
            if (!editButton.isVisible) {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 0)
            }
        }

        editButton.setOnClickListener {
            editButton.visibility = View.GONE
            updateButton.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            themeparkInfo.visibility = View.GONE
            themeparkEdit.visibility = View.VISIBLE
            displayCurrentThemePark(0)
        }

        updateButton.setOnClickListener {
            preventLoop = 0

            if (edittextValidation()) {
                val loading = ProgressDialog(context)
                loading.setMessage("Loading...")
                loading.show()
                uploadImageToFirebase()

                loading.dismiss()
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Update Successful!")
                builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                    displayCurrentThemePark(1)
                }
                builder.show()

                editButton.visibility = View.VISIBLE
                updateButton.visibility = View.GONE
                cancelButton.visibility = View.GONE
                themeparkInfo.visibility = View.VISIBLE
                themeparkEdit.visibility = View.GONE
            }
        }

        cancelButton.setOnClickListener {
            editButton.visibility = View.VISIBLE
            updateButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
            themeparkInfo.visibility = View.VISIBLE
            themeparkEdit.visibility = View.GONE
            displayCurrentThemePark(1)
        }

        return binding.root
    }

    private fun edittextValidation(): Boolean {
        var isValid = true
        if (themeparkNameEditText.text.toString().trim().isEmpty()) {
            isValid = false
            themeparkNameEditText.setError("Theme Park name is required")
        }
        if (themeparkBusinessHourEditText.text.toString().trim().isEmpty()) {
            isValid = false
            themeparkBusinessHourEditText.setError("Business hour is required")
        }
        if (themeparkAdultPriceEditText.text.toString().trim().isEmpty()) {
            isValid = false
            themeparkAdultPriceEditText.setError("Adult Price is required")
        } else {
            val str = themeparkAdultPriceEditText.text.toString().trim()
            var k = 0
            for (i in str) {
                k++
                if (!i.isDigit()) {
                    if (i != '.' && k != 1) {
                        isValid = false
                        themeparkAdultPriceEditText.setError("Please enter a valid price")
                    } else if (i == '.' && k == 1) {
                        isValid = false
                        themeparkAdultPriceEditText.setError("Please enter a valid price")
                    }
                }
            }
        }
        if (themeparkChildPriceEditText.text.toString().trim().isEmpty()) {
            isValid = false
            themeparkChildPriceEditText.setError("Child Price is required")
        } else {
            val str = themeparkChildPriceEditText.text.toString().trim()
            for (i in str) {
                if (!i.isDigit() ) {
                    if (i != '.') {
                        isValid = false
                        themeparkChildPriceEditText.setError("Please enter a valid price")
                    }
                }
            }
        }
        if (uri == null) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Please upload your theme park image!")
            builder.setPositiveButton(android.R.string.ok) { dialog, which -> }
            builder.show()
            isValid = false
        }
        return isValid
    }

    private fun displayCurrentThemePark(j: Int) {
        ref.orderByChild("staffID").equalTo(mAuth.currentUser!!.uid).addListenerForSingleValueEvent(
            object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        for (i in p0.children) {
                            val themepark = i.getValue(ThemePark::class.java)
                            Picasso.with(context!!).load(themepark!!.themeParkImage).into(uploadImageView)
                            if (j == 1) {
                                currentThemParkID = themepark.themeParkID.toInt()
                                uri = Uri.parse(themepark.themeParkImage)
                                // edit button has not click yet
                                themeparkNameTextView.text = "Name: " + themepark.themeParkName
                                themeparkBusinessHourTextView.text =
                                    "Business Hours: " + themepark.themeParkBusinessHours
                                themeparkAdultPriceTextView.text = "Adult Price: RM " +
                                        BigDecimal(themepark.adultPrice).setScale(
                                            2,
                                            RoundingMode.HALF_EVEN
                                        ).toString()
                                themeparkChildPriceTextView.text = "Child Price: RM " +
                                        BigDecimal(themepark.childPrice).setScale(
                                            2,
                                            RoundingMode.HALF_EVEN
                                        ).toString()
                            } else {
                                // edit button has been clicked
                                themeparkNameEditText.setText(themepark.themeParkName)
                                themeparkBusinessHourEditText.setText(themepark.themeParkBusinessHours)
                                themeparkAdultPriceEditText.setText(BigDecimal(themepark.adultPrice)
                                    .setScale(2, RoundingMode.HALF_EVEN).toString())
                                themeparkChildPriceEditText.setText(BigDecimal(themepark.childPrice)
                                    .setScale(2, RoundingMode.HALF_EVEN).toString())
                            }
                        }
                    }
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)

            // resize the chosen image
            var resizeImg: Bitmap = Bitmap.createScaledBitmap(bitmap, 640, 450, false)
            uploadImageView.setImageBitmap(resizeImg)
            isChange = 1
        }
    }

    private fun uploadImageToFirebase() {
        if (uri == null) return

        if (isChange == 1) {
            val imageName = UUID.randomUUID().toString()
            val refStorage = FirebaseStorage.getInstance().getReference("/image/$imageName")
            refStorage.putFile(uri!!).addOnSuccessListener {
                refStorage.downloadUrl.addOnSuccessListener {
                    saveThemeParkToFirebase(it)
                }
            }
        } else {
            val uid = mAuth.currentUser!!.uid
            ref.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val newThemePark = ThemePark(currentThemParkID, uri.toString(), themeparkNameEditText.text.toString(),
                            themeparkBusinessHourEditText.text.toString(), themeparkAdultPriceEditText.text.toString().toDouble(),
                            themeparkChildPriceEditText.text.toString().toDouble(), uid)
                        ref.child(currentThemParkID.toString()).setValue(newThemePark)
                    }
                }
            })
        }
    }

    private fun saveThemeParkToFirebase(uri: Uri) {
        val uid = mAuth.currentUser!!.uid
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists() && preventLoop == 0 && currentThemParkID == 0) {
                    preventLoop++
                    var totalRecords = p0.childrenCount
                    totalRecords++
                    val newThemePark = ThemePark(totalRecords.toInt(), uri.toString(), themeparkNameEditText.text.toString(),
                        themeparkBusinessHourEditText.text.toString(), themeparkAdultPriceEditText.text.toString().toDouble(),
                        themeparkChildPriceEditText.text.toString().toDouble(), uid)
                    ref.child(totalRecords.toString()).setValue(newThemePark)
                } else if (!p0.exists() && preventLoop == 0 && currentThemParkID == 0) {
                    preventLoop++
                    val newThemePark = ThemePark(1, uri.toString(), themeparkNameEditText.text.toString(),
                        themeparkBusinessHourEditText.text.toString(), themeparkAdultPriceEditText.text.toString().toDouble(),
                        themeparkChildPriceEditText.text.toString().toDouble(), uid)
                    ref.child("1").setValue(newThemePark)
                } else if (preventLoop == 0 && currentThemParkID == 1) {
                    preventLoop++
                    val newThemePark = ThemePark(currentThemParkID, uri.toString(), themeparkNameEditText.text.toString(),
                        themeparkBusinessHourEditText.text.toString(), themeparkAdultPriceEditText.text.toString().toDouble(),
                        themeparkChildPriceEditText.text.toString().toDouble(), uid)
                    ref.child(currentThemParkID.toString()).setValue(newThemePark)
                }
            }
        })
    }

}
