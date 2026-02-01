package com.example.jamming.utils;

import com.example.jamming.model.MusicGenre;

import java.util.ArrayList;
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

    /**
     * Converts a list of MusicGenre enums into a formatted text string.
     *
     * @param genres List of MusicGenre values
     * @return Comma-separated string of genre display names
     */
    public static String genresToTextFromEnums(List<MusicGenre> genres) {
        if (genres == null || genres.isEmpty()) {
            return "No genre specified";
        }

        List<String> names = new ArrayList<>();
        for (MusicGenre g : genres) {
            names.add(g.getDisplayName());
        }

        return genresToText(names);
    }

    /**
     * Converts a list of MusicGenre enums to a list of display-name strings.
     */
    public static List<String> genresToStrings(List<MusicGenre> genres) {
        List<String> result = new ArrayList<>();
        if (genres == null) return result;

        for (MusicGenre g : genres) {
            result.add(g.getDisplayName());
        }
        return result;
    }
}
