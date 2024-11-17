package com.example.dermaapplication.fragments.questionnaire

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.google.android.material.button.MaterialButton
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
    private lateinit var closeInfoButton: ImageView
    private lateinit var info: CardView

    /** Lista pinezek dla widoku z przodu */
    private val frontPins = mutableListOf<Pair<Float, Float>>()

    /** Lista pinezek dla widoku z tyłu */
    private val backPins = mutableListOf<Pair<Float, Float>>()

    /** Flaga wskazująca, która strona (przód/tył) jest aktywna */
    private var isFrontSide = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_body, container, false)

        endButton = view.findViewById(R.id.buttonEnd)
        frontEndButton = view.findViewById(R.id.buttonFrontBack)
        frameLayoutContainer = view.findViewById(R.id.frameLayoutContainer)
        helpButton = view.findViewById(R.id.help_body)
        info = view.findViewById(R.id.help_description)
        closeInfoButton = view.findViewById(R.id.close_info)

        endButton.setOnClickListener {
            (activity as MainActivity).replaceFragment(FragmentQuestionnaire())
        }

        frontEndButton.setOnClickListener {
            toggleSide()
        }
        frontEndButton.text = if (isFrontSide) "TYŁ" else "PRZÓD"

        helpButton.setOnClickListener {
            info.visibility = View.VISIBLE

        }
        closeInfoButton.setOnClickListener {
            info.visibility = View.GONE
        }
        frameLayoutContainer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imageView = frameLayoutContainer.findViewById<ImageView>(R.id.imageView6)
                val imageBounds = getImageBounds(imageView)

                if (removePinIfClicked(frameLayoutContainer, event.x, event.y)) {
                    return@setOnTouchListener true
                }

                if (imageBounds.contains(event.x, event.y)) {
                    val relativeX = (event.x - imageBounds.left) / imageBounds.width()
                    val relativeY = (event.y - imageBounds.top) / imageBounds.height()

                    val absoluteX = relativeX * imageBounds.width() + imageBounds.left
                    val absoluteY = relativeY * imageBounds.height() + imageBounds.top
                    addPin(frameLayoutContainer, absoluteX, absoluteY)

                    if (isFrontSide) {
                        frontPins.add(Pair(relativeX, relativeY))
                    } else {
                        backPins.add(Pair(relativeX, relativeY))
                    }
                }
                true
            } else {
                false
            }
        }

        return view
    }

    /**
     * Dodaje nową pinezkę w określonym punkcie na obrazie. Pinezka jest reprezentowana przez
     * mały, czerwony punkt, który jest umieszczany na widoku człowieka. Jest to punkt
     * orientacyjny, wskazujący miejsce, w którym użytkownik zaznaczył coś na obrazie.
     *
     * @param container Kontener, do którego pinezka zostanie dodana.
     * @param x Współrzędna X, gdzie ma zostać umieszczona pinezka.
     * @param y Współrzędna Y, gdzie ma zostać umieszczona pinezka.
     */
    private fun addPin(container: FrameLayout, x: Float, y: Float) {
        val pin = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(40, 40).apply {
                setMargins(x.toInt() - 10, y.toInt() - 10, 0, 0)
            }
            setBackgroundResource(R.drawable.ic_pin)
            tag = "pin"
        }
        container.addView(pin)
    }

    /**
     * Funkcja przełącza między stronami "przód" i "tył" i aktualizuje widoczność pinezek. Po
     * przełączeniu wszystkie pinezki dla danej strony są ładowane z zapisanych współrzędnych, a
     * interfejs jest aktualizowany w zależności od aktywnej strony.
     */
    private fun toggleSide() {
        isFrontSide = !isFrontSide
        clearPins(frameLayoutContainer)

        val pins = if (isFrontSide) frontPins else backPins
        val imageView = frameLayoutContainer.findViewById<ImageView>(R.id.imageView6)
        val imageBounds = getImageBounds(imageView)

        pins.forEach { (relativeX, relativeY) ->
            val absoluteX = relativeX * imageBounds.width() + imageBounds.left
            val absoluteY = relativeY * imageBounds.height() + imageBounds.top
            addPin(frameLayoutContainer, absoluteX, absoluteY)
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
     * @return Zwraca true, jeśli pinezka została usunięta.
     */
    private fun removePinIfClicked(container: FrameLayout, coordinateX: Float, coordinateY: Float)
            : Boolean {
        var removed = false
        val iterator = container.children.iterator()
        while (iterator.hasNext()) {
            val view = iterator.next()
            if (view.tag == "pin") {
                val pinBounds = RectF(
                    view.x, view.y, view.x + view.width,
                    view.y + view.height
                )
                if (pinBounds.contains(coordinateX, coordinateY)) {
                    container.removeView(view)
                    removed = true

                    // Sprawdzamy, która strona jest aktywna
                    val imageView = container.findViewById<ImageView>(R.id.imageView6)
                    val imageBounds = getImageBounds(imageView)

                    if (imageBounds.contains(coordinateX, coordinateY)) {
                        val relativeX = (coordinateX - imageBounds.left) / imageBounds.width()
                        val relativeY = (coordinateY - imageBounds.top) / imageBounds.height()

                        val pinList = if (isFrontSide) frontPins else backPins
                        pinList.removeIf { (x, y) -> abs(x - relativeX) < 0.02 && abs(y - relativeY) < 0.02 }
                    }
                    break
                }
            }
        }
        return removed
    }

}