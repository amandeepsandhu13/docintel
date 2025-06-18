package com.docintel.backend.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;



public class KeyValue {

    public KeyValue() {
    }

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private String key;
    private String value;

    // Getters and setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
