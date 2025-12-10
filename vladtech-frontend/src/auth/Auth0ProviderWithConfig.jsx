import { Auth0Provider } from "@auth0/auth0-react";

const Auth0ProviderWithConfig = ({ children, navigate }) => {
  const domain = "dev-ljz84r2xvrlnftfv.ca.auth0.com";
  const clientId = "sDVdjRgneqMMYuQm8njufqcG0yrPV2j6";
  const audience = "https://vladtech/api";

  const onRedirectCallback = (appState) => {
      navigate(appState?.returnTo || window.location.pathname);
  };

  return (
    <Auth0Provider
      domain={domain}
      clientId={clientId}
      authorizationParams={{
        redirect_uri: window.location.origin,
        audience,
        scope: "openid profile email"
      }}
      onRedirectCallback={onRedirectCallback}
      cacheLocation="localstorage"
    >
      {children}
    </Auth0Provider>
  );
};

export default Auth0ProviderWithConfig;
