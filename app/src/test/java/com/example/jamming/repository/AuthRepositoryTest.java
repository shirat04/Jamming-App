package com.example.jamming.repository;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AuthRepositoryTest {

    @Mock
    FirebaseFirestore mockDb;

    @Mock
    CollectionReference mockUsersCollection;

    @Mock
    DocumentReference mockUserDoc;

    @Mock
    Query mockQuery;
    @Mock
    FirebaseAuth mockAuth;


    private AuthRepository repo;

    @Before
    public void setUp() {
        repo = new AuthRepository(mockAuth, mockDb);

        when(mockDb.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.document(any())).thenReturn(mockUserDoc);
        when(mockUsersCollection.whereEqualTo(eq("username"), any()))
                .thenReturn(mockQuery);
        when(mockQuery.limit(1)).thenReturn(mockQuery);
    }

    // ------------------------
    // saveUserProfile
    // ------------------------

    @Test
    public void saveUserProfile_success() {
        Map<String, Object> data = new HashMap<>();
        data.put("username", "testUser");

        when(mockUserDoc.set(data)).thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.saveUserProfile("uid1", data);

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc).set(data);
    }

    // ------------------------
    // getUserUId
    // ------------------------

    @Test
    public void getUserUId_success() {
        when(mockUserDoc.get())
                .thenReturn(Tasks.forResult(mock(DocumentSnapshot.class)));

        Task<DocumentSnapshot> task = repo.getUserUId("uid1");

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc).get();
    }

    // ------------------------
    // isUsernameTaken
    // ------------------------

    @Test
    public void isUsernameTaken_success() {
        when(mockQuery.get())
                .thenReturn(Tasks.forResult(mock(QuerySnapshot.class)));

        Task<QuerySnapshot> task = repo.isUsernameTaken("testUser");

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection)
                .whereEqualTo("username", "testUser");
        verify(mockQuery).limit(1);
        verify(mockQuery).get();
    }

    // ------------------------
    // getUserByUsername
    // ------------------------

    @Test
    public void getUserByUsername_success() {
        when(mockQuery.get())
                .thenReturn(Tasks.forResult(mock(QuerySnapshot.class)));

        Task<QuerySnapshot> task = repo.getUserByUsername("testUser");

        assertTrue(task.isSuccessful());
        verify(mockUsersCollection)
                .whereEqualTo("username", "testUser");
        verify(mockQuery).limit(1);
        verify(mockQuery).get();
    }
}
