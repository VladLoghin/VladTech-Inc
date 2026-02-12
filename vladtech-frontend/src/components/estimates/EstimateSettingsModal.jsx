import { useEffect, useMemo, useState, useCallback } from "react";
import { X, ChevronDown, ChevronRight } from "lucide-react";
import { useAuth0 } from "@auth0/auth0-react";
import { useTranslation } from "react-i18next";
import { api } from "../../api/http";

const emptySettings = () => ({
  laborRate: "",
  overheadRate: "",
  contingencyRate: "",
  taxRate: "",
  sidingExtraLaborPerStoryRate: "",
  roofingExtraLaborPerStoryRate: "",
  insulationAdderPerSqFt: "",
  roofPitchFactorPerUnit: "",
  roofTearOffCostPerSqFt: "",
  roofSkylightCost: "",
  kitchenPlumbingCost: "",
  kitchenElectricalCost: "",
  windowBaseCostPerUnit: "",
  doorBaseCostPerUnit: "",
  windowDoorLaborRateMultiplier: "",
  deckBaseMaterialCostPerSqFt: "",
  deckRailingCostPerLinearFoot: "",
  deckStairsCost: "",
  deckCoverCostPerSqFt: "",
  floorSubfloorRepairCostPerSqFt: "",
  floorRemovalBaseCostPerSqFt: "",
  sidingFactors: {
    vinyl: "",
    wood: "",
    fiberCement: "",
    brick: "",
    stoneVeneer: "",
  },
  roofFactors: {
    asphalt: "",
    metal: "",
    clay: "",
    slate: "",
    synthetic: "",
  },
  windowFactors: {
    casement: "",
    slider: "",
    doubleHung: "",
    awning: "",
    fixed: "",
  },
  doorFactors: {
    wood: "",
    fiberglass: "",
    steel: "",
    glassPanel: "",
  },
  deckFactors: {
    wood: "",
    composite: "",
    pvc: "",
    aluminum: "",
  },
  flooringFactors: {
    hardwood: "",
    engineeredHardwood: "",
    laminate: "",
    vinyl: "",
    tile: "",
    carpet: "",
    polishedConcrete: "",
  },
  flooringRemovalFactors: {
    hardwood: "",
    engineeredHardwood: "",
    laminate: "",
    vinyl: "",
    tile: "",
    carpet: "",
    polishedConcrete: "",
  },
});

const normalizeSettings = (data) => {
  const base = emptySettings();
  if (!data) return base;
  return {
    ...base,
    ...data,
    sidingFactors: { ...base.sidingFactors, ...(data.sidingFactors || {}) },
    roofFactors: { ...base.roofFactors, ...(data.roofFactors || {}) },
    windowFactors: { ...base.windowFactors, ...(data.windowFactors || {}) },
    doorFactors: { ...base.doorFactors, ...(data.doorFactors || {}) },
    deckFactors: { ...base.deckFactors, ...(data.deckFactors || {}) },
    flooringFactors: { ...base.flooringFactors, ...(data.flooringFactors || {}) },
    flooringRemovalFactors: { ...base.flooringRemovalFactors, ...(data.flooringRemovalFactors || {}) },
  };
};

const toNumberOrNull = (value) => {
  if (value === "" || value === null || value === undefined) {
    return null;
  }
  const numeric = Number(value);
  return Number.isNaN(numeric) ? null : numeric;
};

