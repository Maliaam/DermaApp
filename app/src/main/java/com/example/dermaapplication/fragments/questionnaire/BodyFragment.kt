package com.example.dermaapplication.fragments.questionnaire

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Pin
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.abs

/**
 * Fragment odpowiedzialny za interakcję z użytkownikiem z obrazem widoku ciała. Pozwala
 * użytkownikowi na dodawanie pinezek w obszarze ciała (przód/tył) oraz na przełączanie między
 * stronami. Pinezki są zapisywane i przechowywane w zależności od wybranej strony.
 */
class BodyFragment : Fragment() {
    private lateinit var endButton: MaterialButton
    private lateinit var frontEndButton: MaterialButton
    private lateinit var frameLayoutContainer: FrameLayout
    private lateinit var helpButton: ImageView
    private lateinit var bodyImage: ImageView

    /** Lista pinezek dla widoku z przodu */
    private val frontPins = mutableListOf<Pin>()

    /** Lista pinezek dla widoku z tyłu */
    private val backPins = mutableListOf<Pin>()

    /** Flaga wskazująca, która strona (przód/tył) jest aktywna */
    private var isFrontSide = true

    private fun initializeViews(view: View) {
        endButton = view.findViewById(R.id.buttonEnd)
        frontEndButton = view.findViewById(R.id.buttonFrontBack)
        frameLayoutContainer = view.findViewById(R.id.frameLayoutContainer)
        helpButton = view.findViewById(R.id.help_body)
        bodyImage = view.findViewById(R.id.imageView6)
    }

    private fun setupOnClickListeners() {
        endButton.setOnClickListener {
            val pinsBundle = exportPins(frontPins, backPins)
            val fragment = FragmentQuestionnaire().apply {
                arguments = pinsBundle
            }
            (activity as MainActivity).replaceFragment(fragment)
        }
        frontEndButton.setOnClickListener {
            toggleSide()
        }
        helpButton.setOnClickListener {
            Utilities.infoDialogBuilder(
                requireContext(),
                "Dodatkowe informacje",
                resources.getString(R.string.help_body),
                "OK"
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_body, container, false)
        initializeViews(view)
        setupOnClickListeners()
        frontEndButton.text = if (isFrontSide) "TYŁ" else "PRZÓD"

        frameLayoutContainer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (removePinIfClicked(frameLayoutContainer, event.x, event.y, 20f)) {
                    Log.d("PinDebug", "Pin removed at x: ${event.x}, y: ${event.y}")
                    return@setOnTouchListener true
                }
                addPin(frameLayoutContainer, event.x, event.y)
                Log.d("PinDebug", "Pin added at x: ${event.x}, y: ${event.y}")
                return@setOnTouchListener true
            }
            false
        }

