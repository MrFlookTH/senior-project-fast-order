package com.rsu57.ite.fastorder


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_my_order.*


/**
 * A simple [Fragment] subclass.
 */
class MyOrderFragment : Fragment(),View.OnClickListener {

    private var dbAuth = FirebaseAuth.getInstance()
    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference
    private var myOrder = ArrayList<RequestModel>()
    private var mKeys = ArrayList<String>()
    private var adapter : MyOrderAdapter ?= null
    private var myOrderListener : ChildEventListener?= null
    private var totalPrice = 0.00

    override fun onClick(p0: View?) {
        when(p0){
            btnCheckout -> {
                checkOut()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnCheckout.setOnClickListener(this)
        activity!!.title = "My Order"
        //Set Adapter
        val restaurantUid = activity!!.intent.extras.getString("restaurant_uid")
        val tableUid = activity!!.intent.extras.getString("table_uid")
        recyclerViewOrder.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        adapter = MyOrderAdapter(myOrder,context!!,restaurantUid,tableUid)
        recyclerViewOrder.setHasFixedSize(true)
        recyclerViewOrder.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        myOrder.clear()
        mKeys.clear()
        empty_view.showLoading()
        updateList()
    }

    override fun onPause() {
        super.onPause()
        myOrder.clear()
        mKeys.clear()
        if(myOrderListener != null){
            val restaurantUid = activity!!.intent.extras.getString("restaurant_uid")
            val tableUid = activity!!.intent.extras.getString("table_uid")
            dbRef.child("RequestItems").child(restaurantUid).child(tableUid).removeEventListener(myOrderListener)
        }
    }

    private fun updateList(){
        val restaurantUid = activity!!.intent.extras.getString("restaurant_uid")
        val tableUid = activity!!.intent.extras.getString("table_uid")

        dbRef.child("RequestItems").child(restaurantUid).child(tableUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(!p0!!.exists()){
                    empty_view.showEmpty()
                }
            }
        })

        myOrderListener = object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                myOrder.add(p0!!.getValue(RequestModel::class.java)!!)
                var key = p0!!.key
                mKeys.add(key)
                adapter!!.notifyDataSetChanged()
                updateTotalPrice()
                empty_view.showContent()
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                var value = p0!!.getValue(RequestModel::class.java)!!
                var key = p0!!.key
                var index = mKeys.indexOf(key)
                myOrder[index] = value
                adapter!!.notifyItemChanged(index)
                updateTotalPrice()
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                var key = p0!!.key
                var index = mKeys.indexOf(key)
                mKeys.removeAt(index)
                myOrder.removeAt(index)
                adapter!!.notifyItemRemoved(index)
                updateTotalPrice()
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(context,"Error while fetch data.",Toast.LENGTH_SHORT).show()
            }
        }
        dbRef.child("RequestItems").child(restaurantUid).child(tableUid).addChildEventListener(myOrderListener)

    }

    private fun updateTotalPrice(){
        totalPrice = 0.00
        for(order in myOrder){
            if(order.status != "ยกเลิก"){
                totalPrice += order.total
            }
        }
        if(myOrder.size == 0){
            empty_view.showEmpty()
        }
    }

    private fun checkOut(){
        val restaurantUid = activity!!.intent.extras.getString("restaurant_uid")
        val tableUid = activity!!.intent.extras.getString("table_uid")

        val builder = MaterialDialog.Builder(context!!)
                .title("ชำระเงิน")
                .content("จำนวนเงิน "+this.resources.getString(R.string.price_format,totalPrice))
                .positiveText("ตกลง")
                .negativeText("ยกเลิก")
                .canceledOnTouchOutside(false)
                .onPositive(object : MaterialDialog.SingleButtonCallback{
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        dbRef.child("RequestTable").child(restaurantUid).child(tableUid).removeValue()
                        dbRef.child("RequestItems").child(restaurantUid).child(tableUid).removeValue()
                                .addOnCompleteListener {
                                    Toast.makeText(context,"ชำระเงินแล้ว",Toast.LENGTH_LONG).show()
                                }
                    }
                })
        val dialog = builder.build()
        dialog.show()
    }

}
