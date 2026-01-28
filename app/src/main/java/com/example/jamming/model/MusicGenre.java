package com.example.jamming.model;

/**
 * Enum that represents all supported music genres in the app.
 * Each enum value has a user-friendly display name.
 */
public enum MusicGenre {

    ROCK("Rock"),
    JAZZ("Jazz"),
    POP("Pop"),
    HIP_HOP("Hip Hop"),
    ELECTRONIC("Electronic"),
    OPENMIC("Open Mic"),
    CLASSICAL("Classical");

    // Human-readable name shown in the UI
    private final String displayName;

    // Constructor for assigning display name
    MusicGenre(String displayName) {
        this.displayName = displayName;
    }

    // Returns the name used for display (UI, Firestore, etc.)
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a display name (String) back to a MusicGenre enum.
     * Used when reading data from Firestore.
     */
    public static MusicGenre fromDisplayName(String value) {
        for (MusicGenre g : values()) {
            if (g.displayName.equalsIgnoreCase(value)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Unknown genre: " + value);
    }
}
