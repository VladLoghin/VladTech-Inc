package org.example.vladtech.auth.dataaccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.vladtech.projectsubdomain.dataaccesslayer.Project;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    private String auth0Sub;

    private String email;

    private String nickname;

    // --- Review ban system ---
    @Builder.Default
    private int deletedReviewCount = 0;

    private Instant reviewBanUntil;

    @Builder.Default
    private boolean reviewPermanentlyBanned = false;
}
