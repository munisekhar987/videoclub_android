package com.videoclub.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.videoclub.R;

/**
 * Dialog for changing password
 * Equivalent to React Native Changepasswordpopup component
 */
public class ChangePasswordDialog extends Dialog {
    private final PasswordChangeListener listener;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;

    public interface PasswordChangeListener {
        void onPasswordChange(String oldPassword, String newPassword);
        void onCancel();
    }

    public ChangePasswordDialog(@NonNull Context context, PasswordChangeListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_change_password);

        // Initialize views
        TextView titleTextView = findViewById(R.id.title_text);
        oldPasswordEditText = findViewById(R.id.old_password_edit_text);
        newPasswordEditText = findViewById(R.id.new_password_edit_text);
        Button skipButton = findViewById(R.id.skip_button);
        Button applyButton = findViewById(R.id.apply_button);
        View divider = findViewById(R.id.divider);

        // Set title
        titleTextView.setText(R.string.change_password);

        // Set button click listeners
        skipButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismiss();
        });

        applyButton.setOnClickListener(v -> {
            validateAndSubmit();
        });

        // Set initial focus
        oldPasswordEditText.requestFocus();
    }

    /**
     * Validate inputs and submit password change
     */
    private void validateAndSubmit() {
        String oldPassword = oldPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(getContext(), "Please enter old password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(getContext(), "Please enter new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (listener != null) {
            listener.onPasswordChange(oldPassword, newPassword);
        }
        dismiss();
    }
}