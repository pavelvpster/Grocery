$(function() {
    $(".nav").find(".active").removeClass("active");
    $(".nav").find("#visit").parent().addClass("active");

    initializePagination("/visit/list", "#visit-list-container");
});

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
