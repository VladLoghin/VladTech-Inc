import "./Review.css";

const DeleteConfirmModal = ({ open, title, message, onConfirm, onCancel }) => {
    if (!open) return null;

    return (
        <div className="review-modal-backdrop" onClick={onCancel}>
            <div className="review-modal" onClick={(e) => e.stopPropagation()}>
                <h3 className="review-modal-title">{title}</h3>
                <p className="review-modal-message">{message}</p>
                <div className="review-modal-actions">
                    <button className="btn-secondary" onClick={onCancel}>
                        Cancel
                    </button>
                    <button className="btn-danger" onClick={onConfirm}>
                        Delete
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DeleteConfirmModal;