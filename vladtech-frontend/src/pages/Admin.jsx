import { useAuth0 } from "@auth0/auth0-react";
import { useState, useEffect, useMemo } from "react";
import axios from "axios";
import NewProjectModal from "../components/NewProjectModal";
import AdminProjectCalendar from "../components/AdminProjectCalendar";

const Admin = () => {
  const { getAccessTokenSilently } = useAuth0();
  const [message, setMessage] = useState("");
  const [projects, setProjects] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState(null); // "YYYY-MM-DD"

  const fetchProjects = async () => {
    try {
      const token = await getAccessTokenSilently();
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
    const loadInitialProjects = async () => {
      try {
        const token = await getAccessTokenSilently();
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
      <h1 className="text-4xl font-bold mb-8 tracking-tight">
        Admin Area - Only for Admin Role
      </h1>

      {message && (
        <p className="mt-5 text-lg bg-yellow-100 border-l-4 border-yellow-400 p-4">
          {message}
        </p>
      )}

      {/* Top bar title + New Project button */}
      <div className="mt-10 flex items-center justify-between mb-6">
        <h2 className="text-3xl font-bold tracking-tight">Admin Calendar</h2>
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-yellow-400 hover:bg-yellow-500 text-black px-8 py-3 rounded-lg transition-all font-semibold shadow-lg"
        >
          New Project
        </button>
      </div>

      {/* New project modal */}
      <NewProjectModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onProjectCreated={fetchProjects}
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
            onClick={() => setIsModalOpen(true)}
          >
            ADD
          </button>
        </div>
      </div>

      {/* BOTTOM: All projects scrollable list with full fields */}
      <section className="mt-10">
        <h2 className="text-2xl font-bold mb-4 tracking-tight">
          All Projects
        </h2>

        <div className="border-2 border-black rounded-xl bg-white p-4 max-h-[400px] overflow-y-auto space-y-4">
          {projects.length === 0 && (
            <p className="text-black/60 text-center py-8">
              No projects found.
            </p>
          )}

          {projects.map((project) => (
            <div
              key={project.projectIdentifier}
              className="border border-black/10 rounded-lg p-4 hover:shadow-md transition-shadow"
            >
              <h3 className="text-lg font-semibold mb-2">{project.name}</h3>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                <p>
                  <strong className="text-black/60">ID:</strong>{" "}
                  <span className="font-mono">
                    {project.projectIdentifier}
                  </span>
                </p>
                <p>
                  <strong className="text-black/60">Type:</strong>{" "}
                  <span className="bg-yellow-100 px-2 py-1 rounded">
                    {project.projectType}
                  </span>
                </p>
                <p>
                  <strong className="text-black/60">Start Date:</strong>{" "}
                  {project.startDate}
                </p>
                <p>
                  <strong className="text-black/60">Due Date:</strong>{" "}
                  {project.dueDate}
                </p>
              </div>

              {project.description && (
                <p className="mt-2 text-sm">
                  <strong className="text-black/60">Description:</strong>{" "}
                  {project.description}
                </p>
              )}

              {project.address && (
                <p className="mt-2 text-sm">
                  <strong className="text-black/60">Address:</strong>{" "}
                  {project.address.streetAddress}, {project.address.city},{" "}
                  {project.address.province}, {project.address.country}{" "}
                  {project.address.postalCode}
                </p>
              )}

              {project.assignedEmployeeIds &&
                project.assignedEmployeeIds.length > 0 && (
                  <p className="mt-2 text-sm">
                    <strong className="text-black/60">
                      Assigned Employees:
                    </strong>{" "}
                    {project.assignedEmployeeIds.join(", ")}
                  </p>
                )}

              {project.photos && project.photos.length > 0 && (
                <p className="mt-2 text-sm">
                  <strong className="text-black/60">Photos:</strong>{" "}
                  {project.photos.length}
                </p>
              )}

              
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default Admin;
