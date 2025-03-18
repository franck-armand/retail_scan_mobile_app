package com.example.mafscan;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeCardAdapter extends RecyclerView.Adapter<HomeCardViewHolder> {
    private final List<HomeCard> cardList;
    private final Context context;
    private OnButtonClickListener buttonClickListener;

    public HomeCardAdapter(List<HomeCard> cardList, Context context) {
        this.cardList = cardList;
        this.context = context;
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.buttonClickListener = listener;
    }

    @NonNull
    @Override
    public HomeCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_card_item, parent, false);
        return new HomeCardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeCardViewHolder holder, int position) {
        HomeCard currentCard = cardList.get(position);
        holder.fadedTitle.setText(currentCard.getFadedTitle());
        holder.buttonsContainer.removeAllViews();
        List<String> buttonTitles = currentCard.getButtonTitles();
        List<Integer> buttonIcons = currentCard.getButtonIcons();
        final int cardIndex = position;
        for (int i = 0; i < buttonTitles.size(); i++) {
            final int buttonIndex = i;
            LinearLayout buttonLayout = new LinearLayout(context);
            buttonLayout.setGravity(Gravity.CENTER);
            buttonLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 60, 40);
            buttonLayout.setLayoutParams(layoutParams);
            TextView buttonTextView = new TextView(context);
            buttonTextView.setText(buttonTitles.get(i));
            buttonTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            buttonTextView.setTextSize(16);
            buttonTextView.setTypeface(null, Typeface.BOLD);
            buttonTextView.setGravity(Gravity.CENTER);
            buttonTextView.setCompoundDrawablePadding(1);
            buttonTextView.setCompoundDrawablesWithIntrinsicBounds(0, buttonIcons.get(i),
                    0, 0);
            buttonLayout.addView(buttonTextView);
            buttonLayout.setOnClickListener(v -> {
                if (buttonClickListener != null) {
                    buttonClickListener.onButtonClick(cardIndex, buttonIndex);
                }
            });
            holder.buttonsContainer.addView(buttonLayout);
        }
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}