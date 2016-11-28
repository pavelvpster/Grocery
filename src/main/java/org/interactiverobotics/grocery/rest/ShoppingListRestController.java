/*
 * ShoppingListRestController.java
 *
 * Copyright (C) 2016 Pavel Prokhorov (pavelvpster@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.interactiverobotics.grocery.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.form.ShoppingListForm;
import org.interactiverobotics.grocery.service.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "Get page of ShoppingLists", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Page<ShoppingList> getShoppingListsPage(
            @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
            @RequestParam(value = "size", defaultValue = "10") Integer pageSize) {

        final PageRequest pageRequest = new PageRequest(pageNumber - 1, pageSize);
        return this.shoppingListService.getShoppingLists(pageRequest);
    }

    @ApiOperation(value = "Get ShoppingList by Id", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShoppingList getShoppingListById(@PathVariable Long id) {
        return this.shoppingListService.getShoppingListById(id);
    }

    @ApiOperation(value = "Get ShoppingList by Name", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShoppingList getShoppingListByName(@RequestParam(value = "name") String name) {
        return this.shoppingListService.getShoppingListByName(name);
    }

    @ApiOperation(value = "Create ShoppingList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShoppingList createShoppingList(@RequestBody ShoppingListForm form) {
        return this.shoppingListService.createShoppingList(form);
    }

    @ApiOperation(value = "Update ShoppingList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShoppingList updateShoppingList(@PathVariable Long id, @RequestBody ShoppingListForm form) {
        return this.shoppingListService.updateShoppingList(id, form);
    }

    @ApiOperation(value = "Delete ShoppingList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void deleteShoppingList(@PathVariable Long id) {
        this.shoppingListService.deleteShoppingList(id);
    }

}
