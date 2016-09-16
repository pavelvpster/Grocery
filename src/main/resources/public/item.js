$(function() {

    $(".nav").find(".active").removeClass("active");
    $(".nav").find("#item").parent().addClass("active");

    $.get("/item/list", {}, function(result) {
        $("#item-list-container").html(result);
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
    $.get("/item/list?page=" + page, {}, function(result) {
        $("#item-list-container").html(result);
        updatePaginationControls();
    });
}

function showCreateItemForm() {
    $.get("/item/form", {}, function(result) {
        $("#item-form-container").html(result);
        $("#item-form").modal();
    });
}

function showUpdateItemForm(id) {
    $.get("/item/form/" + id, {}, function(result) {
        $("#item-form-container").html(result);
        $("#item-form").modal();
    });
}

function deleteItem(id) {
    $.ajax({
        type: 'DELETE',
        url: "/item/" + id,
        success: function(result) {
            window.location = "/item/";
        }
    });
}
