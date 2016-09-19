/*
 * ShopWebController.java
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

import org.interactiverobotics.grocery.domain.Shop;
import org.interactiverobotics.grocery.form.ShopForm;
import org.interactiverobotics.grocery.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Shop web controller.
 */
@Controller
@RequestMapping("/shop")
public class ShopWebController {

    private final ShopService shopService;

    /**
     * Parametrized constructor.
     */
    @Autowired
    public ShopWebController(final ShopService shopService) {
        this.shopService = shopService;
    }

    @RequestMapping("/")
    public String index() {
        return "shop";
    }

    /**
     * Returns HTML block with list of Shops.
     */
    @RequestMapping("/list")
    public String getShops(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                           @RequestParam(value = "size", defaultValue = "10") Integer pageSize, Model model) {

        final PageRequest pageRequest = new PageRequest(pageNumber - 1, pageSize);
        final Page<Shop> page = this.shopService.getShops(pageRequest);

        final List<Shop> shops = new ArrayList<>();
        page.forEach(shop -> shops.add(shop));

        model.addAttribute("offset", 1 + (pageNumber - 1) * pageSize);
        model.addAttribute("shops", shops);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", pageNumber);

        return "shop_list";
    }

    /**
     * Returns create Shop form.
     */
    @RequestMapping("/form")
    public String getCreateShopForm(Model model) {
        model.addAttribute("shop", new Shop());
        return "shop_form";
    }

    /**
     * Creates Shop.
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String createShop(@Valid ShopForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/shop/";
        }
        this.shopService.createShop(form);
        return "redirect:/shop/";
    }

    /**
     * Returns update Shop form (with current field values).
     */
    @RequestMapping("/{id}/form")
    public String getUpdateShopForm(@PathVariable Long id, Model model) {
        final Shop shop = this.shopService.getShopById(id);
        model.addAttribute("shop", shop);
        return "shop_form";
    }

    /**
     * Updates Shop.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String updateShop(@PathVariable Long id, @Valid ShopForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/shop/";
        }
        this.shopService.updateShop(id, form);
        return "redirect:/shop/";
    }

    /**
     * Deletes Shop.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deleteShop(@PathVariable Long id) {
        this.shopService.deleteShop(id);
        return "redirect:/shop/";
    }

}
