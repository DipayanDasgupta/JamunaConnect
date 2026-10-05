// Module B: room lookup. Same /api/rooms endpoint the map uses.
// Debounced, abortable, and capped so a 350-room directory stays snappy.
(function () {
    const form = document.querySelector("#lookup-form");
    const input = document.querySelector("#query");
    const results = document.querySelector("#results");
    const MAX_CARDS = 24;
    let timer = null;
    let inflight = null;

    async function search(query) {
        if (inflight) inflight.abort();
        inflight = new AbortController();
        results.innerHTML = `<div class="text-muted">Searching…</div>`;
        try {
            const url = query ? `/api/rooms?query=${encodeURIComponent(query)}` : "/api/rooms";
            const res = await fetch(url, { signal: inflight.signal });
            if (!res.ok) throw new Error("HTTP " + res.status);
            render(await res.json(), query);
        } catch (err) {
            if (err.name === "AbortError") return;
            results.innerHTML = `<div class="alert alert-danger">Lookup failed. Try again.</div>`;
            console.error(err);
        }
    }

    function residentChips(residents) {
        if (!residents || !residents.length) {
            return `<p class="text-muted mb-0">No residents on record</p>`;
        }
        return `<div class="d-flex flex-wrap gap-1 mt-2">` + residents.map(r =>
            `<span class="badge rounded-pill text-bg-success" title="${escapeHtml(r.rollNumber)}">${escapeHtml(r.name)} · ${escapeHtml(r.rollNumber)}</span>`
        ).join("") + `</div>`;
    }

    function render(rows, query) {
        if (!rows.length) {
            results.innerHTML = `<div class="alert alert-warning">No matching room or resident${query ? ` for “${escapeHtml(query)}”` : ""}.</div>`;
            return;
        }
        const shown = rows.slice(0, MAX_CARDS);
        const extra = rows.length - shown.length;
        results.innerHTML =
            `<p class="text-muted">${rows.length} result${rows.length === 1 ? "" : "s"}${extra > 0 ? ` — showing ${shown.length}, refine your search for more` : ""}</p>` +
            `<div class="row g-3">` + shown.map(r => `
            <div class="col-md-4">
                <div class="card h-100 shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title mb-1">Room ${escapeHtml(r.roomNumber)}</h5>
                        <h6 class="card-subtitle mb-2 text-muted">Block ${escapeHtml(r.block)}, floor ${r.floor}</h6>
                        ${residentChips(r.residents)}
                    </div>
                </div>
            </div>`).join("") + `</div>`;
    }

    function schedule() {
        clearTimeout(timer);
        timer = setTimeout(() => {
            const q = input.value.trim();
            if (q && q.length < 2) {
                results.innerHTML = `<p class="text-muted">Type at least 2 characters…</p>`;
                return;
            }
            search(q);
        }, 250);
    }

    form.addEventListener("submit", event => {
        event.preventDefault();
        clearTimeout(timer);
        search(input.value.trim());
    });
    input.addEventListener("input", schedule);
})();

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[ch]));
}