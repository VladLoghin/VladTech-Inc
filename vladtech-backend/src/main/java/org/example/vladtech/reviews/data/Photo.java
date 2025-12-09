package org.example.vladtech.reviews.data;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    private String clientId;
    private String filename;
    private String imageType;
    private String url;
}
