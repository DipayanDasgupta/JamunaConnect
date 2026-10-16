// Resident status check: complaint id + room number, then the audit timeline.
(function () {
    const form = document.querySelector("#track-form");
    const out = document.querySelector("#track-result");

    function render(data) {
        const c = data.complaint;
        const steps = (data.history || []).map(h => `
            <li>
                <div class="fw-semibold">${h.oldStatus ? escapeHtml(h.oldStatus) + " → " : ""}${escapeHtml(h.newStatus)}</div>
                <div class="small muted">${new Date(h.changedAt).toLocaleString()} · ${escapeHtml(h.changedBy)}</div>
            </li>`).join("");
        out.innerHTML = `
            <div class="card">
                <div style="display:flex;justify-content:space-between;gap:1rem;flex-wrap:wrap;">
                    <div>
                        <h3 style="margin:0 0 .2rem;">Complaint #${c.id}</h3>
                        <div class="muted">Room ${escapeHtml(c.roomNumber)} · ${escapeHtml(c.category)}</div>
                    </div>
                    <span class="badge ${escapeHtml(c.status)}">${escapeHtml(c.status)}</span>
                </div>
                <p style="margin:1rem 0 .2rem;">${escapeHtml(c.description)}</p>
                <p class="small muted">Filed ${new Date(c.createdAt).toLocaleString()} by ${escapeHtml(c.submitterName)}</p>
                <hr style="border:0;border-top:1px solid #e5e7eb;margin:1rem 0;"/>
                <h4 class="muted" style="margin:.2rem 0 .6rem;font-size:.95rem;">Timeline</h4>
                <ol class="timeline">${steps || `<li class="muted">No transitions yet.</li>`}</ol>
            </div>`;
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();
        const id = form.complaintId.value.trim();
        const room = form.room.value.trim();
        out.innerHTML = `<p class="muted">Looking up…</p>`;
        try {
            const res = await fetch(`/api/complaints/${encodeURIComponent(id)}/public?room=${encodeURIComponent(room)}`);
            if (res.status === 404) {
                out.innerHTML = `<div class="alert warning">No complaint #${escapeHtml(id)} found for room ${escapeHtml(room)}. Check both values.</div>`;
                return;
            }
            if (!res.ok) throw new Error("HTTP " + res.status);
            render(await res.json());
        } catch (err) {
            out.innerHTML = `<div class="alert danger">Could not look up that complaint. Try again.</div>`;
            console.error(err);
        }
    });
})();

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[ch]));
}