package com.example.jamming.repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public AuthRepository(FirebaseAuth auth, FirebaseFirestore db) {
        this.auth = auth;
        this.db = db;
    }

    // Login with email
    public Task<AuthResult> login(String email, String pass) {
        return auth.signInWithEmailAndPassword(email, pass);
    }
    public void logout() {
        auth.signOut();
    }

    // Create user in FirebaseAuth
    public Task<AuthResult> createUser(String email, String pass) {
        return auth.createUserWithEmailAndPassword(email, pass);
    }

    // Save user profile in Firestore
    public Task<Void> saveUserProfile(String uid, Map<String, Object> data) {
        return db.collection("users").document(uid).set(data);
    }

    // Get profile from Firestore
    public Task<DocumentSnapshot> getUserUId(String uid) {
        return db.collection("users").document(uid).get();
    }

    public Task<QuerySnapshot> isUsernameTaken(String username) {
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get();
    }

    public Task<QuerySnapshot> getUserByUsername(String username) {
        return db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get();
    }

    // Returns current user UID
    public String getCurrentUid() {
        return (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
    }


    public Task<Void> sendPasswordResetEmail(String email) {
        return FirebaseAuth.getInstance().sendPasswordResetEmail(email);
    }

}