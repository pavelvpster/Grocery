package org.interactiverobotics.grocery.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.service.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ShoppingList REST controller.
 */
@Api(value = "ShoppingList", description = "Shopping list management endpoint")
@RestController
@RequestMapping(value = "/api/v1/shopping_list")
public class ShoppingListRestController {

    private final ShoppingListService shoppingListService;

    @Autowired
    public ShoppingListRestController(final ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    @ApiOperation(value = "Get all ShoppingList(s)", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ShoppingList> getShoppingLists() {
        return this.shoppingListService.getShoppingLists();
    }

}
