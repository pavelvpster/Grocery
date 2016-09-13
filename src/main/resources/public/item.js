$(function() {

    $(".nav").find(".active").removeClass("active");
    $(".nav").find("#item").parent().addClass("active");

    $.get("/item/list", {}, function(result) {
        $("#item_list").html(result);
    });
});

function gotoPage(page) {
    $.get("/item/list?page=" + page, {}, function(result) {
        $("#item_list").html(result);
    });
}
