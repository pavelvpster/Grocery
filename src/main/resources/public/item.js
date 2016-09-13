$(function() {
    $(".nav").find(".active").removeClass("active");
    $(".nav").find("#item").parent().addClass("active");
});
