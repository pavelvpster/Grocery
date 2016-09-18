/*
 * PurchaseWebController.java
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

package org.interactiverobotics.grocery.web;

import org.interactiverobotics.grocery.domain.Item;
import org.interactiverobotics.grocery.domain.Purchase;
import org.interactiverobotics.grocery.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Purchase web controller.
 */
@Controller
@RequestMapping("/purchase")
public class PurchaseWebController {

    private final PurchaseService purchaseService;

    @Autowired
    public PurchaseWebController(final PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @RequestMapping("/")
    public String index() {
        return "purchase_select_visit";
    }

    @RequestMapping("/{visitId}")
    public String index(@PathVariable Long visitId, Model model) {
        model.addAttribute("visitId", visitId);
        return "purchase";
    }

    @RequestMapping("/item_selector/{visitId}")
    public String getNotPurchasedItems(@PathVariable Long visitId, Model model) {
        final List<Item> items = this.purchaseService.getNotPurchasedItems(visitId);
        model.addAttribute("items", items);
        return "purchase_item_selector";
    }

    @RequestMapping("/list/{visitId}")
    public String getPurchases(@PathVariable Long visitId,
                               @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                               @RequestParam(value = "size", defaultValue = "10") Integer pageSize, Model model) {

        final PageRequest pageRequest = new PageRequest(pageNumber - 1, pageSize);
        final Page<Purchase> page = this.purchaseService.getPurchases(pageRequest, visitId);

        final List<Purchase> purchases = new ArrayList<>();
        page.forEach(purchase -> purchases.add(purchase));

        model.addAttribute("offset", 1 + (pageNumber - 1) * pageSize);
        model.addAttribute("purchases", purchases);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", pageNumber);

        return "purchase_list";
    }

}
