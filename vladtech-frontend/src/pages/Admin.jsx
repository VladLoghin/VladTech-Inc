import { useAuth0 } from "@auth0/auth0-react";
import { useState, useEffect } from "react";
import axios from "axios";
import NewProjectModal from "../components/projects/NewProjectModal";
import ProjectList from "../components/projects/ProjectList.jsx";

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
          <ProjectList projects={projects} />
        ) : (
          <p className="text-center text-black/60 py-12">No projects found.</p>
        )}
      </div>
    </div>
  );
};

export default Admin;
