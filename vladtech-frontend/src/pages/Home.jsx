import { useState } from "react";
import HomePage from "./HomePage";
import ContactUs from "../components/ContactUs";

const Home = () => {
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
