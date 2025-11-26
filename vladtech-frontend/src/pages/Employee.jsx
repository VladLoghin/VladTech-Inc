import { useAuth0 } from "@auth0/auth0-react";
import { useState } from "react";
import axios from "axios";

const Employee = () => {
  const { getAccessTokenSilently } = useAuth0();
  const [message, setMessage] = useState("");

  const callEmployeeEndpoint = async () => {
    try {
      const token = await getAccessTokenSilently();

      const response = await axios.get(
        "http://localhost:8080/api/employee/info",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setMessage(response.data);
    } catch (error) {
      console.error("Error calling employee endpoint:", error);
      setMessage("You are not authorized or endpoint failed.");
    }
  };

  return (
    <div>
      <h1>Employee Area — Only for Employees</h1>
      <button onClick={callEmployeeEndpoint}>Call Employee Endpoint</button>

      {message && (
        <p style={{ marginTop: "20px", fontSize: "18px" }}>{message}</p>
      )}
    </div>
  );
};

export default Employee;
