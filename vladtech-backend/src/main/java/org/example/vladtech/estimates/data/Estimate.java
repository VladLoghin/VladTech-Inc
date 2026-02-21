package org.example.vladtech.estimates.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "estimates")
public class Estimate {
    @Id
    private String estimateId;

    private String title;

    // Store the full project (inputs + derived values)
    private RenovationProject project;

    // Owner (Auth0 subject / user id)
    private String ownerAuth0Id;

    private Instant createdAt;

    // Optional stored PDF URL (S3 or other)
    private String pdfUrl;
}
