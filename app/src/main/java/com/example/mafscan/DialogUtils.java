package com.example.mafscan;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class DialogUtils {
    public static void showInvalidSelectionDialog(Context context,
                                                  String title,
                                                  String message,
                                                  String positiveButtonText,
                                                  DialogInterface.OnClickListener positiveAction,
                                                  String negativeButtonText,
                                                  DialogInterface.OnClickListener negativeAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme);
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
                positiveButton.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
            }
            if(negativeButton != null) {
                negativeButton.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
            }
        });
        dialog.show();
    }

    public static void showItemDialog(
            Context context,
            ScanData scanData,
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

        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(true);

        android.app.AlertDialog dialog = dialogBuilder.create();

        validateButton.setOnClickListener(v -> {
            String quantityString = quantityEditText.getText().toString();

            quantityString = quantityString.replace(",", ".");

            if (quantityString.isEmpty() || !quantityString.matches("\\d+((\\.\\d{1,3})?)"))
            {
                quantityEditText.setError("Invalid quantity");
                return;
            }
            try {
                float quantity = Float.parseFloat(quantityString);
                scanData.setScanCount(quantity);

                if (positiveAction != null) {
                    positiveAction.onValidate(scanData, quantity);
                }

                dialog.dismiss();
            } catch (NumberFormatException e) {
                quantityEditText.setError("Invalid quantity format.");
            }
        });

        dialog.show();
    }
    public interface OnItemClickListener {
        void onValidate(ScanData scanData, float quantity);
    }
}

