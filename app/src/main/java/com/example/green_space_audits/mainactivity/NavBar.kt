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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.FirebaseError
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.collections.HashMap


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
            // create map
            var gss = getNearestGreenspaces()

            Timer("getGSs", false).schedule( 1000L) {
                Log.i("GS: ", "---GSS---" + gss.toString())
                Log.i("GS: ", "in timer call")
                if (gss.isEmpty()) {
                    Toast.makeText(
                        getContext(),
                        "You're not currently near a green space!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val intent = Intent(getContext(), PreCheckInActivity::class.java)
                    intent.putExtra("gss", gss)

                    getContext().startActivity(intent)
                    val myActivity = getContext() as Activity
                    myActivity.overridePendingTransition(0, 0)
                }
            }
        }
        profile.setOnClickListener {
            val intent = Intent(getContext(), ProfileActivity::class.java)
            getContext().startActivity(intent)
            val myActivity = getContext() as Activity
            myActivity.overridePendingTransition(0,0)
        }
    }

    private fun getNearestGreenspaces() : HashMap<String, String> {
        var gss: HashMap<String, String> = HashMap()

        // look thru each greenspace for distance
        var mDatabaseRef = FirebaseDatabase.getInstance().reference!!.child("GreenSpaces")

        mDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var greenspaces: HashMap<String,Any> = dataSnapshot.getValue() as HashMap<String,Any>

                for ((id, gsRaw) in greenspaces) {
                    Log.i("GS: ", id)
                    Log.i("GS: ", gsRaw.toString())

                    // cast
                    val gs: HashMap<String,Any> = gsRaw as HashMap<String,Any>

                    // get greenspace info
                    val name = gs["gsName"]!!.toString()
                    val gsx:Double = gs["gsLat"] as Double
                    val gsy:Double = gs["gsLong"] as Double

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
//                    val curx: Double = lastKnownLocation.latitude
//                    val cury: Double = lastKnownLocation.longitude

                    val curx = 38.9897
                    val cury = -76.9378

                    // compare user loc to greenspace loc
                    var result: FloatArray = FloatArray(1)
                    Location.distanceBetween(curx, cury, gsx, gsy, result)
                    if (result[0] < 200000.0) {
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

    interface OnDataLoadedListener {
        fun onFinishLoading(data: HashMap<String,String>)
        fun onCancelled(firebaseError: FirebaseError)
    }
}