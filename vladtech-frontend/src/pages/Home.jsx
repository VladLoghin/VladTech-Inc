import { useState } from "react";
import useAssignClientRole from "../hooks/UseAssignClientRole";
import HomePage from "./HomePage";
import ContactUs from "../components/ContactUs";

const Home = () => {
    useAssignClientRole();
    const [isContactModalOpen, setIsContactModalOpen] = useState(false);

  return (
    <>
      <HomePage onOpenContactModal={() => setIsContactModalOpen(true)} />
      <ContactUs 
        isOpen={isContactModalOpen} 
        onClose={() => setIsContactModalOpen(false)} 
      />
    </>
  );
};

export default Home;
