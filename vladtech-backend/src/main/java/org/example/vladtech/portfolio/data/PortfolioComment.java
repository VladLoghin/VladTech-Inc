package org.example.vladtech.portfolio.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioComment {
    private String authorName;
    private String authorInitial; // For avatar display (e.g., "S" for Sarah)
    private String timeAgo; // e.g., "3 hours ago"
    private String text;
}