const buildPayload = (settings) => ({
  laborRate: toNumberOrNull(settings.laborRate),
  overheadRate: toNumberOrNull(settings.overheadRate),
  contingencyRate: toNumberOrNull(settings.contingencyRate),
  taxRate: toNumberOrNull(settings.taxRate),
  sidingExtraLaborPerStoryRate: toNumberOrNull(settings.sidingExtraLaborPerStoryRate),
  roofingExtraLaborPerStoryRate: toNumberOrNull(settings.roofingExtraLaborPerStoryRate),
  insulationAdderPerSqFt: toNumberOrNull(settings.insulationAdderPerSqFt),
  roofPitchFactorPerUnit: toNumberOrNull(settings.roofPitchFactorPerUnit),
  roofTearOffCostPerSqFt: toNumberOrNull(settings.roofTearOffCostPerSqFt),
  roofSkylightCost: toNumberOrNull(settings.roofSkylightCost),
  kitchenPlumbingCost: toNumberOrNull(settings.kitchenPlumbingCost),
  kitchenElectricalCost: toNumberOrNull(settings.kitchenElectricalCost),
  windowBaseCostPerUnit: toNumberOrNull(settings.windowBaseCostPerUnit),
  doorBaseCostPerUnit: toNumberOrNull(settings.doorBaseCostPerUnit),
  windowDoorLaborRateMultiplier: toNumberOrNull(settings.windowDoorLaborRateMultiplier),
  deckBaseMaterialCostPerSqFt: toNumberOrNull(settings.deckBaseMaterialCostPerSqFt),
  deckRailingCostPerLinearFoot: toNumberOrNull(settings.deckRailingCostPerLinearFoot),
  deckStairsCost: toNumberOrNull(settings.deckStairsCost),
  deckCoverCostPerSqFt: toNumberOrNull(settings.deckCoverCostPerSqFt),
  floorSubfloorRepairCostPerSqFt: toNumberOrNull(settings.floorSubfloorRepairCostPerSqFt),
  floorRemovalBaseCostPerSqFt: toNumberOrNull(settings.floorRemovalBaseCostPerSqFt),
  sidingFactors: {
    vinyl: toNumberOrNull(settings.sidingFactors.vinyl),
    wood: toNumberOrNull(settings.sidingFactors.wood),
    fiberCement: toNumberOrNull(settings.sidingFactors.fiberCement),
    brick: toNumberOrNull(settings.sidingFactors.brick),
    stoneVeneer: toNumberOrNull(settings.sidingFactors.stoneVeneer),
  },
  roofFactors: {
    asphalt: toNumberOrNull(settings.roofFactors.asphalt),
    metal: toNumberOrNull(settings.roofFactors.metal),
    clay: toNumberOrNull(settings.roofFactors.clay),
    slate: toNumberOrNull(settings.roofFactors.slate),
    synthetic: toNumberOrNull(settings.roofFactors.synthetic),
  },
  windowFactors: {
    casement: toNumberOrNull(settings.windowFactors.casement),
    slider: toNumberOrNull(settings.windowFactors.slider),
    doubleHung: toNumberOrNull(settings.windowFactors.doubleHung),
    awning: toNumberOrNull(settings.windowFactors.awning),
    fixed: toNumberOrNull(settings.windowFactors.fixed),
  },
  doorFactors: {
    wood: toNumberOrNull(settings.doorFactors.wood),
    fiberglass: toNumberOrNull(settings.doorFactors.fiberglass),
    steel: toNumberOrNull(settings.doorFactors.steel),
    glassPanel: toNumberOrNull(settings.doorFactors.glassPanel),
  },
  deckFactors: {
    wood: toNumberOrNull(settings.deckFactors.wood),
    composite: toNumberOrNull(settings.deckFactors.composite),
    pvc: toNumberOrNull(settings.deckFactors.pvc),
    aluminum: toNumberOrNull(settings.deckFactors.aluminum),
  },
  flooringFactors: {
    hardwood: toNumberOrNull(settings.flooringFactors.hardwood),
    engineeredHardwood: toNumberOrNull(settings.flooringFactors.engineeredHardwood),
    laminate: toNumberOrNull(settings.flooringFactors.laminate),
    vinyl: toNumberOrNull(settings.flooringFactors.vinyl),
    tile: toNumberOrNull(settings.flooringFactors.tile),
    carpet: toNumberOrNull(settings.flooringFactors.carpet),
    polishedConcrete: toNumberOrNull(settings.flooringFactors.polishedConcrete),
  },
  flooringRemovalFactors: {
    hardwood: toNumberOrNull(settings.flooringRemovalFactors.hardwood),
    engineeredHardwood: toNumberOrNull(settings.flooringRemovalFactors.engineeredHardwood),
    laminate: toNumberOrNull(settings.flooringRemovalFactors.laminate),
    vinyl: toNumberOrNull(settings.flooringRemovalFactors.vinyl),
    tile: toNumberOrNull(settings.flooringRemovalFactors.tile),
    carpet: toNumberOrNull(settings.flooringRemovalFactors.carpet),
    polishedConcrete: toNumberOrNull(settings.flooringRemovalFactors.polishedConcrete),
  },
});

