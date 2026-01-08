package org.example.vladtech.projectsubdomain.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectErrorResponse {
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
}
