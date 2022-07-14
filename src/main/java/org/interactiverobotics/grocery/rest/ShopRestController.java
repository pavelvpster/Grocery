/*
 * ShopRestController.java
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
import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.form.ShopForm;
import org.interactiverobotics.grocery.service.ShopService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Shop REST controller.
 */
@AllArgsConstructor
@Api(value = "Shop")
@RestController
@RequestMapping(value = "/api/v1/shop")
public class ShopRestController {

    private final ShopService shopService;

    @ApiOperation(value = "Get all Shop(s)", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Shop> getShops() {
        return this.shopService.getShops();
    }

    @ApiOperation(value = "Get page of Shops", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<Shop> getShopsPage(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                                   @RequestParam(value = "size", defaultValue = "10") Integer pageSize) {
        return this.shopService.getShops(PageRequest.of(pageNumber - 1, pageSize));
    }

    @ApiOperation(value = "Get Shop by Id", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Shop getShopById(@PathVariable Long id) {
        return this.shopService.getShopById(id);
    }

    @ApiOperation(value = "Get Shop by Name", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Shop getShopByName(@RequestParam(value = "name") String name) {
        return this.shopService.getShopByName(name);
    }

    @ApiOperation(value = "Create Shop", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Shop createShop(@RequestBody ShopForm form) {
        return this.shopService.createShop(form);
    }

    @ApiOperation(value = "Update Shop", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Shop updateShop(@PathVariable Long id, @RequestBody ShopForm form) {
        return this.shopService.updateShop(id, form);
    }

    @ApiOperation(value = "Delete Shop", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteShop(@PathVariable Long id) {
        this.shopService.deleteShop(id);
    }
}
