package com.example.ezplay.vendor

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

    lateinit var saveContext: Context
    lateinit var uploadImageView: ImageView

    // view fields
    lateinit var themeparkNameTextView: TextView
    lateinit var themeparkBusinessHourTextView: TextView
    lateinit var themeparkAdultPriceTextView: TextView
    lateinit var themeparkChildPriceTextView: TextView

    // edit fields
    lateinit var themeparkNameEditText: EditText
    lateinit var themeparkBusinessHourEditText: EditText
    lateinit var themeparkAdultPriceEditText: EditText
    lateinit var themeparkChildPriceEditText: EditText

    // cache
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    lateinit var loading: ProgressDialog
    lateinit var ref: DatabaseReference
    val mAuth = FirebaseAuth.getInstance()
    var uri: Uri? = null
    var preventLoop = 0
    var isChange = 0
    var currentThemParkID = 0

    //@RequiresApi(Build.VERSION_CODES.M)
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

        // initiate the cacha file
        sharedPreferences = activity!!.getSharedPreferences(mAuth.currentUser!!.uid, Context.MODE_PRIVATE)
        editor =  sharedPreferences.edit()

        // initiate the firebase database
        ref = FirebaseDatabase.getInstance().getReference("theme park")

        uploadImageView = binding.uploadImageView

        themeparkNameTextView = binding.themeparkNameTextView
        themeparkBusinessHourTextView = binding.themeparkBusinessHourTextView
        themeparkAdultPriceTextView = binding.themeparkAdultPriceTextView
        themeparkChildPriceTextView = binding.themeparkChildPriceTextView

        themeparkNameEditText = binding.themeparkNameEditText
        themeparkBusinessHourEditText = binding.themeparkBusinessHourEditText
        themeparkAdultPriceEditText = binding.themeparkAdultPriceEditText
        themeparkChildPriceEditText = binding.themeparkChildPriceEditText

        displayCurrentThemePark()

        uploadImageView.setOnClickListener {
            if (!binding.editButton.isVisible) {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 0)
            }
        }

        binding.editButton.setOnClickListener {
            isChange = 0
            binding.editButton.visibility = View.GONE
            binding.updateButton.visibility = View.VISIBLE
            binding.cancelButton.visibility = View.VISIBLE
            binding.themeparkInfoTemplate.visibility = View.GONE
            binding.themeparkInfoEditTemplate.visibility = View.VISIBLE
        }

        binding.updateButton.setOnClickListener {
            preventLoop = 0

            if (edittextValidation()) {
                loading = ProgressDialog(context)
                loading.setMessage("Loading...")
                loading.show()
                uploadImageToFirebase()
                binding.editButton.visibility = View.VISIBLE
                binding.updateButton.visibility = View.GONE
                binding.cancelButton.visibility = View.GONE
                binding.themeparkInfoTemplate.visibility = View.VISIBLE
                binding.themeparkInfoEditTemplate.visibility = View.GONE
            }
        }

        binding.cancelButton.setOnClickListener {
            uri = null
            binding.editButton.visibility = View.VISIBLE
            binding.updateButton.visibility = View.GONE
            binding.cancelButton.visibility = View.GONE
            binding.themeparkInfoTemplate.visibility = View.VISIBLE
            binding.themeparkInfoEditTemplate.visibility = View.GONE
            displayCurrentThemePark()
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

    private fun displayCurrentThemePark() {
        var tpid = sharedPreferences.getInt("themeParkID",  0)
        var image = sharedPreferences.getString("themeParkImage", "")
        var name = sharedPreferences.getString("themeParkName", "")
        var hour = sharedPreferences.getString("themeParkBusinessHours", "")
        var adult = sharedPreferences.getString("adultPrice", "")
        var child = sharedPreferences.getString("childPrice", "")

        if (tpid != 0) {
            currentThemParkID = tpid
            if (image != "" && uri.toString() != image) {
                uri = Uri.parse(image)
                Picasso.with(saveContext).load(image).into(uploadImageView)
            }
            if (name != "") {
                themeparkNameTextView.text = "Name: " + name
                themeparkNameEditText.setText(name)
            }
            if (hour != "") {
                themeparkBusinessHourTextView.text = "Business Hours: " + hour
                themeparkBusinessHourEditText.setText(hour)
            }
            if (adult != "" && child != "") {
                themeparkAdultPriceTextView.text = "Adult Price: RM " +
                        BigDecimal(adult!!.toDouble()).setScale(2, RoundingMode.HALF_EVEN)
                            .toString()
                themeparkChildPriceTextView.text = "Child Price: RM " +
                        BigDecimal(child!!.toDouble()).setScale(2, RoundingMode.HALF_EVEN)
                            .toString()

                themeparkAdultPriceEditText.setText(
                    BigDecimal(adult!!.toDouble())
                        .setScale(2, RoundingMode.HALF_EVEN).toString()
                )
                themeparkChildPriceEditText.setText(
                    BigDecimal(child!!.toDouble())
                        .setScale(2, RoundingMode.HALF_EVEN).toString()
                )
            }
        }
        else {
            ref.orderByChild("staffID").equalTo(mAuth.currentUser!!.uid)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                for (i in p0.children) {
                                    val themepark = i.getValue(ThemePark::class.java)
                                    if (tpid == 0) {
                                        AsyncTask.execute {
                                            editor.putInt(
                                                "themeParkID",
                                                themepark!!.themeParkID
                                            )
                                            editor.putString(
                                                "themeParkImage",
                                                themepark.themeParkImage
                                            )
                                            editor.putString(
                                                "themeParkName",
                                                themepark.themeParkName
                                            )
                                            editor.putString(
                                                "themeParkBusinessHours",
                                                themepark.themeParkBusinessHours
                                            )
                                            editor.putString(
                                                "adultPrice",
                                                themepark.adultPrice.toString()
                                            )
                                            editor.putString(
                                                "childPrice",
                                                themepark.childPrice.toString()
                                            )
                                            editor.commit()
                                        }
                                        currentThemParkID = themepark!!.themeParkID
                                        uri = Uri.parse(themepark.themeParkImage)
                                        Picasso.with(saveContext)
                                            .load(themepark.themeParkImage)
                                            .into(uploadImageView)
                                        themeparkNameTextView.text =
                                            "Name: " + themepark.themeParkName
                                        themeparkNameEditText.setText(themepark.themeParkName)
                                        themeparkBusinessHourTextView.text =
                                            "Business Hours: " + themepark.themeParkBusinessHours
                                        themeparkBusinessHourEditText.setText(themepark.themeParkBusinessHours)
                                        themeparkAdultPriceTextView.text =
                                            "Adult Price: RM " +
                                                    BigDecimal(themepark.adultPrice).setScale(
                                                        2,
                                                        RoundingMode.HALF_EVEN
                                                    ).toString()
                                        themeparkChildPriceTextView.text =
                                            "Child Price: RM " +
                                                    BigDecimal(themepark.childPrice).setScale(
                                                        2,
                                                        RoundingMode.HALF_EVEN
                                                    ).toString()
                                        themeparkAdultPriceEditText.setText(
                                            BigDecimal(themepark.adultPrice)
                                                .setScale(2, RoundingMode.HALF_EVEN)
                                                .toString()
                                        )
                                        themeparkChildPriceEditText.setText(
                                            BigDecimal(themepark.childPrice)
                                                .setScale(2, RoundingMode.HALF_EVEN)
                                                .toString()
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.saveContext = context
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

        AsyncTask.execute {
            if (isChange == 1) {
                val imageName = UUID.randomUUID().toString()
                val refStorage = FirebaseStorage.getInstance().getReference("/image/$imageName")
                refStorage.putFile(uri!!).addOnSuccessListener {
                    refStorage.downloadUrl.addOnSuccessListener {
                        editor.putString("themeParkImage", it.toString())
                        saveThemeParkToFirebase(it)
                    }
                }
            } else {
                val uid = mAuth.currentUser!!.uid
                ref.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            val newThemePark = ThemePark(
                                currentThemParkID,
                                uri.toString(),
                                themeparkNameEditText.text.toString(),
                                themeparkBusinessHourEditText.text.toString(),
                                themeparkAdultPriceEditText.text.toString().toDouble(),
                                themeparkChildPriceEditText.text.toString().toDouble(),
                                uid
                            )
                            ref.child(currentThemParkID.toString()).setValue(newThemePark)
                            saveIntoCache(
                                currentThemParkID,
                                uri.toString(),
                                themeparkNameEditText.text.toString(),
                                themeparkBusinessHourEditText.text.toString(),
                                themeparkAdultPriceEditText.text.toString(),
                                themeparkChildPriceEditText.text.toString()
                            )
                        }
                    }
                })
            }
        }
    }

    private fun saveThemeParkToFirebase(uri: Uri) {
        AsyncTask.execute {
            val uid = mAuth.currentUser!!.uid
            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists() && preventLoop == 0 && currentThemParkID == 0) {
                        // add new theme park but not the first record
                        preventLoop++
                        var totalRecords = p0.childrenCount
                        totalRecords++
                        // save into firebase
                        val newThemePark = ThemePark(
                            totalRecords.toInt(),
                            uri.toString(),
                            themeparkNameEditText.text.toString(),
                            themeparkBusinessHourEditText.text.toString(),
                            themeparkAdultPriceEditText.text.toString().toDouble(),
                            themeparkChildPriceEditText.text.toString().toDouble(),
                            uid
                        )
                        ref.child(totalRecords.toString()).setValue(newThemePark)
                        // save into cache
                        saveIntoCache(
                            totalRecords.toInt(),
                            uri.toString(),
                            themeparkNameEditText.text.toString(),
                            themeparkBusinessHourEditText.text.toString(),
                            themeparkAdultPriceEditText.text.toString(),
                            themeparkChildPriceEditText.text.toString()
                        )
                    } else if (!p0.exists() && preventLoop == 0 && currentThemParkID == 0) {
                        // add new theme park that is first record
                        preventLoop++
                        // save into firebase
                        val newThemePark = ThemePark(
                            1,
                            uri.toString(),
                            themeparkNameEditText.text.toString(),
                            themeparkBusinessHourEditText.text.toString(),
                            themeparkAdultPriceEditText.text.toString().toDouble(),
                            themeparkChildPriceEditText.text.toString().toDouble(),
                            uid
                        )
                        ref.child("1").setValue(newThemePark)
                        // save into cache
                        saveIntoCache(
                            1,
                            uri.toString(),
                            themeparkNameEditText.text.toString(),
                            themeparkBusinessHourEditText.text.toString(),
                            themeparkAdultPriceEditText.text.toString(),
                            themeparkChildPriceEditText.text.toString()
                        )
                    } else if (preventLoop == 0 && currentThemParkID != 0) {
                        // edit current theme park
                        preventLoop++
                        // save into firebase
                        val newThemePark = ThemePark(
                            currentThemParkID,
                            uri.toString(),
                            themeparkNameEditText.text.toString(),
                            themeparkBusinessHourEditText.text.toString(),
                            themeparkAdultPriceEditText.text.toString().toDouble(),
                            themeparkChildPriceEditText.text.toString().toDouble(),
                            uid
                        )
                        ref.child(currentThemParkID.toString()).setValue(newThemePark)
                        // save into cache
                        saveIntoCache(
                            currentThemParkID,
                            uri.toString(),
                            themeparkNameEditText.text.toString(),
                            themeparkBusinessHourEditText.text.toString(),
                            themeparkAdultPriceEditText.text.toString(),
                            themeparkChildPriceEditText.text.toString()
                        )
                    }
                }
            })
        }
    }

    private fun saveIntoCache(currentThemParkID: Int, toString: String, toString1: String,
                              toString2: String, toString3: String, toString4: String) {
        AsyncTask.execute {
            editor.putInt("themeParkID", currentThemParkID)
            if (isChange == 0) editor.putString("themeParkImage", toString)
            editor.putString("themeParkName", toString1)
            editor.putString("themeParkBusinessHours", toString2)
            editor.putString("adultPrice", toString3)
            editor.putString("childPrice", toString4)
            editor.commit()
        }
        loading.dismiss()
        Toast.makeText(saveContext, "Theme Park Updated Successful", Toast.LENGTH_SHORT).show()
        displayCurrentThemePark()
    }

}
