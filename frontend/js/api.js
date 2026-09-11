// Keep the backend address in one place so the frontend is easy to move.
const API_BASE_URL = "http://localhost:8080";

// Parse JSON errors when the backend provides them, while retaining network failures.
// Field-level validation errors and rejected-withdrawal details are attached to the
// thrown Error so callers can show more than just the top-level message.
async function requestJson(url, options = {}) {
    const response = await fetch(url, options);
    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json") ? await response.json() : null;

    if (!response.ok) {
        const error = new Error(payload?.message || "The backend rejected the request.");
        error.validationErrors = payload?.validationErrors || null;
        error.rejection = payload?.rejection || null;
        throw error;
    }
    return payload;
}

// Load the selected investor's portfolio and products.
function fetchPortfolio(investorId) {
    return requestJson(`${API_BASE_URL}/api/investors/${investorId}/portfolio`);
}

// Submit a withdrawal request to the Spring Boot API.
function submitWithdrawal(request) {
    return requestJson(`${API_BASE_URL}/api/withdrawals`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request)
    });
}

// Load notices for the selected investor.
function fetchWithdrawalHistory(investorId) {
    return requestJson(`${API_BASE_URL}/api/withdrawals/${investorId}`);
}

// Build the CSV export URL, matching the backend's actual supported filters
// (type, from, to). Only include params the user has actually set.
function getExportUrl(investorId, filters = {}) {
    const params = new URLSearchParams();
    if (filters.type) params.set("type", filters.type);
    if (filters.from) params.set("from", filters.from);
    if (filters.to) params.set("to", filters.to);
    const query = params.toString();
    return `${API_BASE_URL}/api/withdrawals/${investorId}/export${query ? `?${query}` : ""}`;
}
