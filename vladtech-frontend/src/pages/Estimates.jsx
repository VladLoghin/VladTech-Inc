import React, { useEffect, useState, useCallback } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { api } from "../api/http";
import Navbar from "../components/Navbar";
import EstimateInputModal from "../components/estimates/EstimateInputModal";
import { generateEstimatePdfBlob } from "../utils/exportUtils";

const EstimatesPage = () => {
  const { isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const [estimates, setEstimates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [toast, setToast] = useState(null);
  // hovered state removed — saved items are no longer interactive
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
            return (
              <li
                key={est.estimateId}
                className="p-4 border rounded-md flex justify-between items-center"
                style={{
                  cursor: 'pointer'
                }}
                onClick={() => { setSelectedEstimate(est); setOpenEstimateModal(true); }}
              >
                <div>
                  <div className="font-semibold">{est.title || "Untitled"}</div>
                  <div className="text-sm text-gray-600">Created: {new Date(est.createdAt).toLocaleString()}</div>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={async (e) => {
                      e.stopPropagation();
                      e.preventDefault();
                      try {
                        // Generate PDF client-side from the stored estimate data
                        const projectSource = est.project && Object.keys(est.project || {}).length > 0 ? est.project : est;
                        // Normalize common optional keys that may have different names from older saves
                        const normalized = {
                          ...projectSource,
                          applianceAllowance: projectSource.applianceAllowance ?? projectSource.appliance_allowance ?? projectSource.appliance ?? projectSource.appliances,
                          numSkylights: projectSource.numSkylights ?? projectSource.skylights ?? projectSource.num_skylights ?? projectSource.skylightCount,
                          tearOffRequired: projectSource.tearOffRequired ?? projectSource.tearOff ?? projectSource.tear_off ?? projectSource.tearOffNeeded ?? projectSource.tear_off_required,
                          includeInsulation: projectSource.includeInsulation ?? projectSource.include_insulation ?? projectSource.insulationIncluded ?? projectSource.insulation ?? false,
                          subfloorRepairNeeded: projectSource.subfloorRepairNeeded ?? projectSource.subfloorRepair ?? projectSource.subfloor_repair ?? projectSource.subfloorRepairNeeded ?? false,
                        };

                        const blob = await generateEstimatePdfBlob([normalized], `${(est.title || 'estimate').replace(/[^a-z0-9-_]/gi, '_')}.pdf`, { title: 'Estimate', exporterName: 'VladTech', locale: 'en-CA' });
                        if (!blob) throw new Error('PDF generation failed');
                        const url = window.URL.createObjectURL(blob);
                        window.open(url, '_blank');
                        setTimeout(() => window.URL.revokeObjectURL(url), 2000);
                      } catch (err) {
                        console.error('Failed to generate PDF', err);
                        setToast({ type: 'error', message: 'Failed to generate PDF' });
                        setTimeout(() => setToast(null), 3000);
                      }
                    }}
                    className="underline text-sm"
                  >
                    PDF
                  </button>
                  <button onClick={(e) => { e.stopPropagation(); handleDelete(est.estimateId); }} className="text-sm text-red-600">
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
            onSave={(saved) => {
              if (!saved) return;
              setEstimates((prev) => {
                const exists = prev.some(e => e.estimateId === saved.estimateId);
                if (exists) {
                  return prev.map(e => e.estimateId === saved.estimateId ? saved : e);
                }
                return [saved, ...prev];
              });
            }}
            initialProject={selectedEstimate ? (selectedEstimate.project || selectedEstimate) : null}
            initialSavedEstimate={selectedEstimate}
            openResultInitially={false}
            fromSavedList={true}
          />
        )}
      </div>
    </div>
  );
};

export default EstimatesPage;
