package org.example.vladtech.contact.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReviewNotificationRequestDto {

    private String reviewId;
    private String message;
    private  String header;
    private String footer;
}
