package com.example.jamming.view.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * DialogFragment that displays a DatePicker dialog and returns
 * the selected date via a callback interface.
 *
 * This fragment handles only UI interaction and delegates the result
 * back to the caller using a listener.
 */
public class DatePickerDialogFragment extends DialogFragment {

    /**
     * Callback interface used to deliver the selected date
     * back to the caller.
     */
    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int day);
    }

    // Listener for delivering the selected date result
    private OnDateSelectedListener listener;

    /**
     * Factory method for creating a new instance of the dialog.
     * The title is passed via arguments Bundle to survive configuration changes,
     * while the listener is kept as a runtime callback reference.
     */
    public static DatePickerDialogFragment newInstance(OnDateSelectedListener listener, String title) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.listener = listener;

        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);

        return fragment;
    }


    /**
     * Creates and configures the DatePickerDialog.
     * Initializes it with the current system date and applies basic constraints.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Use current date as the default value for the picker
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

        // Prevent selecting dates in the past
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());

        // Retrieve and apply the dialog title from arguments, if exists
        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString("title");
            if (title != null) {
                dialog.setTitle(title);
            }
        }

        return dialog;
    }

}
