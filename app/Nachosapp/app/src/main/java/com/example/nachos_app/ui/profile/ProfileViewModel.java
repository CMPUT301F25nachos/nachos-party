package com.example.nachos_app.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ViewModel for the profile screen, responsible for fetching and holding the user's profile data. This
 * ViewModel uses a real-time snapshot listener to observe changes to the user's data in Firestore and
 * updates the UI automatically. This ensures that the profile information is always up-to-date.
 */
public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> mName;
    private final MutableLiveData<String> mEmail;
    private final MutableLiveData<String> mPhone;

    /**
     * Initializes the ViewModel and sets up a real-time snapshot listener on the user's document in
     * Firestore. This listener will be notified of any changes to the user's data and will update the
     * LiveData objects accordingly.
     */
    public ProfileViewModel() {
        mName = new MutableLiveData<>();
        mEmail = new MutableLiveData<>();
        mPhone = new MutableLiveData<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            // Handle the error
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            mName.setValue(snapshot.getString("name"));
                            mEmail.setValue(snapshot.getString("email"));
                            mPhone.setValue(snapshot.getString("phoneNumber"));
                        } 
                    });
        }
    }

    /**
     * Returns a LiveData object containing the user's name.
     *
     * @return A LiveData object that the UI can observe to get the user's name.
     */
    public LiveData<String> getName() {
        return mName;
    }

    /**
     * Returns a LiveData object containing the user's email.
     *
     * @return A LiveData object that the UI can observe to get the user's email.
     */
    public LiveData<String> getEmail() {
        return mEmail;
    }

    /**
     * Returns a LiveData object containing the user's phone number.
     *
     * @return A LiveData object that the UI can observe to get the user's phone number.
     */
    public LiveData<String> getPhone() {
        return mPhone;
    }
}
