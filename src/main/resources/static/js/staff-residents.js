// Client-side filter for the resident directory; the list is small enough to
// render once and filter in the browser.
(function () {
    const input = document.querySelector("#resident-search");
    const table = document.querySelector("#residents-table");
    const count = document.querySelector("#resident-count");
    if (!input || !table) return;

    const rows = [...table.querySelectorAll("tbody tr")].filter(r => r.cells.length > 1);

    function apply() {
        const query = input.value.trim().toLowerCase();
        let shown = 0;
        rows.forEach(row => {
            const match = !query || row.textContent.toLowerCase().includes(query);
            row.classList.toggle("d-none", !match);
            if (match) shown++;
        });
        count.textContent = `Showing ${shown} of ${rows.length} resident(s).`;
    }

    input.addEventListener("input", apply);
    apply();
})();
