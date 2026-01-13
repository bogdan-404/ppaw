package com.ppaw.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextRequest {
    @NotBlank(message = "Text is required")
    private String text;
    
    private String style;
}
