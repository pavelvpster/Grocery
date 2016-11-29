$(function() {

    $(".nav").find(".active").removeClass("active");
    $(".nav").find("#shopping-list").parent().addClass("active");

    initializePagination("/shopping_list_item/" + getShoppingListId() + "/list", "#shopping-list-item-list-container");
});

function getShoppingListId() {
    return parseInt($("#shopping-list-id").val(), 10);
}

function showCreateShoppingListItemForm() {
    $.get("/shopping_list_item/form?shoppingList=" + getShoppingListId(), {}, function(result) {
        $("#shopping-list-item-form-container").html(result);
        $("#shopping-list-item-form").modal();
    });
}

function showUpdateShoppingListItemForm(id) {
    $.get("/shopping_list_item/" + id + "/form", {}, function(result) {
        $("#shopping-list-item-form-container").html(result);
        $("#shopping-list-item-form").modal();
    });
}

function validateShoppingListItemCreateForm() {
    var itemString = $("#shopping-list-item-properties #item").val();
    if (itemString == null) {
        $("#item-error").text("Item must be selected!");
        return false;
    }
    var quantityString = $("#shopping-list-item-properties #quantity").val();
    if (quantityString == null) {
        $("#quantity-error").text("Quantity must not be empty!");
        return false;
    }
    var quantity = parseInt(quantityString, 10);
    if (quantity < 1) {
        $("#quantity-error").text("Quantity must be > 0!");
        return false;
    }
}

function validateShoppingListItemUpdateForm() {
    var quantityString = $("#shopping-list-item-properties #quantity").val();
    if (quantityString == null) {
        $("#quantity-error").text("Quantity must not be empty!");
        return false;
    }
    var quantity = parseInt(quantityString, 10);
    if (quantity < 1) {
        $("#quantity-error").text("Quantity must be > 0!");
        return false;
    }
}

function deleteShoppingListItem(id) {
    $.ajax({
        type: 'DELETE',
        url: "/shopping_list_item/" + id,
        success: function(result) {
            window.location = "/shopping_list_item/" + getShoppingListId();
        }
    });
}
