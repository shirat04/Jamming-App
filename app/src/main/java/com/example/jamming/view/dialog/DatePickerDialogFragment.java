package com.example.jamming.view.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment {

    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int day);
    }

    private OnDateSelectedListener listener;
    private String title;
    public static DatePickerDialogFragment newInstance(
            OnDateSelectedListener listener,String title
    ) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.listener = listener;
        fragment.title = title;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    if (listener != null) {
                        listener.onDateSelected(year, month, dayOfMonth);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );


        dialog.getDatePicker().setMinDate(System.currentTimeMillis());

        if (title != null) {
            dialog.setTitle(title);
        }
        return dialog;
    }

}
