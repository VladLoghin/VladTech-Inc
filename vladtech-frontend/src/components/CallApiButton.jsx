import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";

const CallApiButton = () => {
  const { getAccessTokenSilently } = useAuth0();

  const callApi = async () => {
    const token = await getAccessTokenSilently();

    const res = await axios.get("http://localhost:8080/api/private", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    console.log(res.data);
    alert("Private API response logged to console.");
  };

  return <button onClick={callApi}>Call Private API</button>;
};

export default CallApiButton;
