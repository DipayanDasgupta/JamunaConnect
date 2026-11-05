// Public notice board: read-only API, newest/nearest event first.
(async function () {
    const list = document.querySelector("#notices-list");
    const empty = document.querySelector("#notices-empty");
    if (!list) return;

    try {
        const res = await fetch("/api/notices");
        if (!res.ok) throw new Error("HTTP " + res.status);
        const notices = await res.json();
        if (!notices.length) {
            empty.classList.remove("d-none");
            return;
        }
        list.innerHTML = notices.map(n => `
            <div class="col-md-6 col-xl-4">
                <article class="card h-100 shadow-sm">
                    ${n.imageUrl ? `<img src="${escapeHtml(n.imageUrl)}" class="card-img-top"
                        alt="" style="height:170px;object-fit:cover;" loading="lazy">` : ""}
                    <div class="card-body d-flex flex-column">
                        <div class="d-flex justify-content-between align-items-start gap-2">
                            <h2 class="h5 mb-1">${escapeHtml(n.title)}</h2>
                            <span class="badge text-bg-light border">${formatDate(n.eventDate)}</span>
                        </div>
                        <p class="text-muted small mb-2">Posted by ${escapeHtml(n.postedBy)}</p>
                        <p class="mb-3" style="white-space:pre-wrap;">${escapeHtml(n.description)}</p>
                        ${n.linkUrl ? `<a class="btn btn-sm btn-primary mt-auto align-self-start"
                            href="${escapeHtml(n.linkUrl)}" target="_blank" rel="noopener noreferrer">Open link</a>` : ""}
                    </div>
                </article>
            </div>`).join("");
    } catch (err) {
        list.innerHTML = `<div class="col-12"><div class="alert alert-danger">Could not load notices.</div></div>`;
        console.error(err);
    }
})();

function formatDate(value) {
    if (!value) return "";
    const date = new Date(value + "T00:00:00");
    return isNaN(date) ? value : date.toLocaleDateString("en-IN", {dateStyle: "medium"});
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, ch => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[ch]));
}
