package org.example.vladtech.projectsubdomain.dataaccesslayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String streetAddress;
    private String city;
    private String province;
    private String country;
    private String postalCode;
}