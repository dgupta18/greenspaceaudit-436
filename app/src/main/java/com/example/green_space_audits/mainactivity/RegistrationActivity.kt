package com.example.green_space_audits.mainactivity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException


class RegistrationActivity : AppCompatActivity() {

    private var emailTV: EditText? = null
    private var nameTV: EditText? = null
    private var passwordTV: EditText? = null
    private var regBtn: Button? = null
    private var progressBar: ProgressBar? = null
   private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        nameTV = findViewById(R.id.name)
        emailTV = findViewById(R.id.email)
        passwordTV = findViewById(R.id.password)
        regBtn = findViewById(R.id.register)
        progressBar = findViewById(R.id.progressBar)
        mAuth = FirebaseAuth.getInstance()

        regBtn!!.setOnClickListener {
            registerNewUser()
        }

//        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }
    private fun registerNewUser() {
        progressBar!!.visibility = View.VISIBLE
        val name: String
        val email: String
        val password: String
        name = nameTV!!.text.toString()
        email = emailTV!!.text.toString()
        password = passwordTV!!.text.toString()

        if(TextUtils.isEmpty(name)){
            Toast.makeText(applicationContext, "Please enter name. please...", Toast.LENGTH_LONG).show()
            return
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(applicationContext, "Please enter email...", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Please enter password!", Toast.LENGTH_LONG).show()
            return
        }

        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()
                    progressBar!!.visibility = View.GONE


//                    val userBadges = mutableListOf<String>("badge0", "badge1", "badge2", "badge3", "badge4")
//                    val userFavorites = mutableMapOf<String,String>("-LukAbc95Yp8UHnEIyVq" to "Green Space", "-LukBGCqBVsn8R3SxJA2" to "Hello", "-LuzHNULfgRhBeBuWSKd" to "The Quad")
//                    Log.i("userBadges: ", userBadges.toString())
//                    Log.i("userFavorites: ", userFavorites.toString())

//                    val user = User(name, email, password, false, 0, ArrayList(), userBadges, userFavorites, mutableMapOf<String, ArrayList<String>>(), mutableMapOf<String,Comment>())
                    val user = User(name, email, password, false, 0, ArrayList(), mutableListOf<String>(), mutableMapOf<String,String>(), mutableMapOf<String, ArrayList<String>>(), mutableMapOf<String,Comment>())
                    Log.i("USER: ", user.toString())

                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(mAuth!!.currentUser!!.uid)
                        .setValue(user)


                    val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    val e: Exception
                    try {
                        e = task.exception as FirebaseAuthWeakPasswordException
                        Log.i("RegistrationActivity", "registration failed: ", e)
                        Toast.makeText(applicationContext, "Password must be at least 6 characters long.", Toast.LENGTH_LONG).show()
                        progressBar!!.visibility = View.GONE
                    } catch(ex: Exception) {
                        Toast.makeText(applicationContext, "Registration failed! Please try again later", Toast.LENGTH_LONG).show()
                        progressBar!!.visibility = View.GONE
                    }

                }
            }
    }









}