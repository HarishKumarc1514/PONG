package com.example.pong

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.concurrent.schedule

class GameView(context: Context, attrs: AttributeSet)
               : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private val thread: GameThread

    private lateinit var paddleA: PaddleAbstract
    private lateinit var paddleB: PaddleAbstract
    private lateinit var ball: Ball
    private lateinit var game: GameAbstract
//    private var settings: Settings = (context as GameActivity).settings
    private val sharedPreferences: SharedPreferences =
    context.getSharedPreferences(1.toString(), Context.MODE_PRIVATE)


    init {
        holder.addCallback(this)
        thread = GameThread(holder, this)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        thread.running = false
        thread.join()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Set up the paddles.
        paddleA = SimplePaddle(Side.A, 0f, height / 2f)

        paddleB = SimplePaddle(Side.B, width.toFloat(), height / 2f)

        // Set up the ball.
        ball = Ball(width / 2f, height / 2f)

        ball.setUpGameView(this)

        game = OnePlayerGame(paddleA,paddleB,ball,sharedPreferences.getInt("best_score", 0) )

        thread.running = true
        thread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        paddleB.paddleX = width.toFloat()
        ball.initX = width / 2f - ball.size / 2
    }

    fun playSound(resId: Int) {
        Thread {
            val player = MediaPlayer.create(context, resId)
            player.start()
            player.setOnCompletionListener {
                it.release()
            }
        }.start()
    }

    fun update() {
        game.bounce()
        if (game.referee(width)) {
            playSound(R.raw.score_sound2)
            if (game is OnePlayerGame) {
                sharedPreferences.edit().putInt("best_score", (game as OnePlayerGame).bestScore).apply()
            }
            ball.kill()
            Timer().schedule(500) {
                ball.resetBall()
            }
        }

        if (paddleA is SmoothPaddle && paddleB is SmoothPaddle) {
            (paddleA as SmoothPaddle).update(height)
            (paddleB as SmoothPaddle).update(height)
        }

        ball.move()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.also {
            it.drawColor(resources.getColor(R.color.colorPrimaryDark))
            paddleA.draw(it)
            paddleB.draw(it)
            updateScore(it)
            ball.draw(it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        for (i in 0 until event.pointerCount) {
            if (event.getX(i) < width / 2) {
                paddleA.movePaddle(event, i, height)
            } else {
                paddleB.movePaddle(event, i, height)
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateScore(canvas: Canvas?) {
        canvas?.also { it ->
            val textPaint = TextPaint()
            textPaint.color = Color.GRAY
            textPaint.textSize = 500f
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = resources.getFont(R.font.bungee)
            val xPos = canvas.width / 2f
            val yPos = (canvas.height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f)

                it.drawText("${(game as OnePlayerGame).points}", xPos, yPos, textPaint)
                textPaint.textSize = 100f
                textPaint.typeface = resources.getFont(R.font.faster_one)
                it.drawText("BEST: ${(game as OnePlayerGame).bestScore}", xPos, 100f, textPaint)

            }
        }
}




