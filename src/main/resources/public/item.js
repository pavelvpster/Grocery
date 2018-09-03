$(function() {

    $(".nav-item").find(".active").removeClass("active");
    $(".nav-item").find("#item").parent().addClass("active");

    initializePagination("/item/list", "#item-list-container");
});

function showCreateItemForm() {
    $.get("/item/form", {}, function(result) {
        $("#item-form-container").html(result);
        $("#item-form").modal();
    });
}

function showUpdateItemForm(id) {
    $.get("/item/" + id + "/form", {}, function(result) {
        $("#item-form-container").html(result);
        $("#item-form").modal();
    });
}

function validateItemForm() {
    var name = $("#item-properties #name").val();
    if (name == null || name.length < 1 || name.length > 255) {
        $("#name-error").text("Name must be 1 to 255 characters.");
        return false;
    }
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
