// Resident status check: complaint id + random tracking code, then the audit timeline.
(function () {
    const form = document.querySelector("#track-form");
    const out = document.querySelector("#track-result");

    const badgeClass = s => `badge text-white status-${s}`;

    function render(data) {
        const c = data.complaint;
        const steps = (data.history || []).map(h => `
            <li>
                <div class="fw-semibold">${h.oldStatus ? escapeHtml(h.oldStatus) + " → " : ""}${escapeHtml(h.newStatus)}</div>
                <div class="small text-muted">${new Date(h.changedAt).toLocaleString()} · ${escapeHtml(h.changedBy)}</div>
            </li>`).join("");
        out.innerHTML = `
            <div class="card shadow-sm">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
                        <div>
                            <h4 class="mb-1">Complaint #${c.id}</h4>
                            <div class="text-muted">Room ${escapeHtml(c.roomNumber)} · ${escapeHtml(c.category)}</div>
                        </div>
                        <span class="${badgeClass(c.status)}">${escapeHtml(c.status)}</span>
                    </div>
                    <p class="mt-3 mb-1">${escapeHtml(c.description)}</p>
                    <p class="small text-muted">Filed ${new Date(c.createdAt).toLocaleString()} by ${escapeHtml(c.submitterName)}</p>
                    <hr/>
                    <h6 class="text-muted">Timeline</h6>
                    <ol class="timeline">${steps || `<li class="text-muted">No transitions yet.</li>`}</ol>
                </div>
            </div>`;
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();
        const id = form.complaintId.value.trim();
        const token = form.trackingToken.value.trim();
        out.innerHTML = `<p class="text-muted">Looking up…</p>`;
        try {
            const res = await fetch(`/api/complaints/${encodeURIComponent(id)}/public?token=${encodeURIComponent(token)}`);
            if (res.status === 404) {
                out.innerHTML = `<div class="alert alert-warning">No complaint #${escapeHtml(id)} matches that tracking code.</div>`;
                return;
            }
            if (!res.ok) throw new Error("HTTP " + res.status);
            render(await res.json());
        } catch (err) {
            out.innerHTML = `<div class="alert alert-danger">Could not look up that complaint. Try again.</div>`;
            console.error(err);
        }
    });
})();

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[ch]));
}