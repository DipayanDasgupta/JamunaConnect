// Module A: contacts page is driven by the editable directory API.
(async function () {
    const tbody = document.querySelector("#contacts-table tbody");
    try {
        const res = await fetch("/api/contacts");
        if (!res.ok) throw new Error("HTTP " + res.status);
        const rows = await res.json();
        tbody.innerHTML = rows.map(c => `
            <tr>
                <td>${escapeHtml(c.role)}</td>
                <td>${escapeHtml(c.name)}</td>
                <td>${c.phone ? `<a href="tel:${encodeURIComponent(c.phone)}">${escapeHtml(c.phone)}</a>` : "-"}</td>
                <td>${c.email ? `<a href="mailto:${encodeURIComponent(c.email)}">${escapeHtml(c.email)}</a>` : "-"}</td>
            </tr>`).join("");
        if (rows.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="text-muted text-center">No contacts yet.</td></tr>`;
        }
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-danger">Could not load contacts.</td></tr>`;
        console.error(err);
    }
})();

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[ch]));
}