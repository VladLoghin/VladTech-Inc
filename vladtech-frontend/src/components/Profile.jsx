import { useAuth0 } from "@auth0/auth0-react";

const Profile = () => {
  const { user, isAuthenticated } = useAuth0();

  if (!isAuthenticated) return null;

  return (
    <div>
      <img src={user.picture} width="70" alt="profile" />
      <h3>{user.name}</h3>
      <p>{user.email}</p>

      <p>
        <strong>Roles:</strong>{" "}
        {user["https://vladtech.com/roles"]?.join(", ") || "None"}
      </p>
    </div>
  );
};

export default Profile;
