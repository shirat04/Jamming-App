package com.example.jamming.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for address-related operations.
 * Provides helpers for converting locations and search queries
 * into Address objects and formatting them for display.
 */
public class AddressUtils {

    /**
     * Performs reverse geocoding: converts latitude and longitude
     * coordinates into an {@link Address} object.
     *
     * @param context Application context
     * @param lat Latitude value
     * @param lng Longitude value
     * @return Address corresponding to the given coordinates, or null if not found
     * @throws IOException If a network or I/O error occurs during geocoding
     */
    public static Address getAddressFromLatLng(
            Context context,
            double lat,
            double lng
    ) throws IOException {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> list = geocoder.getFromLocation(lat, lng, 1);

        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    /**
     * Performs forward geocoding: converts a textual location query
     * into an {@link Address} object.
     *
     * @param context Application context
     * @param query Textual address or place name
     * @return Address matching the query, or null if not found
     * @throws IOException If a network or I/O error occurs during geocoding
     */
    public static Address getAddressFromQuery(
            Context context,
            String query
    ) throws IOException {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> list = geocoder.getFromLocationName(query, 1);

        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    /**
     * Formats an {@link Address} object into a readable string.
     * The format includes street and house number (if available),
     * city, and country (only if outside Israel).
     *
     * @param addr Address to format
     * @return Human-readable address string, or an empty string if the address is null
     */
    public static String formatAddress(Address addr) {
        if (addr == null) return "";

        String street = addr.getThoroughfare();
        String house  = addr.getSubThoroughfare();
        String city   = addr.getLocality();
        String countryName = addr.getCountryName();
        String countryCode = addr.getCountryCode();


        StringBuilder result = new StringBuilder();

        //Append street and house number
        if (!TextUtils.isEmpty(street)) {
            result.append(street);
            if (!TextUtils.isEmpty(house)) {
                result.append(" ").append(house);
            }
        }
        // Append city
        if (!TextUtils.isEmpty(city)) {
            if (result.length() > 0) result.append(", ");
            result.append(city);
        }

        //Append country only if outside Israel
        if (!TextUtils.isEmpty(countryCode)
                && !countryCode.equalsIgnoreCase("IL")) {
            if (result.length() > 0) result.append(", ");
            result.append(countryName);
        }

        return result.toString();
    }

    /**
     * Validates that an {@link Address} object contains
     * both street and city information.
     *
     * @param addr Address to validate
     * @return True if the address has both street and city, false otherwise
     */
    public static boolean hasStreetAndCity(Address addr) {
        return addr != null
                && !TextUtils.isEmpty(addr.getThoroughfare())
                && !TextUtils.isEmpty(addr.getLocality());
    }
}
