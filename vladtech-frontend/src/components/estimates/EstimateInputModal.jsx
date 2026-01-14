// javascript
import React, { useState, useEffect, useMemo } from "react";
import "./Estimate.css";
import { api } from "../../api/http";
import { useLanguage } from "../../context/LanguageContext";
import { estimateTranslations } from "../../translations/estimateTranslations";

const EstimateInputModal = ({ onClose, presets = [], isOpen }) => {
    const { language } = useLanguage();
    const t = estimateTranslations[language];

    const materialBasePrices = useMemo(
        () => ({
            VINYL: 3.5,
            WOOD: 6.0,
            FIBER_CEMENT: 5.0,
            BRICK: 12.0,
            STONE_VENEER: 15.0,
            OTHER: 0
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

    // Helper to auto-fill materialCostPerSqFt for siding when missing
    const withAutoPriceIfSiding = (preset, data) => {
        if (!preset || preset.projectType !== "SIDING_REPLACE") return data;
        const hasPrice = data.materialCostPerSqFt !== undefined && data.materialCostPerSqFt !== "";
        const material = data.sidingMaterial;
        if (!hasPrice && material && materialBasePrices[material] !== undefined) {
            return { ...data, materialCostPerSqFt: String(materialBasePrices[material]) };
        }
        return data;
    };

    useEffect(() => {
        if (isOpen && sortedPresets.length > 0) {
            const defaultPreset = sortedPresets[0];
            const initialValues = withAutoPriceIfSiding(defaultPreset, defaultPreset.defaultValues || {});
            setSelectedPreset(defaultPreset);
            setFormData(initialValues);
        }
    }, [isOpen, sortedPresets, materialBasePrices]);

    useEffect(() => {
        if (toast) {
            const timer = setTimeout(() => setToast(null), 4000);
            return () => clearTimeout(timer);
        }
    }, [toast]);

    // Still update the price if user changes material
    useEffect(() => {
        if (!selectedPreset || selectedPreset.projectType !== "SIDING_REPLACE") return;
        const material = formData.sidingMaterial;
        if (!material) return;
        const autoPrice = materialBasePrices[material];
        if (autoPrice !== undefined) {
            setFormData((prev) => ({
                ...prev,
                materialCostPerSqFt: String(autoPrice),
            }));
        }
    }, [formData.sidingMaterial, selectedPreset, materialBasePrices]);

    const handlePresetSelect = (presetName) => {
        const preset = sortedPresets.find((p) => p.name === presetName);
        const values = withAutoPriceIfSiding(preset, preset?.defaultValues || {});
        setSelectedPreset(preset || null);
        setFormData(values);
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        const nextValue = type === "checkbox" ? checked : value;

        setFormData((prevData) => {
            const updated = { ...prevData, [name]: nextValue };
            if (selectedPreset?.projectType === "SIDING_REPLACE" && name === "sidingMaterial") {
                const autoPrice = materialBasePrices[nextValue];
                if (autoPrice !== undefined) {
                    updated.materialCostPerSqFt = String(autoPrice);
                }
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
                ...(selectedPreset?.projectType ? { projectType: selectedPreset.projectType } : {}),
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
                            {selectedPreset.fields.map((field) => (
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
                            ))}

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