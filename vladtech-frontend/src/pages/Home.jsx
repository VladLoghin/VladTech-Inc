import LoginButton from "../components/LoginButton";
import Profile from "../components/Profile";
import LogoutButton from "../components/LogoutButton";

const Home = () => {
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
