package org.example.vladtech.portfolio.presentation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddCommentRequestDto {
    @NotBlank(message = "Comment text cannot be empty")
    private String text;
    
    private String authorName; // Optional: nickname/name from frontend
}

