package com.example.green_space_audits.mainactivity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var userEmail: EditText? = null
    private var userPassword: EditText? = null
    private var loginBtn: Button? = null
    private var progressBar: ProgressBar? = null
    private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userEmail = findViewById(R.id.email)
        userPassword = findViewById(R.id.password)

        loginBtn = findViewById(R.id.login)
        progressBar = findViewById(R.id.progressBar)

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")
        mAuth = FirebaseAuth.getInstance()



        loginBtn!!.setOnClickListener { loginUserAccount() }

    }

    private fun loginUserAccount() {
        progressBar!!.visibility = View.VISIBLE

        val tempemail = userEmail!!.text.toString()
        val temppass = userPassword!!.text.toString()

        if (TextUtils.isEmpty(tempemail)) {
            Toast.makeText(applicationContext, "Please enter email...", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(temppass)) {
            Toast.makeText(applicationContext, "Please enter password!", Toast.LENGTH_LONG).show()
            return
        }

        mAuth!!.signInWithEmailAndPassword(tempemail,temppass)
            .addOnCompleteListener { task ->
                progressBar!!.visibility = View.GONE
                if(task.isSuccessful){
                    Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_LONG)
                        .show()
                    val uid = mAuth?.uid
                    Log.d("Test", "Test")
//                    val enter = Intent(this@LoginActivity, MapsActivity::class.java)
                    val enter = Intent(this@LoginActivity, AddGreenSpaceActivity::class.java)
                    startActivity(enter)

                }else{
                    Toast.makeText(
                        applicationContext,
                        "Login failed! Please try again later",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }



    }





}