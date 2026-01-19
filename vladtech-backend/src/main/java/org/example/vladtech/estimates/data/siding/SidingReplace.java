package org.example.vladtech.estimates.data.siding;

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
public class SidingReplace extends RenovationProject {
    private SidingMaterial sidingMaterial;
    private int stories;
    private boolean includeInsulation;
    private BigDecimal areaSqFt;

    public void setAreaSqFt(BigDecimal areaSqFt) {
        this.areaSqFt = areaSqFt;
        super.setSquareFeet(areaSqFt);
    }
}
