package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.repository.AuthRepository;
import java.util.HashMap;
import java.util.Map;

public class LoginViewModel extends ViewModel {


private final AuthRepository repo = new AuthRepository();
public MutableLiveData<String> error = new MutableLiveData<>();
public MutableLiveData<String> userType = new MutableLiveData<>();
public void login(String identifier, String password) {

    if (identifier.isEmpty() || password.isEmpty()) {
        error.setValue("נא למלא את כל השדות");
        return;
    }

    if (identifier.contains("@")) {
        // אימייל
        repo.login(identifier, password)
                .addOnSuccessListener(auth -> checkUserType(repo.getCurrentUid()))
                .addOnFailureListener(e -> {

                    String msg = e.getMessage();

                    if (msg != null && msg.contains("credential")) {
                        error.setValue("שם משתמש או סיסמה אינם נכונים");
                    } else {
                        error.setValue("שגיאה בהתחברות, נסה שוב");
                    }
                });


    } else {
        repo.getUserByUsername(identifier)
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        error.setValue("שם המשתמש לא קיים");
                        return;
                    }

                    String email = query.getDocuments().get(0).getString("email");

                    repo.login(email, password)
                            .addOnSuccessListener(auth -> checkUserType(repo.getCurrentUid()))
                            .addOnFailureListener(e -> error.setValue("שם משתמש או סיסמה אינם נכונים"));
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }
}
    private void checkUserType(String uid) {

        repo.getUserUId(uid)
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        error.setValue("פרופיל המשתמש לא קיים");
                        return;
                    }

                    String type = doc.getString("userType");

                    userType.setValue(type);  // שולח לאקטיביטי

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