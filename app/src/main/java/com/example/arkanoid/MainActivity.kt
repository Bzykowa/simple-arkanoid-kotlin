package com.example.arkanoid

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.example.arkanoid.GameActivity.Companion.LOAD

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startGame(view : View){
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    fun loadGame(view : View){
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(LOAD, true)
        startActivity(intent)
    }
}
