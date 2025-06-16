package com.docintel.backend.dto;

import java.util.List;

public class Table {

    // Each row is a list of cell strings
    private List<List<String>> rows;

    // Getters and setters
    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }

}
