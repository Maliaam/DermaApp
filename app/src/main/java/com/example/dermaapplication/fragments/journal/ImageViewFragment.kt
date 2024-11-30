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
import com.example.dermaapplication.R
import com.example.dermaapplication.DrawingOverlay
import com.example.dermaapplication.MainActivity
import kotlin.math.hypot


class ImageViewFragment : Fragment() {

    private var referenceStartX: Float = 0f
    private var referenceStartY: Float = 0f
    private var referenceEndX: Float = 0f
    private var referenceEndY: Float = 0f

    private var firstPointX: Float = 0f
    private var firstPointY: Float = 0f
    private var secondPointX: Float = 0f
    private var secondPointY: Float = 0f

    private val referenceObjectDiameter = 1.6f // Średnica złotówki to 16mm
    private var pixelsPerCm: Float = 0f

    /** Flagi boolean */
    private var isSelecting = false
    private var isSelectingReference = false
    private var isSelectingSkinChange = false
    private var isFirstPointSelected = false

    private lateinit var overlayView: DrawingOverlay

    private lateinit var expandImageView: ImageView
    private lateinit var backButton: ImageView
    private lateinit var pinButton: ImageButton
    private lateinit var referenceButton: ImageButton
    private lateinit var changeButton: ImageButton

    private lateinit var firstLinear: LinearLayout
    private lateinit var secondLinear: LinearLayout

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
    }

    /** Zmiana flagi zaznaczania */
    private fun toggleSelectingMode() {
        isSelecting = !isSelecting
        if (!isSelecting) {
            pinButton.setImageResource(R.drawable.pin_active)
            firstLinear.visibility = View.GONE
            secondLinear.visibility = View.GONE
        } else {
            pinButton.setImageResource(R.drawable.pin_inactive)
            firstLinear.visibility = View.VISIBLE
            secondLinear.visibility = View.VISIBLE
        }
    }

    /** reference mode */
    private fun toggleReferenceMode() {
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
    // Funkcja obliczająca przelicznik pikseli na centymetry
    private fun calculatePixelsPerCm(diameterInPixels: Float): Float {
        return diameterInPixels / referenceObjectDiameter
    }

    // Funkcja do obliczania odległości w pikselach między dwoma punktami
    private fun calculateDistanceInPixels(startX: Float, startY: Float, endX: Float, endY: Float): Float {
        return hypot(endX - startX, endY - startY)
    }

    // Funkcja przeliczająca odległość w pikselach na centymetry
    private fun calculateRealDistanceInCm(pixelDistance: Float, pixelsPerCm: Float): Float {
        return pixelDistance / pixelsPerCm
    }

    // Funkcja, która wywołuje obliczenie rzeczywistej odległości między dwoma punktami
    private fun onSkinChangeSelected() {
        val pixelDistance = calculateDistanceInPixels(firstPointX, firstPointY, secondPointX, secondPointY)
        val realDistanceInCm = calculateRealDistanceInCm(pixelDistance, pixelsPerCm)


        Toast.makeText(
            requireContext(),
            "Rzeczywista odległość między punktami: $realDistanceInCm cm",
            Toast.LENGTH_LONG
        ).show()
    }


    private fun onReferenceObjectSelected(startX: Float, startY: Float, endX: Float, endY: Float) {

        val pixelDistance = calculateDistanceInPixels(startX, startY, endX, endY)


        pixelsPerCm = calculatePixelsPerCm(pixelDistance)


        Toast.makeText(
            requireContext(),
            "Przelicznik: $pixelsPerCm pikseli na cm\nŚrednica obiektu referencyjnego: $pixelDistance pikseli",
            Toast.LENGTH_LONG
        ).show()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
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

        expandImageView.setOnTouchListener { _, event ->
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

                        overlayView.setLine(
                            referenceStartX,
                            referenceStartY,
                            referenceEndX,
                            referenceEndY
                        )
                        true
                    }

                    MotionEvent.ACTION_UP -> {

                        onReferenceObjectSelected(
                            referenceStartX,
                            referenceStartY,
                            referenceEndX,
                            referenceEndY
                        )
                        true
                    }

                    else -> false
                }
            } else if (isSelectingSkinChange) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (!isFirstPointSelected) {

                            firstPointX = event.x
                            firstPointY = event.y


                            overlayView.setPoint(firstPointX, firstPointY)
                            isFirstPointSelected = true
                            true
                        } else {

                            secondPointX = event.x
                            secondPointY = event.y


                            overlayView.setPoint(secondPointX, secondPointY)


                            onSkinChangeSelected()
                            isFirstPointSelected = false
                            true
                        }
                    }

                    else -> false
                }
            } else {
                false
            }
        }


        return view
    }
}
