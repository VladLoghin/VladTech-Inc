import React, { useState, useEffect } from "react";
import "./Estimate.css";
import {api} from "../../api/http";
import { useLanguage } from "../../context/LanguageContext";
import { estimateTranslations } from "../../translations/estimateTranslations";

const EstimateInputModal = ({ onClose, presets = [], isOpen }) => {
    const { language } = useLanguage();
    const t = estimateTranslations[language];
    const [selectedPreset, setSelectedPreset] = useState(null);
    const [formData, setFormData] = useState({});
    const [result, setResult] = useState(null);
    const [isResultModalOpen, setIsResultModalOpen] = useState(false);
    const [toast, setToast] = useState(null);

    useEffect(() => {
        if (isOpen && presets.length > 0) {
            const defaultPreset = presets[0];
            setSelectedPreset(defaultPreset);
            setFormData(defaultPreset.defaultValues || {});
        }
    }, [isOpen, presets]);

    useEffect(() => {
        if (toast) {
            const timer = setTimeout(() => setToast(null), 4000);
            return () => clearTimeout(timer);
        }
    }, [toast]);

    const handlePresetSelect = (presetName) => {
        const preset = presets.find((p) => p.name === presetName);
        setSelectedPreset(preset);
        setFormData(preset?.defaultValues || {});
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prevData) => ({ ...prevData, [name]: value }));
    };

    const handleSubmit = async (event) => {
  event.preventDefault();

  try {
    const response = await api.get("/estimates/calculate", {
      params: formData,
    });

    setResult(response.data);
    setIsResultModalOpen(true);
  } catch (error) {
    console.error("Error submitting estimate:", error);
    setToast({
      type: "error",
      message:
        error.response?.data?.message ||
        "Failed to submit estimate. Please try again.",
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
            <div className="modal" role="dialog" aria-modal="true" onClick={(e) => {
                if (e.target === e.currentTarget) {
                    onClose();
                }
            }}
            >
                <div className="modal-content">
                    <h2>{t.enterEstimateDetails}</h2>
                    {presets.length > 0 && (
                        <div>
                            <label htmlFor="preset-select">{t.presets}:</label>
                            <select
                                id="preset-select"
                                value={selectedPreset?.name || ""}
                                onChange={(e) => handlePresetSelect(e.target.value)}
                            >
                                <option value="">{t.selectPreset}</option>
                                {presets.map((preset) => (
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
                                    <input
                                        id={field.name}
                                        type={field.type}
                                        name={field.name}
                                        value={formData[field.name] || ""}
                                        onChange={handleChange}
                                        required={field.required}
                                        min={field.type === "number" ? "0" : undefined}
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

            {/* Result Modal */}
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
                    }}>
                    <div className="modal-content">
                        <h2>{t.estimateResult}</h2>
                        <p><strong>{t.estimatedTotal}:</strong> ${result.totalPrice}</p>
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

            {/* Toast Notification */}
            {toast && (
                <div
                    className={`toast toast-${toast.type}`}
                    role="alert"
                    data-testid="estimate-toast"
                >
                    {toast.message}
                </div>
            )}
        </>
    );
};

export default EstimateInputModal;