package com.example.jamming.view.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerDialogFragment extends DialogFragment {

    public interface OnTimeSelectedListener {
        void onTimeSelected(int hour, int minute);
    }

    private OnTimeSelectedListener listener;

    public static TimePickerDialogFragment newInstance(
            OnTimeSelectedListener listener
    ) {
        TimePickerDialogFragment fragment =
                new TimePickerDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Calendar c = Calendar.getInstance();

        return new TimePickerDialog(
                requireContext(),
                (view, hour, minute) -> {
                    if (listener != null) {
                        listener.onTimeSelected(hour, minute);
                    }
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        );
    }
}
