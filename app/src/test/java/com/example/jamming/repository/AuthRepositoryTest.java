package com.example.jamming.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    @Mock
    FirebaseUser mockUser;

    private AuthRepository repo;

    @Before
    public void setUp() {
        repo = new AuthRepository(mockAuth, mockDb);

        when(mockDb.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.document(eq("uid1"))).thenReturn(mockUserDoc);
        when(mockUsersCollection.whereEqualTo(eq("username"), any()))
                .thenReturn(mockQuery);
        when(mockQuery.limit(1)).thenReturn(mockQuery);
    }

    // ------------------------
    // Auth methods
    // ------------------------

    @Test
    public void logout_callsSignOut() {
        repo.logout();
        verify(mockAuth).signOut();
    }

    @Test
    public void login_callsFirebaseAuth() {
        when(mockAuth.signInWithEmailAndPassword("a@b.com", "1234"))
                .thenReturn(Tasks.forResult((AuthResult) null));

        Task<AuthResult> task = repo.login("a@b.com", "1234");

        verify(mockAuth).signInWithEmailAndPassword("a@b.com", "1234");
        assertTrue(task.isSuccessful());
    }

    @Test
    public void createUser_callsFirebaseAuth() {
        when(mockAuth.createUserWithEmailAndPassword("a@b.com", "1234"))
                .thenReturn(Tasks.forResult((AuthResult) null));

        Task<AuthResult> task = repo.createUser("a@b.com", "1234");

        verify(mockAuth).createUserWithEmailAndPassword("a@b.com", "1234");
        assertTrue(task.isSuccessful());
    }

    @Test
    public void sendPasswordResetEmail_callsFirebaseAuth() {
        when(mockAuth.sendPasswordResetEmail("a@b.com"))
                .thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.sendPasswordResetEmail("a@b.com");

        verify(mockAuth).sendPasswordResetEmail("a@b.com");
        assertTrue(task.isSuccessful());
    }

    // ------------------------
    // getCurrentUid
    // ------------------------

    @Test
    public void getCurrentUid_whenUserLoggedIn_returnsUid() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid123");

        String uid = repo.getCurrentUid();

        assertEquals("uid123", uid);
    }

    @Test
    public void getCurrentUid_whenNoUser_returnsNull() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        String uid = repo.getCurrentUid();

        assertNull(uid);
    }

    // ------------------------
    // deleteCurrentUser
    // ------------------------

    @Test
    public void deleteCurrentUser_whenUserExists_callsDelete() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.delete()).thenReturn(Tasks.forResult(null));

        Task<Void> task = repo.deleteCurrentUser();

        verify(mockUser).delete();
        assertTrue(task.isSuccessful());
    }

    @Test
    public void deleteCurrentUser_whenNoUser_returnsFailureTask() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        Task<Void> task = repo.deleteCurrentUser();

        assertFalse(task.isSuccessful());
        assertNotNull(task.getException());
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
        DocumentSnapshot snapshot = org.mockito.Mockito.mock(DocumentSnapshot.class);
        when(mockUserDoc.get()).thenReturn(Tasks.forResult(snapshot));

        Task<DocumentSnapshot> task = repo.getUserUId("uid1");

        assertTrue(task.isSuccessful());
        assertNotNull(task.getResult());
        verify(mockUsersCollection).document("uid1");
        verify(mockUserDoc).get();
    }

    // ------------------------
    // isUsernameTaken
    // ------------------------

    @Test
    public void isUsernameTaken_success() {
        QuerySnapshot qs = org.mockito.Mockito.mock(QuerySnapshot.class);
        when(mockQuery.get()).thenReturn(Tasks.forResult(qs));

        Task<QuerySnapshot> task = repo.isUsernameTaken("testUser");

        assertTrue(task.isSuccessful());
        assertNotNull(task.getResult());
        verify(mockUsersCollection).whereEqualTo("username", "testUser");
        verify(mockQuery).limit(1);
        verify(mockQuery).get();
    }

    // ------------------------
    // getUserByUsername
    // ------------------------

    @Test
    public void getUserByUsername_success() {
        QuerySnapshot qs = org.mockito.Mockito.mock(QuerySnapshot.class);
        when(mockQuery.get()).thenReturn(Tasks.forResult(qs));

        Task<QuerySnapshot> task = repo.getUserByUsername("testUser");

        assertTrue(task.isSuccessful());
        assertNotNull(task.getResult());
        verify(mockUsersCollection).whereEqualTo("username", "testUser");
        verify(mockQuery).limit(1);
        verify(mockQuery).get();
    }
}
