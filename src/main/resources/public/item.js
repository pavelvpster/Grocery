$(function() {

    $(".nav").find(".active").removeClass("active");
    $(".nav").find("#item").parent().addClass("active");

    $.get("/item/list", {}, function(result) {
        $("#item-list-container").html(result);
    });
});

function gotoPage(page) {
    $.get("/item/list?page=" + page, {}, function(result) {
        $("#item-list-container").html(result);
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
