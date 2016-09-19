$(function() {
    $(".nav").find(".active").removeClass("active");
    $(".nav").find("#purchase").parent().addClass("active");

    $.get("/purchase/" + getVisitId() + "/item_selector", {}, function(result) {
        $("#item-selector-container").html(result);
    });

    $.get("/purchase/" + getVisitId() + "/list", {}, function(result) {
        $("#purchase-list-container").html(result);
        updatePaginationControls();
    });
});

function getVisitId() {
    return parseInt($("#visit-id").val(), 10);
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
    $.get("/purchase/" + getVisitId() + "/list?page=" + page, {}, function(result) {
        $("#purchase-list-container").html(result);
        updatePaginationControls();
    });
}

function buyItem(itemId) {
    $.post("/api/v1/purchase/" + getVisitId() + "/buy/" + itemId + "?quantity=1")
        .success(function() {
            window.location = "/purchase/" + getVisitId();
        })
        .fail(function() {
            alert("Error buy Item!");
        });
}

function returnItem(itemId) {
    $.post("/api/v1/purchase/" + getVisitId() + "/return/" + itemId + "?quantity=1")
        .success(function() {
            window.location = "/purchase/" + getVisitId();
        })
        .fail(function() {
            alert("Error return Item!");
        });
}
