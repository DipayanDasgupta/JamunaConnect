// Jamuna hostel map: an isometric floor plan generated from the MAP sheet in
// Phase1_jam_26-27.xlsx. Room positions follow that sheet; resident data comes
// from the same /api/rooms endpoint as the lookup page.
(function () {
    const SCALE = 24;
    const THICKNESS = 18;
    const COLORS = {
        1: "#0f6b4f",
        2: "#2563eb",
        3: "#7c3aed",
        4: "#b45309"
    };
    const FACILITY_ROOMS = new Set(["101", "102", "117", "118", "332", "1009", "1010"]);

    const asc = (from, to) => Array.from({length: to - from + 1}, (_, i) => String(from + i));
    const desc = (from, to) => Array.from({length: from - to + 1}, (_, i) => String(from - i));

    const FLOORS = [
        {
            id: 1, name: "Ground floor", block: "A", color: COLORS[1],
            sections: {
                topLeft: desc(164, 149),
                topRight: desc(148, 133),
                bottomLeft: asc(101, 116),
                bottomRight: asc(117, 132),
                extLeft: [],
                extRightTop: asc(1001, 1008),
                extRightBottom: desc(1016, 1009)
            }
        },
        {
            id: 2, name: "First floor", block: "B", color: COLORS[2],
            sections: {
                topLeft: desc(264, 249),
                topRight: desc(248, 233),
                bottomLeft: asc(201, 216),
                bottomRight: asc(217, 232),
                extLeft: asc(265, 274),
                extRightTop: asc(2001, 2008),
                extRightBottom: desc(2016, 2009)
            }
        },
        {
            id: 3, name: "Second floor", block: "C", color: COLORS[3],
            sections: {
                topLeft: desc(364, 349),
                topRight: desc(348, 333),
                bottomLeft: asc(301, 316),
                bottomRight: asc(317, 332),
                extLeft: asc(365, 374),
                extRightTop: asc(3001, 3008),
                extRightBottom: desc(3016, 3009)
            }
        },
        {
            id: 4, name: "Third floor", block: "D", color: COLORS[4],
            sections: {
                topLeft: desc(464, 449),
                topRight: desc(448, 433),
                bottomLeft: asc(401, 416),
                bottomRight: asc(417, 432),
                extLeft: asc(465, 474),
                extRightTop: asc(4001, 4008),
                extRightBottom: desc(4016, 4009)
            }
        }
    ];

    const SECTION_SPECS = {
        topLeft: {x: 0, y: 0, direction: "x", label: "north wing"},
        topRight: {x: 18, y: 0, direction: "x", label: "north wing"},
        bottomLeft: {x: 0, y: 6, direction: "x", label: "south wing"},
        bottomRight: {x: 18, y: 6, direction: "x", label: "south wing"},
        extLeft: {x: -2, y: 1, direction: "y", label: "extension wing"},
        extRightTop: {x: 36, y: 0, direction: "x", label: "extension wing"},
        extRightBottom: {x: 36, y: 3, direction: "x", label: "extension wing"}
    };

    const mapEl = document.querySelector("#hostel-map");
    const panelEl = document.querySelector("#room-panel");
    const searchEl = document.querySelector("#map-room-search");
    const captionEl = document.querySelector("#map-floor-caption");
    if (!mapEl || !panelEl) return;

    const floorByRoom = new Map();
    const roomMeta = new Map();
    FLOORS.forEach(floor => {
        Object.entries(floor.sections).forEach(([section, rooms]) => {
            const spec = SECTION_SPECS[section];
            rooms.forEach(room => {
                floorByRoom.set(room, floor.id);
                roomMeta.set(room, {
                    floor: floor.id,
                    block: section.startsWith("extRight") ? "EXT" : floor.block,
                    section: spec.label
                });
            });
        });
    });

    let currentFloor = 1;
    let selectedRoom = null;
    let rooms = new Map();

    function project(x, y) {
        return {x: (x - y) * SCALE * 0.8660254, y: (x + y) * SCALE * 0.5};
    }

    function sectionCorners(spec, count) {
        const width = spec.direction === "x" ? count : 1;
        const height = spec.direction === "y" ? count : 1;
        return [
            project(spec.x, spec.y),
            project(spec.x + width, spec.y),
            project(spec.x + width, spec.y + height),
            project(spec.x, spec.y + height)
        ];
    }

    function slabMarkup(spec, count, color) {
        const corners = sectionCorners(spec, count);
        const down = {x: 0, y: THICKNESS};
        const sides = [];
        for (let i = 0; i < 4; i++) {
            const a = corners[i];
            const b = corners[(i + 1) % 4];
            sides.push(`<polygon points="${a.x},${a.y} ${b.x},${b.y} ${b.x + down.x},${b.y + down.y} ${a.x + down.x},${a.y + down.y}" fill="${color.side}"/>`);
        }
        const top = corners.map(p => `${p.x},${p.y}`).join(" ");
        return `<g class="map-slab">${sides.join("")}<polygon points="${top}" fill="${color.top}" stroke="${color.edge}" stroke-width="1"/></g>`;
    }

    function tilePoints(cx, cy, halfWidth, halfHeight) {
        return `${cx},${cy - halfHeight} ${cx + halfWidth},${cy} ${cx},${cy + halfHeight} ${cx - halfWidth},${cy}`;
    }

    function roomPosition(spec, index) {
        return spec.direction === "x"
            ? {x: spec.x + index, y: spec.y}
            : {x: spec.x, y: spec.y + index};
    }

    function roomMarkup(room, spec, index, floor) {
        const position = roomPosition(spec, index);
        const center = project(position.x + 0.5, position.y + 0.5);
        const record = rooms.get(room);
        const occupied = Boolean(record && record.residents && record.residents.length);
        const facility = FACILITY_ROOMS.has(room);
        const fill = facility ? "#d97706" : occupied ? floor.color : "#dbe4ea";
        const textFill = facility || occupied ? "#ffffff" : "#334155";
        const residentText = occupied
            ? `${record.residents.length} resident${record.residents.length === 1 ? "" : "s"}`
            : facility ? "facility room" : "no resident on record";
        const selected = selectedRoom === room ? " is-selected" : "";
        return `<g class="hostel-room${selected}" data-room="${room}" tabindex="0" role="button"
                    aria-label="Room ${room}, ${residentText}">
                    <title>Room ${room} — ${residentText}</title>
                    <polygon points="${tilePoints(center.x, center.y, SCALE * 0.78, SCALE * 0.43)}"
                             class="room-shape" fill="${fill}"/>
                    <text x="${center.x}" y="${center.y + 2.6}" class="room-label"
                          fill="${textFill}" text-anchor="middle">${room}</text>
                </g>`;
    }

    function renderFloor(floorId) {
        const floor = FLOORS.find(item => item.id === floorId) || FLOORS[0];
        const markup = [];
        const bounds = [];

        Object.entries(floor.sections).forEach(([section, roomNumbers]) => {
            if (!roomNumbers.length) return;
            const spec = SECTION_SPECS[section];
            const extension = section.startsWith("ext");
            const slabColor = extension
                ? {top: "#eef2f7", side: "#cbd5e1", edge: "#94a3b8"}
                : {top: "#e9f4ef", side: "#b8d8c8", edge: "#8fbfa9"};
            markup.push(slabMarkup(spec, roomNumbers.length, slabColor));
            sectionCorners(spec, roomNumbers.length).forEach(point => bounds.push(point));
        });

        Object.entries(floor.sections).forEach(([section, roomNumbers]) => {
            const spec = SECTION_SPECS[section];
            roomNumbers.forEach((room, index) => {
                const position = roomPosition(spec, index);
                const center = project(position.x + 0.5, position.y + 0.5);
                bounds.push({x: center.x - SCALE, y: center.y - SCALE * 0.6});
                bounds.push({x: center.x + SCALE, y: center.y + SCALE * 0.6});
                markup.push(roomMarkup(room, spec, index, floor));
            });
        });

        const stairs = project(16.5, 0.5);
        const gateY = floor.id === 1 ? 6.5 : 6.5;
        const gate = project(16.5, gateY);
        markup.push(`<text x="${stairs.x}" y="${stairs.y + 3}" class="map-marker" text-anchor="middle">STEPS</text>`);
        markup.push(`<text x="${gate.x}" y="${gate.y + 3}" class="map-marker" text-anchor="middle">${floor.id === 1 ? "MAIN GATE" : "STEPS"}</text>`);

        const extRight = project(40, 2.5);
        if (floor.sections.extRightTop.length) {
            markup.push(`<text x="${extRight.x}" y="${extRight.y}" class="map-section-label" text-anchor="middle">EXTENSION</text>`);
        }
        if (floor.sections.extLeft.length) {
            const extLeft = project(-1.2, 5.5);
            markup.push(`<text x="${extLeft.x}" y="${extLeft.y}" class="map-section-label" text-anchor="middle">EXT</text>`);
        }

        const minX = Math.min(...bounds.map(point => point.x)) - 34;
        const maxX = Math.max(...bounds.map(point => point.x)) + 34;
        const minY = Math.min(...bounds.map(point => point.y)) - 30;
        const maxY = Math.max(...bounds.map(point => point.y)) + 42;
        const width = maxX - minX;
        const height = maxY - minY;
        mapEl.innerHTML = `<svg viewBox="${minX} ${minY} ${width} ${height}" role="img"
              aria-label="Isometric floor plan of Jamuna Hostel, ${floor.name}">
            <defs>
                <filter id="map-shadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="10" stdDeviation="10" flood-color="#0f172a" flood-opacity="0.12"/>
                </filter>
            </defs>
            <g filter="url(#map-shadow)">${markup.filter(item => item.startsWith('<g class="map-slab"')).join("")}</g>
            <g>${markup.filter(item => item.startsWith('<text')).join("")}</g>
            ${markup.filter(item => item.startsWith('<g class="hostel-room')).join("")}
        </svg>`;

        if (captionEl) {
            const count = Object.values(floor.sections).reduce((total, list) => total + list.length, 0);
            captionEl.textContent = `${floor.name} · Block ${floor.block} + extension · ${count} rooms`;
        }
        updateFloorTabs();
        highlightSelection();
    }

    function updateFloorTabs() {
        document.querySelectorAll("[data-floor]").forEach(tab => {
            const active = Number(tab.dataset.floor) === currentFloor;
            tab.classList.toggle("is-active", active);
            tab.setAttribute("aria-selected", active ? "true" : "false");
        });
    }

    function highlightSelection() {
        mapEl.querySelectorAll(".hostel-room.is-selected").forEach(el => el.classList.remove("is-selected"));
        if (!selectedRoom) return;
        const group = mapEl.querySelector(`.hostel-room[data-room="${selectedRoom}"]`);
        if (group) group.classList.add("is-selected");
    }

    function residentList(record) {
        if (!record || !record.residents || !record.residents.length) {
            if (FACILITY_ROOMS.has(selectedRoom)) {
                return `<p class="map-empty-note">Facility / office room. No resident record is published for this room.</p>`;
            }
            return `<p class="map-empty-note">No residents are on record for this room yet.</p>`;
        }
        return `<div class="resident-list">${record.residents.map(resident => `
            <div class="resident-row">
                <span class="resident-avatar">${escapeHtml(resident.name).trim().charAt(0) || "?"}</span>
                <div>
                    <div class="resident-name">${escapeHtml(resident.name)}</div>
                    <div class="resident-roll">${escapeHtml(resident.rollNumber)}</div>
                </div>
            </div>`).join("")}</div>`;
    }

    function showRoom(room) {
        selectedRoom = room;
        const meta = roomMeta.get(room);
        if (meta && meta.floor !== currentFloor) {
            currentFloor = meta.floor;
            renderFloor(currentFloor);
        } else {
            highlightSelection();
        }
        const record = rooms.get(room);
        panelEl.innerHTML = `
            <div class="room-panel-top">
                <span class="room-number">Room ${escapeHtml(room)}</span>
                <button type="button" class="room-panel-close" aria-label="Clear selected room">×</button>
            </div>
            <p class="room-meta">${meta ? `Block ${escapeHtml(meta.block)} · ${escapeHtml(meta.section)} · ${FLOORS[meta.floor - 1].name}` : "Jamuna Hostel"}</p>
            <h3>Residents</h3>
            ${residentList(record)}
            <div class="room-panel-actions">
                <a class="btn btn-sm btn-primary" href="/lookup">Search another room</a>
                <a class="btn btn-sm btn-outline-secondary room-action-secondary" href="/complaints/new?room=${encodeURIComponent(room)}">File a complaint</a>
            </div>`;
        panelEl.querySelector(".room-panel-close").addEventListener("click", clearSelection);
        if (!record) loadRoom(room);
    }

    function clearSelection() {
        selectedRoom = null;
        panelEl.innerHTML = emptyPanel();
        highlightSelection();
    }

    function emptyPanel() {
        return `<div class="room-panel-empty">
            <div class="room-panel-symbol" aria-hidden="true">⌂</div>
            <h2>Pick a room</h2>
            <p>Choose a floor, then click any room tile to see the residents on record.</p>
            <p class="room-panel-hint">Tip: use the room search above the map to jump straight to a number.</p>
        </div>`;
    }

    async function loadRooms() {
        try {
            const res = await fetch("/api/rooms", {headers: {Accept: "application/json"}});
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const rows = await res.json();
            rows.forEach(row => rooms.set(row.roomNumber, row));
        } catch (error) {
            console.warn("Room map data unavailable", error);
        }
        renderFloor(currentFloor);
        if (selectedRoom) showRoom(selectedRoom);
    }

    async function loadRoom(room) {
        try {
            const res = await fetch(`/api/rooms?query=${encodeURIComponent(room)}`);
            if (!res.ok) return;
            const rows = await res.json();
            const match = rows.find(row => row.roomNumber === room);
            if (match) {
                rooms.set(match.roomNumber, match);
                if (selectedRoom === room) showRoom(room);
            }
        } catch (error) {
            console.warn(`Room ${room} load failed`, error);
        }
    }

    mapEl.addEventListener("click", event => {
        const room = event.target.closest(".hostel-room");
        if (room) showRoom(room.dataset.room);
    });
    mapEl.addEventListener("keydown", event => {
        if (event.key !== "Enter" && event.key !== " ") return;
        const room = event.target.closest(".hostel-room");
        if (!room) return;
        event.preventDefault();
        showRoom(room.dataset.room);
    });
    document.querySelectorAll("[data-floor]").forEach(tab => {
        tab.addEventListener("click", () => {
            currentFloor = Number(tab.dataset.floor);
            selectedRoom = null;
            panelEl.innerHTML = emptyPanel();
            renderFloor(currentFloor);
        });
    });
    if (searchEl) {
        searchEl.addEventListener("keydown", event => {
            if (event.key !== "Enter") return;
            const room = searchEl.value.trim();
            if (floorByRoom.has(room)) {
                showRoom(room);
                searchEl.value = "";
            } else {
                searchEl.setAttribute("aria-invalid", "true");
            }
        });
        searchEl.addEventListener("input", () => searchEl.removeAttribute("aria-invalid"));
    }

    panelEl.innerHTML = emptyPanel();
    renderFloor(currentFloor);
    loadRooms();

    const requestedRoom = new URLSearchParams(location.search).get("room");
    if (requestedRoom && floorByRoom.has(requestedRoom)) {
        showRoom(requestedRoom);
    }

    function escapeHtml(value) {
        return String(value ?? "").replace(/[&<>"']/g, character => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
        }[character]));
    }
})();
