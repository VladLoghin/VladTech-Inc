package org.example.vladtech.projectsubdomain.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCalendarEntryResponseModel {

    private String projectIdentifier;
    private String name;
    private String locationSummary;
    private LocalDate startDate;
    private LocalDate dueDate;
}
