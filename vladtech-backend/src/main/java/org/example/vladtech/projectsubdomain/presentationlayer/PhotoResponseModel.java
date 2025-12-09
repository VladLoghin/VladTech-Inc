package org.example.vladtech.projectsubdomain.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponseModel {

    private String photoId;
    private String photoUrl;
    private String description;
}