        return view
    }

    /**
     * Dodaje nową pinezkę w określonym punkcie na obrazie. Pinezka jest reprezentowana przez
     * czerwony punkt (ImageView), który jest umieszczany na widoku człowieka.
     *
     * @param container Kontener, do którego pinezka zostanie dodana.
     * @param x Współrzędna X, gdzie ma zostać umieszczona pinezka.
     * @param y Współrzędna Y, gdzie ma zostać umieszczona pinezka.
     */
    private fun addPin(container: FrameLayout, x: Float, y: Float) {
        val pin = ImageView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(60, 60).apply {
                setMargins(x.toInt() - 30, y.toInt() - 45, 0, 0)
            }
            setImageResource(R.drawable.pin_image)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            tag = "pin"
        }

        val scaleAnimation = ScaleAnimation(
            0f, 1f,
            0f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 300
            fillAfter = true
        }

        container.addView(pin)
        pin.startAnimation(scaleAnimation)

        val pinObject = Pin(x, y)
        if (isFrontSide) {
            frontPins.add(pinObject)
        } else {
            backPins.add(pinObject)
        }
    }


    /**
     * Funkcja przełącza między stronami "przód" i "tył" i aktualizuje widoczność pinezek.
     * Po przełączeniu wszystkie pinezki dla danej strony są ładowane z zapisanych współrzędnych, a
     * interfejs jest aktualizowany w zależności od aktywnej strony.
     */
    private fun toggleSide() {
        isFrontSide = !isFrontSide
        bodyImage.setImageResource(if (isFrontSide) R.drawable.front_body else R.drawable.back_body)
        clearPins(frameLayoutContainer)

        val pinList = if (isFrontSide) frontPins else backPins
        val pinsCopy = ArrayList(pinList)
        pinsCopy.forEach { pin ->
            addPin(frameLayoutContainer, pin.x, pin.y)
        }


        frontEndButton.text = if (isFrontSide) "TYŁ" else "PRZÓD"
    }


    /**
     * Usuwa wszystkie pinezki z widoku, aby przygotować przestrzeń dla nowych pinezek po
     * przełączeniu strony (przód/tył).
     *
     * @param container Kontener, z którego pinezki mają zostać usunięte.
     */
    private fun clearPins(container: FrameLayout) {
        val viewsToRemove = mutableListOf<View>()
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child.tag == "pin") {
                viewsToRemove.add(child)
            }
        }
        viewsToRemove.forEach { container.removeView(it) }
    }

    /**
     * Funkcja oblicza granice obrazu w widoku, uwzględniając wszelkie transformacje (skalowanie,
     * przesunięcie), które mogły zostać zastosowane do obrazu. Granice obrazu są używane do
     * określenia, czy kliknięcie w obszarze obrazu znajduje się w dopuszczalnym obszarze dla
     * dodania pinezki.
     *
     * @param imageView Obiekt ImageView, którego granice mają zostać obliczone.
     * @return Obiekt RectF, który zawiera granice obrazu w widoku.
     */
    private fun getImageBounds(imageView: ImageView): RectF {
        val drawable = imageView.drawable ?: return RectF()
        val matrix = imageView.imageMatrix
        val values = FloatArray(9)
        matrix.getValues(values)
        val left = values[Matrix.MTRANS_X]
        val top = values[Matrix.MTRANS_Y]
        val width = drawable.intrinsicWidth * values[Matrix.MSCALE_X]
        val height = drawable.intrinsicHeight * values[Matrix.MSCALE_Y]
        return RectF(left, top, left + width, top + height)
    }

    /**
     * Funkcja sprawdza, czy użytkownik kliknął na istniejącą pinezkę. Jeśli kliknięcie znajduje się
     * w granicach pinezki, pinezka jest usuwana z widoku. Dodatkowo, współrzędne tej pinezki są
     * usuwane z odpowiedniej listy (przód/tył).
     *
     * @param container Kontener, w którym pinezki są przechowywane.
     * @param coordinateX Współrzędna X miejsca kliknięcia.
     * @param coordinateY Współrzędna Y miejsca kliknięcia.
     * @param marginError Wartość błędu dla współrzędnych.
     * @return Zwraca true, jeśli pinezka została usunięta.
     */
    private fun removePinIfClicked(
        container: FrameLayout,
        coordinateX: Float,
        coordinateY: Float,
        marginError: Float
    ): Boolean {
        var removed = false
        val pinList = if (isFrontSide) frontPins else backPins

        val iterator = container.children.iterator()
        while (iterator.hasNext()) {
            val view = iterator.next()
            if (view.tag == "pin") {
                val pinBounds = RectF(
                    view.x, view.y, view.x + view.width,
                    view.y + view.height
                )
                // Sprawdzamy, czy kliknięcie jest w granicach pinezki
                if (pinBounds.contains(coordinateX, coordinateY)) {
                    container.removeView(view)
                    removed = true
                    // Usuwamy z listy `Pin`
                    pinList.removeIf { pin ->
                        abs(pin.x - coordinateX) <= marginError && abs(pin.y - coordinateY) <= marginError
                    }
                    break
                }
            }
        }
        return removed
    }
    private fun normalizeCoordinates(x: Float, y:Float,imageView: ImageView): Pair<Float,Float>{
        val bounds = getImageBounds(imageView)
        val normalizedX = (x - bounds.left) / bounds.width()
        val normalizedY = (y - bounds.top) / bounds.height()
        return Pair(normalizedX,normalizedY)
    }


    /**
     * Eksportuje listy pinezek jako listy map z wartościami X i Y, które przekazywane są do bundle.
     * @return bundle zawierający listy map.
     */
    private fun exportPins(
        frontPins: MutableList<Pin>,
        backPins: MutableList<Pin>
    ): Bundle {
        val bundle = Bundle()
        val frontPinsAsMap = frontPins.map { mapOf("x" to it.x, "y" to it.y) }
        val backPinsAsMap = backPins.map { mapOf("x" to it.x, "y" to it.y) }

        bundle.putSerializable("frontPins", ArrayList(frontPinsAsMap))
        bundle.putSerializable("backPins", ArrayList(backPinsAsMap))

        return bundle
    }
}