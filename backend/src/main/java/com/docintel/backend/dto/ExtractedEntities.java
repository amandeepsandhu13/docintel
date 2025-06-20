package com.docintel.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExtractedEntities {

    private List<String> dates;
    private List<String> emails;
    private List<String> names;

}
