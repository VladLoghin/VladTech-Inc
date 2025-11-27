import { useAuth0 } from "@auth0/auth0-react";
import Profile from "../components/Profile";
import CallApiButton from "../components/CallApiButton";
import { useEffect } from "react";
import axios from "axios";


const Dashboard = () => {
  const { user, isAuthenticated, getAccessTokenSilently } = useAuth0();

   useEffect(() => {
    console.log(" useEffect triggered:", { isAuthenticated, user });

    if (!isAuthenticated || !user) return;

    const assign = async () => {
      try {
        const token = await getAccessTokenSilently();
        console.log("🔥 Token:", token);

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
  }, [isAuthenticated, user, getAccessTokenSilently]);

  if (!isAuthenticated) return <div>Loading...</div>;

  console.log(" Dashboard user:", user);

  return (
    <div>
      <h1>User Dashboard</h1>
      <Profile />
      <CallApiButton />
    </div>
  );
};

export default Dashboard;
