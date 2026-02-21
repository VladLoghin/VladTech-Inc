import React, { useEffect, useState, useCallback } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { api } from "../api/http";
import Navbar from "../components/Navbar";
import EstimateInputModal from "../components/estimates/EstimateInputModal";

const EstimatesPage = () => {
  const { isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const [estimates, setEstimates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [toast, setToast] = useState(null);
  const [hoveredId, setHoveredId] = useState(null);
  const [openEstimateModal, setOpenEstimateModal] = useState(false);
  const [selectedEstimate, setSelectedEstimate] = useState(null);

  const fetchEstimates = useCallback(async () => {
    if (!isAuthenticated || isLoading) return;
    setLoading(true);
    try {
      const token = await getAccessTokenSilently({ authorizationParams: { audience: "https://vladtech/api" } });
      const resp = await api.get("/estimates", { headers: { Authorization: `Bearer ${token}` } });
      setEstimates(Array.isArray(resp.data) ? resp.data : []);
    } catch (err) {
      console.error("Failed to load estimates", err);
      setEstimates([]);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, isLoading, getAccessTokenSilently]);

  useEffect(() => {
    fetchEstimates();
  }, [fetchEstimates]);

  const handleDelete = (id) => {
    // open inline confirmation modal
    setDeleteTarget(id);
  };

  const confirmDelete = async () => {
    const id = deleteTarget;
    if (!id) return;
    try {
      const token = await getAccessTokenSilently({ authorizationParams: { audience: "https://vladtech/api" } });
      await api.delete(`/estimates/${id}`, { headers: { Authorization: `Bearer ${token}` } });
      setEstimates((prev) => prev.filter((e) => e.estimateId !== id));
      setToast({ type: "success", message: "Estimate deleted" });
    } catch (err) {
      console.error("Failed to delete estimate", err);
      setToast({ type: "error", message: "Failed to delete estimate" });
    } finally {
      setDeleteTarget(null);
      setTimeout(() => setToast(null), 3000);
    }
  };

  const cancelDelete = () => setDeleteTarget(null);

  return (
    <div>
      <Navbar />
      <div className="container mx-auto p-6" style={{ marginTop: 120 }}>
        <h2 className="text-2xl font-bold mb-4">My Estimates</h2>
        {loading && <div>Loading...</div>}
        {!loading && estimates.length === 0 && <div>No saved estimates yet.</div>}
        {toast && (
          <div className={`toast toast-${toast.type}`} role="alert" style={{ marginBottom: 12 }}>
            {toast.message}
          </div>
        )}

        <ul className="space-y-4">
          {estimates.map((est) => {
            const isHovered = hoveredId === est.estimateId;
            return (
              <li
                key={est.estimateId}
                className="p-4 border rounded-md flex justify-between items-center"
                onMouseEnter={() => setHoveredId(est.estimateId)}
                onMouseLeave={() => setHoveredId(null)}
                style={{
                  transition: 'transform 150ms ease, box-shadow 150ms ease',
                  transform: isHovered ? 'translateY(-4px)' : 'none',
                  boxShadow: isHovered ? '0 8px 20px rgba(0,0,0,0.08)' : 'none',
                  cursor: 'default'
                }}
              >
                <div style={{ cursor: 'pointer' }} onClick={() => { setSelectedEstimate(est.project || est); setOpenEstimateModal(true); }}>
                  <div className="font-semibold">{est.title || "Untitled"}</div>
                  <div className="text-sm text-gray-600">Created: {new Date(est.createdAt).toLocaleString()}</div>
                </div>
                <div className="flex gap-2">
                  <a href={est.pdfUrl || '#'} target="_blank" rel="noreferrer" className="underline text-sm">
                    PDF
                  </a>
                  <button onClick={() => handleDelete(est.estimateId)} className="text-sm text-red-600">
                    Delete
                  </button>
                </div>
              </li>
            );
          })}
        </ul>

        {deleteTarget && (
          <div className="modal" role="dialog" aria-modal="true" data-testid="delete-confirm-modal" onPointerDown={(e) => { if (e.target === e.currentTarget) e.currentTarget.dataset.pointerStartedOutside = 'true'; }} onPointerUp={(e) => { if (e.target === e.currentTarget && e.currentTarget.dataset.pointerStartedOutside === 'true') { cancelDelete(); } delete e.currentTarget.dataset.pointerStartedOutside; }}>
            <div className="modal-content">
              <h3>Confirm delete</h3>
              <p style={{ marginTop: 8 }}>Are you sure you want to delete this estimate?</p>
              <div style={{ marginTop: 12 }} className="modal-actions">
                <button onClick={confirmDelete} style={{ backgroundColor: '#ef4444', color: 'white', padding: '0.5rem 1rem', borderRadius: 6 }}>Delete</button>
                <button onClick={cancelDelete} style={{ marginLeft: 8 }}>Cancel</button>
              </div>
            </div>
          </div>
        )}

        {openEstimateModal && (
          <EstimateInputModal
            isOpen={openEstimateModal}
            onClose={() => { setOpenEstimateModal(false); setSelectedEstimate(null); }}
            initialProject={selectedEstimate}
            openResultInitially={true}
          />
        )}
      </div>
    </div>
  );
};

export default EstimatesPage;
