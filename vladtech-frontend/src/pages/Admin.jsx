// Admin.jsx
import { useAuth0 } from "@auth0/auth0-react";
import { useState, useEffect, useMemo, useCallback } from "react";
import { useTranslation } from "react-i18next";
import Navbar from "../components/Navbar.jsx";
import ProjectList from "../components/projects/ProjectList.jsx";
import AdminProjectCalendar from "../components/AdminProjectCalendar.jsx";
import RoleFinderModal from "../components/userManagement/RoleFinderModal.jsx";
import RoleAssignmentModal from "../components/userManagement/RoleAssignmentModal.jsx";
import ProjectModal from "../components/projects/ProjectModal.jsx";
import CreatePortfolioModal from "../components/portfolio/CreatePortfolioModal.jsx";
import DeletePortfolioModal from "../components/portfolio/DeletePortfolioModal.jsx";
import { api } from "../api/http";

const Admin = () => {
  const { getAccessTokenSilently } = useAuth0();
  const { t, i18n } = useTranslation();

  const [message, setMessage] = useState("");
  const [isMessageVisible, setIsMessageVisible] = useState(false);

  const [projects, setProjects] = useState([]);
  const [archivedProjects, setArchivedProjects] = useState([]);
  const [activeTab, setActiveTab] = useState("active");

  const [selectedDate, setSelectedDate] = useState(null);

  const [isRoleFinderModalOpen, setIsRoleFinderModalOpen] = useState(false);
  const [isRoleAssignmentModalOpen, setIsRoleAssignmentModalOpen] = useState(false);

  const [editProject, setEditProject] = useState(null);
  const [isProjectModalOpen, setIsProjectModalOpen] = useState(false);

  /* eslint-disable react-hooks/exhaustive-deps */
  const [employeeIndex, setEmployeeIndex] = useState({});

  const fetchEmployees = useCallback(async () => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });
      const response = await api.get("/users/employees", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const index = {};
      (response.data.users || response.data).forEach((emp) => {
        // emp.user_id is from auth0, often has "auth0|" prefix
        // The backend might return standard user objects. 
        // We'll index by user_id or id.
        const id = emp.user_id || emp.id;
        if (id) index[id] = emp;
      });
      setEmployeeIndex(index);
    } catch (e) {
      console.error("Failed to fetch employees for index", e);
    }
  }, [getAccessTokenSilently]);

  useEffect(() => {
    fetchEmployees();
  }, [fetchEmployees]);

  const [isPortfolioModalOpen, setIsPortfolioModalOpen] = useState(false);
  const [isDeletePortfolioModalOpen, setIsDeletePortfolioModalOpen] = useState(false);

  // ✅ token helper used by ProjectList to load images with auth
  const getApiToken = useCallback(async () => {
    return await getAccessTokenSilently({
      authorizationParams: { audience: "https://vladtech/api" },
    });
  }, [getAccessTokenSilently]);

  const handleEditProject = (project) => {
    setEditProject(project);
    setIsProjectModalOpen(true);
  };

  // Search & Filter State
  const [filters, setFilters] = useState({
    searchField: "name",
    search: "",
    status: "",
    priority: "",
    costStatus: "",
    startDate: "",
    dueDate: "",
    projectType: "",
    estimatedCost: "",
    // includeNACost: true, // Removed per user request
  });

  // Track the *active* filters applied on search button click
  const [activeFilters, setActiveFilters] = useState({ ...filters });

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [searchLoading, setSearchLoading] = useState(false);

  // Fetch Projects with Search & Filters
  const fetchProjects = useCallback(async () => {
    try {
      setSearchLoading(true);
      const token = await getApiToken();
      const stateFilter = activeTab === "active" ? "ACTIVE" : "COMPLETE";

      // Build params from activeFilters, removing empty ones
      const params = {
        state: stateFilter,
        page: page,
        size: pageSize,
      };

      // Map the generic 'search' input to the specific field selected
      if (activeFilters.search) {
        if (activeFilters.searchField === "name") params.name = activeFilters.search;
        else if (activeFilters.searchField === "clientName") params.clientName = activeFilters.search;
        else if (activeFilters.searchField === "projectIdentifier") params.projectIdentifier = activeFilters.search;
        else if (activeFilters.searchField === "assignedEmployeeId") params.assignedEmployeeId = activeFilters.search;
      }

      const otherFields = ["status", "priority", "startDate", "dueDate", "projectType", "costStatus"];
      otherFields.forEach((key) => {
        if (activeFilters[key]) {
          params[key] = activeFilters[key];
        }
      });

      console.log("Fetching projects with params:", params);

      const response = await api.get("/projects/search", {
        headers: { Authorization: `Bearer ${token}` },
        params,
      });

      if (activeTab === "active") {
        setProjects(response.data.content || []);
      } else {
        setArchivedProjects(response.data.content || []);
      }
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
    } catch (error) {
      console.error("Error fetching projects:", error);
      setMessage("Failed to fetch projects.");
    } finally {
      setSearchLoading(false);
    }
  }, [getApiToken, activeTab, activeFilters, page]);

  // Calendar still needs all active projects...
  const [calendarProjects, setCalendarProjects] = useState([]);
  const fetchCalendarProjects = useCallback(async () => {
    try {
      const token = await getApiToken();
      const response = await api.get("/projects/calendar", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setCalendarProjects(response.data);
    } catch (e) {
      console.error("Failed to load calendar", e);
    }
  }, [getApiToken]);

  // ... (handleCompleteProject, handleReactivateProject are unchanged)
  const handleCompleteProject = async (project) => {
    try {
      const token = await getApiToken();
      await api.put(
        `/projects/${project.projectIdentifier}/complete`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setMessage(`Project "${project.name}" has been marked as complete.`);
      await fetchProjects();
    } catch (error) {
      console.error("Error completing project:", error);
      setMessage("Failed to complete project.");
    }
  };

  const handleReactivateProject = async (project) => {
    try {
      const token = await getApiToken();
      await api.put(
        `/projects/${project.projectIdentifier}/reactivate`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setMessage(`Project "${project.name}" has been reactivated.`);
      await fetchProjects();
    } catch (error) {
      console.error("Error reactivating project:", error);
      setMessage("Failed to reactivate project.");
    }
  };

  // Effects
  useEffect(() => {
    fetchProjects();
  }, [fetchProjects]);

  const handlePageSizeChange = (e) => {
    setPageSize(Number(e.target.value));
    setPage(0);
  };

  useEffect(() => {
    fetchCalendarProjects();
  }, [fetchCalendarProjects]);

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
      estimatedCost: "",
      assignedEmployeeId: "",
      // includeNACost: true, // Removed per user request
    };
    setFilters(empty);
    setActiveFilters(empty);
    setPage(0);
  };

  // ... (projectsForSelectedDate, formatSelectedDate unchanged)
  const projectsForSelectedDate = useMemo(() => {
    if (!selectedDate) return [];
    return calendarProjects.filter((p) => {
      if (!p.startDate || !p.dueDate) return false;
      return p.startDate <= selectedDate && p.dueDate >= selectedDate;
    });
  }, [calendarProjects, selectedDate]);

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

  return (
    <>
      <Navbar />

      <div className="p-8 bg-white min-h-screen pt-32">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-4xl font-bold tracking-tight">{t("admin.title")}</h1>

          <div className="flex gap-3">
            <button
              onClick={() => setIsPortfolioModalOpen(true)}
              className="bg-yellow-400 hover:bg-yellow-500 text-black px-6 py-3 rounded-lg transition-all font-semibold shadow-lg"
            >
              {t("admin.createPortfolio")}
            </button>
            <button
              onClick={() => setIsDeletePortfolioModalOpen(true)}
              className="bg-red-500 hover:bg-red-600 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg"
            >
              {t("admin.deletePortfolio")}
            </button>
            <button
              onClick={() => setIsRoleFinderModalOpen(true)}
              className="bg-black hover:bg-black/80 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg"
            >
              {t("admin.roleFinder")}
            </button>
            <button
              onClick={() => setIsRoleAssignmentModalOpen(true)}
              className="bg-blue-500 hover:bg-blue-600 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg"
            >
              {t("admin.roleManager")}
            </button>
          </div>
        </div>

        {message && (
          <div
            className={`fixed top-6 inset-x-0 flex justify-center z-50 transition-all duration-300 ${isMessageVisible ? "opacity-100 translate-y-0" : "opacity-0 -translate-y-4"
              }`}
          >
            <div className="bg-yellow-100 border-l-4 border-yellow-400 px-6 py-4 rounded-lg shadow-xl flex items-center gap-3 relative">
              <span className="text-lg font-medium">{message}</span>
              <button
                onClick={() => {
                  setIsMessageVisible(false);
                  setTimeout(() => setMessage(""), 300);
                }}
                className="ml-4 text-yellow-600 hover:text-yellow-800 font-bold text-xl leading-none"
              >
                ×
              </button>
            </div>
          </div>
        )}

        <RoleFinderModal isOpen={isRoleFinderModalOpen} onClose={() => setIsRoleFinderModalOpen(false)} />

        <RoleAssignmentModal
          isOpen={isRoleAssignmentModalOpen}
          onClose={() => setIsRoleAssignmentModalOpen(false)}
          onSuccess={() => setMessage("Role assigned successfully!")}
        />

        <CreatePortfolioModal
          isOpen={isPortfolioModalOpen}
          onClose={() => setIsPortfolioModalOpen(false)}
          onSuccess={() => setMessage("Portfolio item created successfully!")}
        />

        <DeletePortfolioModal
          isOpen={isDeletePortfolioModalOpen}
          onClose={() => setIsDeletePortfolioModalOpen(false)}
          onSuccess={() => setMessage("Portfolio item deleted successfully!")}
        />

        <ProjectModal
          isOpen={isProjectModalOpen}
          onClose={() => {
            setIsProjectModalOpen(false);
            setEditProject(null);
          }}
          mode={editProject ? "edit" : "create"}
          initialData={editProject}
          onSubmitSuccess={fetchProjects}
          defaultDate={selectedDate}
          employeeIndex={employeeIndex}
        />

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
          <AdminProjectCalendar projects={calendarProjects} onDateSelect={setSelectedDate} />

          <div className="border-2 border-black rounded-xl p-6 bg-white shadow-md">
            <h2 className="text-2xl font-bold mb-2">
              {selectedDate ? formatSelectedDate(selectedDate) : t("admin.selectDate")}
            </h2>

            <div className="mt-4 max-h-80 overflow-y-auto space-y-4">
              {!selectedDate && <p className="text-black/60">{t("admin.pickDay")}</p>}

              {selectedDate && projectsForSelectedDate.length === 0 && (
                <p className="text-black/60">{t("admin.noProjects")}</p>
              )}

              {projectsForSelectedDate.map((project) => (
                <div
                  key={project.projectIdentifier}
                  className="border border-black/20 rounded-lg p-4 flex items-center justify-between bg-gray-50"
                >
                  <div>
                    <p className="font-semibold">{project.name}</p>
                    <p className="text-xs text-black/60">ID: {project.projectIdentifier}</p>
                    <p className="text-xs text-black/60">
                      {project.startDate} - {project.dueDate}
                    </p>
                    {project.address && (
                      <p className="text-xs text-black/60 mt-1">
                        {project.address.city}, {project.address.province}
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>

            <button
              className="mt-4 w-full bg-yellow-400 hover:bg-yellow-500 text-black py-3 rounded-lg font-semibold shadow-lg"
              onClick={() => setIsProjectModalOpen(true)}
            >
              {t("add")}
            </button>
          </div>
        </div>

        <section className="mt-10">
          <div className="flex flex-col gap-6 mb-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold tracking-tight">{t("admin.projects")}</h2>

              <div className="flex border-2 border-black rounded-lg overflow-hidden">
                <button
                  onClick={() => { setActiveTab("active"); setPage(0); }}
                  className={`px-6 py-2 font-semibold transition-all ${activeTab === "active" ? "bg-black text-white" : "bg-white text-black hover:bg-gray-100"
                    }`}
                >
                  {t("admin.active")}
                </button>
                <button
                  onClick={() => { setActiveTab("archived"); setPage(0); }}
                  className={`px-6 py-2 font-semibold transition-all ${activeTab === "archived" ? "bg-black text-white" : "bg-white text-black hover:bg-gray-100"
                    }`}
                >
                  {t("admin.archived")}
                </button>
              </div>
            </div>

            {/* Advanced Filters Grid */}
            <div className="bg-gray-50 rounded-xl border border-black/10 text-left overflow-hidden">
              <div className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-100 transition-colors" onClick={() => setFiltersOpen(!filtersOpen)}>
                <div className="flex items-center gap-3">
                  <svg
                    className={`w-5 h-5 transition-transform duration-300 ${filtersOpen ? 'rotate-90' : ''}`}
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                  <h3 className="text-lg font-bold text-black/80">{t('admin.searchAndFilter')}</h3>
                </div>
                <button
                  onClick={(e) => { e.stopPropagation(); clearFilters(); }}
                  className="text-sm font-semibold text-red-500 hover:underline"
                >
                  {t('admin.clearFilters')}
                </button>
              </div>

              <div
                className={`transition-all duration-300 ease-in-out ${filtersOpen ? 'max-h-[1000px] opacity-100' : 'max-h-0 opacity-0'
                  }`}
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
                        <option value="name">{t('admin.projectName')}</option>
                        <option value="clientName">{t('admin.clientName')}</option>
                        <option value="projectIdentifier">{t('admin.projectId')}</option>
                        <option value="assignedEmployeeId">{t('admin.employeeId')}</option>
                      </select>
                    </div>
                    <div className="relative flex-1">
                      <input
                        type="text"
                        name="search"
                        value={filters.search}
                        onChange={handleFilterChange}
                        onKeyDown={handleKeyDown}
                        placeholder={`${t('admin.searchBy')} ${filters.searchField === 'name' ? t('admin.projectName').toLowerCase() : filters.searchField === 'clientName' ? t('admin.clientName').toLowerCase() : filters.searchField === 'projectIdentifier' ? t('admin.projectId').toLowerCase() : t('admin.employeeId').toLowerCase()}...`}
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
                    {/* 1. Status */}
                    <div>
                      <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('admin.status')}</label>
                      <select
                        name="status"
                        value={filters.status}
                        onChange={handleFilterChange}
                        className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                      >
                        <option value="">{t('admin.anyStatus')}</option>
                        <option value="PENDING">Pending</option>
                        <option value="IN_PROGRESS">In Progress</option>
                        <option value="COMPLETED">Completed</option>
                      </select>
                    </div>

                    {/* 2. Priority */}
                    <div>
                      <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('admin.priority')}</label>
                      <select
                        name="priority"
                        value={filters.priority}
                        onChange={handleFilterChange}
                        className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                      >
                        <option value="">{t('admin.anyPriority')}</option>
                        <option value="LOW">Low</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="HIGH">High</option>
                        <option value="URGENT">Urgent</option>
                      </select>
                    </div>

                    {/* 3. Type */}
                    <div>
                      <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('admin.projectType')}</label>
                      <select
                        name="projectType"
                        value={filters.projectType}
                        onChange={handleFilterChange}
                        className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                      >
                        <option value="">{t('admin.anyType')}</option>
                        <option value="APPOINTMENT">{t('admin.appointment')}</option>
                        <option value="SCHEDULED">{t('admin.scheduled')}</option>
                      </select>
                    </div>

                    {/* 4. Cost Status */}
                    <div>
                      <label className="block text-xs font-bold text-black/60 mb-1 uppercase">Cost Status</label>
                      <select
                        name="costStatus"
                        value={filters.costStatus}
                        onChange={handleFilterChange}
                        className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                      >
                        <option value="">Any</option>
                        <option value="HAS_PRICE">Has Price</option>
                        <option value="NO_PRICE">No Price</option>
                      </select>
                    </div>



                    {/* 5. Start Date */}
                    <div>
                      <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('admin.startDateFrom')}</label>
                      <input
                        type="date"
                        name="startDate"
                        value={filters.startDate}
                        onChange={handleFilterChange}
                        onKeyDown={handleKeyDown}
                        className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                      />
                    </div>

                    {/* 6. Due Date */}
                    <div>
                      <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('admin.dueDateTo')}</label>
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

                  <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mt-4 gap-4">
                    <div className="flex flex-col sm:flex-row items-start sm:items-center gap-2 sm:gap-4 w-full sm:w-auto">
                      <span className="font-medium text-black/70 text-sm">
                        {totalElements > 0
                          ? `${t('admin.showing')} ${page * pageSize + 1} - ${Math.min((page + 1) * pageSize, totalElements)} ${t('admin.of')} ${totalElements} ${t('admin.results')}`
                          : t('admin.showing') + ' 0 ' + t('admin.results')}
                      </span>
                      <div className="flex items-center gap-2">
                        <label className="text-xs font-bold text-black/60 uppercase whitespace-nowrap">{t('admin.perPage')}</label>
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
                      disabled={searchLoading}
                      className="w-full sm:w-auto bg-black text-white px-8 py-2 rounded-lg font-bold hover:bg-black/80 transition-all shadow-lg disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                    >
                      {searchLoading && (
                        <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                      )}
                      {t('admin.searchProjects')}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-white border-2 border-black rounded-xl overflow-hidden p-1">
            {activeTab === "active" ? (
              <ProjectList
                projects={projects}
                onEdit={handleEditProject}
                onComplete={handleCompleteProject}
                employeeIndex={employeeIndex}
                showEdit={true}
                showComplete={true}
                showViewInformation={true}
                getToken={getApiToken}
              />
            ) : (
              <ProjectList
                projects={archivedProjects}
                onReactivate={handleReactivateProject}
                employeeIndex={employeeIndex}
                showEdit={false}
                showComplete={false}
                showReactivate={true}
                showViewInformation={true}
                getToken={getApiToken}
              />
            )}

            {/* Pagination Controls */}
            {totalPages > 0 && (
              <div className="flex justify-center items-center p-4 border-t border-black/10 bg-gray-50 gap-4">
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setPage(Math.max(0, page - 1))}
                    disabled={page === 0}
                    className={`px-4 py-2 rounded-lg font-semibold border-2 border-black/10 transition-all ${page === 0 ? "text-gray-300 cursor-not-allowed" : "hover:bg-black hover:text-white text-black bg-white"
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
                    className={`px-4 py-2 rounded-lg font-semibold border-2 border-black/10 transition-all ${page >= totalPages - 1 ? "text-gray-300 cursor-not-allowed" : "hover:bg-black hover:text-white text-black bg-white"
                      }`}
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </div>
        </section>
      </div>
    </>
  );
};

export default Admin;
