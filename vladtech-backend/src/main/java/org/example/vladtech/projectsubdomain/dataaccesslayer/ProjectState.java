package org.example.vladtech.projectsubdomain.dataaccesslayer;

public enum ProjectState {
    /**
     * Project is currently active and can be modified.
     */
    ACTIVE,

    /**
     * Project has been completed and archived. Cannot be modified.
     */
    COMPLETE
}
