import { useAuth0 } from "@auth0/auth0-react";
import { Navigate } from "react-router-dom";

const ProtectedRoute = ({ children, role }) => {
  const { isAuthenticated, isLoading, user } = useAuth0();

  if (isLoading) return <div>Loading...</div>;

  if (!isAuthenticated) return <Navigate to="/" />;

  // If role required, check claim
  if (role) {
    const roles = user["https://vladtech.com/roles"] || [];
    if (!roles.includes(role)) {
      return <h2>Access Denied (Missing Role: {role})</h2>;
    }
  }

  return children;
};

export default ProtectedRoute;
