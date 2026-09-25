// Module B: room lookup. Same /api/rooms endpoint the map uses.
(function () {
    const form = document.querySelector("#lookup-form");
    const input = document.querySelector("#query");
    const results = document.querySelector("#results");

    async function search(query) {
        results.innerHTML = `<div class="text-muted">Searching…</div>`;
        try {
            const url = query ? `/api/rooms?query=${encodeURIComponent(query)}` : "/api/rooms";
            const res = await fetch(url);
            if (!res.ok) throw new Error("HTTP " + res.status);
            render(await res.json());
        } catch (err) {
            results.innerHTML = `<div class="alert alert-danger">Lookup failed. Try again.</div>`;
            console.error(err);
        }
    }

    function render(rows) {
        if (!rows.length) {
            results.innerHTML = `<div class="alert alert-warning">No matching room or resident.</div>`;
            return;
        }
        results.innerHTML = `<div class="row g-3">` + rows.map(r => `
            <div class="col-md-4">
                <div class="card h-100 shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title mb-1">Room ${escapeHtml(r.roomNumber)}</h5>
                        <h6 class="card-subtitle mb-2 text-muted">Block ${escapeHtml(r.block)}, floor ${r.floor}</h6>
                        <p class="card-text">${escapeHtml(r.occupants || "No residents on record")}</p>
                    </div>
                </div>
            </div>`).join("") + `</div>`;
    }

    form.addEventListener("submit", event => {
        event.preventDefault();
        search(input.value.trim());
    });

    window.__roomSearch = search;
    search(input.value.trim());
})();

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[ch]));
}