package com.example.jamming.Repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {
        private final FirebaseAuth auth;

        public AuthRepository() {
            // את מאתחלת את FirebaseAuth כאן
            auth = FirebaseAuth.getInstance();
        }

        // פונקציית התחברות
        public Task<AuthResult> login(String email, String password) {
            return auth.signInWithEmailAndPassword(email, password);
        }

        // פונקציית הרשמה
        public Task<AuthResult> register(String email, String password) {
            return auth.createUserWithEmailAndPassword(email, password);
        }

        // מחזיר את המשתמש הנוכחי
        public FirebaseUser getCurrentUser() {
            return auth.getCurrentUser();
        }

        // התנתקות
        public void logout() {
            auth.signOut();
        }
    }

