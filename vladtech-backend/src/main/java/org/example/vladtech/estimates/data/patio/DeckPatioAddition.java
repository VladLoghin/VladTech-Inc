package org.example.vladtech.estimates.data.patio;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.vladtech.estimates.data.RenovationProject;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeckPatioAddition extends RenovationProject {
    private DeckMaterial deckMaterial;
    private Boolean hasRailing;
    private Integer stairsCount;
    private Boolean isCovered;
    private Double areaSqFt;
}
