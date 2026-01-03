package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.repository.AuthRepository;
import java.util.HashMap;
import java.util.Map;

public class LoginViewModel extends ViewModel {


    private final AuthRepository repo = new AuthRepository();
    public MutableLiveData<String> message = new MutableLiveData<>();
    public MutableLiveData<String> userType = new MutableLiveData<>();

    public void login(String identifier, String password) {

        if (identifier.isEmpty() || password.isEmpty()) {
            message.setValue("נא למלא את כל השדות");
            return;
        }

        if (identifier.contains("@")) {
            repo.login(identifier, password)
                    .addOnSuccessListener(auth -> checkUserType(repo.getCurrentUid()))
                    .addOnFailureListener(e -> {

                        String msg = e.getMessage();

                        if (msg != null && msg.contains("credential")) {
                            message.setValue("שם משתמש או סיסמה אינם נכונים");
                        } else {
                            message.setValue("שגיאה בהתחברות, נסה שוב");
                        }
                    });


        } else {
            repo.getUserByUsername(identifier)
                    .addOnSuccessListener(query -> {

                        if (query.isEmpty()) {
                            message.setValue("שם המשתמש או סיסמה אינם נכונים");
                            return;
                        }

                        String email = query.getDocuments().get(0).getString("email");

                        repo.login(email, password)
                                .addOnSuccessListener(auth -> checkUserType(repo.getCurrentUid()))
                                .addOnFailureListener(e -> message.setValue("שם משתמש או סיסמה אינם נכונים"));
                    })
                    .addOnFailureListener(e -> message.setValue(e.getMessage()));
        }
    }
    private void checkUserType(String uid) {
            repo.getUserUId(uid).addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    message.setValue("פרופיל המשתמש לא קיים");
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
            message.setValue("נא להזין אימייל או שם משתמש");
            return;
        }
        if (identifier.contains("@")) {
            repo.sendPasswordResetEmail(identifier)
                    .addOnSuccessListener(aVoid ->
                            message.setValue("נשלח מייל לאיפוס סיסמה"))
                    .addOnFailureListener(e ->
                            message.setValue("שגיאה בשליחת המייל"));
        }
        else {
            repo.getUserByUsername(identifier)
                    .addOnSuccessListener(query -> {
                        if (query.isEmpty()) {
                            message.setValue("שם המשתמש לא קיים");
                            return;
                        }

                        String email = query.getDocuments().get(0).getString("email");

                        repo.sendPasswordResetEmail(email)
                                .addOnSuccessListener(aVoid ->
                                        message.setValue("נשלח מייל לאיפוס סיסמה"))
                                .addOnFailureListener(e ->
                                        message.setValue("שגיאה בשליחת המייל"));
                    })
                    .addOnFailureListener(e ->
                            message.setValue("שגיאה באיתור המשתמש"));
        }
    }
    public void clearMessage() {
        message.setValue(null);
    }


}