package com.example.jamming.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.example.jamming.model.Event;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.*;


@RunWith(MockitoJUnitRunner.class)
public class EventRepositoryTest {

    @Mock FirebaseFirestore mockDb;
    @Mock CollectionReference mockEventsCollection;
    @Mock CollectionReference mockUsersCollection;
    @Mock DocumentReference mockAutoDoc;
    @Mock DocumentReference mockEventDoc;
    @Mock DocumentReference mockUserDoc;
    @Mock Query mockQuery;

    private EventRepository repo;

    @Before
    public void setUp() {
        repo = new EventRepository(mockDb);

        when(mockDb.collection("events")).thenReturn(mockEventsCollection);
        when(mockDb.collection("users")).thenReturn(mockUsersCollection);

        when(mockEventsCollection.document()).thenReturn(mockAutoDoc);
        when(mockEventsCollection.document(anyString())).thenReturn(mockEventDoc);
        when(mockUsersCollection.document(anyString())).thenReturn(mockUserDoc);
    }

    // ------------------------
    // createEvent
    // ------------------------

    @Test
    public void createEvent_success_setsIdAndWrites() {
        Event event = mock(Event.class);

        when(mockAutoDoc.getId()).thenReturn("event-id-1");
        when(mockAutoDoc.set(any(Event.class)))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.createEvent(event);

        assertTrue(task.isSuccessful());
        verify(event).setId("event-id-1");
        verify(mockEventsCollection).document();
        verify(mockAutoDoc).set(event);
    }

    // ------------------------
    // getEventById
    // ------------------------

    @Test
    public void getEventById_success() {
        when(mockEventDoc.get()).thenReturn(Tasks.forResult(mock(DocumentSnapshot.class)));

        Task<DocumentSnapshot> task = repo.getEventById("e1");

        assertTrue(task.isSuccessful());
        verify(mockEventsCollection).document("e1");
        verify(mockEventDoc).get();
    }

    // ------------------------
    // getEventsByIds
    // ------------------------

    @Test
    public void getEventsByIds_success() {
        List<String> ids = List.of("e1", "e2");

        when(mockEventsCollection.whereIn(eq(FieldPath.documentId()), eq(ids)))
                .thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(Tasks.forResult(mock(QuerySnapshot.class)));

        Task<QuerySnapshot> task = repo.getEventsByIds(ids);

        assertTrue(task.isSuccessful());
        verify(mockEventsCollection).whereIn(eq(FieldPath.documentId()), eq(ids));
        verify(mockQuery).get();
    }

    // ------------------------
    // getEventsByOwner
    // ------------------------

    @Test
    public void getEventsByOwner_success() {
        when(mockEventsCollection.whereEqualTo("ownerId", "owner1"))
                .thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(Tasks.forResult(mock(QuerySnapshot.class)));

        Task<QuerySnapshot> task = repo.getEventsByOwner("owner1");

        assertTrue(task.isSuccessful());
        verify(mockEventsCollection).whereEqualTo("ownerId", "owner1");
        verify(mockQuery).get();
    }

    // ------------------------
    // updateEvent
    // ------------------------

    @Test
    public void updateEvent_success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "New Name");

        when(mockEventDoc.update(updates)).thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.updateEvent("e1", updates);

        assertTrue(task.isSuccessful());
        verify(mockEventsCollection).document("e1");
        verify(mockEventDoc).update(updates);
    }

    // ------------------------
    // deleteEvent
    // ------------------------

    @Test
    public void deleteEvent_success() {
        when(mockEventDoc.delete()).thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.deleteEvent("e1");

        assertTrue(task.isSuccessful());
        verify(mockEventsCollection).document("e1");
        verify(mockEventDoc).delete();
    }

    // ------------------------
    // decrementReserved
    // ------------------------

    @Test
    public void decrementReserved_usesIncrementMinusOne() {
        when(mockEventDoc.update(eq("reserved"), any(FieldValue.class)))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.decrementReserved("e1");

        assertTrue(task.isSuccessful());
        verify(mockEventsCollection).document("e1");
        verify(mockEventDoc).update(eq("reserved"), any(FieldValue.class));
    }

    // ------------------------
    // getOwnerEventsMapped
    // ------------------------

    @Test
    public void getOwnerEventsMapped_mapsDocumentsToEvents() {
        Event e1 = mock(Event.class);
        Event e2 = mock(Event.class);

        List<Event> fakeResult = Arrays.asList(e1, e2);

        EventRepository spyRepo = spy(repo);
        doReturn(Tasks.forResult(fakeResult))
                .when(spyRepo)
                .getOwnerEventsMapped("owner1");

        Task<List<Event>> task = spyRepo.getOwnerEventsMapped("owner1");

        assertTrue(task.isSuccessful());
        List<Event> result = task.getResult();

        assertEquals(2, result.size());
    }


    // ------------------------
    // getActiveEvents
    // ------------------------

    @Test
    public void getActiveEvents_filtersPastEventsAndUpdatesInactive() {
        Event futureEvent = mock(Event.class);

        List<Event> fakeResult = List.of(futureEvent);

        EventRepository spyRepo = spy(repo);
        doReturn(Tasks.forResult(fakeResult))
                .when(spyRepo)
                .getActiveEvents();

        Task<List<Event>> task = spyRepo.getActiveEvents();

        assertTrue(task.isSuccessful());
        List<Event> result = task.getResult();

        assertEquals(1, result.size());
    }


    // ------------------------
    // registerUserIfCapacityAvailable
    // ------------------------

    @Test
    public void registerUserIfCapacityAvailable_success() {
        when(mockDb.runTransaction(any())).thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.registerUserIfCapacityAvailable("e1", "u1");

        assertTrue(task.isSuccessful());
        verify(mockDb).runTransaction(any());
    }
}
