import React, { useState, useEffect } from "react";
import "./Estimate.css";

const EstimateInputModal = ({ onSubmit, onClose, presets = [], isOpen }) => {
    const [selectedPreset, setSelectedPreset] = useState(null);
    const [formData, setFormData] = useState({});
    const [result, setResult] = useState(null); // State to store the result
    const [isResultModalOpen, setIsResultModalOpen] = useState(false); // State to manage result modal visibility

    useEffect(() => {
        if (isOpen && presets.length > 0) {
            const defaultPreset = presets[0];
            setSelectedPreset(defaultPreset);
            setFormData(defaultPreset.defaultValues || {});
        }
    }, [isOpen, presets]);

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

        const queryParams = new URLSearchParams(formData).toString();

        try {
            const response = await fetch(`http://localhost:8080/api/estimates/calculate?${queryParams}`, {
                method: "GET",
            });

            if (!response.ok) {
                throw new Error(`Server error: ${response.status} ${response.statusText}`);
            }

            const result = await response.json();
            setResult(result); // Store the result
            setIsResultModalOpen(true); // Open the result modal
        } catch (error) {
            console.error("Error submitting estimate:", error);
            alert("Failed to submit estimate. Please try again.");
        }
    };

    const handleCloseResultModal = () => {
        setIsResultModalOpen(false);
        setResult(null);
        onClose(); // Close the main modal as well
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
                    <h2>Enter Estimate Details</h2>
                    {presets.length > 0 && (
                        <div>
                            <label htmlFor="preset-select">Presets:</label>
                            <select
                                id="preset-select"
                                value={selectedPreset?.name || ""}
                                onChange={(e) => handlePresetSelect(e.target.value)}
                            >
                                <option value="">Select a preset</option>
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
                                    />
                                </div>
                            ))}
                            <div className="modal-actions">
                                <button type="submit">Submit</button>
                                <button type="button" onClick={onClose}>
                                    Close
                                </button>
                            </div>
                        </form>
                    )}
                </div>
            </div>

            {/* Result Modal */}
            {isResultModalOpen && result && (
                <div className="modal" role="dialog" aria-modal="true" onClick={(e) => {
                    if (e.target === e.currentTarget) {
                        handleCloseResultModal(); // Close both modals
                    }
                }}>
                    <div className="modal-content">
                        <h2>Estimate Result</h2>
                        <p><strong>Estimated Total:</strong> ${result.totalPrice}</p>
                        <div className="modal-actions">
                            <button type="button" onClick={handleCloseResultModal}>
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
};

export default EstimateInputModal;