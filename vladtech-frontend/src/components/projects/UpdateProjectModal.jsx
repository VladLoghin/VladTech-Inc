import { useState } from "react";
import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";

const UpdateProjectModal = ({ isOpen, onClose, onProjectCreated }) => {
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
  const [submitError, setSubmitError] = useState("");

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
      await axios.put("http://localhost:8080/api/projects", formData, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      onProjectCreated();
      handleClose();
    } catch (error) {
      console.error("Error creating project:", error);
      setSubmitError(error.response?.data?.message || "Failed to create project. Please try again.");
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

  const handleClose = () => {
    setFormData({
      name: "",
      description: "",
      startDate: "",
      dueDate: "",
      projectType: "",
      address: { streetAddress: "", city: "", province: "", country: "", postalCode: "" }
    });
    setErrors({});
    setSubmitError("");
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex justify-center items-center z-50">
      <div className="bg-white border-2 border-yellow-400 rounded-2xl p-8 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto shadow-2xl">
        <h2 className="text-3xl font-bold mb-6 text-black tracking-tight">New Project</h2>
        {submitError && (
          <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded mb-4">
            {submitError}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="mb-5">
            <label className="block text-sm font-semibold text-black mb-2">Project Name *</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
            />
            {errors.name && <span className="text-red-600 text-sm mt-1 block">{errors.name}</span>}
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-black mb-2">Street Address</label>
            <input
              type="text"
              name="address.streetAddress"
              value={formData.address.streetAddress}
              onChange={handleChange}
              className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
            />
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-black mb-2">City *</label>
            <input
              type="text"
              name="address.city"
              value={formData.address.city}
              onChange={handleChange}
              className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
            />
            {errors.city && <span className="text-red-600 text-sm mt-1 block">{errors.city}</span>}
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-black mb-2">Province</label>
            <input
              type="text"
              name="address.province"
              value={formData.address.province}
              onChange={handleChange}
              className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
            />
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-black mb-2">Country</label>
            <input
              type="text"
              name="address.country"
              value={formData.address.country}
              onChange={handleChange}
              className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
            />
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-black mb-2">Postal Code</label>
            <input
              type="text"
              name="address.postalCode"
              value={formData.address.postalCode}
              onChange={handleChange}
              className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
            />
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-black mb-2">Start Date</label>
            <input
              type="date"
              name="startDate"
              value={formData.startDate}
              onChange={handleChange}
              className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
            />
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-black mb-2">Due Date *</label>
            <input
              type="date"
              name="dueDate"
              value={formData.dueDate}
              onChange={handleChange}
              className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
            />
            {errors.dueDate && <span className="text-red-600 text-sm mt-1 block">{errors.dueDate}</span>}
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-black mb-2">Project Type *</label>
            <select
              name="projectType"
              value={formData.projectType}
              onChange={handleChange}
              className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
            >
              <option value="">Select</option>
              <option value="APPOINTMENT">Appointment</option>
              <option value="SCHEDULED">Scheduled</option>
            </select>
            {errors.projectType && <span className="text-red-600 text-sm mt-1 block">{errors.projectType}</span>}
          </div>

          <div className="mb-6">
            <label className="block text-sm font-semibold text-black mb-2">Description</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black min-h-[100px] resize-none"
            />
          </div>

          <div className="flex gap-4 justify-end">
            <button
              type="button"
              onClick={handleClose}
              className="px-8 py-3 border-2 border-black text-black rounded-lg hover:bg-black hover:text-white transition-all font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-8 py-3 bg-yellow-400 text-black rounded-lg hover:bg-yellow-500 transition-all font-semibold shadow-lg"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UpdateProjectModal;