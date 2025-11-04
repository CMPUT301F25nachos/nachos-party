package com.example.nachos_app.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ViewModel for the profile screen. It fetches the user's profile data and notification preferences
 * from Firestore in real-time and provides methods to update them.
 */
public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> mName = new MutableLiveData<>();
    private final MutableLiveData<String> mEmail = new MutableLiveData<>();
    private final MutableLiveData<String> mPhone = new MutableLiveData<>();
    private final MutableLiveData<String> mNotificationPreference = new MutableLiveData<>(); // Renamed

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public ProfileViewModel() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            mName.setValue(snapshot.getString("name"));
                            mEmail.setValue(snapshot.getString("email"));
                            mPhone.setValue(snapshot.getString("phoneNumber"));
                            mNotificationPreference.setValue(snapshot.getString("notificationPreference")); // Renamed
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

    public LiveData<String> getNotificationPreference() { // Renamed
        return mNotificationPreference;
    }

    /**
     * Updates the user's notification preference in Firestore.
     * @param preference The new preference, either "yes" or "no".
     */
    public void updateNotificationPreference(String preference) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .update("notificationPreference", preference); // Renamed
        }
    }
}
