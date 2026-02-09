package com.example.jamming.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.example.jamming.model.EventFilter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class UserRepositoryTest {

    @Mock
    FirebaseFirestore mockDb;

    @Mock
    CollectionReference mockUsersCollection;

    @Mock
    DocumentReference mockUserDoc;

    private UserRepository repo;

    @Before
    public void setUp() {
        repo = new UserRepository(mockDb);

        when(mockDb.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.document(anyString())).thenReturn(mockUserDoc);
    }

    // ------------------------
    // getUserById
    // ------------------------

    @Test
    public void getUserById_success() {
        when(mockUserDoc.get()).thenReturn(Tasks.forResult(mock(DocumentSnapshot.class)));

        Task<DocumentSnapshot> task = repo.getUserById("uid1");

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc).get();
    }

    // ------------------------
    // updateUserField
    // ------------------------

    @Test
    public void updateUserField_success() {
        when(mockUserDoc.update("field", "value")).thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.updateUserField("uid1", "field", "value");

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc).update("field", "value");
    }

    // ------------------------
    // updateUserProfile
    // ------------------------

    @Test
    public void updateUserProfile_success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "new name");

        when(mockUserDoc.update(updates)).thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.updateUserProfile("uid1", updates);

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc).update(updates);
    }

    // ------------------------
    // registerEventForUser
    // ------------------------

    @Test
    public void registerEventForUser_success() {
        when(mockUserDoc.update(eq("registeredEventIds"), any(FieldValue.class)))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.registerEventForUser("uid1", "event1");

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc)
                .update(eq("registeredEventIds"), any(FieldValue.class));
    }

    // ------------------------
    // unregisterEventForUser
    // ------------------------

    @Test
    public void unregisterEventForUser_success() {
        when(mockUserDoc.update(eq("registeredEventIds"), any(FieldValue.class)))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.unregisterEventForUser("uid1", "event1");

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc)
                .update(eq("registeredEventIds"), any(FieldValue.class));
    }

    // ------------------------
    // updateProfileImage
    // ------------------------

    @Test
    public void updateProfileImage_success() {
        when(mockUserDoc.update("profileImageUrl", "url"))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.updateProfileImage("uid1", "url");

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc).update("profileImageUrl", "url");
    }

    // ------------------------
    // updateNotificationsEnabled
    // ------------------------

    @Test
    public void updateNotificationsEnabled_success() {
        when(mockUserDoc.update("notificationsEnabled", true))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.updateNotificationsEnabled("uid1", true);

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc).update("notificationsEnabled", true);
    }

    // ------------------------
    // deleteUserProfile
    // ------------------------

    @Test
    public void deleteUserProfile_success() {
        when(mockUserDoc.delete())
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.deleteUserProfile("uid1");

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc).delete();
    }

    // ------------------------
    // getUserFullName (with spy)
    // ------------------------

    @Test
    public void getUserFullName_returnsFullName() {
        UserRepository spyRepo = spy(repo);

        doReturn(Tasks.forResult("Shirat Hutterer"))
                .when(spyRepo)
                .getUserFullName("uid1");

        Task<String> task = spyRepo.getUserFullName("uid1");

        assertTrue(task.isSuccessful());
        assertEquals("Shirat Hutterer", task.getResult());
    }

    // ------------------------
    // getLastEventFilter (with spy)
    // ------------------------

    @Test
    public void getLastEventFilter_returnsFilter() {
        EventFilter mockFilter = mock(EventFilter.class);

        UserRepository spyRepo = spy(repo);

        doReturn(Tasks.forResult(mockFilter))
                .when(spyRepo)
                .getLastEventFilter("uid1");

        Task<EventFilter> task = spyRepo.getLastEventFilter("uid1");

        assertTrue(task.isSuccessful());
        assertEquals(mockFilter, task.getResult());
    }

    // ------------------------
    // saveLastEventFilter
    // ------------------------

    @Test
    public void saveLastEventFilter_callsUpdateUserField() {
        UserRepository spyRepo = spy(repo);
        EventFilter filter = mock(EventFilter.class);

        doReturn(Tasks.forResult(null))
                .when(spyRepo)
                .updateUserField(eq("uid1"), eq("lastEventFilter"), eq(filter));

        spyRepo.saveLastEventFilter("uid1", filter);

        verify(spyRepo).updateUserField("uid1", "lastEventFilter", filter);
    }

    // ------------------------
    // getRegisteredEvents (with spy)
    // ------------------------

    @Test
    public void getRegisteredEvents_returnsList() {
        List<String> fakeEvents = List.of("e1", "e2");

        UserRepository spyRepo = spy(repo);

        doReturn(Tasks.forResult(fakeEvents))
                .when(spyRepo)
                .getRegisteredEvents("uid1");

        Task<List<String>> task = spyRepo.getRegisteredEvents("uid1");

        assertTrue(task.isSuccessful());
        assertEquals(2, task.getResult().size());
        assertEquals("e1", task.getResult().get(0));
        assertEquals("e2", task.getResult().get(1));
    }
}
