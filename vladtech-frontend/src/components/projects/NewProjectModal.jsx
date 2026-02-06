import { useState, useEffect, useRef } from "react";
import { api } from "../../api/http";
import { useAuth0 } from "@auth0/auth0-react";
import { useTranslation } from "react-i18next";
import EmployeeFinderModal from "../projects/EmployeeFinderModal.jsx";
import { countries, provinces } from "../../utils/locationData";

const NewProjectModal = ({ isOpen, onClose, onProjectCreated, defaultDate }) => {
  const { t } = useTranslation();
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
    },
    estimatedCost: "",
    estimatedCostCurrency: "CAD"
  });
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isEmployeeModalOpen, setIsEmployeeModalOpen] = useState(false);
  const [selectedEmployees, setSelectedEmployees] = useState([]); // [{id,email,name}]
  
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


  useEffect(() => {
    if (defaultDate) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setFormData(prev => ({ ...prev, startDate: defaultDate, dueDate: defaultDate }));
    }
  }, [defaultDate]);

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
        cityRef.current?.setCustomValidity(`${t("project.city")} is required`);
        if (!firstInvalidRef) firstInvalidRef = cityRef;
        isValid = false;
      }
      if (!hasProvince) {
        provinceRef.current?.setCustomValidity(`${t("project.province")} is required`);
        if (!firstInvalidRef) firstInvalidRef = provinceRef;
        isValid = false;
      }
      if (!hasCountry) {
        countryRef.current?.setCustomValidity(`${t("project.country")} is required`);
        if (!firstInvalidRef) firstInvalidRef = countryRef;
        isValid = false;
      }
    }

    // City requires Province, Country
    if (hasCity && !hasStreet) {
      if (!hasProvince) {
        provinceRef.current?.setCustomValidity(`${t("project.province")} is required`);
        if (!firstInvalidRef) firstInvalidRef = provinceRef;
        isValid = false;
      }
      if (!hasCountry) {
        countryRef.current?.setCustomValidity(`${t("project.country")} is required`);
        if (!firstInvalidRef) firstInvalidRef = countryRef;
        isValid = false;
      }
    }

    // Province requires Country
    if (hasProvince && !hasCity && !hasStreet) {
      if (!hasCountry) {
        countryRef.current?.setCustomValidity(`${t("project.country")} is required`);
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

    // Validate estimated cost is non-negative
    if (formData.estimatedCost && Number(formData.estimatedCost) < 0) {
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

    // Report validity on the first invalid field to show browser tooltip
    if (firstInvalidRef?.current) {
      firstInvalidRef.current.reportValidity();
    }

    setErrors({});
    return isValid;
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
      setFormData(prev => {
        const updatedParent = { ...prev[parent], [child]: value };
        if (parent === "address" && child === "country") {
          updatedParent.province = "";
        }
        return {
          ...prev,
          [parent]: updatedParent
        };
      });
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
      address: { streetAddress: "", city: "", province: "", country: "", postalCode: "" },
      estimatedCost: "",
      estimatedCostCurrency: "CAD"
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
          <h2 className="text-3xl font-bold mb-6 text-black tracking-tight">{t("project.newProject")}</h2>
          {submitError && (
            <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded mb-4">
              {submitError}
            </div>
          )}
          <form onSubmit={handleSubmit}>
            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">{t("project.projectName")} *</label>
              <input
                ref={nameRef}
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
              />
            </div>
            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">
                {t("project.employee")}
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
                    t("project.selectEmployees")
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
              <label className="block text-sm font-semibold text-black mb-2">{t("project.streetAddress")}</label>
              <input
                ref={streetAddressRef}
                type="text"
                name="address.streetAddress"
                value={formData.address.streetAddress}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
              />
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">{t("project.city")}</label>
              <input
                ref={cityRef}
                type="text"
                name="address.city"
                value={formData.address.city}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-5">
              <div>
                <label className="block text-sm font-semibold text-black mb-2">{t("project.country")}</label>
                <select
                  ref={countryRef}
                  name="address.country"
                  value={formData.address.country}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
                >
                  <option value="">{t("project.select")}</option>
                  {countries.map(c => (
                    <option key={c.code} value={c.name}>{c.flag} {c.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-semibold text-black mb-2">{t("project.province")}</label>
                <select
                  ref={provinceRef}
                  name="address.province"
                  value={formData.address.province}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
                  disabled={!formData.address.country}
                >
                  <option value="">{t("project.select")}</option>
                  {formData.address.country && (provinces[countries.find(c => c.name === formData.address.country)?.code] || []).map(p => (
                    <option key={p.code} value={p.name}>{p.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-semibold text-black mb-2">{t("project.postalCode")}</label>
                <input
                  ref={postalCodeRef}
                  type="text"
                  name="address.postalCode"
                  value={formData.address.postalCode}
                  onChange={handleChange}
                  className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
                  placeholder={formData.address.country === "Canada" ? "A1A1A1" : formData.address.country === "United States" ? "10001" : ""}
                />
              </div>
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">{t("project.startDate")}</label>
              <input
                ref={startDateRef}
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
              />
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">{t("project.dueDate")} *</label>
              <input
                ref={dueDateRef}
                type="date"
                name="dueDate"
                value={formData.dueDate}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
              />
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">{t("project.projectType")} *</label>
              <select
                ref={projectTypeRef}
                name="projectType"
                value={formData.projectType}
                onChange={handleChange}
                className="w-full px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
              >
                <option value="">{t("project.select")}</option>
                <option value="APPOINTMENT">{t("project.appointment")}</option>
                <option value="SCHEDULED">{t("project.scheduled")}</option>
              </select>
            </div>

            <div className="mb-5">
              <label className="block text-sm font-semibold text-black mb-2">{t("project.estimatedCost")}</label>
              <div className="flex gap-2">
                <select
                  name="estimatedCostCurrency"
                  value={formData.estimatedCostCurrency}
                  onChange={handleChange}
                  className="w-24 px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
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
                  className="flex-1 px-4 py-3 border-2 border-black/20 rounded-lg focus:border-yellow-400 focus:outline-none bg-white text-black"
                />
              </div>
              {errors.estimatedCost && <span className="text-red-600 text-sm mt-1 block">{errors.estimatedCost}</span>}
            </div>

            <div className="mb-6">
              <label className="block text-sm font-semibold text-black mb-2">{t("project.description")}</label>
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
                {t("cancel")}
              </button>
              <button
                type="submit"
                className="px-8 py-3 bg-yellow-400 text-black rounded-lg hover:bg-yellow-500 transition-all font-semibold shadow-lg"
              >
                {t("project.create")}
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