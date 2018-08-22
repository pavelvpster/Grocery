/*
 * PurchaseRestController.java
 *
 * Copyright (C) 2016-2018 Pavel Prokhorov (pavelvpster@gmail.com)
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
import org.interactiverobotics.grocery.domain.Purchase;
import org.interactiverobotics.grocery.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Purchase REST controller.
 */
@Api(value = "Purchase", description = "Buy/return endpoint")
@RestController
@RequestMapping(value = "/api/v1/purchase")
public class PurchaseRestController {

    private final PurchaseService purchaseService;

    @Autowired
    public PurchaseRestController(final PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @ApiOperation(value = "Get Items that not existing in Purchases", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{visitId}/not_purchased_items", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Item> getNotPurchasedItems(@PathVariable Long visitId) {
        return this.purchaseService.getNotPurchasedItems(visitId);
    }

    @ApiOperation(value = "Get page of Purchases", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{visitId}/list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Page<Purchase> getPurchasesPage(@PathVariable Long visitId,
                                           @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                                           @RequestParam(value = "size", defaultValue = "10") Integer pageSize) {
        return this.purchaseService.getPurchases(PageRequest.of(pageNumber - 1, pageSize), visitId);
    }

    @ApiOperation(value = "Buy Item in Visit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{visitId}/buy/{itemId}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Purchase buyItem(@PathVariable Long visitId, @PathVariable Long itemId,
                            @RequestParam(value = "quantity") Long quantity,
                            @RequestParam(value = "price", required = false) BigDecimal price) {
        return this.purchaseService.buyItem(visitId, itemId, quantity, price);
    }

    @ApiOperation(value = "Return Item in Visit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{visitId}/return/{itemId}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Purchase returnItem(@PathVariable Long visitId, @PathVariable Long itemId, @RequestParam Long quantity) {
        return this.purchaseService.returnItem(visitId, itemId, quantity);
    }

    @ApiOperation(value = "Update purchase's Price", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/{visitId}/price/{itemId}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Purchase updatePrice(@PathVariable Long visitId, @PathVariable Long itemId, @RequestParam BigDecimal price) {
        return this.purchaseService.updatePrice(visitId, itemId, price);
    }
}
