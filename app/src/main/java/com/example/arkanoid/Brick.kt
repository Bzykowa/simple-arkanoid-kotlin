package com.example.arkanoid

import android.graphics.RectF

class Brick (private val row : Int, private val column : Int, private val width : Int, private val height : Int){

    private lateinit var rect: RectF
    private var isVisible: Boolean = false

    init {
        isVisible = true
        val padding = 1
        rect = RectF(
            (column * width + padding).toFloat(),
            (row * height + padding).toFloat(),
            (column * width + width - padding).toFloat(),
            (row * height + height - padding).toFloat()
        )
    }

    fun getRect(): RectF {
        return this.rect
    }

    fun setInvisible() {
        isVisible = false
    }

    fun getVisibility(): Boolean {
        return isVisible
    }
}