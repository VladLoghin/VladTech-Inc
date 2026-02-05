package org.example.vladtech.projectsubdomain.dataaccesslayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_project_pins")
public class UserProjectPin {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String projectId;

    private LocalDateTime pinnedAt = LocalDateTime.now();
}
