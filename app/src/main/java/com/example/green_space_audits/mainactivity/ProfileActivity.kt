package com.example.green_space_audits.mainactivity

import android.os.Bundle
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





class ProfileActivity : AppCompatActivity() {

    private lateinit var nameHolder: TextView
    private lateinit var pointsHolder: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mDatabaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        nameHolder = findViewById<View>(R.id.profile_name) as TextView
        pointsHolder = findViewById<View>(R.id.profile_points) as TextView
        mAuth = FirebaseAuth.getInstance()

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")
    }

    override fun onStart() {
        super.onStart()
        var id = mAuth!!.currentUser!!.uid

        var usr = mDatabaseReference.child(id)

//        // Attach a listener to read the data
//        mDatabaseReference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val currUser = dataSnapshot.getValue(User::class.java)
//                if ()
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                println("The read failed: " + databaseError.code)
//            }
//        })
    }
}