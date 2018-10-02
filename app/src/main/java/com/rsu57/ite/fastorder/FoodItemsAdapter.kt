package com.rsu57.ite.fastorder

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

/**
 * Created by Flook on 2018-01-03.
 */
class FoodItemsAdapter(private val foodItemList:ArrayList<FoodItemModel>, val mContext : Context): RecyclerView.Adapter<FoodItemsAdapter.ViewHolder>() {

    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference
    private var dbAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FoodItemsAdapter.ViewHolder {
        val v = LayoutInflater.from(parent!!.context).inflate(R.layout.row_food_item, parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val foodItem : FoodItemModel = foodItemList[position]
        holder?.textViewName?.text = foodItem.name
        holder?.textViewDesc?.text = foodItem.description
        holder?.textViewPrice?.text = mContext.resources.getString(R.string.price_format,foodItem.price)
        holder?.ratingBar?.rating = foodItem.rating.toFloat()
        Picasso.with(mContext)
                .load(foodItem.image_path)
                .placeholder(R.drawable.img_placeholder)
                .resize(100, 100)
                .centerInside()
                .into(holder?.imageViewFoodItem)


        holder?.itemView?.setOnClickListener{
            MaterialDialog.Builder(mContext)
                    .title(foodItem.name)
                    .content("กรุณาระบุจำนวนที่ต้องการ")
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .buttonsGravity(GravityEnum.CENTER)
                    .inputRangeRes(1, 2, R.color.Red)
                    .input("จำนวน", "", MaterialDialog.InputCallback { dialog, input ->
                        addToMyCart(foodItem, input.toString())
                    }).show()
            true
        }

    }

    override fun getItemCount(): Int {
        return foodItemList.size
    }


    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val imageViewFoodItem = itemView.findViewById<ImageView>(R.id.ivFoodItem)
        val textViewName = itemView.findViewById<TextView>(R.id.tvFoodItemName)
        val textViewDesc = itemView.findViewById<TextView>(R.id.tvFoodItemDescription)
        val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
        val textViewPrice = itemView.findViewById<TextView>(R.id.tvFoodItemPrice)
    }

    private fun addToMyCart(fooditem:FoodItemModel, qty:String){
        val currentUser = dbAuth.currentUser!!.uid
        var total = 0.0
        dbRef.child("UserCart").child(currentUser).child(fooditem.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()){
                    val item = p0.getValue(MyCartModel::class.java)
                    var old_qty = item!!.qty
                    var old_total = item!!.total
                    total = old_total + (fooditem.price * qty.toInt())
                    dbRef.child("UserCart").child(currentUser).child(fooditem.uid).setValue(MyCartModel(fooditem.uid,fooditem.name,fooditem.price,old_qty+qty.toInt(),total))
                            .addOnCompleteListener {
                                Toast.makeText(mContext,"อัพเดตตระกร้าสินค้าแล้ว",Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(mContext,"Error",Toast.LENGTH_SHORT).show()
                            }
                }else{
                    total = fooditem.price * qty.toInt()
                    dbRef.child("UserCart").child(currentUser).child(fooditem.uid).setValue(MyCartModel(fooditem.uid,fooditem.name,fooditem.price,qty.toInt(),total))
                            .addOnCompleteListener {
                                Toast.makeText(mContext,"เพิ่มลงตระกร้าสินค้าแล้ว",Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(mContext,"Error",Toast.LENGTH_SHORT).show()
                            }
                }
            }

        })
    }




}