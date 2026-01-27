package com.example.jamming.uihelpers;

/**
 * Shared test data for location simulation.
 * Provides presets and a small value object for flexibility.
 */
public final class LocationTestData {

    private LocationTestData() {}

    //Simple value object representing a location selection.
    public static final class LocationData {
        public final double lat;
        public final double lng;
        public final String address;

        public LocationData(double lat, double lng, String address) {
            this.lat = lat;
            this.lng = lng;
            this.address = address;
        }
    }

    // Presets (useful defaults)
    public static final LocationData TEL_AVIV =
            new LocationData(32.0853, 34.7818, "הבנאי 9, תל אביב");

    public static final LocationData JERUSALEM =
            new LocationData(31.7683, 35.2137, "יפו 1, ירושלים");

    public static final LocationData HAIFA =
            new LocationData(32.7940, 34.9896, "שדרות בן גוריון 1, חיפה");

    // Factory method for custom locations.
    public static LocationData of(double lat, double lng, String address) {
        return new LocationData(lat, lng, address);
    }
}
