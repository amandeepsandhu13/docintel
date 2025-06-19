package com.docintel.backend.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpenAIRequest {

    private String model; // optional with Azure
    private List<OpenAIMessage> messages;

}
