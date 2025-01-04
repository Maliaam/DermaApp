package com.example.dermaapplication.fragments.journal

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dermaapplication.DrawingOverlay
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.google.android.material.button.MaterialButton
import kotlin.math.hypot


class ImageViewFragment : Fragment() {

    private var referenceStartX: Float = 0f
    private var referenceStartY: Float = 0f
    private var referenceEndX: Float = 0f
    private var referenceEndY: Float = 0f

    private val referenceObjectDiameter = 2.3f // Średnica złotówki
    private var pixelsPerCm: Float = 0f
    private var points = mutableListOf<Pair<Float, Float>>()
    private var totalDistance: Float = 0f
    private var previousDistance: Float = 0f

    /** Flagi boolean */
    private var isSelecting = false
    private var isSelectingReference = false
    private var isSelectingSkinChange = false
    private var isZoomMode = false

    private lateinit var overlayView: DrawingOverlay

    private lateinit var expandImageView: ImageView
    private lateinit var backButton: ImageView
    private lateinit var pinButton: ImageButton
    private lateinit var referenceButton: ImageButton
    private lateinit var changeButton: ImageButton
    private lateinit var undoButton: ImageButton
    private lateinit var finishButton: MaterialButton
    private lateinit var zoomButton: ImageButton

    private lateinit var firstLinear: LinearLayout
    private lateinit var secondLinear: LinearLayout
    private lateinit var thirdLinear: LinearLayout

    /** Inicjalizowanie widoków */
    private fun initializeViews(view: View) {
        expandImageView = view.findViewById(R.id.expand_image)
        backButton = view.findViewById(R.id.btnBack)
        pinButton = view.findViewById(R.id.imageView_pin)
        overlayView = view.findViewById(R.id.overlay_view)
        firstLinear = view.findViewById(R.id.firstLinear)
        secondLinear = view.findViewById(R.id.secondLinear)
        referenceButton = view.findViewById(R.id.reference_symbol)
        changeButton = view.findViewById(R.id.skin_symbol)
        undoButton = view.findViewById(R.id.undo_button)
        finishButton = view.findViewById(R.id.end_button)
        thirdLinear = view.findViewById(R.id.thirdLinear)
        zoomButton = view.findViewById(R.id.zoom_symbol)
    }

