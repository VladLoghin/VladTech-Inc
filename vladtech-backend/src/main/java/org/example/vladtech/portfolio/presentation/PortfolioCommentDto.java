package org.example.vladtech.portfolio.presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioCommentDto {
    private String authorName;
    private String authorUserId;
    private Instant timestamp;
    private String text;
}

