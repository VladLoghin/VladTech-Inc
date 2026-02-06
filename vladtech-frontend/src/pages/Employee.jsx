// Employee.jsx
import { useAuth0 } from "@auth0/auth0-react";
import { useEffect, useMemo, useState, useCallback } from "react";
import { useTranslation } from "react-i18next";
import Navbar from "../components/Navbar.jsx";
import { api } from "../api/http";
import { generateCsv, generatePdf } from "../utils/exportUtils";
import ProjectList from "../components/projects/ProjectList.jsx";
import EmployeeProjectCalendar from "../components/EmployeeProjectCalendar";
import i18n from "../i18n";
import EmployeeProjectStatsCards from "../components/projects/EmployeeProjectStatsCards.jsx";

const Employee = () => {
  const { getAccessTokenSilently, isAuthenticated, isLoading, user } = useAuth0();
  const { t } = useTranslation();

  const [message] = useState("");
  const [projects, setProjects] = useState([]);
  const [projectsLoading, setProjectsLoading] = useState(false);
  const [projectsError, setProjectsError] = useState("");
  const [selectedDate, setSelectedDate] = useState(null);

  // Search & Filter State (client-side for employee list)
  const [filters, setFilters] = useState({
    searchField: "name",
    search: "",
    status: "",
    priority: "",
    costStatus: "",
    startDate: "",
    dueDate: "",
    projectType: "",
  });

  const [activeFilters, setActiveFilters] = useState({ ...filters });

  // Sorting state
  const [sortBy, setSortBy] = useState("projectIdentifier");
  const [sortOrder, setSortOrder] = useState("ASC");
  const [activeSortBy, setActiveSortBy] = useState("projectIdentifier");
  const [activeSortOrder, setActiveSortOrder] = useState("ASC");

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [sortOpen, setSortOpen] = useState(false);

  const loadMyProjects = useCallback(async () => {
    setProjectsLoading(true);
    setProjectsError("");

    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });

      const response = await api.get("/employee/projects", {
        headers: { Authorization: `Bearer ${token}` },
      });

      setProjects(response.data || []);
    } catch (error) {
      console.error("Error loading employee projects:", error);
      setProjectsError(
        error.response?.data?.message ||
          `Failed to load projects (status: ${error.response?.status || "unknown"})`
      );
      setProjects([]);
    } finally {
      setProjectsLoading(false);
    }
  }, [getAccessTokenSilently]);

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      loadMyProjects();
    }
  }, [isLoading, isAuthenticated, loadMyProjects]);

  const projectsForSelectedDate = useMemo(() => {
    if (!selectedDate) return [];
    return projects.filter((p) => {
      if (!p.startDate) return false;
      const end = p.dueDate || p.startDate;
      return p.startDate <= selectedDate && end >= selectedDate;
    });
  }, [projects, selectedDate]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const applyFilters = () => {
    setActiveFilters({ ...filters });
    setPage(0);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      applyFilters();
    }
  };

  const clearFilters = () => {
    const empty = {
      searchField: "name",
      search: "",
      status: "",
      priority: "",
      costStatus: "",
      startDate: "",
      dueDate: "",
      projectType: "",
    };
    setFilters(empty);
    setActiveFilters(empty);
    setPage(0);
  };

  const handleSortChange = (e) => {
    const { name, value } = e.target;
    if (name === "sortBy") {
      setSortBy(value);
    } else if (name === "sortOrder") {
      setSortOrder(value);
    }
  };

  const applySorting = () => {
    setActiveSortBy(sortBy);
    setActiveSortOrder(sortOrder);
    setPage(0);
  };

  // Helper function to get sort priority for enums
  const getPrioritySortValue = (priority) => {
    const priorityOrder = { "URGENT": 4, "HIGH": 3, "MEDIUM": 2, "LOW": 1 };
    return priorityOrder[priority] || 0;
  };

  const getStatusSortValue = (status) => {
    const statusOrder = { "COMPLETED": 3, "IN_PROGRESS": 2, "PENDING": 1 };
    return statusOrder[status] || 0;
  };

  const handlePageSizeChange = (e) => {
    setPageSize(Number(e.target.value));
    setPage(0);
  };

  const filteredProjects = useMemo(() => {
    const searchValue = (activeFilters.search || "").toLowerCase().trim();

    let results = projects.filter((project) => {
      if (searchValue) {
        let fieldValue = "";
        if (activeFilters.searchField === "name") fieldValue = project.name || "";
        else if (activeFilters.searchField === "clientName") fieldValue = project.clientName || "";
        else if (activeFilters.searchField === "projectIdentifier") fieldValue = project.projectIdentifier || "";

        if (!fieldValue.toLowerCase().includes(searchValue)) return false;
      }

      if (activeFilters.status && project.status !== activeFilters.status) return false;
      if (activeFilters.priority && project.priority !== activeFilters.priority) return false;
      if (activeFilters.projectType && project.projectType !== activeFilters.projectType) return false;

      if (activeFilters.costStatus) {
        const estimatedCost = Number(project.estimatedCost || 0);
        const hasPrice = estimatedCost > 0;
        if (activeFilters.costStatus === "HAS_PRICE" && !hasPrice) return false;
        if (activeFilters.costStatus === "NO_PRICE" && hasPrice) return false;
      }

      if (activeFilters.startDate && project.startDate) {
        if (project.startDate < activeFilters.startDate) return false;
      }

      if (activeFilters.dueDate && project.dueDate) {
        if (project.dueDate > activeFilters.dueDate) return false;
      }

      return true;
    });

    // Apply sorting
    results.sort((a, b) => {
      let aVal, bVal, comparison = 0;

      if (activeSortBy === "priority") {
        // Priority: URGENT > HIGH > MEDIUM > LOW
        aVal = getPrioritySortValue(a.priority);
        bVal = getPrioritySortValue(b.priority);
        comparison = aVal - bVal;
      } else if (activeSortBy === "status") {
        // Status: COMPLETED > IN_PROGRESS > PENDING
        aVal = getStatusSortValue(a.status);
        bVal = getStatusSortValue(b.status);
        comparison = aVal - bVal;
      } else if (activeSortBy === "startDate" || activeSortBy === "dueDate") {
        // Date fields: later dates have higher values
        aVal = new Date(a[activeSortBy]).getTime();
        bVal = new Date(b[activeSortBy]).getTime();
        comparison = aVal - bVal;
      } else {
        // String fields: alphabetical
        aVal = a[activeSortBy] || "";
        bVal = b[activeSortBy] || "";
        aVal = String(aVal).toLowerCase();
        bVal = String(bVal).toLowerCase();
        if (aVal < bVal) comparison = -1;
        else if (aVal > bVal) comparison = 1;
      }

      return activeSortOrder === "ASC" ? comparison : -comparison;
    });

    return results;
  }, [projects, activeFilters, activeSortBy, activeSortOrder]);

  const totalElements = filteredProjects.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / pageSize));

  const paginatedProjects = useMemo(() => {
    const start = page * pageSize;
    return filteredProjects.slice(start, start + pageSize);
  }, [filteredProjects, page, pageSize]);

  useEffect(() => {
    if (page > totalPages - 1) {
      setPage(Math.max(0, totalPages - 1));
    }
  }, [page, totalPages]);

  const formatSelectedDate = (dateStr) => {
    if (!dateStr) return "";

    const [year, month, day] = dateStr.split("-");
    const date = new Date(Number(year), Number(month) - 1, Number(day));
    const locale = i18n.language === "fr" ? "fr-CA" : "en-CA";

    return date.toLocaleDateString(locale, {
      weekday: "long",
      month: "long",
      day: "numeric",
      year: "numeric",
    });
  };

  const handleUpdateStatus = async (project, newStatus) => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });

      await api.put(
        `/employee/projects/${project.projectIdentifier}/status`,
        { status: newStatus },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setProjects((prev) =>
        prev.map((p) =>
          p.projectIdentifier === project.projectIdentifier ? { ...p, status: newStatus } : p
        )
      );
    } catch (error) {
      console.error("Error updating project status:", error);
    }
  };

  const handleUploadInformation = async (project, file, comments) => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });

      const trimmed = (comments || "").trim();
      const hasFile = !!file;
      const hasComment = trimmed.length > 0;

      // ✅ Must have at least one
      if (!hasFile && !hasComment) return;

      const form = new FormData();

      // ✅ Only append photo if it exists
      if (hasFile) {
        form.append("photo", file);
      }

      // ✅ Always send comments field, backend treats it optional
      if (hasComment) {
        form.append("comments", trimmed);
      } else {
        form.append("comments", "");
      }

      await api.post(`/employee/projects/${project.projectIdentifier}/photo`, form, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "multipart/form-data",
        },
      });

      await loadMyProjects();
    } catch (error) {
      console.error("Error uploading information:", error);
    }
  };

  const handleExport = (type) => {
    try {
      const exportOptions = {
        exporterName: user?.name || user?.email || "Employee",
        title: t("project.personalReport"),
        locale: i18n.language === "fr" ? "fr-CA" : "en-CA",
        sortBy: activeSortBy,
        sortOrder: activeSortOrder
      };

      const langSuffix = i18n.language === "fr" ? "-fr" : "-en";

      // Use filteredProjects directly as it reflects current client-side view
      if (type === "csv") {
        generateCsv(filteredProjects, `my_projects_${new Date().toISOString().split('T')[0]}${langSuffix}.csv`, {
          locale: i18n.language === "fr" ? "fr-CA" : "en-CA"
        });
      } else {
        generatePdf(filteredProjects, `my_projects_${new Date().toISOString().split('T')[0]}${langSuffix}.pdf`, exportOptions);
      }
    } catch (e) {
      console.error("Export failed", e);
    }
  };

  return (
    <>
      <Navbar />

      <div className="p-8 bg-white min-h-screen pt-32">
        <div className="mb-8 border-b-2 border-black/5 pb-6">
            <h1 className="text-6xl font-light tracking-tight mb-8">{t("employee.title")}</h1>
            
            <div className="flex flex-col lg:flex-row justify-between items-start lg:items-end gap-6">
            <div className="flex items-baseline gap-2 bg-gray-100 p-1.5 rounded-xl border border-black/10 w-full lg:w-auto">
               <span className="text-xs font-bold text-black/40 px-2 uppercase tracking-wider whitespace-nowrap">{t("project.allProjects")}</span>
               <button
                  onClick={() => handleExport("csv")}
                  className="bg-white border-2 border-green-600 text-green-700 hover:bg-green-50 px-4 py-2 rounded-lg transition-all font-bold text-sm shadow-sm flex items-center justify-center gap-2 flex-1"
                  title="Export to CSV"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
                  CSV
                </button>
                <button
                  onClick={() => handleExport("pdf")}
                  className="bg-white border-2 border-red-600 text-red-700 hover:bg-red-50 px-4 py-2 rounded-lg transition-all font-bold text-sm shadow-sm flex items-center justify-center gap-2 flex-1"
                  title="Export to PDF"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" /></svg>
                  PDF
                </button>
            </div>
          </div>
        </div>

        {message && (
          <p className="mt-5 text-lg bg-yellow-100 border-l-4 border-yellow-400 p-4">
            {message}
          </p>
        )}

        <section className="mt-10">
          {projectsLoading && <p className="text-black/60">{t("employee.loadingProjects")}</p>}
          {projectsError && <p className="text-red-600">{projectsError}</p>}

          {!projectsLoading && !projectsError && (
            <>
            <section className="mb-8">
      <EmployeeProjectStatsCards projects={projects} />
    </section>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
                <EmployeeProjectCalendar projects={projects} onDateSelect={setSelectedDate} />

                <div className="border-2 border-black rounded-xl p-6 bg-white shadow-md">
                  <h3 className="text-2xl font-bold mb-2">
                    {selectedDate ? formatSelectedDate(selectedDate) : t("employee.selectDate")}
                  </h3>

                  <div className="mt-4 max-h-80 overflow-y-auto space-y-4">
                    {!selectedDate && <p className="text-black/60">{t("employee.pickDay")}</p>}

                    {selectedDate && projectsForSelectedDate.length === 0 && (
                      <p className="text-black/60">{t("employee.noProjects")}</p>
                    )}

                    {projectsForSelectedDate.map((project) => (
                      <div
                        key={project.projectIdentifier}
                        className="border border-black/20 rounded-lg p-4 bg-gray-50"
                      >
                        <p className="font-semibold">{project.name}</p>
                        <p className="text-xs text-black/60">ID: {project.projectIdentifier}</p>
                        <p className="text-xs text-black/60">
                          {project.startDate} - {project.dueDate}
                        </p>
                        {project.address && (
                          <p className="text-xs text-black/60 mt-1">
                            {[
                              project.address.city,
                              project.address.province,
                              project.address.country,
                              project.address.postalCode
                            ].filter(Boolean).join(", ")}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              <div className="border-b-2 border-black/5 mt-16 mb-12" />

              <div className="mt-12 text-left">
                <h3 className="text-5xl font-light tracking-tight mb-8">{t("employee.myProjects")}</h3>

                {/* Search & Filter */}
                <div className="bg-gray-50 rounded-xl border border-black/10 text-left overflow-hidden mb-6">
                  <div
                    className="flex items-baseline justify-between p-4 cursor-pointer hover:bg-gray-100 transition-colors"
                    onClick={() => setFiltersOpen(!filtersOpen)}
                  >
                    <div className="flex items-center gap-3">
                      <svg
                        className={`w-5 h-5 transition-transform duration-300 ${filtersOpen ? "rotate-90" : ""}`}
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                      </svg>
                      <h3 className="text-lg font-bold text-black/80">{t("admin.searchAndFilter")}</h3>
                    </div>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        clearFilters();
                      }}
                      className="text-sm font-semibold text-red-500 hover:underline"
                    >
                      {t("admin.clearFilters")}
                    </button>
                  </div>

                  <div
                    className={`transition-all duration-300 ease-in-out ${filtersOpen ? "max-h-[1000px] opacity-100" : "max-h-0 opacity-0"}`}
                  >
                    <div className="p-6 pt-0">
                      {/* Main Search Bar with Dropdown */}
                      <div className="mb-6 flex flex-col sm:flex-row gap-2 sm:gap-0">
                        <div className="relative w-full sm:w-auto">
                          <select
                            name="searchField"
                            value={filters.searchField}
                            onChange={handleFilterChange}
                            className="w-full h-full px-4 py-3 bg-gray-100 border-2 sm:border-r-0 border-black/20 rounded-xl sm:rounded-l-xl sm:rounded-r-none focus:border-black outline-none font-bold text-sm uppercase tracking-wide cursor-pointer hover:bg-gray-200 transition-colors"
                          >
                            <option value="name">{t("project.projectName")}</option>
                            <option value="clientName">{t("admin.clientName")}</option>
                            <option value="projectIdentifier">{t("admin.projectId")}</option>
                          </select>
                        </div>
                        <div className="relative flex-1">
                          <input
                            type="text"
                            name="search"
                            value={filters.search}
                            onChange={handleFilterChange}
                            onKeyDown={handleKeyDown}
                            placeholder={`${t("admin.searchBy")} ${
                              filters.searchField === "name"
                                ? t("project.projectName").toLowerCase()
                                : filters.searchField === "clientName"
                                ? t("admin.clientName").toLowerCase()
                                : t("admin.projectId").toLowerCase()
                            }...`}
                            className="w-full pl-4 pr-12 py-3 border-2 border-black/20 rounded-xl sm:rounded-r-xl sm:rounded-l-none focus:border-black outline-none bg-white font-medium text-lg"
                          />
                          <div className="absolute right-4 top-1/2 -translate-y-1/2 text-black/40">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                            </svg>
                          </div>
                        </div>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                        <div>
                          <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t("project.status")}</label>
                          <select
                            name="status"
                            value={filters.status}
                            onChange={handleFilterChange}
                            className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                          >
                            <option value="">{t("admin.anyStatus")}</option>
                            <option value="PENDING">{t('project.pending')}</option>
                            <option value="IN_PROGRESS">{t('project.inProgress')}</option>
                            <option value="COMPLETED">{t('project.completed')}</option>
                          </select>
                        </div>

                        <div>
                          <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t("project.priority")}</label>
                          <select
                            name="priority"
                            value={filters.priority}
                            onChange={handleFilterChange}
                            className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                          >
                            <option value="">{t("admin.anyPriority")}</option>
                            <option value="LOW">{t('project.priorityLow')}</option>
                            <option value="MEDIUM">{t('project.priorityMedium')}</option>
                            <option value="HIGH">{t('project.priorityHigh')}</option>
                            <option value="URGENT">{t('project.priorityUrgent')}</option>
                          </select>
                        </div>

                        <div>
                          <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t("project.projectType")}</label>
                          <select
                            name="projectType"
                            value={filters.projectType}
                            onChange={handleFilterChange}
                            className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                          >
                            <option value="">{t("admin.anyType")}</option>
                            <option value="APPOINTMENT">{t("admin.appointment")}</option>
                            <option value="SCHEDULED">{t("admin.scheduled")}</option>
                          </select>
                        </div>

                        <div>
                          <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t("admin.costStatus")}</label>
                          <select
                            name="costStatus"
                            value={filters.costStatus}
                            onChange={handleFilterChange}
                            className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                          >
                            <option value="">{t("admin.any")}</option>
                            <option value="HAS_PRICE">{t("admin.hasPrice")}</option>
                            <option value="NO_PRICE">{t("admin.noPrice")}</option>
                          </select>
                        </div>

                        <div>
                          <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t("admin.startDateFrom")}</label>
                          <input
                            type="date"
                            name="startDate"
                            value={filters.startDate}
                            onChange={handleFilterChange}
                            onKeyDown={handleKeyDown}
                            className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                          />
                        </div>

                        <div>
                          <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t("admin.dueDateTo")}</label>
                          <input
                            type="date"
                            name="dueDate"
                            value={filters.dueDate}
                            onChange={handleFilterChange}
                            onKeyDown={handleKeyDown}
                            className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                          />
                        </div>
                      </div>

                      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-baseline mt-4 gap-4">
                        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-2 sm:gap-4 w-full sm:w-auto">
                          <span className="font-medium text-black/70 text-sm">
                            {totalElements > 0
                              ? `${t("admin.showing")} ${page * pageSize + 1} - ${Math.min((page + 1) * pageSize, totalElements)} ${t("admin.of")} ${totalElements} ${t("admin.results")}`
                              : `${t("admin.showing")} 0 ${t("admin.results")}`}
                          </span>
                          <div className="flex items-center gap-2">
                            <label className="text-xs font-bold text-black/60 uppercase whitespace-nowrap">{t("admin.perPage")}</label>
                            <select
                              value={pageSize}
                              onChange={handlePageSizeChange}
                              className="px-3 py-1 border border-black/20 rounded-lg bg-white font-medium text-sm"
                            >
                              <option value="5">5</option>
                              <option value="10">10</option>
                              <option value="20">20</option>
                              <option value="50">50</option>
                            </select>
                          </div>
                        </div>
                        <button
                          onClick={applyFilters}
                          className="w-full sm:w-auto bg-black text-white px-8 py-2 rounded-lg font-bold hover:bg-black/80 transition-all shadow-lg"
                        >
                          {t("admin.searchProjects")}
                        </button>

                      </div>
                    </div>
                  </div>
                </div>

                {/* Sorting Section */}
                <div className="bg-gray-50 rounded-xl border border-black/10 text-left overflow-hidden mt-4">
                  <div className="flex items-baseline justify-between p-4 cursor-pointer hover:bg-gray-100 transition-colors" onClick={() => setSortOpen(!sortOpen)}>
                    <div className="flex items-center gap-3">
                      <svg
                        className={`w-5 h-5 transition-transform duration-300 ${sortOpen ? "rotate-90" : ""}`}
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                      </svg>
                      <h3 className="text-lg font-bold text-black/80">{t('admin.sortBy')}</h3>
                    </div>
                  </div>

                  <div
                    className={`transition-all duration-300 ease-in-out ${sortOpen ? "max-h-[500px] opacity-100" : "max-h-0 opacity-0"}`}
                  >
                    <div className="p-6 pt-0">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                          <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('admin.sortField')}</label>
                          <select
                            name="sortBy"
                            value={sortBy}
                            onChange={handleSortChange}
                            className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                          >
                            <option value="projectIdentifier">{t('admin.projectId')}</option>
                            <option value="name">{t('project.projectName')}</option>
                            <option value="clientName">{t('admin.clientName')}</option>
                            <option value="dueDate">{t('project.dueDate')}</option>
                            <option value="startDate">{t('project.startDate')}</option>
                            <option value="priority">{t('project.priority')}</option>
                            <option value="status">{t('project.status')}</option>
                          </select>
                        </div>

                        <div>
                          <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('admin.sortOrder')}</label>
                          <select
                            name="sortOrder"
                            value={sortOrder}
                            onChange={handleSortChange}
                            className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                          >
                            <option value="ASC">{t('admin.sortAsc')}</option>
                            <option value="DESC">{t('admin.sortDesc')}</option>
                          </select>
                        </div>
                      </div>

                      <button
                        onClick={applySorting}
                        className="w-full mt-4 bg-black text-white px-8 py-2 rounded-lg font-bold hover:bg-black/80 transition-all shadow-lg"
                      >
                        {t('admin.sortApply')}
                      </button>
                    </div>
                  </div>
                </div>

                <div className="border-2 border-black rounded-xl bg-white p-4 max-h-[400px] overflow-y-auto mt-4">
                  <ProjectList
                    projects={paginatedProjects}
                    showEdit={false}
                    employeeIndex={{}}
                    showStatusControl={true}
                    onUpdateStatus={handleUpdateStatus}
                    showUploadInformation={true}
                    showViewInformation={true}
                    onUploadInformation={handleUploadInformation}
                    getToken={() =>
                      getAccessTokenSilently({
                        authorizationParams: { audience: "https://vladtech/api" },
                      })
                    }
                  />
                </div>

                {/* Pagination Controls */}
                {totalElements > 0 && (
                  <div className="flex justify-center items-center p-4 border-t border-black/10 bg-gray-50 gap-4 mt-2 rounded-xl">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => setPage(Math.max(0, page - 1))}
                        disabled={page === 0}
                        className={`px-4 py-2 rounded-lg font-semibold border-2 border-black/10 transition-all ${
                          page === 0 ? "text-gray-300 cursor-not-allowed" : "hover:bg-black hover:text-white text-black bg-white"
                        }`}
                      >
                        Previous
                      </button>
                      <span className="font-medium text-black px-2">
                        Page {page + 1} / {totalPages}
                      </span>
                      <button
                        onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                        disabled={page >= totalPages - 1}
                        className={`px-4 py-2 rounded-lg font-semibold border-2 border-black/10 transition-all ${
                          page >= totalPages - 1 ? "text-gray-300 cursor-not-allowed" : "hover:bg-black hover:text-white text-black bg-white"
                        }`}
                      >
                        Next
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </>
          )}
        </section>
      </div>
    </>
  );
};

export default Employee;
