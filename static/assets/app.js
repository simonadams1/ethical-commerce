$(".app-accordion").accordion();


Array.from(document.querySelectorAll(".app--select-from-remote")).forEach((el) => {
    const url = el.getAttribute("data-search-url");

    $(el).autocomplete({
        source: url,
        select: (event, ui) => {
            el.nextElementSibling.value = ui.item.id;
        },
    });
})

Array.from(document.querySelectorAll(".app--autocomplete-from-remote")).forEach((el) => {
    const url = el.getAttribute("data-search-url");

    $(el).autocomplete({
        source: url,
    });
})

document.addEventListener("click", (event) => {
    if (
        event.target != null &&
        event.target.tagName === "A" &&
        event.target.getAttribute("data-stop-message") != null
    ) {
        event.preventDefault();
        window.alert(event.target.getAttribute("data-stop-message"))
    }
});

for (const el of Array.from(document.querySelectorAll(".js-temp-message"))) {
    const timeout = el.getAttribute("data-timeout");

    if (timeout != null) {
        setTimeout(() => {
            el.remove();
        }, parseInt(timeout, 10));
    }
}

/*
    Bulk checkboxes for tables
*/
document.addEventListener("change", (event) => {
    const el = event.target;
    const value = el.checked;

    if (el.tagName === 'INPUT' && el.getAttribute("type") === 'checkbox' && el.parentElement.tagName === 'TH') {
        const th = el.parentElement;
        const trHead = th.parentElement;
        const table = trHead.parentElement.parentElement;

         if (!table.classList.contains('js-table-bulk-checkboxes')) {
             return
         }

        const columnIndex = Array.from(trHead.children).indexOf(th)
        const tds = table.querySelectorAll(`tbody > tr > td:nth-child(${columnIndex + 1})`)

        for (const td of Array.from(tds)) {
            for (element of Array.from(td.querySelectorAll('input[type="checkbox"]'))) {
                element.checked = value;
            }
        }
    }
});
