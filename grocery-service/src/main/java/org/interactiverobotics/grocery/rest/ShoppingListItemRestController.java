/*
 * ShoppingListItemRestController.java
 *
 * Copyright (C) 2016-2022 Pavel Prokhorov (pavelvpster@gmail.com)
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
import lombok.AllArgsConstructor;
import org.interactiverobotics.grocery.domain.ShoppingListItem;
import org.interactiverobotics.grocery.form.ShoppingListItemCreateForm;
import org.interactiverobotics.grocery.form.ShoppingListItemUpdateForm;
import org.interactiverobotics.grocery.service.ShoppingListItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ShoppingListItem REST controller.
 */
@AllArgsConstructor
@Api(value = "ShoppingListItem")
@RestController
@RequestMapping(value = "/api/v1/shopping_list_item")
public class ShoppingListItemRestController {

    private final ShoppingListItemService shoppingListItemService;

    @ApiOperation(value = "Get all ShoppingListItem(s)", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{shoppingListId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ShoppingListItem> getShoppingListItems(@PathVariable Long shoppingListId) {
        return this.shoppingListItemService.getShoppingListItems(shoppingListId);
    }

    @ApiOperation(value = "Get page of ShoppingListItems", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{shoppingListId}/list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ShoppingListItem> getShoppingListItemsPage(
            @PathVariable Long shoppingListId,
            @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
            @RequestParam(value = "size", defaultValue = "10") Integer pageSize) {
        return this.shoppingListItemService
                .getShoppingListItems(PageRequest.of(pageNumber - 1, pageSize), shoppingListId);
    }

    @ApiOperation(value = "Create ShoppingListItem", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ShoppingListItem createShoppingListItem(@RequestBody ShoppingListItemCreateForm form) {
        return this.shoppingListItemService.createShoppingListItem(form);
    }

    @ApiOperation(value = "Update ShoppingListItem", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ShoppingListItem updateShoppingListItem(@PathVariable Long id,
                                                   @RequestBody ShoppingListItemUpdateForm form) {
        return this.shoppingListItemService.updateShoppingListItem(id, form);
    }

    @ApiOperation(value = "Delete ShoppingListItem", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteShoppingListItem(@PathVariable Long id) {
        this.shoppingListItemService.deleteShoppingListItem(id);
    }
}