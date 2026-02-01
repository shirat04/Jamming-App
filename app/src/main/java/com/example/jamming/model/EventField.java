package com.example.jamming.model;

/**
 * Enumeration representing the editable fields of an event.
 * Used to identify and validate specific event attributes
 * in a type-safe manner.
 */
public enum EventField {

    /** Event title */
    TITLE,

    /** Event location */
    LOCATION,

    /** Event date */
    DATE,

    /** Event start time */
    TIME,

    /** Event music genre */
    GENRE,

    /** Maximum event capacity */
    CAPACITY,

    /** Event description */
    DESCRIPTION
}
