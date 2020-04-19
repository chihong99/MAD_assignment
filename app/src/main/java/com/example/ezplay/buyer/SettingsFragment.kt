package com.example.ezplay.buyer

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ezplay.MainActivity
import com.example.ezplay.R
import com.example.ezplay.database.Entity.Users
import com.example.ezplay.databinding.FragmentSettingsBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.user_navbar.view.*

class SettingsFragment : Fragment() {

    lateinit var oldPassword: EditText
    lateinit var newPassword: EditText
    lateinit var newRetypePassword: EditText
    lateinit var currentNameText: TextView
    lateinit var ref: DatabaseReference
    val mAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentSettingsBinding>(inflater,
            R.layout.fragment_settings,container,false)

        // set the custom layout and navigation
        binding.customNavbar.back.visibility = View.INVISIBLE
        binding.customNavbar.sellerNavbar.visibility = View.GONE
        binding.customNavbar.searchBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_settingsFragment_to_searchFragment)}
        binding.customNavbar.favouriteBtn.setOnClickListener {view : View ->
            view.findNavController().navigate(R.id.action_settingsFragment_to_favouriteFragment)}

        currentNameText = binding.currentUserName
        ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(mAuth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentNameText.text = p0.getValue(Users::class.java)!!.username + " >"
            }
        })
        binding.currentUserEmail.text = mAuth.currentUser!!.email

        binding.username.setOnClickListener {
            popUpChangeNameDialog()
        }

        binding.password.setOnClickListener {
            popUpChangePassword()
        }

        binding.transaction.setOnClickListener {
            view?.findNavController()?.navigate(SettingsFragmentDirections.actionSettingsStaffFragmentToTransactionHistoryFragment())
        }

        binding.logoutBtn.setOnClickListener {
            mAuth.signOut()
            // redirect to the login page
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return binding.root
    }

    private fun popUpChangeNameDialog() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.edit_username, null)
        val changeName = view.findViewById<EditText>(R.id.editUsername)
        changeName.setText(currentNameText.text.toString().substring(0, currentNameText.text.toString().length - 2))

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Update Name")
        builder.setPositiveButton("Update") { p0, p1 -> }
        builder.setNegativeButton("Cancel") { p0, p1 -> }
        builder.setView(view)

        val alert = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (changeName.text.toString().trim().isNotEmpty()) {
                alert.dismiss()
                val user = Users(mAuth.currentUser!!.uid, changeName.text.toString().trim(), "Customer")
                ref.child(mAuth.currentUser!!.uid).setValue(user)
                Toast.makeText(activity, "Name updated", Toast.LENGTH_SHORT).show()
                currentNameText.text = changeName.text.toString().trim() + " >"
            } else {
                changeName.setError("Name cannot be blank")
                changeName.requestFocus()
            }
        }
    }

    private fun popUpChangePassword() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.edit_password, null)
        oldPassword  = view.findViewById(R.id.oldPassword)
        newPassword = view.findViewById(R.id.newPassword)
        newRetypePassword = view.findViewById(R.id.retypeNewPassword)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Update Password")
        builder.setPositiveButton("Update") { p0, p1 -> }
        builder.setNegativeButton("Cancel") { p0, p1 -> }
        builder.setView(view)

        val alert = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (checkPasswordValidation()) {
                val credential = EmailAuthProvider.getCredential(
                    mAuth.currentUser!!.email!!, oldPassword.text.toString().trim())
                mAuth.currentUser!!.reauthenticate(credential).addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Toast.makeText(activity, "Invalid old password", Toast.LENGTH_SHORT).show()
                    } else {
                        alert.dismiss()
                        mAuth.currentUser!!.updatePassword(newPassword.text.toString().trim()).addOnCompleteListener { view ->
                            if (view.isSuccessful) {
                                Toast.makeText(activity, "Password updated", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkPasswordValidation(): Boolean {
        var isValid = true
        if (oldPassword.text.toString().trim().isEmpty()) {
            oldPassword.setError("Old password cannot be blank")
            isValid = false
        }
        if (newPassword.text.toString().trim().isEmpty()) {
            newPassword.setError("New password cannot be blank")
            isValid = false
        } else {
            if (newPassword.text.toString().trim().length < 8) {
                newPassword.setError("Password too short, minimum 8 character.")
                isValid = false
            }
        }
        if (newRetypePassword.text.toString().trim().isEmpty()) {
            newRetypePassword.setError("Retype password cannot be blank")
            isValid = false
        }
        if (newPassword.text.toString().trim() != newRetypePassword.text.toString().trim() && isValid) {
            Toast.makeText(activity, "Password do not match", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }

}
