package com.example.mt

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    // TODO: darkmode, consecutive days, landscape
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var place: String =""
    private var db:FirebaseFirestore = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val email = user!!.email
    private var quote:String =""
    private val REQUEST_PHONE_CALL = 1
    private val phonenumber= "+448088002234"


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        get and setfullname from firebase
            db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val query = usersCollection.whereEqualTo("email", email)
        query.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val user = documents.first()
                val fullName = user.getString("studentName")

                if (fullName != null) {
                    val firstName = fullName.split(" ")[0]
                    loggedinuser.text = "Hello $firstName"
                } else {
                    loggedinuser.text = "Hello"
                }
            } else {
                loggedinuser.text = "Hello"
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to retrieve user data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }

//        log new mood funtion
        addemotion()

        //emergency button

        sos.setOnClickListener(){
            makesos()
        }
//EMERGENCY CALL BTN
        dialhelp.setOnClickListener(){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startcall()
            } else {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
            }

        }


//   -----------------------location permissions and coods ----------------------

// Initialize the Fused location variable
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!isLocationEnabled()) {
            Toast.makeText(this,
                "Your location provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()

            // direct user to location settings.
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withActivity(this).withPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(this@MainActivity, "Please Enable Location to use the Application", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread() // tells the device to check all the permissions attached in the thread
                .check()
        }
        getLocationWeatherDetails()


    }

//send sos message using whatsapp
    fun makesos() {
//            Log.i("sos", "SoS")
        place = getPlaceNameFromCoordinates( latitude, longitude)
        val usersCollection = db.collection("users")
        val query = usersCollection.whereEqualTo("email", email)
        query.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val user = documents.first()
//                Log.i("user", user.toString())
                val fullName = user.getString("studentName").toString()
                val message = "Hello, my name is $fullName. If you are receiving this message, I need immediate mental health support.\n" +
                        "I'm currently at $place. My exact coordinates are Longitude: $longitude and Latitude: $latitude. Please help me."
                val phoneNumber = "+447860022821"
                Log.i("Message", "$fullName $place")
                val packageManager = this.packageManager
                val intent = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${URLEncoder.encode(message, "UTF-8")}"
                intent.`package` = "com.whatsapp"
                intent.data = Uri.parse(url)
                if (intent.resolveActivity(packageManager) != null) {
                    this.startActivity(intent)
                } else {
                    Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




 
//    mood log confirmation alert
    private  fun showAlertDialog(mood:String, img:Int){
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Confirm")
            alertDialogBuilder.setMessage("Are you sure you want to log mood as $mood?")
            alertDialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                // Code to execute when OK button is clicked
                val intent = Intent(this, Moodhistory::class.java)
                intent.putExtra("mood", mood)
                intent.putExtra("imageResId", img)
                startActivity(intent)
            })
            alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                          })

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }


    private fun addemotion() {
        var mood: String
        var img: Int

        //happy
        happyImg.setOnClickListener(){
            mood = "Happy"
            img = R.drawable.happy
            showAlertDialog(mood, img)
        }

        //Sad
        sadImg.setOnClickListener(){
            mood = "Sad"
            img = R.drawable.sad
            showAlertDialog(mood, img)
        }

        //Depressed
        depressedImg.setOnClickListener(){
            mood = "Depressed"
            img = R.drawable.sick
            showAlertDialog(mood, img)
        }

        //angry
        angryImg.setOnClickListener(){
            mood = "Angry"
            img = R.drawable.angry
            showAlertDialog(mood, img)
        }

        //grateful
        gratefulImg.setOnClickListener(){
            mood = "Grateful"
            img = R.drawable.grateful
            showAlertDialog(mood, img)
        }

        //cool
        coolImg.setOnClickListener(){
            mood = "Cool"
            img = R.drawable.cool
            showAlertDialog(mood, img)
        }


    }

    private fun startcall(){
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phonenumber")
        startActivity(callIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PHONE_CALL) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startcall()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_lightmodetoggle -> {
                // Handle the night mode toggle here
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            R.id.nav_darkmodetoggle -> {
                // Handle the night mode toggle here
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            R.id.nav_signout -> {
                firebaseAuth = FirebaseAuth.getInstance()
                firebaseAuth.signOut()
                val intent = Intent(this, signin::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }









    /**
     * A function which is used to verify that the location or GPS is enable or not of the user's device.
     */
    private fun isLocationEnabled(): Boolean {

        // This provides access to the system location services.
        val locationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * function used to show the alert dialog when the permissions are denied and need to allow it from settings app info.
     */
    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this Application. It can be enabled under Application Settings")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    /**
     *  function to request the current location. Using the fused location provider client.
     */
    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority = LocationRequest.QUALITY_HIGH_ACCURACY

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }


    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            latitude = mLastLocation?.latitude!!
//            Log.i("Current Latitude", "$latitude")

            longitude = mLastLocation?.longitude!!

//            Log.i("la/lom", "$latitude  $longitude")
            getLocationWeatherDetails()
        }
    }


    private fun getLocationWeatherDetails() {
        if (Constants.isNetworkAvailable(this)) {
            if (latitude != null && longitude != null) {
                CallAPILogicAsyncTask().execute()
            }
        } else {
            Toast.makeText(this, "No internet connection available.", Toast.LENGTH_SHORT).show()
        }
    }


    private inner class CallAPILogicAsyncTask(): AsyncTask<Any, Void, String>(){

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)


            val jsonObject = JSONObject(result)
            val weatherArray = jsonObject.getJSONArray("weather")
            val firstWeatherObject = weatherArray.getJSONObject(0)
            val mainValue = firstWeatherObject.optString("main")
            place = firstWeatherObject.optString("name")
//            Log.i("Message", mainValue)
            setMotivationalQuote(mainValue)
            motivation.setText(quote)
//            Log.i("JSON", jsonObject.toString())
        }

        override fun doInBackground(vararg p0: Any?): String {
            var result: String
            var connection: HttpURLConnection? = null

            try {

                val url = URL("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=0025b06808e8cf3f01053412cfdd16a8")
                connection = url.openConnection() as HttpURLConnection
                connection?.doInput = true
                connection?.doOutput = true

                val httpResult: Int = connection?.responseCode ?: -1

                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val stringBuilder = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                } else {
                    result = connection?.responseMessage ?: "Unknown error"
                }
            } catch (e: SocketTimeoutException) {
                result = "Error: ${e.message}"
            } finally {
                connection?.disconnect()
            }
            return result

        }

    }

    fun getPlaceNameFromCoordinates( latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        var placeName: String= ""

        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) as List<Address>
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                placeName = address.getAddressLine(0)

            }
        } catch (e: IOException) {
            Log.e("Geocoding", "Error: ${e.message}")
        }

        return placeName
    }


    @SuppressLint("SetTextI18n")
    fun setMotivationalQuote(weather: String) {
        when (weather) {
            "Thunderstorm" -> {
                quote = "In the midst of the storm, remember that your strength and resilience will carry you through to brighter days."
                motivation.text = quote
            }
            "Rain" -> {
                 quote = "Just like rain nourishes the earth, let the rain in your life cleanse your soul and wash away all doubts and fears."
                motivation.text = quote
            }
            "Snow" -> {
                 quote = "Embrace the beauty of snowflakes and find inspiration in their uniqueness. Let the snow remind you that each day is a fresh start."
                motivation.text = quote
            }
            "Clear" -> {
                quote = "On clear days, the sky is limitless, and so are your possibilities. Keep your vision clear and reach for the stars."
                motivation.text = quote
            }
            "Clouds" -> {
                quote = "Even behind the clouds, the sun still shines. Stay hopeful and trust that brighter days are just beyond the horizon."
                    motivation.text = quote
            }
            "Drizzle" -> {
                quote = "In the gentle drizzle, find solace and tranquility. Let it be a reminder that even the smallest steps can lead to significant progress."
                motivation.text = quote
            }

            "Mist" -> {
                quote = "Like a gentle mist that envelops the landscape, uncertainty can shroud our vision, yet it beckons us to seek inner clarity. Embrace the unfamiliar, for within its embrace lies a hidden path, revealing boundless possibilities and uncharted horizons."
                motivation.text = quote
            }
            else -> {
                motivation.text="Success is not final, failure is not fatal: It is the courage to continue that counts."
            }
        }
    }
}




