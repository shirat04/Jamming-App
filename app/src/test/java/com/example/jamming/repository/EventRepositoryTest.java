package com.example.jamming.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.example.jamming.model.Event;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;
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
    @Mock DocumentReference mockAutoDoc;
    @Mock DocumentReference mockEventDoc;

    private EventRepository repo;

    @Before
    public void setUp() {
        repo = new EventRepository(mockDb);

        when(mockDb.collection("events")).thenReturn(mockEventsCollection);
        when(mockEventsCollection.document()).thenReturn(mockAutoDoc);
        when(mockEventsCollection.document(anyString())).thenReturn(mockEventDoc);
    }

    // createEvent
    @Test
    public void createEvent_success_setsIdAndWrites() {
        Event event = new Event(
                "owner1",
                "Jam Night",
                "Live music",
                List.of("Rock"),
                "Tel Aviv",
                System.currentTimeMillis() + 100000,
                100,
                32.0,
                34.8
        );

        when(mockAutoDoc.getId()).thenReturn("event-id-1");
        when(mockAutoDoc.set(any(Event.class)))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.createEvent(event);
        assertTrue(task.isComplete());

        assertTrue(task.isSuccessful());
        assertEquals("event-id-1", event.getId());

        verify(mockEventsCollection).document();
        verify(mockAutoDoc).set(event);
    }

    // getEventById
    @Test
    public void getEventById_success() throws Exception {
        DocumentSnapshot snap = mock(DocumentSnapshot.class);
        when(mockEventDoc.get()).thenReturn(Tasks.forResult(snap));

        Task<DocumentSnapshot> task = repo.getEventById("e1");
        assertTrue(task.isComplete());

        assertNotNull(task.getResult());
        verify(mockEventsCollection).document("e1");
        verify(mockEventDoc).get();
    }

    // updateEvent
    @Test
    public void updateEvent_success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "New Name");

        when(mockEventDoc.update(updates))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.updateEvent("e1", updates);
        assertTrue(task.isComplete());

        assertTrue(task.isSuccessful());
        verify(mockEventDoc).update(updates);
    }

    // deleteEvent
    @Test
    public void deleteEvent_success()  {
        when(mockEventDoc.delete())
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.deleteEvent("e1");
        assertTrue(task.isComplete());

        assertTrue(task.isSuccessful());
        verify(mockEventDoc).delete();
    }

    // decrementReserved
    @Test
    public void decrementReserved_updatesReserved() {
        when(mockEventDoc.update(eq("reserved"), any()))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.decrementReserved("e1");
        assertTrue(task.isComplete());

        assertTrue(task.isSuccessful());
        verify(mockEventDoc).update(eq("reserved"), any());
    }

}
