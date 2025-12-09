package org.example.vladtech.projectsubdomain.dataaccesslayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPhoto {

    private String photoId;
    private String photoUrl;
    private String description;
}