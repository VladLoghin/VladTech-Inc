import { useState, useEffect, useRef } from "react";
//import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";
import { useTranslation } from "react-i18next";
import ClientFinderModal from "./ClientFinderModal.jsx";
import EmployeeFinderModal from "./EmployeeFinderModal.jsx";
import { api } from "../../api/http";
import { countries, provinces } from "../../utils/locationData";

// Time conversion constants
const SECONDS_PER_MINUTE = 60;
const SECONDS_PER_HOUR = 3600;
const SECONDS_PER_DAY = 86400;
const SECONDS_PER_WEEK = 604800;
const SECONDS_PER_MONTH = 2592000; // 30 days
const SECONDS_PER_YEAR = 31536000; // 365 days

// Format seconds to Jira-like time format for display
const formatSecondsToJiraTime = (seconds) => {
  if (!seconds || seconds <= 0) return "";
  
  const parts = [];
  let remaining = seconds;
  
  const years = Math.floor(remaining / SECONDS_PER_YEAR);
  if (years > 0) {
    parts.push(`${years}y`);
    remaining -= years * SECONDS_PER_YEAR;
  }
  
  const months = Math.floor(remaining / SECONDS_PER_MONTH);
  if (months > 0) {
    parts.push(`${months}mo`);
    remaining -= months * SECONDS_PER_MONTH;
  }
  
  const weeks = Math.floor(remaining / SECONDS_PER_WEEK);
  if (weeks > 0) {
    parts.push(`${weeks}w`);
    remaining -= weeks * SECONDS_PER_WEEK;
  }
  
  const days = Math.floor(remaining / SECONDS_PER_DAY);
  if (days > 0) {
    parts.push(`${days}d`);
    remaining -= days * SECONDS_PER_DAY;
  }
  
  const hours = Math.floor(remaining / SECONDS_PER_HOUR);
  if (hours > 0) {
    parts.push(`${hours}h`);
  }
  
  return parts.join(" ");
};

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
  estimatedTime: "",
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
  
  // Time estimate breakdown
  const [timeEstimate, setTimeEstimate] = useState({
    years: 0,
    months: 0,
    days: 0,
    hours: 0
  });

  // Refs for native browser validation
  const nameRef = useRef(null);
  const streetAddressRef = useRef(null);
  const cityRef = useRef(null);
  const countryRef = useRef(null);
  const provinceRef = useRef(null);
  const postalCodeRef = useRef(null);
  const dueDateRef = useRef(null);
  const projectTypeRef = useRef(null);
  const startDateRef = useRef(null);

  const isEdit = mode === "edit";
  
  // Helper to break down seconds into time units
  const breakdownSeconds = (seconds) => {
    if (!seconds || seconds <= 0) return { years: 0, months: 0, days: 0, hours: 0 };
    
    let remaining = seconds;
    const years = Math.floor(remaining / SECONDS_PER_YEAR);
    remaining -= years * SECONDS_PER_YEAR;
    
    const months = Math.floor(remaining / SECONDS_PER_MONTH);
    remaining -= months * SECONDS_PER_MONTH;
    
    const days = Math.floor(remaining / SECONDS_PER_DAY);
    remaining -= days * SECONDS_PER_DAY;
    
    const hours = Math.floor(remaining / SECONDS_PER_HOUR);
    
    return { years, months, days, hours };
  };

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
        estimatedCost: initialData.estimatedCost || "",
        estimatedCostCurrency: initialData.estimatedCostCurrency || "CAD",
        estimatedTime: initialData.estimatedTime || "",
        priority: initialData.priority || "MEDIUM",
      });
      // Break down estimatedTime for display
      if (initialData.estimatedTime) {
        const breakdown = breakdownSeconds(initialData.estimatedTime);
        setTimeEstimate(breakdown);
      }
    } else if (!isEdit) {
       
      setFormData(EMPTY_FORM);
      setTimeEstimate({ years: 0, months: 0, days: 0, hours: 0 });
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
    streetAddressRef.current?.setCustomValidity("");
    cityRef.current?.setCustomValidity("");
    countryRef.current?.setCustomValidity("");
    provinceRef.current?.setCustomValidity("");
    postalCodeRef.current?.setCustomValidity("");
    dueDateRef.current?.setCustomValidity("");
    projectTypeRef.current?.setCustomValidity("");
    startDateRef.current?.setCustomValidity("");
    
    let isValid = true;
    let firstInvalidRef = null;
    
    // Validate name is not empty
    if (!formData.name?.trim()) {
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

    // Address validation hierarchy:
    // Country -> Province -> City -> Street Address
    const { streetAddress, city, country, province } = formData.address;

    const hasStreet = (streetAddress || "").length > 0;
    const hasCity = (city || "").length > 0;
    const hasProvince = (province || "").length > 0;
    const hasCountry = (country || "").length > 0;

    // Street Address requires City, Province, Country
    if (hasStreet) {
      if (!hasCity) {
        cityRef.current?.setCustomValidity(`${t('project.city')} is required`);
        if (!firstInvalidRef) firstInvalidRef = cityRef;
        isValid = false;
      }
      if (!hasProvince) {
        provinceRef.current?.setCustomValidity(`${t('project.province')} is required`);
        if (!firstInvalidRef) firstInvalidRef = provinceRef;
        isValid = false;
      }
      if (!hasCountry) {
        countryRef.current?.setCustomValidity(`${t('project.country')} is required`);
        if (!firstInvalidRef) firstInvalidRef = countryRef;
        isValid = false;
      }
    }

    // City requires Province, Country
    if (hasCity && !hasStreet) { // only trigger if street didn't already
      if (!hasProvince) {
        provinceRef.current?.setCustomValidity(`${t('project.province')} is required`);
        if (!firstInvalidRef) firstInvalidRef = provinceRef;
        isValid = false;
      }
      if (!hasCountry) {
        countryRef.current?.setCustomValidity(`${t('project.country')} is required`);
        if (!firstInvalidRef) firstInvalidRef = countryRef;
        isValid = false;
      }
    }

    // Province requires Country
    if (hasProvince && !hasCity && !hasStreet) { // only trigger if city/street didn't already
      if (!hasCountry) {
        countryRef.current?.setCustomValidity(`${t('project.country')} is required`);
        if (!firstInvalidRef) firstInvalidRef = countryRef;
        isValid = false;
      }
    }

    // Postal code length verification
    if (formData.address.postalCode?.trim() && !postalCodeRef.current?.validationMessage) {
      const pc = formData.address.postalCode.replace(/[\s-]/g, "");
      const isCanada = formData.address.country === "Canada";
      const isUS = formData.address.country === "United States";
      
      let pcValid = true;
      if (isCanada && pc.length !== 6) pcValid = false;
      if (isUS && pc.length !== 5 && pc.length !== 9) pcValid = false;
      
      if (!pcValid) {
        postalCodeRef.current?.setCustomValidity(t("project.invalidPostalCode"));
        if (!firstInvalidRef) firstInvalidRef = postalCodeRef;
        isValid = false;
      }
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

      // Calculate total seconds from time estimate breakdown
      const estimatedTimeSeconds = 
        (timeEstimate.years * SECONDS_PER_YEAR) +
        (timeEstimate.months * SECONDS_PER_MONTH) +
        (timeEstimate.days * SECONDS_PER_DAY) +
        (timeEstimate.hours * SECONDS_PER_HOUR);
      
      const finalEstimatedTime = estimatedTimeSeconds > 0 ? estimatedTimeSeconds : null;

      if (isEdit) {
        const before = initialData?.assignedEmployeeIds || [];
        const after = formData.assignedEmployeeIds || [];
        const newlyAdded = after.filter((id) => !before.includes(id));

        const payload = { ...formData, estimatedTime: finalEstimatedTime };
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
        const payload = { ...formData, estimatedTime: finalEstimatedTime };
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
      setFormData((prev) => {
        const updatedParent = { ...prev[parent], [child]: value };
        
        // Reset province if country changes
        if (parent === "address" && child === "country") {
          updatedParent.province = "";
        }
        
        return {
          ...prev,
          [parent]: updatedParent,
        };
      });
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
                ref={streetAddressRef}
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

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-5">
              <div>
                <label className="block text-sm font-semibold mb-2">{t('project.country')}</label>
                <select
                  ref={countryRef}
                  name="address.country"
                  value={formData.address.country}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border-2 border-black/20 rounded-lg bg-white"
                >
                  <option value="">{t('project.select')}</option>
                  {countries.map(c => (
                    <option key={c.code} value={c.name}>{c.flag} {c.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-semibold mb-2">{t('project.province')}</label>
                <select
                  ref={provinceRef}
                  name="address.province"
                  value={formData.address.province}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border-2 border-black/20 rounded-lg bg-white"
                  disabled={!formData.address.country}
                >
                  <option value="">{t('project.select')}</option>
                {formData.address.country && (provinces[countries.find(c => c.name === formData.address.country)?.code] || []).map(p => (
                  <option key={p.code} value={p.name}>{p.name}</option>
                ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-semibold mb-2">{t('project.postalCode')}</label>
                <input
                  ref={postalCodeRef}
                  type="text"
                  name="address.postalCode"
                  value={formData.address.postalCode}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border-2 border-black/20 rounded-lg"
                  placeholder={formData.address.country === "Canada" ? "A1A1A1" : formData.address.country === "United States" ? "10001" : ""}
                />
              </div>
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

            <div className="mb-5">
              <label className="block text-sm font-semibold mb-2">
                {t('project.estimatedTime')}
              </label>
              <div className="grid grid-cols-4 gap-3">
                <div>
                  <label className="block text-xs text-black/60 mb-1">{t('project.years')}</label>
                  <input
                    type="number"
                    min="0"
                    value={timeEstimate.years}
                    onChange={(e) => setTimeEstimate(prev => ({ ...prev, years: Math.max(0, parseInt(e.target.value) || 0) }))}
                    className="w-full px-3 py-2 border-2 border-black/20 rounded-lg text-center"
                  />
                </div>
                <div>
                  <label className="block text-xs text-black/60 mb-1">{t('project.months')}</label>
                  <input
                    type="number"
                    min="0"
                    value={timeEstimate.months}
                    onChange={(e) => setTimeEstimate(prev => ({ ...prev, months: Math.max(0, parseInt(e.target.value) || 0) }))}
                    className="w-full px-3 py-2 border-2 border-black/20 rounded-lg text-center"
                  />
                </div>
                <div>
                  <label className="block text-xs text-black/60 mb-1">{t('project.days')}</label>
                  <input
                    type="number"
                    min="0"
                    value={timeEstimate.days}
                    onChange={(e) => setTimeEstimate(prev => ({ ...prev, days: Math.max(0, parseInt(e.target.value) || 0) }))}
                    className="w-full px-3 py-2 border-2 border-black/20 rounded-lg text-center"
                  />
                </div>
                <div>
                  <label className="block text-xs text-black/60 mb-1">{t('project.hours')}</label>
                  <input
                    type="number"
                    min="0"
                    value={timeEstimate.hours}
                    onChange={(e) => setTimeEstimate(prev => ({ ...prev, hours: Math.max(0, parseInt(e.target.value) || 0) }))}
                    className="w-full px-3 py-2 border-2 border-black/20 rounded-lg text-center"
                  />
                </div>
              </div>
              {(timeEstimate.years > 0 || timeEstimate.months > 0 || timeEstimate.days > 0 || timeEstimate.hours > 0) && (
                <p className="text-xs text-black/60 mt-2">
                  Total: {formatSecondsToJiraTime(
                    (timeEstimate.years * SECONDS_PER_YEAR) +
                    (timeEstimate.months * SECONDS_PER_MONTH) +
                    (timeEstimate.days * SECONDS_PER_DAY) +
                    (timeEstimate.hours * SECONDS_PER_HOUR)
                  )}
                </p>
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