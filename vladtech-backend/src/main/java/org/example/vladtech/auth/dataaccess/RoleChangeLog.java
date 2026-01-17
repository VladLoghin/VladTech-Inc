package org.example.vladtech.auth.dataaccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "role_change_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeLog {

    @Id
    private String id;

    private String userId;
    private String userName;
    private String userEmail;
    private String roleName;
    private String action; // "ASSIGNED" or "REMOVED"
    private Instant performedAt;
}
