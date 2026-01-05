import { authOptions } from "../config/auth.js";

export async function getFreshUserInfo(getAccessTokenSilently) {
  const token = await getAccessTokenSilently(authOptions);

  const res = await fetch("https://dev-ljz84r2xvrlnftfv.ca.auth0.com/userinfo", {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  return res.json();
}
