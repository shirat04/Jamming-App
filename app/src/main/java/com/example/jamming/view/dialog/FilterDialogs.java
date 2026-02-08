package com.example.jamming.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.jamming.R;
import com.example.jamming.model.MusicGenre;
import com.example.jamming.utils.DateUtils;
import com.google.android.material.slider.Slider;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class responsible for displaying filter dialogs
 * and returning user selections via callbacks.
 *
 * The class contains only UI-related logic and does not hold state.
 */
public final class FilterDialogs {

    private FilterDialogs() {}

    /* ===== Temporary holders for lambdas ===== */

    static class TimeRangeTmp {
        int start;
        int end;
    }

    static class DateRangeTmp {
        long start;
        long end;
    }

    /* ===== Callbacks ===== */

    // Callback for multi-selection of music genres
    public interface GenresCallback {
        void onSelected(Set<MusicGenre> selected);
    }

    // Callback for selecting a time range (minutes from midnight)
    public interface TimeRangeCallback {
        void onSelected(Integer startMinute, Integer endMinute);
    }

    // Callback for selecting a date range in milliseconds
    public interface DateRangeCallback {
        void onSelected(Long startMillis, Long endMillis);
    }

    // Callback for combined capacity filtering
    public interface CapacityCombinedCallback {
        void onSelected(Integer minAvailable, Integer maxAvailable, Integer minCapacity, Integer maxCapacity);
    }

    // Callback for distance-based filtering
    public interface DistanceCallback {
        void onSelected(double lat, double lng, int radiusKm);
    }

    /* ===================== MUSIC ===================== */

