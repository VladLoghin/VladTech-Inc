package org.example.vladtech.estimates.data.floor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.vladtech.estimates.data.RenovationProject;
import org.example.vladtech.estimates.data.shared.FlooringMaterial;

@Data
@EqualsAndHashCode(callSuper = true)
public class FloorReplace extends RenovationProject {
    private FlooringMaterial existingFloorMaterial;
    private FlooringMaterial newFloorMaterial;
    private Boolean subfloorRepairNeeded;
}
