package com.example.dermaapplication

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dermaapplication.fragments.journal.JournalFragment
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JournalFragmentTest: TestCase() {
    private lateinit var scenario: FragmentScenario<JournalFragment>
    @Before
    fun setup(){
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_DermaApplication)
        scenario.moveToState(Lifecycle.State.STARTED)
    }
    @Test
    fun testAddJournalRecord() {

        onView(withId(R.id.journal_add_new_record)).perform(click())
        onView(withText("Dodaj wpis")).check(matches(isDisplayed()))
        onView(withHint("Wpisz tytuł")).perform(typeText("Nowy wpis"))
        onView(withText("OK")).perform(click())
        closeSoftKeyboard()
        onView(withId(R.id.journal_RecyclerView))
            .check(matches(hasDescendant(withText("Nowy wpis"))))
    }
}
