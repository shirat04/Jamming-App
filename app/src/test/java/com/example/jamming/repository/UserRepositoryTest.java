package com.example.jamming.repository;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // getUserById
    @Test
    public void getUserById_success() {
        when(mockUserDoc.get()).thenReturn(Tasks.forResult(mock(DocumentSnapshot.class)));

        Task<DocumentSnapshot> task = repo.getUserById("uid1");

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc).get();
    }

    // updateUserField
    @Test
    public void updateUserField_success() {
        when(mockUserDoc.update("field", "value")).thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.updateUserField("uid1", "field", "value");

        assertTrue(task.isSuccessful());
        verify(mockUserDoc).update("field", "value");
    }

    // updateUserProfile
    @Test
    public void updateUserProfile_success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("age", 25);

        when(mockUserDoc.update(updates)).thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.updateUserProfile("uid1", updates);

        assertTrue(task.isSuccessful());
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
        verify(mockUserDoc).update("profileImageUrl", "url");
    }

    // ------------------------
    // updateSearchRadius
    // ------------------------

    @Test
    public void updateSearchRadius_success() {
        when(mockUserDoc.update("searchRadiusKm", 10))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.updateSearchRadius("uid1", 10);

        assertTrue(task.isSuccessful());
        verify(mockUserDoc).update("searchRadiusKm", 10);
    }

    // ------------------------
    // updateFavoriteMusicTypes
    // ------------------------

    @Test
    public void updateFavoriteMusicTypes_success() {
        List<String> types = List.of("Rock");

        when(mockUserDoc.update("favoriteMusicTypes", types))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.updateFavoriteMusicTypes("uid1", types);

        assertTrue(task.isSuccessful());
        verify(mockUserDoc).update("favoriteMusicTypes", types);
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
        verify(mockUserDoc).delete();
    }
}
