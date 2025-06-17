package com.docintel.backend.dto;

import lombok.Data;

@Data
public class Chunk {

    private int index;
    private String text;

    // Getter and Setter for index
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    // Getter and Setter for text
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    // Optional: toString() for logging/debugging
    @Override
    public String toString() {
        return "Chunk{" +
                "index=" + index +
                ", text='" + text + '\'' +
                '}';
    }

}
