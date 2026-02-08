package com.example.jamming.view.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * DialogFragment that displays a TimePicker dialog and returns
 * the selected time via a callback interface.
 *
 * This fragment is responsible only for UI interaction and
 * does not hold any business logic.
 */
public class TimePickerDialogFragment extends DialogFragment {

    /**
     * Callback interface used to deliver the selected time
     * back to the caller.
     */
    public interface OnTimeSelectedListener {
        void onTimeSelected(int hour, int minute);
    }

    // Listener for delivering the selected time result
    private OnTimeSelectedListener listener;

    /**
     * Factory method for creating a new instance of the dialog.
     * The title is passed via arguments Bundle to survive configuration changes,
     * while the listener is kept as a runtime callback reference.
     */
    public static TimePickerDialogFragment newInstance(OnTimeSelectedListener listener, String title) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        fragment.listener = listener;

        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Creates and configures the TimePickerDialog.
     * Initializes it with the current system time and sets a title if provided.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Use current time as the default value for the picker
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
