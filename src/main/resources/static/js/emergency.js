// Public emergency card: first aid + one-tap phone links.
(async function () {
    const contents = document.querySelector("#first-aid-contents");
    const guidelines = document.querySelector("#first-aid-guidelines");
    const contacts = document.querySelector("#emergency-contacts");
    if (!contents || !guidelines || !contacts) return;

    try {
        const res = await fetch("/api/emergency");
        if (!res.ok) throw new Error("HTTP " + res.status);
        const data = await res.json();

        const info = data.firstAid || {};
        contents.textContent = info.contents || "Not added yet.";
        guidelines.textContent = info.guidelines || "Not added yet.";

        if (!data.contacts || !data.contacts.length) {
            contacts.innerHTML = `<p class="text-muted">No emergency contacts added yet.</p>`;
            return;
        }
        contacts.innerHTML = data.contacts.map(c => `
            <a class="list-group-item list-group-item-action d-flex justify-content-between align-items-center gap-3"
               href="tel:${telHref(c.phone)}">
                <span class="fw-semibold">${escapeHtml(c.roleTitle)}</span>
                <span class="text-muted">${escapeHtml(c.phone)}</span>
            </a>`).join("");
    } catch (err) {
        contents.textContent = "Could not load the first-aid card.";
        guidelines.textContent = "";
        contacts.innerHTML = `<div class="alert alert-danger mb-0">Could not load emergency contacts.</div>`;
        console.error(err);
    }
})();

function telHref(phone) {
    const trimmed = String(phone ?? "").trim();
    const plus = trimmed.startsWith("+") ? "+" : "";
    return plus + trimmed.replace(/\D/g, "");
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[ch]));
}
