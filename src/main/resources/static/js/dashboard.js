// Module C: apply a status transition, then view the appended audit timeline.
(function () {
    const alertBox = document.querySelector("#alert");
    const table = document.querySelector("#complaints-table");
    if (!table) return;

    function show(kind, message) {
        alertBox.className = `alert alert-${kind}`;
        alertBox.textContent = message;
        alertBox.classList.remove("d-none");
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    async function applyTransition(row) {
        const id = row.dataset.id;
        const status = row.querySelector(".transition-target").value;
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
            badge.className = "badge text-bg-secondary status-badge status-" + updated.status;
            show("success", `Complaint #${id} moved to ${updated.status}.`);
        } catch (err) {
            show("danger", "Network error while updating status.");
            console.error(err);
        }
    }

    async function showTimeline(row) {
        const id = row.dataset.id;
        try {
            const res = await fetch(`/api/complaints/${id}/history`);
            const entries = await res.json();
            const lines = entries.map(e =>
                `${new Date(e.changedAt).toLocaleString()} - ` +
                `${e.oldStatus ? e.oldStatus + " → " : ""}${e.newStatus} (${e.changedBy})`);
            alert(`Complaint #${id} timeline:\n\n` + lines.join("\n"));
        } catch (err) {
            show("danger", "Could not load the timeline.");
            console.error(err);
        }
    }

    table.addEventListener("click", event => {
        const row = event.target.closest("tr[data-id]");
        if (!row) return;
        if (event.target.classList.contains("apply-transition")) {
            applyTransition(row);
        } else if (event.target.classList.contains("view-timeline")) {
            showTimeline(row);
        }
    });
})();