$(function() {

    $(".nav-item").find(".active").removeClass("active");
    $(".nav-item").find("#shopping-list").parent().addClass("active");

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
    return true;
}

function submitShoppingListItemCreateForm() {
    if (!validateShoppingListItemCreateForm) {
        return false;
    }
    submitForm();
    return false;
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
    return true;
}

function submitShoppingListItemUpdateForm() {
    if (!validateShoppingListItemUpdateForm()) {
        return false;
    }
    submitForm();
    return false;
}

function submitForm() {
    $.ajax({
        type: 'POST',
        url: $("#shopping-list-item-properties").attr("action"),
        data: JSON.stringify($("#shopping-list-item-properties").serializeObject()),
        contentType: "application/json",
        success: function(result) {
            window.location = "/shopping_list_item/" + getShoppingListId();
        },
        fail: function(result) {
            alert("Error create/update shopping list item!");
        }
    });
}

function deleteShoppingListItem(id) {
    $.ajax({
        type: 'DELETE',
        url: "api/v1/shopping_list_item/" + id,
        success: function(result) {
            window.location = "/shopping_list_item/" + getShoppingListId();
        }
    });
}
