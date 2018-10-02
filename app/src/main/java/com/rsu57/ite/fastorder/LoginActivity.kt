package com.rsu57.ite.fastorder

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.alert
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.FirebaseException


class LoginActivity : AppCompatActivity(),View.OnClickListener {

    private lateinit var dbAuth : FirebaseAuth
    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Init()
        dbAuth = FirebaseAuth.getInstance()
        //val shared = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE)
        //etUsername.setText(shared.getString("email", ""))
    }

    override fun onStart() {
        super.onStart()
        var currentUser = dbAuth.currentUser
        if(currentUser != null){
            LoadMain(currentUser)
        }

    }

    override fun onClick(p0: View?) {
        when(p0){
            btnLogin ->{
                val email = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()
                if(ValidateLogin(email,password)) {
                    LoginToFirebase(email, password)
                }
            }
            btnSignup ->{
                val intent = Intent(this,SignupActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun Init(){
        btnLogin.setOnClickListener(this)
        btnSignup.setOnClickListener(this)
    }

    private fun LoginToFirebase(email:String,password:String){
        val progressDialog = ProgressDialog(this@LoginActivity)
        btnLogin.isEnabled = false
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) { }

        progressDialog.setMessage("กำลังเข้าสู่ระบบ")
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        dbAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){ task ->
                    if (task.isSuccessful){
                        //Load User data & Shared Preferences
                        dbRef.child("Users").child(dbAuth.currentUser!!.uid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                                        var shared = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE)
                                        var editor = shared.edit()
                                        val userData = dataSnapshot!!.getValue(Users::class.java)
                                        editor.putString("email",dbAuth.currentUser!!.email.toString())
                                        editor.putString("firstName",userData!!.firstname)
                                        editor.putString("lastName",userData!!.lastname)
                                        editor.apply()
                                        progressDialog.dismiss()
                                        LoadMain(dbAuth.currentUser as FirebaseUser)
                                    }

                                    override fun onCancelled(p0: DatabaseError?) {
                                        ShortToast("Can't load user data")
                                    }
                                })
                    }else{
                        progressDialog.dismiss()
                        btnLogin.isEnabled = true
                        ShortToast(task.exception!!.message.toString())
                    }
                }
    }



    private fun ValidateLogin(email:String, password:String) : Boolean{
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

    private fun LoadMain(currentUser: FirebaseUser){
        var intent = Intent(this,ScanTagActivity::class.java)
        //var intent = Intent(this,MainActivity::class.java)
        //intent.putExtra("email",currentUser.email.toString())
        startActivity(intent)
        finish()
    }


    private fun ShortToast(message:String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }

}
