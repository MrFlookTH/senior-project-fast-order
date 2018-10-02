package com.rsu57.ite.fastorder

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_signup.*
import org.jetbrains.anko.alert



class SignupActivity : AppCompatActivity(), View.OnClickListener {

    private var dbAuth : FirebaseAuth ?= null
    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        Init()
        dbAuth = FirebaseAuth.getInstance()
    }

    override fun onClick(p0: View?) {
        when(p0){
            btnSignup -> {
                val email = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val firstname = etFirstName.text.toString().trim()
                val lastname = etLastName.text.toString().trim()
                ValidateForm(email,password)
                SignupToFirebase(email,password,firstname,lastname)
            }
        }
    }

    private fun Init(){
        btnSignup.setOnClickListener(this)
    }

    private fun SignupToFirebase(email:String,password:String,firstname:String,lastname:String){
        val progressDialog = ProgressDialog(this)
        btnSignup.isEnabled = false
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm!!.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) { }
        progressDialog.setMessage("กำลังสร้างบัญชีผู้ใช้")
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        dbAuth!!.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task ->
                    if(task.isSuccessful){
                        //Save to database
                        var currentUser = dbAuth!!.currentUser
                        val newUser = Users(email, firstname, lastname)
                        if(currentUser != null) {
                            dbRef.child("Users").child(currentUser.uid).setValue(newUser)
                            progressDialog.dismiss()
                            ShortToast("Signup Successful")
                            LoadMain()
                        }

                    }else{
                        progressDialog.dismiss()
                        btnSignup.isEnabled = true
                        ShortToast(task.exception!!.message.toString())
                    }
                }
    }


    private fun ValidateForm(email:String, password:String) : Boolean{
        if(email.equals("") || password.equals("")){
            alert("Please enter your email and password"){
                positiveButton("OK"){}
            }.show()
            return false
        }

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            alert("Invalid email"){
                positiveButton("OK"){}
            }.show()
            return false
        }

        if(password.length < 6){
            alert("Password should be at least 6 characters"){
                positiveButton("OK"){}
            }.show()
            return false
        }
        return true
    }

    private fun LoadMain(){
        var intent = Intent(this,ScanTagActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun ShortToast(message:String){
        Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
    }
}
