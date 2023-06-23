package com.example.presentaion.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.presentaion.databinding.ActivityConnectBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val connectionFragment = ConnectionFragment()
        showFragment(connectionFragment)
    }

    private fun showFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction().apply {
            replace(com.example.presentaion.R.id.container, fragment)
            val bundle = Bundle()
            bundle.putString("roomId", intent.getStringExtra("roomId"))
            bundle.putBoolean("isJoin", intent.getBooleanExtra("isJoin", false))
            fragment.arguments = bundle
            commit()
        }
        return true
    }

    companion object {
        fun startActivity(context: Context, roomId: String, isJoin: Boolean) {
            val intent = Intent(context, ConnectActivity::class.java).apply {
                putExtra("roomId", roomId)
                putExtra("isJoin", isJoin)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }
    }
}