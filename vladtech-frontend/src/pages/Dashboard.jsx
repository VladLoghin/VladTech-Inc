import Profile from "../components/Profile";
import CallApiButton from "../components/CallApiButton";

const Dashboard = () => {
  return (
    <div>
      <h1>User Dashboard</h1>
      <Profile />
      <CallApiButton />
    </div>
  );
};

export default Dashboard;
