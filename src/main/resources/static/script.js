// simple table filter
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".table-filter").forEach(inp => {
    inp.addEventListener("input", () => {
      const table = document.getElementById(inp.getAttribute("data-table"));
      if (!table) return;
      const q = inp.value.toLowerCase();
      table.querySelectorAll("tbody tr").forEach(tr => {
        tr.style.display = tr.innerText.toLowerCase().includes(q) ? "" : "none";
      });
    });
  });
});
