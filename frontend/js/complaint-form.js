(function () {
    const form = document.querySelector("#complaint-form");
    const alertBox = document.querySelector("#alert");
    const clientToken = (crypto.randomUUID ? crypto.randomUUID()
        : String(Date.now()) + Math.random().toString(16).slice(2));

    function show(kind, message) {
        alertBox.className = `alert ${kind}`;
        alertBox.textContent = message;
        alertBox.classList.remove("hidden");
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();
        const payload = {
            submitterName: form.submitterName.value.trim(),
            roomNumber: form.roomNumber.value.trim(),
            category: form.category.value,
            description: form.description.value.trim(),
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
            show("success", `Complaint #${saved.id} filed. The hostel office has been notified.`);
            form.reset();
        } catch (err) {
            show("danger", "Network error. Please try again.");
            console.error(err);
        }
    });
})();