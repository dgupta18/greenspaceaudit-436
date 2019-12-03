package layout

import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.LinearLayout
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.green_space_audits.mainactivity.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class NavBar(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {
    init {
        inflate(context, R.layout.navbar, this)

        val map = findViewById<ImageButton>(R.id.map_button)
        val add = findViewById<ImageButton>(R.id.add_button)
        val checkin = findViewById<ImageButton>(R.id.checkin_button)
        val profile = findViewById<ImageButton>(R.id.profile_button)

        map.setOnClickListener {
            val intent = Intent(getContext(), MapsActivity::class.java)
            getContext().startActivity(intent)
            val myActivity = getContext() as Activity
            myActivity.overridePendingTransition(0,0)
        }
        add.setOnClickListener {
            val intent = Intent(getContext(), AddGreenSpaceActivity::class.java)
            getContext().startActivity(intent)
            val myActivity = getContext() as Activity
            myActivity.overridePendingTransition(0,0)
        }
        checkin.setOnClickListener {

            val gss = getNearestGreenspaces()
            if (gss == null) {
                Toast.makeText(getContext(), "You're not currently near a green space!", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(getContext(), PreCheckInActivity::class.java)
                getContext().startActivity(intent)
                val myActivity = getContext() as Activity
                for ((k,v) in gss) {
                    intent.putExtra(k, v)
                }
                myActivity.overridePendingTransition(0,0)
            }
        }
        profile.setOnClickListener {
            val intent = Intent(getContext(), ProfileActivity::class.java)
            getContext().startActivity(intent)
            val myActivity = getContext() as Activity
            myActivity.overridePendingTransition(0,0)
        }
    }

    private fun getNearestGreenspaces(): MutableMap<String,String>? {
        // create map
        var gss: MutableMap<String,String> = mutableMapOf<String,String>()

        // look thru each greenspace for distance
        var mDatabaseRef = FirebaseDatabase.getInstance().reference!!.child("Greenspaces")
        mDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val greenspaces = dataSnapshot.children
                for (gs in greenspaces) {
                    Log.i("GS: ", gs.key)

                    // get greenspace info
                    val id = gs.key
                    val name = gs.getValue(GreenSpace::class.java)!!.gsName
                    val gsx:Double = gs.getValue(GreenSpace::class.java)!!.gsLat.toDouble()
                    val gsy:Double = gs.getValue(GreenSpace::class.java)!!.gsLong.toDouble()

                    // get user location
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(context as Activity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    MapsActivity.LOCATION_PERMISSION_REQUEST_CODE
                                )
                                return
                    }
                    val locationProvider = locationManager.getBestProvider(Criteria(), true)
                    val lastKnownLocation = locationManager.getLastKnownLocation(locationProvider)
                    val curx: Double = lastKnownLocation.latitude
                    val cury: Double = lastKnownLocation.longitude

                    // compare user loc to greenspace loc
                    var result: FloatArray = FloatArray(1)
                    Location.distanceBetween(curx, cury, gsx, gsy, result)
                    if (result[0] < 16094.0 && id != null) {
                        gss[name] = id
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                println("The read failed: " + p0.code)
            }
        })

        return gss
    }
}