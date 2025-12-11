import { useState, useEffect } from "react";
import { X, ChevronLeft, ChevronRight, Search } from "lucide-react";
import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";

const RoleFinderModal = ({ isOpen, onClose }) => {
  const { getAccessTokenSilently } = useAuth0();
  const [users, setUsers] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalUsers, setTotalUsers] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedRole, setSelectedRole] = useState("clients");
  const [searchQuery, setSearchQuery] = useState("");
  const [activeQuery, setActiveQuery] = useState("");

  const perPage = 25;

  const roleEndpoints = {
    clients: "/api/users/clients",
    employees: "/api/users/employees",
    admins: "/api/users/admins",
  };

  const roleLabels = {
    clients: "Client",
    employees: "Employee",
    admins: "Admin",
  };

  const fetchUsers = async (page, query = "") => {
  setLoading(true);
  setError("");
  try {
    const token = await getAccessTokenSilently();
    let url;

    if (query.trim()) {
      url = `http://localhost:8080/api/users/search?query=${encodeURIComponent(query)}&role=${selectedRole}&page=${page}&perPage=${perPage}`;
    } else {
      url = `http://localhost:8080${roleEndpoints[selectedRole]}?page=${page}&perPage=${perPage}`;
    }

    const response = await axios.get(url, {
      headers: { Authorization: `Bearer ${token}` },
    });

    setUsers(response.data.users || []);
    setTotalUsers(response.data.total || 0);
  } catch (err) {
    console.error("Error fetching users:", err);
    setError("Failed to load users");
  } finally {
    setLoading(false);
  }
};

  useEffect(() => {
    if (isOpen) {
      setCurrentPage(0);
      setActiveQuery("");
      setSearchQuery("");
      fetchUsers(0);
    }
  }, [isOpen, selectedRole]);

  useEffect(() => {
    if (isOpen && currentPage > 0) {
      fetchUsers(currentPage, activeQuery);
    }
  }, [currentPage]);

  const totalPages = Math.ceil(totalUsers / perPage);

  const getRoleBadgeColor = (roleName) => {
    const colors = {
      Admin: "bg-red-100 text-red-800 border-red-200",
      Employee: "bg-blue-100 text-blue-800 border-blue-200",
      Client: "bg-green-100 text-green-800 border-green-200",
    };
    return colors[roleName] || "bg-gray-100 text-gray-800 border-gray-200";
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    setActiveQuery(searchQuery);
    fetchUsers(0, searchQuery);
  };

  const handleClearSearch = () => {
    setSearchQuery("");
    setActiveQuery("");
    setCurrentPage(0);
    fetchUsers(0);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-2xl max-w-5xl w-full max-h-[90vh] flex flex-col">
        <div className="flex items-center justify-between p-6 border-b border-black/10">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">Role Finder</h2>
            <p className="text-sm text-black/60 mt-1">
              {totalUsers} users found
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-black/5 rounded-lg transition-colors"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <div className="p-6 border-b border-black/10 space-y-4">
          <div className="flex gap-2">
            <button
              onClick={() => setSelectedRole("clients")}
              className={`px-4 py-2 rounded-lg transition-all ${
                selectedRole === "clients"
                  ? "bg-green-400 text-black"
                  : "bg-black/5 hover:bg-black/10"
              }`}
            >
              Clients
            </button>
            <button
              onClick={() => setSelectedRole("employees")}
              className={`px-4 py-2 rounded-lg transition-all ${
                selectedRole === "employees"
                  ? "bg-blue-400 text-black"
                  : "bg-black/5 hover:bg-black/10"
              }`}
            >
              Employees
            </button>
            <button
              onClick={() => setSelectedRole("admins")}
              className={`px-4 py-2 rounded-lg transition-all ${
                selectedRole === "admins"
                  ? "bg-red-400 text-black"
                  : "bg-black/5 hover:bg-black/10"
              }`}
            >
              Admins
            </button>
          </div>

          <form onSubmit={handleSearch} className="flex gap-2">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-black/40" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search by email, name, or user ID..."
                className="w-full pl-10 pr-4 py-2 border border-black/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400"
              />
            </div>
            <button
              type="submit"
              className="px-6 py-2 bg-yellow-400 hover:bg-yellow-500 rounded-lg transition-colors font-semibold"
            >
              Search
            </button>
            {activeQuery && (
              <button
                type="button"
                onClick={handleClearSearch}
                className="px-4 py-2 border border-black/20 hover:bg-black/5 rounded-lg transition-colors"
              >
                Clear
              </button>
            )}
          </form>
        </div>

        <div className="flex-1 overflow-y-auto p-6">
          {loading ? (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-4 border-yellow-400 border-t-transparent"></div>
            </div>
          ) : error ? (
            <div className="text-center py-12">
              <p className="text-red-600">{error}</p>
              <button
                onClick={() => fetchUsers(currentPage, activeQuery)}
                className="mt-4 text-sm text-yellow-600 hover:text-yellow-700"
              >
                Try again
              </button>
            </div>
          ) : users.length === 0 ? (
            <div className="text-center py-12 text-black/60">
              No users found
            </div>
          ) : (
            <div className="space-y-3">
              {users.map((user, index) => (
                <div
                  key={user.user_id || index}
                  className="border border-black/10 rounded-lg p-4 hover:bg-black/5 transition-colors"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <p className="font-semibold text-lg">
                        {user.name || "No name"}
                      </p>
                      <p className="text-sm text-black/60">{user.email}</p>
                      <p className="text-xs text-black/40 mt-1">
                        ID: {encodeURIComponent(user.user_id)}
                      </p>
                      {user.created_at && (
                        <p className="text-xs text-black/40">
                          Created: {new Date(user.created_at).toLocaleDateString()}
                        </p>
                      )}
                      <div className="flex gap-2 mt-2">
                        <span
                          className={`text-xs px-2 py-1 rounded border ${getRoleBadgeColor(roleLabels[selectedRole])}`}
                        >
                          {roleLabels[selectedRole]}
                        </span>
                      </div>
                    </div>
                    {user.picture && (
                      <img
                        src={user.picture}
                        alt={user.name}
                        className="w-12 h-12 rounded-full"
                      />
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="border-t border-black/10 p-6 flex items-center justify-between">
          <button
            onClick={() => setCurrentPage(currentPage - 1)}
            disabled={currentPage === 0 || loading}
            className="flex items-center gap-2 px-4 py-2 border border-black/20 rounded-lg hover:bg-black/5 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <ChevronLeft className="h-4 w-4" />
            Previous
          </button>

          <div className="text-sm text-black/60">
            Page {currentPage + 1} of {totalPages || 1}
          </div>

          <button
            onClick={() => setCurrentPage(currentPage + 1)}
            disabled={currentPage >= totalPages - 1 || loading}
            className="flex items-center gap-2 px-4 py-2 border border-black/20 rounded-lg hover:bg-black/5 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            Next
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default RoleFinderModal;