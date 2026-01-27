package com.example.jamming.uihelpers;

import android.widget.EditText;

import androidx.test.espresso.ViewAssertion;

/**
 * Custom assertions for EditText fields.
 */
public final class EditTextAssertions {

    private EditTextAssertions() {}

    // Asserts that an EditText has no error (error == null).
    public static ViewAssertion hasNoError() {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;

            if (!(view instanceof EditText)) {
                throw new AssertionError("Expected EditText but was: " + view.getClass().getName());
            }

            EditText et = (EditText) view;
            if (et.getError() != null) {
                throw new AssertionError("Expected no error, but got: " + et.getError());
            }
        };
    }
}
