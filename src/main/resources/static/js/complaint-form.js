// Module A: complaint form. One client token per page load makes a
// double-click/submit idempotent; server validation errors are shown in place.
(function () {
    const form = document.querySelector("#complaint-form");
    const alertBox = document.querySelector("#alert");
    const categorySelect = document.querySelector("#category");
    const otherWrap = document.querySelector("#otherCategoryWrap");
    const otherInput = document.querySelector("#otherCategory");
    const clientToken = (crypto.randomUUID ? crypto.randomUUID()
        : String(Date.now()) + Math.random().toString(16).slice(2));

    function toggleOther() {
        const isOther = categorySelect.value === "OTHER";
        otherWrap.classList.toggle("d-none", !isOther);
        otherInput.required = isOther;
        if (!isOther) {
            otherInput.value = "";
        }
    }
    categorySelect.addEventListener("change", toggleOther);
    toggleOther();

    function show(kind, message) {
        alertBox.className = `alert alert-${kind}`;
        alertBox.textContent = message;
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();
        const description = form.description.value.trim();
        const other = otherInput.value.trim();
        const isOther = categorySelect.value === "OTHER";
        if (isOther && !other) {
            show("danger", "Please specify the exact category for your complaint.");
            otherInput.focus();
            return;
        }
        const payload = {
            submitterName: form.submitterName.value.trim(),
            roomNumber: form.roomNumber.value.trim(),
            category: categorySelect.value,
            description: isOther ? `[${other}] ${description}` : description,
            clientToken
        };
        try {
            const res = await fetch("/api/complaints", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            if (res.status === 429) {
                show("warning", "Too many submissions from this address. Please wait a minute.");
                return;
            }
            if (!res.ok) {
                const problem = await res.json().catch(() => ({}));
                show("danger", problem.detail || "Could not submit. Check the fields and try again.");
                return;
            }
            const saved = await res.json();
            show("success", `Complaint #${saved.id} filed. The hostel office has been notified; ` +
                `you can check its status from the dashboard.`);
            form.reset();
            toggleOther();
        } catch (err) {
            show("danger", "Network error. Please try again.");
            console.error(err);
        }
    });
})();