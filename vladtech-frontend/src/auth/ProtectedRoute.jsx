import { useAuth0 } from "@auth0/auth0-react";
import { Navigate } from "react-router-dom";
import NotAuthorized from "../pages/NotAuthorized";

const ProtectedRoute = ({ children, roles = [] }) => {
  const { isAuthenticated, isLoading, user } = useAuth0();

  if (isLoading) return <div>Loading...</div>;

  // Not logged in? Send home
  if (!isAuthenticated) return <Navigate to="/" />;

  const userRoles = user["https://vladtech.com/roles"] || [];

  if (roles.length > 0) {
    const hasRequiredRole = roles.some((role) => userRoles.includes(role));

    if (!hasRequiredRole) {
      return <NotAuthorized />;
    }
  }

  return children;
};

export default ProtectedRoute;
