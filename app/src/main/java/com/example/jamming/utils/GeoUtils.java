package com.example.jamming.utils;

/**
 * Utility class for geographic calculations.
 * Provides helper methods related to latitude and longitude coordinates.
 */
public class GeoUtils {

    /**
     * Calculates the distance in kilometers between two geographic points
     * using the Haversine formula.
     *
     * @param lat1 Latitude of the first point
     * @param lng1 Longitude of the first point
     * @param lat2 Latitude of the second point
     * @param lng2 Longitude of the second point
     * @return Distance between the two points in kilometers
     */
    public static double calculateDistanceKm(
            double lat1,
            double lng1,
            double lat2,
            double lng2
    ) {
        // Earth's radius in kilometers
        double R = 6371.0;

        // Differences in latitude and longitude (converted to radians)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        // Convert original latitudes to radians
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        // Haversine formula: calculates the square of half the chord length
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(rLat1) * Math.cos(rLat2)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        // Angular distance in radians
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Final distance in kilometers
        return R * c;
    }
}
