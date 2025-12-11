package org.example.vladtech.portfolio.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioComment {
    private String authorName;
    private String authorUserId; // Auth0 user ID
    private Instant timestamp; // Actual timestamp for sorting and calculating "timeAgo"
    private String text;
}

