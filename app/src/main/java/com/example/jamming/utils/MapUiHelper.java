package com.example.jamming.utils;

import com.example.jamming.model.Event;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * UI helper for drawing event markers on a Google Map.
 * Clears the map, restores user location, and adds a marker for each event
 * with a valid geographic position.
 */
public class MapUiHelper {

    public static void drawEvents(GoogleMap map, List<Event> events, Runnable reEnableLocation) {
        map.clear();
        reEnableLocation.run();

        for (Event event : events) {
            if (event.getLatitude() == 0 || event.getLongitude() == 0) continue;

            Marker marker = map.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(event.getLatitude(), event.getLongitude()))
                            .title(event.getName()));

            if (marker != null) {
                marker.setTag(event.getId());
            }
        }
    }
}

