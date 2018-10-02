package com.rsu57.ite.fastorder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by Flook on 2018-01-11.
 */
class MyCartAdapter(private val foodItemList:ArrayList<MyCartModel>, val mContext : Context): RecyclerView.Adapter<MyCartAdapter.ViewHolder>() {

    private var dbAuth = FirebaseAuth.getInstance()
    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val foodItem : MyCartModel = foodItemList[position]

        holder?.textViewName?.text = foodItem.name
        holder?.textViewQuantity?.text = mContext.resources.getString(R.string.quantity_format,foodItem.qty)
        holder?.textViewPrice?.text = mContext.resources.getString(R.string.price_format,foodItem.total)

        holder?.itemView?.setOnClickListener{
            MaterialDialog.Builder(mContext)
                    .title(foodItem.name)
                    .content("กรุณาระบุจำนวนที่ต้องการ")
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .inputRangeRes(1, 2, R.color.FireBrick)
                    .input("จำนวน", foodItem.qty.toString(), MaterialDialog.InputCallback { dialog, input ->
                        val qty = input.toString().toInt()
                        if(qty > 0){
                            updateOrder(foodItem,input.toString())
                        }else{
                            removeOrder(foodItem.uid)
                        }
                    }).show()
            true
        }

        holder?.itemView?.setOnLongClickListener{
            val builder = MaterialDialog.Builder(mContext)
                    .title("ลบรายการอาหาร")
                    .content("ต้องการลบ \""+foodItem.name+"\" หรือไม่?")
                    .positiveText("ลบ")
                    .negativeText("ยกเลิก")
                    .canceledOnTouchOutside(false)
                    .onPositive(object : MaterialDialog.SingleButtonCallback{
                        override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                            if(foodItem.uid != ""){
                                removeOrder(foodItem.uid)
                            }else{
                                Toast.makeText(mContext,"UID not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
            val dialog = builder.build()
            dialog.show()
            true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent!!.context).inflate(R.layout.row_cart_item, parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return foodItemList.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val textViewName = itemView.findViewById<TextView>(R.id.tvFoodItemName)
        val textViewQuantity = itemView.findViewById<TextView>(R.id.tvQuantity)
        val textViewPrice = itemView.findViewById<TextView>(R.id.tvFoodItemPrice)
    }

    private fun updateOrder(foodItem : MyCartModel, qty : String){
        val currentUser = dbAuth.currentUser!!.uid
        val total = foodItem.price * qty.toInt()
        dbRef.child("UserCart").child(currentUser).child(foodItem.uid).setValue(MyCartModel(foodItem.uid,foodItem.name,foodItem.price,qty.toInt(),total))
                .addOnCompleteListener {
                    Toast.makeText(mContext,"อัพเดตข้อมูลแล้ว",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(mContext,"Error",Toast.LENGTH_SHORT).show()
                }
    }

    private fun removeOrder(foodUid:String){
        val currentUser = dbAuth.currentUser!!.uid
        dbRef.child("UserCart").child(currentUser).child(foodUid).removeValue()
                .addOnCompleteListener {
                    Toast.makeText(mContext,"ลบรายการแล้ว",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(mContext,"Error",Toast.LENGTH_SHORT).show()
                }
    }

}