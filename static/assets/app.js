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
