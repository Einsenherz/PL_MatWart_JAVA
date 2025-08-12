// === Tabellen-Sortierung & Filter ===
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("th").forEach(header => {
        header.style.cursor = "pointer"; // Klick-Feedback
        header.addEventListener("click", () => {
            const table = header.closest("table");
            const tbody = table.querySelector("tbody");
            const index = Array.from(header.parentNode.children).indexOf(header);
            const rows = Array.from(tbody.querySelectorAll("tr"));
            const asc = !header.classList.contains("asc");

            rows.sort((a, b) => {
                const cellA = a.children[index].innerText.trim().toLowerCase();
                const cellB = b.children[index].innerText.trim().toLowerCase();
                return cellA.localeCompare(cellB, undefined, { numeric: true }) * (asc ? 1 : -1);
            });

            tbody.innerHTML = "";
            rows.forEach(row => tbody.appendChild(row));

            // Sortierzustände zurücksetzen und aktuelle Spalte markieren
            table.querySelectorAll("th").forEach(th => th.classList.remove("asc", "desc"));
            header.classList.toggle("asc", asc);
            header.classList.toggle("desc", !asc);

            // Pfeil-Symbol hinzufügen
            table.querySelectorAll("th span.sort-indicator").forEach(el => el.remove());
            const arrow = document.createElement("span");
            arrow.classList.add("sort-indicator");
            arrow.textContent = asc ? " ▲" : " ▼";
            header.appendChild(arrow);
        });
    });

    // === Tabellen-Filter ===
    document.querySelectorAll(".table-filter").forEach(input => {
        input.addEventListener("input", () => {
            const tableId = input.getAttribute("data-table");
            const filter = input.value.trim().toLowerCase();
            const rows = document.querySelectorAll(`#${tableId} tbody tr`);
            rows.forEach(row => {
                row.style.display = row.innerText.toLowerCase().includes(filter) ? "" : "none";
            });
        });
    });
});
