package com.example.nachos_app.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A {@link ViewModel} that acts as a lifecycle-aware data holder for the {@link ProfileFragment}.
 * It is responsible for:
 * <ul>
 *     <li>Fetching user profile data (name, email, phone, notification preference) from Firestore.</li>
 *     <li>Using a real-time snapshot listener to ensure the data is always fresh.</li>
 *     <li>Exposing this data to the UI via {@link LiveData} objects.</li>
 *     <li>Providing methods to update data in Firestore, such as the user's notification preference.</li>
 * </ul>
 * This architecture separates the data logic from the UI controller, improving testability and maintainability.
 */
public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> mName = new MutableLiveData<>();
    private final MutableLiveData<String> mEmail = new MutableLiveData<>();
    private final MutableLiveData<String> mPhone = new MutableLiveData<>();
    private final MutableLiveData<String> mNotificationPreference = new MutableLiveData<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    /**
     * Initializes the ViewModel. It immediately checks for a logged-in user and attaches a snapshot
     * listener to their document in Firestore. This listener will automatically update the LiveData
     * objects whenever the user's data changes on the server.
     */
    public ProfileViewModel() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            // Log error or handle appropriately
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            mName.setValue(snapshot.getString("name"));
                            mEmail.setValue(snapshot.getString("email"));
                            mPhone.setValue(snapshot.getString("phoneNumber"));
                            mNotificationPreference.setValue(snapshot.getString("notificationPreference"));
                        }
                    });
        }
    }

    /**
     * @return A LiveData object containing the user's name.
     */
    public LiveData<String> getName() {
        return mName;
    }

    /**
     * @return A LiveData object containing the user's email.
     */
    public LiveData<String> getEmail() {
        return mEmail;
    }

    /**
     * @return A LiveData object containing the user's phone number.
     */
    public LiveData<String> getPhone() {
        return mPhone;
    }

    /**
     * @return A LiveData object containing the user's notification preference.
     */
    public LiveData<String> getNotificationPreference() {
        return mNotificationPreference;
    }

    /**
     * Updates the user's notification preference in their Firestore document.
     * @param preference The new preference, typically "yes" or "no".
     */
    public void updateNotificationPreference(String preference) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .update("notificationPreference", preference);
        }
    }
}
