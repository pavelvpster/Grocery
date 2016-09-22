var listUrl = "";
var listContainer = "";

function initializePagination(_listUrl, _listContainer) {
    listUrl = _listUrl;
    listContainer = _listContainer;
    gotoDefaultPage();
}

function gotoDefaultPage() {
    $.get(listUrl, {}, function(result) {
        $(listContainer).html(result);
        updatePaginationControls();
    });
}

function getTotalPages() {
    return parseInt($("#total-pages").val(), 10);
}

function getCurrentPage() {
    return parseInt($("#current-page").val(), 10);
}

function updatePaginationControls() {
    var currentPage = getCurrentPage();
    if (currentPage > 1) {
        $("#previous-page").removeClass("disabled");
    } else {
        $("#previous-page").addClass("disabled");
    }
    if (currentPage < getTotalPages()) {
        $("#next-page").removeClass("disabled");
    } else {
        $("#next-page").addClass("disabled");
    }
}

function gotoPreviousPage() {
    gotoPage(getCurrentPage() - 1);
}

function gotoNextPage() {
    gotoPage(getCurrentPage() + 1);
}

function gotoPage(page) {
    if (page < 1 || page > getTotalPages()) {
        return;
    }
    $.get(listUrl + "?page=" + page, {}, function(result) {
        $(listContainer).html(result);
        updatePaginationControls();
    });
}
