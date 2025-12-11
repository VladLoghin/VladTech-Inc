import { useState, useEffect } from "react";
import { X, ChevronLeft, ChevronRight, Search } from "lucide-react";
import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";

const ClientFinderModal = ({ isOpen, onClose, onSelectClient, selectedClientId }) => {
  const { getAccessTokenSilently } = useAuth0();
  const [clients, setClients] = useState([]);
  const [selectedClient, setSelectedClient] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalClients, setTotalClients] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [activeQuery, setActiveQuery] = useState("");

  const perPage = 25;

  const fetchClients = async (page, query = "") => {
  setLoading(true);
  setError("");
  try {
    const token = await getAccessTokenSilently();
    let url;

    if (query.trim()) {
      url = `http://localhost:8080/api/users/search?query=${encodeURIComponent(query)}&role=clients&page=${page}&perPage=${perPage}`;
    } else {
      url = `http://localhost:8080/api/users/clients?page=${page}&perPage=${perPage}`;
    }

    const response = await axios.get(url, {
      headers: { Authorization: `Bearer ${token}` },
    });

    setClients(response.data.users || []);
    setTotalClients(response.data.total || 0);
  } catch (err) {
    console.error("Error fetching clients:", err);
    setError("Failed to load clients");
  } finally {
    setLoading(false);
  }
};

  const fetchSelectedClient = async () => {
    if (!selectedClientId) return;
    
    try {
      const token = await getAccessTokenSilently();
      const response = await axios.get(
        `http://localhost:8080/api/users/${encodeURIComponent(selectedClientId)}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setSelectedClient(response.data);
    } catch (err) {
      console.error("Error fetching selected client:", err);
    }
  };

  useEffect(() => {
    if (isOpen) {
      setCurrentPage(0);
      setActiveQuery("");
      setSearchQuery("");
      fetchClients(0);
      fetchSelectedClient();
    }
  }, [isOpen, selectedClientId]);

  useEffect(() => {
    if (isOpen && currentPage > 0) {
      fetchClients(currentPage, activeQuery);
    }
  }, [currentPage]);

  const totalPages = Math.ceil(totalClients / perPage);

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    setActiveQuery(searchQuery);
    fetchClients(0, searchQuery);
  };

  const handleClearSearch = () => {
    setSearchQuery("");
    setActiveQuery("");
    setCurrentPage(0);
    fetchClients(0);
  };

  const handleSelectClient = (client) => {
    onSelectClient({
      id: client.user_id,
      name: client.name || client.email,
      email: client.email,
    });
    onClose();
  };

  const getDisplayClients = () => {
    if (!selectedClient) return clients;
    
    const filteredClients = clients.filter(c => c.user_id !== selectedClientId);
    return [selectedClient, ...filteredClients];
  };

  if (!isOpen) return null;

  const displayClients = getDisplayClients();

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-2xl max-w-4xl w-full max-h-[90vh] flex flex-col">
        <div className="flex items-center justify-between p-6 border-b border-black/10">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">Select Client</h2>
            <p className="text-sm text-black/60 mt-1">
              {totalClients} clients found
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-black/5 rounded-lg transition-colors"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

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

        <div className="flex-1 overflow-y-auto p-6">
          {loading ? (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-4 border-yellow-400 border-t-transparent"></div>
            </div>
          ) : error ? (
            <div className="text-center py-12">
              <p className="text-red-600">{error}</p>
              <button
                onClick={() => fetchClients(currentPage, activeQuery)}
                className="mt-4 text-sm text-yellow-600 hover:text-yellow-700"
              >
                Try again
              </button>
            </div>
          ) : displayClients.length === 0 ? (
            <div className="text-center py-12 text-black/60">
              No clients found
            </div>
          ) : (
            <div className="space-y-3">
              {displayClients.map((client, index) => {
                const isSelected = client.user_id === selectedClientId;
                return (
                  <button
                    key={client.user_id || index}
                    onClick={() => handleSelectClient(client)}
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
                            {client.name || "No name"}
                          </p>
                          {isSelected && (
                            <span className="text-xs bg-yellow-400 px-2 py-1 rounded font-semibold">
                              Currently Selected
                            </span>
                          )}
                        </div>
                        <p className="text-sm text-black/60">{client.email}</p>
                        <p className="text-xs text-black/40 mt-1">
                          ID: {encodeURIComponent(client.user_id)}
                        </p>
                      </div>
                      {client.picture && (
                        <img
                          src={client.picture}
                          alt={client.name}
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

export default ClientFinderModal;