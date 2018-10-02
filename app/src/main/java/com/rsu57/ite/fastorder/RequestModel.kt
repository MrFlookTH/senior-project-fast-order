package com.rsu57.ite.fastorder



/**
 * Created by Flook on 2018-01-11.
 */
class RequestModel(val request_uid:String,val item_uid:String, val name:String, val price:Double, val qty:Int, val total:Double, val timeStamp:Any?, val status:String, val user_uid:String) {
    constructor():this("","","",0.0,0,0.0,null,"","")

}