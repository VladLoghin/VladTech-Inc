import { useEffect } from "react";
import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";

export default function useAssignClientRole() {
  const { user, getAccessTokenSilently, isAuthenticated } = useAuth0();

  console.log(" Hook loaded");

  useEffect(() => {
    //console.log(" useEffect triggered:", { isAuthenticated, user });

    if (!isAuthenticated || !user) return;

    const assign = async () => {
      try {
        const token = await getAccessTokenSilently();
        console.log("Token:", token);

        const res = await axios.patch(
          `http://localhost:8080/api/role-assignment/users/${user.sub}/roles/client`,
          {},
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        console.log(" ROLE ASSIGNED:", res.data);
      } catch (err) {
        console.error(" ROLE ERROR:", err.response?.data || err);
      }
    };

    assign();
  }, [isAuthenticated, user]);
}
