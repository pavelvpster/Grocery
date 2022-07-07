/*
 * ItemRestController.java
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
import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.form.ItemForm;
import org.interactiverobotics.grocery.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Item REST controller.
 */
@Api(value = "Item")
@RestController
@RequestMapping(value = "/api/v1/item")
public class ItemRestController {

    private final ItemService itemService;

    @Autowired
    public ItemRestController(final ItemService itemService) {
        this.itemService = itemService;
    }

    @ApiOperation(value = "Get all Item(s)", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Item> getItems() {
        return this.itemService.getItems();
    }

    @ApiOperation(value = "Get page of Items", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<Item> getItemsPage(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                                   @RequestParam(value = "size", defaultValue = "10") Integer pageSize) {
        return this.itemService.getItems(PageRequest.of(pageNumber - 1, pageSize));
    }

    @ApiOperation(value = "Get Item by Id", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Item getItemById(@PathVariable Long id) {
        return this.itemService.getItemById(id);
    }

    @ApiOperation(value = "Get Item by Name", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Item getItemByName(@RequestParam(value = "name") String name) {
        return this.itemService.getItemByName(name);
    }

    @ApiOperation(value = "Create Item", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Item createItem(@RequestBody ItemForm form) {
        return this.itemService.createItem(form);
    }

    @ApiOperation(value = "Update Item", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Item updateItem(@PathVariable Long id, @RequestBody ItemForm form) {
        return this.itemService.updateItem(id, form);
    }

    @ApiOperation(value = "Delete Item", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteItem(@PathVariable Long id) {
        this.itemService.deleteItem(id);
    }
}
