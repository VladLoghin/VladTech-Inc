// Employee.jsx
import { useAuth0 } from "@auth0/auth0-react";
import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import Navbar from "../components/Navbar.jsx";
import { api } from "../api/http";
import ProjectList from "../components/projects/ProjectList.jsx";
import EmployeeProjectCalendar from "../components/EmployeeProjectCalendar";
import i18n from "../i18n";

const Employee = () => {
  const { getAccessTokenSilently, isAuthenticated, isLoading } = useAuth0();
  const { t } = useTranslation();
  const [message] = useState("");
  const [projects, setProjects] = useState([]);
  const [projectsLoading, setProjectsLoading] = useState(false);
  const [projectsError, setProjectsError] = useState("");
  const [selectedDate, setSelectedDate] = useState(null); // "YYYY-MM-DD"

  const loadMyProjects = async () => {
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
  };

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      loadMyProjects();
    }
  }, [isLoading, isAuthenticated]);

  const projectsForSelectedDate = useMemo(() => {
    if (!selectedDate) return [];

    return projects.filter((p) => {
      if (!p.startDate) return false;
      const end = p.dueDate || p.startDate;
      return p.startDate <= selectedDate && end >= selectedDate;
    });
  }, [projects, selectedDate]);

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
          p.projectIdentifier === project.projectIdentifier
            ? { ...p, status: newStatus }
            : p
        )
      );
    } catch (error) {
      console.error("Error updating project status:", error);
    }
  };

  // NEW: upload latest photo
  const handleUploadPhoto = async (project, file) => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });

      const form = new FormData();
      form.append("photo", file);

      await api.post(`/employee/projects/${project.projectIdentifier}/photo`, form, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "multipart/form-data",
        },
      });

      await loadMyProjects();
    } catch (error) {
      console.error("Error uploading project photo:", error);
    }
  };

  return (
    <>
      {/* Navigation Bar */}
      <Navbar />

      <div className="p-8 bg-white min-h-screen pt-32">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-4xl font-bold tracking-tight">{t("employee.title")}</h1>
        </div>

        {message && (
          <p className="mt-5 text-lg bg-yellow-100 border-l-4 border-yellow-400 p-4">
            {message}
          </p>
        )}

        <section className="mt-10">
          {projectsLoading && (
            <p className="text-black/60">{t("employee.loadingProjects")}</p>
          )}

          {projectsError && <p className="text-red-600">{projectsError}</p>}

          {!projectsLoading && !projectsError && (
            <>
              {/* TOP: calendar (left) + projects on selected date (right) */}
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
                <EmployeeProjectCalendar projects={projects} onDateSelect={setSelectedDate} />

                <div className="border-2 border-black rounded-xl p-6 bg-white shadow-md">
                  <h3 className="text-2xl font-bold mb-2">
                    {selectedDate ? formatSelectedDate(selectedDate) : t("employee.selectDate")}
                  </h3>

                  <div className="mt-4 max-h-80 overflow-y-auto space-y-4">
                    {!selectedDate && (
                      <p className="text-black/60">{t("employee.pickDay")}</p>
                    )}

                    {selectedDate && projectsForSelectedDate.length === 0 && (
                      <p className="text-black/60">{t("employee.noProjects")}</p>
                    )}

                    {projectsForSelectedDate.map((project) => (
                      <div
                        key={project.projectIdentifier}
                        className="border border-black/20 rounded-lg p-4 bg-gray-50"
                      >
                        <p className="font-semibold">{project.name}</p>
                        <p className="text-xs text-black/60">
                          ID: {project.projectIdentifier}
                        </p>
                        <p className="text-xs text-black/60">
                          {project.startDate} - {project.dueDate}
                        </p>
                        {project.address && (
                          <p className="text-xs text-black/60 mt-1">
                            {project.address.city}, {project.address.province}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              {/* BOTTOM: scrollable list of all assigned projects */}
              <div className="mt-10">
                <h3 className="text-2xl font-bold mb-4 tracking-tight">
                  {t("employee.myProjects")}
                </h3>

                <div className="border-2 border-black rounded-xl bg-white p-4 max-h-[400px] overflow-y-auto">
                  <ProjectList
                    projects={projects}
                    showEdit={false}
                    employeeIndex={{}}
                    showStatusControl={true}
                    onUpdateStatus={handleUpdateStatus}
                    showUploadPhoto={true}
                    onUploadPhoto={handleUploadPhoto}
                  />
                </div>
              </div>
            </>
          )}
        </section>
      </div>
    </>
  );
};

export default Employee;
