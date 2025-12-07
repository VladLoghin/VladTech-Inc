import { useAuth0 } from "@auth0/auth0-react";
import { LogOut } from "lucide-react";

const LogoutButton = () => {
  const { logout } = useAuth0();

  return (
    <button
      onClick={() =>
        logout({
          logoutParams: { returnTo: window.location.origin },
        })
      }
      className="flex items-center gap-2 px-6 py-2.5 bg-white text-black font-medium tracking-wide hover:bg-gray-100 transition-colors rounded"
    >
      <LogOut className="h-5 w-5" />
      LOG OUT
    </button>
  );
};

export default LogoutButton;
