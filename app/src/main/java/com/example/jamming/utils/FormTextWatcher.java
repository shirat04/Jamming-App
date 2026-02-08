package com.example.jamming.utils;

import android.text.TextWatcher;
import android.text.Editable;

/**
 * Utility TextWatcher for form fields.
 * Executes a callback whenever the text changes.
 */
public class FormTextWatcher implements TextWatcher {

    /**
     * Callback interface invoked on text changes.
     */
    public interface OnChanged {
        void run(String text);
    }

    // Callback to be executed when the text changes
    private final OnChanged onChanged;

    /**
     * Creates a new FormTextWatcher with the given callback.
     *
     * @param onChanged callback to execute on text changes
     */
    private FormTextWatcher(OnChanged onChanged) {
        this.onChanged = onChanged;
    }

    /**
     * Factory method for creating a FormTextWatcher that reacts to text changes.
     *
     * @param onChanged callback to execute on each text change
     * @return a new FormTextWatcher instance
     */
    public static FormTextWatcher after(OnChanged onChanged) {
        return new FormTextWatcher(onChanged);
    }

    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

    @Override
    public void onTextChanged(CharSequence s, int st, int b, int c) {
        onChanged.run(s == null ? "" : s.toString());
    }

    @Override public void afterTextChanged(Editable s) {}
}
