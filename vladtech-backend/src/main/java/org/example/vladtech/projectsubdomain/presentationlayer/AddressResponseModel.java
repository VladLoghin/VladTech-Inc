package org.example.vladtech.projectsubdomain.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseModel {

    private String streetAddress;
    private String city;
    private String province;
    private String country;
    private String postalCode;
}