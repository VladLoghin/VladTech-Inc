import { useAuth0 } from "@auth0/auth0-react";
import { useState } from "react";
import axios from "axios";

export default function CompleteProfile({ onComplete }) {
  const { user, getAccessTokenSilently } = useAuth0();

  const [form, setForm] = useState({
    name: "",
    phone_number: ""
  });

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();

    const token = await getAccessTokenSilently();

    await axios.patch(
      `http://localhost:8080/api/user-profile/${user.sub}`,
      form,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );

    alert("Profile Updated!");
    onComplete();
  };

  return (
    <div style={{ padding: 20 }}>
      <h2>Complete Your Profile</h2>

      <form onSubmit={handleSubmit}>
        <label>Name</label>
        <input
          name="name"
          value={form.name}
          onChange={handleChange}
          required
        />

        <label>Phone Number</label>
        <input
          name="phone_number"
          value={form.phone_number}
          onChange={handleChange}
          required
        />

        <button type="submit">Save Profile</button>
      </form>
    </div>
  );
}
