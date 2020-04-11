//package com.example.ezplay.database.Adapter
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import com.bumptech.glide.Glide
//import com.example.ezplay.R
//import com.example.ezplay.database.Entity.ThemePark
//import java.math.BigDecimal
//import java.math.RoundingMode
//
//class ThemeParkEditAdapter(val ctx: Context, val layoutResID: Int, val themeparkList: List<ThemePark>)
//    : ArrayAdapter<ThemePark>(ctx, layoutResID, themeparkList){
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
//        val view: View = layoutInflater.inflate(layoutResID, null)
//
//        // after edit widgets
//        val themeparkImageButton = view.findViewById<ImageButton>(R.id.uploadImageBtn)
//        val themeparkNameEditText = view.findViewById<EditText>(R.id.themeparkNameEditText)
//        val themeparkBusinessHourEditText = view.findViewById<EditText>(R.id.themeparkBusinessHourEditText)
//        val themeparkAdultPriceEditText = view.findViewById<EditText>(R.id.themeparkAdultPriceEditText)
//        val themeparkChildPriceEditText = view.findViewById<EditText>(R.id.themeparkChildPriceEditText)
//
//        val themepark = themeparkList[position]
//        Glide.with(ctx).load(themepark.themeParkImage).into(themeparkImageButton)
//        themeparkNameEditText.setText(themepark.themeParkName)
//        themeparkBusinessHourEditText.setText(themepark.themeParkBusinessHours)
//        themeparkAdultPriceEditText.setText(BigDecimal(themepark.adultPrice).setScale(2, RoundingMode.HALF_EVEN).toString())
//        themeparkChildPriceEditText.setText(BigDecimal(themepark.childPrice).setScale(2, RoundingMode.HALF_EVEN).toString())
//
//        return view
//    }
//
//}