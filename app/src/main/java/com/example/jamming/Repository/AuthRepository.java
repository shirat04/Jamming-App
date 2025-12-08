package com.example.jamming.Repository;
import com.example.jamming.model.Event;
import com.example.jamming.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth auth;        //Firebase authentication mechanism
    private final FirebaseFirestore db;     //Accessing the Firestore database

    // Initialize Firebase Auth and Firestore (Singleton instances)
    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Registers a new user using email, password and username.
     * Flow:
     * 1. Check if username already exists.
     * 2. Create Firebase Auth user.
     * 3. Update User object with UID and email.
     * 4. Store user profile in Firestore.
     */
    public Task<AuthResult> register(String email, String password, String username, User user) {

        return isUsernameTaken(username).onSuccessTask(isTaken -> {

            if (isTaken) {
                return Tasks.forException(new Exception("Username already exists"));
            }

            return auth.createUserWithEmailAndPassword(email, password)
                    .onSuccessTask(result -> {

                        FirebaseUser fbUser = result.getUser();

                        if (fbUser == null) {
                            return Tasks.forException(new Exception("Registration failed"));
                        }

                        user.setFirebaseId(fbUser.getUid());
                        user.setEmail(email);
                        user.setUsername(username);

                        return createUserProfile(fbUser.getUid(), user)
                                .onSuccessTask(v -> Tasks.forResult(result));
                    });
        });
    }

    //Stores a complete user profile in Firestore under the user UID.
    public Task<Void> createUserProfile(String uid, User user) {
        return db.collection("users")
                .document(uid)
                .set(user);
    }

    /**
     * Logs in using either email OR username.
     * If the identifier is a valid email, authenticate directly.
     * Otherwise, treat it as a username and fetch its corresponding email from Firestore.
     */
    public Task<AuthResult> login(String identifier, String password) {

        if (isEmail(identifier)) {
            return auth.signInWithEmailAndPassword(identifier, password);
        }

        return db.collection("users")
                .whereEqualTo("username", identifier)
                .limit(1)
                .get()
                .onSuccessTask(query -> {

                    if (query.isEmpty()) {
                        return Tasks.forException(new Exception("Username not found"));
                    }

                    DocumentSnapshot doc = query.getDocuments().get(0);
                    String email = doc.getString("email");

                    if (email == null) {
                        return Tasks.forException(new Exception("Email not found for username"));
                    }

                    return auth.signInWithEmailAndPassword(email, password);
                });
    }


    //Checks if the given value conforms to a valid email pattern.
    private boolean isEmail(String input) {
        return Patterns.EMAIL_ADDRESS.matcher(input).matches();
    }


    //Checks whether a username already exists in Firestore.
    public Task<Boolean> isUsernameTaken(String username) {
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .continueWith(task -> !task.getResult().isEmpty());
    }

    //Retrieves a user profile document from Firestore.
    public Task<DocumentSnapshot> getUserProfile(String uid) {
        return db.collection("users")
                .document(uid)
                .get();
    }


    //Updates one or more fields in the user's Firestore profile.
    public Task<Void> updateUserProfile(String uid, Map<String, Object> updates) {
        return db.collection("users")
                .document(uid)
                .update(updates);
    }

    //Deletes the user's Firestore profile document.
    public Task<Void> deleteUserProfile(String uid) {
        return db.collection("users")
                .document(uid)
                .delete();
    }

    //Sends a password-reset email to the user.
    public Task<Void> resetPassword(String email) {
        return auth.sendPasswordResetEmail(email);
    }


    //Sends an email verification message to the current Firebase user.
    public Task<Void> sendEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.sendEmailVerification();
        }
        return null;
    }

    //Returns the currently authenticated Firebase user, if exists.
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    //Logs out the current user.
    public void logout() {
        auth.signOut();
    }

}