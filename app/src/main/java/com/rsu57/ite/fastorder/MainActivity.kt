package com.rsu57.ite.fastorder

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity(),View.OnClickListener {

    private lateinit var dbAuth : FirebaseAuth
    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference

    private var ft: FragmentTransaction? = null
    private var currentFragment: Fragment? = null
    private val manager = supportFragmentManager
    private var currentFragmentPosition = 0


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if(currentFragmentPosition != item.itemId){
            when (item.itemId) {
                R.id.action_menu -> {
                    currentFragmentPosition = item.itemId
                    currentFragment = FoodMenuFragment()
                    ft = supportFragmentManager.beginTransaction()
                    ft!!.replace(R.id.mainContent, currentFragment)
                    ft!!.commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_myAccount -> {
                    /*currentFragmentPosition = item.itemId
                    currentFragment = AccountFragment()
                    ft = supportFragmentManager.beginTransaction()
                    ft!!.replace(R.id.mainContent, currentFragment)
                    ft!!.commit()
                    return@OnNavigationItemSelectedListener true*/
                }
                R.id.action_myCart -> {
                    currentFragmentPosition = item.itemId
                    currentFragment = MyCartFragment()
                    ft = supportFragmentManager.beginTransaction()
                    ft!!.replace(R.id.mainContent, currentFragment)
                    ft!!.commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_myOrder -> {
                    currentFragmentPosition = item.itemId
                    currentFragment = MyOrderFragment()
                    ft = supportFragmentManager.beginTransaction()
                    ft!!.replace(R.id.mainContent, currentFragment)
                    ft!!.commit()
                    return@OnNavigationItemSelectedListener true
                }
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Bottom navigation
        val bnav = bottomNavigation as BottomNavigationView
        BottomNavigationViewHelper.disableShiftMode(bnav)

        val transaction = manager.beginTransaction()
        val fragment = FoodMenuFragment()
        currentFragmentPosition = R.id.action_menu
        transaction.replace(R.id.mainContent,fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        //val restaurantUid = intent.extras.getString("restaurant_uid")
        //loadRestaurantData(restaurantUid)

    }

    override fun onClick(p0: View?) {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    private fun loadRestaurantData(uid:String){
        dbRef.child("Restaurant").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                val restaurant = p0!!.getValue(RestaurantModel::class.java)
                title = restaurant!!.name
            }
        })
    }

    private fun logOut(){
        var pDialog = ProgressDialog(this)
        pDialog.setCancelable(false)
        pDialog.setCanceledOnTouchOutside(false)
        pDialog.setTitle("กำลังออกจากระบบ")
        pDialog.show()
        dbAuth.signOut()
        pDialog.dismiss()
        var intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun shortToast(message:String){
        Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
    }


}












