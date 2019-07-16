package com.example.arkanoid

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.arkanoid.GameActivity.Companion.load
import com.example.arkanoid.GameActivity.Companion.sharedPref
import com.example.arkanoid.Paddle.Companion.LEFT
import com.example.arkanoid.Paddle.Companion.RIGHT
import com.example.arkanoid.Paddle.Companion.STOPPED


class GameView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private lateinit var thread: GameThread
    private var canvas: Canvas? = null
    @Volatile
    private var touched: Boolean = false
    @Volatile
    private var touched_x: Int = 0
    @Volatile
    private var touched_y: Int = 0
    private var screenX: Int = 0
    private var screenY: Int = 0
    private lateinit var paddle: Paddle
    private lateinit var ball: Ball
    private lateinit var bricks: ArrayList<Brick>
    private var numBricks: Int = 0
    private var levelScore: Int = 0
    private var totalScore: Int = 0
    private var highScore: Int = 0
    private var lives: Int = 0
    private var won: Boolean = false
    private var lost: Boolean = false
    private var invisibles : String = ""

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

        highScore = sharedPref.getInt("hs", 0)
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        if(load){
            lives = sharedPref.getInt("lives", 0)
            totalScore = sharedPref.getInt("ts",0)
            levelScore = sharedPref.getInt("ls",0)
            invisibles = sharedPref.getString("invisible", "")
        }
        this.isFocusable = true
        screenX = width
        screenY = height
        paddle = Paddle(screenX, screenY)
        ball = Ball(screenX, screenY)
        thread = GameThread(holder, this)
        thread.setRunning(true)
        thread.start()
        createBricksAndRestart(true)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenX = width
        screenY = height
        surfaceCreated(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        saveState()
        thread.setRunning(false)
        thread.join()
    }

    //save current score to SharedPrefs
    fun saveState(){
        val editor : SharedPreferences.Editor = sharedPref.edit()
        editor.putInt("hs",highScore)
        editor.putInt("ts", totalScore)
        editor.putInt("ls", levelScore)
        editor.putInt("lives",lives)
        var tmp : String = ""
        for (i : Int in 0 until numBricks){
            if(bricks[i].getVisibility()){
                tmp+='0'
            } else {
                tmp+='1'
            }
        }
        editor.putString("invisible", tmp)
        editor.commit()
    }

    //update game state and check for victory or loss
    fun update(fps: Long) {
        paddle.update(fps)
        ball.update(fps)
        //update score and visibility if any visible block was hit
        for (i in 0 until numBricks) {
            if (bricks[i].getVisibility()) {
                if (RectF.intersects(bricks[i].getRect(), ball.getRect())) {
                    bricks[i].setInvisible()
                    ball.reverseYVelocity()
                    levelScore += 10
                    totalScore += 10
                    if(highScore < totalScore){
                        highScore = totalScore
                    }
                }
            }
        }

        //bounce the ball off the paddle
        if (RectF.intersects(paddle.getRect(), ball.getRect())) {
            ball.setRandomXVelocity()
            ball.reverseYVelocity()
            ball.clearObstacleY(paddle.getRect().top - 2f)
        }

        //ball missed the paddle and fell
        if (ball.getRect().bottom > screenY) {
            ball.reverseYVelocity()
            ball.clearObstacleY(screenY - 2f)
            lives--
            if (lives == 0) {
                lost = true
                thread.setPaused(true)
                createBricksAndRestart(false)
            }
        }

        //bounce off top
        if (ball.getRect().top < 0) {
            ball.reverseYVelocity()
            ball.clearObstacleY(22f)
        }

        //bounce off left
        if (ball.getRect().left < 0) {
            ball.reverseXVelocity()
            ball.clearObstacleX(2f)
        }

        //bounce off right
        if (ball.getRect().right > screenX - 10) {
            ball.reverseXVelocity()
            ball.clearObstacleX(screenX - 32f)
        }

        // Pause if cleared screen
        if (levelScore == numBricks * 10) {
            won = true
            thread.setPaused(true)
            createBricksAndRestart(false)
        }

    }

    //draw current state of game (all visible blocks plus paddle, ball and scores)
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        val paint1 = Paint()
        val paint2 = Paint()
        paint1.color = resources.getColor(R.color.title1, resources.newTheme())
        paint2.color = resources.getColor(R.color.colorPrimary, resources.newTheme())
        try {
            canvas!!.drawColor(resources.getColor(R.color.bg1, resources.newTheme()))
            canvas.drawRect(paddle.getRect(), paint2)
            canvas.drawRect(ball.getRect(), paint2)
            for (i in 0 until numBricks) {
                if (bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].getRect(), paint1)
                }
            }
            paint2.textSize = 40f
            canvas.drawText("Score: " + totalScore + "   Lives: " + lives+"   Highscore: "+highScore, 10f, 50f, paint2)

            if (won) {
                paint2.textSize = 90f
                canvas.drawText("YOU HAVE WON!", 10f, screenY / 2f, paint2)
            }

            if (lost) {
                paint2.textSize = 90f
                canvas.drawText("YOU HAVE LOST!", 10f, screenY / 2f, paint2)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //paddle movement (touch on left side moves it to the left, touch on the right side to the right)
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        touched_x = event!!.x.toInt()
        touched_y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touched = true
                if (won) {
                    won = false
                }
                if (lost) {
                    lost = false
                }
                thread.setPaused(false)
                if (event.x > screenX / 2) {
                    paddle.setMovementState(RIGHT)
                } else {
                    paddle.setMovementState(LEFT)
                }
            }
            MotionEvent.ACTION_MOVE -> touched = false
            MotionEvent.ACTION_UP -> {
                touched = true
                paddle.setMovementState(STOPPED)
            }
            MotionEvent.ACTION_CANCEL -> touched = false
            MotionEvent.ACTION_OUTSIDE -> touched = false
        }
        return true
    }

    fun pause() {
        thread.setRunning(false)
        try {
            thread.join()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resume() {
        thread = GameThread(holder, this)
        thread.setRunning(true)
        thread.start()
    }

    fun createBricksAndRestart(isRestore : Boolean) {
        ball.reset(screenX, screenY)
        val brickWidth = screenX / 10
        val brickHeight = screenY / 10
        bricks = ArrayList(200)
        numBricks = 0
        if(!isRestore){
            levelScore = 0
        }

        for (column in 0..9) {
            for (row in 0..4) {
                bricks.add(numBricks, Brick(row, column, brickWidth, brickHeight))
                numBricks++
                if(isRestore && invisibles.length >= numBricks){
                    if (invisibles[numBricks-1] == '1'){
                        bricks[numBricks-1].setInvisible()
                    }
                }
            }
        }
        if (lives == 0) {
            totalScore = 0
            lives = 3
        }
    }
}
