package com.rsu57.ite.fastorderforrestaurant

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.google.firebase.database.FirebaseDatabase
import com.rsu57.ite.fastorder.FoodItemsActivity
import com.rsu57.ite.fastorder.R


/**
 * Created by Flook on 2018-01-02.
 */
class CategoryAdapter(val cList:ArrayList<CategoryModel>, val mContext:Context, val restaurantUID:String): RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var db = FirebaseDatabase.getInstance()

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val category : CategoryModel = cList[position]
        holder?.textViewName?.text = category.name
        Picasso.with(mContext)
                .load(category.image_path)
                .resize(160, 160)
                .placeholder(R.drawable.img_placeholder)
                .centerInside()
                .into(holder?.imageViewCategory)


        holder?.itemView?.setOnClickListener{
            if(category.uid != ""){
                mContext.startActivity(Intent(mContext, FoodItemsActivity::class.java)
                        .putExtra("category_uid",category.uid)
                        .putExtra("restaurant_uid", restaurantUID))
            }else{
                Toast.makeText(mContext,"UID not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent!!.context).inflate(R.layout.row_category, parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return cList.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val imageViewCategory = itemView.findViewById<ImageView>(R.id.ivCategory)
        val textViewName = itemView.findViewById<TextView>(R.id.tvCategoryName)
    }

}