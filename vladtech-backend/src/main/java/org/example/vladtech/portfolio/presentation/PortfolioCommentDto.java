package org.example.vladtech.portfolio.presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioCommentDto {
    private String authorName;
    private String authorInitial;
    private String timeAgo;
    private String text;
}

