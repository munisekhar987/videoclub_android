package com.videoclub.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.videoclub.R;

/**
 * Dialog for coupon code entry
 * Equivalent to React Native Couponpopup component
 */
public class CouponPopupDialog extends Dialog {
    private final CouponSubmitListener listener;
    private EditText codeEditText;

    public interface CouponSubmitListener {
        void onCouponSubmit(String code);
        void onCancel();
    }

    public CouponPopupDialog(@NonNull Context context, CouponSubmitListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_coupon_popup);

        // Initialize views
        TextView titleTextView = findViewById(R.id.title_text);
        codeEditText = findViewById(R.id.code_edit_text);
        Button skipButton = findViewById(R.id.skip_button);
        Button checkButton = findViewById(R.id.check_button);
        View divider = findViewById(R.id.divider);

        // Set title
        titleTextView.setText(R.string.have_code);

        // Set button click listeners
        skipButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismiss();
        });

        checkButton.setOnClickListener(v -> {
            String code = codeEditText.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(getContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            } else {
                if (listener != null) {
                    listener.onCouponSubmit(code);
                }
                dismiss();
            }
        });

        // Set initial focus
        codeEditText.requestFocus();
    }
}