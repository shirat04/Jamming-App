package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.repository.AuthRepository;
import com.example.jamming.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import java.util.HashMap;
import java.util.Map;

public class LoginViewModel extends ViewModel {


    private final AuthRepository authRepo;
    private final UserRepository userRepo;

    public MutableLiveData<String> identifierError = new MutableLiveData<>();
    public MutableLiveData<String> passwordError = new MutableLiveData<>();
    public MutableLiveData<String> error = new MutableLiveData<>();

    public MutableLiveData<String> userType = new MutableLiveData<>();
    public LoginViewModel() {
        authRepo = new AuthRepository();
        userRepo = new UserRepository();
    }
    public void login(String identifier, String password) {

        boolean hasError = false;

        if (identifier == null || identifier.isEmpty()) {
            identifierError.setValue("חובה להזין שם משתמש או אימייל");
            hasError = true;
        } else {
            identifierError.setValue(null);
        }

        if (password == null || password.isEmpty()) {
            passwordError.setValue("חובה להזין סיסמה");
            hasError = true;
        } else {
            passwordError.setValue(null);
        }

        if (hasError) return;


        if (identifier.contains("@")) {
        // אימייל
        authRepo.login(identifier, password)
                .addOnSuccessListener(auth -> checkUserType( authRepo.getCurrentUid()))
                .addOnFailureListener(e -> {

                    String msg = e.getMessage();

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        error.setValue("שם משתמש או סיסמה אינם נכונים");
                    } else {
                        error.setValue("שגיאה בהתחברות, נסה שוב");
                    }
                });


    } else {
        authRepo.getUserByUsername(identifier)
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        identifierError.setValue("שם המשתמש לא קיים");
                        return;
                    }

                    String email = query.getDocuments().get(0).getString("email");

                    authRepo.login(email, password)
                            .addOnSuccessListener(auth -> checkUserType( authRepo.getCurrentUid()))
                            .addOnFailureListener(e -> error.setValue("שם משתמש או סיסמה אינם נכונים"));
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }
}
    private void checkUserType(String uid) {

        if (uid == null) {
            error.setValue("משתמש לא מחובר");
            return;
        }
        userRepo.getUserById(uid)
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        error.setValue("פרופיל המשתמש לא קיים");
                        return;
                    }

                    String type = doc.getString("userType");

                    userType.setValue(type);

                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }
    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getUserType() {
        return userType;
    }


}