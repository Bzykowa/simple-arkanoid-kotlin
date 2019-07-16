package com.example.arkanoid

import android.graphics.Canvas
import android.view.SurfaceHolder
import java.lang.Exception

class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {

    private var running: Boolean = false
    private val targetFPS = 60
    private var canvas: Canvas? = null
    private var paused : Boolean = false

    fun setRunning(isRunning: Boolean) {
        running = isRunning
    }

    fun setPaused(isPaused: Boolean) {
        paused = isPaused
    }

    override fun run() {
        var startTime : Long
        var timeMillis : Long
        var waitTime : Long
        val targetTime = (1000/targetFPS).toLong()

        while (running){
            startTime = System.nanoTime()
            canvas = null
            try {
                canvas = surfaceHolder.lockCanvas()
                synchronized(surfaceHolder){
                    if(!paused){
                        gameView.update(targetFPS.toLong())
                    }
                    gameView.draw(canvas!!)
                }
            } catch (e : Exception){
                e.printStackTrace()
            } finally {
                if (canvas != null){
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e : Exception){
                        e.printStackTrace()
                    }
                }
            }
            timeMillis = (System.nanoTime() - startTime)/1000000
            waitTime = targetTime - timeMillis
            try {
                sleep(waitTime)
            } catch (e : Exception){
                e.printStackTrace()
            }
        }
    }
}