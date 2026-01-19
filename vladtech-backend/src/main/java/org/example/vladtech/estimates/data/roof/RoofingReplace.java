package org.example.vladtech.estimates.data.roof;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.vladtech.estimates.data.RenovationProject;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class RoofingReplace extends RenovationProject {
    private RoofMaterial roofMaterial;
    private int stories;
    private BigDecimal roofPitch;
    private boolean hasSkylights;
    private int numSkylights;
    private boolean tearOffRequired;

    private BigDecimal areaSqFt;

    public void setAreaSqFt(BigDecimal areaSqFt) {
        this.areaSqFt = areaSqFt;
        super.setSquareFeet(areaSqFt);
    }
}
