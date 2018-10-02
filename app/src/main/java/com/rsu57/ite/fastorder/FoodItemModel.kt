package com.rsu57.ite.fastorder

/**
 * Created by Flook on 2018-01-03.
 */
class FoodItemModel(val uid:String,val category_uid:String,val name:String, val description:String, val price:Double, val rating:Int, val image_path:String) {
    constructor():this("","","","",0.0,0,"")
}