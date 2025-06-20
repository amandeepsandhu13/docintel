package com.docintel.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chunk {

    private int id;
    private int index;
    private String text;

    private double[] embedding;  // to store vector representation

    private String sectionTitle; // optional for header metadata


    // Optional: toString() for logging/debugging
//    @Override
//    public String toString() {
//        return "Chunk{" +
//                "index=" + index +
//                ", text='" + text + '\'' +
//                '}';
//    }

}
