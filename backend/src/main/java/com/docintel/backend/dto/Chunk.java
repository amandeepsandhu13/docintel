package com.docintel.backend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Chunk {

    private int id;
    private int index;
    private String text;



      // Optional: toString() for logging/debugging
//    @Override
//    public String toString() {
//        return "Chunk{" +
//                "index=" + index +
//                ", text='" + text + '\'' +
//                '}';
//    }

}
