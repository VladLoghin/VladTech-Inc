import { Auth0Provider } from "@auth0/auth0-react";

const Auth0ProviderWithConfig = ({ children }) => {
  const domain = "dev-ljz84r2xvrlnftfv.ca.auth0.com";
  const clientId = "sDVdjRgneqMMYuQm8njufqcG0yrPV2j6";
  const audience = "https://vladtech/api";

  return (
    <Auth0Provider
      domain={domain}
      clientId={clientId}
      authorizationParams={{
        redirect_uri: window.location.origin,
        audience,
        scope: "openid profile email"
      }}
    >
      {children}
    </Auth0Provider>
  );
};

export default Auth0ProviderWithConfig;
