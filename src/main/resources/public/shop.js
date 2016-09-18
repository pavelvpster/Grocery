$(function() {
    $(".nav").find(".active").removeClass("active");
    $(".nav").find("#shop").parent().addClass("active");

    $.get("/shop/list", {}, function(result) {
        $("#shop-list-container").html(result);
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
    $.get("/shop/list?page=" + page, {}, function(result) {
        $("#shop-list-container").html(result);
        updatePaginationControls();
    });
}

function showCreateShopForm() {
    $.get("/shop/form", {}, function(result) {
        $("#shop-form-container").html(result);
        $("#shop-form").modal();
    });
}

function showUpdateShopForm(id) {
    $.get("/shop/form/" + id, {}, function(result) {
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
        success: function(result) {
            window.location = "/shop/";
        }
    });
}

function createVisit(shopId) {
    $.post("/api/v1/visit/shop/" + shopId, {}, function(result) {
        window.location = "/visit/";
    });
}
