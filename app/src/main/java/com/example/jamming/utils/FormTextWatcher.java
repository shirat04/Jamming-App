package com.example.jamming.utils;

import android.text.TextWatcher;
import android.text.Editable;

/**
 * Utility TextWatcher for form fields.
 * Executes a callback after text changes.
 */
public class FormTextWatcher implements TextWatcher {

    public interface OnChanged {
        void run(String text);
    }

    private final OnChanged onChanged;

    private FormTextWatcher(OnChanged onChanged) {
        this.onChanged = onChanged;
    }

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
