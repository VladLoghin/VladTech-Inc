package org.example.vladtech.auth.presentation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeSummaryResponseModelTest {

    @Test
    void constructor_setsFields() {
        EmployeeSummaryResponseModel model =
                new EmployeeSummaryResponseModel("u1", "John Doe", "john@x.com");

        assertEquals("u1", model.userId());
        assertEquals("John Doe", model.name());
        assertEquals("john@x.com", model.email());
    }
}
