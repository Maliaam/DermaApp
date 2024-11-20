package com.example.dermaapplication.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide

/**
 * Adapter obsługujący wyświetlanie obrazów w ViewPager.
 * @param imageUrls Lista URL obrazów do wyświetlenia.
 */
class ImagePagerAdapter(private val imageUrls: List<String>) : PagerAdapter() {
    /**
     * Zwraca liczbę elementów w adapterze tj. liczbę obrazów do wyświetlenia.
     * @return Liczba obrazów.
     */
    override fun getCount(): Int = imageUrls.size

    /**
     * Sprawdza, czy dany widok jest powiązany z podanym obiektem.
     * @param view Widok ViewPager
     * @param `object` Obiekt.
     * @return True, jeśli widok odpowiada obiektowi w przeciwnym razie False.
     */
    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    /**
     * Tworzy nowy widok dla danego elementu.
     * Załadowany obraz jest dodawany do kontenera ViewPager.
     * @param container Kontener ViewPager.
     * @param position Indeks obrazu w liście URL.
     * @return Obiekt ImageView reprezentujący dany element.
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(container.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        Glide.with(container.context)
            .load(imageUrls[position])
            .into(imageView)

        container.addView(imageView)
        return imageView
    }

    /**
     * Usuwa widok z kontenera ViewPager, gdy nie jest już potrzebny.
     * Używany do czyszczenia pamięci podczas przewijania ViewPagera.
     * @param container Kontener ViewPager.
     * @param position Indeks elementu do usunięcia.
     * @param `object` Obiekt ImageView do usunięcia.
     */
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}