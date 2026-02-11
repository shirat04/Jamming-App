package com.example.jamming.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.jamming.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.example.jamming.view.OwnerActivity;
import com.example.jamming.view.OwnerPastEventsActivity;

@RunWith(AndroidJUnit4.class)
public class OwnerActivityTest {

    @Rule
    public ActivityScenarioRule<OwnerActivity> activityRule =
            new ActivityScenarioRule<>(OwnerActivity.class);

    @Before
    public void setUp() {

        Intents.init();
    }

    @After
    public void tearDown() {

        Intents.release();
    }


    @Test
    public void testActivityViewsDisplay() {

        onView(withId(R.id.createEventButton))
                .check(matches(isDisplayed()));


    }


    @Test
    public void testNavigateToPastEvents() {

        onView(withId(R.id.btnPastEvents))
                .perform(click());


        intended(hasComponent(OwnerPastEventsActivity.class.getName()));
    }




    @Test
    public void testDoubleBackPressBehavior() {

        onView(isRoot()).perform(pressBack());


        onView(withId(R.id.createEventButton))
                .check(matches(isDisplayed()));
    }


    private static org.hamcrest.Matcher<android.view.View> isRoot() {
        return androidx.test.espresso.matcher.ViewMatchers.isRoot();
    }
}