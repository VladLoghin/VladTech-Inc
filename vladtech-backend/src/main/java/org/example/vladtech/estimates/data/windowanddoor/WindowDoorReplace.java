package org.example.vladtech.estimates.data.windowanddoor;

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
public class WindowDoorReplace extends RenovationProject {
    private WindowType windowType;
    private DoorType doorType;
    private int windowCount;
    private int doorCount;
}
