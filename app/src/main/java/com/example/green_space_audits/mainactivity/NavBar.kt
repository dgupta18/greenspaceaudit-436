package layout

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.LinearLayout
import com.example.green_space_audits.mainactivity.R
import android.content.Intent
import com.example.green_space_audits.mainactivity.AddGreenSpaceActivity
import com.example.green_space_audits.mainactivity.MapsActivity
import com.example.green_space_audits.mainactivity.ProfileActivity




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
//        checkin.setOnClickListener {
//            val intent = Intent(getContext(), CheckinActivity::class.java)
//            getContext().startActivity(intent)
//            val myActivity = getContext() as Activity
//            myActivity.overridePendingTransition(0,0)
//        }
        profile.setOnClickListener {
            val intent = Intent(getContext(), ProfileActivity::class.java)
            getContext().startActivity(intent)
            val myActivity = getContext() as Activity
            myActivity.overridePendingTransition(0,0)
        }
    }
}