package com.example.ezplay.vendor

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.isInvisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ezplay.MealViewHolder
import com.example.ezplay.R
import com.example.ezplay.database.Entity.Meal
import com.example.ezplay.databinding.FragmentMealStaffBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.staff_meal_list.view.*
import kotlinx.android.synthetic.main.user_navbar.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class MealFragment : Fragment() {

    lateinit var loading: CardView
    lateinit var mealRecyclerView: RecyclerView
    lateinit var initAddNewMealButton: ImageView
    lateinit var addImage: ImageView
    lateinit var name: EditText
    lateinit var price: EditText
    lateinit var mealList: MutableList<Meal>
    lateinit var ref: DatabaseReference
    lateinit var FirebaseRecyclerAdapter : FirebaseRecyclerAdapter<Meal, MealViewHolder>
    val mAuth = FirebaseAuth.getInstance()
    var mealCount = 0
    var uri: Uri? = null
    var isChange = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentMealStaffBinding>(inflater,
            R.layout.fragment_meal_staff,container,false)
        binding.customNavbar.back.visibility = View.INVISIBLE
        binding.customNavbar.userNavbar.visibility = View.GONE
        binding.customNavbar.themeparkBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_mealFragment_to_themeParkFragment)}
        binding.customNavbar.seller_settingsBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_mealFragment_to_settingsStaffFragment)}

        loading = binding.customNavbar.loadingLayout
        mealRecyclerView = binding.mealList
        initAddNewMealButton = binding.initAddNewMealBtn
        mealList = mutableListOf()
        ref = FirebaseDatabase.getInstance().getReference("meal")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                mealCount = p0.childrenCount.toInt()
            }
        })
        mealRecyclerView.setHasFixedSize(true)
        mealRecyclerView.layoutManager = LinearLayoutManager(context)
        displayMealList()

        initAddNewMealButton.setOnClickListener {
            popupAddOrEditMealDialog(0)
        }

        return binding.root
    }

    private fun displayMealList() {
        val query = ref.orderByChild("staffID").equalTo(mAuth.currentUser!!.uid)
        FirebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Meal, MealViewHolder>(
            Meal::class.java,
            R.layout.staff_meal_list,
            MealViewHolder::class.java,
            query
        ) {
            override fun populateViewHolder(viewHolder: MealViewHolder?, model: Meal?, position: Int) {
                Picasso.with(context).load(model?.mealImage)
                    .into(viewHolder!!.mealView.mealImageView)
                viewHolder.mealView.mealNameTextView.setText(model?.mealName)
                viewHolder.mealView.mealPriceTextView.setText("RM" +
                        BigDecimal(model!!.mealPrice).setScale(2, RoundingMode.HALF_EVEN).toString())
                viewHolder.mealView.setOnClickListener {
                    // pop up a edit dialog
                    popupAddOrEditMealDialog(model.mealID)
                }
            }

        }
        mealRecyclerView.adapter = FirebaseRecyclerAdapter
    }

    private fun popupAddOrEditMealDialog(id: Int) {
        isChange = 0
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.add_or_edit_meal, null)
        addImage = view.findViewById<ImageView>(R.id.addNewMealImageViewButton)
        name = view.findViewById<EditText>(R.id.addNewMealNameEditText)
        price = view.findViewById<EditText>(R.id.addNewMealPriceEditText)

        addImage.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 0)
        }

        val builder = AlertDialog.Builder(context)

        // if id != 0 , it means user wants to edit meal record
        if (id != 0) {
            ref.orderByChild(id.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (i in p0.children) {
                        val meal = i.getValue(Meal::class.java)
                        if (meal!!.mealID == id) {
                            uri = Uri.parse(meal.mealImage)
                            Picasso.with(context).load(uri).into(addImage)
                            name.setText(meal.mealName)
                            price.setText(
                                BigDecimal(meal.mealPrice).setScale(
                                    2,
                                    RoundingMode.HALF_EVEN
                                ).toString()
                            )
                        }
                    }
                }
            })
            builder.setTitle("Update Meal")
            builder.setPositiveButton("Update") { p0, p1 -> }
        } else {
            builder.setTitle("Add New Meal")
            builder.setPositiveButton("Add") { p0, p1 -> }
        }

        builder.setNegativeButton("Cancel") { p0, p1 -> }
        builder.setView(view)

        val alert = builder.create()
        alert.show()

        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (checkInputValidation()) {
                alert.dismiss()
                loading.visibility = View.VISIBLE
                saveMealRecordToFirebase(id)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)

            // resize the chosen image
            var resizeImg: Bitmap = Bitmap.createScaledBitmap(bitmap, 320, 240, false)
            addImage.setImageBitmap(resizeImg)
            isChange = 1
        }
    }

    private fun checkInputValidation(): Boolean {
        var isValid = true
        if (uri == null) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Please upload your meal image!")
            builder.setPositiveButton(android.R.string.ok) { p0, p1 -> }
            builder.show()
            isValid = false
        }
        if (name.text.toString().trim().isEmpty()) {
            name.setError("Meal name is required")
            isValid = false
        }
        if (price.text.toString().trim().isEmpty()) {
            price.setError("Meal price is required")
        } else {
            val str = price.text.toString().trim()
            var k = 0
            for (i in str) {
                k++
                if (!i.isDigit()) {
                    if (i != '.' && k != 1) {
                        isValid = false
                        price.setError("Please enter a valid price")
                    } else if (i == '.' && k == 1) {
                        isValid = false
                        price.setError("Please enter a valid price")
                    }
                }
            }
        }

        return isValid
    }

    private fun saveMealRecordToFirebase(id: Int) {
        val uid = mAuth.currentUser!!.uid
        var mealID = mealCount
        if (id == 0)
            mealID++
        else
            mealID = id

        if (isChange == 1) {
            val imageName = UUID.randomUUID().toString()
            val refStorage = FirebaseStorage.getInstance().getReference("/image/$imageName")
            refStorage.putFile(uri!!).addOnSuccessListener {
                refStorage.downloadUrl.addOnSuccessListener {
                    AsyncTask.execute {
                        val meal = Meal(
                            mealID,
                            it.toString(),
                            name.text.toString(),
                            price.text.toString().toDouble(),
                            uid
                        )
                        ref.child(mealID.toString()).setValue(meal)
                    }
                    Toast.makeText(activity, "Meal added successful", Toast.LENGTH_SHORT).show()
                    loading.visibility = View.GONE
                }
            }
        } else {
            AsyncTask.execute {
                val meal = Meal(
                    mealID,
                    uri.toString(),
                    name.text.toString(),
                    price.text.toString().toDouble(),
                    uid
                )
                ref.child(mealID.toString()).setValue(meal)
            }
            Toast.makeText(activity, "Meal updated successful", Toast.LENGTH_SHORT).show()
            loading.visibility = View.GONE
        }
    }

}
