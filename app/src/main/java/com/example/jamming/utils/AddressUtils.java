package com.example.jamming.utils;

import android.location.Address;
import android.text.TextUtils;

public class AddressUtils {
    public static String formatAddress(Address addr) {
        if (addr == null) return "";

        String street = addr.getThoroughfare();
        String house  = addr.getSubThoroughfare();
        String city   = addr.getLocality();
        String countryName = addr.getCountryName();
        String countryCode = addr.getCountryCode();


        StringBuilder result = new StringBuilder();

        // רחוב + מספר
        if (!TextUtils.isEmpty(street)) {
            result.append(street);
            if (!TextUtils.isEmpty(house)) {
                result.append(" ").append(house);
            }
        }

        // עיר
        if (!TextUtils.isEmpty(city)) {
            if (result.length() > 0) result.append(", ");
            result.append(city);
        }

        // מדינה – רק אם לא ישראל
        if (!TextUtils.isEmpty(countryCode)
                && !countryCode.equalsIgnoreCase("IL")) {
            if (result.length() > 0) result.append(", ");
            result.append(countryName);
        }

        return result.toString();
    }
}
