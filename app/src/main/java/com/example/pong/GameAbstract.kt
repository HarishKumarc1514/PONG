package com.example.pong

import kotlin.random.Random

abstract class GameAbstract(
    private val paddleA: PaddleAbstract,
    private val paddleB: PaddleAbstract,
    private val ball: Ball,
) {
    abstract fun referee(width: Int): Boolean
    abstract fun bounce()

    fun checkBounce(): Boolean {
        val left = paddleA.paddleX + paddleA.width
        val right = paddleB.paddleX - paddleB.width

        return if (((ball.ballX in (left..(left - ball.dx))) && (ball.ballY + ball.size / 2 in (paddleA.paddleY..paddleA.paddleY + paddleA.height))) ||
            (ball.ballX + ball.size in (right - ball.dx..right)) && (ball.ballY + ball.size / 2 in (paddleB.paddleY..paddleB.paddleY + paddleA.height))
        ) {
            ball.playPaddleBounceSound()
            ball.flipDirection(Ball.SpeedComponent.X)
            true
        } else {
            false
        }
        TODO("better collisions")
    }
}
