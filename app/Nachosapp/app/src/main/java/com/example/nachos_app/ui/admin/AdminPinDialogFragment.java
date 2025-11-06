package com.example.nachos_app.ui.admin;



import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.nachos_app.R;


/**
 * cancelable dialog prompting the user for an admin pin
 * <p>
 * The expected pin is the strings.xml file (not secure)
 * I have no idea how to use javadoc XD
 * </p>
 *
 * @author Darius
 */
public class AdminPinDialogFragment extends DialogFragment {

    /** called when the pin is correct. */
    public interface OnPinOk { void run(); }
    private final OnPinOk onPinOk;



    /**
     * creates a new admin pin dialog
     *
     * @param onPinOk called when pin is correct
     */
    public AdminPinDialogFragment(OnPinOk onPinOk) {
        this.onPinOk = onPinOk;
    }


    /**
     * Creates the dialog, inflates layout and wires submit/cancel buttons
     *
     * @param savedInstanceState state bundle
     * @return the created dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog d = new Dialog(requireContext());
        d.setContentView(R.layout.dialog_admin_pin);
        d.setTitle("Admin PIN");
        d.setCancelable(true); // outside press is available to cancel


        // find views
        EditText etPin = d.findViewById(R.id.et_pin);
        Button btnSubmit = d.findViewById(R.id.btn_submit);
        Button btnCancel = d.findViewById(R.id.btn_cancel);

        // cancel closes the dialog
        btnCancel.setOnClickListener(v -> d.dismiss());

        // submit validates the pin and invokes the callback
        btnSubmit.setOnClickListener(v -> {
            String entered = etPin.getText().toString().trim();
            String expected = getString(R.string.admin_pin_fallback);

            if (entered.equals(expected)) {
                Toast.makeText(requireContext(), R.string.admin_mode_enabled, Toast.LENGTH_SHORT).show();
                d.dismiss();
                if (onPinOk != null) onPinOk.run();
            } else {
                Toast.makeText(requireContext(), R.string.admin_incorrect_pin, Toast.LENGTH_SHORT).show();
            }
        });

        return d;
    }
}
