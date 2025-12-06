import { Link } from "react-router-dom";
import { useAuth0 } from "@auth0/auth0-react";
import LoginButton from "./LoginButton";
import LogoutButton from "./LogoutButton";

const Navbar = () => {
  const { isAuthenticated, user } = useAuth0();

  // Get user roles from Auth0 custom claim
  const roles = user?.["https://vladtech.com/roles"] || [];

  return (
    <nav className="flex items-center justify-between px-8 py-4 bg-black/95 backdrop-blur-sm border-b border-white/10 text-white sticky top-0 z-50">
      {/* LEFT SIDE LINKS */}
      <div className="flex items-center gap-8">
        <Link className="text-white tracking-widest hover:text-yellow-400 transition-colors" to="/">
          Home
        </Link>
      </div>

      {/* RIGHT SIDE ACTIONS */}
      <div className="flex items-center gap-4">
        {!isAuthenticated && <LoginButton />}
        {isAuthenticated && <LogoutButton />}
      </div>
    </nav>
  );
};

export default Navbar;
