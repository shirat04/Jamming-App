package com.example.jamming.uihelpers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Helpers for interacting with standard Android dialogs.
 */
public final class DialogHelpers {

    private DialogHelpers() {}

    /** Clicks the positive button in standard Android dialogs (OK/Confirm). */
    public static void clickPositive() {
        onView(withId(android.R.id.button1)).perform(click());
    }

    /** Clicks the negative button in standard Android dialogs (Cancel). */
    public static void clickNegative() {
        onView(withId(android.R.id.button2)).perform(click());
    }

    /** Clicks the neutral button in standard Android dialogs (optional). */
    public static void clickNeutral() {
        onView(withId(android.R.id.button3)).perform(click());
    }
}