const SectionHeader = ({ title, isOpen, onToggle }) => (
  <button
    type="button"
    onClick={onToggle}
    className="flex items-center gap-2 w-full py-2 px-3 -mx-3 rounded-lg hover:bg-black/5 transition-colors group"
  >
    {isOpen ? (
      <ChevronDown className="h-5 w-5 text-black/60 group-hover:text-black/80 transition-colors" />
    ) : (
      <ChevronRight className="h-5 w-5 text-black/60 group-hover:text-black/80 transition-colors" />
    )}
    <h3 className="text-lg font-bold text-black/80 group-hover:text-black transition-colors">{title}</h3>
  </button>
);

const EstimateSettingsModal = ({ isOpen, onClose, onSuccess }) => {
  const { getAccessTokenSilently } = useAuth0();
  const { t } = useTranslation();
  const [settings, setSettings] = useState(() => emptySettings());
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [openSections, setOpenSections] = useState(() => ({
    core: true,
    siding: false,
    roofing: false,
    kitchen: false,
    windowDoor: false,
    deck: false,
    flooring: false,
    flooringRemoval: false,
  }));

  const numberInputProps = {
    type: "number",
    step: "0.01",
    min: 0,
    title: t("admin.estimateSettingsNonNegativeHint"),
    className:
      "w-full px-4 py-2 border border-black/20 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-yellow-400 transition-shadow",
  };

  const resetMessages = () => {
    setError("");
    setSuccess("");
  };

  const toggleSection = (key) => () => {
    setOpenSections((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const fetchSettings = useCallback(async () => {
    setLoading(true);
    resetMessages();
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });
      const response = await api.get("/estimates/config", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setSettings(normalizeSettings(response.data));
    } catch (err) {
      console.error("Failed to load estimate settings", err);
      setError(t("admin.estimateSettingsLoadFailed"));
      setSettings(emptySettings());
    } finally {
      setLoading(false);
    }
  }, [getAccessTokenSilently, t]);

  useEffect(() => {
    if (isOpen) {
      fetchSettings();
    }
  }, [isOpen, fetchSettings]);

  const handleChange = (field) => (event) => {
    const { value } = event.target;
    setSettings((prev) => ({ ...prev, [field]: value }));
  };

  const handleNestedChange = (group, field) => (event) => {
    const { value } = event.target;
    setSettings((prev) => ({
      ...prev,
      [group]: {
        ...prev[group],
        [field]: value,
      },
    }));
  };

  const handleSave = async () => {
    resetMessages();
    const hasNegativeValue = (value) => {
      if (value === "" || value === null || value === undefined) {
        return false;
      }
      const numeric = Number(value);
      return Number.isNaN(numeric) ? false : numeric < 0;
    };

    const containsNegative = (value) => {
      if (value && typeof value === "object") {
        return Object.values(value).some((entry) => containsNegative(entry));
      }
      return hasNegativeValue(value);
    };

    if (containsNegative(settings)) {
      setError(t("admin.estimateSettingsNonNegativeError"));
      return;
    }

    setSaving(true);
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });
      const payload = buildPayload(settings);
      const response = await api.put("/estimates/config", payload, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setSettings(normalizeSettings(response.data));
      setSuccess(t("admin.estimateSettingsSaved"));
      if (onSuccess) {
        onSuccess();
      }
    } catch (err) {
      console.error("Failed to save estimate settings", err);
      setError(t("admin.estimateSettingsFailed"));
    } finally {
      setSaving(false);
    }
  };

  const overlayContent = useMemo(() => {
    if (!loading) return null;
    return (
      <div className="absolute inset-0 bg-white/70 flex items-center justify-center z-10">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-yellow-400 border-t-transparent" />
      </div>
    );
  }, [loading]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="relative bg-white rounded-xl shadow-2xl max-w-5xl w-full max-h-[90vh] flex flex-col">
        {overlayContent}
        <div className="flex items-center justify-between p-6 border-b border-black/10">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">{t("admin.estimateSettingsTitle")}</h2>
            <p className="text-sm text-black/60 mt-1">{t("admin.estimateSettingsSubtitle")}</p>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-black/5 rounded-lg transition-colors"
            disabled={saving}
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6 space-y-8">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}
          {success && (
            <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
              {success}
            </div>
          )}

          <section className="space-y-3">
            <SectionHeader
              title={t("admin.estimateSettingsCore")}
              isOpen={openSections.core}
              onToggle={toggleSection("core")}
            />
            {openSections.core && (
              <div className="pl-7 grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-4 mt-3">
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldLaborRate")}</span>
                  <input {...numberInputProps} value={settings.laborRate} onChange={handleChange("laborRate")} />
                </label>
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldOverheadRate")}</span>
                  <input {...numberInputProps} value={settings.overheadRate} onChange={handleChange("overheadRate")} />
                </label>
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldContingencyRate")}</span>
                  <input {...numberInputProps} value={settings.contingencyRate} onChange={handleChange("contingencyRate")} />
                </label>
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldTaxRate")}</span>
                  <input {...numberInputProps} value={settings.taxRate} onChange={handleChange("taxRate")} />
                </label>
              </div>
            )}
          </section>

          <section className="space-y-3">
            <SectionHeader title={t("admin.sectionSiding")} isOpen={openSections.siding} onToggle={toggleSection("siding")} />
            {openSections.siding && (
              <div className="pl-7 space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldSidingExtraLaborPerStory")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.sidingExtraLaborPerStoryRate}
                      onChange={handleChange("sidingExtraLaborPerStoryRate")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldInsulationAdder")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.insulationAdderPerSqFt}
                      onChange={handleChange("insulationAdderPerSqFt")}
                    />
                  </label>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldVinylFactor")}</span>
                    <input {...numberInputProps} value={settings.sidingFactors.vinyl} onChange={handleNestedChange("sidingFactors", "vinyl")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldWoodFactor")}</span>
                    <input {...numberInputProps} value={settings.sidingFactors.wood} onChange={handleNestedChange("sidingFactors", "wood")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldFiberCementFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.sidingFactors.fiberCement}
                      onChange={handleNestedChange("sidingFactors", "fiberCement")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldBrickFactor")}</span>
                    <input {...numberInputProps} value={settings.sidingFactors.brick} onChange={handleNestedChange("sidingFactors", "brick")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldStoneVeneerFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.sidingFactors.stoneVeneer}
                      onChange={handleNestedChange("sidingFactors", "stoneVeneer")}
                    />
                  </label>
                </div>
              </div>
            )}
          </section>

          <section className="space-y-3">
            <SectionHeader title={t("admin.sectionRoofing")} isOpen={openSections.roofing} onToggle={toggleSection("roofing")} />
            {openSections.roofing && (
              <div className="pl-7 space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldRoofingExtraLaborPerStory")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.roofingExtraLaborPerStoryRate}
                      onChange={handleChange("roofingExtraLaborPerStoryRate")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldPitchFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.roofPitchFactorPerUnit}
                      onChange={handleChange("roofPitchFactorPerUnit")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldTearOffCost")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.roofTearOffCostPerSqFt}
                      onChange={handleChange("roofTearOffCostPerSqFt")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldSkylightCost")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.roofSkylightCost}
                      onChange={handleChange("roofSkylightCost")}
                    />
                  </label>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldAsphaltFactor")}</span>
                    <input {...numberInputProps} value={settings.roofFactors.asphalt} onChange={handleNestedChange("roofFactors", "asphalt")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldMetalFactor")}</span>
                    <input {...numberInputProps} value={settings.roofFactors.metal} onChange={handleNestedChange("roofFactors", "metal")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldClayFactor")}</span>
                    <input {...numberInputProps} value={settings.roofFactors.clay} onChange={handleNestedChange("roofFactors", "clay")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldSlateFactor")}</span>
                    <input {...numberInputProps} value={settings.roofFactors.slate} onChange={handleNestedChange("roofFactors", "slate")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldSyntheticFactor")}</span>
                    <input {...numberInputProps} value={settings.roofFactors.synthetic} onChange={handleNestedChange("roofFactors", "synthetic")} />
                  </label>
                </div>
              </div>
            )}
          </section>

          <section className="space-y-3">
            <SectionHeader title={t("admin.sectionKitchen")} isOpen={openSections.kitchen} onToggle={toggleSection("kitchen")} />
            {openSections.kitchen && (
              <div className="pl-7 grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-4 mt-3">
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldPlumbingCost")}</span>
                  <input
                    {...numberInputProps}
                    value={settings.kitchenPlumbingCost}
                    onChange={handleChange("kitchenPlumbingCost")}
                  />
                </label>
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldElectricalCost")}</span>
                  <input
                    {...numberInputProps}
                    value={settings.kitchenElectricalCost}
                    onChange={handleChange("kitchenElectricalCost")}
                  />
                </label>
              </div>
            )}
          </section>

          <section className="space-y-3">
            <SectionHeader title={t("admin.sectionWindowDoor")} isOpen={openSections.windowDoor} onToggle={toggleSection("windowDoor")} />
            {openSections.windowDoor && (
              <div className="pl-7 space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldWindowBaseCost")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.windowBaseCostPerUnit}
                      onChange={handleChange("windowBaseCostPerUnit")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldDoorBaseCost")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.doorBaseCostPerUnit}
                      onChange={handleChange("doorBaseCostPerUnit")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldLaborRateMultiplier")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.windowDoorLaborRateMultiplier}
                      onChange={handleChange("windowDoorLaborRateMultiplier")}
                    />
                  </label>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldCasementFactor")}</span>
                    <input {...numberInputProps} value={settings.windowFactors.casement} onChange={handleNestedChange("windowFactors", "casement")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldSliderFactor")}</span>
                    <input {...numberInputProps} value={settings.windowFactors.slider} onChange={handleNestedChange("windowFactors", "slider")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldDoubleHungFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.windowFactors.doubleHung}
                      onChange={handleNestedChange("windowFactors", "doubleHung")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldAwningFactor")}</span>
                    <input {...numberInputProps} value={settings.windowFactors.awning} onChange={handleNestedChange("windowFactors", "awning")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldFixedFactor")}</span>
                    <input {...numberInputProps} value={settings.windowFactors.fixed} onChange={handleNestedChange("windowFactors", "fixed")} />
                  </label>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldWoodDoorFactor")}</span>
                    <input {...numberInputProps} value={settings.doorFactors.wood} onChange={handleNestedChange("doorFactors", "wood")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldFiberglassDoorFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.doorFactors.fiberglass}
                      onChange={handleNestedChange("doorFactors", "fiberglass")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldSteelDoorFactor")}</span>
                    <input {...numberInputProps} value={settings.doorFactors.steel} onChange={handleNestedChange("doorFactors", "steel")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldGlassPanelFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.doorFactors.glassPanel}
                      onChange={handleNestedChange("doorFactors", "glassPanel")}
                    />
                  </label>
                </div>
              </div>
            )}
          </section>

          <section className="space-y-3">
            <SectionHeader
              title={t("admin.sectionDeck")}
              isOpen={openSections.deck}
              onToggle={toggleSection("deck")}
            />
            {openSections.deck && (
              <div className="pl-7 space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldBaseMaterialCost")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.deckBaseMaterialCostPerSqFt}
                      onChange={handleChange("deckBaseMaterialCostPerSqFt")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldRailingCost")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.deckRailingCostPerLinearFoot}
                      onChange={handleChange("deckRailingCostPerLinearFoot")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldStairsCost")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.deckStairsCost}
                      onChange={handleChange("deckStairsCost")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldCoverCost")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.deckCoverCostPerSqFt}
                      onChange={handleChange("deckCoverCostPerSqFt")}
                    />
                  </label>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldWoodDeckFactor")}</span>
                    <input {...numberInputProps} value={settings.deckFactors.wood} onChange={handleNestedChange("deckFactors", "wood")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldCompositeFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.deckFactors.composite}
                      onChange={handleNestedChange("deckFactors", "composite")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldPVCFactor")}</span>
                    <input {...numberInputProps} value={settings.deckFactors.pvc} onChange={handleNestedChange("deckFactors", "pvc")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldAluminumFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.deckFactors.aluminum}
                      onChange={handleNestedChange("deckFactors", "aluminum")}
                    />
                  </label>
                </div>
              </div>
            )}
          </section>

          <section className="space-y-3">
            <SectionHeader title={t("admin.sectionFlooring")} isOpen={openSections.flooring} onToggle={toggleSection("flooring")} />
            {openSections.flooring && (
              <div className="pl-7 space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldSubfloorRepairCost")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.floorSubfloorRepairCostPerSqFt}
                      onChange={handleChange("floorSubfloorRepairCostPerSqFt")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldRemovalBaseCost")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.floorRemovalBaseCostPerSqFt}
                      onChange={handleChange("floorRemovalBaseCostPerSqFt")}
                    />
                  </label>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-4">
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldHardwoodFactor")}</span>
                    <input {...numberInputProps} value={settings.flooringFactors.hardwood} onChange={handleNestedChange("flooringFactors", "hardwood")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldEngineeredHardwoodFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.flooringFactors.engineeredHardwood}
                      onChange={handleNestedChange("flooringFactors", "engineeredHardwood")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldLaminateFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.flooringFactors.laminate}
                      onChange={handleNestedChange("flooringFactors", "laminate")}
                    />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldVinylFlooringFactor")}</span>
                    <input {...numberInputProps} value={settings.flooringFactors.vinyl} onChange={handleNestedChange("flooringFactors", "vinyl")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldTileFactor")}</span>
                    <input {...numberInputProps} value={settings.flooringFactors.tile} onChange={handleNestedChange("flooringFactors", "tile")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldCarpetFactor")}</span>
                    <input {...numberInputProps} value={settings.flooringFactors.carpet} onChange={handleNestedChange("flooringFactors", "carpet")} />
                  </label>
                  <label className="flex flex-col gap-2">
                    <span className="text-sm font-semibold text-black/70">{t("admin.fieldPolishedConcreteFactor")}</span>
                    <input
                      {...numberInputProps}
                      value={settings.flooringFactors.polishedConcrete}
                      onChange={handleNestedChange("flooringFactors", "polishedConcrete")}
                    />
                  </label>
                </div>
              </div>
            )}
          </section>

          <section className="space-y-3">
            <SectionHeader
              title={t("admin.sectionFlooringRemoval")}
              isOpen={openSections.flooringRemoval}
              onToggle={toggleSection("flooringRemoval")}
            />
            {openSections.flooringRemoval && (
              <div className="pl-7 grid grid-cols-1 md:grid-cols-1 lg:grid-cols-2 gap-4 mt-3">
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldHardwoodRemoval")}</span>
                  <input
                    {...numberInputProps}
                    value={settings.flooringRemovalFactors.hardwood}
                    onChange={handleNestedChange("flooringRemovalFactors", "hardwood")}
                  />
                </label>
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldEngineeredHardwoodRemoval")}</span>
                  <input
                    {...numberInputProps}
                    value={settings.flooringRemovalFactors.engineeredHardwood}
                    onChange={handleNestedChange("flooringRemovalFactors", "engineeredHardwood")}
                  />
                </label>
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldLaminateRemoval")}</span>
                  <input
                    {...numberInputProps}
                    value={settings.flooringRemovalFactors.laminate}
                    onChange={handleNestedChange("flooringRemovalFactors", "laminate")}
                  />
                </label>
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldVinylRemoval")}</span>
                  <input
                    {...numberInputProps}
                    value={settings.flooringRemovalFactors.vinyl}
                    onChange={handleNestedChange("flooringRemovalFactors", "vinyl")}
                  />
                </label>
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldTileRemoval")}</span>
                  <input
                    {...numberInputProps}
                    value={settings.flooringRemovalFactors.tile}
                    onChange={handleNestedChange("flooringRemovalFactors", "tile")}
                  />
                </label>
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldCarpetRemoval")}</span>
                  <input
                    {...numberInputProps}
                    value={settings.flooringRemovalFactors.carpet}
                    onChange={handleNestedChange("flooringRemovalFactors", "carpet")}
                  />
                </label>
                <label className="flex flex-col gap-2">
                  <span className="text-sm font-semibold text-black/70">{t("admin.fieldPolishedConcreteRemoval")}</span>
                  <input
                    {...numberInputProps}
                    value={settings.flooringRemovalFactors.polishedConcrete}
                    onChange={handleNestedChange("flooringRemovalFactors", "polishedConcrete")}
                  />
                </label>
              </div>
            )}
          </section>
        </div>

        <div className="p-6 border-t border-black/10 flex flex-col sm:flex-row gap-3 justify-end bg-black/[0.02]">
          <button
            onClick={onClose}
            className="px-6 py-2 border border-black/20 rounded-lg font-semibold hover:bg-black/5 transition-colors"
            disabled={saving}
          >
            {t("close")}
          </button>
          <button
            onClick={handleSave}
            className="px-6 py-2 bg-yellow-400 hover:bg-yellow-500 text-black rounded-lg font-semibold transition-colors disabled:opacity-60 disabled:hover:bg-yellow-400"
            disabled={saving}
          >
            {saving ? t("admin.estimateSettingsSaving") : t("admin.estimateSettingsSave")}
          </button>
        </div>
      </div>
    </div>
  );
};

export default EstimateSettingsModal;
