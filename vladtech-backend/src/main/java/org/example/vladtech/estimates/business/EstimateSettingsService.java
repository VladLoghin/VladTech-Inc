package org.example.vladtech.estimates.business;

import org.example.vladtech.estimates.data.EstimateSettings;

public interface EstimateSettingsService {
    EstimateSettings getSettings();

    EstimateSettings updateSettings(EstimateSettings updates);
}
