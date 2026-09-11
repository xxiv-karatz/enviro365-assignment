const currencyFormatter = new Intl.NumberFormat("en-ZA", {
    style: "currency",
    currency: "ZAR"
});
const dateFormatter = new Intl.DateTimeFormat("en-ZA", {
    dateStyle: "medium",
    timeStyle: "short"
});

const elements = {
    investorId: document.querySelector("#investor-id"),
    investorName: document.querySelector("#investor-name"),
    investorAge: document.querySelector("#investor-age"),
    balance: document.querySelector("#portfolio-balance"),
    productsBody: document.querySelector("#products-body"),
    productCount: document.querySelector("#product-count"),
    amount: document.querySelector("#amount"),
    amountError: document.querySelector("#amount-error"),
    form: document.querySelector("#withdrawal-form"),
    submitButton: document.querySelector("#submit-button"),
    formMessage: document.querySelector("#form-message"),
    pageMessage: document.querySelector("#page-message"),
    historyBody: document.querySelector("#history-body"),
    emptyHistory: document.querySelector("#empty-history"),
    exportButton: document.querySelector("#export-button"),
    filterType: document.querySelector("#filter-type"),
    filterFrom: document.querySelector("#filter-from"),
    filterTo: document.querySelector("#filter-to")
};

let currentBalance = 0;
let loadRequestNumber = 0;
// The full, unfiltered history for the currently loaded investor. Filters are applied
// to this in the browser so the table and the CSV download stay in sync without an
// extra round trip to the backend every time a filter changes.
let latestHistory = [];

// Format money consistently across the portfolio and history views.
function formatCurrency(value) {
    return currencyFormatter.format(Number(value) || 0);
}

// Format backend timestamps for people rather than machines.
function formatDate(value) {
    if (!value) return "-";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : dateFormatter.format(date);
}

// Display a message in a shared area and make its intent visible by color.
function showMessage(element, text, kind) {
    element.textContent = text;
    element.className = `message ${kind}`;
    element.hidden = !text;
}

// Reset a message area without leaving an empty colored box behind.
function clearMessage(element) {
    showMessage(element, "", "info");
}

// Turn a thrown request error into a message that includes field-level validation
// details when the backend supplied them, instead of just the generic top-line message.
function formatErrorMessage(error) {
    const base = error.message || "The request could not be completed.";
    if (error.validationErrors && Object.keys(error.validationErrors).length) {
        const details = Object.values(error.validationErrors).join(" ");
        return `${base} ${details}`.trim();
    }
    return base;
}

// Render the portfolio response without exposing backend objects directly in the page.
function renderPortfolio(portfolio) {
    currentBalance = Number(portfolio.balance) || 0;
    elements.investorName.textContent = portfolio.name || portfolio.investorName || "Unnamed investor";
    elements.investorAge.textContent = portfolio.age ? `Age ${portfolio.age}` : "";
    elements.balance.textContent = formatCurrency(currentBalance);
    elements.productCount.textContent = `${portfolio.products?.length || 0} products`;
    elements.productsBody.replaceChildren();

    (portfolio.products || []).forEach((product) => {
        const row = document.createElement("tr");
        row.innerHTML = `<td>${escapeHtml(product.name || "-")}</td><td>${escapeHtml(product.type || "-")}</td><td class="numeric">${formatCurrency(product.value)}</td>`;
        elements.productsBody.appendChild(row);
    });
}

// Render history in the backend's newest-first order, with a defensive client sort.
function renderHistory(history) {
    elements.historyBody.replaceChildren();
    const sortedHistory = [...(history || [])].sort((left, right) =>
        new Date(right.requestDate || 0) - new Date(left.requestDate || 0)
    );
    elements.emptyHistory.hidden = sortedHistory.length > 0;

    sortedHistory.forEach((withdrawal) => {
        const row = document.createElement("tr");
        const status = String(withdrawal.status || "").toLowerCase();
        row.innerHTML = `<td>${escapeHtml(formatDate(withdrawal.requestDate))}</td>
            <td>${escapeHtml(withdrawal.type || "-")}</td>
            <td class="numeric">${formatCurrency(withdrawal.amount)}</td>
            <td><span class="status ${status}">${escapeHtml(withdrawal.status || "-")}</span></td>
            <td>${escapeHtml(withdrawal.status === "REJECTED" ? (withdrawal.reason || "-") : "")}</td>`;
        elements.historyBody.appendChild(row);
    });
}

// Escape backend text before inserting it into table HTML.
function escapeHtml(value) {
    return String(value).replace(/[&<>'"]/g, (character) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;"
    }[character]));
}

