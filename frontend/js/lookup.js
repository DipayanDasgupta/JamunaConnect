(function () {
    const form = document.querySelector("#lookup-form");
    const input = document.querySelector("#query");
    const results = document.querySelector("#results");

    async function search(query) {
        results.innerHTML = `<p class="muted">Searching…</p>`;
        try {
            const url = query ? `/api/rooms?query=${encodeURIComponent(query)}` : "/api/rooms";
            const res = await fetch(url);
            if (!res.ok) throw new Error("HTTP " + res.status);
            render(await res.json());
        } catch (err) {
            results.innerHTML = `<div class="alert danger">Lookup failed. Try again.</div>`;
            console.error(err);
        }
    }

    function render(rows) {
        if (!rows.length) {
            results.innerHTML = `<div class="alert warning">No matching room or resident.</div>`;
            return;
        }
        results.innerHTML = `<div class="cards">` + rows.map(r => `
            <div class="card">
                <h3>Room ${escapeHtml(r.roomNumber)}</h3>
                <p class="muted">Block ${escapeHtml(r.block)}, floor ${r.floor}</p>
                <p>${escapeHtml(r.occupants || "No residents on record")}</p>
            </div>`).join("") + `</div>`;
    }

    form.addEventListener("submit", event => {
        event.preventDefault();
        search(input.value.trim());
    });

    search("");
})();

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[ch]));
}