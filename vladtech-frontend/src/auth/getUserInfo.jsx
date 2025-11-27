export async function getFreshUserInfo(getAccessTokenSilently) {
  const token = await getAccessTokenSilently();

  const res = await fetch("https://dev-ljz84r2xvrlnftfv.ca.auth0.com/userinfo", {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  return res.json(); // returns full user with updated metadata
}
