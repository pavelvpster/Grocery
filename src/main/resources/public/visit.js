$(function() {
    $(".nav").find(".active").removeClass("active");
    $(".nav").find("#visit").parent().addClass("active");

    $.get("/visit/list", {}, function(result) {
        $("#visit-list-container").html(result);
        updatePaginationControls();
    });
});

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
    $.get("/visit/list?page=" + page, {}, function(result) {
        $("#visit-list-container").html(result);
        updatePaginationControls();
    });
}

function startVisit(id) {
    $.post("/api/v1/visit/" + id + "/start")
        .success(function() {
            window.location = "/visit/";
        })
        .fail(function() {
            alert("Error starting Visit!")
        });
}

function completeVisit(id) {
    $.post("/api/v1/visit/" + id + "/complete")
        .success(function() {
            window.location = "/visit/";
        })
        .fail(function() {
            alert("Error completing Visit!")
        });
}

function deleteVisit(id) {
    $.ajax({
        type: 'DELETE',
        url: "/api/v1/visit/" + id,
        success: function(result) {
            window.location = "/visit/";
        }
    });
}

function gotoPurchases(visitId) {
    window.location = "/purchase/" + visitId;
}