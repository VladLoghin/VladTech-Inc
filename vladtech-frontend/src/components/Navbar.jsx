import { Link } from "react-router-dom";
import { useAuth0 } from "@auth0/auth0-react";
import LoginButton from "./LoginButton";
import LogoutButton from "./LogoutButton";

const Navbar = () => {
  const { isAuthenticated, user } = useAuth0();

  // Get user roles from Auth0 custom claim
  const roles = user?.["https://vladtech.com/roles"] || [];

  return (
    <nav
      style={{
        display: "flex",
        padding: "12px 20px",
        background: "#222",
        color: "white",
        alignItems: "center",
        justifyContent: "space-between",
      }}
    >
      {/* LEFT SIDE LINKS */}
      <div style={{ display: "flex", gap: "18px" }}>
        <Link style={{ color: "white" }} to="/">
          Home
        </Link>

        {isAuthenticated && (
          <Link style={{ color: "white" }} to="/dashboard">
            Dashboard
          </Link>
        )}

        {/* Admin-only link */}
        {roles.includes("Admin") && (
          <Link style={{ color: "white" }} to="/admin">
            Admin Panel
          </Link>
        )}

        {/* Employee-only link */}
        {roles.includes("Employee") && (
          <Link style={{ color: "white" }} to="/employee">
            Employee Tools
          </Link>
        )}

        {roles.includes("Client") && (
          <Link style={{ color: "white" }} to="/client">
            Client Area
          </Link>
        )}

      </div>



      {/* RIGHT SIDE ACTIONS */}
      <div style={{ display: "flex", gap: "12px" }}>
        {!isAuthenticated && <LoginButton />}
        {isAuthenticated && <LogoutButton />}
      </div>
    </nav>
  );
};

export default Navbar;
