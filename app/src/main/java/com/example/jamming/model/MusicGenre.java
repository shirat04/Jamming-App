package com.example.jamming.model;

/**
 * Enumeration representing all supported music genres in the application.
 * Each genre is associated with a user-friendly display name used in the UI
 * and for data storage.
 */
public enum MusicGenre {

    ROCK("Rock"),
    JAZZ("Jazz"),
    POP("Pop"),
    HIP_HOP("Hip Hop"),
    ELECTRONIC("Electronic"),
    OPENMIC("Open Mic"),
    CLASSICAL("Classical");

    // Human-readable name used for display and persistence
    private final String displayName;

    /**
     * Constructs a music genre with its display name.
     *
     * @param displayName User-friendly name of the genre
     */
    MusicGenre(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name of the genre.
     *
     * @return Display name used in the UI and Firestore
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a display name back to its corresponding enum value.
     * This method is typically used when reading data from Firestore.
     *
     * @param value Display name to convert
     * @return Matching MusicGenre enum value
     * @throws IllegalArgumentException If the display name is not recognized
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
