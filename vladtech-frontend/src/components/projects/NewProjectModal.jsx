import { useState, useEffect } from "react";
import {api} from "../../api/http";
import { useAuth0 } from "@auth0/auth0-react";
import EmployeeFinderModal from "../projects/EmployeeFinderModal.jsx";

const NewProjectModal = ({ isOpen, onClose, onProjectCreated, defaultDate }) => {
  const { getAccessTokenSilently } = useAuth0();
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    startDate: "",
    dueDate: "",
    projectType: "",
    assignedEmployeeIds: [],
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
  const [isEmployeeModalOpen, setIsEmployeeModalOpen] = useState(false);
const [selectedEmployees, setSelectedEmployees] = useState([]); // [{id,email,name}]


  useEffect(() => {
    if (defaultDate) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setFormData(prev => ({ ...prev, startDate: defaultDate, dueDate: defaultDate }));
    }
  }, [defaultDate]);

  const validateForm = () => {
    const newErrors = {};
    if (!formData.name.trim()) newErrors.name = "Project name is required";
    if (!formData.dueDate) newErrors.dueDate = "Due date is required";
    if (!formData.projectType) newErrors.projectType = "Project type is required";
    if (!formData.address.city.trim()) newErrors.city = "City is required";
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleToggleEmployee = (employee) => {
  const empId = employee.id ?? employee.userId;
  if (!empId) return;

  setSelectedEmployees((prev) => {
    const exists = prev.some((e) => (e.id ?? e.userId) === empId);

    const updated = exists
      ? prev.filter((e) => (e.id ?? e.userId) !== empId)
      : [...prev, employee];

    setFormData((prevForm) => ({
      ...prevForm,
      assignedEmployeeIds: updated.map((e) => e.id ?? e.userId).filter(Boolean),
    }));

    return updated;
  });
};


const handleClearEmployees = () => {
  setSelectedEmployees([]);
  setFormData((prev) => ({ ...prev, assignedEmployeeIds: [] }));
};

const assignEmployeesToProject = async (projectIdentifier, employeeIds, token) => {
  for (const id of employeeIds) {
    const encodedId = encodeURIComponent(id);

    try {
      await api.post(
        `/projects/${projectIdentifier}/assign/${encodedId}`,
        null,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      console.log("ASSIGN OK", projectIdentifier, id);
    } catch (e) {
      console.error("ASSIGN FAILED", projectIdentifier, id, e?.response?.status, e?.response?.data);
      throw e; // optional: stop the flow so you see the error immediately
    }
  }
};



const handleSubmit = async (e) => {
  e.preventDefault();
  if (!validateForm()) return;

  try {
    const token = await getAccessTokenSilently({
      authorizationParams: { audience: "https://vladtech/api" },
    });

    const payload = { ...formData }; // keep assignedEmployeeIds

    const createRes = await api.post("/projects", payload, {
      headers: { Authorization: `Bearer ${token}` },
    });

    const created = createRes.data;
    const projectIdentifier = created.projectIdentifier;

    const employeeIds = formData.assignedEmployeeIds || [];
    console.log("CREATE -> will assign employees:", employeeIds);

    if (employeeIds.length > 0) {
      await assignEmployeesToProject(projectIdentifier, employeeIds, token);
    }

    onProjectCreated();
    handleClose();
  } catch (error) {
    console.error("Error creating project:", error);
    setSubmitError(
      error.response?.data?.message || "Failed to create project. Please try again."
    );
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
      assignedEmployeeIds: [],
      address: { streetAddress: "", city: "", province: "", country: "", postalCode: "" }
    });
    setErrors({});
    setSubmitError("");
    onClose();
  };

  if (!isOpen) return null;

  return (
    <>
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
  <label className="block text-sm font-semibold text-black mb-2">
    Employees
  </label>

  <div className="flex gap-2">
    <button
      type="button"
      onClick={() => setIsEmployeeModalOpen(true)}
      className="flex-1 px-4 py-3 border-2 border-black/20 rounded-lg text-left hover:bg-black/5 transition-colors"
    >
      {selectedEmployees.length > 0 ? (
        <div className="text-sm text-black/80">
          {selectedEmployees.map((e) => e.email).join(", ")}
        </div>
      ) : (
        "Select employees"
      )}
    </button>

    {formData.assignedEmployeeIds?.length > 0 && (
      <button
        type="button"
        onClick={handleClearEmployees}
        className="px-4 py-3 border-2 border-black/20 rounded-lg hover:bg-red-50 hover:border-red-400 transition-colors"
      >
        Clear
      </button>
    )}
  </div>
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
              Create
            </button>
          </div>
        </form>
      </div>
    </div>
    <EmployeeFinderModal
      isOpen={isEmployeeModalOpen}
      onClose={() => setIsEmployeeModalOpen(false)}
      selectedEmployeeIds={formData.assignedEmployeeIds}
      onToggleEmployee={handleToggleEmployee}
    />
    </>
  );
};

export default NewProjectModal;