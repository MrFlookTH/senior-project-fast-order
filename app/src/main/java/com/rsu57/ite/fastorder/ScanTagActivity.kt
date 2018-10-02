package com.rsu57.ite.fastorder

import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_scan_tag.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import com.afollestad.materialdialogs.MaterialDialog
import com.squareup.picasso.Picasso


class ScanTagActivity : AppCompatActivity(), View.OnClickListener {

    private var dbAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private var db = FirebaseDatabase.getInstance()
    private var dbRef = db.reference

    //NFC Module
    var nfcAdapter : NfcAdapter? = null
    var pendingIntent: PendingIntent? = null
    var readTagFilters: Array<IntentFilter>? = null
    var myTag: Tag? = null
    var readMode : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_tag)
        btnLogout.setOnClickListener(this)
        btnBypass.setOnClickListener(this)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this,"สมาร์ทโฟนของคุณไม่รองรับ NFC", Toast.LENGTH_SHORT).show()
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
            val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
            tagDetected.addCategory(Intent.CATEGORY_DEFAULT)
            readTagFilters = arrayOf(tagDetected)
            readMode = true
        }
    }

    override fun onClick(p0: View?) {
        when(p0){
            btnLogout ->{
                alert("ต้องการออกจากระบบ?"){
                    yesButton { logOut() }
                    noButton {  }
                }.show()
            }
            btnBypass ->{
                loadMain("ljneEuqBYWODHD3KivNSIdsSkVw1","-L2-ZCo0Vvy_IZ65vBFq")
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        disableForegroundDispatch()
    }

    public override fun onResume() {
        super.onResume()
        enableForegroundDispatch()
    }

    public override fun onNewIntent(intent: Intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent!!.action) {
            if(readMode){
                myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                getDataFromTag(myTag!!, intent)
            }
        }
    }


    private fun loadMain(restaurantUid : String, tableUid : String){
        var intent = Intent(this,MainActivity::class.java)
        intent.putExtra("restaurant_uid",restaurantUid)
        intent.putExtra("table_uid",tableUid)
        startActivity(intent)
        finish()
    }

    private fun logOut(){
        var pDialog = ProgressDialog(this)
        pDialog.setCancelable(false)
        pDialog.setCanceledOnTouchOutside(false)
        pDialog.setTitle("กำลังออกจากระบบ")
        pDialog.show()
        dbAuth!!.signOut()
        pDialog.dismiss()
        var intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun enableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, readTagFilters, null)
        }
    }

    private fun disableForegroundDispatch() {
        if (nfcAdapter != null) {
            nfcAdapter!!.disableForegroundDispatch(this)
        }
    }

    private fun getDataFromTag(tag: Tag, intent: Intent) {
        val currentUser = dbAuth.currentUser!!.uid
        val ndef = Ndef.get(tag)
        try {
            ndef.connect()
            val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (messages != null) {
                val ndefMessages = arrayOfNulls<NdefMessage>(messages.size)
                for (i in messages.indices) {
                    ndefMessages[i] = messages[i] as NdefMessage
                }

                var payload = ndefMessages[0]!!.records[0].payload
                var text = String(payload)
                val restaurantUid = text.substring(3, text.length)

                payload = ndefMessages[0]!!.records[1].payload
                text = String(payload)
                val tableUid = text.substring(3, text.length)

                //Check available
                dbRef.child("Tables").child(restaurantUid).child(tableUid).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {
                        Toast.makeText(applicationContext,"Error",Toast.LENGTH_LONG).show()
                    }
                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0!!.hasChild("customer")){
                            //Check is user's table
                            dbRef.child("Tables").child(restaurantUid).child(tableUid).child("customer").addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onCancelled(p0: DatabaseError?) {}
                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0!!.getValue(String::class.java) == currentUser){
                                        loadMain(restaurantUid,tableUid)
                                    }else{
                                        Toast.makeText(applicationContext,"มีลูกค้าท่านอื่นกำลังใช้บริการอยู่" ,Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })
                        }else{
                            dbRef.child("Tables").child(restaurantUid).child(tableUid).child("customer").setValue(currentUser)
                            loadMain(restaurantUid,tableUid)
                        }
                    }
                })

            }
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot Read From Tag.", Toast.LENGTH_LONG).show()
        }finally {
            ndef.close()
        }

    }


}
