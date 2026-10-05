// Module C: triage with stat cards, client-side search/filter, transitions,
// and the audit timeline in a modal instead of an alert() box.
(function () {
    const alertBox = document.querySelector("#alert");
    const table = document.querySelector("#complaints-table");
    const statusFilter = document.querySelector("#filter-status");
    const categoryFilter = document.querySelector("#filter-category");
    const textFilter = document.querySelector("#filter-text");
    const rowCount = document.querySelector("#row-count");
    if (!table) return;

    // Relative ages from the ISO timestamps rendered server-side.
    table.querySelectorAll(".age-cell[data-created]").forEach(cell => {
        const created = new Date(cell.dataset.created);
        if (isNaN(created)) return;
        const mins = Math.max(0, Math.round((Date.now() - created.getTime()) / 60000));
        const age = mins < 60 ? `${mins}m ago`
            : mins < 1440 ? `${Math.floor(mins / 60)}h ago`
            : `${Math.floor(mins / 1440)}d ago`;
        cell.textContent = `${age} · ${cell.textContent}`;
        if (mins >= 72 * 60) {
            const row = cell.closest("tr");
            const badge = row && row.querySelector(".status-badge");
            if (badge && (badge.textContent === "OPEN" || badge.textContent === "ACKNOWLEDGED")) {
                row.classList.add("table-warning");
                cell.textContent += " · stale";
            }
        }
    });

    function show(kind, message) {
        alertBox.className = `alert alert-${kind} alert-dismissible fade show`;
        alertBox.innerHTML = `${escapeHtml(message)}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>`;
        alertBox.classList.remove("d-none");
    }

    function visibleRows() {
        return [...table.querySelectorAll("tbody tr[data-id]")].filter(r => !r.classList.contains("d-none"));
    }

    function applyFilters() {
        const cat = categoryFilter.value;
        const text = textFilter.value.trim().toLowerCase();
        let shown = 0;
        table.querySelectorAll("tbody tr[data-id]").forEach(row => {
            const rowCat = (row.querySelector(".category-badge") || {}).textContent || "";
            const hay = row.textContent.toLowerCase();
            const ok = (!cat || rowCat === cat) && (!text || hay.includes(text));
            row.classList.toggle("d-none", !ok);
            if (ok) shown++;
        });
        rowCount.textContent = `Showing ${shown} of ${table.querySelectorAll("tbody tr[data-id]").length} complaint(s).`;
    }

    statusFilter.addEventListener("change", () => {
        const params = new URLSearchParams(window.location.search);
        if (statusFilter.value) params.set("status", statusFilter.value);
        else params.delete("status");
        window.location.search = params.toString();
    });
    categoryFilter.addEventListener("change", applyFilters);
    textFilter.addEventListener("input", applyFilters);
    applyFilters();

    async function applyTransition(row, button) {
        const id = row.dataset.id;
        const status = row.querySelector(".transition-target").value;
        button.disabled = true;
        try {
            const res = await fetch(`/api/complaints/${id}`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ status })
            });
            if (!res.ok) {
                const problem = await res.json().catch(() => ({}));
                show("danger", problem.detail || `Could not move complaint #${id}.`);
                return;
            }
            const updated = await res.json();
            const badge = row.querySelector(".status-badge");
            badge.textContent = updated.status;
            badge.className = "badge text-white status-badge status-" + updated.status;
            row.classList.remove("table-warning");
            show("success", `Complaint #${id} moved to ${updated.status}. Timeline updated.`);
        } catch (err) {
            show("danger", "Network error while updating status.");
            console.error(err);
        } finally {
            button.disabled = false;
        }
    }

    async function showTimeline(row) {
        const id = row.dataset.id;
        const title = document.querySelector("#timeline-title");
        const body = document.querySelector("#timeline-body");
        title.textContent = `Complaint #${id} timeline`;
        body.innerHTML = `<p class="text-muted">Loading…</p>`;
        bootstrap.Modal.getOrCreateInstance("#timeline-modal").show();
        try {
            const res = await fetch(`/api/complaints/${id}/history`);
            if (!res.ok) throw new Error("HTTP " + res.status);
            const entries = await res.json();
            if (!entries.length) {
                body.innerHTML = `<p class="text-muted">No transitions yet.</p>`;
                return;
            }
            body.innerHTML = `<ol class="timeline">` + entries.map(e => `
                <li>
                    <div class="fw-semibold">${e.oldStatus ? escapeHtml(e.oldStatus) + " → " : ""}${escapeHtml(e.newStatus)}</div>
                    <div class="small text-muted">${new Date(e.changedAt).toLocaleString()} · ${escapeHtml(e.changedBy)}</div>
                </li>`).join("") + `</ol>`;
        } catch (err) {
            body.innerHTML = `<div class="alert alert-danger">Could not load the timeline.</div>`;
            console.error(err);
        }
    }

    table.addEventListener("click", event => {
        const row = event.target.closest("tr[data-id]");
        if (!row) return;
        if (event.target.classList.contains("apply-transition")) {
            applyTransition(row, event.target);
        } else if (event.target.classList.contains("view-timeline")) {
            showTimeline(row);
        }
    });
})();

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[ch]));
}