package com.example.dermaapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class DrawingOverlay @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val pointPaint = Paint().apply {
        color = android.graphics.Color.RED
        style = Paint.Style.FILL
        strokeWidth = 15f
    }

    private val linePaint = Paint().apply {
        color = android.graphics.Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val pointLinePaint = Paint().apply {
        color = android.graphics.Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val points = mutableListOf<Pair<Float, Float>>()
    private var lineStart: Pair<Float, Float>? = null
    private var lineEnd: Pair<Float, Float>? = null


    fun setPoint(x: Float, y: Float) {
        points.add(Pair(x, y))
        invalidate()
    }

    fun removePoint(){
        if(points.isNotEmpty()){
            points.removeAt(points.size - 1)
            invalidate()
        }
    }

    fun setLine(startX: Float, startY: Float, endX: Float, endY: Float) {
        this.lineStart = Pair(startX, startY)
        this.lineEnd = Pair(endX, endY)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Rysowanie punktów
        for ((x, y) in points) {
            canvas.drawCircle(x, y, 10f, pointPaint)
        }

            // Rysowanie linii, jeśli zostały ustawione dwa punkty
        if (lineStart != null && lineEnd != null) {
            canvas.drawLine(
                lineStart!!.first, lineStart!!.second,
                lineEnd!!.first, lineEnd!!.second, linePaint
            )
        }
        if (points.size >= 2) {
            for (i in 0 until points.size - 1) {
                val start = points[i]
                val end = points[i + 1]
                canvas.drawLine(start.first, start.second, end.first, end.second, pointLinePaint)
            }
        }
    }
}

