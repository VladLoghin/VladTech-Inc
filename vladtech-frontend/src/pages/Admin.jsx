import { useAuth0 } from "@auth0/auth0-react";
import { useState } from "react";
import axios from "axios";

const Admin = () => {
  const { getAccessTokenSilently } = useAuth0();
  const [message, setMessage] = useState("");

  const callAdminEndpoint = async () => {
    try {
      const token = await getAccessTokenSilently();
      //console.log("Access Token:", token);

      const response = await axios.get("http://localhost:8080/api/admin/dashboard", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      setMessage(response.data);
    } catch (error) {
      console.error("Error calling admin endpoint:", error);
      setMessage("You are not authorized or endpoint failed.");
    }
  };

  return (
    <div>
      <h1>Admin Area — Only for Admin Role</h1>
      <button onClick={callAdminEndpoint}>Call Admin Endpoint</button>

      {message && (
        <p style={{ marginTop: "20px", fontSize: "18px" }}>{message}</p>
      )}
    </div>
  );
};

export default Admin;
