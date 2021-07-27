package com.example.pong

import android.view.MotionEvent

class SimplePaddle(side: Side, x: Float, y: Float) : PaddleAbstract(side, x, y ) {

    override fun movePaddle(event: MotionEvent, index: Int, height: Int) {
        updatePosition(event.getY(index) - this.height / 2)
    }
}