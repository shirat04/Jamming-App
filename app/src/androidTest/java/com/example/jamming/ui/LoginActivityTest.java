package com.example.jamming.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.jamming.R;
import com.example.jamming.view.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {


    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testUIElementsVisibility() {

        onView(withId(R.id.usernameInput))
                .check(matches(isDisplayed()));


        onView(withId(R.id.passwordInput))
                .check(matches(isDisplayed()));


        onView(withId(R.id.loginButton))
                .check(matches(isDisplayed()));
    }



    @Test
    public void testTypingInInputFields() {

        String testUser = "testuser@example.com";
        String testPass = "password123";

        onView(withId(R.id.usernameInput))
                .perform(androidx.test.espresso.action.ViewActions.typeText(testUser))
                .check(matches(withText(testUser)));

        onView(withId(R.id.passwordInput))
                .perform(androidx.test.espresso.action.ViewActions.typeText(testPass))
                .check(matches(withText(testPass)));
    }



    @Test
    public void testRegisterButtonNavigation() {

        onView(withId(R.id.registerText))
                .check(matches(isDisplayed()))
                .check(matches(androidx.test.espresso.matcher.ViewMatchers.isClickable()));
    }

    @Test
    public void testGoogleSignInButtonDisplayed() {

        onView(withId(R.id.googleSignInButton))
                .check(matches(isDisplayed()));
    }
}