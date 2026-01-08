import { useAuth0 } from "@auth0/auth0-react";
import { useState, useEffect, useMemo } from "react";
import axios from "axios";
// import NewProjectModal from "../components/projects/NewProjectModal.jsx";
import ProjectList from "../components/projects/ProjectList.jsx";
import AdminProjectCalendar from "../components/AdminProjectCalendar.jsx";
import RoleFinderModal from "../components/userManagement/RoleFinderModal.jsx";
import ProjectModal from "../components/projects/ProjectModal.jsx";
import CreatePortfolioModal from "../components/portfolio/CreatePortfolioModal.jsx";
import DeletePortfolioModal from "../components/portfolio/DeletePortfolioModal.jsx";

const Admin = () => {
  const { getAccessTokenSilently } = useAuth0();
  const [message, setMessage] = useState("");
  const [isMessageVisible, setIsMessageVisible] = useState(false);
  const [projects, setProjects] = useState([]);
  const [archivedProjects, setArchivedProjects] = useState([]);
  const [activeTab, setActiveTab] = useState("active"); // "active" or "archived"
  // const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState(null); // "YYYY-MM-DD"
  const [isRoleFinderModalOpen, setIsRoleFinderModalOpen] = useState(false);
  const [editProject, setEditProject] = useState(null);
  const [isProjectModalOpen, setIsProjectModalOpen] = useState(false);
  const [employeeIndex, setEmployeeIndex] = useState({});
  const [isPortfolioModalOpen, setIsPortfolioModalOpen] = useState(false);
  const [isDeletePortfolioModalOpen, setIsDeletePortfolioModalOpen] =
    useState(false);

  const handleEditProject = (project) => {
    setEditProject(project);
    setIsProjectModalOpen(true);
  };

  const fetchActiveProjects = async () => {
    try {
      const token = await getAccessTokenSilently({
                authorizationParams: {
                    audience: "https://vladtech/api",
                },
            });
      const response = await axios.get("http://localhost:8080/api/projects", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setProjects(response.data);
    } catch (error) {
      console.error("Error fetching projects:", error);
      setMessage("Failed to fetch projects.");
    }
  };

  useEffect(() => {
  const loadEmployees = async () => {
    try {
      const token = await getAccessTokenSilently({
                authorizationParams: {
                    audience: "https://vladtech/api",
                },
            });
      const res = await axios.get("http://localhost:8080/api/employee/list", {
        headers: { Authorization: `Bearer ${token}` },
      });

      const index = {};
      (res.data || []).forEach((emp) => {
        index[emp.userId] = {
          name: emp.name,
          email: emp.email,
        };
      });

      setEmployeeIndex(index);
    } catch (err) {
      console.error("Error fetching employees for index", err);
    }
  };

  loadEmployees();
}, [getAccessTokenSilently]);


  useEffect(() => {
    const loadInitialProjects = async () => {
      try {
        const token = await getAccessTokenSilently({
                authorizationParams: {
                    audience: "https://vladtech/api",
                },
            });
        const response = await axios.get("http://localhost:8080/api/projects", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setProjects(response.data);
    } catch (error) {
      console.error("Error fetching active projects:", error);
      setMessage("Failed to fetch active projects.");
    }
  };

  const fetchArchivedProjects = async () => {
    try {
      const token = await getAccessTokenSilently();
      const response = await axios.get(
        "http://localhost:8080/api/projects/archived",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setArchivedProjects(response.data);
    } catch (error) {
      console.error("Error fetching archived projects:", error);
      setMessage("Failed to fetch archived projects.");
    }
  };

  const fetchProjects = async () => {
    await fetchActiveProjects();
    await fetchArchivedProjects();
  };

  const handleCompleteProject = async (project) => {
    try {
      const token = await getAccessTokenSilently();
      await axios.put(
        `http://localhost:8080/api/projects/${project.projectIdentifier}/complete`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
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
      const token = await getAccessTokenSilently();
      await axios.put(
        `http://localhost:8080/api/projects/${project.projectIdentifier}/reactivate`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setMessage(`Project "${project.name}" has been reactivated.`);
      await fetchProjects();
    } catch (error) {
      console.error("Error reactivating project:", error);
      setMessage("Failed to reactivate project.");
    }
  };

  // Auto-dismiss message after 5 seconds with fade out
  useEffect(() => {
    if (message) {
      setIsMessageVisible(true);
      const timer = setTimeout(() => {
        setIsMessageVisible(false);
        setTimeout(() => setMessage(""), 300); // Wait for fade out animation
      }, 4700);
      return () => clearTimeout(timer);
    }
  }, [message]);

  useEffect(() => {
    const loadEmployees = async () => {
      try {
        const token = await getAccessTokenSilently();
        const res = await axios.get("http://localhost:8080/api/employee/list", {
          headers: { Authorization: `Bearer ${token}` },
        });

        const index = {};
        (res.data || []).forEach((emp) => {
          index[emp.userId] = {
            name: emp.name,
            email: emp.email,
          };
        });

        setEmployeeIndex(index);
      } catch (err) {
        console.error("Error fetching employees for index", err);
      }
    };

    loadEmployees();
  }, [getAccessTokenSilently]);

  useEffect(() => {
    const loadInitialProjects = async () => {
      await fetchActiveProjects();
      await fetchArchivedProjects();
    };

    loadInitialProjects();
  }, [getAccessTokenSilently]);

  // Projects that cover the selected date (startDate <= date <= dueDate)
  const projectsForSelectedDate = useMemo(() => {
    if (!selectedDate) return [];
    return projects.filter((p) => {
      if (!p.startDate || !p.dueDate) return false;
      return p.startDate <= selectedDate && p.dueDate >= selectedDate;
    });
  }, [projects, selectedDate]);

  const formatSelectedDate = (dateStr) => {
    if (!dateStr) return "";

    // dateStr is "YYYY-MM-DD"
    const [year, month, day] = dateStr.split("-");
    const date = new Date(Number(year), Number(month) - 1, Number(day)); // local date

    return date.toLocaleDateString("en-US", {
      weekday: "long",
      month: "long",
      day: "numeric",
      year: "numeric",
    });
  };

  return (
    <div className="p-8 bg-white min-h-screen">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-4xl font-bold tracking-tight">
          Admin Area - Only for Admin Role
        </h1>

        <div className="flex gap-3">
          <button
            onClick={() => setIsPortfolioModalOpen(true)}
            className="bg-yellow-400 hover:bg-yellow-500 text-black px-6 py-3 rounded-lg transition-all font-semibold shadow-lg"
          >
            Create Portfolio
          </button>
          <button
            onClick={() => setIsDeletePortfolioModalOpen(true)}
            className="bg-red-500 hover:bg-red-600 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg"
          >
            Delete Portfolio
          </button>
          <button
            onClick={() => setIsRoleFinderModalOpen(true)}
            className="bg-black hover:bg-black/80 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg"
          >
            Role Finder
          </button>
        </div>
      </div>

      {message && (
        <div
          className={`fixed top-6 inset-x-0 flex justify-center z-50 transition-all duration-300 ${isMessageVisible ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-4'
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

      <style>{`
        .animate-bounce-in {
          animation: bounce-in 0.4s ease-out forwards;
        }
      `}</style>

      <RoleFinderModal
        isOpen={isRoleFinderModalOpen}
        onClose={() => setIsRoleFinderModalOpen(false)}
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

      {/* New project modal */}
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
      />

      {/* TOP: calendar (left) + selected-date projects (right) */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
        {/* Calendar: pass projects so days can show events */}
        <AdminProjectCalendar
          projects={projects}
          onDateSelect={setSelectedDate}
        />

        {/* Selected date detail card */}
        <div className="border-2 border-black rounded-xl p-6 bg-white shadow-md">
          <h2 className="text-2xl font-bold mb-2">
            {selectedDate
              ? formatSelectedDate(selectedDate)
              : "Select a date on the calendar"}
          </h2>

          <div className="mt-4 max-h-80 overflow-y-auto space-y-4">
            {!selectedDate && (
              <p className="text-black/60">Pick a day to see its projects.</p>
            )}

            {selectedDate && projectsForSelectedDate.length === 0 && (
              <p className="text-black/60">
                No projects scheduled for this date.
              </p>
            )}

            {projectsForSelectedDate.map((project) => (
              <div
                key={project.projectIdentifier}
                className="border border-black/20 rounded-lg p-4 flex items-center justify-between bg-gray-50"
              >
                <div>
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
              </div>
            ))}
          </div>

          <button
            className="mt-4 w-full bg-yellow-400 hover:bg-yellow-500 text-black py-3 rounded-lg font-semibold shadow-lg"
            onClick={() => setIsProjectModalOpen(true)}
          >
            ADD
          </button>
        </div>
      </div>

      {/* BOTTOM: Projects with Tab Toggle */}
      <section className="mt-10">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-2xl font-bold tracking-tight">Projects</h2>

          {/* Tab Toggle */}
          <div className="flex border-2 border-black rounded-lg overflow-hidden">
            <button
              onClick={() => setActiveTab("active")}
              className={`px-6 py-2 font-semibold transition-all ${activeTab === "active"
                ? "bg-black text-white"
                : "bg-white text-black hover:bg-gray-100"
                }`}
            >
              Active ({projects.length})
            </button>
            <button
              onClick={() => setActiveTab("archived")}
              className={`px-6 py-2 font-semibold transition-all ${activeTab === "archived"
                ? "bg-black text-white"
                : "bg-white text-black hover:bg-gray-100"
                }`}
            >
              Archived ({archivedProjects.length})
            </button>
          </div>
        </div>

        {activeTab === "active" ? (
          <ProjectList
            projects={projects}
            onEdit={handleEditProject}
            onComplete={handleCompleteProject}
            employeeIndex={employeeIndex}
            showEdit={true}
            showComplete={true}
          />
        ) : (
          <ProjectList
            projects={archivedProjects}
            onReactivate={handleReactivateProject}
            employeeIndex={employeeIndex}
            showEdit={false}
            showComplete={false}
            showReactivate={true}
          />
        )}
      </section>
    </div>
  );
};

export default Admin;

