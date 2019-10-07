package com.vapps.crossprocessapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

abstract class CanvasView @JvmOverloads constructor(
    val ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(ctx, attrs, defStyleAttr), View.OnTouchListener {

    private val mPaint = Paint()
    private val mSupportList = ArrayList<Float>()

    init {
        setOnTouchListener(this)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        mPaint.strokeWidth = 2f
        val wUtil = width * 0.90f
        val centerVertical = height / 2f
        val startX: Float = ((width - wUtil) / 2.0).toFloat()

        canvas?.drawLine(startX, centerVertical, startX + wUtil, centerVertical, mPaint)

        mPaint.color = Color.RED
        mSupportList.forEach {
            canvas?.drawCircle(it, centerVertical, 0.02f * wUtil, mPaint)
        }
        mPaint.color = Color.BLACK
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Toast.makeText(ctx, "Touched at ${event?.x}", Toast.LENGTH_SHORT).show()
        if (event == null) return false

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                addSupport(event.x)
                true
            }
            else -> false
        }
    }

    private fun addSupport(x: Float) {
        val wUtil = width * 0.90f
        val startX: Float = ((width - wUtil) / 2.0).toFloat()
        if (x > startX && x < wUtil + startX)
            mSupportList.add(x)

        this.invalidate()
    }
}