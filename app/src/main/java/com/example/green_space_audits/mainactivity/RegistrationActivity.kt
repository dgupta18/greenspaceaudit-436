package com.example.green_space_audits.mainactivity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference



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



                    val user = User(name, email, password, false, 0, ArrayList(), ArrayList(), ArrayList(), HashMap(), mutableMapOf<String,Comment>())
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(mAuth!!.getCurrentUser()!!.getUid())
                        .setValue(user)





                    val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(applicationContext, "Registration failed! Please try again later", Toast.LENGTH_LONG).show()
                    progressBar!!.visibility = View.GONE
                }
            }
    }









}