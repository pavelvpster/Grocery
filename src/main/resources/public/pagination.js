function gotoDefaultPage() {
    var listUrl = $("#list-url").val();
    $.get(listUrl, {}, function(result) {
        $("#list-container").html(result);
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
    var listUrl = $("#list-url").val();
    $.get(listUrl + "?page=" + page, {}, function(result) {
        $("#list-container").html(result);
        updatePaginationControls();
    });
}
