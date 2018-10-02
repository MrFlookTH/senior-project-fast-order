package com.rsu57.ite.fastorder


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_my_cart.*
import com.afollestad.materialdialogs.MaterialDialog




/**
 * A simple [Fragment] subclass.
 */
class MyCartFragment : Fragment(), View.OnClickListener {

    private var dbAuth = FirebaseAuth.getInstance()
    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference
    private var myCart = ArrayList<MyCartModel>()
    private var mKeys = ArrayList<String>()
    private var adapter : MyCartAdapter ?= null
    private var myCartListener : ChildEventListener?= null
    private var totalPrice = 0.00
    private var tableNumber : String = ""

    override fun onClick(p0: View?) {
        when(p0){
            btnConfirmOrder ->{
                val builder = MaterialDialog.Builder(context!!)
                        .title("กรุณายืนยันคำสั่ง")
                        .content("ต้องการสั่งอาหารใช่หรือไม่?")
                        .positiveText("ตกลง")
                        .negativeText("ยกเลิก")
                        .canceledOnTouchOutside(false)
                        .onPositive(object : MaterialDialog.SingleButtonCallback{
                            override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                                confirmOrder()
                            }
                        })
                val dialog = builder.build()
                dialog.show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnConfirmOrder.setOnClickListener(this)
        activity!!.title = "My Cart"
        setTableNumber()

        //Set Adapter
        recyclerViewCartItems.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        adapter = MyCartAdapter(myCart,context!!)
        recyclerViewCartItems.setHasFixedSize(true)
        recyclerViewCartItems.adapter = adapter

    }

    private fun setTableNumber(){
        val restaurantUid = activity!!.intent.extras.getString("restaurant_uid")
        val tableUid = activity!!.intent.extras.getString("table_uid")
        dbRef.child("Tables").child(restaurantUid).child(tableUid).child("table_number").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()){
                    tableNumber = p0.getValue(String::class.java)!!
                    activity!!.title = "My Cart ($tableNumber)"
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        myCart.clear()
        mKeys.clear()
        empty_view.showLoading()
        updateList()
    }

    override fun onStop() {
        super.onStop()
        myCart.clear()
        mKeys.clear()
        if(myCartListener != null){
            val currentUser = dbAuth.currentUser!!.uid
            dbRef.child("UserCart").child(currentUser).removeEventListener(myCartListener)
        }
    }

    private fun updateList(){
        val currentUser = dbAuth.currentUser!!.uid

        dbRef.child("UserCart").child(currentUser).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(!p0!!.exists()){
                    empty_view.showEmpty()
                }
            }
        })

        myCartListener = object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                myCart.add(p0!!.getValue(MyCartModel::class.java)!!)
                var key = p0!!.key
                mKeys.add(key)
                adapter!!.notifyDataSetChanged()
                updateTotalPrice()
                empty_view.showContent()
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                var value = p0!!.getValue(MyCartModel::class.java)!!
                var key = p0!!.key
                var index = mKeys.indexOf(key)
                myCart[index] = value
                adapter!!.notifyItemChanged(index)
                updateTotalPrice()
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                var key = p0!!.key
                var index = mKeys.indexOf(key)
                mKeys.removeAt(index)
                myCart.removeAt(index)
                adapter!!.notifyItemRemoved(index)
                updateTotalPrice()
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show()
            }
        }
        dbRef.child("UserCart").child(currentUser).addChildEventListener(myCartListener)

    }

    private fun updateTotalPrice(){
        totalPrice = 0.00
        for(cart in myCart){
            totalPrice += cart.total
        }
        tvTotalPrice.text = this.resources.getString(R.string.price_format,totalPrice)
        if(myCart.size == 0){
            empty_view.showEmpty()
        }
    }

    private fun confirmOrder(){
        if(myCart.size > 0){
            val builder = MaterialDialog.Builder(context!!)
                    .content("กำลังส่งรายการอาหาร")
                    .progress(true, 0)
            val dialog = builder.build()
            dialog.show()

            val restaurantUid = activity!!.intent.extras.getString("restaurant_uid")
            val tableUid = activity!!.intent.extras.getString("table_uid")
            val currentUser = dbAuth.currentUser!!.uid

            dbRef.child("RequestTable").child(restaurantUid).child(tableUid).setValue(TableModel(tableUid,tableNumber))

            for (cart in myCart){
                val requestKey = dbRef.child("RequestItems").child(restaurantUid).child(tableUid).push().key
                dbRef.child("RequestItems").child(restaurantUid).child(tableUid).child(requestKey)
                        .setValue(RequestModel(requestKey,cart.uid,cart.name,cart.price,cart.qty,cart.total,ServerValue.TIMESTAMP,"กำลังรอ",currentUser))
            }

            dbRef.child("UserCart").child(currentUser).removeValue()
                    .addOnCompleteListener {
                        Toast.makeText(context,"สั่งอาหารเรียบร้อยแล้ว",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
        }
    }










}