    // Displays a multi-choice dialog for selecting music genres.
    // Pre-selects currently active genres.
    public static void showMusic(
            Context ctx,
            Set<MusicGenre> current,
            MusicGenre[] allGenres,
            GenresCallback callback
    ) {
        String[] displayNames = new String[allGenres.length];
        boolean[] checked = new boolean[allGenres.length];

        for (int i = 0; i < allGenres.length; i++) {
            displayNames[i] = allGenres[i].getDisplayName();
            checked[i] = current.contains(allGenres[i]);
        }

        new AlertDialog.Builder(ctx)
                .setTitle(R.string.select_genres)
                .setMultiChoiceItems(displayNames, checked, (d, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    Set<MusicGenre> result = new HashSet<>();
                    for (int i = 0; i < allGenres.length; i++) {
                        if (checked[i]) {
                            result.add(allGenres[i]);
                        }
                    }
                    callback.onSelected(result);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /* ===================== TIME RANGE ===================== */

    // Shows the time filter dialog.
    // If a filter is already applied, displays the current range
    // and allows the user to change or clear it.
    public static void showTimeRange(
            FragmentActivity activity,
            Integer currentStartMinute,
            Integer currentEndMinute,
            TimeRangeCallback callback
    ) {
        Context ctx = activity;
        FragmentManager fm = activity.getSupportFragmentManager();

        if (currentStartMinute != null && currentEndMinute != null) {
            new AlertDialog.Builder(ctx)
                    .setTitle(R.string.current_time_filter)
                    .setMessage(DateUtils.formatTimeRange(currentStartMinute, currentEndMinute))
                    .setPositiveButton(R.string.change, (d, w) ->
                            openTimePickers(fm, ctx, currentStartMinute, currentEndMinute, callback))
                    .setNegativeButton(R.string.clear, (d, w) -> callback.onSelected(null, null))
                    .setNeutralButton(R.string.cancel, null)
                    .show();
            return;
        }

        openTimePickers(fm, ctx, null, null, callback);
    }

    // Opens two sequential time pickers (start → end).
    private static void openTimePickers(
            FragmentManager fm,
            Context ctx,
            Integer startMinute,
            Integer endMinute,
            TimeRangeCallback callback
    ) {
        TimeRangeTmp range = new TimeRangeTmp();
        range.start = startMinute != null ? startMinute : 18 * 60;
        range.end   = endMinute   != null ? endMinute   : 23 * 60;

        TimePickerDialogFragment.newInstance((h, m) -> {
                    range.start = h * 60 + m;

                    TimePickerDialogFragment.newInstance((h2, m2) -> {
                                range.end = h2 * 60 + m2;
                                callback.onSelected(range.start, range.end);
                            }, ctx.getString(R.string.select_end_time))
                            .show(fm, "timeEnd");

                }, ctx.getString(R.string.select_start_time))
                .show(fm, "timeStart");
    }

    /* ===================== DATE RANGE ===================== */

    // Shows the date filter dialog.
    // If a date range is already selected, displays it
    // and allows the user to change or clear the filter.
    public static void showDateRange(
            FragmentActivity activity,
            Long currentStart,
            Long currentEnd,
            DateRangeCallback callback
    ) {
        Context ctx = activity;
        FragmentManager fm = activity.getSupportFragmentManager();

        if (currentStart != null && currentEnd != null) {
            new AlertDialog.Builder(ctx)
                    .setTitle(R.string.current_date_filter)
                    .setMessage(DateUtils.formatOnlyDate(currentStart) + " – " + DateUtils.formatOnlyDate(currentEnd))
                    .setPositiveButton(R.string.change, (d, w) ->
                            openDatePickers(fm, ctx, currentStart, currentEnd, callback))
                    .setNegativeButton(R.string.clear, (d, w) -> callback.onSelected(null, null))
                    .setNeutralButton(R.string.cancel, null)
                    .show();
            return;
        }

        openDatePickers(fm, ctx, null, null, callback);
    }

    // Opens two sequential date pickers (start → end).
    private static void openDatePickers(
            FragmentManager fm,
            Context ctx,
            Long startMillis,
            Long endMillis,
            DateRangeCallback callback
    ) {
        DateRangeTmp range = new DateRangeTmp();
        long now = System.currentTimeMillis();
        range.start = startMillis != null ? startMillis : now;
        range.end   = endMillis   != null ? endMillis   : now;

        DatePickerDialogFragment.newInstance(
                (y, mo, d) -> {
                    range.start = toStartOfDayMillis(y, mo, d);

                    DatePickerDialogFragment.newInstance((y2, mo2, d2) -> {
                                range.end = toEndOfDayMillis(y2, mo2, d2);
                                callback.onSelected(range.start, range.end);
                            }, ctx.getString(R.string.select_end_date))
                            .show(fm, "dateEnd");
                },
                ctx.getString(R.string.select_start_date)
        ).show(fm, "dateStart");
    }

    // Converts a selected date to the beginning of the day (00:00:00)
    private static long toStartOfDayMillis(int y, int mo, int d) {
        Calendar c = Calendar.getInstance();
        c.set(y, mo, d, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    // Converts a selected date to the end of the day (23:59:59)
    private static long toEndOfDayMillis(int y, int mo, int d) {
        Calendar c = Calendar.getInstance();
        c.set(y, mo, d, 23, 59, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    /* ===================== CAPACITY ===================== */

    // Displays a combined capacity filter dialog
    // for available spots and total event capacity.
    public static void showCapacityCombinedFilter(
            Context ctx,
            Integer curMinAvailable,
            Integer curMaxAvailable,
            Integer curMinCapacity,
            Integer curMaxCapacity,
            CapacityCombinedCallback callback
    ) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.activity_dialog_capacity_filter, null);

        EditText minAvail = v.findViewById(R.id.inputMinAvailable);
        EditText maxAvail = v.findViewById(R.id.inputMaxAvailable);
        EditText minCap   = v.findViewById(R.id.inputMinCapacity);
        EditText maxCap   = v.findViewById(R.id.inputMaxCapacity);

        if (curMinAvailable != null) minAvail.setText(String.valueOf(curMinAvailable));
        if (curMaxAvailable != null) maxAvail.setText(String.valueOf(curMaxAvailable));
        if (curMinCapacity != null)  minCap.setText(String.valueOf(curMinCapacity));
        if (curMaxCapacity != null)  maxCap.setText(String.valueOf(curMaxCapacity));

        new AlertDialog.Builder(ctx)
                .setTitle(R.string.seat_filtering)
                .setView(v)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    Integer minA = parseIntOrNull(minAvail.getText().toString());
                    Integer maxA = parseIntOrNull(maxAvail.getText().toString());
                    Integer minC = parseIntOrNull(minCap.getText().toString());
                    Integer maxC = parseIntOrNull(maxCap.getText().toString());

                    callback.onSelected(minA, maxA, minC, maxC);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static Integer parseIntOrNull(String s) {
        try {
            s = s.trim();
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* ===================== DISTANCE ===================== */

    // Displays a distance filter dialog using a slider.
    // Requires a valid current location.
    public static void showDistance(
            Context ctx,
            Location currentLocation,
            Integer currentRadius,
            DistanceCallback callback
    ) {
        if (currentLocation == null) {
            new AlertDialog.Builder(ctx)
                    .setMessage(R.string.no_location)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }

        View v = LayoutInflater.from(ctx).inflate(R.layout.activity_dialog_distance_filter, null);
        Slider slider = v.findViewById(R.id.radiusSlider);
        TextView label = v.findViewById(R.id.radiusLabel);

        int startRadius = currentRadius != null ? currentRadius : 10;
        slider.setValue(startRadius);
        label.setText(ctx.getString(R.string.distance_km, startRadius));

        slider.addOnChangeListener((s, value, fromUser) ->
                label.setText(ctx.getString(R.string.distance_km, Math.round(value)))
        );

        new AlertDialog.Builder(ctx)
                .setTitle(R.string.filter_by_distance)
                .setView(v)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    int radius = Math.round(slider.getValue());
                    callback.onSelected(
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude(),
                            radius
                    );
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
