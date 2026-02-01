package com.example.jamming.utils;

import java.util.List;

/**
 * Utility class for formatting music genre data.
 * Provides helper methods for converting genre lists
 * into user-friendly text representations.
 */
public class GenreUtils {

    /**
     * Converts a list of genre names into a single formatted string.
     *
     * @param genres List of genre names
     * @return Comma-separated string of genres, or a default message if empty
     */
    public static String genresToText(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return "No genre specified";
        }
        return String.join(" , ", genres);
    }
}
