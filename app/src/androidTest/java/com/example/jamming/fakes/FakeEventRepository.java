package com.example.jamming.fakes;

import com.example.jamming.model.Event;
import com.example.jamming.repository.EventRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

/**
 * Generic fake EventRepository for UI tests across multiple screens.
 * By configuring the behavior, you can simulate success/failure for repository calls.
 */
public class FakeEventRepository extends EventRepository {

    private boolean shouldFail = false;
    private Exception failure = new Exception("Repository operation failed");

    // Optional: store last event passed to createEvent for assertions
    public Event lastCreatedEvent = null;

    public FakeEventRepository() {}

    public FakeEventRepository failWith(Exception e) {
        this.shouldFail = true;
        this.failure = (e != null) ? e : new Exception("Repository operation failed");
        return this;
    }

    public FakeEventRepository succeed() {
        this.shouldFail = false;
        return this;
    }

    @Override
    public Task<Void> createEvent(Event event) {
        lastCreatedEvent = event;
        if (shouldFail) return Tasks.forException(failure);
        return Tasks.forResult(null);
    }

    /*
     * If later you add update/delete methods in EventRepository,
     * override them here in the same style:
     *
     * public Task<Void> updateEvent(Event event) { ... }
     * public Task<Void> deleteEvent(String id) { ... }
     */
}
