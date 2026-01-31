/**
 * Helper to normalize image URLs coming from the backend
 * Handles both GridFS-based URLs and traditional static file URLs
 *
 * @param {string} url - The URL or file ID from the backend
 * @param {boolean} forceDownload - Whether to append download parameter (default: false)
 * @returns {string} - Normalized URL ready for use in <img> src or fetch
 */
export default function getImageUrl(url, forceDownload = false) {
    if (!url) {
        return "/images/placeholder.png";
    }

    // If already an absolute URL (http:// or https://), return as-is
    try {
        const parsed = new URL(url);
        return parsed.href;
    } catch {
        // Not an absolute URL, continue processing
    }

    // IMPORTANT: GridFS paths starting with /api/uploads/ or /uploads/
    // will be proxied to backend by Vite dev server
    // In production, nginx handles this routing
    
    // If it starts with /, it's already a path - ensure it goes through backend
    if (url.startsWith("/")) {
        // Convert /uploads/reviews/{id} to /api/uploads/reviews/{id}
        // to ensure it goes through the backend API
        let finalUrl = url;
        if (url.startsWith("/uploads/") && !url.startsWith("/api/uploads/")) {
            finalUrl = `/api${url}`;
        }
        
        if (forceDownload && !finalUrl.includes("?download=")) {
            const separator = finalUrl.includes("?") ? "&" : "?";
            return `${finalUrl}${separator}download=true`;
        }
        return finalUrl;
    }

    // If it looks like a MongoDB ObjectId (24 hex characters),
    // construct the GridFS URL
    if (/^[0-9a-fA-F]{24}$/.test(url)) {
        const baseUrl = `/api/uploads/reviews/${url}`;
        return forceDownload ? `${baseUrl}?download=true` : baseUrl;
    }

    // If it's a simple filename without path, assume it's in the reviews folder
    if (!url.includes("/") && !url.includes("\\")) {
        const baseUrl = `/api/uploads/reviews/${url}`;
        return forceDownload ? `${baseUrl}?download=true` : baseUrl;
    }

    // Default: return as-is (for legacy static file paths)
    return url;
}

/**
 * Create a download link for a file
 * @param {string} url - The URL or file ID
 * @returns {string} - URL with download parameter
 */
export function getDownloadUrl(url) {
    return getImageUrl(url, true);
}

/**
 * Extract file ID from a GridFS URL
 * @param {string} url - The full URL
 * @returns {string|null} - The file ID or null if not a GridFS URL
 */
export function extractFileId(url) {
    if (!url) return null;

    // Match /uploads/reviews/{id} pattern
    const match = url.match(/\/(?:api\/)?uploads\/reviews\/([0-9a-fA-F]{24})/);
    return match ? match[1] : null;
}

/**
 * Validate if a string is a valid MongoDB ObjectId
 * @param {string} id - The ID to validate
 * @returns {boolean} - Whether the ID is valid
 */
export function isValidFileId(id) {
    return /^[0-9a-fA-F]{24}$/.test(id);
}