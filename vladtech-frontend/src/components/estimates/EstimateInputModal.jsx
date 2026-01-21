// javascript
import React, { useState, useEffect, useMemo } from "react";
import "./Estimate.css";
import { api } from "../../api/http";
import { useLanguage } from "../../context/LanguageContext";
import { estimateTranslations } from "../../translations/estimateTranslations";

const EstimateInputModal = ({ onClose, presets = [], isOpen }) => {
    const { language } = useLanguage();
    const t = estimateTranslations[language];

    const sidingBasePrices = useMemo(
        () => ({
            VINYL: 3.5,
            WOOD: 6.0,
            FIBER_CEMENT: 5.0,
            BRICK: 12.0,
            STONE_VENEER: 15.0,
            OTHER: 0,
        }),
        []
    );

    const roofBasePrices = useMemo(
        () => ({
            ASPHALT: 4.0,
            METAL: 7.5,
            CLAY: 9.0,
            SLATE: 12.0,
            SYNTHETIC: 6.0,
        }),
        []
    );

    const kitchenBasePrices = useMemo(
        () => ({
            STOCK: 150,
            SEMI_CUSTOM: 250,
            CUSTOM: 350,
        }),
        []
    );

    const countertopBasePrices = useMemo(
        () => ({
            LAMINATE: 35,
            BUTCHERBLOCK: 45,
            GRANITE: 70,
            QUARTZ: 80,
            MARBLE: 90,
            CONCRETE: 65,
            STAINLESS_STEEL: 75,
            SOLID_SURFACE: 60,
            TILE: 40,
        }),
        []
    );

    const flooringBasePrices = useMemo(
        () => ({
            HARDWOOD: 8,
            ENGINEERED_HARDWOOD: 6,
            LAMINATE: 3,
            VINYL: 2.5,
            TILE: 5,
            CARPET: 3.5,
            POLISHED_CONCRETE: 6,
        }),
        []
    );

    const builtInPresets = useMemo(
        () => [
            {
                name: t.sidingReplacePreset ?? "Siding Replace",
                key: "SIDING_REPLACE",
                projectType: "SIDING_REPLACE",
                defaultValues: {
                    squareFeet: "",
                    materialCostPerSqFt: "",
                    locationFactor: "1.00",
                    sidingMaterial: "VINYL",
                    stories: "1",
                    includeInsulation: false,
                },
                fields: [
                    { name: "squareFeet", label: t.squareFeet ?? "Area (sq ft)", type: "number", required: true, min: 1, step: "0.01" },
                    { name: "materialCostPerSqFt", label: t.materialCostPerSqFt, type: "number", required: true, min: 0, step: "0.01" },
                    {
                        name: "sidingMaterial",
                        label: t.sidingMaterial ?? "Siding Material",
                        type: "select",
                        required: true,
                        options: [
                            { value: "VINYL", label: t?.sidingMaterialOptions?.VINYL ?? "Vinyl" },
                            { value: "WOOD", label: t?.sidingMaterialOptions?.WOOD ?? "Wood" },
                            { value: "FIBER_CEMENT", label: t?.sidingMaterialOptions?.FIBER_CEMENT ?? "Fiber Cement" },
                            { value: "BRICK", label: t?.sidingMaterialOptions?.BRICK ?? "Brick" },
                            { value: "STONE_VENEER", label: t?.sidingMaterialOptions?.STONE_VENEER ?? "Stone Veneer" },
                        ],
                    },
                    { name: "stories", label: t.stories ?? "Stories", type: "number", required: true, min: 1 },
                    { name: "includeInsulation", label: t.includeInsulation ?? "Include Insulation", type: "checkbox", required: false },
                ],
            },
            {
                name: t.roofReplacePreset ?? "Roof Replace",
                key: "ROOFING_REPLACE",
                projectType: "ROOFING_REPLACE",
                defaultValues: {
                    squareFeet: "",
                    materialCostPerSqFt: "",
                    locationFactor: "1.00",
                    roofMaterial: "ASPHALT",
                    roofPitch: "1.0",
                    stories: "1",
                    tearOffRequired: false,
                    hasSkylights: false,
                    numSkylights: "0",
                },
                fields: [
                    { name: "squareFeet", label: t.squareFeet ?? "Area (sq ft)", type: "number", required: true, min: 1, step: "0.01" },
                    { name: "materialCostPerSqFt", label: t.materialCostPerSqFt, type: "number", required: true, min: 0, step: "0.01" },
                    {
                        name: "roofMaterial",
                        label: t.roofMaterial ?? "Roof Material",
                        type: "select",
                        required: true,
                        options: [
                            { value: "ASPHALT", label: t?.roofMaterialOptions?.ASPHALT ?? "Asphalt" },
                            { value: "METAL", label: t?.roofMaterialOptions?.METAL ?? "Metal" },
                            { value: "CLAY", label: t?.roofMaterialOptions?.CLAY ?? "Clay" },
                            { value: "SLATE", label: t?.roofMaterialOptions?.SLATE ?? "Slate" },
                            { value: "SYNTHETIC", label: t?.roofMaterialOptions?.SYNTHETIC ?? "Synthetic" },
                        ],
                    },
                    { name: "roofPitch", label: t.roofPitch ?? "Roof Pitch", type: "number", required: true, min: 0.1, step: "0.1" },
                    { name: "stories", label: t.stories ?? "Stories", type: "number", required: true, min: 1 },
                    { name: "tearOffRequired", label: t.tearOffRequired ?? "Tear Off Required", type: "checkbox", required: false },
                    { name: "hasSkylights", label: t.hasSkylights ?? "Has Skylights", type: "checkbox", required: false },
                    { name: "numSkylights", label: t.numSkylights ?? "Number of Skylights", type: "number", required: false, min: 0 },
                ],
            },
            {
                name: t.kitchenRemodelPreset ?? "Kitchen Remodel",
                key: "KITCHEN_REMODEL",
                projectType: "KITCHEN_REMODEL",
                defaultValues: {
                    squareFeet: "",
                    materialCostPerSqFt: "",
                    locationFactor: "1.00",
                    cabinetQuality: "STOCK",
                    countertopMaterial: "LAMINATE",
                    flooringMaterial: "VINYL",
                    includeApplianceAllowance: false,
                    applianceAllowance: "0",
                },
                fields: [
                    { name: "squareFeet", label: t.squareFeet ?? "Area (sq ft)", type: "number", required: true, min: 1, step: "0.01" },
                    { name: "materialCostPerSqFt", label: t.materialCostPerSqFt, type: "number", required: true, min: 0, step: "0.01" },
                    {
                        name: "cabinetQuality",
                        label: t.cabinetQuality ?? "Cabinet Quality",
                        type: "select",
                        required: true,
                        options: [
                            { value: "STOCK", label: t?.cabinetQualityOptions?.STOCK ?? "Stock" },
                            { value: "SEMI_CUSTOM", label: t?.cabinetQualityOptions?.SEMI_CUSTOM ?? "Semi-Custom" },
                            { value: "CUSTOM", label: t?.cabinetQualityOptions?.CUSTOM ?? "Custom" },
                        ],
                    },
                    {
                        name: "countertopMaterial",
                        label: t.countertopMaterial ?? "Countertop Material",
                        type: "select",
                        required: true,
                        options: [
                            { value: "LAMINATE", label: t?.countertopMaterialOptions?.LAMINATE ?? "Laminate" },
                            { value: "BUTCHERBLOCK", label: t?.countertopMaterialOptions?.BUTCHERBLOCK ?? "Butcherblock" },
                            { value: "GRANITE", label: t?.countertopMaterialOptions?.GRANITE ?? "Granite" },
                            { value: "QUARTZ", label: t?.countertopMaterialOptions?.QUARTZ ?? "Quartz" },
                            { value: "MARBLE", label: t?.countertopMaterialOptions?.MARBLE ?? "Marble" },
                            { value: "CONCRETE", label: t?.countertopMaterialOptions?.CONCRETE ?? "Concrete" },
                            { value: "STAINLESS_STEEL", label: t?.countertopMaterialOptions?.STAINLESS_STEEL ?? "Stainless Steel" },
                            { value: "SOLID_SURFACE", label: t?.countertopMaterialOptions?.SOLID_SURFACE ?? "Solid Surface" },
                            { value: "TILE", label: t?.countertopMaterialOptions?.TILE ?? "Tile" },
                        ],
                    },
                    {
                        name: "flooringMaterial",
                        label: t.flooringMaterial ?? "Flooring Material",
                        type: "select",
                        required: true,
                        options: [
                            { value: "HARDWOOD", label: t?.flooringMaterialOptions?.HARDWOOD ?? "Hardwood" },
                            { value: "ENGINEERED_HARDWOOD", label: t?.flooringMaterialOptions?.ENGINEERED_HARDWOOD ?? "Engineered Hardwood" },
                            { value: "LAMINATE", label: t?.flooringMaterialOptions?.LAMINATE ?? "Laminate" },
                            { value: "VINYL", label: t?.flooringMaterialOptions?.VINYL ?? "Vinyl" },
                            { value: "TILE", label: t?.flooringMaterialOptions?.TILE ?? "Tile" },
                            { value: "CARPET", label: t?.flooringMaterialOptions?.CARPET ?? "Carpet" },
                            { value: "POLISHED_CONCRETE", label: t?.flooringMaterialOptions?.POLISHED_CONCRETE ?? "Polished Concrete" },
                        ],
                    },
                    { name: "includeApplianceAllowance", label: t.includeApplianceAllowance ?? "Include Appliance Allowance", type: "checkbox", required: false },
                    { name: "applianceAllowance", label: t.applianceAllowance ?? "Appliance Allowance", type: "number", required: false, min: 0, step: "0.01" },
                ],
            },
        ],
        [t]
    );

    const availablePresets = useMemo(
        () => [...builtInPresets, ...presets],
        [builtInPresets, presets]
    );

    // Decide which preset is the "default": the first external preset if present, else the first built-in
    const defaultPresetName = useMemo(() => {
        if (presets && presets.length > 0) return presets[0].name;
        return builtInPresets[0]?.name;
    }, [presets, builtInPresets]);

    // Keep the default preset first; sort the rest alphabetically
    const sortedPresets = useMemo(() => {
        if (!availablePresets.length) return [];
        return [...availablePresets].sort((a, b) => {
            if (a.name === defaultPresetName) return -1;
            if (b.name === defaultPresetName) return 1;
            return a.name.localeCompare(b.name);
        });
    }, [availablePresets, defaultPresetName]);

    const [selectedPreset, setSelectedPreset] = useState(null);
    const [formData, setFormData] = useState({});
    const [result, setResult] = useState(null);
    const [isResultModalOpen, setIsResultModalOpen] = useState(false);
    const [toast, setToast] = useState(null);

    // Helper to auto-fill materialCostPerSqFt when missing based on preset type
    const withAutoPrice = (preset, data) => {
        if (!preset) return data;
        const hasPrice = data.materialCostPerSqFt !== undefined && data.materialCostPerSqFt !== "";

        if (preset.projectType === "SIDING_REPLACE") {
            const material = data.sidingMaterial;
            if (!hasPrice && material && sidingBasePrices[material] !== undefined) {
                return { ...data, materialCostPerSqFt: String(sidingBasePrices[material]) };
            }
        }

        if (preset.projectType === "ROOFING_REPLACE") {
            const material = data.roofMaterial;
            if (!hasPrice && material && roofBasePrices[material] !== undefined) {
                return { ...data, materialCostPerSqFt: String(roofBasePrices[material]) };
            }
        }

        if (preset.projectType === "KITCHEN_REMODEL") {
            if (!hasPrice) {
                const cabinet = kitchenBasePrices[data.cabinetQuality];
                const countertop = countertopBasePrices[data.countertopMaterial];
                const flooring = flooringBasePrices[data.flooringMaterial];
                const parts = [cabinet, countertop, flooring].filter((v) => v !== undefined);
                if (parts.length > 0) {
                    const avg = (parts.reduce((a, b) => a + b, 0) / parts.length).toFixed(2);
                    return { ...data, materialCostPerSqFt: String(avg) };
                }
            }
        }

        return data;
    };

     
    useEffect(() => {
        if (isOpen && sortedPresets.length > 0) {
            const defaultPreset = sortedPresets[0];
            const initialValues = withAutoPrice(defaultPreset, defaultPreset.defaultValues || {});
            setSelectedPreset(defaultPreset);
            setFormData(initialValues);
        }
    }, [isOpen, sortedPresets, sidingBasePrices, roofBasePrices, kitchenBasePrices, countertopBasePrices, flooringBasePrices]);

    useEffect(() => {
        if (toast) {
            const timer = setTimeout(() => setToast(null), 4000);
            return () => clearTimeout(timer);
        }
    }, [toast]);

     
    useEffect(() => {
        if (!selectedPreset) return;

        if (selectedPreset.projectType === "SIDING_REPLACE") {
            const material = formData.sidingMaterial;
            if (!material) return;
            const autoPrice = sidingBasePrices[material];
            if (autoPrice !== undefined) {
                setFormData((prev) => ({
                    ...prev,
                    materialCostPerSqFt: String(autoPrice),
                }));
            }
        }

        if (selectedPreset.projectType === "ROOFING_REPLACE") {
            const material = formData.roofMaterial;
            if (!material) return;
            const autoPrice = roofBasePrices[material];
            if (autoPrice !== undefined) {
                setFormData((prev) => ({
                    ...prev,
                    materialCostPerSqFt: String(autoPrice),
                }));
            }
        }

        if (selectedPreset.projectType === "KITCHEN_REMODEL") {
            const cabinet = kitchenBasePrices[formData.cabinetQuality];
            const countertop = countertopBasePrices[formData.countertopMaterial];
            const flooring = flooringBasePrices[formData.flooringMaterial];
            const parts = [cabinet, countertop, flooring].filter((v) => v !== undefined);
            if (parts.length > 0) {
                const avg = (parts.reduce((a, b) => a + b, 0) / parts.length).toFixed(2);
                setFormData((prev) => ({
                    ...prev,
                    materialCostPerSqFt: String(avg),
                }));
            }
        }
    }, [formData.sidingMaterial, formData.roofMaterial, formData.cabinetQuality, formData.countertopMaterial, formData.flooringMaterial, selectedPreset, sidingBasePrices, roofBasePrices, kitchenBasePrices, countertopBasePrices, flooringBasePrices]);

    const handlePresetSelect = (presetName) => {
        const preset = sortedPresets.find((p) => p.name === presetName);
        const values = withAutoPrice(preset, preset?.defaultValues || {});
        setSelectedPreset(preset || null);
        setFormData(values);
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        const nextValue = type === "checkbox" ? checked : value;

        setFormData((prevData) => {
            const updated = { ...prevData, [name]: nextValue };
            if (selectedPreset?.projectType === "SIDING_REPLACE" && name === "sidingMaterial") {
                const autoPrice = sidingBasePrices[nextValue];
                if (autoPrice !== undefined) {
                    updated.materialCostPerSqFt = String(autoPrice);
                }
            }
            if (selectedPreset?.projectType === "ROOFING_REPLACE" && name === "roofMaterial") {
                const autoPrice = roofBasePrices[nextValue];
                if (autoPrice !== undefined) {
                    updated.materialCostPerSqFt = String(autoPrice);
                }
            }
            if (selectedPreset?.projectType === "KITCHEN_REMODEL" && name === "cabinetQuality") {
                const cabinet = kitchenBasePrices[nextValue];
                const countertop = countertopBasePrices[prevData.countertopMaterial];
                const flooring = flooringBasePrices[prevData.flooringMaterial];
                const parts = [cabinet, countertop, flooring].filter((v) => v !== undefined);
                if (parts.length > 0) {
                    const avg = (parts.reduce((a, b) => a + b, 0) / parts.length).toFixed(2);
                    updated.materialCostPerSqFt = String(avg);
                }
            }
            if (selectedPreset?.projectType === "KITCHEN_REMODEL" && name === "countertopMaterial") {
                const cabinet = kitchenBasePrices[prevData.cabinetQuality];
                const countertop = countertopBasePrices[nextValue];
                const flooring = flooringBasePrices[prevData.flooringMaterial];
                const parts = [cabinet, countertop, flooring].filter((v) => v !== undefined);
                if (parts.length > 0) {
                    const avg = (parts.reduce((a, b) => a + b, 0) / parts.length).toFixed(2);
                    updated.materialCostPerSqFt = String(avg);
                }
            }
            if (selectedPreset?.projectType === "KITCHEN_REMODEL" && name === "flooringMaterial") {
                const cabinet = kitchenBasePrices[prevData.cabinetQuality];
                const countertop = countertopBasePrices[prevData.countertopMaterial];
                const flooring = flooringBasePrices[nextValue];
                const parts = [cabinet, countertop, flooring].filter((v) => v !== undefined);
                if (parts.length > 0) {
                    const avg = (parts.reduce((a, b) => a + b, 0) / parts.length).toFixed(2);
                    updated.materialCostPerSqFt = String(avg);
                }
            }
            if (name === "hasSkylights" && !nextValue) {
                updated.numSkylights = "0"; // reset count when skylights are toggled off
            }
            if (name === "includeApplianceAllowance" && !nextValue) {
                updated.applianceAllowance = "0";
            }
            return updated;
        });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            const params = {
                ...formData,
                locationFactor: formData.locationFactor ?? "1.00",
                projectType: selectedPreset?.projectType || "GENERAL",
            };
            const response = await api.get("/estimates/calculate", { params });
            setResult(response.data);
            setIsResultModalOpen(true);
        } catch (error) {
            console.error("Error submitting estimate:", error);
            setToast({
                type: "error",
                message: error.response?.data?.message || "Failed to submit estimate. Please try again.",
            });
        }
    };

    const handleCloseResultModal = () => {
        setIsResultModalOpen(false);
        setResult(null);
        onClose();
    };

    if (!isOpen) return null;

    return (
        <>
            <div
                className="modal"
                role="dialog"
                aria-modal="true"
                onClick={(e) => {
                    if (e.target === e.currentTarget) onClose();
                }}
            >
                <div className="modal-content">
                    <h2>{t.enterEstimateDetails}</h2>

                    {sortedPresets.length > 0 && (
                        <div>
                            <label htmlFor="preset-select">{t.presets}:</label>
                            <select
                                id="preset-select"
                                value={selectedPreset?.name || ""}
                                onChange={(e) => handlePresetSelect(e.target.value)}
                            >
                                <option value="">{t.selectPreset}</option>
                                {sortedPresets.map((preset) => (
                                    <option key={preset.name} value={preset.name}>
                                        {preset.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}

                    {selectedPreset && (
                        <form onSubmit={handleSubmit}>
                            {selectedPreset.fields.map((field) => {
                                if (field.name === "numSkylights" && !formData.hasSkylights) return null;
                                if (field.name === "applianceAllowance" && !formData.includeApplianceAllowance) return null;
                                return (
                                    <div key={field.name}>
                                        <label htmlFor={field.name}>{field.label}:</label>

                                        {field.type === "select" ? (
                                            <select
                                                id={field.name}
                                                name={field.name}
                                                value={formData[field.name] ?? ""}
                                                onChange={handleChange}
                                                required={field.required}
                                            >
                                                <option value="" disabled>
                                                    {t.selectPreset}
                                                </option>
                                                {field.options.map((opt) => (
                                                    <option key={opt.value} value={opt.value}>
                                                        {opt.label}
                                                    </option>
                                                ))}
                                            </select>
                                        ) : field.type === "checkbox" ? (
                                            <input
                                                id={field.name}
                                                type="checkbox"
                                                name={field.name}
                                                checked={!!formData[field.name]}
                                                onChange={handleChange}
                                            />
                                        ) : (
                                            <input
                                                id={field.name}
                                                type={field.type}
                                                name={field.name}
                                                value={formData[field.name] ?? ""}
                                                onChange={handleChange}
                                                required={field.required}
                                                min={field.min !== undefined ? String(field.min) : undefined}
                                                step={field.step}
                                                onInvalid={(e) => {
                                                    if (e.target.validity.valueMissing) {
                                                        e.target.setCustomValidity(`${field.label} ${t.isRequired}`);
                                                    } else if (e.target.validity.rangeUnderflow) {
                                                        e.target.setCustomValidity(`${field.label} ${t.mustBeGreaterThanZero}`);
                                                    } else if (e.target.validity.typeMismatch) {
                                                        e.target.setCustomValidity(`${field.label} ${t.mustBeValidNumber}`);
                                                    }
                                                }}
                                                onInput={(e) => e.target.setCustomValidity("")}
                                            />
                                        )}
                                    </div>
                                );
                            })}

                            <div className="modal-actions">
                                <button type="submit">{t.submit}</button>
                                <button type="button" onClick={onClose}>
                                    {t.close}
                                </button>
                            </div>
                        </form>
                    )}
                </div>
            </div>

            {isResultModalOpen && result && (
                <div
                    className="modal"
                    role="dialog"
                    aria-modal="true"
                    data-testid="estimate-result-modal"
                    onClick={(e) => {
                        if (e.target === e.currentTarget) {
                            handleCloseResultModal();
                        }
                    }}
                >
                    <div className="modal-content">
                        <h2>{t.estimateResult}</h2>
                        <p>
                            <strong>{t.estimatedTotal}:</strong> ${result.totalPrice}
                        </p>
                        <div className="modal-actions">
                            <button
                                type="button"
                                onClick={handleCloseResultModal}
                                data-testid="estimate-result-close"
                            >
                                {t.close}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {toast && (
                <div className={`toast toast-${toast.type}`} role="alert" data-testid="estimate-toast">
                    {toast.message}
                </div>
            )}
        </>
    );
};

export default EstimateInputModal;