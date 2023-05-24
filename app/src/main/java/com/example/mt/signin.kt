package com.example.mt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signup.*

class signin : AppCompatActivity() {
    private lateinit var firebaseauth: FirebaseAuth
    private lateinit var emailEt: TextInputEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        signuplink.setOnClickListener(){
            val intent= Intent(this, signup::class.java)
            startActivity(intent)
        }

        signinbutton.setOnClickListener(){
            emailEt = findViewById(R.id.signinEmail)
            val emailOrStudentNumber = emailEt.text.toString()
            val password = password.text.toString()

            if (emailOrStudentNumber.isNotEmpty() && password.isNotEmpty()) {
                firebaseauth = FirebaseAuth.getInstance()

                // Check if the input is an email or student number
                val loginTask = if (emailOrStudentNumber.contains('@')) {
                    // Input is an email
                    firebaseauth.signInWithEmailAndPassword(emailOrStudentNumber, password)
                } else {
                    // Input is a student number
                    // Retrieve the email associated with the student number from Firestore
                    val db = FirebaseFirestore.getInstance()
                    val usersCollection = db.collection("users")
                    val query = usersCollection.whereEqualTo("studentNumber", emailOrStudentNumber)
                    query.get().addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val user = documents.first()
                            val userEmail = user.getString("email")
                            // Sign in with the retrieved email and password
                            if (userEmail != null) {
                                firebaseauth.signInWithEmailAndPassword(userEmail, password)
                            } else {
                                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to retrieve user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                // Sign in with the provided credentials
                loginTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val landingintent = Intent(this, MainActivity::class.java)
                        startActivity(landingintent)
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        firebaseauth= FirebaseAuth.getInstance()
        if(firebaseauth.currentUser!=null){
            intent = Intent(this, com.example.mt.Moodhistory::class.java)
            startActivity(intent);
        }
    }
}