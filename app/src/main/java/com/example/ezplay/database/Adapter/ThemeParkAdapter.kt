//package com.example.ezplay.database.Adapter
//
//import android.app.Activity
//import android.app.AlertDialog
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.graphics.Bitmap
//import android.net.Uri
//import android.provider.MediaStore
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.core.app.ActivityCompat.startActivityForResult
//import com.bumptech.glide.Glide
//import com.example.ezplay.R
//import com.example.ezplay.database.Entity.ThemePark
//import com.example.ezplay.vendor.ThemeParkFragment
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import java.math.BigDecimal
//import java.math.RoundingMode
//
//class ThemeParkAdapter(val ctx: Context, val layoutResID: Int, val themeparkList: List<ThemePark>)
//    : ArrayAdapter<ThemePark>(ctx, layoutResID, themeparkList){
//
//    lateinit var ref: DatabaseReference
//    var uri: Uri? = null
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
//        val view: View = layoutInflater.inflate(layoutResID, null)
//
//        val editButton = view.findViewById<Button>(R.id.editButton)
//        val themeparkImage = view.findViewById<ImageView>(R.id.uploadImageView)
//        val themeparkNameT = view.findViewById<TextView>(R.id.themeparkNameText)
//        val themeparkBusinessHourT = view.findViewById<TextView>(R.id.themeparkBusinessHourText)
//        val themeparkAdultPriceT = view.findViewById<TextView>(R.id.themeparkAdultPriceText)
//        val themeparkChildPriceT = view.findViewById<TextView>(R.id.themeparkChildPriceText)
//
//        val themepark = themeparkList[position]
//        Glide.with(ctx).load(themepark.themeParkImage).into(themeparkImage)
//        themeparkNameT.text = "Theme Park Name: " + themepark.themeParkName
//        themeparkBusinessHourT.text = "Business Hours: " + themepark.themeParkBusinessHours
//        themeparkAdultPriceT.text = "Adult Price: RM" +
//                BigDecimal(themepark.adultPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
//        themeparkChildPriceT.text = "Child Price: RM" +
//                BigDecimal(themepark.childPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
//
//        editButton.setOnClickListener {
////            val sharedPreferences: SharedPreferences = ctx!!.getSharedPreferences("ThemeParkImage", Context.MODE_PRIVATE)
////            val editor: SharedPreferences.Editor =  sharedPreferences.edit()
////            editor.putString("ImageUrl", themepark.themeParkImage)
////            editor.commit()
////            themeparkImage.visibility = View.GONE
//            //popupEditDialog(themepark)
//        }
//
//        return view
//    }
//
//    private fun popupEditDialog(themepark: ThemePark) {
//        // initiate the firebase database
//        ref = FirebaseDatabase.getInstance().getReference("theme park")
//
//        val builder = AlertDialog.Builder(ctx)
//        val inflater = LayoutInflater.from(ctx)
//        val view = inflater.inflate(R.layout.custom_themepark_seller_edit, null)
//
//        val themeparkImageBtn = view.findViewById<ImageButton>(R.id.uploadImageBtn)
//        val themeparkNameET = view.findViewById<EditText>(R.id.themeparkNameEditText)
//        val themeparkBusinessHourET = view.findViewById<EditText>(R.id.themeparkBusinessHourEditText)
//        val themeparkAdultPriceET = view.findViewById<EditText>(R.id.themeparkAdultPriceEditText)
//        val themeparkChildPriceET = view.findViewById<EditText>(R.id.themeparkChildPriceEditText)
//
//        Glide.with(ctx).load(themepark.themeParkImage).into(themeparkImageBtn)
//        themeparkNameET.setText(themepark.themeParkName)
//        themeparkBusinessHourET.setText(themepark.themeParkBusinessHours)
//        themeparkAdultPriceET.setText(BigDecimal(themepark.adultPrice).setScale(2, RoundingMode.HALF_EVEN).toString())
//        themeparkChildPriceET.setText(BigDecimal(themepark.childPrice).setScale(2, RoundingMode.HALF_EVEN).toString())
//
//        builder.setTitle("Edit Theme Park Info")
//        builder.setView(view)
//        builder.setPositiveButton("Update") { p0, p1 ->
//            val tpName = themeparkNameET.text.toString().trim()
//            val tpHour = themeparkBusinessHourET.text.toString().trim()
//            val tpAdultprice = themeparkAdultPriceET.text.toString().trim()
//            val tpChildPrice = themeparkChildPriceET.text.toString().trim()
//
//            if (tpName.isEmpty() || tpHour.isEmpty() || tpAdultprice.isEmpty() || tpChildPrice.isEmpty()) {
//                // error
//                return@setPositiveButton
//            }
//        }
//        builder.setNegativeButton("Cancel") { p0, p1 -> }
//        val alert = builder.create()
//        alert.show()
//    }
//
//}