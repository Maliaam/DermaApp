package com.example.dermaapplication

import android.view.View
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dermaapplication.fragments.wikiFragments.SkinDiseasesFragment
import junit.framework.TestCase
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SkinDiseasesFragmentTest : TestCase() {
    private lateinit var scenario: FragmentScenario<SkinDiseasesFragment>

    @Before
    fun setup() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_DermaApplication)
        scenario.moveToState(Lifecycle.State.STARTED)
    }

    private fun hasItemCountGreaterThan(minCount: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description?) {
                description?.appendText("with item count greater than $minCount")
            }

            override fun matchesSafely(view: View?): Boolean {
                if (view is RecyclerView) {
                    return view.adapter?.itemCount ?: 0 > minCount
                }
                return false
            }
        }
    }

    @Test
    fun isRecyclerViewDisplayed(){
        onView(withId(R.id.wiki_skinMenu_RecyclerView)).check(matches(isDisplayed()))
    }
    @Test
    fun doesRecyclerHaveMoreThanOneItem(){
        Thread.sleep(1000)
        onView(withId(R.id.wiki_skinMenu_RecyclerView))
            .check(matches(hasItemCountGreaterThan(1)))
    }
    @Test
    fun onRecyclerViewItemClickOpenNewFragment(){
        onView(withId(R.id.wiki_skinMenu_RecyclerView))
            .perform(click())
        onView(withText("Egzema")).check(matches(isDisplayed()))
    }
}