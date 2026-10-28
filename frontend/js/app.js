// Marks the nav link for the page you are on.
(function () {
    const path = (location.pathname.replace(/\/+$/, "") || "/");
    document.querySelectorAll(".nav a[href]:not(.brand)").forEach(link => {
        const href = link.getAttribute("href").replace(/\/+$/, "") || "/";
        const active = href === "/" ? path === "/" : path === href || path.startsWith(href + "/");
        if (active) {
            link.classList.add("active");
            link.setAttribute("aria-current", "page");
        }
    });
})();
