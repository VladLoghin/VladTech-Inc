import { useAuth0 } from "@auth0/auth0-react";
import { useState } from "react";
import axios from "axios";

const Client = () => {
  const { getAccessTokenSilently } = useAuth0();
  const [message, setMessage] = useState("");

  const callClientEndpoint = async () => {
    try {
      const token = await getAccessTokenSilently();

      const response = await axios.get("http://localhost:8080/api/client/info", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      setMessage(response.data);
    } catch (error) {
      console.error("Error calling client endpoint:", error);
      setMessage("You are not authorized or endpoint failed.");
    }
  };

  return (
    <div>
      <h1>Client Area — Only for Client Role</h1>
      <button onClick={callClientEndpoint}>Call Client Endpoint</button>

      {message && <p style={{ marginTop: "20px", fontSize: "18px" }}>{message}</p>}
    </div>
  );
};

export default Client;
