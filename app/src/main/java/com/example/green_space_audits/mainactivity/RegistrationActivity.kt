package com.example.green_space_audits.mainactivity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.google.firebase.auth.UserProfileChangeRequest


class RegistrationActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private var emailTV: EditText? = null
    private var nameTV: EditText? = null
    private var passwordTV: EditText? = null
    private var adminPasswordTV: EditText? = null
    private val adminAcceptedPW: String = "admin"

    private var regBtn: Button? = null
    private var progressBar: ProgressBar? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        nameTV = findViewById(R.id.name)
        emailTV = findViewById(R.id.email)
        passwordTV = findViewById(R.id.password)
        adminPasswordTV = findViewById(R.id.admin_password)

        regBtn = findViewById(R.id.register)
        progressBar = findViewById(R.id.progressBar)
        mAuth = FirebaseAuth.getInstance()

        pref = getSharedPreferences("adminKey",Context.MODE_PRIVATE)

        regBtn!!.setOnClickListener {
            registerNewUser()
        }
    }
    private fun registerNewUser() {
        progressBar!!.visibility = View.VISIBLE
        val name: String
        val email: String
        val password: String
        val adminPassword: String
        name = nameTV!!.text.toString()
        email = emailTV!!.text.toString()
        password = passwordTV!!.text.toString()
        adminPassword = adminPasswordTV!!.text.toString()

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

        // checking if user is admin
        var isAdmin: Boolean = false
        if (!TextUtils.isEmpty(adminPassword)) {
            if (adminPassword == adminAcceptedPW) {
                isAdmin = true
            } else {
                Toast.makeText(applicationContext, "Are you an admin? If so, please enter correct admin password!", Toast.LENGTH_LONG).show()
                return
            }
        }

        Log.i("ADMIN: ", "isAdmin boolean value: " + isAdmin.toString())

        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()
                    progressBar!!.visibility = View.GONE

                    val user = User(name, email, password, isAdmin, 0, ArrayList(), mutableListOf<String>(), mutableMapOf<String,String>(), mutableMapOf<String, ArrayList<String>>(), mutableMapOf<String,Comment>())
                    Log.i("USER: ", user.toString())

                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(mAuth!!.currentUser!!.uid)
                        .setValue(user)

                    // add email and password to sharedprefs
                    editor = pref.edit()
                    editor.putString(email, password)
                    editor.commit()

                    // if the user is an admin, update displayname and sharedprefs
                    if (isAdmin) {
                        // if admin, set name to admin
                        val currUser = FirebaseAuth.getInstance().currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName("admin").build()
                        currUser!!.updateProfile(profileUpdates)

                         editor.putString(mAuth!!.currentUser!!.uid, isAdmin.toString())
                         Log.i("ADMIN: ", "changed editor")
                         editor.commit()
                    }

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