    private fun setupOnClickListeners() {
        backButton.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as MainActivity).showBottomNav()
        }

        pinButton.setOnClickListener {
            toggleSelectingMode()
        }

        referenceButton.setOnClickListener {
            toggleReferenceMode()
        }
        changeButton.setOnClickListener {
            toggleSkinChangeMode()
        }
        undoButton.setOnClickListener {
            undoLastPoint()
        }
        finishButton.setOnClickListener {
            finishDrawing()
        }
        zoomButton.setOnClickListener {
            toggleZoomMode()
        }
    }

    private fun toggleZoomMode() {
        isZoomMode = !isZoomMode
        if (!isZoomMode) {
            zoomButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.skin_color
                ), PorterDuff.Mode.SRC_IN
            )
        } else {
            zoomButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                ), PorterDuff.Mode.SRC_IN
            )
        }
    }

    /** Zmiana flagi zaznaczania */
    private fun toggleSelectingMode() {
        isSelecting = !isSelecting
        if (!isSelecting) {
            pinButton.setImageResource(R.drawable.pin_active)
            firstLinear.visibility = View.GONE
            secondLinear.visibility = View.GONE
            thirdLinear.visibility = View.GONE
        } else {
            pinButton.setImageResource(R.drawable.pin_inactive)
            firstLinear.visibility = View.VISIBLE
            secondLinear.visibility = View.VISIBLE
//            thirdLinear.visibility = View.VISIBLE
        }
    }

    /** reference mode */
    private fun toggleReferenceMode() {
        if (isSelectingSkinChange) {
            toggleSkinChangeMode()
        }
        isSelectingReference = !isSelectingReference
        if (isSelectingReference) {
            referenceButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.skin_color
                ), PorterDuff.Mode.SRC_IN
            )
        } else {
            referenceButton.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    /** skin change mode */
    private fun toggleSkinChangeMode() {
        if (isSelectingReference) {
            toggleReferenceMode()
        }
        isSelectingSkinChange = !isSelectingSkinChange
        if (isSelectingSkinChange) {
            changeButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.skin_color
                ), PorterDuff.Mode.SRC_IN
            )
        } else {
            changeButton.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun undoLastPoint() {
        if (points.isNotEmpty()) {
            if (points.size > 1) {
                if( points.size > 3){
                    finishButton.visibility = View.VISIBLE
                } else {
                    finishButton.visibility = View.GONE
                }
                val lastPoint = points.last()
                val secondLastPoint = points[points.size - 2]
                val distanceToRemove = calculateDistance(
                    secondLastPoint.first,
                    secondLastPoint.second,
                    lastPoint.first,
                    lastPoint.second
                ) / pixelsPerCm
                totalDistance -= distanceToRemove
            } else {
                totalDistance = 0f
            }
            points.removeAt(points.size - 1)
            overlayView.removePoint()
            updateUndoButtonVisibility()
        }
    }


    private fun finishDrawing() {
        if (points.size > 3) {
            val firstPoint = points.first()
            val lastPoint = points.last()
            val closingDistance = calculateDistance(
                firstPoint.first, firstPoint.second,
                lastPoint.first, lastPoint.second
            ) / pixelsPerCm
            totalDistance += closingDistance

            val area = calculatePolygonArea(points)
            Toast.makeText(
                requireContext(),
                "Koniec. Obecny obwód: $totalDistance cm, Pole: $area cm²",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("calculatearea", area.toString())
        }

    }

    // Funkcja obliczająca przelicznik pikseli na centymetry
    private fun calculatePixelsPerCm(pixelsDistance: Float): Float {
        return pixelsDistance / referenceObjectDiameter
    }

        // Metoda do obliczania odległości w pikselach między dwoma punktami
        private fun calculateDistance(
            startX: Float,
            startY: Float,
            endX: Float,
            endY: Float
        ): Float {
            return hypot(endX - startX, endY - startY)
        }

    /**
     * Metoda ustawiająca przelicznik pikseli na centymetry po wybraniu obiektu referencyjnego.
     */
    private fun onReferenceObjectSelected(startX: Float, startY: Float, endX: Float, endY: Float) {
        val pixelDistance = calculateDistance(startX, startY, endX, endY)
        pixelsPerCm = calculatePixelsPerCm(pixelDistance)
    }

    private fun updateUndoButtonVisibility() {
        undoButton.visibility = if (points.isNotEmpty()) View.VISIBLE else View.GONE
    }





    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_view, container, false)
        (activity as MainActivity).hideBottomNav()
        initializeViews(view)
        setupOnClickListeners()

        val imageUrl = arguments?.getString("imageUrl")
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(expandImageView)
        }

        expandImageView.setOnTouchListener { v, event ->

            if (isSelectingReference) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        referenceStartX = event.x
                        referenceStartY = event.y
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        referenceEndX = event.x
                        referenceEndY = event.y
                        overlayView.setLine(referenceStartX, referenceStartY, referenceEndX, referenceEndY)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        onReferenceObjectSelected(referenceStartX, referenceStartY, referenceEndX, referenceEndY)
                        true
                    }
                    else -> false
                }
            } else if (isSelectingSkinChange && event.action == MotionEvent.ACTION_DOWN) {
                val x = event.x
                val y = event.y
                overlayView.setPoint(x, y)
                if (points.isNotEmpty()) {
                    val lastPoint = points.last()
                    val distance = calculateDistance(lastPoint.first, lastPoint.second, x, y) / pixelsPerCm
                    previousDistance = distance
                    totalDistance += distance
                    if(points.size >= 2){
                        finishButton.visibility = View.VISIBLE
                    }
                }
                points.add(Pair(x, y))
                updateUndoButtonVisibility()
                Toast.makeText(requireContext(), "Punkt dodany. Obecny obwód: $totalDistance cm", Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        }
        return view
    }

    private fun scalePointsToCm(points: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
        return points.map { (x, y) ->
            Pair(x / pixelsPerCm, y / pixelsPerCm)  // Skalowanie punktów do centymetrów
        }
    }

    private fun calculatePolygonArea(points: List<Pair<Float, Float>>): Float {
        val scaledPoints = scalePointsToCm(points)  // Przeskalowanie punktów do cm
        var area = 0f
        val n = scaledPoints.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            val (x1, y1) = scaledPoints[i]
            val (x2, y2) = scaledPoints[j]
            area += x1 * y2 - x2 * y1
        }

        area = Math.abs(area) / 2f
        return area  // Pole powierzchni w cm²
    }

}
