package com.example.jamming.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.repository.AuthRepository;

public class LoginViewModel extends ViewModel {


    private final AuthRepository repo = new AuthRepository();
    public MutableLiveData<String> message = new MutableLiveData<>();
    public MutableLiveData<String> userType = new MutableLiveData<>();

    public void login(String identifier, String password) {

        if (identifier.isEmpty() || password.isEmpty()) {
            message.setValue("נא למלא את כל השדות");
            return;
        }

        repo.getUserByUsername(identifier)
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        message.setValue("שם המשתמש או סיסמה אינם נכונים");
                        return;
                    }

                    String email = query.getDocuments().get(0).getString("email");
                    if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        message.setValue("כתובת האימייל של המשתמש אינה תקינה");
                        return;
                    }

                    repo.login(email.trim(), password)
                            .addOnSuccessListener(auth ->
                                    checkUserType(repo.getCurrentUid()))
                            .addOnFailureListener(e ->
                                    message.setValue("שם משתמש או סיסמה אינם נכונים"));
                })
                .addOnFailureListener(e ->
                        message.setValue("שגיאה בהתחברות"));

    }
    private void checkUserType(String uid) {
            repo.getUserUId(uid).addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                            return;}
                        String type = doc.getString("userType");
                        userType.setValue(type);
                    })
                    .addOnFailureListener(e -> message.setValue(e.getMessage()));
        }
    public LiveData<String> getMessage() {
        return message;
    }
    public LiveData<String> getUserType() {
        return userType;
    }

    public void resetPassword(String identifier) {

        if (identifier == null || identifier.trim().isEmpty()) {
            message.setValue("נא להזין שם משתמש");
            return;
        }

        repo.getUserByUsername(identifier)
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        message.setValue("שם המשתמש לא קיים");
                        return;
                    }

                    String email = query.getDocuments().get(0).getString("email");
                    if (email == null) {
                        message.setValue("שגיאה בפרטי המשתמש");
                        return;
                    }

                    repo.sendPasswordResetEmail(email)
                            .addOnSuccessListener(v ->
                                    message.setValue("נשלח מייל לאיפוס סיסמה"))
                            .addOnFailureListener(e ->
                                    message.setValue("שגיאה בשליחת המייל"));
                })
                .addOnFailureListener(e ->
                        message.setValue("שגיאה באיתור המשתמש"));
    }
    public void clearMessage() {
        message.setValue(null);
    }


}