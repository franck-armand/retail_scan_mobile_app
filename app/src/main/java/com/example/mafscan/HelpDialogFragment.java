package com.example.mafscan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class HelpDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the dialog
        View view = inflater.inflate(R.layout.dialog_help, container, false);

        // Get the ImageView
        ImageView helpImageView = view.findViewById(R.id.helpImageView);

        // Set the image resource
        helpImageView.setImageResource(R.drawable.scan_code_help);

        // Get the display metrics
        LinearLayout.LayoutParams layoutParams = getLayoutParams();
        helpImageView.setLayoutParams(layoutParams);

        return view;
    }

    private LinearLayout.LayoutParams getLayoutParams() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        // Calculate the desired width and height for the image
        int desiredWidth = (int) (width * 0.9); // 90% of screen width
        int desiredHeight = (int) (height * 0.45); // 45% of screen height

        // Set the layout parameters for the ImageView
        return new LinearLayout.LayoutParams(
                desiredWidth,
                desiredHeight
        );
    }
}