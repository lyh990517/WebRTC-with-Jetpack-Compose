package com.example.webrtc.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.webrtc.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() = with(binding) {
        start.setOnClickListener {
            val roomID = roomID.text.toString()
            db.collection("calls")
                .document(roomID)
                .get()
                .addOnSuccessListener {
                    if (it["type"] == "OFFER" || it["type"] == "ANSWER" || it["type"] == "END_CALL") {
                    } else {
                        val intent = Intent(this@MainActivity, WebRTCConnectActivity::class.java)
                        intent.putExtra("roomID", roomID)
                        intent.putExtra("isJoin", false)
                        startActivity(intent)
                    }
                }
        }
        join.setOnClickListener {
            val roomID = roomID.text.toString()
            val intent = Intent(this@MainActivity, WebRTCConnectActivity::class.java)
            intent.putExtra("roomID", roomID)
            intent.putExtra("isJoin", true)
            startActivity(intent)
        }
    }
}