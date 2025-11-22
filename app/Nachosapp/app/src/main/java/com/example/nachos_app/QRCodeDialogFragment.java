package com.example.nachos_app;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.Button;

/**
 * Dialog fragment for displaying an event's QR code.
 * Shows the event name and QR code image in a full-screen dialog.
 * Users can close the dialog with a close button.
 */
public class QRCodeDialogFragment extends DialogFragment {

    private static final String ARG_EVENT_NAME = "event_name";
    private static final String ARG_QR_CODE_BASE64 = "qr_code_base64";

    private String eventName;
    private String qrCodeBase64;

    /**
     * Creates a new instance of QRCodeDialogFragment with event data.
     * @param eventName The name of the event
     * @param qrCodeBase64 Base64 encoded QR code image
     * @return New QRCodeDialogFragment instance
     */
    public static QRCodeDialogFragment newInstance(String eventName, String qrCodeBase64) {
        QRCodeDialogFragment fragment = new QRCodeDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_NAME, eventName);
        args.putString(ARG_QR_CODE_BASE64, qrCodeBase64);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventName = getArguments().getString(ARG_EVENT_NAME);
            qrCodeBase64 = getArguments().getString(ARG_QR_CODE_BASE64);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_qr_code, container, false);

        TextView titleTextView = view.findViewById(R.id.qrCodeTitleTextView);
        ImageView qrCodeImageView = view.findViewById(R.id.qrCodeImageView);
        Button closeButton = view.findViewById(R.id.closeButton);

        // Set title
        titleTextView.setText(eventName);

        // Load QR code
        Bitmap qrBitmap = ImageUtils.decodeBase64ToBitmap(qrCodeBase64);
        if (qrBitmap != null) {
            qrCodeImageView.setImageBitmap(qrBitmap);
        } else {
            // Use a placeholder if decoding fails
            qrCodeImageView.setImageResource(R.drawable.ic_camera_placeholder);
        }

        // Close button
        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // Make dialog wider
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}