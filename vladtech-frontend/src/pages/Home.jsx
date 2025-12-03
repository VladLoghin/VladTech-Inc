import useAssignClientRole from "../hooks/UseAssignClientRole";
import HomePage from "./HomePage";

const Home = () => {
    useAssignClientRole();

  return <HomePage />;
};

export default Home;
