import { useState, useEffect } from "react";
import { X, ChevronLeft, ChevronRight, Search } from "lucide-react";
import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";

const EmployeeFinderModal = ({
  isOpen,
  onClose,
  selectedEmployeeIds = [],
  onToggleEmployee,
}) => {
  const { getAccessTokenSilently } = useAuth0();
  const [employees, setEmployees] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalEmployees, setTotalEmployees] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [activeQuery, setActiveQuery] = useState("");

  const perPage = 25;


  const fetchEmployees = async (page, query = "") => {
    setLoading(true);
    setError("");
    try {
      const token = await getAccessTokenSilently();
      let url;

      if (query.trim()) {
        url = `http://localhost:8080/api/users/search?query=${encodeURIComponent(
          query
        )}&role=employees&page=${page}&perPage=${perPage}`;
      } else {
        url = `http://localhost:8080/api/users/employees?page=${page}&perPage=${perPage}`;
      }

      const response = await axios.get(url, {
        headers: { Authorization: `Bearer ${token}` },
      });

      setEmployees(response.data.users || []);
      setTotalEmployees(response.data.total || 0);
    } catch (err) {
      console.error("Error fetching employees:", err);
      setError("Failed to load employees");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
  if (isOpen) {
    setCurrentPage(0);
    setActiveQuery("");
    setSearchQuery("");
    fetchEmployees(0);
  }
}, [isOpen]);


  useEffect(() => {
  if (isOpen && currentPage > 0) {
    fetchEmployees(currentPage, activeQuery);
  }
}, [currentPage, isOpen, activeQuery]);


  const totalPages = Math.ceil(totalEmployees / perPage);

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    setActiveQuery(searchQuery);
    fetchEmployees(0, searchQuery);
  };

  const handleClearSearch = () => {
    setSearchQuery("");
    setActiveQuery("");
    setCurrentPage(0);
    fetchEmployees(0);
  };

  const handleSelectEmployee = (employee) => {
    onToggleEmployee({
      id: employee.user_id,
      name: employee.name || employee.email,
      email: employee.email,
    });
  };

  if (!isOpen) return null;

  const isEmployeeSelected = (userId) =>
  selectedEmployeeIds?.includes(userId);

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-2xl max-w-4xl w-full max-h-[90vh] flex flex-col">
        <div className="flex items-center justify-between p-6 border-b border-black/10">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">
              Select Employee
            </h2>
            <p className="text-sm text-black/60 mt-1">
              {totalEmployees} employees found
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-black/5 rounded-lg transition-colors"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        {/* Search bar */}
        <div className="p-6 border-b border-black/10">
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

                {/* List */}
        <div className="flex-1 overflow-y-auto p-6">
          {loading ? (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-4 border-yellow-400 border-t-transparent"></div>
            </div>
          ) : error ? (
            <div className="text-center py-12">
              <p className="text-red-600">{error}</p>
              <button
                onClick={() => fetchEmployees(currentPage, activeQuery)}
                className="mt-4 text-sm text-yellow-600 hover:text-yellow-700"
              >
                Try again
              </button>
            </div>
          ) : employees.length === 0 ? (
            <div className="text-center py-12 text-black/60">
              No employees found
            </div>
          ) : (
            <div className="space-y-3">
              {employees.map((emp, index) => {
                const isSelected = isEmployeeSelected(emp.user_id);
                return (
                  <button
                    key={emp.user_id || index}
                    onClick={() => handleSelectEmployee(emp)}
                    className={`w-full border rounded-lg p-4 hover:bg-yellow-50 transition-colors text-left ${
                      isSelected
                        ? "bg-yellow-100 border-yellow-400 border-2"
                        : "border-black/10"
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-2">
                          <p className="font-semibold text-lg">
                            {emp.name || "No name"}
                          </p>
                          {isSelected && (
                            <span className="text-xs bg-yellow-400 px-2 py-1 rounded font-semibold">
                              Selected
                            </span>
                          )}
                        </div>
                        <p className="text-sm text-black/60">{emp.email}</p>
                        <p className="text-xs text-black/40 mt-1">
                          ID: {encodeURIComponent(emp.user_id)}
                        </p>
                      </div>

                      {emp.picture && (
                        <img
                          src={emp.picture}
                          alt={emp.name}
                          className="w-12 h-12 rounded-full"
                        />
                      )}
                    </div>
                  </button>
                );
              })}
            </div>
          )}
        </div>


        {/* Pagination + Confirm */}
<div className="border-t border-black/10 p-6 flex items-center justify-between">
  
  {/* Previous */}
  <button
    onClick={() => setCurrentPage(currentPage - 1)}
    disabled={currentPage === 0 || loading}
    className="flex items-center gap-2 px-4 py-2 border border-black/20 rounded-lg hover:bg-black/5 disabled:opacity-50 transition-colors"
  >
    <ChevronLeft className="h-4 w-4" />
    Previous
  </button>

  {/* Page info */}
  <div className="text-sm text-black/60">
    Page {currentPage + 1} of {totalPages || 1}
  </div>

  {/* Next */}
  <button
    onClick={() => setCurrentPage(currentPage + 1)}
    disabled={currentPage >= totalPages - 1 || loading}
    className="flex items-center gap-2 px-4 py-2 border border-black/20 rounded-lg hover:bg-black/5 disabled:opacity-50 transition-colors"
  >
    Next
    <ChevronRight className="h-4 w-4" />
  </button>

  {/* Confirm Selection */}
  <button
    onClick={onClose}
    className="ml-4 px-6 py-2 bg-yellow-400 hover:bg-yellow-500 rounded-lg font-semibold shadow"
  >
    Confirm
  </button>

</div>

      </div>
    </div>
  );
};

export default EmployeeFinderModal;
