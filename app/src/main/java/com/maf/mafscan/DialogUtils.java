package com.maf.mafscan;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class DialogUtils {
    private static final String TAG = DialogUtils.class.getSimpleName();
//    public static void showInvalidSelectionDialog(Context context,
//                                                  String title,
//                                                  String message,
//                                                  String positiveButtonText,
//                                                  DialogInterface.OnClickListener positiveAction,
//                                                  String negativeButtonText,
//                                                  DialogInterface.OnClickListener negativeAction) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme);
//        builder.setTitle(title)
//                .setMessage(message)
//                .setPositiveButton(positiveButtonText, positiveAction)
//                .setNegativeButton(negativeButtonText, negativeAction)
//                .setCancelable(false); // Prevent dismissal by clicking outside
//        AlertDialog dialog = builder.create();
//        dialog.setOnShowListener(d -> {
//            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//
//            if(positiveButton != null) {
//                positiveButton.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
//            }
//            if(negativeButton != null) {
//                negativeButton.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
//            }
//        });
//        dialog.show();
//    }
public static void showInvalidSelectionDialog(AppCompatActivity activity,
                                              String title,
                                              String message,
                                              String positiveButtonText,
                                              DialogInterface.OnClickListener positiveAction,
                                              String negativeButtonText,
                                              DialogInterface.OnClickListener negativeAction) {
    if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
        Log.w(TAG, "Attempting to show dialog after activity has been destroyed or finished.");
        return; // Don't show the dialog if the activity is not valid
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.CustomAlertDialogTheme);
    builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText, positiveAction)
            .setNegativeButton(negativeButtonText, negativeAction)
            .setCancelable(false); // Prevent dismissal by clicking outside
    AlertDialog dialog = builder.create();
    dialog.setOnShowListener(d -> {
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if(positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(activity, R.color.primary_color));
        }
        if(negativeButton != null) {
            negativeButton.setTextColor(ContextCompat.getColor(activity, R.color.primary_color));
        }
    });
    try {
        dialog.show();
    } catch (Exception e) {
        Log.e(TAG, "Error showing dialog: ", e);
    }
}

    public static void showItemDialog(
            Context context,
            ScanData scanData,
            int position,
            final OnItemClickListener positiveAction)
    {
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(context,
                R.style.CustomAlertDialogTheme);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_scan_item, null);

        // Initialize dialog views
        EditText quantityEditText = dialogView.findViewById(R.id.dialog_quantity);
        TextView scannedDataTextView = dialogView.findViewById(R.id.dialog_scanned_data);
        TextView scanTypeTextView = dialogView.findViewById(R.id.dialog_scan_type);
        TextView scanDateTextView = dialogView.findViewById(R.id.dialog_scan_date);
        Button validateButton = dialogView.findViewById(R.id.dialog_validate_button);

        // Populate views with data
        scannedDataTextView.setText(scanData.getScannedData());
        scanTypeTextView.setText(scanData.getCodeType());
        scanDateTextView.setText(scanData.getFormattedScanDate());
        quantityEditText.setText(String.valueOf(scanData.getScanCount())); // Default to existing quantity

        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(10);
        // limit field to 10 characters
        quantityEditText.setFilters(filters);

        // Clear text on focus and input
        quantityEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                quantityEditText.setText("");
            }
        });
        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed ( To set minimum length )
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    quantityEditText.setSelection(s.length());
                }
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        android.app.AlertDialog dialog = dialogBuilder.create();

        validateButton.setOnClickListener(v -> {
            String quantityString = quantityEditText.getText().toString();

            quantityString = quantityString.replace(",", ".");

            if (quantityString.isEmpty() || !quantityString.matches("\\d{1,7}(\\.\\d{1,3})?"))
            {
                quantityEditText.setError("Invalid quantity. Max (7) and (3) digits " +
                        "Before and After the decimal point respectively.");
                return;
            }
            try {
                float quantity = Float.parseFloat(quantityString);
                if(quantity <= 0.0f){
                    quantityEditText.setError("Quantity must be greater than 0");
                    return;
                }
                scanData.setScanCount(quantity);

                if (positiveAction != null) {
                    positiveAction.onValidate(scanData, quantity, position);
                }

                dialog.dismiss();
            } catch (NumberFormatException e) {
                quantityEditText.setError("Invalid quantity format.");
            }
        });

        dialog.show();
    }
    public interface OnItemClickListener {
        void onValidate(ScanData scanData, float quantity, int position);
    }
}

