/*
 * ShoppingListWebController.java
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

package org.interactiverobotics.grocery.web;

import org.interactiverobotics.grocery.domain.ShoppingList;
import org.interactiverobotics.grocery.form.ShoppingListForm;
import org.interactiverobotics.grocery.service.ShoppingListService;
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
 * ShoppingList web controller.
 */
@Controller
@RequestMapping("/shopping_list")
public class ShoppingListWebController {

    private static final String REDIRECT_TO_SHOPPING_LIST = "redirect:/shopping_list/";

    private final ShoppingListService shoppingListService;

    /**
     * Parametrized constructor.
     */
    @Autowired
    public ShoppingListWebController(final ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    /**
     * Returns index page.
     */
    @RequestMapping("/")
    public String index() {
        return "shopping_list";
    }

    /**
     * Returns HTML block with list of ShoppingLists.
     */
    @RequestMapping("/list")
    public String getShoppingLists(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                                   @RequestParam(value = "size", defaultValue = "10") Integer pageSize, Model model) {

        final Page<ShoppingList> page = this.shoppingListService.getShoppingLists(PageRequest.of(pageNumber - 1, pageSize));

        final List<ShoppingList> shoppingLists = new ArrayList<>();
        page.forEach(shoppingList -> shoppingLists.add(shoppingList));

        model.addAttribute("offset", 1 + (pageNumber - 1) * pageSize);
        model.addAttribute("shoppingLists", shoppingLists);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", pageNumber);

        return "shopping_list_list";
    }

    /**
     * Returns create ShoppingList form.
     */
    @RequestMapping("/form")
    public String getCreateShoppingListForm(Model model) {
        model.addAttribute("shoppingList", new ShoppingList());
        return "shopping_list_form";
    }

    /**
     * Creates ShoppingList.
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String createShoppingList(@Valid ShoppingListForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return REDIRECT_TO_SHOPPING_LIST;
        }
        this.shoppingListService.createShoppingList(form);
        return REDIRECT_TO_SHOPPING_LIST;
    }

    /**
     * Returns update ShoppingList form (with current field values).
     */
    @RequestMapping("/{id}/form")
    public String getUpdateShoppingListForm(@PathVariable Long id, Model model) {
        final ShoppingList shoppingList = this.shoppingListService.getShoppingListById(id);
        model.addAttribute("shoppingList", shoppingList);
        return "shopping_list_form";
    }

    /**
     * Updates ShoppingList.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String updateShoppingList(@PathVariable Long id, @Valid ShoppingListForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return REDIRECT_TO_SHOPPING_LIST;
        }
        this.shoppingListService.updateShoppingList(id, form);
        return REDIRECT_TO_SHOPPING_LIST;
    }

    /**
     * Deletes ShoppingList.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deleteShoppingList(@PathVariable Long id) {
        this.shoppingListService.deleteShoppingList(id);
        return REDIRECT_TO_SHOPPING_LIST;
    }
}
