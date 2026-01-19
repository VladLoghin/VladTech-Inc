package org.example.vladtech.estimates.data.kitchen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.shared.FlooringMaterial;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KitchenRemodel extends RenovationProject {
    private CabinetQuality cabinetQuality;
    private CountertopMaterial countertopMaterial;
    private FlooringMaterial flooringMaterial;
    private Double applianceAllowance;
    private Boolean plumbingChanges;
    private Boolean electricalChanges;
    private BigDecimal areaSqFt;

    public void setAreaSqFt(BigDecimal areaSqFt) {
        this.areaSqFt = areaSqFt;
        super.setSquareFeet(areaSqFt);
    }
}
