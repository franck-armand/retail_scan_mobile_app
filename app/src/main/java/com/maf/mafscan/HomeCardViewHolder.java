package com.maf.mafscan;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;

public class HomeCardViewHolder extends RecyclerView.ViewHolder {
    public TextView fadedTitle;
    public FlexboxLayout buttonsContainer;

    public HomeCardViewHolder(View itemView) {
        super(itemView);
        fadedTitle = itemView.findViewById(R.id.cardFadedTitle);
        buttonsContainer = itemView.findViewById(R.id.cardButtonsContainer);
    }
}