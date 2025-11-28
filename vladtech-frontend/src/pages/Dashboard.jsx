import { useAuth0 } from "@auth0/auth0-react";
import Profile from "../components/Profile";
import CallApiButton from "../components/CallApiButton";

const Dashboard = () => {
  const { user, isAuthenticated } = useAuth0();


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
