package com.example.dermaapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class DrawingOverlay @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = android.graphics.Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val pointPaint = Paint().apply {
        color = android.graphics.Color.BLUE
        style = Paint.Style.FILL
        strokeWidth = 10f
    }

    private val linePaint = Paint().apply {
        color = android.graphics.Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    var centerX: Float? = null
    var centerY: Float? = null
    private var radius: Float? = null
    private val points = mutableListOf<Pair<Float, Float>>()
    private var lineStart: Pair<Float, Float>? = null
    private var lineEnd: Pair<Float, Float>? = null


    // Funkcja do dodawania punktów
    fun setPoint(x: Float, y: Float) {
        points.add(Pair(x, y))
        invalidate()
    }

    // Funkcja do rysowania linii między dwoma punktami
    fun setLine(startX: Float, startY: Float, endX: Float, endY: Float) {
        this.lineStart = Pair(startX, startY)
        this.lineEnd = Pair(endX, endY)
        invalidate()  // Odśwież widok
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Rysowanie okręgu
        if (centerX != null && centerY != null && radius != null) {
            canvas.drawCircle(centerX!!, centerY!!, radius!!, paint)
        }

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
    }
}

