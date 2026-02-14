/**
 * Validate if a status transition is allowed
 * Valid transitions:
 * - PENDING → IN_PROGRESS
 * - IN_PROGRESS → COMPLETED
 * - Same status to itself (allowed, no-op)
 * 
 * Invalid transitions:
 * - PENDING → COMPLETED (skipping IN_PROGRESS)
 * - IN_PROGRESS → PENDING (backward)
 * - COMPLETED → anything (backward)
 */
export const isValidStatusTransition = (currentStatus, newStatus) => {
  // Same status is allowed
  if (currentStatus === newStatus) return true;

  const statusHierarchy = {
    PENDING: 0,
    IN_PROGRESS: 1,
    COMPLETED: 2,
  };

  const currentLevel = statusHierarchy[currentStatus] ?? -1;
  const newLevel = statusHierarchy[newStatus] ?? -1;

  // Only allow forward progression by exactly one step
  return newLevel === currentLevel + 1;
};

/**
 * Get a user-friendly error message for invalid transitions
 */
export const getStatusTransitionErrorMessage = (currentStatus, newStatus, t) => {
  const transitionKey = `${currentStatus}_to_${newStatus}`;

  const messages = {
    PENDING_to_COMPLETED:
      t("project.cannotSkipInProgress", {
        defaultValue: "Cannot skip IN_PROGRESS. Please update to IN_PROGRESS first.",
      }),
    IN_PROGRESS_to_PENDING:
      t("project.cannotReverse", {
        defaultValue: "Cannot revert to PENDING after starting work.",
      }),
    COMPLETED_to_PENDING:
      t("project.cannotReverse", {
        defaultValue: "Cannot revert to PENDING after completing the project.",
      }),
    COMPLETED_to_IN_PROGRESS:
      t("project.cannotReverse", {
        defaultValue: "Cannot revert to IN_PROGRESS after completing the project.",
      }),
  };

  return (
    messages[transitionKey] ||
    t("project.invalidStatusTransition", {
      defaultValue: `Cannot change status from ${currentStatus} to ${newStatus}.`,
    })
  );
};
