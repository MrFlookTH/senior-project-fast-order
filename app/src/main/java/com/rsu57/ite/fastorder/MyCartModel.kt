package com.rsu57.ite.fastorder

/**
 * Created by Flook on 2018-01-10.
 */
class MyCartModel(val uid:String, val name:String, val price:Double, val qty:Int, val total:Double) {
    constructor():this("","",0.0,0,0.0)
}