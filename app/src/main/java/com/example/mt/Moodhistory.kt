package com.example.mt

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.basic.programming.mygridlayoutapp.adapters.historyadapter
import com.basic.programming.mygridlayoutapp.model.mooddata
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_moodhistory.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Moodhistory : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val items: ArrayList<mooddata> = ArrayList()
    private val phonenumber= "+448088002234"
    private val REQUEST_PHONE_CALL = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moodhistory)

        val previouslySavedMoods = getSavedRecyclerViewData()
        val mood = intent.getStringExtra("mood")

        if (previouslySavedMoods != null) {
                        for (item in previouslySavedMoods.asReversed()) {
                if (item.icons != null && item.mood != null) {
                    items.add(0, mooddata(item.icons, item.mood)) // Add at index 0 to place it at the beginning
                }
            }

            recyclerView = findViewById(R.id.recyclerView)
            recyclerView.adapter = historyadapter(this, items)

            if(consecutivemoodcheck(items)){
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Depression Alert")
                alertDialogBuilder.setMessage("You have felt depressed seven time in a row, do you need help?")
                alertDialogBuilder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    // Check if the CALL_PHONE permission is already granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        startcall()
                    } else {
                        // Permission is not granted, request it
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
                    }

                })
                alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                    Toast.makeText(this, "Please Ensure you take care of your Mental Health", Toast.LENGTH_SHORT).show()
                })

                val alertDialog: AlertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }

        Log.i("items", items.toString())
        val moodImg = intent.getIntExtra("imageResId", 0)
        val currentDateAsString = getCurrentDateAsString()
        val moodLogText = "$mood on $currentDateAsString"

        if (mood != null) {
            val moodsForCurrentDate = getMoodsForCurrentDate(currentDateAsString)

            if (moodsForCurrentDate.size < 2) {
                recyclerView = findViewById(R.id.recyclerView)
                val adapter = recyclerView.adapter
                val newItem = mooddata(moodImg, moodLogText)
                items.add(0, newItem) // Add at index 0 to place it at the beginning
                recyclerView.adapter = historyadapter(this, items)

                if (adapter != null) {
                    adapter.notifyDataSetChanged()
                }

                saveRecyclerViewData(items)


            } else {
                Toast.makeText(this, "You can only add up to 2 moods per day.", Toast.LENGTH_SHORT).show()
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }


        // TODO:  darkmode, consecutive days,  orientation, custom message with location
        // for swimming add position to gala, messages,
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_mood_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addmood -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getCurrentDateAsString(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    private fun saveRecyclerViewData(data: List<mooddata>) {
        val gson = Gson()
        val json = gson.toJson(data)
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("RecyclerViewData", json)
        editor.apply()
    }

    private fun resetRecyclerView() {
        saveRecyclerViewData(ArrayList())
    }

    private fun getSavedRecyclerViewData(): List<mooddata>? {
        val gson = Gson()
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("RecyclerViewData", null)
        val type = object : TypeToken<List<mooddata>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun getMoodsForCurrentDate(currentDate: String): List<mooddata> {
        val moodsForCurrentDate = mutableListOf<mooddata>()
        for (item in items) {
            val moodDate = item.mood?.let { extractDateFromMoodLog(it) }
            if (moodDate == currentDate) {
                moodsForCurrentDate.add(item)
            }
        }
        return moodsForCurrentDate
    }

    private fun extractDateFromMoodLog(moodLog: String): String {
        // Assuming the moodLog has the format: "Mood on yyyy-MM-dd"
        val parts = moodLog.split(" ")
        return parts.lastOrNull() ?: ""
    }

    fun consecutivemoodcheck(moods: List<mooddata>): Boolean {
        if (moods.size < 7) {
            // If the array has less than three elements, return false
            return false
        }


        val firstsevenMoods = moods.subList(0, 6)
        return firstsevenMoods.all { it.mood?.startsWith("Depressed") == true }
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
    private fun startcall(){
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phonenumber")
        startActivity(callIntent)
    }
    }



