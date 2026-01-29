package com.example.jamming.utils;

import android.location.Location;

public class GeoUtils {
    public static double calculateDistanceKm(
            double lat1,
            double lng1,
            double lat2,
            double lng2
    ) {
        double R = 6371.0; // Earth radius in KM

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(rLat1) * Math.cos(rLat2)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}