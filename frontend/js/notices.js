(async function () {
    const list = document.querySelector("#notices-list");
    const empty = document.querySelector("#notices-empty");
    if (!list) return;

    try {
        const res = await fetch("/api/notices");
        if (!res.ok) throw new Error("HTTP " + res.status);
        const notices = await res.json();
        if (!notices.length) {
            empty.classList.remove("hidden");
            return;
        }
        list.innerHTML = notices.map(n => `
            <article class="card">
                ${n.imageUrl ? `<img class="notice-image" src="${escapeHtml(n.imageUrl)}" alt="" loading="lazy">` : ""}
                <div class="notice-meta">
                    <h3>${escapeHtml(n.title)}</h3>
                    <span class="badge">${formatDate(n.eventDate)}</span>
                </div>
                <p class="muted small">Posted by ${escapeHtml(n.postedBy)}</p>
                <p style="white-space:pre-wrap;">${escapeHtml(n.description)}</p>
                ${n.linkUrl ? `<a class="btn secondary" href="${escapeHtml(n.linkUrl)}"
                    target="_blank" rel="noopener noreferrer">Open link</a>` : ""}
            </article>`).join("");
    } catch (err) {
        list.innerHTML = `<div class="alert danger">Could not load notices.</div>`;
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
