package com.docintel.backend.dto;

import lombok.Data;

@Data
public class AskRequest {
    private String chunkContent;
    private String question;
}
