import { useAuth0 } from "@auth0/auth0-react";
import { LogIn } from "lucide-react";

const LoginButton = () => {
  const { loginWithRedirect } = useAuth0();

  return (
    <button 
      onClick={() => loginWithRedirect()}
      className="flex items-center gap-2 px-6 py-2.5 bg-yellow-400 text-black font-medium tracking-wide hover:bg-yellow-500 transition-colors rounded"
    >
      <LogIn className="h-5 w-5" />
      LOG IN
    </button>
  );
};

export default LoginButton;
