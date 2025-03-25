package com.maf.mafscan;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchableSpinner extends LinearLayout {

    private TextView selectedItemTextView;
    private Dialog dialog;
    private SearchableSpinnerAdapter adapter;
    private List<String> items;
    private OnItemSelectedListener listener;

    public SearchableSpinner(Context context) {
        super(context);
        init(context);
    }

    public SearchableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.searchable_spinner, this, true);

        selectedItemTextView = findViewById(R.id.selectedItemEditText);
        selectedItemTextView.setFocusable(false);
        selectedItemTextView.setClickable(true);
        selectedItemTextView.setOnClickListener(v -> showDropdown());

        items = new ArrayList<>();
        adapter = new SearchableSpinnerAdapter(context, items);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<String> items) {
        this.items.clear();
        this.items.addAll(items);
        adapter.notifyDataSetChanged();
    }

    private void showDropdown() {
        // Initialize dialog
        dialog = new Dialog(getContext());
        // set custom dialog
        dialog.setContentView(R.layout.dialog_searchable_spinner);
        // set custom height and width
        Objects.requireNonNull(dialog.getWindow()).setLayout(800, 1200);
        // set transparent background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // show dialog
        dialog.show();
        // Initialize and assign variable
        EditText searchEditText = dialog.findViewById(R.id.edit_text);
        RecyclerView dropdownRecyclerView = dialog.findViewById(R.id.recycler_view);
        // set layout manager
        dropdownRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // set adapter
        dropdownRecyclerView.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        adapter.getFilter().filter("");
        adapter.setOnItemClickListener(item -> {
            selectedItemTextView.setText(item);
            dialog.dismiss();
            if (listener != null) {
                listener.onItemSelected(item);
            }
        });
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(String item);
    }
}