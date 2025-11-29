import { useState } from "react";
import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";

const NewProjectModal = ({ isOpen, onClose, onProjectCreated }) => {
  const { getAccessTokenSilently } = useAuth0();
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    startDate: "",
    dueDate: "",
    projectType: "",
    address: {
      streetAddress: "",
      city: "",
      province: "",
      country: "",
      postalCode: ""
    }
  });
  const [errors, setErrors] = useState({});

  const validateForm = () => {
    const newErrors = {};
    if (!formData.name.trim()) newErrors.name = "Project name is required";
    if (!formData.dueDate) newErrors.dueDate = "Due date is required";
    if (!formData.projectType) newErrors.projectType = "Project type is required";
    if (!formData.address.city.trim()) newErrors.city = "City is required";
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      const token = await getAccessTokenSilently();
      await axios.post("http://localhost:8080/api/projects", formData, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      onProjectCreated();
      onClose();
      setFormData({
        name: "",
        description: "",
        startDate: "",
        dueDate: "",
        projectType: "",
        address: { streetAddress: "", city: "", province: "", country: "", postalCode: "" }
      });
    } catch (error) {
      console.error("Error creating project:", error);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name.includes(".")) {
      const [parent, child] = name.split(".");
      setFormData(prev => ({
        ...prev,
        [parent]: { ...prev[parent], [child]: value }
      }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  if (!isOpen) return null;

  return (
    <div style={{
      position: "fixed",
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: "rgba(0,0,0,0.5)",
      display: "flex",
      justifyContent: "center",
      alignItems: "center",
      zIndex: 1000
    }}>
      <div style={{
        backgroundColor: "white",
        padding: "30px",
        borderRadius: "8px",
        maxWidth: "500px",
        width: "90%",
        maxHeight: "90vh",
        overflowY: "auto"
      }}>
        <h2>New Project</h2>
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: "15px" }}>
            <label>Project Name *</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            />
            {errors.name && <span style={{ color: "red", fontSize: "12px" }}>{errors.name}</span>}
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label>Street Address</label>
            <input
              type="text"
              name="address.streetAddress"
              value={formData.address.streetAddress}
              onChange={handleChange}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            />
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label>City *</label>
            <input
              type="text"
              name="address.city"
              value={formData.address.city}
              onChange={handleChange}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            />
            {errors.city && <span style={{ color: "red", fontSize: "12px" }}>{errors.city}</span>}
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label>Province</label>
            <input
              type="text"
              name="address.province"
              value={formData.address.province}
              onChange={handleChange}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            />
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label>Country</label>
            <input
              type="text"
              name="address.country"
              value={formData.address.country}
              onChange={handleChange}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            />
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label>Postal Code</label>
            <input
              type="text"
              name="address.postalCode"
              value={formData.address.postalCode}
              onChange={handleChange}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            />
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label>Start Date</label>
            <input
              type="date"
              name="startDate"
              value={formData.startDate}
              onChange={handleChange}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            />
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label>Due Date *</label>
            <input
              type="date"
              name="dueDate"
              value={formData.dueDate}
              onChange={handleChange}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            />
            {errors.dueDate && <span style={{ color: "red", fontSize: "12px" }}>{errors.dueDate}</span>}
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label>Project Type *</label>
            <select
              name="projectType"
              value={formData.projectType}
              onChange={handleChange}
              style={{ width: "100%", padding: "8px", marginTop: "5px" }}
            >
              <option value="">Select</option>
              <option value="APPOINTMENT">Appointment</option>
              <option value="SCHEDULED">Scheduled</option>
            </select>
            {errors.projectType && <span style={{ color: "red", fontSize: "12px" }}>{errors.projectType}</span>}
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label>Description</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              style={{ width: "100%", padding: "8px", marginTop: "5px", minHeight: "80px" }}
            />
          </div>

          <div style={{ display: "flex", gap: "10px", justifyContent: "flex-end" }}>
            <button
              type="button"
              onClick={onClose}
              style={{ padding: "10px 20px", cursor: "pointer" }}
            >
              Cancel
            </button>
            <button
              type="submit"
              style={{ padding: "10px 20px", backgroundColor: "#007bff", color: "white", border: "none", cursor: "pointer" }}
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default NewProjectModal;