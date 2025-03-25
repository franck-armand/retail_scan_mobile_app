package com.maf.mafscan;

import java.util.List;

public class UserGuideSectionData {
    private final String sectionTitle;
    private final List<TableRowData> tableRows;

    public UserGuideSectionData(String sectionTitle, List<TableRowData> tableRows) {
        this.sectionTitle = sectionTitle;
        this.tableRows = tableRows;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public List<TableRowData> getTableRows() {
        return tableRows;
    }

    public static class TableRowData {
        private final String prefix;
        private final String dataField;

        public TableRowData(String prefix, String dataField) {
            this.prefix = prefix;
            this.dataField = dataField;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getDataField() {
            return dataField;
        }
    }
}