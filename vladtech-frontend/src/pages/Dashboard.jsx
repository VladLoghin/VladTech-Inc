import { useAuth0 } from '@auth0/auth0-react';
import { useTranslation } from 'react-i18next';
import Profile from '../components/Profile';
import CallApiButton from '../components/CallApiButton';

const Dashboard = () => {
  const { user, isAuthenticated } = useAuth0();
  const { t } = useTranslation();

  if (!isAuthenticated) return <div>{t('loading')}</div>;

  console.log(' Dashboard user:', user);

  return (
    <div>
      <h1>{t('dashboard.title')}</h1>
      <Profile />
      <CallApiButton />
    </div>
  );
};

export default Dashboard;
