package com.example.jamming.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Map;

/**
 * Repository responsible for authentication and user account management.
 * Handles interactions with Firebase Authentication and basic user-related
 * queries in Firestore.
 *
 * Acts as an abstraction layer between ViewModels and Firebase services.
 */
public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    /**
     * Default constructor using Firebase singleton instances.
     */
    public AuthRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Constructor for dependency injection (mainly used for testing).
     *
     * @param auth FirebaseAuth instance
     * @param db Firestore instance
     */
    public AuthRepository(FirebaseAuth auth, FirebaseFirestore db) {
        this.auth = auth;
        this.db = db;
    }

    /**
     * Authenticates a user using email and password.
     *
     * @param email User email
     * @param pass User password
     * @return Task containing the authentication result
     */
    public Task<AuthResult> login(String email, String pass) {
        return auth.signInWithEmailAndPassword(email, pass);
    }

    /**
     * Signs out the currently authenticated user.
     */
    public void logout() {
        auth.signOut();
    }

    /**
     * Creates a new user account in Firebase Authentication.
     *
     * @param email User email
     * @param pass User password
     * @return Task containing the authentication result
     */
    public Task<AuthResult> createUser(String email, String pass) {
        return auth.createUserWithEmailAndPassword(email, pass);
    }

    /**
     * Saves the user's profile information in Firestore.
     *
     * @param uid User ID
     * @param data Map containing user profile fields and values
     * @return Task representing the save operation
     */
    public Task<Void> saveUserProfile(String uid, Map<String, Object> data) {
        return db.collection("users").document(uid).set(data);
    }

    /**
     * Retrieves a user's profile document from Firestore by UID.
     *
     * @param uid User ID
     * @return Task containing the user document snapshot
     */
    public Task<DocumentSnapshot> getUserUId(String uid) {
        return db.collection("users").document(uid).get();
    }

    /**
     * Checks whether a given username is already taken.
     *
     * @param username Username to check
     * @return Task containing the query result (empty if available)
     */
    public Task<QuerySnapshot> isUsernameTaken(String username) {
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get();
    }

    /**
     * Retrieves a user profile by username.
     *
     * @param username Username to search for
     * @return Task containing the query result
     */
    public Task<QuerySnapshot> getUserByUsername(String username) {
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get();
    }

    /**
     * Returns the UID of the currently authenticated user.
     *
     * @return User UID, or null if no user is logged in
     */
    public String getCurrentUid() {
        return (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
    }

    /**
     * Sends a password reset email to the given address.
     *
     * @param email User email
     * @return Task representing the password reset request
     */
    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    /**
     * Deletes the currently authenticated user's Firebase Auth account.
     * Note: This requires the user to have recently authenticated.
     * If the user hasn't authenticated recently, this will fail with
     * FirebaseAuthRecentLoginRequiredException.
     *
     * @return Task representing the delete operation
     */
    public Task<Void> deleteCurrentUser() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().delete();
        }
        return Tasks.forException(new Exception("No user logged in"));
    }

}