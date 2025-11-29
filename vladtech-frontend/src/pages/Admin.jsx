import { useAuth0 } from "@auth0/auth0-react";
import { useState, useEffect } from "react";
import axios from "axios";
import NewProjectModal from "../components/NewProjectModal";

const Admin = () => {
  const { getAccessTokenSilently } = useAuth0();
  const [message, setMessage] = useState("");
  const [projects, setProjects] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const callAdminEndpoint = async () => {
    try {
      const token = await getAccessTokenSilently();
      //console.log("Access Token:", token);

      const response = await axios.get("http://localhost:8080/api/admin/dashboard", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      setMessage(response.data);
    } catch (error) {
      console.error("Error calling admin endpoint:", error);
      setMessage("You are not authorized or endpoint failed.");
    }
  };

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

  return (
    <div>
      <h1>Admin Area — Only for Admin Role</h1>
      <button onClick={callAdminEndpoint}>Call Admin Endpoint</button>

      {message && (
        <p style={{ marginTop: "20px", fontSize: "18px" }}>{message}</p>
      )}
      <div style={{ marginTop: "40px" }}>
        <h2>Projects</h2>
        <button onClick={fetchProjects}>Refresh Projects</button>
        <button onClick={() => setIsModalOpen(true)} style={{ marginLeft: "10px" }}>
          New Project
        </button>
        <NewProjectModal 
          isOpen={isModalOpen} 
          onClose={() => setIsModalOpen(false)}
          onProjectCreated={fetchProjects}
        />
        {projects.length > 0 ? (
          <div>
            {projects.map((project) => (
              <div key={project.projectIdentifier} style={{ border: "1px solid #ccc", padding: "15px", marginTop: "10px" }}>
                <h3>{project.name}</h3>
                <p><strong>ID:</strong> {project.projectIdentifier}</p>
                <p><strong>Description:</strong> {project.description}</p>
                <p><strong>Type:</strong> {project.projectType}</p>
                <p><strong>Start Date:</strong> {project.startDate}</p>
                <p><strong>Due Date:</strong> {project.dueDate}</p>
                {project.address && (
                  <p><strong>Address:</strong> {project.address.streetAddress}, {project.address.city}, {project.address.province}, {project.address.country} {project.address.postalCode}</p>
                )}
                {project.assignedEmployeeIds && project.assignedEmployeeIds.length > 0 && (
                  <p><strong>Assigned Employees:</strong> {project.assignedEmployeeIds.join(", ")}</p>
                )}
                {project.photos && project.photos.length > 0 && (
                  <p><strong>Photos:</strong> {project.photos.length}</p>
                )}
              </div>
            ))}
          </div>
        ) : (
          <p>No projects found.</p>
        )}
      </div>
    </div>
  );
};

export default Admin;
