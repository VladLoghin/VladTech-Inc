import { useState, useEffect } from "react";
import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";
import ClientFinderModal from "./ClientFinderModal.jsx";
import EmployeeFinderModal from "./EmployeeFinderModal.jsx";

const EMPTY_FORM = {
  name: "",
  description: "",
  startDate: "",
  dueDate: "",
  projectType: "",
  clientId: "",
  clientName: "",
  clientEmail: "",
  assignedEmployeeIds: [],
  address: {
    streetAddress: "",
    city: "",
    province: "",
    country: "",
    postalCode: "",
  },
};

const ProjectModal = ({
  isOpen,
  onClose,
  mode = "create",
  initialData = null,
  onSubmitSuccess,
  defaultDate,
}) => {
  const { getAccessTokenSilently } = useAuth0();
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isClientModalOpen, setIsClientModalOpen] = useState(false);
  const [isEmployeeModalOpen, setIsEmployeeModalOpen] = useState(false);
  const [selectedEmployee, setSelectedEmployee] = useState([]);

  const isEdit = mode === "edit";

  useEffect(() => {
    if (isEdit && initialData) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setFormData({
        ...initialData,
        clientId: initialData.clientId || "",
        clientName: initialData.clientName || "",
        clientEmail: initialData.clientEmail || "",
        assignedEmployeeIds: initialData.assignedEmployeeIds || [],
        address: {
          streetAddress: initialData.address?.streetAddress || "",
          city: initialData.address?.city || "",
          province: initialData.address?.province || "",
          country: initialData.address?.country || "",
          postalCode: initialData.address?.postalCode || "",
        },
      });
    }
  }, [isEdit, initialData]);

  useEffect(() => {
    if (!isEdit && defaultDate) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setFormData((prev) => ({
        ...prev,
        startDate: defaultDate,
        dueDate: defaultDate,
      }));
    }
  }, [defaultDate, isEdit]);
  
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

      if (isEdit) {
        await axios.put(
          `http://localhost:8080/api/projects/${formData.projectIdentifier}`,
          formData,
          { headers: { Authorization: `Bearer ${token}` } }
        );
      } else {
        await axios.post("http://localhost:8080/api/projects", formData, {
          headers: { Authorization: `Bearer ${token}` },
        });
      }

      onSubmitSuccess();
      handleClose();
    } catch (error) {
      console.error("Error submitting project:", error);
      setSubmitError(error.response?.data?.message || "Failed to save project.");
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    if (name.includes(".")) {
      const [parent, child] = name.split(".");
      setFormData((prev) => ({
        ...prev,
        [parent]: { ...prev[parent], [child]: value },
      }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSelectClient = (client) => {
    setFormData((prev) => ({
      ...prev,
      clientId: client.id,
      clientName: client.name,
      clientEmail: client.email,
    }));
  };

  const handleClearClient = () => {
    setFormData((prev) => ({
      ...prev,
      clientId: "",
      clientName: "",
      clientEmail: "",
    }));
  };

const handleSelectEmployee = (employee) => {
  setSelectedEmployee((prev) => {
    const exists = prev.some((e) => e.id === employee.id);
    let updated;

    if (exists) {
     
      updated = prev.filter((e) => e.id !== employee.id);
    } else {
      
      updated = [...prev, employee];
    }

    setFormData((prevForm) => ({
      ...prevForm,
      assignedEmployeeIds: updated.map((e) => e.id),
      
      assignedEmployeeEmails: updated.map((e) => e.email),
    }));

    return updated;
  });
};

const handleClearEmployee = () => {
  setSelectedEmployee([]);
  setFormData((prev) => ({
    ...prev,
    assignedEmployeeIds: [],
    assignedEmployeeEmails: [],
  }));
};


  const handleClose = () => {
    setFormData(EMPTY_FORM);
    setErrors({});
    setSubmitError("");
    onClose();
  };

  if (!isOpen) return null;

  return (
    <>
      <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex justify-center items-center z-50">
        <div className="bg-white border-2 border-yellow-400 rounded-2xl p-8 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto shadow-2xl">
          <h2 className="text-3xl font-bold mb-6 text-black tracking-tight">
            {isEdit ? "Update Project" : "New Project"}
          </h2>

          {submitError && (
            <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded mb-4">
              {submitError}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">
                Project Name *
              </label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              />
              {errors.name && (
                <span className="text-red-600 text-sm">{errors.name}</span>
              )}
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">
                Client
              </label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setIsClientModalOpen(true)}
                  className="flex-1 px-4 py-3 border-2 border-black/20 rounded-lg text-left hover:bg-black/5 transition-colors"
                >
                  {formData.clientName ? (
                    <div>
                      <div className="font-medium">{formData.clientName}</div>
                      <div className="text-sm text-black/60">{formData.clientEmail}</div>
                    </div>
                  ) : (
                    "Select a client"
                  )}
                </button>
                {formData.clientId && (
                  <button
                    type="button"
                    onClick={handleClearClient}
                    className="px-4 py-3 border-2 border-black/20 rounded-lg hover:bg-red-50 hover:border-red-400 transition-colors"
                  >
                    Clear
                  </button>
                )}
              </div>
            </div>

                        {/* Employee picker */}
<div className="mb-5">
  <label className="block text-sm font-semibold text-black mb-2">
    Employee
  </label>
  <div className="flex gap-2">
    <button
      type="button"
      onClick={() => setIsEmployeeModalOpen(true)}
      className="flex-1 px-4 py-3 border-2 border-black/20 rounded-lg text-left hover:bg-black/5 transition-colors"
    >
      {selectedEmployee.length > 0 ? (
  <div className="text-sm text-black/80">
    {selectedEmployee.map((e) => e.email).join(", ")}
  </div>
) : (
  "Select employees"
)}

    </button>

    {formData.assignedEmployeeIds?.length > 0 && (
      <button
        type="button"
        onClick={handleClearEmployee}
        className="px-4 py-3 border-2 border-black/20 rounded-lg hover:bg-red-50 hover:border-red-400 transition-colors"
      >
        Clear
      </button>
    )}
  </div>
</div>



            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">
                Street Address
              </label>
              <input
                type="text"
                name="address.streetAddress"
                value={formData.address.streetAddress}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              />
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">City *</label>
              <input
                type="text"
                name="address.city"
                value={formData.address.city}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              />
              {errors.city && (
                <span className="text-red-600 text-sm">{errors.city}</span>
              )}
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">
                Start Date
              </label>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              />
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">
                Due Date *
              </label>
              <input
                type="date"
                name="dueDate"
                value={formData.dueDate}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              />
              {errors.dueDate && (
                <span className="text-red-600 text-sm">{errors.dueDate}</span>
              )}
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">
                Project Type *
              </label>
              <select
                name="projectType"
                value={formData.projectType}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              >
                <option value="">Select</option>
                <option value="APPOINTMENT">Appointment</option>
                <option value="SCHEDULED">Scheduled</option>
              </select>
              {errors.projectType && (
                <span className="text-red-600 text-sm">{errors.projectType}</span>
              )}
            </div>

            <div className="mb-6">
              <label className="block text-sm font-semibold mb-2">
                Description
              </label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg min-h-[100px]"
              />
            </div>

            <div className="flex gap-4 justify-end">
              <button
                type="button"
                onClick={handleClose}
                className="px-8 py-3 border-2 border-black rounded-lg"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-8 py-3 bg-yellow-400 rounded-lg shadow-lg font-semibold"
              >
                {isEdit ? "Save" : "Create"}
              </button>
            </div>
          </form>
        </div>
      </div>

      <ClientFinderModal
        isOpen={isClientModalOpen}
        onClose={() => setIsClientModalOpen(false)}
        onSelectClient={handleSelectClient}
        selectedClientId={formData.clientId}
      />
      <EmployeeFinderModal
  isOpen={isEmployeeModalOpen}
  onClose={() => setIsEmployeeModalOpen(false)}
  selectedEmployeeIds={formData.assignedEmployeeIds}
  onToggleEmployee={handleSelectEmployee}
/>
    </>
  );
};

export default ProjectModal;