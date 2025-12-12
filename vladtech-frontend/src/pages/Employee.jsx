import { useAuth0 } from "@auth0/auth0-react";
import { useState } from "react";
import axios from "axios";
import ProjectList from "../components/projects/ProjectList.jsx";

const Employee = () => {
  const { getAccessTokenSilently } = useAuth0();
  const [message, setMessage] = useState("");
  const [projects, setProjects] = useState([]);
  const [projectsLoading, setProjectsLoading] = useState(false);
  const [projectsError, setProjectsError] = useState("");

  const callEmployeeEndpoint = async () => {
    try {
      const token = await getAccessTokenSilently();

      const response = await axios.get("http://localhost:8080/api/employee/info", {
        headers: { Authorization: `Bearer ${token}` },
      });

      setMessage(response.data);
    } catch (error) {
      console.error("Error calling employee endpoint:", error);
      setMessage("You are not authorized or endpoint failed.");
    }
  };

  const loadMyProjects = async () => {
    setProjectsLoading(true);
    setProjectsError("");

    try {
      const token = await getAccessTokenSilently();

      const response = await axios.get("http://localhost:8080/api/employee/projects", {
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

  return (
    <div className="p-8 bg-white min-h-screen">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-4xl font-bold tracking-tight">
          Employee Area - Only for Employees
        </h1>

        <div className="flex gap-3">
          <button
            onClick={callEmployeeEndpoint}
            className="bg-black hover:bg-black/80 text-white px-6 py-3 rounded-lg transition-all font-semibold shadow-lg"
          >
            Call Employee Endpoint
          </button>

          <button
            onClick={loadMyProjects}
            className="bg-yellow-400 hover:bg-yellow-500 text-black px-6 py-3 rounded-lg transition-all font-semibold shadow-lg"
          >
            Load My Projects
          </button>
        </div>
      </div>

      {message && (
        <p className="mt-5 text-lg bg-yellow-100 border-l-4 border-yellow-400 p-4">
          {message}
        </p>
      )}

      <section className="mt-10">
        <h2 className="text-2xl font-bold mb-4 tracking-tight">
          My Assigned Projects
        </h2>

        {projectsLoading && (
          <p className="text-black/60">Loading projects...</p>
        )}

        {projectsError && (
          <p className="text-red-600">{projectsError}</p>
        )}

        {!projectsLoading && !projectsError && (
          <ProjectList projects={projects} showEdit={false} employeeIndex={{}} />
        )}
      </section>
    </div>
  );
};

export default Employee;
