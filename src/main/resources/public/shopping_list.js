$(function() {

    $(".nav-item").find(".active").removeClass("active");
    $(".nav-item").find("#shopping-list").parent().addClass("active");

    initializePagination("/shopping_list/list", "#shopping-list-list-container");
});

function showCreateShoppingListForm() {
    $.get("/shopping_list/form", {}, function(result) {
        $("#shopping-list-form-container").html(result);
        $("#shopping-list-form").modal();
    });
}

function showUpdateShoppingListForm(id) {
    $.get("/shopping_list/" + id + "/form", {}, function(result) {
        $("#shopping-list-form-container").html(result);
        $("#shopping-list-form").modal();
    });
}

function validateShoppingListForm() {
    var name = $("#shopping-list-properties #name").val();
    if (name == null || name.length < 1 || name.length > 255) {
        $("#name-error").text("Name must be 1 to 255 characters.");
        return false;
    }
}

function deleteShoppingList(id) {
    $.ajax({
        type: 'DELETE',
        url: "/shopping_list/" + id,
        success: function(result) {
            window.location = "/shopping_list/";
        }
    });
}

function gotoShoppingListItems(shoppingListId) {
    window.location = "/shopping_list_item/" + shoppingListId;
}
