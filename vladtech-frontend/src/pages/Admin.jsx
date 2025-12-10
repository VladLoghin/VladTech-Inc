import { useAuth0 } from "@auth0/auth0-react";
import { useState, useEffect } from "react";
import axios from "axios";
import NewProjectModal from "../components/projects/NewProjectModal";

const Admin = () => {
  const { getAccessTokenSilently } = useAuth0();
  const [message, setMessage] = useState("");
  const [projects, setProjects] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);

  

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
    <div className="p-8 bg-white min-h-screen">
      <h1 className="text-4xl font-bold mb-8 tracking-tight">Admin Area — Only for Admin Role</h1>
      {message && (
        <p className="mt-5 text-lg bg-yellow-100 border-l-4 border-yellow-400 p-4">{message}</p>
      )}
      <div className="mt-10">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-3xl font-bold tracking-tight">Projects</h2>
          <button 
            onClick={() => setIsModalOpen(true)} 
            className="bg-yellow-400 hover:bg-yellow-500 text-black px-8 py-3 rounded-lg transition-all font-semibold shadow-lg"
          >
            New Project
          </button>
        </div>
        <NewProjectModal 
          isOpen={isModalOpen} 
          onClose={() => setIsModalOpen(false)}
          onProjectCreated={fetchProjects}
        />
        {projects.length > 0 ? (
          <div className="space-y-6">
            {projects.map((project) => (
              <div key={project.projectIdentifier} className="border-2 border-black rounded-xl p-6 bg-white shadow-md hover:shadow-xl transition-shadow">
                <h3 className="text-2xl font-bold mb-4 text-black">{project.name}</h3>
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <p><strong className="text-black/60">ID:</strong> <span className="font-mono">{project.projectIdentifier}</span></p>
                  <p><strong className="text-black/60">Type:</strong> <span className="bg-yellow-100 px-2 py-1 rounded">{project.projectType}</span></p>
                  <p><strong className="text-black/60">Start Date:</strong> {project.startDate}</p>
                  <p><strong className="text-black/60">Due Date:</strong> {project.dueDate}</p>
                </div>
                {project.description && (
                  <p className="mt-4"><strong className="text-black/60">Description:</strong> {project.description}</p>
                )}
                {project.address && (
                  <p className="mt-2"><strong className="text-black/60">Address:</strong> {project.address.streetAddress}, {project.address.city}, {project.address.province}, {project.address.country} {project.address.postalCode}</p>
                )}
                {project.assignedEmployeeIds && project.assignedEmployeeIds.length > 0 && (
                  <p className="mt-2"><strong className="text-black/60">Assigned Employees:</strong> {project.assignedEmployeeIds.join(", ")}</p>
                )}
                {project.photos && project.photos.length > 0 && (
                  <p className="mt-2"><strong className="text-black/60">Photos:</strong> {project.photos.length}</p>
                )}
              </div>
            ))}
          </div>
        ) : (
          <p className="text-center text-black/60 py-12">No projects found.</p>
        )}
      </div>
    </div>
  );
};

export default Admin;