// Read the current filter controls into a plain object shared by the table render
// and the CSV export link.
function currentFilters() {
    return {
        type: elements.filterType.value || null,
        from: elements.filterFrom.value || null,
        to: elements.filterTo.value || null
    };
}

// Apply the type/date filters to the cached history, re-render the table, and keep
// the CSV download link pointed at the same filters.
function applyFilters() {
    const filters = currentFilters();
    const fromDate = filters.from ? new Date(`${filters.from}T00:00:00`) : null;
    const toDate = filters.to ? new Date(`${filters.to}T23:59:59.999`) : null;

    const filtered = latestHistory.filter((withdrawal) => {
        if (filters.type && withdrawal.type !== filters.type) return false;
        const requestDate = withdrawal.requestDate ? new Date(withdrawal.requestDate) : null;
        if (fromDate && (!requestDate || requestDate < fromDate)) return false;
        if (toDate && (!requestDate || requestDate > toDate)) return false;
        return true;
    });

    renderHistory(filtered);
    updateExportLink(Number(elements.investorId.value), filters);
}

// Load both dashboard sections for the currently selected investor.
async function loadInvestor(investorId) {
    const requestNumber = ++loadRequestNumber;
    clearMessage(elements.pageMessage);
    elements.investorName.textContent = "Loading portfolio...";
    elements.productsBody.replaceChildren();
    elements.historyBody.replaceChildren();
    elements.emptyHistory.hidden = true;

    try {
        const [portfolio, history] = await Promise.all([
            fetchPortfolio(investorId),
            fetchWithdrawalHistory(investorId)
        ]);
        // Ignore a slower response if the user changed IDs while it was loading.
        if (requestNumber !== loadRequestNumber) return;
        renderPortfolio(portfolio);
        latestHistory = history || [];
        applyFilters();
    } catch (error) {
        if (requestNumber !== loadRequestNumber) return;
        currentBalance = 0;
        latestHistory = [];
        elements.investorName.textContent = "Portfolio unavailable";
        elements.investorAge.textContent = "";
        elements.balance.textContent = "--";
        showMessage(elements.pageMessage, formatErrorMessage(error), "error");
    }
}

// Validate the amount before the request can reach the backend.
function validateAmount() {
    const amount = Number(elements.amount.value);
    let error = "";
    if (!elements.amount.value.trim()) error = "Amount is required.";
    else if (!Number.isFinite(amount) || amount <= 0) error = "Amount must be a positive number.";
    else if (amount > currentBalance) error = "Amount must not exceed the current balance.";
    elements.amountError.textContent = error;
    return !error;
}

// Submit the request, then reload the balance and audit history from the server.
async function handleWithdrawalSubmit(event) {
    event.preventDefault();
    clearMessage(elements.formMessage);
    if (!validateAmount()) return;

    const request = {
        investorId: Number(elements.investorId.value),
        amount: Number(elements.amount.value),
        type: document.querySelector("input[name='withdrawal-type']:checked").value
    };
    elements.submitButton.disabled = true;

    try {
        await submitWithdrawal(request);
        showMessage(elements.formMessage, "Withdrawal request approved.", "success");
        elements.amount.value = "";
        await loadInvestor(request.investorId);
    } catch (error) {
        showMessage(elements.formMessage, formatErrorMessage(error), "error");
        // Re-fetch rejected attempts too because the backend stores them in history.
        try {
            latestHistory = await fetchWithdrawalHistory(request.investorId);
            applyFilters();
        } catch (historyError) {
            showMessage(elements.pageMessage, formatErrorMessage(historyError), "error");
        }
    } finally {
        elements.submitButton.disabled = false;
    }
}

// Keep the CSV link pointed at the selected investor and current filters without
// leaving the page.
function updateExportLink(investorId, filters = {}) {
    elements.exportButton.href = getExportUrl(investorId, filters);
}

elements.investorId.addEventListener("change", () => {
    const investorId = Number(elements.investorId.value);
    if (!Number.isInteger(investorId) || investorId < 1) {
        showMessage(elements.pageMessage, "Investor ID must be a positive whole number.", "error");
        return;
    }
    loadInvestor(investorId);
});
elements.amount.addEventListener("input", validateAmount);
elements.form.addEventListener("submit", handleWithdrawalSubmit);
elements.filterType.addEventListener("change", applyFilters);
elements.filterFrom.addEventListener("change", applyFilters);
elements.filterTo.addEventListener("change", applyFilters);

// Start with the seeded default investor when the page opens.
loadInvestor(Number(elements.investorId.value));
