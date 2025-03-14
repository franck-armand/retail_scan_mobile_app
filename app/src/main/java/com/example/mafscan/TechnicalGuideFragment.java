package com.example.mafscan;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class TechnicalGuideFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_technical_guide, container,
                false);
        LinearLayout mainLayout = view.findViewById(R.id.mainLayout);

        List<UserGuideSectionData> userGuideSectionDataList = createSectionDataList();

        for (UserGuideSectionData userGuideSectionData : userGuideSectionDataList) {
            MaterialCardView cardView = createCardView(userGuideSectionData, inflater);
            mainLayout.addView(cardView);
        }

        return view;
    }

    private MaterialCardView createCardView(UserGuideSectionData userGuideSectionData, LayoutInflater inflater) {
        MaterialCardView cardView = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(4);
        cardView.setRadius(10);

        LinearLayout cardLayout = new LinearLayout(requireContext());
        cardLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardLayout.setOrientation(LinearLayout.VERTICAL);

        // Card Header
        LinearLayout headerLayout = new LinearLayout(requireContext());
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setPadding(16, 16, 16, 16);
        headerLayout.setClickable(true);

        TextView sectionTitle = new TextView(requireContext());
        sectionTitle.setText(userGuideSectionData.getSectionTitle());
        TextViewCompat.setTextAppearance(sectionTitle,
                androidx.appcompat.R.style.TextAppearance_AppCompat_Title);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        sectionTitle.setLayoutParams(titleParams);
        headerLayout.addView(sectionTitle);

        ImageView expandCollapseIcon = new ImageView(requireContext());
        expandCollapseIcon.setImageResource(R.drawable.ic_expand_more);
        headerLayout.addView(expandCollapseIcon);

        cardLayout.addView(headerLayout);

        // Expandable Content
        TableLayout tableLayout = createTableLayout(userGuideSectionData.getTableRows(), inflater);
        cardLayout.addView(tableLayout);
        tableLayout.setVisibility(View.GONE);

        headerLayout.setOnClickListener(v -> {
            if (tableLayout.getVisibility() == View.GONE) {
                tableLayout.setVisibility(View.VISIBLE);
                expandCollapseIcon.setImageResource(R.drawable.ic_expand_less);
            } else {
                tableLayout.setVisibility(View.GONE);
                expandCollapseIcon.setImageResource(R.drawable.ic_expand_more);
            }
        });

        cardView.addView(cardLayout);
        return cardView;
    }

    private TableLayout createTableLayout(List<UserGuideSectionData.TableRowData> tableRows,
                                          LayoutInflater inflater) {
        LinearLayout tempRoot = new LinearLayout(requireContext());
        TableLayout tableLayout = (TableLayout) inflater.inflate(R.layout.table_user_guide,
                tempRoot, false);

        for (UserGuideSectionData.TableRowData rowData : tableRows) {
            TableRow tableRow = new TableRow(requireContext());

            TextView prefixTextView = new TextView(requireContext());
            //prefixTextView.setText(R.string.scan_code_table_label_1);
            prefixTextView.setText(rowData.getPrefix());
            prefixTextView.setPadding(8, 8, 8, 8);
            prefixTextView.setLayoutParams(new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 1));
            prefixTextView.setTextSize(16);
            prefixTextView.setTypeface(null, Typeface.BOLD);
            tableRow.addView(prefixTextView);

            TextView dataFieldTextView = new TextView(requireContext());
            //dataFieldTextView.setText(R.string.scan_code_table_label_1);
            dataFieldTextView.setText(rowData.getDataField());
            dataFieldTextView.setPadding(8, 8, 8, 8);
            dataFieldTextView.setLayoutParams(new TableRow.LayoutParams(0,
                    TableRow.LayoutParams.WRAP_CONTENT, 3));
            dataFieldTextView.setTextSize(16);
            dataFieldTextView.setTypeface(null, Typeface.BOLD_ITALIC);
            tableRow.addView(dataFieldTextView);

            tableLayout.addView(tableRow);
        }

        return tableLayout;
    }

    private List<UserGuideSectionData> createSectionDataList() {
        List<UserGuideSectionData> userGuideSectionDataList = new ArrayList<>();

        // User Section
        List<UserGuideSectionData.TableRowData> userTableRows = new ArrayList<>();
        userTableRows.add(new UserGuideSectionData.TableRowData("00", "USR"));
        userTableRows.add(new UserGuideSectionData.TableRowData("05", "Id"));
        userTableRows.add(new UserGuideSectionData.TableRowData("06", "Code"));
        userTableRows.add(new UserGuideSectionData.TableRowData("07", "Nom"));
        userGuideSectionDataList.add(new UserGuideSectionData("User - USR", userTableRows));

        // Location Section
        List<UserGuideSectionData.TableRowData> locationTableRows = new ArrayList<>();
        locationTableRows.add(new UserGuideSectionData.TableRowData("00", "LOC"));
        locationTableRows.add(new UserGuideSectionData.TableRowData("10", "Id"));
        locationTableRows.add(new UserGuideSectionData.TableRowData("11", "Code"));
        locationTableRows.add(new UserGuideSectionData.TableRowData("12", "Nom"));
        userGuideSectionDataList.add(new UserGuideSectionData("Location - LOC", locationTableRows));

        // Container Section
        List<UserGuideSectionData.TableRowData> containerTableRows = new ArrayList<>();
        containerTableRows.add(new UserGuideSectionData.TableRowData("00", "CNR"));
        containerTableRows.add(new UserGuideSectionData.TableRowData("20", "Id"));
        containerTableRows.add(new UserGuideSectionData.TableRowData("21", "Code"));
        userGuideSectionDataList.add(new UserGuideSectionData("Container - CNR", containerTableRows));

        // OF(Maitre) Section
        List<UserGuideSectionData.TableRowData> ofMaitreTableRows = new ArrayList<>();
        ofMaitreTableRows.add(new UserGuideSectionData.TableRowData("00", "WOH"));
        ofMaitreTableRows.add(new UserGuideSectionData.TableRowData("30", "N°OF"));
        ofMaitreTableRows.add(new UserGuideSectionData.TableRowData("31", "N°ARC"));
        ofMaitreTableRows.add(new UserGuideSectionData.TableRowData("32", "Quantité"));
        ofMaitreTableRows.add(new UserGuideSectionData.TableRowData("35", "Traitement"));
        ofMaitreTableRows.add(new UserGuideSectionData.TableRowData("50", "Code article"));
        ofMaitreTableRows.add(new UserGuideSectionData.TableRowData("51", "Description 1"));
        ofMaitreTableRows.add(new UserGuideSectionData.TableRowData("52", "Description 2"));
        ofMaitreTableRows.add(new UserGuideSectionData.TableRowData("53", "Lieu stock par défaut article"));
        ofMaitreTableRows.add(new UserGuideSectionData.TableRowData("54", "Unité de mesure stock"));
        userGuideSectionDataList.add(new UserGuideSectionData("OF(Maitre) - WOH", ofMaitreTableRows));

        // OF(Detail) Section
        List<UserGuideSectionData.TableRowData> ofDetailTableRows = new ArrayList<>();
        ofDetailTableRows.add(new UserGuideSectionData.TableRowData("00", "WOD"));
        ofDetailTableRows.add(new UserGuideSectionData.TableRowData("30", "N°OF"));
        ofDetailTableRows.add(new UserGuideSectionData.TableRowData("31", "N°ARC"));
        ofDetailTableRows.add(new UserGuideSectionData.TableRowData("32", "Quantité"));
        ofDetailTableRows.add(new UserGuideSectionData.TableRowData("35", "Traitement"));
        ofDetailTableRows.add(new UserGuideSectionData.TableRowData("50", "Code article"));
        ofDetailTableRows.add(new UserGuideSectionData.TableRowData("51", "Description 1"));
        ofDetailTableRows.add(new UserGuideSectionData.TableRowData("52", "Description 2"));
        ofDetailTableRows.add(new UserGuideSectionData.TableRowData("53", "Lieu stock par défaut article"));
        ofDetailTableRows.add(new UserGuideSectionData.TableRowData("54", "Unité de mesure stock"));
        userGuideSectionDataList.add(new UserGuideSectionData("OF(Détail) - WOD", ofDetailTableRows));

        // Order buyer Section
        List<UserGuideSectionData.TableRowData> orderBuyerTableRows = new ArrayList<>();
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("00", "PUR"));
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("40", "N°Cde"));
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("41", "N°Ligne"));
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("43", "Quantité"));
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("44", "N°Arc"));
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("45", "Traitement"));
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("50", "Code article"));
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("51", "Description 1"));
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("52", "Description 2"));
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("53", "Lieu stock par défaut article"));
        orderBuyerTableRows.add(new UserGuideSectionData.TableRowData("54", "Unité de mesure stock"));
        userGuideSectionDataList.add(new UserGuideSectionData("Ligne commande achat - PUR", orderBuyerTableRows));

        // launching Section
        List<UserGuideSectionData.TableRowData> launchingTableRows = new ArrayList<>();
        launchingTableRows.add(new UserGuideSectionData.TableRowData("00", "LNC"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("30", "N°Of"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("31", "N°Arc"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("32", "Quantité"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("50", "Code article"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("51", "Description 1"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("52", "Description 2"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("53", "Lieu stock par défaut article"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("54", "Unité de mesure stock"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("60", "Code client"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("61", "Nom client"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("62", "Description ARC"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("70", "N°Fiche"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("71", "Type fiche"));
        launchingTableRows.add(new UserGuideSectionData.TableRowData("72", "Fabricant"));
        userGuideSectionDataList.add(new UserGuideSectionData("Lancement - LNC", launchingTableRows));

        // Article Section
        List<UserGuideSectionData.TableRowData> articleTableRows = new ArrayList<>();
        articleTableRows.add(new UserGuideSectionData.TableRowData("00", "ART"));
        articleTableRows.add(new UserGuideSectionData.TableRowData("50", "Code article"));
        articleTableRows.add(new UserGuideSectionData.TableRowData("51", "Description 1"));
        articleTableRows.add(new UserGuideSectionData.TableRowData("52", "Description 2"));
        articleTableRows.add(new UserGuideSectionData.TableRowData("53", "Lieu stock par défaut article"));
        articleTableRows.add(new UserGuideSectionData.TableRowData("54", "Unité de mesure stock"));
        articleTableRows.add(new UserGuideSectionData.TableRowData("55", "Quantité"));
        userGuideSectionDataList.add(new UserGuideSectionData("Article - ART", articleTableRows));

        return userGuideSectionDataList;
    }
}