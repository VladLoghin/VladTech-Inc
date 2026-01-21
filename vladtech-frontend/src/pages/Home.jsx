// javascript
import { useState } from 'react';
import HomePage from './HomePage';
import ContactUs from '../components/ContactUs';
import EstimateInputModal from '../components/estimates/EstimateInputModal';
import { useLanguage } from '../context/LanguageContext';
import { estimateTranslations } from '../translations/estimateTranslations';

const Home = () => {
  const { language } = useLanguage();
  const t = estimateTranslations[language];
  const [isContactModalOpen, setIsContactModalOpen] = useState(false);
  const [isEstimateModalOpen, setIsEstimateModalOpen] = useState(false);

  return (
    <>
      <HomePage
        onOpenContactModal={() => setIsContactModalOpen(true)}
        onOpenEstimateModal={() => setIsEstimateModalOpen(true)}
      />
      <ContactUs isOpen={isContactModalOpen} onClose={() => setIsContactModalOpen(false)} />
      <EstimateInputModal
        onClose={() => setIsEstimateModalOpen(false)}
        presets={[
          {
            name: t.defaultPreset,
            defaultValues: { squareFeet: 0, materialCostPerSqFt: 0 },
            fields: [
              {
                name: 'squareFeet',
                label: t.squareFeet,
                type: 'number',
                required: true,
                min: 1,
                step: '0.01',
              },
              {
                name: 'materialCostPerSqFt',
                label: t.materialCostPerSqFt,
                type: 'number',
                required: true,
                min: 0,
                step: '0.01',
              },
            ],
          },
        ]}
        isOpen={isEstimateModalOpen}
      />
    </>
  );
};

export default Home;
