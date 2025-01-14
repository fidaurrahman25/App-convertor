package com.threedev.appconvertor.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.threedev.appconvertor.R

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val name = intent.getStringExtra("name") ?: "N/A"
        val phone = intent.getStringExtra("phone") ?: "N/A"
        val dob = intent.getStringExtra("dob") ?: "N/A"

        findViewById<TextView>(R.id.user_name_value).text = name
        findViewById<TextView>(R.id.phone_number_value).text = phone
        findViewById<TextView>(R.id.date_of_birth_value).text = dob
    }

}
