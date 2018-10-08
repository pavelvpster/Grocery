$(function() {
    $(".nav-item").find(".active").removeClass("active");
    $(".nav-item").find("#purchase").parent().addClass("active");

    $.get("/purchase/" + getVisitId() + "/item_selector", {}, function(result) {
        $("#item-selector-container").html(result);
    });

    initializePagination("/purchase/" + getVisitId() + "/list", "#purchase-list-container");
});

function getVisitId() {
    return parseInt($("#visit-id").val(), 10);
}

function buyItem(itemId) {
    $.post("/api/v1/purchase/" + getVisitId() + "/buy/" + itemId + "?quantity=1")
        .done(function() {
            window.location = "/purchase/" + getVisitId();
        })
        .fail(function() {
            alert("Error buy Item!");
        });
}

function returnItem(itemId) {
    $.post("/api/v1/purchase/" + getVisitId() + "/return/" + itemId + "?quantity=1")
        .done(function() {
            window.location = "/purchase/" + getVisitId();
        })
        .fail(function() {
            alert("Error return Item!");
        });
}
