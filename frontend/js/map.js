(function () {
    const map = L.map("map").setView([12.9916, 80.2337], 17);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19,
        attribution: "&copy; OpenStreetMap contributors"
    }).addTo(map);

    async function lookupRoom(roomNumber) {
        const res = await fetch(`/api/rooms?query=${encodeURIComponent(roomNumber)}`);
        if (!res.ok) return `<div class="alert danger">Lookup failed.</div>`;
        const rows = await res.json();
        const match = rows.find(r => r.roomNumber === roomNumber) || rows[0];
        if (!match) return `<div>No resident on record for ${roomNumber}.</div>`;
        const who = (match.residents && match.residents.length)
            ? match.residents.map(r => `${r.name} (${r.rollNumber})`).join(", ")
            : "No residents on record";
        return `<strong>Room ${match.roomNumber}</strong><br/>Block ${match.block}, floor ${match.floor}<br/>${who}`;
    }

    fetch("/geo/blocks.geojson")
        .then(res => res.json())
        .then(geo => {
            L.geoJSON(geo, {
                style: feature => ({
                    color: "#1f6f4a", weight: 2, fillOpacity: 0.25,
                    fillColor: feature.properties.block === "A" ? "#1f6f4a"
                        : feature.properties.block === "B" ? "#0d6efd" : "#fd7e14"
                }),
                onEachFeature: (feature, layer) => {
                    const room = feature.properties.room;
                    layer.bindPopup(`<em>Loading room ${room}…</em>`);
                    layer.on("popupopen", async e => {
                        e.popup.setContent(await lookupRoom(room));
                    });
                    layer.bindTooltip(`Room ${room}`);
                }
            }).addTo(map);
        })
        .catch(err => console.error("Could not load GeoJSON", err));
})();