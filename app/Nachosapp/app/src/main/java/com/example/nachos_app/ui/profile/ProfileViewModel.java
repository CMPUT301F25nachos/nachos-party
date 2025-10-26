package com.example.nachos_app.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> mName;
    private final MutableLiveData<String> mEmail;
    private final MutableLiveData<String> mPhone;

    public ProfileViewModel() {
        mName = new MutableLiveData<>();
        mEmail = new MutableLiveData<>();
        mPhone = new MutableLiveData<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                mName.setValue(document.getString("name"));
                                mEmail.setValue(document.getString("email"));
                                mPhone.setValue(document.getString("phoneNumber"));
                            }
                        }
                    });
        }
    }

    public LiveData<String> getName() {
        return mName;
    }

    public LiveData<String> getEmail() {
        return mEmail;
    }

    public LiveData<String> getPhone() {
        return mPhone;
    }
}
