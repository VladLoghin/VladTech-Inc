// Admin.jsx
import { useAuth0 } from "@auth0/auth0-react";
import { useState, useEffect, useMemo, useCallback, useRef } from "react";
import { useTranslation } from "react-i18next";
import Navbar from "../components/Navbar.jsx";
import ProjectList from "../components/projects/ProjectList.jsx";
import AdminProjectCalendar from "../components/AdminProjectCalendar.jsx";
import RoleFinderModal from "../components/userManagement/RoleFinderModal.jsx";
import RoleAssignmentModal from "../components/userManagement/RoleAssignmentModal.jsx";
import ProjectModal from "../components/projects/ProjectModal.jsx";
import ProjectStatsCards from "../components/projects/ProjectStatsCards.jsx";
import CreatePortfolioModal from "../components/portfolio/CreatePortfolioModal.jsx";
import EditPortfolioModal from "../components/portfolio/EditPortfolioModal.jsx";
import ArchivePortfolioModal from "../components/portfolio/ArchivePortfolioModal.jsx";
import EstimateSettingsModal from "../components/estimates/EstimateSettingsModal.jsx";
import { api } from "../api/http";
import { generateCsv, generatePdf } from "../utils/exportUtils";

const Admin = () => {
  const { getAccessTokenSilently, user } = useAuth0();
  const { t, i18n } = useTranslation();

  const [message, setMessage] = useState("");

  const [projects, setProjects] = useState([]);
  const [archivedProjects, setArchivedProjects] = useState([]);
  const [projectStats, setProjectStats] = useState(null);
  const [activeTab, setActiveTab] = useState("active");

  const [selectedDate, setSelectedDate] = useState(null);

  // Ref for scrolling to project list
  const projectsListRef = useRef(null);

  // Stats view mode: 'status', 'priority', 'projectType'
  const [statsViewMode, setStatsViewMode] = useState('status');

  const [isRoleFinderModalOpen, setIsRoleFinderModalOpen] = useState(false);
  const [isRoleAssignmentModalOpen, setIsRoleAssignmentModalOpen] = useState(false);
  const [isEstimateSettingsModalOpen, setIsEstimateSettingsModalOpen] = useState(false);

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
  const [isEditPortfolioModalOpen, setIsEditPortfolioModalOpen] = useState(false);
  const [editPortfolioItem, setEditPortfolioItem] = useState(null);
  const [isArchivePortfolioModalOpen, setIsArchivePortfolioModalOpen] = useState(false);

  const isAdmin = useMemo(() => {
    const rawRoles = user?.["https://vladtech.com/roles"];
    const rolesArray = Array.isArray(rawRoles)
      ? rawRoles
      : typeof rawRoles === "string"
        ? [rawRoles]
        : [];
    return rolesArray.map((role) => String(role).toLowerCase()).includes("admin");
  }, [user]);

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

  // Sorting state
  const [sortBy, setSortBy] = useState("projectIdentifier");
  const [sortOrder, setSortOrder] = useState("ASC");
  const [activeSortBy, setActiveSortBy] = useState("projectIdentifier");
  const [activeSortOrder, setActiveSortOrder] = useState("ASC");

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [animatedFilter, setAnimatedFilter] = useState(null);
  const [sortOpen, setSortOpen] = useState(false);
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
        sortBy: activeSortBy,
        sortOrder: activeSortOrder,
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
        setProjects(applySortingToArray(response.data.content || []));
      } else {
        setArchivedProjects(applySortingToArray(response.data.content || []));
      }
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
    } catch (error) {
      console.error("Error fetching projects:", error);
      setMessage(t("admin.failedFetchProjects"));
    } finally {
      setSearchLoading(false);
    }
  }, [getApiToken, activeTab, activeFilters, page, activeSortBy, activeSortOrder]);

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

  const fetchProjectStats = useCallback(async () => {
    try {
      const token = await getApiToken();
      const response = await api.get("/projects/stats", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setProjectStats(response.data);
    } catch (error) {
      console.error("Error fetching project stats:", error);
    }
  }, [getApiToken]);

  // Refresh all project data (list, calendar, and stats)
  const refreshAllProjectData = useCallback(async () => {
    await Promise.all([
      fetchProjects(),
      fetchCalendarProjects(),
      fetchProjectStats()
    ]);
  }, [fetchProjects, fetchCalendarProjects, fetchProjectStats]);

  // ... (handleCompleteProject, handleReactivateProject are unchanged)
  const handleCompleteProject = async (project) => {
    try {
      const token = await getApiToken();
      await api.put(
        `/projects/${project.projectIdentifier}/complete`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setMessage(t("admin.projectCompleted", { name: project.name }));
      await refreshAllProjectData();
    } catch (error) {
      console.error("Error completing project:", error);
      setMessage(t("admin.failedComplete"));
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

      setMessage(t("admin.projectReactivated", { name: project.name }));
      await refreshAllProjectData();
    } catch (error) {
      console.error("Error reactivating project:", error);
      setMessage(t("admin.failedReactivate"));
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

  // Apply client-side sorting to projects (fallback if backend doesn't support it)
  const applySortingToArray = (projectsArray) => {
    return [...projectsArray].sort((a, b) => {
      let aVal, bVal, comparison = 0;

      if (activeSortBy === "priority") {
        aVal = getPrioritySortValue(a.priority);
        bVal = getPrioritySortValue(b.priority);
        comparison = aVal - bVal;
      } else if (activeSortBy === "status") {
        aVal = getStatusSortValue(a.status);
        bVal = getStatusSortValue(b.status);
        comparison = aVal - bVal;
      } else if (activeSortBy === "startDate" || activeSortBy === "dueDate") {
        aVal = new Date(a[activeSortBy]).getTime();
        bVal = new Date(b[activeSortBy]).getTime();
        comparison = aVal - bVal;
      } else {
        aVal = a[activeSortBy] || "";
        bVal = b[activeSortBy] || "";
        aVal = String(aVal).toLowerCase();
        bVal = String(bVal).toLowerCase();
        if (aVal < bVal) comparison = -1;
        else if (aVal > bVal) comparison = 1;
      }

      return activeSortOrder === "ASC" ? comparison : -comparison;
    });
  };

  useEffect(() => {
    fetchProjectStats();
  }, [fetchProjectStats]);

  // Calculate priority and project type stats from calendarProjects
  const computedStats = useMemo(() => {
    // Only count active projects in stats
    const activeProjects = calendarProjects.filter(p => 
      !(p.state && p.state.toUpperCase() === 'COMPLETE')
    );

    if (!activeProjects.length) return null;

    // Priority stats - check both uppercase and proper case
    const lowCount = activeProjects.filter(p => 
      p.priority && (p.priority === 'LOW' || p.priority.toUpperCase() === 'LOW')
    ).length;
    const mediumCount = activeProjects.filter(p => 
      p.priority && (p.priority === 'MEDIUM' || p.priority.toUpperCase() === 'MEDIUM')
    ).length;
    const highCount = activeProjects.filter(p => 
      p.priority && (p.priority === 'HIGH' || p.priority.toUpperCase() === 'HIGH')
    ).length;
    const urgentCount = activeProjects.filter(p => 
      p.priority && (p.priority === 'URGENT' || p.priority.toUpperCase() === 'URGENT')
    ).length;

    // Project type stats - check both uppercase and proper case
    const appointmentCount = activeProjects.filter(p => 
      p.projectType && (p.projectType === 'APPOINTMENT' || p.projectType.toUpperCase() === 'APPOINTMENT')
    ).length;
    const scheduledCount = activeProjects.filter(p => 
      p.projectType && (p.projectType === 'SCHEDULED' || p.projectType.toUpperCase() === 'SCHEDULED')
    ).length;

    return {
      priority: {
        total: activeProjects.length,
        activeCount: activeProjects.length,
        lowCount,
        mediumCount,
        highCount,
        urgentCount
      },
      projectType: {
        total: activeProjects.length,
        activeCount: activeProjects.length,
        appointmentCount,
        scheduledCount
      }
    };
  }, [calendarProjects]);

  // Get stats based on current view mode
  const displayStats = useMemo(() => {
    if (statsViewMode === 'priority') {
      return computedStats?.priority;
    } else if (statsViewMode === 'projectType') {
      return computedStats?.projectType;
    }
    // Default to status stats
    return projectStats;
  }, [statsViewMode, computedStats, projectStats]);

  const projectsForSelectedDate = useMemo(() => {
    if (!selectedDate) return [];
    return calendarProjects.filter((p) => {
      // Exclude archived projects, matching AdminProjectCalendar logic
      if (p.state && p.state.toUpperCase() === 'COMPLETE') return false;

      if (!p.startDate || !p.dueDate) return false;
      return p.startDate <= selectedDate && p.dueDate >= selectedDate;
    });
  }, [calendarProjects, selectedDate]);

  const formatSelectedDate = (dateStr) => {
    if (!dateStr) return "";

    const [year, month, day] = dateStr.split("-");
    const date = new Date(Number(year), Number(month) - 1, Number(day));
    const locale = i18n.language === "fr" ? "fr-CA" : "en-CA";

    const parts = new Intl.DateTimeFormat(locale, {
      weekday: "long",
      month: "long",
      day: "numeric",
      year: "numeric",
    }).formatToParts(date);

    const titleCasePart = (value) => {
      if (!value) return value;
      const lower = value.toLocaleLowerCase(locale);
      return lower.charAt(0).toLocaleUpperCase(locale) + lower.slice(1);
    };

    return parts
      .map((part) => {
        if (part.type === "weekday" || part.type === "month") {
          return titleCasePart(part.value);
        }
        return part.value;
      })
      .join("");
  };

  // Handle stat card clicks - scroll to projects and apply filter
  const handleStatClick = useCallback((filterType, filterValue) => {
    // Scroll to projects list
    if (projectsListRef.current) {
      projectsListRef.current.scrollIntoView({ 
        behavior: 'smooth', 
        block: 'start' 
      });
    }

    // Ensure we're on active tab (stats are for active projects)
    setActiveTab("active");
    setPage(0);

    // Open filters
    setFiltersOpen(true);

    const newFilters = {
      search: "",
      searchField: "name",
      status: "",
      priority: "",
      projectType: "",
      costStatus: "",
      startDate: "",
      dueDate: "",
      estimatedCost: "",
      assignedEmployeeId: "",
      state: "ACTIVE", // Assuming stats are for active projects
    };

    let animatedField = null;

    if (filterType === "status") {
      newFilters.status = filterValue;
      animatedField = "status";
    } else if (filterType === "priority") {
      newFilters.priority = filterValue;
      animatedField = "priority";
    } else if (filterType === "projectType") {
      newFilters.projectType = filterValue;
      animatedField = "projectType";
    } else if (filterType === "overdue") {
      // For overdue, we filter by due date before today
      const today = new Date();
      const todayStr = today.toISOString().split('T')[0];
      newFilters.dueDate = todayStr;
      animatedField = "dueDate";
    }
    // For "total", we don't apply any additional filter (show all active)

    setFilters(newFilters);
    setActiveFilters(newFilters);

    // Trigger animation
    if (animatedField) {
      setAnimatedFilter(animatedField);
      setTimeout(() => setAnimatedFilter(null), 800);
    }
  }, []);

  const handleExport = async (type) => {
    try {
      setMessage(t("admin.exporting", { type: type.toUpperCase() }));
      const token = await getApiToken();
      
      const stateFilter = activeTab === "active" ? "ACTIVE" : "COMPLETE";
      const params = { 
        state: stateFilter,
        sortBy: activeSortBy,
        sortOrder: activeSortOrder
      };

      // Apply all active filters
      if (activeFilters.search) {
        if (activeFilters.searchField === "name") params.name = activeFilters.search;
        else if (activeFilters.searchField === "clientName") params.clientName = activeFilters.search;
        else if (activeFilters.searchField === "projectIdentifier") params.projectIdentifier = activeFilters.search;
        else if (activeFilters.searchField === "assignedEmployeeId") params.assignedEmployeeId = activeFilters.search;
      }
      const otherFields = ["status", "priority", "startDate", "dueDate", "projectType", "costStatus"];
      otherFields.forEach((key) => {
        if (activeFilters[key]) params[key] = activeFilters[key];
      });

      // Fetch ALL matching projects from the list endpoint
      const response = await api.get("/projects/list", {
        headers: { Authorization: `Bearer ${token}` },
        params,
      });

      const projectsToExport = response.data;
      const langSuffix = i18n.language === "fr" ? "-fr" : "-en";

      if (type === "csv") {
        generateCsv(projectsToExport, `projects_export_${new Date().toISOString().split('T')[0]}${langSuffix}.csv`, {
          locale: i18n.language === "fr" ? "fr-CA" : "en-CA"
        });
      } else {
        const reportTitle = activeTab === "active" 
          ? t("project.fullActiveReport") 
          : t("project.fullArchivedReport");

        generatePdf(projectsToExport, `projects_export_${new Date().toISOString().split('T')[0]}${langSuffix}.pdf`, {
          exporterName: user?.name || user?.email || "Admin",
          title: reportTitle,
          locale: i18n.language === "fr" ? "fr-CA" : "en-CA",
          sortBy: activeSortBy,
          sortOrder: activeSortOrder
        });
      }

      setMessage(t("admin.exportComplete", { type: type.toUpperCase() }));
    } catch (e) {
      console.error("Export failed", e);
      setMessage(t("admin.exportFailed", { type: type.toUpperCase() }));
    }
  };

  return (
    <>
      <Navbar />

      <div className="p-8 bg-white min-h-screen pt-32">
        <div className="mb-8 border-b-2 border-black/5 pb-6">
          <h1 className="text-6xl font-light tracking-tight mb-8">{t("admin.title")}</h1>
          
          <div className="flex flex-col lg:flex-row justify-between items-start lg:items-end gap-6">
            {/* Left Side: Export */}
            <div className="flex items-center gap-2 bg-gray-100 p-1.5 rounded-xl border border-black/10 w-full lg:w-auto">
              <span className="text-xs font-bold text-black/40 px-2 uppercase tracking-wider whitespace-nowrap">{t("project.allProjects")}</span>
              <button
                onClick={() => handleExport("csv")}
                className="flex-1 bg-white border-2 border-green-600 text-green-700 hover:bg-green-50 px-4 py-2 rounded-lg transition-all font-bold text-sm shadow-sm flex items-center justify-center gap-2"
                title="Export to CSV"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
                CSV
              </button>
              <button
                onClick={() => handleExport("pdf")}
                className="flex-1 bg-white border-2 border-red-600 text-red-700 hover:bg-red-50 px-4 py-2 rounded-lg transition-all font-bold text-sm shadow-sm flex items-center justify-center gap-2"
                title="Export to PDF"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" /></svg>
                PDF
              </button>
            </div>
 
            {/* Right Side: Action Buttons */}
            <div className="flex flex-col lg:flex-row gap-3 items-center w-full lg:w-auto">
              <button
                onClick={() => setIsPortfolioModalOpen(true)}
                className="bg-yellow-400 hover:bg-yellow-500 text-black px-6 py-3 rounded-lg transition-all font-semibold shadow-lg w-full lg:w-auto"
              >
                {t("admin.createPortfolio")}
              </button>
              <button
                onClick={() => setIsEditPortfolioModalOpen(true)}
                className="bg-orange-500 hover:bg-orange-600 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg w-full lg:w-auto"
              >
                {t("admin.editPortfolio")}
              </button>
              <button
                onClick={() => setIsArchivePortfolioModalOpen(true)}
                className="bg-red-500 hover:bg-red-600 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg w-full lg:w-auto"
              >
                {t("admin.archivePortfolio")}
              </button>
              <button
                onClick={() => setIsRoleFinderModalOpen(true)}
                className="bg-black hover:bg-black/80 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg w-full lg:w-auto"
              >
                {t("admin.roleFinder")}
              </button>
              <button
                onClick={() => setIsRoleAssignmentModalOpen(true)}
                className="bg-blue-500 hover:bg-blue-600 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg w-full lg:w-auto"
              >
                {t("admin.roleManager")}
              </button>
              {isAdmin && (
                <button
                  onClick={() => setIsEstimateSettingsModalOpen(true)}
                  className="bg-emerald-500 hover:bg-emerald-600 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg w-full lg:w-auto"
                >
                  {t("admin.estimateSettings")}
                </button>
              )}
            </div>
          </div>
        </div>

        {message && (
          <div className="bg-yellow-100 border-l-4 border-yellow-400 px-6 py-4 rounded-lg shadow-xl flex items-center gap-3 relative">
            <span className="text-lg font-medium">{message}</span>
            <button
              onClick={() => setMessage("")}
              className="ml-4 text-yellow-600 hover:text-yellow-800 font-bold text-xl leading-none"
            >
              ×
            </button>
          </div>
        )}

        <RoleFinderModal isOpen={isRoleFinderModalOpen} onClose={() => setIsRoleFinderModalOpen(false)} />

        <RoleAssignmentModal
          isOpen={isRoleAssignmentModalOpen}
          onClose={() => setIsRoleAssignmentModalOpen(false)}
          onSuccess={() => setMessage(t("admin.roleAssigned"))}
        />

        <CreatePortfolioModal
          isOpen={isPortfolioModalOpen}
          onClose={() => setIsPortfolioModalOpen(false)}
          onSuccess={() => setMessage(t("admin.portfolioCreated"))}
        />

        <ArchivePortfolioModal
          isOpen={isArchivePortfolioModalOpen}
          onClose={() => setIsArchivePortfolioModalOpen(false)}
          onSuccess={() => setMessage(t("admin.portfolioArchived"))}
        />

        <EditPortfolioModal
          isOpen={isEditPortfolioModalOpen}
          onClose={() => {
            setIsEditPortfolioModalOpen(false);
            setEditPortfolioItem(null);
          }}
          onSuccess={() => setMessage(t("admin.portfolioUpdated"))}
          portfolioItem={editPortfolioItem}
        />

        <EstimateSettingsModal
          isOpen={isEstimateSettingsModalOpen}
          onClose={() => setIsEstimateSettingsModalOpen(false)}
          onSuccess={() => setMessage(t("admin.estimateSettingsSavedToast"))}
        />

        <ProjectModal
          isOpen={isProjectModalOpen}
          onClose={() => {
            setIsProjectModalOpen(false);
            setEditProject(null);
          }}
          mode={editProject ? "edit" : "create"}
          initialData={editProject}
          onSubmitSuccess={refreshAllProjectData}
          defaultDate={selectedDate}
          employeeIndex={employeeIndex}
        />

        {/* Stats Section */}
        <section className="mb-8">
          <ProjectStatsCards 
            stats={displayStats} 
            onStatClick={handleStatClick} 
            viewMode={statsViewMode}
            onViewModeChange={setStatsViewMode}
          />
        </section>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
          <AdminProjectCalendar projects={calendarProjects} onDateSelect={setSelectedDate} selectedDate={selectedDate} />

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
                  className="border border-black/20 rounded-lg p-4 flex items-center justify-between bg-gray-50 relative"
                >
                  {/* Status Dot */}
                  <div 
                    className={`absolute top-2 right-2 w-3 h-3 rounded-full ${
                      project.status === "COMPLETED" ? "bg-green-500" :
                      project.status === "IN_PROGRESS" ? "bg-blue-500" : "bg-yellow-400"
                    }`}
                    title={project.status || "PENDING"}
                  />
                  <div>
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

        <div className="border-b-2 border-black/5 mt-16 mb-12" />

        <section ref={projectsListRef}>
          <div className="flex flex-col gap-6 mb-6">
            <div className="flex flex-col sm:flex-row items-start sm:items-baseline justify-between gap-4 mb-2">
              <h2 className="text-5xl font-light tracking-tight">{t("admin.projects")}</h2>

              <div className="flex border-2 border-black rounded-lg overflow-hidden shrink-0">
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
                        <option value="name">{t('project.projectName')}</option>
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
                        placeholder={`${t('admin.searchBy')} ${filters.searchField === 'name' ? t('project.projectName').toLowerCase() : filters.searchField === 'clientName' ? t('admin.clientName').toLowerCase() : filters.searchField === 'projectIdentifier' ? t('admin.projectId').toLowerCase() : t('admin.employeeId').toLowerCase()}...`}
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
                      <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('project.status')}</label>
                      <select
                        name="status"
                        value={filters.status}
                        onChange={handleFilterChange}
                        className={`w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium transition-all ${
                          animatedFilter === 'status' ? 'animate-pulse ring-4 ring-blue-400 bg-blue-50' : ''
                        }`}
                      >
                        <option value="">{t('admin.anyStatus')}</option>
                        <option value="PENDING">{t('project.pending')}</option>
                        <option value="IN_PROGRESS">{t('project.inProgress')}</option>
                        <option value="COMPLETED">{t('project.completed')}</option>
                      </select>
                    </div>

                    {/* 2. Priority */}
                    <div>
                      <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('project.priority')}</label>
                      <select
                        name="priority"
                        value={filters.priority}
                        onChange={handleFilterChange}
                        className={`w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium transition-all ${
                          animatedFilter === 'priority' ? 'animate-pulse ring-4 ring-blue-400 bg-blue-50' : ''
                        }`}
                      >
                        <option value="">{t('admin.anyPriority')}</option>
                        <option value="LOW">{t('project.priorityLow')}</option>
                        <option value="MEDIUM">{t('project.priorityMedium')}</option>
                        <option value="HIGH">{t('project.priorityHigh')}</option>
                        <option value="URGENT">{t('project.priorityUrgent')}</option>
                      </select>
                    </div>

                    {/* 3. Type */}
                    <div>
                      <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('project.projectType')}</label>
                      <select
                        name="projectType"
                        value={filters.projectType}
                        onChange={handleFilterChange}
                        className={`w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium transition-all ${
                          animatedFilter === 'projectType' ? 'animate-pulse ring-4 ring-blue-400 bg-blue-50' : ''
                        }`}
                      >
                        <option value="">{t('admin.anyType')}</option>
                        <option value="APPOINTMENT">{t('admin.appointment')}</option>
                        <option value="SCHEDULED">{t('admin.scheduled')}</option>
                      </select>
                    </div>

                    {/* 4. Cost Status */}
                    <div>
                      <label className="block text-xs font-bold text-black/60 mb-1 uppercase">{t('admin.costStatus')}</label>
                      <select
                        name="costStatus"
                        value={filters.costStatus}
                        onChange={handleFilterChange}
                        className="w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium"
                      >
                        <option value="">{t('admin.any')}</option>
                        <option value="HAS_PRICE">{t('admin.hasPrice')}</option>
                        <option value="NO_PRICE">{t('admin.noPrice')}</option>
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
                        className={`w-full px-3 py-2 border border-black/20 rounded-lg bg-white font-medium transition-all ${
                          animatedFilter === 'dueDate' ? 'animate-pulse ring-4 ring-blue-400 bg-blue-50' : ''
                        }`}
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

          {/* Sorting Section */}
          <div className="bg-gray-50 rounded-xl border border-black/10 text-left overflow-hidden mt-4">
            <div className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-100 transition-colors" onClick={() => setSortOpen(!sortOpen)}>
              <div className="flex items-center gap-3">
                <svg
                  className={`w-5 h-5 transition-transform duration-300 ${sortOpen ? 'rotate-90' : ''}`}
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
              className={`transition-all duration-300 ease-in-out ${sortOpen ? 'max-h-[500px] opacity-100' : 'max-h-0 opacity-0'}`}
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

          <div className="bg-white border-2 border-black rounded-xl overflow-hidden p-1 mt-6">
            {activeTab === "active" ? (
              <ProjectList
                projects={projects}
                onEdit={handleEditProject}
                onComplete={handleCompleteProject}
                employeeIndex={employeeIndex}
                showEdit={true}
                showComplete={true}
                showViewInformation={true}
                isAdmin={true}
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
                isAdmin={true}
                getToken={getApiToken}
              />
            )}

            {/* Pagination Controls */}
            {totalPages > 0 && (
              <div className="flex justify-center items-center p-4 border-t border-black/10 bg-gray-50 gap-4">
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => {
                      setPage(Math.max(0, page - 1));
                      setTimeout(() => window.scrollTo(0, 100000), 200);
                    }}
                    disabled={page === 0}
                    className={`px-4 py-2 rounded-lg font-semibold border-2 border-black/10 transition-all ${page === 0 ? "text-gray-300 cursor-not-allowed" : "hover:bg-black hover:text-white text-black bg-white"
                      }`}
                  >
                    {t('previous')}
                  </button>
                  <span className="font-medium text-black px-2">
                    {t('pageOf', { current: page + 1, total: totalPages })}
                  </span>
                  <button
                    onClick={() => {
                      setPage(Math.min(totalPages - 1, page + 1));
                      setTimeout(() => window.scrollTo(0, 100000), 200);
                    }}
                    disabled={page >= totalPages - 1}
                    className={`px-4 py-2 rounded-lg font-semibold border-2 border-black/10 transition-all ${page >= totalPages - 1 ? "text-gray-300 cursor-not-allowed" : "hover:bg-black hover:text-white text-black bg-white"
                      }`}
                  >
                    {t('next')}
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
