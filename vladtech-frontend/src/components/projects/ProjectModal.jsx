import { useState, useEffect, useRef } from "react";
//import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";
import { useTranslation } from "react-i18next";
import ClientFinderModal from "./ClientFinderModal.jsx";
import EmployeeFinderModal from "./EmployeeFinderModal.jsx";
import { api } from "../../api/http";

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
  estimatedCost: "",
  estimatedCostCurrency: "CAD",
  priority: "MEDIUM",
};

const ProjectModal = ({
  isOpen,
  onClose,
  mode = "create",
  initialData = null,
  onSubmitSuccess,
  defaultDate,
  employeeIndex = {},
}) => {
  const { t } = useTranslation();
  const { getAccessTokenSilently } = useAuth0();
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isClientModalOpen, setIsClientModalOpen] = useState(false);
  const [isEmployeeModalOpen, setIsEmployeeModalOpen] = useState(false);
  const [_selectedEmployee, setSelectedEmployee] = useState([]);

  // Refs for native browser validation
  const nameRef = useRef(null);
  const cityRef = useRef(null);
  const dueDateRef = useRef(null);
  const projectTypeRef = useRef(null);
  const startDateRef = useRef(null);

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
          postalCode: initialData.address?.postalCode || "",
        },
        estimatedCost: initialData.estimatedCost || "",
        estimatedCostCurrency: initialData.estimatedCostCurrency || "CAD",
        priority: initialData.priority || "MEDIUM",
      });
    } else if (!isEdit) {
       
      setFormData(EMPTY_FORM);
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

  useEffect(() => {
    if (isOpen && isEdit && initialData?.assignedEmployeeIds && employeeIndex) {
      const employees = initialData.assignedEmployeeIds
        .map(id => employeeIndex[id])
        .filter(emp => emp)
        .map(emp => ({ id: emp.user_id || emp.id, email: emp.email, name: emp.name }));
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setSelectedEmployee(employees);
    } else if (isOpen && !isEdit) {
       
      setSelectedEmployee([]);
    }
  }, [isOpen, isEdit, initialData, employeeIndex]);

  const validateForm = () => {
    // Clear all custom validity
    nameRef.current?.setCustomValidity("");
    dueDateRef.current?.setCustomValidity("");
    projectTypeRef.current?.setCustomValidity("");
    startDateRef.current?.setCustomValidity("");
    cityRef.current?.setCustomValidity("");
    
    let isValid = true;
    let firstInvalidRef = null;
    
    // Validate name is not empty
    if (!formData.name.trim()) {
      nameRef.current?.setCustomValidity("Project name is required");
      if (!firstInvalidRef) firstInvalidRef = nameRef;
      isValid = false;
    }
    
    // Validate due date is required
    if (!formData.dueDate) {
      dueDateRef.current?.setCustomValidity("Due date is required");
      if (!firstInvalidRef) firstInvalidRef = dueDateRef;
      isValid = false;
    }
    
    // Validate project type is required
    if (!formData.projectType) {
      projectTypeRef.current?.setCustomValidity("Project type is required");
      if (!firstInvalidRef) firstInvalidRef = projectTypeRef;
      isValid = false;
    }
    
    // Validate estimated cost is non-negative (handled by min="0" on input)
    if (formData.estimatedCost && Number(formData.estimatedCost) < 0) {
      // estimatedCost uses native min validation, but we can still set error state
      setErrors({ estimatedCost: t("project.costPositiveError") });
      isValid = false;
    }

    // Validate start date <= due date
    if (formData.startDate && formData.dueDate) {
      const start = new Date(formData.startDate);
      const due = new Date(formData.dueDate);
      if (start > due) {
        startDateRef.current?.setCustomValidity("Start date cannot be after due date");
        dueDateRef.current?.setCustomValidity("Due date cannot be before start date");
        if (!firstInvalidRef) firstInvalidRef = startDateRef;
        isValid = false;
      }
    }
    
    // Validate due date is not in the past (only for create mode)
    if (!isEdit && formData.dueDate) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const dueDate = new Date(formData.dueDate);
      dueDate.setHours(0, 0, 0, 0);
      
      if (dueDate < today) {
        dueDateRef.current?.setCustomValidity("Due date cannot be in the past");
        if (!firstInvalidRef) firstInvalidRef = dueDateRef;
        isValid = false;
      }
    }

    // Validate city if address is provided
    const hasAddressData = 
      (formData.address.streetAddress && formData.address.streetAddress.trim()) ||
      (formData.address.province && formData.address.province.trim()) ||
      (formData.address.country && formData.address.country.trim()) ||
      (formData.address.postalCode && formData.address.postalCode.trim());
    
    if (hasAddressData && !formData.address.city.trim()) {
      cityRef.current?.setCustomValidity("City is required when address information is provided");
      if (!firstInvalidRef) firstInvalidRef = cityRef;
      isValid = false;
    }

    // Report validity on the first invalid field to show browser tooltip
    if (firstInvalidRef?.current) {
      firstInvalidRef.current.reportValidity();
    }

    setErrors({});
    return isValid;
  };

  const assignEmployeesToProject = async (projectIdentifier, employeeIds, token) => {
    for (const id of employeeIds) {
      const encodedId = encodeURIComponent(id); // auth0|xxx → auth0%7Cxxx

      try {
        await api.post(
          `/projects/${projectIdentifier}/assign/${encodedId}`,
          null,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        console.log("ASSIGN SUCCESS:", projectIdentifier, id);
      } catch (e) {
        console.error(
          "ASSIGN FAILED:",
          id,
          e?.response?.status,
          e?.response?.data
        );
      }
    }
  };



  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: "https://vladtech/api",
        },
      });

      if (isEdit) {
        const before = initialData?.assignedEmployeeIds || [];
        const after = formData.assignedEmployeeIds || [];
        const newlyAdded = after.filter((id) => !before.includes(id));

        const payload = { ...formData };
        if (payload.estimatedCost === "") {
          payload.estimatedCost = null;
        }
        delete payload.assignedEmployeeIds;
        delete payload.assignedEmployeeEmails;

        await api.put(
          `/projects/${formData.projectIdentifier}`,
          payload,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        if (newlyAdded.length > 0) {
          await assignEmployeesToProject(formData.projectIdentifier, newlyAdded, token);
        }
      } else {
        const employeeIds = formData.assignedEmployeeIds || [];

        // 1) create project WITHOUT employees
        const payload = { ...formData };
        if (payload.estimatedCost === "") {
          payload.estimatedCost = null;
        }
        delete payload.assignedEmployeeIds;
        delete payload.assignedEmployeeEmails;

        const createRes = await api.post("/projects", payload, {
          headers: { Authorization: `Bearer ${token}` },
        });

        // 2) get the new identifier from backend response
        const projectIdentifier = createRes?.data?.projectIdentifier;

        if (!projectIdentifier) {
          console.error("Create response missing projectIdentifier:", createRes?.data);
          setSubmitError("Project created but missing projectIdentifier in response.");
          return;
        }

        // 3) trigger email by assigning employees
        if (employeeIds.length > 0) {
          await assignEmployeesToProject(projectIdentifier, employeeIds, token);
        }
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
    
    // Clear native validation error on change
    e.target.setCustomValidity?.("");

    if (name === "estimatedCostCurrency") {
      setFormData((prev) => {
        const oldCurrency = prev.estimatedCostCurrency;
        const newCurrency = value;
        let newCost = prev.estimatedCost;

        if (newCost && oldCurrency !== newCurrency) {
          const costNum = parseFloat(newCost);
          if (!isNaN(costNum)) {
            // Simple fixed conversion rates
            if (oldCurrency === "CAD" && newCurrency === "USD") {
              newCost = (costNum / 1.4).toFixed(2);
            } else if (oldCurrency === "USD" && newCurrency === "CAD") {
              newCost = (costNum * 1.4).toFixed(2);
            }
          }
        }

        return { ...prev, [name]: value, estimatedCost: newCost };
      });
    } else if (name.includes(".")) {
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
            {isEdit ? t('project.editProject') : t('project.newProject')}
          </h2>

          {submitError && (
            <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded mb-4">
              {submitError}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">
                {t('project.projectName')} *
              </label>
              <input
                ref={nameRef}
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              />
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">
                {t('project.client')}
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
                    t('project.selectClient')
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
                {t('project.employee')}
              </label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setIsEmployeeModalOpen(true)}
                  className="flex-1 px-4 py-3 border-2 border-black/20 rounded-lg text-left hover:bg-black/5 transition-colors"
                >
                  {formData.assignedEmployeeIds?.length > 0 ? (
                    formData.assignedEmployeeIds.map(id => {
                      const emp = employeeIndex[id];
                      if (!emp) return null;
                      return (
                        <div key={id}>
                          <div className="font-medium">{emp.name}</div>
                          <div className="text-sm text-black/60">{emp.email}</div>
                        </div>
                      );
                    }).filter(Boolean)
                  ) : (
                    t('project.selectEmployees')
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
                {t('project.streetAddress')}
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
              <label className="block text-sm font-semibold mb-2">{t('project.city')}</label>
              <input
                ref={cityRef}
                type="text"
                name="address.city"
                value={formData.address.city}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              />
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">
                {t('project.startDate')}
              </label>
              <input
                ref={startDateRef}
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              />
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">
                {t('project.dueDate')} *
              </label>
              <input
                ref={dueDateRef}
                type="date"
                name="dueDate"
                value={formData.dueDate}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              />
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">
                {t('project.projectType')} *
              </label>
              <select
                ref={projectTypeRef}
                name="projectType"
                value={formData.projectType}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              >
                <option value="">{t('project.select')}</option>
                <option value="APPOINTMENT">Appointment</option>
                <option value="SCHEDULED">Scheduled</option>
              </select>
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">
                {t('project.priority')}
              </label>
              <select
                name="priority"
                value={formData.priority}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
              >
                <option value="LOW">{t('project.priorityLow')}</option>
                <option value="MEDIUM">{t('project.priorityMedium')}</option>
                <option value="HIGH">{t('project.priorityHigh')}</option>
                <option value="URGENT">{t('project.priorityUrgent')}</option>
              </select>
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">
                {t('project.estimatedCost')}
              </label>
              <div className="flex gap-2">
                <select
                  name="estimatedCostCurrency"
                  value={formData.estimatedCostCurrency}
                  onChange={handleChange}
                  className="w-24 px-4 py-3 border-2 border-black/20 rounded-lg bg-white"
                >
                  <option value="CAD">CAD</option>
                  <option value="USD">USD</option>
                </select>
                <input
                  type="number"
                  name="estimatedCost"
                  value={formData.estimatedCost}
                  onChange={handleChange}
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  className="flex-1 px-4 py-3 border-2 border-black/20 rounded-lg"
                />
              </div>
              {errors.estimatedCost && (
                <span className="text-red-600 text-sm">{errors.estimatedCost}</span>
              )}
            </div>

            <div className="mb-6">
              <label className="block text-sm font-semibold mb-2">
                {t('project.description')}
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
                {t('cancel')}
              </button>
              <button
                type="submit"
                className="px-8 py-3 bg-yellow-400 rounded-lg shadow-lg font-semibold"
              >
                {isEdit ? t('save') : t('project.create')}
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