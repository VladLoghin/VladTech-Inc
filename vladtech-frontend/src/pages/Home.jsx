import LoginButton from "../components/LoginButton";
import Profile from "../components/Profile";
import LogoutButton from "../components/LogoutButton";
import useAssignClientRole from "../hooks/UseAssignClientRole";
import ContactUs from "../components/ContactUs";

const Home = () => {
    useAssignClientRole();

  return (
    <div>
      <h1>Home Page</h1>
      <LoginButton />
      <LogoutButton />
      <Profile />
      <ContactUs />
    </div>
  );
};

export default Home;
