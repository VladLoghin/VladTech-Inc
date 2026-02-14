package org.example.vladtech.estimates.business;

import org.example.vladtech.estimates.data.EstimateSettings;
import org.example.vladtech.estimates.data.EstimateSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstimateSettingsServiceImplTest {

    private EstimateSettingsServiceImpl service;

    @Mock
    private EstimateSettingsRepository repository;

    @BeforeEach
    void setUp() {
        service = new EstimateSettingsServiceImpl(repository);
    }

    @Test
    void getSettingsReturnsExistingSettingsFromRepository() {
        EstimateSettings existing = EstimateSettings.defaultSettings();
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings result = service.getSettings();

        assertNotNull(result);
        assertEquals(EstimateSettings.DEFAULT_ID, result.getId());
        assertEquals(new BigDecimal("50.00"), result.getLaborRate());
        verify(repository).findById(EstimateSettings.DEFAULT_ID);
    }

    @Test
    void getSettingsCreatesDefaultsWhenNotFound() {
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.empty());
        
        EstimateSettings defaults = EstimateSettings.defaultSettings();
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(defaults);

        EstimateSettings result = service.getSettings();

        assertNotNull(result);
        assertEquals(EstimateSettings.DEFAULT_ID, result.getId());
        verify(repository).findById(EstimateSettings.DEFAULT_ID);
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void updateSettingsThrowsExceptionWhenNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateSettings(null)
        );
        assertEquals("Estimate settings payload is required", ex.getMessage());
    }

    @Test
    void updateSettingsPartialUpdateMergesWithBase() {
        EstimateSettings existing = EstimateSettings.defaultSettings();
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings updates = new EstimateSettings();
        updates.setLaborRate(new BigDecimal("75.00"));
        updates.setTaxRate(new BigDecimal("0.20"));
        // Leave other fields null to test merge behavior

        EstimateSettings saved = new EstimateSettings();
        saved.setId(EstimateSettings.DEFAULT_ID);
        saved.setLaborRate(new BigDecimal("75.00"));
        saved.setTaxRate(new BigDecimal("0.20"));
        
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(saved);

        EstimateSettings result = service.updateSettings(updates);

        assertNotNull(result);
        assertEquals(EstimateSettings.DEFAULT_ID, result.getId());
        assertEquals(new BigDecimal("75.00"), result.getLaborRate());
        assertEquals(new BigDecimal("0.20"), result.getTaxRate());
        
        verify(repository).findById(EstimateSettings.DEFAULT_ID);
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void updateSettingsCreatesDefaultsWhenNotFound() {
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.empty());

        EstimateSettings updates = new EstimateSettings();
        updates.setLaborRate(new BigDecimal("60.00"));

        EstimateSettings saved = new EstimateSettings();
        saved.setId(EstimateSettings.DEFAULT_ID);
        saved.setLaborRate(new BigDecimal("60.00"));
        
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(saved);

        EstimateSettings result = service.updateSettings(updates);

        assertNotNull(result);
        verify(repository).findById(EstimateSettings.DEFAULT_ID);
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void updateSettingsUpdatesSidingFactors() {
        EstimateSettings existing = EstimateSettings.defaultSettings();
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings updates = new EstimateSettings();
        EstimateSettings.SidingFactors sidingUpdates = new EstimateSettings.SidingFactors();
        sidingUpdates.setVinyl(new BigDecimal("1.50"));
        sidingUpdates.setWood(new BigDecimal("1.25"));
        updates.setSidingFactors(sidingUpdates);

        EstimateSettings saved = new EstimateSettings();
        saved.setId(EstimateSettings.DEFAULT_ID);
        EstimateSettings.SidingFactors savedSiding = new EstimateSettings.SidingFactors();
        savedSiding.setVinyl(new BigDecimal("1.50"));
        savedSiding.setWood(new BigDecimal("1.25"));
        saved.setSidingFactors(savedSiding);
        
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(saved);

        EstimateSettings result = service.updateSettings(updates);

        assertNotNull(result);
        assertNotNull(result.getSidingFactors());
        assertEquals(new BigDecimal("1.50"), result.getSidingFactors().getVinyl());
        assertEquals(new BigDecimal("1.25"), result.getSidingFactors().getWood());
        
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void updateSettingsUpdatesRoofFactors() {
        EstimateSettings existing = EstimateSettings.defaultSettings();
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings updates = new EstimateSettings();
        EstimateSettings.RoofFactors roofUpdates = new EstimateSettings.RoofFactors();
        roofUpdates.setMetal(new BigDecimal("1.40"));
        roofUpdates.setAsphalt(new BigDecimal("1.10"));
        updates.setRoofFactors(roofUpdates);

        EstimateSettings saved = new EstimateSettings();
        saved.setId(EstimateSettings.DEFAULT_ID);
        EstimateSettings.RoofFactors savedRoof = new EstimateSettings.RoofFactors();
        savedRoof.setMetal(new BigDecimal("1.40"));
        savedRoof.setAsphalt(new BigDecimal("1.10"));
        saved.setRoofFactors(savedRoof);
        
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(saved);

        EstimateSettings result = service.updateSettings(updates);

        assertNotNull(result);
        assertNotNull(result.getRoofFactors());
        assertEquals(new BigDecimal("1.40"), result.getRoofFactors().getMetal());
        
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void updateSettingsUpdatesWindowFactors() {
        EstimateSettings existing = EstimateSettings.defaultSettings();
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings updates = new EstimateSettings();
        EstimateSettings.WindowFactors windowUpdates = new EstimateSettings.WindowFactors();
        windowUpdates.setCasement(new BigDecimal("1.15"));
        updates.setWindowFactors(windowUpdates);

        EstimateSettings saved = new EstimateSettings();
        saved.setId(EstimateSettings.DEFAULT_ID);
        EstimateSettings.WindowFactors savedWindow = new EstimateSettings.WindowFactors();
        savedWindow.setCasement(new BigDecimal("1.15"));
        saved.setWindowFactors(savedWindow);
        
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(saved);

        EstimateSettings result = service.updateSettings(updates);

        assertNotNull(result);
        assertNotNull(result.getWindowFactors());
        assertEquals(new BigDecimal("1.15"), result.getWindowFactors().getCasement());
        
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void updateSettingsUpdatesDoorFactors() {
        EstimateSettings existing = EstimateSettings.defaultSettings();
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings updates = new EstimateSettings();
        EstimateSettings.DoorFactors doorUpdates = new EstimateSettings.DoorFactors();
        doorUpdates.setWood(new BigDecimal("1.20"));
        updates.setDoorFactors(doorUpdates);

        EstimateSettings saved = new EstimateSettings();
        saved.setId(EstimateSettings.DEFAULT_ID);
        EstimateSettings.DoorFactors savedDoor = new EstimateSettings.DoorFactors();
        savedDoor.setWood(new BigDecimal("1.20"));
        saved.setDoorFactors(savedDoor);
        
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(saved);

        EstimateSettings result = service.updateSettings(updates);

        assertNotNull(result);
        assertNotNull(result.getDoorFactors());
        assertEquals(new BigDecimal("1.20"), result.getDoorFactors().getWood());
        
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void updateSettingsUpdatesDeckFactors() {
        EstimateSettings existing = EstimateSettings.defaultSettings();
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings updates = new EstimateSettings();
        EstimateSettings.DeckFactors deckUpdates = new EstimateSettings.DeckFactors();
        deckUpdates.setComposite(new BigDecimal("1.35"));
        updates.setDeckFactors(deckUpdates);

        EstimateSettings saved = new EstimateSettings();
        saved.setId(EstimateSettings.DEFAULT_ID);
        EstimateSettings.DeckFactors savedDeck = new EstimateSettings.DeckFactors();
        savedDeck.setComposite(new BigDecimal("1.35"));
        saved.setDeckFactors(savedDeck);
        
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(saved);

        EstimateSettings result = service.updateSettings(updates);

        assertNotNull(result);
        assertNotNull(result.getDeckFactors());
        assertEquals(new BigDecimal("1.35"), result.getDeckFactors().getComposite());
        
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void updateSettingsUpdatesFlooringFactors() {
        EstimateSettings existing = EstimateSettings.defaultSettings();
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings updates = new EstimateSettings();
        EstimateSettings.FlooringFactors flooringUpdates = new EstimateSettings.FlooringFactors();
        flooringUpdates.setHardwood(new BigDecimal("1.15"));
        flooringUpdates.setVinyl(new BigDecimal("0.60"));
        updates.setFlooringFactors(flooringUpdates);

        EstimateSettings saved = new EstimateSettings();
        saved.setId(EstimateSettings.DEFAULT_ID);
        EstimateSettings.FlooringFactors savedFlooring = new EstimateSettings.FlooringFactors();
        savedFlooring.setHardwood(new BigDecimal("1.15"));
        savedFlooring.setVinyl(new BigDecimal("0.60"));
        saved.setFlooringFactors(savedFlooring);
        
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(saved);

        EstimateSettings result = service.updateSettings(updates);

        assertNotNull(result);
        assertNotNull(result.getFlooringFactors());
        assertEquals(new BigDecimal("1.15"), result.getFlooringFactors().getHardwood());
        assertEquals(new BigDecimal("0.60"), result.getFlooringFactors().getVinyl());
        
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void updateSettingsUpdatesFlooringRemovalFactors() {
        EstimateSettings existing = EstimateSettings.defaultSettings();
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings updates = new EstimateSettings();
        EstimateSettings.FlooringRemovalFactors removalUpdates = new EstimateSettings.FlooringRemovalFactors();
        removalUpdates.setHardwood(new BigDecimal("1.30"));
        updates.setFlooringRemovalFactors(removalUpdates);

        EstimateSettings saved = new EstimateSettings();
        saved.setId(EstimateSettings.DEFAULT_ID);
        EstimateSettings.FlooringRemovalFactors savedRemoval = new EstimateSettings.FlooringRemovalFactors();
        savedRemoval.setHardwood(new BigDecimal("1.30"));
        saved.setFlooringRemovalFactors(savedRemoval);
        
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(saved);

        EstimateSettings result = service.updateSettings(updates);

        assertNotNull(result);
        assertNotNull(result.getFlooringRemovalFactors());
        assertEquals(new BigDecimal("1.30"), result.getFlooringRemovalFactors().getHardwood());
        
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void updateSettingsUpdatesAllCoreRates() {
        EstimateSettings existing = EstimateSettings.defaultSettings();
        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings updates = new EstimateSettings();
        updates.setLaborRate(new BigDecimal("65.00"));
        updates.setOverheadRate(new BigDecimal("0.18"));
        updates.setContingencyRate(new BigDecimal("0.12"));
        updates.setTaxRate(new BigDecimal("0.13"));
        updates.setSidingExtraLaborPerStoryRate(new BigDecimal("0.12"));
        updates.setRoofingExtraLaborPerStoryRate(new BigDecimal("0.18"));
        updates.setInsulationAdderPerSqFt(new BigDecimal("0.85"));

        EstimateSettings saved = new EstimateSettings();
        saved.setId(EstimateSettings.DEFAULT_ID);
        saved.setLaborRate(new BigDecimal("65.00"));
        saved.setOverheadRate(new BigDecimal("0.18"));
        saved.setContingencyRate(new BigDecimal("0.12"));
        saved.setTaxRate(new BigDecimal("0.13"));
        saved.setSidingExtraLaborPerStoryRate(new BigDecimal("0.12"));
        saved.setRoofingExtraLaborPerStoryRate(new BigDecimal("0.18"));
        saved.setInsulationAdderPerSqFt(new BigDecimal("0.85"));
        
        when(repository.save(any(EstimateSettings.class)))
                .thenReturn(saved);

        EstimateSettings result = service.updateSettings(updates);

        assertNotNull(result);
        assertEquals(new BigDecimal("65.00"), result.getLaborRate());
        assertEquals(new BigDecimal("0.18"), result.getOverheadRate());
        assertEquals(new BigDecimal("0.12"), result.getContingencyRate());
        assertEquals(new BigDecimal("0.13"), result.getTaxRate());
        
        verify(repository).save(any(EstimateSettings.class));
    }

    @Test
    void getSettingsAppliesDefaultsToExistingSettings() {
        EstimateSettings existing = new EstimateSettings();
        existing.setId(EstimateSettings.DEFAULT_ID);
        existing.setLaborRate(new BigDecimal("55.00"));
        // Other fields are null, will be filled with defaults

        when(repository.findById(EstimateSettings.DEFAULT_ID))
                .thenReturn(Optional.of(existing));

        EstimateSettings result = service.getSettings();

        assertNotNull(result);
        assertEquals(new BigDecimal("55.00"), result.getLaborRate());
        // Verify defaults were applied for null fields
        assertNotNull(result.getOverheadRate());
        assertNotNull(result.getTaxRate());
    }
}
