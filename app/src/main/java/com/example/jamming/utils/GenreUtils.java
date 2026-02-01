package com.example.jamming.utils;

import java.util.List;

public class GenreUtils {
    public static String genresToText(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return "No genre specified";
        }
        return String.join(" , ", genres);
    }
}
