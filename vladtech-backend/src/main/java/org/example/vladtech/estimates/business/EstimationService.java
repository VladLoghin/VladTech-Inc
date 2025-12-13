package org.example.vladtech.estimates.business;

import org.example.vladtech.estimates.data.RenovationProject;

public interface EstimationService {
    RenovationProject calculateEstimate(RenovationProject project);
}
