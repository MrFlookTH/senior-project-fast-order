package com.rsu57.ite.fastorder

/**
 * Created by Flook on 2017-12-04.
 */


class RestaurantModel(val uid:String,val name:String, val description:String, val logoPath:String, val bannerPath:String){
    constructor():this("","","","","")
}