package com.rsu57.ite.fastorder

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by Flook on 2018-01-12.
 */
class MyOrderAdapter(private val orderList:ArrayList<RequestModel>, private val mContext : Context, private val restaurantUID:String, private val tableUID:String): RecyclerView.Adapter<MyOrderAdapter.ViewHolder>() {
    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val order : RequestModel = orderList[position]

        holder?.textViewName?.text = order.name
        holder?.textViewQuantity?.text = order.qty.toString()
        holder?.textViewPrice?.text = mContext.resources.getString(R.string.price_format,order.price)
        holder?.textViewTotal?.text = mContext.resources.getString(R.string.price_format,order.total)
        when(order.status) {
            "กำลังรอ" -> holder?.textViewStatus?.setTextColor(ContextCompat.getColor(mContext,R.color.DimGray))
            "กำลังปรุง" -> holder?.textViewStatus?.setTextColor(ContextCompat.getColor(mContext,R.color.DarkOrange))
            "เสิร์ฟแล้ว" -> holder?.textViewStatus?.setTextColor(ContextCompat.getColor(mContext,R.color.DarkGreen))
            "ยกเลิก" -> holder?.textViewStatus?.setTextColor(ContextCompat.getColor(mContext,R.color.FireBrick))
        }
        holder?.textViewStatus?.text = order.status

        holder?.itemView?.setOnLongClickListener {
            if(order.status == "กำลังรอ"){
                MaterialDialog.Builder(mContext)
                        .title("ยกเลิกรายการ")
                        .content("ต้องการยกเลิกรายการใช่ไหม?")
                        .positiveText("ใช่")
                        .negativeText("ไม่")
                        .onPositive { dialog, which ->
                            dbRef.child("RequestItems").child(restaurantUID).child(tableUID).child(order.request_uid).child("status").setValue("ยกเลิก")
                                    .addOnCompleteListener {
                                        Toast.makeText(mContext,"ยกเลิกรายการแล้ว",Toast.LENGTH_SHORT).show()
                                    }
                        }
                        .show()
            }else{
                Toast.makeText(mContext,"ไม่สามารถยกเลิกได้",Toast.LENGTH_SHORT).show()
            }
            true
        }


    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyOrderAdapter.ViewHolder {
        val v = LayoutInflater.from(parent!!.context).inflate(R.layout.row_order, parent,false)
        return MyOrderAdapter.ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val textViewName = itemView.findViewById<TextView>(R.id.tvFoodItemName)
        val textViewPrice = itemView.findViewById<TextView>(R.id.tvFoodItemPrice)
        val textViewQuantity = itemView.findViewById<TextView>(R.id.tvFoodItemQty)
        val textViewTotal = itemView.findViewById<TextView>(R.id.tvFoodTotalPrice)
        val textViewStatus = itemView.findViewById<TextView>(R.id.tvStatus)
    }
}