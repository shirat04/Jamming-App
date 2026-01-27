package com.example.jamming.model;

public enum MusicGenre {

    ROCK("Rock"),
    JAZZ("Jazz"),
    POP("Pop"),
    HIP_HOP("Hip Hop"),
    ELECTRONIC("Electronic"),
    OPENMIC("Open Mic"),
    CLASSICAL("Classical");


    private final String displayName;

    MusicGenre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MusicGenre fromDisplayName(String value) {
        for (MusicGenre g : values()) {
            if (g.displayName.equalsIgnoreCase(value)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Unknown genre: " + value);
    }
}
