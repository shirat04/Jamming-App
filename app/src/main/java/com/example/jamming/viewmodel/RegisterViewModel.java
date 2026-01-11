package com.example.jamming.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jamming.repository.AuthRepository;
import java.util.HashMap;
import java.util.Map;


public class RegisterViewModel extends ViewModel {
    private final AuthRepository repo = new AuthRepository();
    public MutableLiveData<String> error = new MutableLiveData<>();
    public MutableLiveData<String> userType = new MutableLiveData<>();

    public void register(String fullName, String email, String pass,String confPass, String userName, String type) {

        if (fullName.isEmpty() || email.isEmpty() || pass.isEmpty() || userName.isEmpty()) {
            error.setValue("נא למלא את כל השדות");
            return;
        }

        if (!pass.equals(confPass)) {
            error.setValue("הסיסמאות אינן תואמות");
            return;
        }
        if(pass.length()<6){
            error.setValue("הסיסמא צריכה להיות בת שישה תווים לפחות");
            return;
        }
        if (!email.contains("@")) {
            error.setValue("נא להזין כתובת מייל חוקית");
            return;
        }
        repo.isUsernameTaken(userName)
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        error.setValue("שם משתמש תפוס");
                        return;
                    }

                    repo.createUser(email, pass)
                            .addOnSuccessListener(auth -> {
                                String uid = repo.getCurrentUid();
                                saveUserProfile(uid, fullName, email, userName, type);
                            })
                            .addOnFailureListener(e -> error.setValue(e.getMessage()));
                })
                .addOnFailureListener(e -> error.setValue(e.getMessage()));

    }

    private void saveUserProfile(String uid, String fullName, String email, String userName, String type) {

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", fullName);
        data.put("email", email);
        data.put("username", userName);
        data.put("userType", type);

        repo.saveUserProfile(uid, data)
                .addOnSuccessListener(a -> userType.setValue(type))
                .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getUserType() {
        return userType;
    }


}
