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
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class FilterDialogs {

    public interface GenresCallback {
        void onSelected(Set<MusicGenre> selected);
    }

    public interface TimeRangeCallback {
        void onSelected(int startMinute, int endMinute);
    }

    public interface DateRangeCallback {
        void onSelected(long startMillis, long endMillis);
    }

    public interface CapacityCallback {
        void onSelected(int minAvailable, int maxAvailable);
    }

    public interface DistanceCallback {
        void onSelected(double lat, double lng, int radiusKm);
    }

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

        // בוחרים שעה התחלה
        TimePickerDialogFragment.newInstance((h, m) -> {
            start[0] = h * 60 + m;

            // ואז שעה סיום
            TimePickerDialogFragment.newInstance((h2, m2) -> {
                end[0] = h2 * 60 + m2;

                if (end[0] < start[0]) {
                    new AlertDialog.Builder(ctx)
                            .setMessage("שעת סיום חייבת להיות אחרי שעת התחלה")
                            .setPositiveButton("אישור", null)
                            .show();
                    return;
                }

                callback.onSelected(start[0], end[0]);
            }).show(fm, "timeEnd");

        }).show(fm, "timeStart");
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

        DatePickerDialogFragment.newInstance((y, mo, d) -> {
            start[0] = toStartOfDayMillis(y, mo, d);

            DatePickerDialogFragment.newInstance((y2, mo2, d2) -> {
                end[0] = toEndOfDayMillis(y2, mo2, d2);

                if (end[0] < start[0]) {
                    new AlertDialog.Builder(ctx)
                            .setMessage("תאריך סיום חייב להיות אחרי תאריך התחלה")
                            .setPositiveButton("אישור", null)
                            .show();
                    return;
                }

                callback.onSelected(start[0], end[0]);
            }).show(fm, "dateEnd");

        }).show(fm, "dateStart");
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

    /* ===================== CAPACITY (AVAILABLE SPOTS) ===================== */

    public static void showCapacity(Context ctx, Integer currentMin, Integer currentMax, CapacityCallback callback) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.dialog_capacity_filter, null);

        EditText minInput = v.findViewById(R.id.inputMinCapacity);
        EditText maxInput = v.findViewById(R.id.inputMaxCapacity);

        if (currentMin != null) {
            minInput.setText(String.valueOf(currentMin));
        }
        if (currentMax != null) {
            maxInput.setText(String.valueOf(currentMax));
        }

        new AlertDialog.Builder(ctx)
                .setTitle("סינון לפי קיבולת")
                .setView(v)
                .setPositiveButton("אישור", (d, w) -> {

                    String minStr = minInput.getText().toString().trim();
                    String maxStr = maxInput.getText().toString().trim();

                    Integer min = minStr.isEmpty() ? null : Integer.parseInt(minStr);
                    Integer max = maxStr.isEmpty() ? null : Integer.parseInt(maxStr);

                    // בדיקות תקינות
                    if (min != null && min < 0) min = 0;
                    if (max != null && max < 0) max = 0;

                    if (min != null && max != null && max < min) {
                        new AlertDialog.Builder(ctx)
                                .setMessage("הערך 'עד' חייב להיות גדול או שווה ל-'מ־'")
                                .setPositiveButton("אישור", null)
                                .show();
                        return;
                    }

                    callback.onSelected(
                            min != null ? min : 0,
                            max != null ? max : Integer.MAX_VALUE
                    );
                })
                .setNegativeButton("ביטול", null)
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

        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_distance_filter, null);
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
