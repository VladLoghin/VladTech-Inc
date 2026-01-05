/**
 * Auth0 configuration for API calls
 * 
 * This module provides the correct configuration for getAccessTokenSilently()
 * to ensure proper audience is set for role-based authorization in Docker.
 */

export const AUTH0_API_AUDIENCE = "https://vladtech/api";

/**
 * Options to pass to getAccessTokenSilently() for API calls
 * 
 * Usage:
 * const token = await getAccessTokenSilently(authOptions);
 */
export const authOptions = {
    authorizationParams: {
        audience: AUTH0_API_AUDIENCE,
    },
};
