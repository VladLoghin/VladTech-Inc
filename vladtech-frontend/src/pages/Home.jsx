import LoginButton from "../components/LoginButton";
import Profile from "../components/Profile";
import LogoutButton from "../components/LogoutButton";
import useAssignClientRole from "../hooks/UseAssignClientRole";

const Home = () => {
    useAssignClientRole();

  return (
    <div>
      <h1>Home Page</h1>
      <LoginButton />
      <LogoutButton />
      <Profile />
    </div>
  );
};

export default Home;
