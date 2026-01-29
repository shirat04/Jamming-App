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
    private String title;
    public static TimePickerDialogFragment newInstance(
            OnTimeSelectedListener listener,String title) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        fragment.listener = listener;
        fragment.title = title;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hour, minute) -> {
                    if (listener != null) {
                        listener.onTimeSelected(hour, minute);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        if (title != null) {
            dialog.setTitle(title);
        }

        return dialog;
    }
}
