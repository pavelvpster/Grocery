$(function() {
    $(".nav-item").find(".active").removeClass("active");
    $(".nav-item").find("#shop").parent().addClass("active");

    initializePagination("/shop/list", "#shop-list-container");
});

function showCreateShopForm() {
    $.get("/shop/form", {}, function(result) {
        $("#shop-form-container").html(result);
        $("#shop-form").modal();
    });
}

function showUpdateShopForm(id) {
    $.get("/shop/" + id + "/form", {}, function(result) {
        $("#shop-form-container").html(result);
        $("#shop-form").modal();
    });
}

function validateShopForm() {
    var name = $("#shop-properties #name").val();
    if (name == null || name.length < 1 || name.length > 255) {
        $("#name-error").text("Name must be 1 to 255 characters.");
        return false;
    }
}

function deleteShop(id) {
    $.ajax({
        type: 'DELETE',
        url: "/shop/" + id,
        done: function(result) {
            window.location = "/shop/";
        }
    });
}

function createVisit(shopId) {
    $.post("/api/v1/visit/shop/" + shopId, {}, function(result) {
        window.location = "/visit/";
    });
}
