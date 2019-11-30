package com.example.green_space_audits.mainactivity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text


class ProfileActivity : AppCompatActivity() {

    private lateinit var nameHolder: TextView
    private lateinit var emailHolder: TextView
    private lateinit var pointsHolder: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mDatabaseReference: DatabaseReference
    private var currName: String? = null
    private var currEmail: String? = null
    private var currPoints: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        nameHolder = findViewById<View>(R.id.profile_name) as TextView
        emailHolder = findViewById<View>(R.id.profile_email) as TextView
        pointsHolder = findViewById<View>(R.id.profile_points) as TextView
        mAuth = FirebaseAuth.getInstance()

        mDatabase = FirebaseDatabase.getInstance()
//        mDatabaseReference = mDatabase!!.reference!!
        mDatabaseReference = mDatabase!!.reference!!.child("Users")
    }

    override fun onStart() {
        super.onStart()
        val id = mAuth!!.currentUser!!.uid
//        val usr = mDatabaseReference.child("Users").child(id)

        // Attach a listener to read the data
        mDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currUser = dataSnapshot.child(id).getValue(User::class.java)
                currName = currUser!!.userName
                currEmail = currUser!!.userEmail
                currPoints = currUser!!.uPoints.toString()

//                currName = dataSnapshot.child("userName").getValue(String::class.java)
//                currEmail = dataSnapshot.child("userEmail").getValue(String::class.java)
//                currPoints = dataSnapshot.child("uPoints").getValue(String::class.java)
                Log.i("profile", currName!!.toString() + " " + currEmail!!.toString() + " " + currPoints!!.toString())

                // set values in textviews
                nameHolder.text = currName
                emailHolder.text = currEmail
                pointsHolder.text = currPoints
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })

    }
}