package com.rsu57.ite.fastorder

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_food_items.*

class FoodItemsActivity : AppCompatActivity(), View.OnClickListener {

    override fun onClick(p0: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var dbAuth = FirebaseAuth.getInstance()
    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference
    private var mKeys = ArrayList<String>()
    private var adapter : FoodItemsAdapter ?= null
    private var foodItems = ArrayList<FoodItemModel>()
    private var mListener : ChildEventListener?= null
    private var priceListener : ChildEventListener?= null
    private var categoryUID : String? = null
    private var restaurantUID : String? = null
    private var myCart = ArrayList<MyCartModel>()
    private var myCartKeys = ArrayList<String>()
    private var totalPrice : Double = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_items)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        categoryUID = intent.extras.getString("category_uid")
        restaurantUID = intent.extras.getString("restaurant_uid")

        //Set Adapter
        recyclerViewFoodItems.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        adapter = FoodItemsAdapter(foodItems,this)
        recyclerViewFoodItems.setHasFixedSize(true)
        recyclerViewFoodItems.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        foodItems.clear()
        mKeys.clear()
        myCart.clear()
        empty_view.showLoading()
        updateList()
        addPriceListener()
    }

    override fun onStop() {
        super.onStop()
        foodItems.clear()
        myCart.clear()
        mKeys.clear()
        if(mListener != null){
            dbRef.child("MenuItems").child(restaurantUID).child(categoryUID).removeEventListener(mListener)
        }
        if(priceListener != null){
            val currentUser = dbAuth.currentUser!!.uid
            dbRef.child("UserCart").child(currentUser).removeEventListener(priceListener)
        }
    }

    private fun updateList(){
        mListener = object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                empty_view.showError()
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                var value = p0!!.getValue(FoodItemModel::class.java)!!
                var key = p0!!.key
                var index = mKeys.indexOf(key)
                foodItems[index] = value
                adapter!!.notifyItemChanged(index)
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                foodItems.add(p0!!.getValue(FoodItemModel::class.java)!!)
                var key = p0!!.key
                mKeys.add(key)
                adapter!!.notifyDataSetChanged()
                empty_view.showContent()
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                var key = p0!!.key
                var index = mKeys.indexOf(key)
                mKeys.removeAt(index)
                foodItems.removeAt(index)
                adapter!!.notifyItemRemoved(index)
            }
        }

        dbRef.child("MenuItems").child(restaurantUID).child(categoryUID).addChildEventListener(mListener)

    }

    private fun addPriceListener(){
        val currentUser = dbAuth.currentUser!!.uid
        priceListener = object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                var value = p0!!.getValue(MyCartModel::class.java)!!
                var key = p0!!.key
                var index = myCartKeys.indexOf(key)
                myCart[index] = value
                updateTotalPrice()
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                myCart.add(p0!!.getValue(MyCartModel::class.java)!!)
                var key = p0!!.key
                myCartKeys.add(key)
                updateTotalPrice()
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                var key = p0!!.key
                var index = myCartKeys.indexOf(key)
                myCartKeys.removeAt(index)
                myCart.removeAt(index)
                updateTotalPrice()
            }
        }
        dbRef.child("UserCart").child(currentUser).addChildEventListener(priceListener)
    }

    private fun updateTotalPrice(){
        totalPrice = 0.00
        for(cart in myCart){
            totalPrice += cart.total
        }
        tvTotalPrice.text = this.resources.getString(R.string.total_price_format,totalPrice)
    }


}
