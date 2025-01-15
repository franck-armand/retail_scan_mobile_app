package com.example.mafscan;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class DialogUtils {
    public static void showInvalideSelectionDialog(Context context,
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
}

