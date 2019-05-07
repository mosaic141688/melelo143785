package com.melelo.melelo

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.*
import android.nfc.NdefRecord.createMime
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_write_nfc.*
import org.json.JSONArray
import java.io.IOException
import java.util.prefs.PreferenceChangeListener

class WriteNFCActivity : AppCompatActivity(), NfcAdapter.CreateNdefMessageCallback, DeleteContact {

    private var nfcAdapter: NfcAdapter? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val myDataset = arrayListOf<Contact>()


    override fun onDelete(possition: Int) {
        myDataset.removeAt(possition)
        viewAdapter.notifyDataSetChanged()

        val arr =JSONArray()

        for(num in myDataset){
            arr.put(num.number)
        }
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putString("text_numbers",arr.toString())
            .apply()


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_nfc)
        setSupportActionBar(toolbar)

        val arrString = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("text_numbers","[]")

        val arr =JSONArray(arrString)

        PreferenceChangeListener{
            Log.e("Preference Change",it.key)
        }



        call_number_tv.text = PreferenceManager.getDefaultSharedPreferences(this).getString("call_number","")

        requestPermission(Manifest.permission.SEND_SMS,10003)


        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdatpter(myDataset,this)

        edit_call_number_btn.setOnClickListener {
            val builder:AlertDialog.Builder = let {
                AlertDialog.Builder(it)
            }

            val ly: View = layoutInflater.inflate(R.layout.call_number_dialog_ly,null)

            builder.setView(ly)
                .setPositiveButton("SAVE",  DialogInterface.OnClickListener { dialog, id ->
                    val call_number =  ly.findViewById<EditText>(R.id.call_number_edit_text).text.toString()
                    call_number_tv.text = call_number
                    PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putString("call_number",call_number)
                        .apply()
                })
                .setTitle("Enter Number to Call")
                .create()
                .show()
        }

        for (i in 0..arr.length()-1){
            myDataset.add(Contact(arr.getString(i)!!,false,false))
        }


        add_text_number_btn.setOnClickListener {
            val builder:AlertDialog.Builder = let {
                AlertDialog.Builder(it)
            }

            val ly: View = layoutInflater.inflate(R.layout.call_number_dialog_ly,null)

            builder.setView(ly)
                .setPositiveButton("SAVE",  DialogInterface.OnClickListener { dialog, id ->
                    val number =  ly.findViewById<EditText>(R.id.call_number_edit_text).text.toString()
                    myDataset.add(Contact(number,false,false))
                    viewAdapter.notifyDataSetChanged()
                    val arr = JSONArray()
                    for(num in myDataset){
                        arr.put(num.number)
                    }
                    PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putString("text_numbers",arr.toString())
                        .apply()
                })
                .setTitle("Add Another Number")
                .create()
                .show()
        }

        recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Aproach an NFC TAG", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            if (nfcAdapter == null) {
                Log.e("NFC","Might be null")
                Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
            }else{
                nfcAdapter?.let{
                    enableNFCInForeground(it,this,javaClass)
                }
                nfcAdapter?.setNdefPushMessageCallback(this, this)
                Log.e("NFC","Might not be null")
            }
            // Register callback

        }



    }


    fun <T> enableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity, classType : Class<T>) {
        Log.e("Enable","Foreground")
        val pendingIntent = PendingIntent.getActivity(activity, 0,
            Intent(activity,classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        val nfcIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val filters = arrayOf(nfcIntentFilter)

        val TechLists = arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, TechLists)
        Log.e("Done","Enable Foreground")
    }

    fun disableNFCInForeground(nfcAdapter: NfcAdapter,activity: Activity) {
        nfcAdapter.disableForegroundDispatch(activity)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("Discovered ", "NFC")
        val ndefMessage = NdefMessage(
            arrayOf(
                createMime("application/vnd.com.melelo.melelo", "999".toByteArray())
               // NdefRecord.createApplicationRecord("com.melelo.melelo")
            )
        )
        intent?.let {
            val tag = it.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            val writen = writeMessageToTag(ndefMessage, tag)
            if (writen) {
                Toast.makeText(this,"Written to Nfc",Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this,"Failed to write to Nfc",Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onPause() {
        nfcAdapter?.let {
            disableNFCInForeground(nfcAdapter!!,this)
        }
        super.onPause()
    }


    override fun createNdefMessage(event: NfcEvent): NdefMessage {
        Log.e("NDF","Received event")
        return NdefMessage(
            arrayOf(
                NdefRecord.createApplicationRecord("com.melelo.melelo.MainActivity")
            )
        )
    }

    override fun onResume() {
        super.onResume()
        Log.e("ON","Resume")
        // Check to see that the Activity started due to an Android Beam
        nfcAdapter?.let{
            enableNFCInForeground(it,this,javaClass)
        }

        Log.e("ACTION",intent.action)

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
          Log.e("Started","From NFC")
        }
    }

    private fun writeMessageToTag(nfcMessage: NdefMessage, tag: Tag?): Boolean {

        try {
            val nDefTag = Ndef.get(tag)

            nDefTag?.let {
                it.connect()
                if (it.maxSize < nfcMessage.toByteArray().size) {
                    //Message to large to write to NFC tag
                    return false
                }
                if (it.isWritable) {
                    it.writeNdefMessage(nfcMessage)
                    it.close()
                    //Message is written to tag
                    return true
                } else {
                    //NFC tag is read-only
                    return false
                }
            }

            val nDefFormatableTag = NdefFormatable.get(tag)

            nDefFormatableTag?.let {
                try {
                    it.connect()
                    it.format(nfcMessage)
                    it.close()
                    //The data is written to the tag
                    return true
                } catch (e: IOException) {
                    //Failed to format tag
                    return false
                }
            }
            //NDEF is not supported
            return false

        } catch (e: Exception) {
            //Write operation has failed
        }
        return false
    }

    fun requestPermission(permission:String,enumerated:Int){
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permission
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    enumerated
                )
            }
        } else {

            // Permission has already been granted
        }

    }

}

