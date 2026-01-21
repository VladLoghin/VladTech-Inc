export async function getFreshUserInfo(getAccessTokenSilently) {
  const token = await getAccessTokenSilently({
    authorizationParams: {
      audience: 'https://vladtech/api',
    },
  });

  const res = await fetch('https://dev-ljz84r2xvrlnftfv.ca.auth0.com/userinfo', {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return res.json();
}
