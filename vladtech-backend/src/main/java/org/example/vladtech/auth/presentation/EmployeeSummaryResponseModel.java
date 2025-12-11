package org.example.vladtech.auth.presentation;

public record EmployeeSummaryResponseModel(
        String userId,
        String name,
        String email
) {}
