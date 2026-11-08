(function () {
    const form = document.querySelector("#complaint-form");
    const alertBox = document.querySelector("#alert");
    const categorySelect = document.querySelector("#category");
    const otherWrap = document.querySelector("#otherCategoryWrap");
    const otherInput = document.querySelector("#otherCategory");

    function toggleOther() {
        const isOther = categorySelect.value === "OTHER";
        otherWrap.classList.toggle("hidden", !isOther);
        otherInput.required = isOther;
        if (!isOther) {
            otherInput.value = "";
        }
    }
    categorySelect.addEventListener("change", toggleOther);
    const requestedCategory = new URLSearchParams(location.search).get("category");
    if (requestedCategory && [...categorySelect.options].some(o => o.value === requestedCategory)) {
        categorySelect.value = requestedCategory;
    }
    const requestedRoom = new URLSearchParams(location.search).get("room");
    if (requestedRoom) {
        form.roomNumber.value = requestedRoom;
    }
    toggleOther();
    let clientToken = newClientToken();
    function newClientToken() {
        return crypto.randomUUID ? crypto.randomUUID()
            : String(Date.now()) + Math.random().toString(16).slice(2);
    }

    function show(kind, message) {
        alertBox.className = `alert ${kind}`;
        alertBox.textContent = message;
        alertBox.classList.remove("hidden");
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
            residentEmail: form.residentEmail.value.trim() || null,
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
            show("success", `Complaint #${saved.id} filed. Save this tracking code: ` +
                `${saved.trackingToken} — use it with the complaint number on the Track page.`);
            form.reset();
            toggleOther();
            clientToken = newClientToken();
        } catch (err) {
            show("danger", "Network error. Please try again.");
            console.error(err);
        }
    });
})();