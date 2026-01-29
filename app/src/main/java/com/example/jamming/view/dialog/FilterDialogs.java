package com.example.jamming.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jamming.R;
import com.example.jamming.model.MusicGenre;
import com.google.android.material.slider.Slider;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class FilterDialogs {

    public interface GenresCallback { void onSelected(Set<MusicGenre> selected);}

    public interface TimeRangeCallback { void onSelected(int startMinute, int endMinute);}

    public interface DateRangeCallback { void onSelected(long startMillis, long endMillis);}

    public interface CapacityCombinedCallback {
        void onSelected(Integer minAvailable, Integer maxAvailable, Integer minCapacity, Integer maxCapacity);
    }

    public interface DistanceCallback { void onSelected(double lat, double lng, int radiusKm);}

    /* ===================== MUSIC ===================== */

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
                .setTitle("Select genres")
                .setMultiChoiceItems(displayNames, checked, (d, which, isChecked) -> {
                    checked[which] = isChecked;
                })
                .setPositiveButton("OK", (d, w) -> {
                    Set<MusicGenre> result = new HashSet<>();
                    for (int i = 0; i < allGenres.length; i++) {
                        if (checked[i]) {
                            result.add(allGenres[i]);
                        }
                    }
                    callback.onSelected(result);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    /* ===================== TIME RANGE ===================== */

    public static void showTimeRange(
            androidx.fragment.app.FragmentManager fm,
            Context ctx,
            Integer currentStartMinute,
            Integer currentEndMinute,
            TimeRangeCallback callback
    ) {
        final int[] start = { currentStartMinute != null ? currentStartMinute : 18 * 60 };
        final int[] end   = { currentEndMinute != null ? currentEndMinute : 23 * 60 };

        TimePickerDialogFragment.newInstance(
                (h, m) -> {
                    start[0] = h * 60 + m;

                    TimePickerDialogFragment.newInstance(
                            (h2, m2) -> {
                                end[0] = h2 * 60 + m2;
                                callback.onSelected(start[0], end[0]);
                            },
                            "Select end time"
                    ).show(fm, "timeEnd");

                },
                "Select start time"
        ).show(fm, "timeStart");

    }

    /* ===================== DATE RANGE ===================== */

    public static void showDateRange(
            androidx.fragment.app.FragmentManager fm,
            Context ctx,
            Long currentStart,
            Long currentEnd,
            DateRangeCallback callback
    ) {
        final long[] start = { currentStart != null ? currentStart : System.currentTimeMillis() };
        final long[] end   = { currentEnd != null ? currentEnd : System.currentTimeMillis() };

        DatePickerDialogFragment.newInstance(
                (y, mo, d) -> {
                    start[0] = toStartOfDayMillis(y, mo, d);

                    DatePickerDialogFragment.newInstance(
                            (y2, mo2, d2) -> {
                                end[0] = toEndOfDayMillis(y2, mo2, d2);

                                if (end[0] < start[0]) {
                                    new AlertDialog.Builder(ctx)
                                            .setMessage("End date must be after start date")
                                            .setPositiveButton("OK", null)
                                            .show();
                                    return;
                                }

                                callback.onSelected(start[0], end[0]);
                            },
                            "Select end date"
                    ).show(fm, "dateEnd");

                },
                "Select start date"
        ).show(fm, "dateStart");
    }


    private static long toStartOfDayMillis(int y, int mo, int d) {
        Calendar c = Calendar.getInstance();
        c.set(y, mo, d, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static long toEndOfDayMillis(int y, int mo, int d) {
        Calendar c = Calendar.getInstance();
        c.set(y, mo, d, 23, 59, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    /* ===================== CAPACITY (AVAILABLE SPOTS, MAXIMUM SPOTS) ===================== */

    public static void showCapacityCombinedFilter(
            Context ctx,
            Integer curMinAvailable,
            Integer curMaxAvailable,
            Integer curMinCapacity,
            Integer curMaxCapacity,
            CapacityCombinedCallback callback
    ) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.activity_dialog_capacity_filter, null);

        EditText minAvail = v.findViewById(R.id.inputMinAvailable);
        EditText maxAvail = v.findViewById(R.id.inputMaxAvailable);
        EditText minCap   = v.findViewById(R.id.inputMinCapacity);
        EditText maxCap   = v.findViewById(R.id.inputMaxCapacity);

        if (curMinAvailable != null) minAvail.setText(String.valueOf(curMinAvailable));
        if (curMaxAvailable != null) maxAvail.setText(String.valueOf(curMaxAvailable));
        if (curMinCapacity != null)  minCap.setText(String.valueOf(curMinCapacity));
        if (curMaxCapacity != null)  maxCap.setText(String.valueOf(curMaxCapacity));

        new AlertDialog.Builder(ctx)
                .setTitle("Seat Filtering")
                .setView(v)
                .setPositiveButton("אישור", (d, w) -> {

                    Integer minA = parseIntOrNull(minAvail.getText().toString());
                    Integer maxA = parseIntOrNull(maxAvail.getText().toString());
                    Integer minC = parseIntOrNull(minCap.getText().toString());
                    Integer maxC = parseIntOrNull(maxCap.getText().toString());

                    if (minA != null && maxA != null && maxA < minA) {
                        showError(ctx, "טווח כיסאות פנויים לא תקין");
                        return;
                    }
                    if (minC != null && maxC != null && maxC < minC) {
                        showError(ctx, "טווח גודל אירוע לא תקין");
                        return;
                    }

                    callback.onSelected(minA, maxA, minC, maxC);
                })
                .setNegativeButton("ביטול", null)
                .show();
    }
    private static Integer parseIntOrNull(String s) {
        s = s.trim();
        return s.isEmpty() ? null : Integer.parseInt(s);
    }

    private static void showError(Context ctx, String message) {
        new AlertDialog.Builder(ctx)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }


    /* ===================== DISTANCE ===================== */

    public static void showDistance(
            Context ctx,
            Location currentLocation,
            Integer currentRadius,
            DistanceCallback callback
    ) {
        if (currentLocation == null) {
            new AlertDialog.Builder(ctx)
                    .setMessage("אין מיקום זמין כרגע. ודא/י שהרשאות מיקום מאופשרות.")
                    .setPositiveButton("אישור", null)
                    .show();
            return;
        }

        View v = LayoutInflater.from(ctx).inflate(R.layout.activity_dialog_distance_filter, null);
        Slider slider = v.findViewById(R.id.radiusSlider);
        TextView label = v.findViewById(R.id.radiusLabel);

        int startRadius = currentRadius != null ? currentRadius : 10;
        slider.setValue(startRadius);
        label.setText("מרחק: " + startRadius + " ק\"מ");

        slider.addOnChangeListener((s, value, fromUser) -> {
            label.setText("מרחק: " + Math.round(value) + " ק\"מ");
        });

        new AlertDialog.Builder(ctx)
                .setTitle("סינון לפי מרחק")
                .setView(v)
                .setPositiveButton("אישור", (d, w) -> {
                    int radius = Math.round(slider.getValue());
                    callback.onSelected(
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude(),
                            radius
                    );
                })
                .setNegativeButton("ביטול", null)
                .show();
    }
}
