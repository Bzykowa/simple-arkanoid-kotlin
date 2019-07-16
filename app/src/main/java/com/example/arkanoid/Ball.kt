package com.example.arkanoid

import android.graphics.RectF
import kotlin.random.Random


class Ball(private val screenX : Int, private val screenY : Int) {
    private val rect : RectF
    private var xVelocity : Float
    private var yVelocity : Float
    private val ballWidth = 20
    private val ballHeight = 20

    init {
        rect = RectF()
        xVelocity = 200f
        yVelocity = -400f
    }

    fun getRect() : RectF{
        return rect
    }

    fun update(fps : Long){
        rect.left = rect.left + (xVelocity/fps)
        rect.top = rect.top + (yVelocity/fps)
        rect.right = rect.left + ballWidth
        rect.bottom = rect.top - ballHeight
    }

    fun reverseYVelocity(){
        yVelocity = -yVelocity
    }

    fun reverseXVelocity(){
        xVelocity = -xVelocity
    }

    fun setRandomXVelocity(){
        val generator : Random = Random(System.nanoTime())
        val answer : Int = generator.nextInt(2)
        if (answer == 0){
            reverseXVelocity()
        }
    }

    fun clearObstacleY(y : Float){
        rect.bottom = y
        rect.top = y-ballHeight
    }

    fun clearObstacleX(x : Float){
        rect.left = x
        rect.right = x+ballWidth
    }

    fun reset(x: Int, y: Int) {
        rect.left = x / 2f
        rect.top = y - 20f
        rect.right = x / 2f + ballWidth
        rect.bottom = y - 20f - ballHeight
        xVelocity = 200f
        yVelocity = -400f
    }
}