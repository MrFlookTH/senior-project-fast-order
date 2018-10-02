package com.rsu57.ite.fastorder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_food_menu.*
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.rsu57.ite.fastorderforrestaurant.CategoryAdapter
import com.rsu57.ite.fastorderforrestaurant.CategoryModel
import com.squareup.picasso.Picasso

/**
 * A simple [Fragment] subclass.
 */
class FoodMenuFragment : Fragment(){

    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference
    private var category = ArrayList<CategoryModel>()
    private var mKeys = ArrayList<String>()
    private var adapter : CategoryAdapter ?= null
    private var mListener : ChildEventListener?= null
    private var recyclerViewLayoutManager : RecyclerView.LayoutManager ?= null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_food_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewLayoutManager = GridLayoutManager(context, 3)
        recyclerViewCategories.layoutManager = recyclerViewLayoutManager
        //recyclerViewMenu.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        val restaurantUid = activity!!.intent.extras.getString("restaurant_uid")
        adapter = CategoryAdapter(category,context!!,restaurantUid)
        recyclerViewCategories.adapter = adapter

        empty_view.showLoading()
        loadRestaurantData(restaurantUid)
    }

    private fun updateList(){
        mListener = object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                category.add(p0!!.getValue(CategoryModel::class.java)!!)
                var key = p0!!.key
                mKeys.add(key)
                adapter!!.notifyDataSetChanged()
                empty_view.showContent()
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                var value = p0!!.getValue(CategoryModel::class.java)!!
                var key = p0!!.key
                var index = mKeys.indexOf(key)
                category[index] = value
                adapter!!.notifyItemChanged(index)
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                var key = p0!!.key
                var index = mKeys.indexOf(key)
                mKeys.removeAt(index)
                category.removeAt(index)
                adapter!!.notifyItemRemoved(index)
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(activity,"Error",Toast.LENGTH_SHORT).show()
            }
        }
        val restaurantUid = activity!!.intent.extras.getString("restaurant_uid")
        dbRef.child("Categories").child(restaurantUid).addChildEventListener(mListener)

    }

    override fun onStart() {
        super.onStart()
        category.clear()
        mKeys.clear()
        empty_view.showLoading()
        updateList()
    }

    override fun onStop() {
        super.onStop()
        if(mListener != null){
            val restaurantUid = activity!!.intent.extras.getString("restaurant_uid")
            dbRef.child("Categories").child(restaurantUid).removeEventListener(mListener)
        }
        category.clear()
        mKeys.clear()
    }

    private fun loadRestaurantData(uid:String){
        dbRef.child("Restaurant").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                val restaurant = p0!!.getValue(RestaurantModel::class.java)
                activity!!.title = restaurant!!.name
                tvRestaurantName.text = restaurant!!.name
                tvRestaurantDesc.text = restaurant!!.description
                Picasso.with(context)
                        .load(restaurant.bannerPath)
                        .fit().centerCrop()
                        .into(ivRestaurantBanner)
                Picasso.with(context)
                        .load(restaurant.logoPath)
                        .placeholder(R.drawable.img_placeholder)
                        .resize(100,100)
                        .centerInside()
                        .into(ivRestaurantLogo)
            }
        })
    }


}
