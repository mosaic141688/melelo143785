package com.melelo.melelo

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.location.Criteria
import android.location.LocationListener
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.telecom.TelecomManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.LocationSource
import java.util.jar.Manifest


val MY_PERMISSIONS_REQUEST_LOCATION = 10001
val MY_PERMISSIONS_REQUEST_CALL = 10002


class MainActivity : AppCompatActivity(),LocationListener {

    private lateinit var location: Location
    private  var trustedNumbers:List<String> = arrayListOf()
    private var emegencyNumber = "76754005"

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                } else {

                }
                return
            }

            else -> {

            }
        }
    }


    override fun onLocationChanged(_location: Location) {
        location = _location
    }

    override fun onProviderDisabled(provider: String?) {
        Log.e("Provider","Enabled")
    }

    override fun onProviderEnabled(provider: String?) {
        Log.e("Provider","Enabled")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.e("Provider","Changed")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, MY_PERMISSIONS_REQUEST_LOCATION)
        requestPermission(android.Manifest.permission.CALL_PHONE, MY_PERMISSIONS_REQUEST_CALL)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = locationManager
            .isProviderEnabled(LocationManager.GPS_PROVIDER)

// check if enabled and if not send user to the GSP settings
// Better solution would be to display a dialog and suggesting to
// go to the settings
        if (!enabled) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, false)

        try {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100L,100F,this)
            location?.let {
               // makeACall()
                sendAnSMS()
            }
        }catch (e:SecurityException ){
            Log.e("Error",e.toString())
            Toast.makeText(this,"You need the location Permission to use the app",Toast.LENGTH_LONG).show()
        }

    }

    fun makeACall() {
        val uri = Uri.fromParts("tel", emegencyNumber , null)
        val extras = Bundle()
        extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        try {
            telecomManager.placeCall(uri, extras)
        }catch (e:SecurityException){
            Toast.makeText(this,"Need Call Permission to make call",Toast.LENGTH_LONG).show()
        }

    }

    fun sendAnSMS() {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(emegencyNumber, null, "Please help I am at https://maps.google.com/maps?q=${location.latitude},${location.longitude}", null, null)
        trustedNumbers.forEach{
            val sendIntent = Intent(Intent.ACTION_VIEW)
            sendIntent.putExtra("Please help I am at https://maps.google.com/maps?q=${location}", "default content")
            sendIntent.type = "vnd.android-dir/mms-sms"
            startActivity(sendIntent)
        }
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
