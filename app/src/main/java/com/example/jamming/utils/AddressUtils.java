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
    /** Reverse geocoding */
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

    /** Forward geocoding */
    public static Address getAddressFromQuery(
            Context context,
            String query
    ) throws IOException {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> list = geocoder.getFromLocationName(query, 1);

        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    public static String formatAddress(Address addr) {
        if (addr == null) return "";

        String street = addr.getThoroughfare();
        String house  = addr.getSubThoroughfare();
        String city   = addr.getLocality();
        String countryName = addr.getCountryName();
        String countryCode = addr.getCountryCode();


        StringBuilder result = new StringBuilder();

        //
        if (!TextUtils.isEmpty(street)) {
            result.append(street);
            if (!TextUtils.isEmpty(house)) {
                result.append(" ").append(house);
            }
        }

        if (!TextUtils.isEmpty(city)) {
            if (result.length() > 0) result.append(", ");
            result.append(city);
        }

        //
        if (!TextUtils.isEmpty(countryCode)
                && !countryCode.equalsIgnoreCase("IL")) {
            if (result.length() > 0) result.append(", ");
            result.append(countryName);
        }

        return result.toString();
    }

    /** Validation */
    public static boolean hasStreetAndCity(Address addr) {
        return addr != null
                && !TextUtils.isEmpty(addr.getThoroughfare())
                && !TextUtils.isEmpty(addr.getLocality());
    }
}
