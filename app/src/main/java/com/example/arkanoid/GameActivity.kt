package com.example.arkanoid

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager

class GameActivity : AppCompatActivity() {

    private lateinit var gameView : GameView
    private val PREFS_NAME = "gameState"

    companion object{
        const val LOAD = "load"
        var load : Boolean = false
        lateinit var sharedPref : SharedPreferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        load = intent.getBooleanExtra(LOAD, false)
        sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        gameView = findViewById(R.id.game)
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        gameView.saveState()
        load = true
        gameView.surfaceChanged(gameView.holder, PixelFormat.RGB_565, 0,0)
        gameView.invalidate()
    }

}
