package com.example.mt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_signup.*
import java.lang.Exception

class signup : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        signupbutton.setOnClickListener(){
            val studentid = studentnumber.text.toString()
            val studentname = name.text.toString()
            val email = signinEmail.text.toString()
            val password = passET.text.toString()
            val confirmpassword = confirmPassEt.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmpassword.isNotEmpty() && studentid.isNotEmpty() && studentname.isNotEmpty()) {
                if (password == confirmpassword) {
                    firebaseAuth = FirebaseAuth.getInstance()
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = firebaseAuth.currentUser?.uid
                            val userData = HashMap<String, Any>()
                            userData["email"] = email
                            userData["studentName"] = studentname
                            userData["studentId"] = studentid

                            // Save user data to Firestore or Realtime Database
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(userId!!)
                                .set(userData)
                                .addOnSuccessListener {
                                    // User data saved successfully
                                    val intent = Intent(this, signinEmail::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener { exception ->
                                    // Failed to save user data
                                    Toast.makeText(this, "Failed to save user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }

                            Toast.makeText(this, "Signup Succesful", Toast.LENGTH_SHORT).show()
                            val intent= Intent(this, MainActivity::class.java)
                            startActivity(intent)

                        } else {
                            Log.i("error", Exception().message.toString())
                            Toast.makeText(this, "Unable to sign you up at this Time, Please try again Later", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password and Confirm Password must match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        signinlink.setOnClickListener(){
            val intent= Intent(this, signinEmail::class.java)
            startActivity(intent)
        }
    }
}
