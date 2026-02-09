package com.example.jamming.model;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EventFilterEngineTest {

    private Event baseEventFutureActive() {
        List<String> genres = new ArrayList<>();
        genres.add("Rock");

        Event e = new Event(
                "owner1",
                "Jam Night",
                "Live music",
                genres,
                "Tel Aviv",
                System.currentTimeMillis() + 60_000,
                100,
                32.0,
                34.8
        );

        e.setActive(true);
        e.setReserved(0);

        return e;
    }

    // ------------------------
    // filter = null
    // ------------------------

    @Test
    public void filter_null_returnsOriginalList() {
        List<Event> events = new ArrayList<>();
        events.add(baseEventFutureActive());
        events.add(baseEventFutureActive());

        List<Event> result = EventFilterEngine.filter(events, null);

        assertEquals(2, result.size());
    }

    // ------------------------
    // inactive event
    // ------------------------

    @Test
    public void filter_excludesInactiveEvents() {
        Event e = baseEventFutureActive();
        e.setActive(false);

        EventFilter filter = new EventFilter();

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertTrue(result.isEmpty());
    }

    // ------------------------
    // past event
    // ------------------------

    @Test
    public void filter_excludesPastEvents() {
        Event e = baseEventFutureActive();
        e.setDateTime(System.currentTimeMillis() - 60_000);

        EventFilter filter = new EventFilter();

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertTrue(result.isEmpty());
    }

    // ------------------------
    // genre filter
    // ------------------------

    @Test
    public void filter_byGenre_matches() {
        Event e = baseEventFutureActive();

        EventFilter filter = new EventFilter();
        filter.getMusicTypes().add(MusicGenre.ROCK);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertEquals(1, result.size());
    }

    @Test
    public void filter_byGenre_noMatch_excluded() {
        Event e = baseEventFutureActive();

        EventFilter filter = new EventFilter();
        filter.getMusicTypes().add(MusicGenre.JAZZ);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertTrue(result.isEmpty());
    }

    @Test
    public void filter_byGenre_unknownGenreIgnored() {
        Event e = baseEventFutureActive();
        List<String> types = new ArrayList<>();
        types.add("Rock");
        types.add("UnknownGenre");
        e.setMusicTypes(types);

        EventFilter filter = new EventFilter();
        filter.getMusicTypes().add(MusicGenre.ROCK);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertEquals(1, result.size());
    }

    // ------------------------
    // radius filter
    // ------------------------

    @Test
    public void filter_byRadius_insideRadius_included() {
        Event e = baseEventFutureActive();

        EventFilter filter = new EventFilter();
        filter.setLocation(32.0, 34.8, 5);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertEquals(1, result.size());
    }

    @Test
    public void filter_byRadius_outsideRadius_excluded() {
        Event e = baseEventFutureActive();
        e.setLatitude(0.0);
        e.setLongitude(0.0);

        EventFilter filter = new EventFilter();
        filter.setLocation(32.0, 34.8, 1);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertTrue(result.isEmpty());
    }

    // ------------------------
    // date range filter
    // ------------------------

    @Test
    public void filter_byDateRange_insideRange_included() {
        Event e = baseEventFutureActive();
        long t = e.getDateTime();

        EventFilter filter = new EventFilter();
        filter.setDateRange(t - 1000, t + 1000);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertEquals(1, result.size());
    }

    @Test
    public void filter_byDateRange_outsideRange_excluded() {
        Event e = baseEventFutureActive();
        long t = e.getDateTime();

        EventFilter filter = new EventFilter();
        filter.setDateRange(t + 10_000, t + 20_000);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertTrue(result.isEmpty());
    }

    // ------------------------
    // time range filter
    // ------------------------

    @Test
    public void filter_byTimeRange_sameDay_match() {
        Event e = baseEventFutureActive();

        EventFilter filter = new EventFilter();
        filter.setTimeRange(0, 24 * 60);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertEquals(1, result.size());
    }

    @Test
    public void filter_byTimeRange_overnight_doesNotCrash() {
        Event e = baseEventFutureActive();

        EventFilter filter = new EventFilter();
        filter.setTimeRange(23 * 60, 3 * 60); // overnight

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertNotNull(result);
    }

    // ------------------------
    // available spots filter
    // ------------------------

    @Test
    public void filter_byAvailableSpots_minMax_match() {
        Event e = baseEventFutureActive();
        e.setReserved(90);

        EventFilter filter = new EventFilter();
        filter.setAvailableSpotsRange(5, 20);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertEquals(1, result.size());
    }

    @Test
    public void filter_byAvailableSpots_tooFew_excluded() {
        Event e = baseEventFutureActive();
        e.setReserved(98);

        EventFilter filter = new EventFilter();
        filter.setAvailableSpotsRange(5, null);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertTrue(result.isEmpty());
    }

    // ------------------------
    // capacity filter
    // ------------------------

    @Test
    public void filter_byCapacity_minMax_match() {
        Event e = baseEventFutureActive();

        EventFilter filter = new EventFilter();
        filter.setCapacityRange(50, 200);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertEquals(1, result.size());
    }

    @Test
    public void filter_byCapacity_tooSmall_excluded() {
        List<String> genres = new ArrayList<>();
        genres.add("Rock");

        Event e = new Event(
                "owner1",
                "Small Event",
                "Small",
                genres,
                "Somewhere",
                System.currentTimeMillis() + 60_000,
                10,
                32.0,
                34.8
        );

        e.setActive(true);
        e.setReserved(0);

        EventFilter filter = new EventFilter();
        filter.setCapacityRange(50, null);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertTrue(result.isEmpty());
    }

    // ------------------------
    // combined filters
    // ------------------------

    @Test
    public void filter_combined_allMatch_included() {
        Event e = baseEventFutureActive();
        e.setReserved(90);

        EventFilter filter = new EventFilter();
        filter.getMusicTypes().add(MusicGenre.ROCK);
        filter.setAvailableSpotsRange(5, 20);
        filter.setCapacityRange(50, 200);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertEquals(1, result.size());
    }

    @Test
    public void filter_combined_oneFails_excluded() {
        Event e = baseEventFutureActive();
        e.setReserved(99);

        EventFilter filter = new EventFilter();
        filter.getMusicTypes().add(MusicGenre.ROCK);
        filter.setAvailableSpotsRange(5, 20);

        List<Event> result = EventFilterEngine.filter(List.of(e), filter);

        assertTrue(result.isEmpty());
    }
}
