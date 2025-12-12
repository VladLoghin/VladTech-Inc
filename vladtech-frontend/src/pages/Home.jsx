import { useState } from "react";
import HomePage from "./HomePage";
import ContactUs from "../components/ContactUs";
import EstimateInputModal from "../components/estimates/EstimateInputModal";

const Home = () => {
    const [isContactModalOpen, setIsContactModalOpen] = useState(false);
    const [isEstimateModalOpen, setIsEstimateModalOpen] = useState(false);

    const handleEstimateSubmit = (formData) => {
        console.log("Estimate submitted:", formData);
        setIsEstimateModalOpen(false);
    };

  return (
    <>
      <HomePage onOpenContactModal={() => setIsContactModalOpen(true)}
                onOpenEstimateModal={() => {console.log("Opening Estimate Modal");
                                                    setIsEstimateModalOpen(true)}}
      />
      <ContactUs 
        isOpen={isContactModalOpen} 
        onClose={() => setIsContactModalOpen(false)} 
      />
      <EstimateInputModal
          onSubmit={handleEstimateSubmit}
          onClose={() => setIsEstimateModalOpen(false)}
          presets={[
              {
                  name: "Default",
                  defaultValues: { squareFeet: 0, materialCostPerSqFt: 0 },
                  fields: [
                      { name: "squareFeet", label: "Square Feet", type: "number", required: true },
                      { name: "materialCostPerSqFt", label: "Average Material Cost per Sq Ft", type: "number", required: true },
                  ],
              },
          ]}
          isOpen={isEstimateModalOpen}
      />
    </>
  );
};

export default Home;
