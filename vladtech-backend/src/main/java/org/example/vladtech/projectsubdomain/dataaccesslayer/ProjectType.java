package org.example.vladtech.projectsubdomain.dataaccesslayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectType {

    private ProjectTypeEnum type;

    public enum ProjectTypeEnum {
        APPOINTMENT,
        SCHEDULED
    }
}